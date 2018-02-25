
package net.clackrouter.component.extension;


import java.net.InetAddress;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.DNSPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.router.core.Router;


/**
 * <p> A fun little component that hijacks DNS requests being forwarded
 * by the IP layer and sends back false replies. </p>
 */
public class DNSHijacker extends ClackComponent {

	public static int PORT_IN = 0, PORT_OUT = 1, NUM_PORTS = 2;
	private String original_domain;
	private InetAddress hijack_address;
	
	public static String DEFAULT_TARGET = "www.google.com";
	public static String DEFAULT_FALSE_ADDRESS = "10.101.1.11"; // garbage
	
	public DNSHijacker(Router r, String name){
		this(r, name, DEFAULT_TARGET, DEFAULT_FALSE_ADDRESS);
	}
	
	public DNSHijacker(Router r, String name, String target, String falseAddress){
		super(r, name);
		setupPorts(NUM_PORTS);
		original_domain = target;
		try{
			hijack_address = InetAddress.getByName(falseAddress);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets a new address to hijack, and false IP
	 * @param toHijack the domain name to hijack
	 * @param toReply the false IP address with which to reply
	 */
	public void setHijackPair(String toHijack, InetAddress toReply) {
		original_domain = toHijack;
		hijack_address = toReply;
	}

    protected void setupPorts(int numPorts)
    {
    	super.setupPorts(numPorts);
        String in_desc = "hijacker input";
        String out_desc = "hijacker output";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
    } // -- setupPorts
    
    /**
     * Extract the packet and see if its UDP, and then DNS.  If it is, check if their is a query
     * for the target DNS name we are trying to hijack.  If there is, we create a DNS answer and
     * add it to the original packet, modify the flags, flip the IP addresses and pass it on to 
     * be forwarded.
     */
    public void acceptPacket(VNSPacket packet, int port_number)
    {
        try {
        	IPPacket ippacket = (IPPacket)packet;
        	
        	if(ippacket.getHeader().getProtocol() == IPPacket.PROTO_UDP){
        		VNSUDPPacket udppacket = new VNSUDPPacket(ippacket.getBodyBuffer());
        		if(udppacket.dst_port == DNSPacket.DNS_PORT){
        			
        			// udp packet being sent to DNS.  Check each Query Record
        			DNSPacket dnspacket = new DNSPacket(udppacket.getBodyBuffer());
        			for(int i = 0; i < dnspacket.getQueryRecordCount(); i++){
        				DNSPacket.Query query = dnspacket.getQueryRecord(i);
        				
        				if(query.name.equals(original_domain)){

        					DNSPacket.Answer answer = new DNSPacket.Answer(query.name,
        							(short)DNSPacket.TYPE_A, (short)DNSPacket.CLASS_INET, 1000, hijack_address.getAddress());
        					dnspacket.addAnswerRecord(answer);
        					dnspacket.setFlags((short)DNSPacket.FLAGS_QUERY_RESPONSE_NO_ERROR);
        					
        					// encapsulate in UDP, then IP, swapping ports and IPs
        					VNSUDPPacket returnUDPPacket = new VNSUDPPacket(DNSPacket.DNS_PORT, 
        							udppacket.src_port, dnspacket.getByteBuffer());
        					IPPacket returnIPPacket = IPPacket.wrap(returnUDPPacket, IPPacket.PROTO_UDP, 
        							ippacket.getHeader().getDestinationAddress(), ippacket.getHeader().getSourceAddress());
        					
        					// send spoofed packet, return without forwarding original request
        					m_ports[PORT_OUT].pushOut(returnIPPacket);
        					return; 
        				}
        					
        			}
        		}
        	}
        } catch (Exception e){
        	e.printStackTrace();
        }
        // forward not hijacked packets
    	m_ports[PORT_OUT].pushOut(packet);
    } // -- acceptPacket
    
    public boolean isModifying() { return false; }    
}
