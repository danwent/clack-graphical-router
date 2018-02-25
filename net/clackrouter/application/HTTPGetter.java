/*
 * Created on Feb 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.application;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;


/**
 * A simple Clack application to send an HTTP request to a webserver and print the response
 */
public class HTTPGetter extends ClackApplication {
	
	public void application_main(String[] args) {
		try {
			if(args.length < 1){
				print("usage: httpget [ip address]\n");
			}
			
			TCPSocket socket = createTCPSocket();
			InetAddress localAddr = getRouter().getInputInterfaces()[0].getIPAddress();
			InetAddress remoteAddr = InetAddress.getByName(args[1]);
			if(args.length > 2)
				localAddr = InetAddress.getByName(args[2]);

			StringBuffer sBuf = new StringBuffer(200);
			print("HTTPGetter is connecting to " + remoteAddr + "\n");
			Random rand = new Random();
			socket.bind(localAddr, rand.nextInt(5000));
			socket.connect(remoteAddr, 80);
			Thread.sleep(500);
			socket.send(ByteBuffer.wrap("GET / HTTP/1.0\r\n\r\n".getBytes()));

			while(true){
				ByteBuffer buf_in = socket.recv(2000, 1000);
				if(buf_in == null) break;
				if(buf_in.capacity() > 0)
					sBuf.append(new String(buf_in.array()));
			}
			socket.close();
			print("HTTP Get Request returned: \n" + sBuf + "\n");
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	 public String getDescription() { return "performs a simple 'HTTP GET /' for a dns/ip address"; }
}
