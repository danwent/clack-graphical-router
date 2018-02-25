
package net.clackrouter.component.simplerouter;

import java.awt.Color;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;


/**
 * Strips off the Ethernet header. 
 */
public class EtherStrip extends ClackComponent {
	
    public static int PORT_IN = 0; 
    public static int PORT_OUT     = 1; 
    public static int NUM_PORTS  = 2;

    public EtherStrip(Router router, String name)
    {
    	super(router, name);
        setupPorts(NUM_PORTS);
    }
    
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input_desc = "Incoming Ethernet Packet";
        String output_desc  = "Packets with Ethernet Header Stripped";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, input_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSEthernetPacket.class);
        m_ports[PORT_OUT]     = new ClackPort(this, PORT_OUT, output_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);		
    } // -- setupPorts

    /**
     * Removes the Ethernet header, and stores the input interface using the 
     * @see VNSPacket#setInputInterfaceName(String) method.
     * 
     * <p> Both ARP and IP packets are sent to the same output port. </p>
     */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {
    	
        VNSEthernetPacket e_packet = (VNSEthernetPacket) packet;

        short ethertype = e_packet.getType();
        if(ethertype == VNSEthernetPacket.TYPE_ARP)
        {
            VNSARPPacket arppacket = new VNSARPPacket(e_packet.getBodyBuffer());
            arppacket.setInputInterfaceName(e_packet.getInputInterfaceName());
            m_ports[PORT_OUT].pushOut(arppacket);
        }else if(ethertype == VNSEthernetPacket.TYPE_IP)
        {
            IPPacket ippacket = new IPPacket(e_packet.getBodyBuffer());
            ippacket.setInputInterfaceName(e_packet.getInputInterfaceName());
            m_ports[PORT_OUT].pushOut(ippacket);

        }


    } // -- acceptPacket
    
    public Color getColor() { return new Color(139,105,20); }  // strip/encap group color
}
