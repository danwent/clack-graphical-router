package net.clackrouter.component.simplerouter;

import java.awt.Color;
import java.io.Serializable;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;

/**
 * Decrements the Time-to-Live field of the IP header for packets
 * being forwarded through this router.  
 * 
 * <p> If the TTL value is greater than zero after decrement
 * the packet is sent out one port to be forwarded, otherwise it is
 * sent out a different port to generate an ICMP TTL-exceeded message. </p>
 *
 * @author  Martin Casado 
 */

public class IPTTLDec extends ClackComponent implements Cloneable, Serializable
{
    public static int PORT_IN   = 0;
    public static int PORT_OUT  = 1;
	public static int TTL_XCD   = 2;
    public static int NUM_PORTS = 3;

    public IPTTLDec(Router router, String name) {
        super(router, name);
        setupPorts(NUM_PORTS);
    }
    

	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
		String PORT_IN_descr   = "incoming IP packets";
		String PORT_OUT_descr  = "outgoing IP packets with decremented TTL";
		String TTL_XCD_descr   = "packets with TTL less than 1 after decrement";

		m_ports[PORT_IN]   = new ClackPort(this, PORT_IN,  PORT_IN_descr,  ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
		m_ports[PORT_OUT]  = new ClackPort(this, PORT_OUT, PORT_OUT_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
		m_ports[TTL_XCD]   = new ClackPort(this, TTL_XCD,  TTL_XCD_descr,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
	}

    public void acceptPacket(VNSPacket packet, int port_number) 
    {

        // Double check we get the packet from a valid port 
        if (port_number != PORT_IN)
        { error( "Received packet on invalid port: " + port_number); return; }

        IPPacket ippacket = (IPPacket)packet;

        if ( ttlDecrement(ippacket) ) {  
        	signalError();
        	error("IP TTL Exceeded, dropping packet..."); 
            m_ports[TTL_XCD].pushOut(ippacket);
            return;
        }

        m_ports[PORT_OUT].pushOut(ippacket);

    } // -- acceptPacket

    private boolean ttlDecrement(IPPacket packet)
    {
        if ( packet.getHeader().getTTL() < 2 ){ log("TTL exceeded"); return true; } // -- end time exceeded

        packet.getHeader().setTTL((byte)(packet.getHeader().getTTL() - 1));
        packet.pack();
        packet.getHeader().setChecksum(packet.getHeader().calculateChecksum());
        packet.pack();

        return false;

    } // -- ttlDecrement(..)
    
    public Color getColor() { return new Color(130, 255, 154); }
    
} // -- class DecIPTTL
