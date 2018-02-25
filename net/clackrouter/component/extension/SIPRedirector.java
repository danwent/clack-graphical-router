package net.clackrouter.component.extension;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.router.core.Router;

public class SIPRedirector extends ClackComponent {
	public static final int PORT_FROM_NET = 0, PORT_TO_NET = 1, NUM_PORTS = 2;
	
	public static final String STUN_SERVER_NAME = "gs5025.sp.cs.cmu.edu";
	public static final int STUN_CLACKSHARE_PORT = 9999;
	
	InetAddress remote_clack_addr; /* address we want the other SIP client to think packets are coming from */
	InetAddress remote_real_addr;  /* address of the other SIP client */
	
	public SIPRedirector(Router router, String name){
		super(router, name);
		setupPorts(NUM_PORTS);
		
		try {
			InetAddress local_clack_addr = getRouter().getInputInterfaces()[0].getIPAddress();
			DatagramSocket toStun = new DatagramSocket();
			byte[] data = local_clack_addr.getAddress();
			toStun.send(new DatagramPacket(data, data.length, InetAddress.getByName(STUN_SERVER_NAME), STUN_CLACKSHARE_PORT));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void setupPorts(int numPorts){
		super.setupPorts(numPorts);
        m_ports[PORT_FROM_NET] = new ClackPort(this, PORT_FROM_NET, "Incoming SIP packets", ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_TO_NET] = new ClackPort(this, PORT_TO_NET, "Outgoing SIP packets", ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
	}
	
	public void acceptPacket(VNSPacket packet, int port_number){
		if(port_number == PORT_FROM_NET){
			try {
				IPPacket in_ippacket = (IPPacket)packet;
				
				// send same UDP packet with same source/dest ports, but spoofing the source to appear to come from 
				// the remote clients clack topo, and setting the source to go to the remote client
				VNSUDPPacket udp = new VNSUDPPacket(in_ippacket.getBodyBuffer());	
				IPPacket out_ippacket = IPPacket.wrap(udp, IPPacket.PROTO_UDP,
						ByteBuffer.wrap(remote_clack_addr.getAddress()), ByteBuffer.wrap(remote_real_addr.getAddress()));
					m_ports[PORT_TO_NET].pushOut(out_ippacket);
					
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
	

}
