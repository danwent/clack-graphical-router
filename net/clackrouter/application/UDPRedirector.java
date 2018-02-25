
package net.clackrouter.application;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;


/**
 * A Clack application that uses a Clack host to proxy to a real Internet host, thus redirecting the UDP traffic.
 * 
 * <p>  The redirect effect is achieved using two sockets.  One is a socket running on the clack router and another
 * is a real Java socket connected to the host that the redirector is proxying.  Any data received in the Clack socket
 * is sent to the real host using the Java socket, and any data received using the java socket is sent back to the 
 * client using the Clack socket.  This effectively allows us to pretend like a real-host is inside our Clack network.  </p>
 */
public class UDPRedirector extends ClackApplication {
	
	public void application_main(String[] args){
		
		if(args.length != 5) {
			print("usage: udpproxy  <orig address> <orig port> <proxied address> <proxied port>\n");
			return;
		}
		
		try {
			InetAddress local_addr = InetAddress.getByName(args[1]);
			int local_port = Integer.parseInt(args[2]);
			InetAddress proxied_addr = InetAddress.getByName(args[3]);
			int proxied_port = Integer.parseInt(args[4]);
			
			print("redirector listening on : " + local_addr.getHostAddress() + " : " + local_port + "\n");
			print("proxying traffic to : " + proxied_addr.getHostAddress() + " : " + proxied_port + "\n");
			UDPSocket clackSock = createUDPSocket();
			
			clackSock.bind(local_addr, local_port);
			
			DatagramSocket javaSock = new DatagramSocket();
			javaSock.connect(proxied_addr, proxied_port); // only talk to this address/port
			javaSock.setSoTimeout(400);
			DatagramPacket javaPacket = new DatagramPacket(new byte[2000], 2000); // max packet size
			
			// note:  this implementation does not look inside the UDP
			// packet and instead just assumes that the last host to send us
			// anything is the one who the data should be sent back to. 
			UDPSocket.HostInfo originatingHost = new UDPSocket.HostInfo();
			
			while(true){
			
				// read in with clack, send via java
				ByteBuffer clackbuff = clackSock.recvFrom(400, originatingHost);
				if(clackbuff.capacity() > 0){
					print("Clack sock recieved packet of size " + clackbuff.capacity() + "\n");

					javaSock.send(new DatagramPacket(clackbuff.array(), clackbuff.capacity(), proxied_addr, proxied_port ));
				}
				
				// read in with java, send via clack
				try {
					javaSock.receive(javaPacket);
					print("java sock received packet of size " + javaPacket.getLength() + "\n");
					int received_len = javaPacket.getLength();
					ByteBuffer javabuf = ByteBuffer.allocate(received_len);
					javabuf.put(javaPacket.getData(), 0 , received_len);

					clackSock.sendTo(javabuf, originatingHost);
				}catch(SocketTimeoutException toe){
					// ignore, this is expected for timeout mechanism
				}
			
				
				
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}	

	public String getDescription() { return "Use route as a proxy to redirect UDP packets to a given port"; }
}
