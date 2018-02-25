package net.clackrouter.component.simplerouter;



import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.netutils.ICMPListener;
import net.clackrouter.packets.VNSICMPPacket;
import net.clackrouter.packets.VNSICMPType;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;



/**
 * Class to reply to ICMP Echo requests to any local interface. 
 */
public class ICMPEcho extends ClackComponent implements Cloneable, Serializable
{
    public static int ECHO_IN    = 0;
    public static int ECHO_OUT   = 1;
	public static int NUM_PORTS  = 2;
	private ArrayList listeners;
    
    public ICMPEcho(Router router, String name)
    {
        super(router, name);
        setupPorts(NUM_PORTS);
        listeners = new ArrayList();
    }

	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
		String echo_in_descr    = "incoming ICMP echo requests/replies";
		String echo_out_descr   = "outgoing ICMP echo requests/replies";

		m_ports[ECHO_IN]    = new ClackPort(this, ECHO_IN,  echo_in_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSICMPPacket.class);
		m_ports[ECHO_OUT]   = new ClackPort(this, ECHO_OUT, echo_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
	}

	/** 
	 * Processing Echo request and replies to the source address with an Echo Reply packet.
	 */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {

        // Double check we get the packet from a valid port 
        if (port_number != ECHO_IN) {  
        	error( "Received packet on invalid port: " + port_number);
        	return;
        }

        VNSICMPPacket icmppacket = (VNSICMPPacket)packet;

        if (icmppacket.getType().getValue() == VNSICMPType.ECHO_REQUEST) { 
        	handleEchoRequest(icmppacket); 
        } else if(icmppacket.getType().getValue() == VNSICMPType.ECHO_REPLY){
        	handleEchoReply(icmppacket);
        } else{
        	error(" ** ICMPEchoResponder received non echo packet");
        }

    } // -- acceptPacket

    private void handleEchoRequest(VNSICMPPacket echo_request)
    {   
        IPPacket   old_ippacket = (IPPacket)echo_request.getParentHeader();

        try{
        	 VNSICMPPacket icmpecho =    VNSICMPPacket.wrapEchoReply( echo_request );

            // RFC1812 states that on echo reply simply switch the source
            // and destination addresses in the IP header
        
            sendPacket(icmpecho, old_ippacket.getHeader().getDestinationAddress(), 
                    old_ippacket.getHeader().getSourceAddress(), false); 
            
        	Point echo_point = VNSICMPPacket.parseEcho(icmpecho);
        	
        	int identifier = echo_point.x;
        	int seq_num = echo_point.y;
         	// fire listeners
     	    for(int i = 0; i < listeners.size(); i++){
     	    	((ICMPListener)listeners.get(i)).receivedEchoRequest(identifier, seq_num);
     	    }

        }catch(Exception e) { 
        	e.printStackTrace();
        }
        
    } // -- handleEcho 
    
    /**
     * Parse data field of an echo reply, then fire listeners to alert listening applications
     * @param echo_reply
     */
    private void handleEchoReply(VNSICMPPacket echo_reply){
    	Point echo_point = VNSICMPPacket.parseEcho(echo_reply);
    	int identifier = echo_point.x;
    	int seq_num = echo_point.y;
	    
    	// fire listeners
	    for(int i = 0; i < listeners.size(); i++){
	    	((ICMPListener)listeners.get(i)).receivedEchoReply(identifier, seq_num);
	    }
    }
    
    public void sendEchoRequest(InetAddress dest, int identifier, int seq_num){
    	try {
    		ByteBuffer fake = ByteBuffer.allocate(4);
    		VNSICMPPacket icmpecho = VNSICMPPacket.wrapEcho(VNSICMPType.ECHO_REQUEST, identifier, seq_num);
    		
    		sendPacket(icmpecho, fake, ByteBuffer.wrap(dest.getAddress()), true /*route must set src */);
    	} catch (Exception e){
    		e.printStackTrace();
    		error(e.getMessage());
    	}
    }
    
    private void sendPacket(VNSICMPPacket packet, ByteBuffer src, ByteBuffer dst, boolean isLocal) throws Exception {
        IPPacket newIPPacket = IPPacket.wrap(packet,
                IPPacket.PROTO_ICMP, src, dst); // Dst
        newIPPacket.setNeedsSourceAddress(isLocal);
        m_ports[ECHO_OUT].pushOut(newIPPacket); 	
    }
    
    public Color getColor() { return new Color(255, 190, 133); } // ICMP group color
    
    public void addListener(ICMPListener l) { listeners.add(l); }
    public void removeListener(ICMPListener l) { listeners.remove(l); }
    
} // -- class ICMPEchoResponder
