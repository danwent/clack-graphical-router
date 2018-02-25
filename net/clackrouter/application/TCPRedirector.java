/*
 * Created on Feb 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.application;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * A Clack application that uses a Clack host to proxy to a real Internet host, thus redirecting the TCP traffic.
 * 
 * <p>  The redirect effect is achieved using two sockets.  One is a socket running on the clack router and another
 * is a real Java socket connected to the host that the redirector is proxying.  Any data received in the Clack socket
 * is sent to the real host using the Java socket, and any data received using the java socket is sent back to the 
 * client using the Clack socket.  This effectively allows us to pretend like a real-host is inside our Clack network.  </p>
 */
public class TCPRedirector extends ClackApplication {

	
	public void application_main(String[] args) {
		try {
			int localPort = Integer.parseInt(args[1]);
			String proxiedHost = args[2];
			int remotePort = Integer.parseInt(args[3]);
			TCPSocket sock = createTCPSocket();
			InetAddress local_addr = getRouter().getInputInterfaces()[0].getIPAddress();
			sock.bind(local_addr, localPort);
			sock.listen();
			
			while (true) {

				print("TCPRedirector is listening on port " + localPort + " : "
						+ local_addr + " and ready forward to " + proxiedHost + " : " + remotePort + "\n");

				TCPSocket clientSocket = sock.accept(); // blocks until connection is established
				print("TCPRedirector received a connection! \n");

				Socket javaSock = new Socket();
				javaSock.connect(new InetSocketAddress(proxiedHost, remotePort));
				OutputStream out = javaSock.getOutputStream();
				InputStream in = javaSock.getInputStream();

				boolean clientDone = false, proxyDone = false;

				clientDone = true;
				byte[] input = new byte[2000];

				while (!clientDone || !proxyDone) {
					try {
						ByteBuffer fromClient = clientSocket.recv(2000, 20);
						
						if (fromClient == null){
							print("proxy received -1.  client done\n");
							clientDone = true;
							javaSock.close();
						}else if (fromClient.capacity() > 0){
							print("Proxy Received " + fromClient.capacity() + " bytes from client\n");
							out.write(fromClient.array());
						}
						
					
						if( in.available() > 0) {
							int byte_count = in.read(input);
							if (byte_count < 0){
								print("proxy received -1. server done\n");
								proxyDone = true;
								clientSocket.close(); // server closed, so we should "pass it on".
							}else if (byte_count > 0) {
								ByteBuffer relayData = ByteBuffer
									.allocate(byte_count);
								relayData.put(input, 0, byte_count);
								clientSocket.send(relayData);
								print("Proxy received " + byte_count + " bytes from the server\n");
							}
						}
					} catch (Exception e) {
				
						proxyDone = true;
					}
				}
				clientSocket.close();
				javaSock.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		    



	}
	
	public String getDescription() { return "Use route as a proxy to redirect TCP streams to a given port"; }

}
