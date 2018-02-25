package net.clackrouter.component.simplerouter;


import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.IPRouteLookupPview;
import net.clackrouter.router.core.Router;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;

/**
Performs route lookup for an IP packet, identifying local packets, setting the output
interface for forwarding, or dropping the packet.

@author martin
*/

public class IPRouteLookup extends ClackComponent  
{

    public static int PORT_IP_IN    = 0;
    public static int PORT_NOROUTE  = 1; 
    public static int PORT_LOCAL    = 2; 
    public static int PORT_OUT = 3;
    public static int NUM_PORTS = 4;

    protected RoutingTable mRtable = null;  
    protected Hashtable mPortsToInterface = null;// Map outgoing port number to interface name

    public IPRouteLookup(Router router, String name)
    {
    	super(router, name);
        mPortsToInterface = new Hashtable();
        mRtable = new RoutingTable(this);

        mRouter.setIPRouteLookup(this);
        setupPorts(NUM_PORTS);

    } // -- LookupIPRoute(..)


    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input   = "incoming IP packets";
        String noroute = "packets without valid routes";
        String localpackets = "packets addressed to one of our interfaces";
        String forwarded = "non-local packets to be forwaded";

        m_ports[PORT_IP_IN]   = new ClackPort(this, PORT_IP_IN,  input, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_NOROUTE] = new ClackPort(this, PORT_NOROUTE, noroute, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_LOCAL]   = new ClackPort(this, PORT_LOCAL, localpackets, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_OUT] = new ClackPort(this, PORT_OUT, forwarded, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
      
    } // -- setupPorts

