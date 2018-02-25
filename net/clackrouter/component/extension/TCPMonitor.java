
package net.clackrouter.component.extension;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JPanel;
import javax.swing.Timer;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.gui.tcp.TCPStateAnalyzer;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.propertyview.TCPMonitorPopup;
import net.clackrouter.router.core.Router;

/**
 * <p> A class for tracking and analyzing the TCP flows being forwarded by a router. </p>
 * 
 * <p>  TODO: This class provides the basic infrastructure for tracking TCP flows and keeping certain
 * stats about the flows.  It's property view is meant to give a graphical view of all this data
 * but both have fallen into a state of disrepair and need a little care before they will be
 * usuable again. </p>
 */
public class TCPMonitor extends ClackComponent {
	
	
	public static int PORT_IN = 0;
	public static int PORT_OUT = 1;
	public static int NUM_PORTS = 2;
	
	
	private static int TIMEOUT_MILLIS = 30000;
	private Hashtable flow_map;  // map from 4-tuple string to TCPFlow objects
	private TCPMonitorPopup m_popup;
	private Timer mTimer;
	
	public TCPMonitor(Router router, String name){
		super(router, name);
		setupPorts(NUM_PORTS);
		flow_map = new Hashtable();
		mTimer = new Timer(TIMEOUT_MILLIS, new TimerCallback());
		mTimer.start();
	}
	
	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
        String in_desc = "TCPMonitor input";
        String out_desc = "TCPMonitor output";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
    } // -- setupPorts
	
	public boolean isModifying() { return false; } 
	
    public JPanel getPropertiesView(){
    	m_popup = new TCPMonitorPopup(this);
    	return m_popup;
    }
    
    /**
     * Extracts TCP packets from the IP that is being passed through the component and analyzes them.
     * 
     * <p> We identify a flow by a 4-tuple of (host A address, host A port, host B address, host B port).  
     * We arbitrarily define host A to be the host with the numerically lower address when the two addresses are 
     * compared as integers.  </p>
     * 
     * <p>Each flow then has a {@link TCPFlow} object that is updated each time we see a packet.</p>
     */
    public void acceptPacket(VNSPacket packet, int port_number)
    {
        
        if ( port_number == PORT_IN ){
        	if(packet instanceof IPPacket) {
        		IPPacket ippacket = (IPPacket) packet;
        		if(ippacket.getHeader().getProtocol() == IPPacket.PROTO_TCP){
        			try {
						int src = ippacket.getHeader().getSourceAddress().getInt();
						InetAddress src_addr = InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array());
        				int dst = ippacket.getHeader().getDestinationAddress().getInt();
        				InetAddress dst_addr = InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array());
        				VNSTCPPacket tcppacket = new VNSTCPPacket(ippacket.getBodyBuffer());
        				tcppacket.setDestIPAddress(dst_addr);
        				tcppacket.setSourceIPAddress(src_addr);
        				int src_port = tcppacket.getSourcePort();
						int dst_port = tcppacket.getDestinationPort();
        				
						String key = null;
						
						InetAddress a_addr, b_addr;
						int a_port, b_port;
        				if(src < dst){
        					a_addr = src_addr; a_port = src_port; b_addr = dst_addr; b_port = dst_port;
        				}else{
            				a_addr = dst_addr; a_port = dst_port; b_addr = src_addr; b_port = src_port;
        				}
        				key = createKey(a_addr, a_port, b_addr, b_port);
            			TCPFlow flow = (TCPFlow) flow_map.get(key);
            			if(flow == null){
            				flow = new TCPFlow(a_addr, a_port, b_addr, b_port);
            				flow_map.put(key, flow);
            				if(m_popup != null)
            					m_popup.addTCPFlow(flow);
            			}
            			flow.updateFlow(tcppacket);
	
        			} catch (UnknownHostException e){
        				// shouldn't happen
        				e.printStackTrace();
        			}
        		}
        	}
        	
        	m_ports[PORT_OUT].pushOut(packet); 
        } else
        { error( "Received packet on invalid port: " + port_number); }

    } // -- acceptPacket
    
    /** Get map from flow id to TCPFlow objects */
    public Hashtable getFlowMap() { return flow_map; }
    
    public String createKey(InetAddress aAddr, int aPort, InetAddress bAddr, int bPort){
    	return aAddr.toString() + ":" + aPort + " <-> " + bAddr.toString() + ":" + bPort;
    }

    /**
     * 
     * Helper class to contain all information about a single TCP flow.  
     * 
     * <p> Relies on the {@link TCPStateAnalyzer} class.  </p>
     */
    public class TCPFlow {
    	public TCPFlow(InetAddress a_addr, int a_port, InetAddress b_addr, int b_port) {
    		listeners = new ArrayList();
    		mStateA = new TCPStateAnalyzer(this, a_addr, a_port);
    		mStateB = new TCPStateAnalyzer(this, b_addr, b_port);
    		startTime = getTime();
    	}
    	
    	/** Represents the state for either TCP connection end-point */
    	public TCPStateAnalyzer mStateA , mStateB;
    	/** List of classes implementing {@link TCPMonitor.TCPFlowListener} and interested in this flow */
    	public ArrayList listeners; 
    	
    	public long startTime, updated;
    	
    	/** Return the TCPStateAnalyzer object in this pair that is not the one passed in */
    	public TCPStateAnalyzer getOtherTCPStateAnalyzer(TCPStateAnalyzer me) {
    		return (me == mStateA) ? mStateB : mStateA;
    	}
    	
    	/** Main method, used to update the TCPStateAnalyzers with information
    	 * from a new packet.
    	 */
    	public void updateFlow(VNSTCPPacket tcppacket){
	    		
	    		mStateA.analyzePacket(tcppacket);
	    		mStateB.analyzePacket(tcppacket);
	            updated = getTime();
				fireListeners();   		
    	}
    	
    	public void registerListener(TCPFlowListener l){
    		listeners.add(l);
    	}
    	public void fireListeners(){
    		for(int i = 0; i < listeners.size(); i++){
    			TCPFlowListener l = (TCPFlowListener) listeners.get(i);
    			l.flowUpdated();
    		}
    	}
    }
    
    /** Listener interface for classes wishing to be updated about changes to a {@link TCPMonitor.TCPFlow}.  */
    public interface TCPFlowListener {
       	public void flowUpdated();
    }
    
    
	public class TimerCallback implements ActionListener {
	      public void actionPerformed(ActionEvent evt) {
	     	long cur_time = getTime();
	        for(Enumeration enumer = flow_map.keys(); enumer.hasMoreElements(); ){
	  
	        	String key = (String) enumer.nextElement();
	       		TCPMonitor.TCPFlow flow = (TCPMonitor.TCPFlow) flow_map.get(key);
	       		if(flow.updated < cur_time + TIMEOUT_MILLIS) {
	       			flow_map.remove(key);
	       			if(m_popup != null)
	       				m_popup.removeTCPFlow(flow);
	       		}
	        }
	        fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_PACKET_IN)); // not really, but we want the Popup to refresh
	      }
	}
	

}
