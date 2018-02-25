
package net.clackrouter.component.simplerouter;


import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.InterfacePopup;
import net.clackrouter.protocol.data.VNSHWInfo;
import net.clackrouter.router.core.Router;
import net.clackrouter.topology.core.TopologyModel;



/**
 * Represents an input interface, a FromDevice(ethX) component,  in a Clack router.  
 */
public class InterfaceIn extends Interface {
	public static int PORT_TO_INTERNAL = 0, PORT_FROM_ROUTER = 1, NUM_PORTS = 2;
	private boolean isPromiscuous = false; 
	
	public InterfaceIn(String name, Router router, VNSHWInfo.InterfaceEntry entry){
		super(name, router, entry);
        setupPorts(NUM_PORTS);
	}
	
	public InterfaceIn(String name, Router router, TopologyModel.Interface entry){
		super(name, router, entry);
		setupPorts(NUM_PORTS);
	}

    protected void setupPorts(int numports) {
    	super.setupPorts(numports);
        String intern_out_desc = "Port sending all packets to an internal component";
        String from_router_desc = "Port accepting packets from the router";
        m_ports[PORT_TO_INTERNAL] = new ClackPort(this, PORT_TO_INTERNAL, intern_out_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSEthernetPacket.class);
        m_ports[PORT_FROM_ROUTER] = new ClackPort(this, PORT_FROM_ROUTER, from_router_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSEthernetPacket.class);
    }
    
    /** Not a valid method for InterfaceIn, which intead communicates directly with the router */
    public void acceptPacket(VNSPacket packet, int port_number) {
        try{
        	VNSEthernetPacket eth_packet = (VNSEthernetPacket) packet;
        	
        	/* turning this off for shipment
        	 try {
        	 TcpdumpWriter.appendVNSEthernetPacket(getDeviceName() + ".pcap", e_packet);
        	 } catch (Exception e){
        	 e.printStackTrace();
        	 }
        	 */
        	
        	ByteBuffer dest_addr = eth_packet.getDestinationAddressBuffer();
        	EthernetAddress incoming_eth_addr = EthernetAddress.getByAddress(
        			dest_addr.array());
        	
        	boolean isHWAddrMatch = incoming_eth_addr.equals(m_eth_addr);
        	boolean isBroadcast = incoming_eth_addr.equals(ETH_BCAST_ADDR);  
        	if(isHWAddrMatch || isBroadcast || isPromiscuous){
        		packet.setInputInterfaceName(device_name); 
        		m_ports[PORT_TO_INTERNAL].pushOut(packet);    
               }	
           }catch(UnknownHostException e) {
               e.printStackTrace();
           }
    }
    
	public JPanel getPropertiesView() {
		return new InterfacePopup(this, InterfacePopup.INTERFACE_IN);
	}
	

	public void initializeProperties(Properties props) {
		String p = (String) props.get("promiscuous");
		if(p != null) { 
			this.isPromiscuous = p.equals("true"); 
		}
	}

    public Properties getSerializableProperties(boolean isTransient){
    	Properties props = super.getSerializableProperties(isTransient);
    	if(!isTransient) return props;
    	
    	props.setProperty("promiscuous", "" + this.isPromiscuous);
    	return props;
    }
	
}
