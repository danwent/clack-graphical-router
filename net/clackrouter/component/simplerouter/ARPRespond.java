
package net.clackrouter.component.simplerouter;

import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;



/**
 * Component to respond to ARP Request packets sent to one of the router's interfaces.
 */
public class ARPRespond extends ClackComponent {

    public static int PORT_REQUEST_IN = 0;
    public static int PORT_OUT = 1;
    public static int NUM_PORTS = 2;

    public ARPRespond(Router router, String name)  {
        super(router, name);
        setupPorts(NUM_PORTS);
    }

    protected void setupPorts(int num_ports){
    	super.setupPorts(num_ports);
        String request_in_desc = "Incoming ARP Request packets";
        String out_desc = "Outgoing ARP reply packets";

        m_ports[PORT_REQUEST_IN] = new ClackPort(this, PORT_REQUEST_IN, request_in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSARPPacket.class);
        m_ports[PORT_OUT] = new ClackPort(this, PORT_OUT, out_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSARPPacket.class);

    } // -- setupPorts
    
   
    /**
     * Assures the packet is a valid request, then if the IP matches the address
     * of the interface the ARP request was received on, we send a reply.
     */

    public void acceptPacket(VNSPacket packet, int port_number)
    {
        if ( port_number == PORT_REQUEST_IN ){ 
        	
            // -- grab the incoming infterface from the router
            String iface_name = packet.getInputInterfaceName(); 
            Interface iface = mRouter.getInterfaceByName(iface_name);
            if ( iface == null )  {
            	error("No associated interface found for packet from '" + iface_name + "'"); 
            }

            VNSARPPacket arp_packet = (VNSARPPacket) packet;
            if(arp_packet.getOperation() == VNSARPPacket.OPERATION_REQUEST)  {
          
                try{
                	InetAddress   iface_ip_address = iface.getIPAddress(); 
                	InetAddress   dest_ip_address = 
                        InetAddress.getByAddress(arp_packet.getDestinationProtocolAddress().array());    	
                   
                    if(iface_ip_address.equals(dest_ip_address)) {
                        ByteBuffer src_ip_address = arp_packet.getSourceProtocolAddress();
                        ByteBuffer src_hw_address = arp_packet.getSourceHWAddress();
                        sendARPReply(src_hw_address, src_ip_address, iface);
                    }	
                }catch(UnknownHostException e) {
                    e.printStackTrace();
                }
            } else {
                error("ARPRespond received non Request ARP packet");
            }     	
        }
        else  
        { error( "Received packet on invalid port: " + port_number); }

    } // -- acceptPacket	


    /** Create and Send an ARP reply to the specified dest_hw and IP address, out the specified interface */
    private void sendARPReply(ByteBuffer dest_hw, ByteBuffer dest_ip, Interface iface)  {


        ByteBuffer arp_type = ByteBuffer.allocate(VNSEthernetPacket.TYPE_LEN);
        arp_type.putShort(VNSEthernetPacket.TYPE_ARP);
        
        VNSARPPacket reply_packet = new VNSARPPacket(VNSARPPacket.HW_TYPE_ETHERNET,
                VNSARPPacket.PROTOCOL_TYPE_IP,
                (byte)VNSARPPacket.HW_ADDR_LEN,
                (byte)VNSARPPacket.PROTOCOL_ADDR_LEN,
                VNSARPPacket.OPERATION_REPLY,
                ByteBuffer.wrap(iface.getMACAddress().getAddress()),
                ByteBuffer.wrap(iface.getIPAddress().getAddress()),
                dest_hw,
                dest_ip);
        
        	dest_hw.rewind();
        	reply_packet.setNextHopMacAddress(dest_hw);
        	reply_packet.setLevel2Type(arp_type);
            reply_packet.setOutputInterfaceName(iface.getDeviceName());
            
            m_ports[PORT_OUT].pushOut(reply_packet);

    } // -- sendARPReply

    public Color getColor() { return new Color(255, 254, 167); }// ARP group color
}
