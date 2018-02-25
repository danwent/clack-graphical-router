
package net.clackrouter.component.extension;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import net.clackrouter.application.UDPSocket;
import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.router.core.Router;


/**
 * Simplified implementation of the User Datagram Protocol (UDP).
 * 
 * <p> Takes care of UDP's main functions of performing a checksum and 
 *  demultiplexing packets by port </p>
 */
public class UDP extends ClackComponent {

	
	public static final int PORT_FROM_NET = 0, PORT_TO_NET = 1, PORT_UNREACH = 2, NUM_PORTS = 3;
	
	private Hashtable socketMap; // maps from IP+port to UDPSocket
	
	public UDP(Router router, String name){
		super(router, name);
		router.setUDPStack(this);
		socketMap = new Hashtable();
		setupPorts(NUM_PORTS);
	}
	
	
	public void setupPorts(int numPorts){
		super.setupPorts(numPorts);
        m_ports[PORT_FROM_NET] = new ClackPort(this, PORT_FROM_NET, "Incoming UDP", ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_TO_NET] = new ClackPort(this, PORT_TO_NET, "Outgoing UDP", ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_UNREACH] = new ClackPort(this, PORT_TO_NET, "Packets to Unreachable Ports", ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
	}
	
	public void acceptPacket(VNSPacket packet, int port_number){
		if(port_number == PORT_FROM_NET){
			try {
				IPPacket ippacket = (IPPacket)packet;
				VNSUDPPacket udp = new VNSUDPPacket(ippacket.getBodyBuffer());
				InetAddress destAddr = InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array());
				int destPort = udp.getDestinationPort();
				UDPSocket sock = (UDPSocket)socketMap.get(hashPair(destAddr, destPort));
				if(sock != null){
					InetAddress sourceAddress = InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array());
					udp.setSourceAddress(sourceAddress);
					udp.setDestinationAddress(destAddr);
			  //      System.out.println("Recieved UDP packet with header checksum of " + udp.checksum + " sums to " + (~(udp.checksum & 0xffff) & 0xffff));
			  //      System.out.println("We calculate a checksum of " + udp.calculateChecksum());
					sock.addDatagramToQueue(udp);
				}else {
		        	error("Packet to unused UDP port " + destPort); 
					m_ports[PORT_UNREACH].pushOut(packet);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Interface for packets to be send by UDP sockets.  
	 * @param packet
	 */
	public void sendPacket(VNSUDPPacket packet){
		try {
			IPPacket ippacket = IPPacket.wrap(packet, IPPacket.PROTO_UDP,
				ByteBuffer.wrap(packet.getSourceAddress().getAddress()), ByteBuffer.wrap(packet.getDestinationAddress().getAddress()));
			m_ports[PORT_TO_NET].pushOut(ippacket);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a local address / port binding to the UDP stack
	 * @param addr
	 * @param port
	 * @param sock
	 * @throws Exception if another socket is already bound to this IP / UDP port pair 
	 */
	public void addMapping(InetAddress addr, int port, UDPSocket sock)throws Exception {
		String key = hashPair(addr, port);
		if(socketMap.get(key) != null) throw new Exception("UDP port " + port + " already bound on address " + addr.getHostAddress());
		socketMap.put(key, sock);
	}
	
	private String hashPair(InetAddress addr, int port){ return addr.getHostAddress() + ":" + port; }
}
