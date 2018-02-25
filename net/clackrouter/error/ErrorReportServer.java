
package net.clackrouter.error;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Server to run on a remote host and record errors reported back to it by clack clients.  
 * 
 * <p> This server is not part of the standard Clack application and is not meant to be run
 * on any client machines.  The server listens indefinitely and dumps all information to a file
 * specified at the command-line. </p>  
 */
public class ErrorReportServer {
	
	public static final int ERROR_PORT = 5999;
	
	public static void main(String[] args){
		
		if(args.length < 1){
			System.out.println("Usage ErrorReportServer <filename>");
			return;
		}
		
		
		try {
			File errorFile = new File(args[0]);
			System.out.println("Error reporting server listening on port #" + ERROR_PORT + " and writing to file: " + errorFile.getName());
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(errorFile)));
			ServerSocket serverSock = new ServerSocket(ERROR_PORT);
			
		    // server infinite loop
		    while(true) {
		    	Socket socket = serverSock.accept();
		    	writer.println("*****************************************************************************");
		    	writer.println("Error Message from Address: " + socket.getInetAddress());
		    	writer.println("Received: " + (new Date()));
		    	BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
		
			    while(true) {
			    	String message = input.readLine();
			    	if (message==null) break;
			    	writer.println(message);
			    	writer.flush();
			    }
		    }
		
		}catch (Exception e){
			e.printStackTrace();
			
		}
	}

}
