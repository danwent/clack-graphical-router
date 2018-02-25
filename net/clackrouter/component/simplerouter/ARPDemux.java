
package net.clackrouter.component.simplerouter;


import java.awt.Color;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;



/**
 * Demultiplexes between ARP Requests and ARP Reply Packets
 */
public class ARPDemux extends ClackComponent {

    public static int PORT_IN = 0;
    public static int REPLY_OUT  = 1;
    public static int REQUEST_OUT    = 2;
    public static int NUM_PORTS = 3;


    public ARPDemux(Router r, String name) 
    {
        super(r, name);
        setupPorts(NUM_PORTS);
    }
    
    protected void setupPorts(int num_ports)
    {
    	super.setupPorts(num_ports);
        String in_desc = "Incoming ARP packets";
        String reply_out_desc  = "ARP Reply Packets";
        String request_out_desc    = "ARP Request Packets";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSARPPacket.class);
        m_ports[REPLY_OUT] =  new ClackPort(this, REPLY_OUT,  reply_out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSARPPacket.class);
        m_ports[REQUEST_OUT] = new ClackPort(this, REQUEST_OUT, request_out_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSARPPacket.class);

    } // -- setupPorts

    /**
     * Accepts packets that are either ARP requests or ARP replies, and sends them to different output ports.
     */

    public void acceptPacket(VNSPacket packet, int port_number)
    {
        
        if ( port_number == PORT_IN )
        { 
            VNSARPPacket arp_packet = (VNSARPPacket)packet;
            
            if(arp_packet.getOperation() == VNSARPPacket.OPERATION_REQUEST) 
            {
                m_ports[REQUEST_OUT].pushOut(arp_packet);
            } else if(arp_packet.getOperation() == VNSARPPacket.OPERATION_REPLY){
            
                m_ports[REPLY_OUT].pushOut(arp_packet);
            }else {
            	error("Recieved ARP packet with unknow operation = " + arp_packet.getOperation());
            }
        }
        else
        { error( "Received packet on invalid port: " + port_number); }

    } // -- acceptPacket	

    public Color getColor() { return new Color(255, 254, 167); } // ARP group color

}
