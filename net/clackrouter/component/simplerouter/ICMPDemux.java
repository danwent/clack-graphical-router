
package net.clackrouter.component.simplerouter;

import java.awt.Color;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSICMPPacket;
import net.clackrouter.packets.VNSICMPType;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;



/**
 * Demultiplexes a subset of ICMP types to different output ports.
 * 
 * <p> Currently demultiplexed are the following types: Echo Request, Destination Unreachable,
 * Echo Reply, and TTL-Exceeded.  An additional output takes all other ICMP types.  </p>
 */
public class ICMPDemux extends ClackComponent {

    public static int ICMP_IN    = 0;
    public static int ECHO_REQUEST_OUT   = 1;
    public static int UNREACH_OUT = 2;
    public static int ECHO_REPLY_OUT = 3;
    public static int TTL_EXCEEDED_OUT = 4;
    public static int OTHER_OUT = 5;
	public static int NUM_PORTS  = 6;

    public ICMPDemux(Router router, String name) 
    { 
    	super(router, name); 
    	setupPorts(NUM_PORTS);
    }

	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
		String icmp_in_descr    = "all incoming ICMP packets";
		String echo_request_out_descr   = "ICMP echo request packets";
		String unreach_out_descr = "ICMP dest. unreachable packets";
		String echo_reply_out_descr = "ICMP echo reply packets";
		String ttl_exceeded_out_descr = "TTL exceeded packets";
		String other_out_descr = "unsupported ICMP packets";

		m_ports[ICMP_IN]    = new ClackPort(this, ICMP_IN,  icmp_in_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
		m_ports[ECHO_REQUEST_OUT]   = new ClackPort(this, ECHO_REQUEST_OUT, echo_request_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSICMPPacket.class);
		m_ports[UNREACH_OUT] = new ClackPort(this, UNREACH_OUT, unreach_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSICMPPacket.class);
		m_ports[ECHO_REPLY_OUT] = new ClackPort(this, ECHO_REPLY_OUT, echo_reply_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSICMPPacket.class);
		m_ports[TTL_EXCEEDED_OUT] = new ClackPort(this, TTL_EXCEEDED_OUT, ttl_exceeded_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSICMPPacket.class);
		m_ports[OTHER_OUT] = new ClackPort(this, OTHER_OUT, other_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT,VNSICMPPacket.class);
	}
	
	/**
	 * Checks the TYPE field in the ICMP header and sends the packet to the corrresponding port.  
	 */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {

        // Double check we get the packet from a valid port 
        if (port_number != ICMP_IN)
        { 	
        	error( "Received packet on invalid port: " + port_number);
        	return;
        }

        IPPacket ippacket = (IPPacket)packet;

        // -- ICMP only please 
        if(ippacket.getHeader().getProtocol() !=  IPPacket.PROTO_ICMP)
        {  
        	error("Received non-icmp packet"); 
        	return;
        }

        VNSICMPPacket icmppacket = new VNSICMPPacket(ippacket.getBodyBuffer());
        icmppacket.setParentHeader(ippacket);
        
        if (icmppacket.getType().getValue() == VNSICMPType.ECHO_REQUEST
        		|| icmppacket.getType().getValue() == VNSICMPType.ECHO_REPLY){  
        	m_ports[ECHO_REQUEST_OUT].pushOut(icmppacket);
        }else if (icmppacket.getType().getValue() == VNSICMPType.UNREACH){  
           	m_ports[UNREACH_OUT].pushOut(icmppacket);
        }else if (icmppacket.getType().getValue() == VNSICMPType.ECHO_REPLY){  
             m_ports[ECHO_REPLY_OUT].pushOut(icmppacket);
        }else if (icmppacket.getType().getValue() == VNSICMPType.TIME_EXCEEDED){  
          	m_ports[TTL_EXCEEDED_OUT].pushOut(icmppacket);
        }else{ 
        	signalError();
        	error("Received unsupport local ICMP of type " + icmppacket.getType());
        	m_ports[OTHER_OUT].pushOut(icmppacket); 
        }

    } // -- acceptPacket
    

    public Color getColor() { return new Color(255, 190, 133); } // ICMP group color
}
