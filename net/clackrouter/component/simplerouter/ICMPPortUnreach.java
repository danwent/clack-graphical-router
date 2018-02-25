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
 * 
 * Generates ICMP port-unreachable messages when a local TCP or UDP 
 * stack provides it with an IP packet sent to an invalid local port.  
 **/
public class ICMPPortUnreach extends ClackComponent implements Serializable
{
    public static int PORT_IN = 0, PORT_OUT = 1, NUM_PORTS = 2;


    public ICMPPortUnreach(Router router, String name)
    {
        super(router, name);
        setupPorts(NUM_PORTS);
    } // -- ICMPPortUnreach

    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String in_descr    = "incoming TCP/UDP  packets encapsulated in IP";
        String out_descr   = "ICMP port unreach packets";

        m_ports[PORT_IN]     = new ClackPort(this, PORT_IN,   in_descr,  ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_OUT]    = new ClackPort(this, PORT_OUT,  out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
    } // -- setupPorts

    /***
     * Accepts IP packets that were received by the local TCP or UDP stacks and that
     * do not have any receiving socket bound on that port.  Generates a port-unreachable
     * ICMP packet and sends routes it back to the source address of the invalid packet.  
     */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {
        IPPacket ippacket = (IPPacket)packet;
        if ( ippacket == null )
        { error("Received non-IP packet in ICMPPortUnreach"); return; }

        if (ippacket.getHeader().getProtocol() != IPPacket.PROTO_TCP &&
                ippacket.getHeader().getProtocol() != IPPacket.PROTO_UDP)
        { error("ICMPPortUnreach packet not UDP or TCP"); return; }

        // -- generate PORT unreach packet
        VNSICMPPacket icmp = null;

        try {
            icmp = 
                VNSICMPPacket.wrapUnreach(VNSICMPCode.UnreachPort,  ippacket);

            IPPacket newpacket = null;

            newpacket =
                IPPacket.wrap(icmp,
                        IPPacket.PROTO_ICMP,
                        NetUtils.Inet2ByteBuffer("0.0.0.0"),     // src
                        ippacket.getHeader().getSourceAddress());// dst
            
            // -- let the forwarding component know that this is a local packet.
            //    so it will set the source address
            newpacket.setNeedsSourceAddress(true);

            m_ports[PORT_OUT].pushOut(newpacket);
            
        }catch(Exception e) {
            error(e.toString());
            return;
        }

    } // -- acceptPacket
    
    public Color getColor() { return new Color(255, 190, 133); } // ICMP group color
    
} // -- class ICMPPortUnreach
