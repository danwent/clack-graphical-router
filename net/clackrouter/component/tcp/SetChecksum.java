/*
 * Created on Feb 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.component.tcp;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;


/**
 * Subcomponent to set the checksum of a TCP header.  
 */
public class SetChecksum extends ClackComponent {
	
    public static int PORT_TCP_IN    = 0;
    public static int PORT_IP_OUT    = 1; 
	public static int NUM_PORTS = 2;
	private TCB mTCB;
	
	public SetChecksum(TCB tcb, Router router, String name){
		super(router, name);
		setupPorts(NUM_PORTS);
		mTCB = tcb;
	}
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input   = "TCP packets needing checksum";
        String output = "IP encapsulated packets with checksum set";

        m_ports[PORT_TCP_IN]   = new ClackPort(this, PORT_TCP_IN,  input, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSTCPPacket.class);
        m_ports[PORT_IP_OUT] = new ClackPort(this, PORT_IP_OUT, output, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
       
      
    } // -- setupPorts
    
    public void acceptPacket(VNSPacket packet, int port_num){
    	if(port_num == PORT_TCP_IN){
    		if(!(packet instanceof VNSTCPPacket)) {
				System.err.println("TCPChecksum received non-TCP Packet from TCP stack");
				return;
			}	
			VNSTCPPacket tcppacket = (VNSTCPPacket)packet;
			tcppacket.pack();
	        try {
	        	IPPacket ipEncap = IPPacket.wrap(tcppacket,
	                    IPPacket.PROTO_TCP,
	                    ByteBuffer.wrap(tcppacket.getSourceIPAddress().getAddress()), // src
	                    ByteBuffer.wrap(tcppacket.getDestIPAddress().getAddress())); // Dst
				TCPChecksum.setTCPChecksum(ipEncap);
				log("Packet from " + InetAddress.getByAddress(ipEncap.getHeader().getSourceAddress().array())
						+ " to " + InetAddress.getByAddress(ipEncap.getHeader().getDestinationAddress().array()));
				mTCB.fireListeners(new TCP.UpdateEvent(TCP.UpdateEvent.EVENT_PACKET_OUT, tcppacket));
				m_ports[PORT_IP_OUT].pushOut(ipEncap);
	        }catch (Exception e){
	        	e.printStackTrace();
	        }
    	}
    }
}
