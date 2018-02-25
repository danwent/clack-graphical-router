
package net.clackrouter.component.simplerouter;

import java.awt.Color;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;


/**
 * Component to strip the IP header off of a packet.  
 * 
 * <p> Like the IPEncap class, this is not implemented yet, and only
 * a dummy class exists that simply passes the packet on. </p>
 */
public class IPStrip extends ClackComponent {
    public static int PORT_IN = 0; 
    public static int PORT_OUT     = 1; 
    public static int NUM_PORTS  = 2;

    public IPStrip(Router router, String name) {
    	super(router, name);
        setupPorts(NUM_PORTS);
    }
    
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input_desc = "Incoming IP Packet";
        String output_desc  = "Packets with IP Header Stripped";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, input_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_OUT]     = new ClackPort(this, PORT_OUT, output_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);		
    } // -- setupPorts

    /**
     * Dummy method right now, simply passes the packet on to the output port with no 
     * modification.
     */
    public void acceptPacket(VNSPacket packet, int port_number)  {
        m_ports[PORT_OUT].pushOut(packet);

    } // -- acceptPacket
    
    public Color getColor() { return new Color(255, 128,128); }  // strip/encap group color
}
