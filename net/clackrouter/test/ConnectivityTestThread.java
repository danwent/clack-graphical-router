package net.clackrouter.test;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JTextArea;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.simplerouter.ICMPEcho;
import net.clackrouter.component.simplerouter.InterfaceIn;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.netutils.ICMPListener;
import net.clackrouter.router.core.Router;
import net.clackrouter.routing.LocalLinkInfo;
import net.clackrouter.topology.core.TopologyModel;


/**
 * This class was created to test the IP allocation portion
 * of the Basic IP tutorial assignment.
 * 
 * @author Dan
 *
 */

public class ConnectivityTestThread extends Thread implements ICMPListener  {
	
	int cur_id; // id of current ping, just to make sure nothing weird happens
				// as a result of old ping packets
	boolean got_it;
	Hashtable<String,Router> router_map;
	Hashtable<String, ICMPEcho> echo_map;
	JTextArea m_log;
	boolean killed;
	boolean no_failures;
	boolean test_completed;
	PrintWriter out;
	
	public ConnectivityTestThread(ArrayList hosts, JTextArea log){
		if(hosts.size() == 0){
			System.err.print("Error, given empty array for connectivity test");
			return;
		}
		killed = false;
		m_log = log;
		router_map = getRouterMap(hosts);
		echo_map = getEchoMap(router_map);	
		
	}
	
	public void setOutput(PrintWriter o) { out = o; }
	
    public void log(String s) { 
    	if(m_log != null) {
    		m_log.append(s + "\n");  
    		m_log.setCaretPosition(m_log.getDocument().getLength());
    	}
    	if(out != null)
    		out.println(s);
    	else
    		System.out.println(s);
    }
	
	/*
	 * The goal is to make sure all of the hosts in a 
	 * topology have connectivity, ie: they can ping eachother
	 */
	public void run() {
		log("Running connectivity test for all enabled interfaces .... ");
		
		no_failures = true;
		test_completed = false;
		
		for(Enumeration e = router_map.keys(); e.hasMoreElements(); ){
			String r_name = (String)e.nextElement();
			boolean result = testRouter(r_name, router_map, echo_map);
			no_failures = no_failures && result;

		}
		
		if(no_failures) {
			log("Nice reachability!");
		}else {
			log("ERROR: Reachability failed");
		}	
		test_completed = true;
		
	}
	


	
	private boolean testRouter(String hostname, Hashtable<String, Router> router_map,
					Hashtable<String, ICMPEcho> echo_map){
		//System.out.println("******** Starting reachability tests from host: " + hostname + " ********");
		ICMPEcho local_echo = echo_map.get(hostname);
		
		boolean all_succeeded = true;
		for(Enumeration e = router_map.keys(); e.hasMoreElements(); ){
			String remote_host = (String)e.nextElement();
			Router remote_router = router_map.get(remote_host);
			
			if(out != null) out.flush();
			
			ICMPEcho remote_echo = echo_map.get(remote_host);
			remote_echo.addListener(this);
			
			InterfaceIn[] ifaces = remote_router.getInputInterfaces();
			
			for(int j = 0; j < ifaces.length; j++) {
				InetAddress dest_addr = ifaces[j].getIPAddress();
				//System.out.println("Testing connectivity to " + hostname + " with " + dest_addr.getHostAddress());
				
				LocalLinkInfo info = (LocalLinkInfo)remote_router.getLocalLinkInfo().get(ifaces[j].getDeviceName());
				if(!info.is_up) {
				//	log("skipping probe to down interface " + ifaces[j].getDeviceName() 
				//				+ " on " + remote_host);
					continue;
				}
				
				got_it = false;
				//	System.out.println("Sending id = " + cur_id);
				local_echo.sendEchoRequest(dest_addr, cur_id, 0);
				int i = 0;

				while(!got_it && i < 10){
					try {
						Thread.sleep(200);
					}catch(Exception ex) { ex.printStackTrace(); }
					i++;
					if(killed) return false;
				}
				cur_id++;
			
				if(!got_it){
					log("ERROR: " + hostname + " failed to connect to " + remote_host + "(" + ifaces[j].getDeviceName() + ") :" + 
							dest_addr.getHostAddress());
					all_succeeded = false;
				}
	
				// avoid weirdness with ping reply flying around
				// though we should be safe b/c of cur_id
				try {
					Thread.sleep(200);
				}catch(Exception ex) { ex.printStackTrace(); }
				
				if(killed) return false;
			}
			
			remote_echo.removeListener(this);
		}
		
		
		return all_succeeded;
	}
	
	/**
	 * Tells you whether last test was successfull (i.e., no failures)
	 * Will return false unless the last test fully completed
	 * 
	 * @return
	 */
	public boolean testSuccess(){
		return no_failures && test_completed;
	}
	
	public void receivedEchoReply(int id, int seq){
		// we don't care about replies for this test, just requests
	}
	
	public void receivedEchoRequest(int id, int seq){
		if(id == cur_id){
		//	System.out.println("Got it: " + cur_id);
			got_it = true;

		}else {
			log("weird, got packet with id = " + id + " when i was expecting " + cur_id);
		}	
	}
	
	public void killTest() { killed = true; }
	
	private static Hashtable<String, ICMPEcho> getEchoMap(Hashtable<String, Router> router_map){
	
		Hashtable<String, ICMPEcho> echo_map = new Hashtable<String, ICMPEcho>();
		for(Enumeration e = router_map.keys(); e.hasMoreElements(); ){
			String hostname = (String)e.nextElement();
			Router r = router_map.get(hostname);
			ClackComponent[] all_comps = r.getAllComponents();
			for(int i = 0; i < all_comps.length; i++){
				if(all_comps[i] instanceof ICMPEcho) {
					echo_map.put(hostname, (ICMPEcho)all_comps[i]);
					break;
				}
			}	
		}
		return echo_map;
	}
	
	
	// creates a map from string name to router object for each host in the list
	private static Hashtable<String, Router> getRouterMap(ArrayList hosts) {
		ClackDocument clack_doc = ((TopologyModel.Host)hosts.get(0)).document;
		Hashtable<String,Router> router_map = new Hashtable<String, Router>();
		
		for(int i = 0; i < hosts.size(); i++){
			TopologyModel.Host host = (TopologyModel.Host)hosts.get(i);
			Router r = clack_doc.getRouter(host.name);
			
			if(r == null) {
				System.out.println("Couldn't find Router for host = " + host.name);
				continue;
			}
			
			router_map.put(host.name, r);
			
		}
		return router_map;		
	}

}
