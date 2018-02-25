/*
 * Created on Jun 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.component.simplerouter;

import java.awt.Color;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;


/**
 * Encapsulates a Level 3 packet in IP. 
 * 
 * <p> This method is not yet complete, and all it does for 
 * now is pass IP packets on, assuming that they have been encapsulated by the L3 stack. 
 * Need to refactor the TCP and UDP stacks so they do not perform the encapsulation. </p>
 */

public class IPEncap extends ClackComponent {
    public static int PORT_IN = 0; 
    public static int PORT_OUT     = 1; 
    public static int NUM_PORTS  = 2;

    public IPEncap(Router router, String name)
    {
    	super(router, name);
        setupPorts(NUM_PORTS);
    }
    
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input_desc = "Incoming Level3 Packet";
        String output_desc  = "Packet encapsulated in IP";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, input_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT] = new ClackPort(this, PORT_OUT, output_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);		
    } // -- setupPorts

    public void acceptPacket(VNSPacket packet, int port_number) 
    {
    	// dummy method for now, just pass packet on
        m_ports[PORT_OUT].pushOut(packet);

    } // -- acceptPacket
    
    public Color getColor() { return new Color(255, 128,128); }  // strip/encap group color
}
