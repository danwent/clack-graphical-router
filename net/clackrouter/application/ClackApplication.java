
package net.clackrouter.application;


import javax.swing.JOptionPane;

import net.clackrouter.component.extension.UDP;
import net.clackrouter.component.tcp.TCB;
import net.clackrouter.component.tcp.TCP;
import net.clackrouter.router.core.Alarm;
import net.clackrouter.router.core.Router;


/**
 * <p>Abstract class to be sub-classed by all applications to be run on Clack. </p>
 * 
 * <p>The functionality of the application should be implemented with {@link #application_main(String[])} with 
 * parameters passed in using the argument array.  </p>
 * 
 * <p> Sockets are created by calling either of the supplied methods, {@link #createTCPSocket()} or {@link #createUDPSocket()} . 
 *  
 */
public abstract class ClackApplication extends Thread implements Alarm {
	
	private String[] mConfig;
	private Router mRouter; 
	private String mName;
	private ClackShell mShell;
	boolean is_sleeping = false;
	
	/**
	 * Used to configure the application before it is run (for internal Clack use).
	 * 
	 * @param name An identifier for this application
	 * @param router The router hosting this application
	 * @param config A String[] of parameters
	 */
	public void configure(String name, Router router, String[] config){
		mRouter = router; 
		mName = name;
		mConfig = config;
	}
	
	/**
	 * Set active shell for this application.
	 * @param shell
	 */
	public void setShell(ClackShell shell){ mShell = shell; }
	
	/**
	 * Starts the application (for internal Clack use)
	 */
	public void run(){
		try {
			application_main(mConfig);
		}catch (InterruptedException e){
			/*ignore, this just means we were killed */
			System.err.println("Application killed, thread " + getName() + " interrupted");
		}
		if(mShell != null)
			mShell.appIsFinished();
	}
	
	/**
	 * Create a TCPSocket running on this router
	 * @throws Exception errors if TCP is not enabled on the router
	 */
	protected TCPSocket createTCPSocket() throws Exception {
		TCP stack = mRouter.getTCPStack();
		if(stack == null) 
			throw new Exception("Error: TCP is not enabled in this router.");
		
		TCB tcb  = stack.createTCB(mName + "(" + getTime() % 10000 + ")");
		tcb.mSocket.setCurrentApplication(this);
		return tcb.mSocket;
	}
	
	/**
	 * Create a UDPSocket running on this router
	 * @throws Exception errors if UDP is not enabled on the router
	 */
	protected UDPSocket createUDPSocket() throws Exception {
		UDP stack = mRouter.getUDPStack();
		if(stack == null) 
			throw new Exception("Error: UDP is not enabled in this router.");
		return new UDPSocket(mName, stack);
	}
	
	/**
	 * Creates a simple dialog box to display a message
	 * @param msg message to be displayed
	 */
	protected void alert(String msg){
		JOptionPane.showMessageDialog(mRouter.getDocument().getFramework(),"[" + getAppName() + "]" + msg);
	}
	
	/**
	 * Print out a message to the application console
	 * @param s text to be printed
	 */
	protected void print(String s){
		System.out.println("[" + getAppName() + "]" + s);
		if(mShell != null){
			mShell.putText(s);
		}
	}
	
	/**
	 * Abstract "main method" to be implemented by sub-class
	 * @param args  the application arguments
	 */
	public abstract void application_main(String[] args) throws InterruptedException;
	
	/**
	 * Returns a very brief (single line) description of the application
	 */
	public abstract String getDescription();
	
	/**
	 * Get the router associated with this application
	 */
	public Router getRouter() { return mRouter; }
	
	/**
	 * Get application name
	 */
	public String getAppName() { return mName; }
	
	public void pause(long msecs){
		getRouter().getTimeManager().setAlarm(this, msecs);
		is_sleeping = true;
		while(is_sleeping){
			try {
			//	System.out.println("sleeping...");
				Thread.sleep(50);		
			}catch (Exception ex) { /*ignore */ }
		}
	}
	
	public void setAlarm(long msecs_from_now){
		getRouter().getTimeManager().setAlarm(this, msecs_from_now);
	}
	
	public long getTime() { return mRouter.getTimeManager().getTimeMillis(); }
	
	public void notifyAlarm() {
		is_sleeping = false;
	}
	
	
}