    /**
     * Accept IP packet and either forward, drop, or pass on for local processing.
     */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {

        // -- Double check we get the packet from a valid port 
        if (port_number != PORT_IP_IN)
        {  error( "Received packet on invalid port: " + port_number); return; }

        IPPacket ippacket = (IPPacket)packet;
        
        if(isForUs(ippacket)){
        	m_ports[PORT_LOCAL].pushOut(packet);
        	return;
        }
        
        boolean success = ipForward(ippacket);
        if(!success){
        	try {
        		byte type = ippacket.getHeader().getProtocol();
        		InetAddress dest = InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array());
        		error("No route found for packet of protocol-type " + type
        				+ " to dest: " + dest.getHostAddress());
        		//m_ports[PORT_NOROUTE].pushOut(packet);
        	}catch (Exception e) {e.printStackTrace(); }
        }

    } // -- acceptPacket


    protected boolean isForUs(IPPacket egg)
    {
        InterfaceIn[] interfaces = mRouter.getInputInterfaces();
        InetAddress ip_dest = null;

        try{
            ip_dest = 
                InetAddress.getByAddress(egg.getHeader().getDestinationAddress().array());    	
        }catch(UnknownHostException e) {
            System.err.println(e);
            e.printStackTrace();
            return false;
        }

        for ( int i = 0 ; i < interfaces.length ; ++i )
        {
            if ( interfaces[i].getIPAddress().equals(ip_dest) ){ 
            	// local packet
            	if(egg.needsSourceAddress()){
            		egg.getHeader().setSrcAddr(ByteBuffer.wrap(interfaces[i].getIPAddress().getAddress()));
            	}
            	return true; 
            }
        }

        return false;
    } // -- isForUs

    /**
     * Finds the correct source address for a given destination.
     * 
     * <p> Used in the case of "local packets" such as ICMP messages that
     * need their source address set according to the interface they are 
     * being forwarded out of. </p>
     * @param dest destination IP address of the packet
     * @return
     */
    protected InetAddress getSourceFromRtable(InetAddress dest)
    {
        RoutingEntry match = mRtable.longestPrefixMatch(dest);
        if ( match == null ) { 
        	error("null match for source on routing table");
            return null; 
        }
        Interface iface = mRouter.getInterfaceByName(match.getInterface());
        if ( iface == null )  {
        	signalError();
            error("Routing table entry has non-existant interface");
            return null;
        }

        return iface.getIPAddress();
    }

    public RoutingTable getRoutingTable() {    return mRtable;   }
    public void setRoutingTable(RoutingTable table) { mRtable = table; }

    /**
     * Internal function used to forward a packet, either sending it to the local output port, dropping it,
     * or forwarding it via an output port. 
     * @param packet
     * @return
     */
    protected boolean ipForward(IPPacket packet) {
    	try{


    		InetAddress dest = 
                InetAddress.getByAddress(packet.getHeader().getDestinationAddress().array());		
            RoutingEntry match = mRtable.longestPrefixMatch(dest);

            if ( match == null ) {   	return false;   }
            
        	match.last_access = getRouter().getTimeManager().getTimeMillis();
        	
    		// -- check to see if we need to set the source address
    		if ( packet.needsSourceAddress() )  {
    			InetAddress srcaddy = getSourceFromRtable(
                    NetUtils.ByteBuffer2Inet(packet.getHeader().getDestinationAddress()));
    			packet.getHeader().setSrcAddr(NetUtils.Inet2ByteBuffer(srcaddy));
    			packet.pack();
    			packet.getHeader().setChecksum(packet.getHeader().calculateChecksum());
    			packet.pack();
    		}
    		if(match.getInterface() == null) {
    			// this is a null route, drop the packet
    			return true; 
    		}
            // -- forward
            packet.setOutputInterfaceName(match.getInterface());
            
            if(match.getNextHop() != null){
            	// set next-hop adddress as look-up's next-hop
            	packet.setNextHopIPAddress(ByteBuffer.wrap(match.getNextHop().getAddress()));
            }else {
            	// we're directly connected, so set dest as next-hop address
            	packet.setNextHopIPAddress(packet.getHeader().getDestinationAddress());
            }
            	
            	

  //           System.out.println(mRouter.getHost() + " forwarding packet to host :" +match.getNextHop().getHostAddress() +
  //                              " on interface "+match.getInterface());

            getRouter().getDocument().redrawTopoView();
            m_ports[PORT_OUT].pushOut(packet);
            return true;
            
        }catch(Exception e) { 
        	error(e.toString()); 
        	e.printStackTrace();
        	return false; 
        }


    } // -- ipForward
    
    public JPanel getPropertiesView(){ return new IPRouteLookupPview(this);   }
    
    public Color getColor() { return new Color(130, 255, 154); }
    
    protected void debug_addr_print(IPPacket ippacket){
        try {
        	String src = InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array()).getHostAddress();
        	String dest = InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array()).getHostAddress();
        	log("FROM: " + src + " TO: " + dest);
        } catch (Exception e){
        	e.printStackTrace();
        }
    }
    
    /** Load and static entries that have been hard-coded into the topo file */
	public void initializeProperties(Properties props) {
		for(Enumeration e = props.keys(); e.hasMoreElements(); ){
			String key = (String)e.nextElement();
			String value = props.getProperty(key);
			if(key.indexOf("entry") != -1){
				try{ 
					String[] chunks = value.split(":");
					if(chunks.length != 4) continue;
					
					InetAddress dest = InetAddress.getByName(chunks[0]);
					InetAddress next_hop = null;
					if(!chunks[1].equals("*"))
						next_hop = InetAddress.getByName(chunks[1]);
					InetAddress mask = InetAddress.getByName(chunks[2]);
					String eth_name = chunks[3];
					
					RoutingEntry entry = new RoutingEntry(dest,next_hop,mask,eth_name);
					getRoutingTable().addEntry(entry);
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}
	
    /** Serialize the routing table state for reloading the router */
    public Properties getSerializableProperties(boolean isTransient){
    	Properties props = super.getSerializableProperties(isTransient);
    	if(!isTransient) return props;
    	
    	// only serialize routing table state for transient "save all"
    	RoutingTable rt = getRoutingTable();
    	for(int i = 0; i < rt.numEntries(); i++){
    		RoutingEntry entry = rt.getEntry(i);
    		String nh = (entry.nextHop == null) ? "*" : entry.nextHop.getHostAddress();
    		String val = entry.network.getHostAddress() + ":" + nh + ":" + entry.mask.getHostAddress() +
    					":" + entry.getInterface();
    		props.setProperty("entry" + i, val);
    	}

    	return props;
    }
    
  
} // -- class LookupIPRoute
