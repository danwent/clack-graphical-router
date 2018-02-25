package net.clackrouter.component.simplerouter;


import java.awt.Color;
import java.io.Serializable;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.packets.VNSICMPCode;
import net.clackrouter.packets.VNSICMPPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;


/**
 * Generates an ICMP TTL-Exceeded message when the router decrements
 * the TTL of a forwarded packet to zero.
 */

public class ICMPTTLExpired extends ClackComponent implements Serializable
{
    public static int IN        = 0;
    public static int OUT       = 1;
    public static int NUM_PORTS = 2;

    public ICMPTTLExpired(Router router, String name)
    {
        super(router, name);
        setupPorts(NUM_PORTS);
    } // -- ICMPTTLExpired

    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String in_descr    = "IP Packets with TTL expired";
        String out_descr   = "ICMP TTL-expired packets";

        m_ports[IN]     = new ClackPort(this, IN,   in_descr,  ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[OUT]    = new ClackPort(this, OUT,  out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
    } // -- setupPorts

    /**
     * Accepts an expired IP packet and generates an ICMP TTL-Exceeded message
     * with the IP packet as data.  
     */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {
        try{
        	IPPacket ippacket = (IPPacket)packet;
        	VNSICMPPacket icmptimex =
                VNSICMPPacket.wrapTimeExceeded(   VNSICMPCode.TimeExceededInTransit, ippacket);

        	IPPacket newIPPacket = IPPacket.wrap(icmptimex,
                        IPPacket.PROTO_ICMP,
                        NetUtils.Inet2ByteBuffer("0.0.0.0"),    // src
                        ippacket.getHeader().getSourceAddress()); // dst

            newIPPacket.setNeedsSourceAddress(true);

            m_ports[OUT].pushOut(newIPPacket);
        }  catch (Exception e) 
        { System.err.println(e); e.printStackTrace(); return; }


    } // -- acceptPacket
    
    public Color getColor() { return new Color(255, 190, 133); } // ICMP group color
    
} // -- class ICMPTTLExpired
