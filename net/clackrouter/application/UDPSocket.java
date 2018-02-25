
package net.clackrouter.application;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import net.clackrouter.component.extension.UDP;
import net.clackrouter.packets.VNSUDPPacket;


/**
 * A socket implementing the UDP protocol for a Clack application.
 * 
 * <p> Unlike a {@link TCPSocket}, a UDPSocket is not a ClackComponent and 
 * simple receives datagrams via the {@link #addDatagramToQueue(VNSUDPPacket)} method
 * and sends datagrams by directly accessing the {@link UDP} component. </p>
 */
public class UDPSocket  {
	
	private ArrayList mDatagrams;
	private UDP mParent;
	private int mPort;
	private InetAddress mAddress;
	private String mName;
	
	public UDPSocket(String name, UDP parent){
		mName = name;
		mParent = parent;
		mDatagrams = new ArrayList();
	}
	
	public void addDatagramToQueue(VNSUDPPacket packet){ mDatagrams.add(packet);}
	
	/**
	 * Send a datagram to the specified address and port
	 * @param buf data to be sent
	 * @param info destination address and port information
	 */
	public void sendTo(ByteBuffer buf, HostInfo info){
		
		VNSUDPPacket udp = new VNSUDPPacket(mPort, info.port, buf);
		udp.setDestinationAddress(info.address);
		udp.setSourceAddress(mAddress);
		udp.pack();
		mParent.sendPacket(udp);
		System.out.println("Sending UDP packet of " + udp.getByteBuffer().capacity() + 
				" byte to: " + info.address + " port " + info.port);
	}
	
	/**
	 * Default bind behavior selects a random port in the range between 1024 and 2024
	 * and binds to the first listed interface on the router (often eth0)
	 * @throws Exception
	 */
	public void bind() throws Exception {
		Random rand = new Random();
		int port = rand.nextInt(1000) + 1024;
		bind(mParent.getRouter().getInputInterfaces()[0].getIPAddress(), port);
	}
	
	/**
	 * Binds the socket to a specified address and port.
	 * 
	 * <p> At this time, no restrictions are placed on what addresses or ports 
	 * may be bound to.  
	 * 
	 * @param local_address address to bind as source 
	 * @param local_port port to bind as source
	 * @throws Exception
	 */
	public void bind(InetAddress local_address, int local_port)throws Exception {
		mParent.addMapping(local_address, local_port, this);
		mAddress = local_address;
		mPort = local_port;
	}
	
	/**
	 *  Receive a UDP datagram 
	 * @param msec_timeout number of milliseconds before the call returns
	 * @param info upon return, contains information about datagram source address and port
	 * @return buffer of data, or an empty buffer if timeout
	 */
	public ByteBuffer recvFrom(long msec_timeout, HostInfo info) { 
		for(int i = 0; i < 10; i++){
			
			if(mDatagrams.size() > 0){
				VNSUDPPacket packet =  (VNSUDPPacket) mDatagrams.remove(0);
				info.address = packet.getSourceAddress();
				info.port = packet.getSourcePort();
				return packet.getBodyBuffer();
			}
			try{
				Thread.sleep(msec_timeout / 10);
			} catch(Exception e) { /* ignore */ }
		}
		
		// return empty byte buffer
		return ByteBuffer.allocate(0);	
	}
	
	/**
	 * the socket's name
	 */
	public String getName() { return mName; }
	
	/**
	 * Simple structure to tell the source address and port of a
	 * received datagram
	 */
	public static class HostInfo {
		public InetAddress address;
		public int port;
	}
	

}
