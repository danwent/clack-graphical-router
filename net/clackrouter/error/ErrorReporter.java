
package net.clackrouter.error;

import java.io.PrintWriter;
import java.net.Socket;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.GPLogConsole;
import net.clackrouter.jgraph.pad.resources.Translator;



/**
 * Class for remote error reporting when Clack peforms a fatal error.  
 * 
 * <p> The class attempts to dump System.out and System.err to a listening port on a remote server,
 * so that they error can be recognized and fixed. </p>  
 */
public class ErrorReporter {

	/**
	 * Report an error with a String message
	 * @param graphpad
	 * @param msg
	 */
	public static void reportError(ClackFramework graphpad, String msg){
		try {
		/*	
			Socket socket = new Socket(Translator.getString("ERROR_REPORTING_SERVER"), ErrorReportServer.ERROR_PORT);
		    PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
			output.println(msg);
			if(graphpad != null){
				GPLogConsole logger = graphpad.getLogConsole();
				output.println("Standard Out:\n" + logger.getStandardOutText());
				output.println("Standard Error:\n" + logger.getStandardErrorText());
			}
		    socket.close();
			*/
		}catch (Exception e){
			System.out.println("Unable to contact the error reporting server");
		}
	}
	
		/** 
		 * Report an error using a string message and an exception, for use when no ClackGraphpad handle exists.   
		 * @param msg
		 * @param error
		 */
	public static void reportError(String msg, Exception error){
		try {
			
			Socket socket = new Socket(Translator.getString("ERROR_REPORTING_SERVER"), ErrorReportServer.ERROR_PORT);
		    PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
		    output.println(msg);
			if(error!= null){
				error.printStackTrace(output);
			}
		    socket.close();
		
		}catch (Exception e){
			e.printStackTrace();
		}		
	}
	
}
