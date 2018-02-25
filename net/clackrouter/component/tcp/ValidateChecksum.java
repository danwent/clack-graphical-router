
package net.clackrouter.component.tcp;

import java.net.InetAddress;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;


/**
 * TCP Subcomponent used to validate the checksum of an incoming TCP segment.  
 * 
 * <p> Segments with valid checksums are sent to one output port, and packets
 * with invalid checksums are sent to the other. </p> 
 */
public class ValidateChecksum extends ClackComponent {

    public static int PORT_IP_IN = 0, PORT_IP_INVALID = 1, PORT_TCP_VALID  = 2, NUM_PORTS = 3;

	private TCB mTCB;
	
	public ValidateChecksum(TCB tcb, Router router, String name){
		super(router, name);
		setupPorts(NUM_PORTS);
		mTCB = tcb;
	}
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input   = "incoming IP packets";
        String invalid = "packets with invalid checksum fields";
        String valid = "packets with valid checksum fields";

        m_ports[PORT_IP_IN]   = new ClackPort(this, PORT_IP_IN,  input, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_IP_INVALID] = new ClackPort(this, PORT_IP_INVALID, invalid, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_TCP_VALID]   = new ClackPort(this, PORT_TCP_VALID, valid, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
 
    } // -- setupPorts
    
    public void acceptPacket(VNSPacket packet, int port_num){
    	if(port_num == PORT_IP_IN){
    		
			IPPacket ippacket = (IPPacket) packet;
			if(!(ippacket.getHeader().getProtocol() == IPPacket.PROTO_TCP)) {
				error("TCPChecksum received non-TCP packet");
				return;
			}
			try {
				log("Packet from " + InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array())
					+ " to " + InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array()));
			} catch (Exception e){
				e.printStackTrace();
			}
			if(!TCPChecksum.verifyTCPChecksum(ippacket)){
				System.err.println("TCP Checksum verification failed");
				m_ports[PORT_IP_INVALID].pushOut(ippacket);
				return;
			}
			try {
				VNSTCPPacket tcppacket = new VNSTCPPacket(ippacket.getBodyBuffer());
				tcppacket.setSourceIPAddress(InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array()));
				tcppacket.setDestIPAddress(InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array()));
				mTCB.fireListeners(new TCP.UpdateEvent(TCP.UpdateEvent.EVENT_PACKET_IN, tcppacket));
				m_ports[PORT_TCP_VALID].pushOut(tcppacket);
			}catch (Exception e){
				e.printStackTrace();
			}
			
		}else {
			System.err.println("TCPChecksum received packet on invalid port # " + port_num);
		}
    }
}
