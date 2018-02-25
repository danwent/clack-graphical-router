package net.clackrouter.router.core;


import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.Interface;
import net.clackrouter.component.extension.UDP;
import net.clackrouter.component.simplerouter.IPRouteLookup;
import net.clackrouter.component.simplerouter.InterfaceIn;
import net.clackrouter.component.simplerouter.InterfaceOut;
import net.clackrouter.component.tcp.TCP;
import net.clackrouter.error.ErrorReporter;
import net.clackrouter.ethereal.Ethereal;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.util.RouterSpeedSlider;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.protocol.VNSProtocolManager;
import net.clackrouter.protocol.data.VNSBanner;
import net.clackrouter.protocol.data.VNSData;
import net.clackrouter.protocol.data.VNSHWInfo;
import net.clackrouter.routing.LocalLinkChangedListener;
import net.clackrouter.routing.LocalLinkInfo;
import net.clackrouter.routing.RoutingTable;
import net.clackrouter.test.ClackRouterTest;
import net.clackrouter.topology.core.TopologyModel;


/**
 * Main router class that contains all components representing a given host in a Clack topology
 * and handles all packet receiving and transmission.  
 */

public class Router extends Thread implements Serializable
{
	/**
	 * Time between alerts for any host using this router's {@link Alerter}.
	 * 
	 */
    public static int ROUTER_ALERT_TIME = 300;
    /**
     * <p>Time the router pauses during each iteration of the main processing loop in run(). </p>
     * 
     * <p> Used so that the router does not eat up a majority of the CPU cycles on a computer
     * by just spining in the processing loop </p>
     */
    public static int ROUTER_SLEEP_MSEC = 20;
       
    /**
     * The protocol manager that allows this router to communicate with VNS
     */
    private VNSProtocolManager mProtocolManager;  
    private Hashtable mComponents;
    private Hashtable mInputInterfaces;
    private Hashtable mOutputInterfaces;
    private ArrayList mPollingPool;
    private String mVHostName;
    
    /**
     * Support for dynamic local link information
     */
    private Hashtable mLocalLinkInfo = new Hashtable();
    private ArrayList mLinkChangedListeners = new ArrayList();
    public static final int DEFAULT_ROUTE_METRIC = 1;
    private InetAddress default_next_hop = null;
    private String default_route_iface = null;
    
    private Ethereal mEthereal = null;
    private boolean mEtherealIsRunning = false;
    public String setup_routing_key = "0";
   
    /**
     * flag indicated whether the router is finished, in which case is should kill the thread.
     */
    private boolean mIsDone = false;
    /**
     * flag indicating if the router is currently running a test and should not send/receive traffic.
     */
    private boolean mDisconnectForTest = false;
    /**
     * Reference to the current test running on the router (null for no test).
     */
    private ClackRouterTest mCurrentTest = null;
    /**
     * <p>The graphical ClackDocument associated with this router. </p>
     * 
     * <p> This class can be used to get references to other graphical entities, including the 
     * link RouterView and ClackGraphpad
     */
    private ClackDocument mDocument; 
    /**
     * The data model for the topology that includes this router
     */
    private TopologyModel mTopologyModel;
    /**
     * Reference to this router's TCP stack (null if none)
     */
    private TCP mTCPStack;
    /**
     * Reference to this router's UDP stack (null if none)
     */
    private UDP mUDPStack;
    /**
     * flag indicating if this router is graphical (ie: is a router view visible in Clack?)
     */
    private boolean mIsGUI;
    /**
     * Map from router iterface name to the TopologyModel.Link object connected to this interface 
     */
    private Hashtable mLinks;
    /**
     * Alerter used by components and wires
     */
    private Alerter mAlerter;
    
    private IPRouteLookup mIPRouteLookup = null; // cache this for routing table look-up request
    private EthernetAddress m_fw_hw_addr; 
	private ClackComponentEvent data_change_event;
	
	private Vector<Alarm> need_to_notify = new Vector<Alarm>();
	private boolean pending_error = false;

    public Router(VNSProtocolManager proto_manager, TopologyModel model, boolean isGUI)   {
    	mProtocolManager  = proto_manager;
    	mTopologyModel = model;
    	mComponents  = new Hashtable();
    	mInputInterfaces  = new Hashtable();
    	mOutputInterfaces = new Hashtable();
    	mPollingPool      = new ArrayList();
 		mAlerter = new Alerter(ROUTER_ALERT_TIME);
		mIsGUI = isGUI;
		link_change_info = new ArrayList<LinkChangeInfo>();

		data_change_event = new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE, 0);
		m_fw_hw_addr = mTopologyModel.getFirewallHWAddr();
    }
    
    /**
     * Reads interface hardware information from the ProtocolManager (must be called before Router is started).
     */
    public boolean initializeVNSConnectedRouter() throws Exception 
    {

    	mProtocolManager.setRouter(this);
    	mVHostName = mProtocolManager.getHostName();
    	System.out.println("got hostname: " + mVHostName);
        VNSHWInfo hwinfo = (VNSHWInfo)mProtocolManager.getHardwareInfo();
        System.out.println("got hardware info: " + hwinfo.toString()); 
        if(hwinfo == null) { return false; } // -- couldn't connect

        for(int i = 0; i < hwinfo.getNumberOfInterfaces(); i++) {
            VNSHWInfo.InterfaceEntry entry = hwinfo.getInterfaceEntryAt(i);   
            InterfaceIn iface_in  = new InterfaceIn("FromDevice(" + entry.getName() + ")", this, entry);
            InterfaceOut iface_out = new InterfaceOut("ToDevice(" + entry.getName() + ")", this, entry);
            mComponents.put(iface_in.getName(), iface_in);
            mComponents.put(iface_out.getName(), iface_out);
            mInputInterfaces.put(entry.getName(), iface_in); // plain name for these, no 'ToDevice' or 'FromDevice'
            mOutputInterfaces.put(entry.getName(), iface_out);
        }    
        System.out.println("trying to get links for host: " + getHost().toString());
		mLinks = mTopologyModel.getLinksForHost(getHost());
	/*	for(Enumeration e = mLinks.keys(); e.hasMoreElements(); ){
			String key = (String) e.nextElement();
			System.out.println("iface: " + key + " link = " + mLinks.get(key));
		}*/

    	System.out.println("Initializing VNS connected router (name = " + getHost() + ")");
        return true;
    }
    
    public boolean initializeNonVNSRouter(TopologyModel.Host host) throws Exception {
    
		mLinks = mTopologyModel.getLinksForHost(host.name);
		mVHostName = host.name;
    	for(int i = 0; i < host.interfaces.size(); i++) {
            TopologyModel.Interface entry = (TopologyModel.Interface) host.interfaces.get(i);  
            InterfaceIn iface_in  = new InterfaceIn("FromDevice(" + entry.name + ")", this, entry);
            InterfaceOut iface_out = new InterfaceOut("ToDevice(" + entry.name + ")", this, entry);
            mComponents.put(iface_in.getName(), iface_in);
            mComponents.put(iface_out.getName(), iface_out);
            mInputInterfaces.put(entry.name, iface_in); // plain name for these, no 'ToDevice' or 'FromDevice'
            mOutputInterfaces.put(entry.name, iface_out);
        }     	
    
    	return true;
    }
    
    // put the router to work 
    // for each loop, we grab one chunk of data from the protocol manager, handle it,
    // then we poll each component that has registered to be polled.
    /**
     * <p>Main processing loop for a Clack Router. </p>
     * 
     * <p>In each loop, we check if we need to exit, sleep, poll() each registered component,
     * and then process a single piece of data from the protocol manager.  </p>
     */
    public void run() {
		try {
			
			while (true) {

				if (mIsDone) {
					if(mProtocolManager != null){
						mProtocolManager.disconnectFromVNS(); 
						mProtocolManager.join();
					}
					if(this.getDocument().getFramework().isDebug())
						System.out.println("Router Exiting");
					return; // kill thread
				}
				

				try {
					sleep(ROUTER_SLEEP_MSEC);
				}catch (InterruptedException sie){
					// ignore
				}
				
				
				VNSData data = null;
				//	if we're not local, get data from VNS
				if(mProtocolManager != null) 
					data = mProtocolManager.getData();

			
				try {

					if(data != null) {
						if (data.isPacket() && !mDisconnectForTest) { handlePacket((VNSPacket) data);} 
						else if (data.isBanner()) { handleBanner((VNSBanner) data);} 
						else if (data.isClose()) { mIsDone = true;}
					}
					
					while(need_to_notify.size() > 0){
						Alarm a = need_to_notify.remove(0);
						a.notifyAlarm();
					}
		
					// if we need to alert anyone about link status
					// changes, do so now
					informLinkChangeListeners();

					// call all components registered for a poll()
					for (int i = 0; i < mPollingPool.size(); i++) 
						((ClackComponent) mPollingPool.get(i)).poll();
					
				}catch (Exception e){
					e.printStackTrace();
					JOptionPane.showMessageDialog(getDocument().getFramework(),
							" Error on Router " + getHost() + " (will try and continue)\n See standard-error for more info");
					setPendingError(true);
				}

			} // -- while
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getDocument().getFramework(),
					" Fatal Router Error for: " + getHost() + "\n See standard-error for more info");
		}
	}

 

    /**
     * <p>Handles packets that arrive at one of the router's input interfaces. </p>
     * 
     * <p> The router checks that the packet info is valid and then hands it
     * off to the correct InterfaceIn component.  We also notify the 
     * topology link to let it know that a packet has arrived </p>
     */
	public void handlePacket(VNSPacket packet)
    {
		String iface_name = packet.getInputInterfaceName();
		InterfaceIn iface = (InterfaceIn) mInputInterfaces.get(iface_name);
		if(iface == null){
			System.err.println("Could not find interface: " + iface_name);
			return;
		}
		if(!(packet instanceof VNSEthernetPacket)){
			System.err.println("Router received non-ethernet packet");
			return;
		}
		VNSEthernetPacket e_packet = (VNSEthernetPacket)packet;
		
        // test to see if this is an a "false ARP" (ie: its an artifact of VNS, 
        // one that i wouldn't actually get if 
        // our topology was actually what the network looked like)
		
		try {
			if(e_packet.getType() == VNSEthernetPacket.TYPE_ARP){
				
				EthernetAddress src = EthernetAddress.getByAddress(e_packet.getSourceAddressBuffer().array());
				if(src.equals(m_fw_hw_addr)) {
					VNSARPPacket arppacket = new VNSARPPacket(e_packet.getBodyBuffer());
					InetAddress dest = InetAddress.getByAddress(arppacket.getDestinationProtocolAddress().array());
					// TODO: should do subnet match, not just exact match.  for simple topos it doesn't matter
					if(!iface.getIPAddress().equals(dest)){
						return;
					}
				}
			}
		}catch (Exception e) { 
			e.printStackTrace(); 
			ErrorReporter.reportError(mDocument.getFramework(), "Error in router method handlePacket()");
		}
		
		TopologyModel.Link link = (TopologyModel.Link)mLinks.get(iface_name);	
		if(link == null){
			String error = getHost() + " received packet on " + iface_name + 
				" but has no associated link";
			System.err.println(error);
			ErrorReporter.reportError(mDocument.getFramework(), error);
		}
		
		
		if(!link.isEnabled)return;
		link.fireListeners();
		
		if(mEtherealIsRunning && mEthereal != null)
			mEthereal.addPacket(e_packet, iface_name + " (in)");
		
		iface.acceptPacket(e_packet, InterfaceIn.PORT_FROM_ROUTER);
	}
	
	private void handleBanner(VNSBanner banner) 
    { System.out.println("Got a Banner!");		}

	
	/**
	 * Method used by InterfaceOut components to send packets to VNS .
	 * @param packet the Ethernet frame to be sent
	 */
	public void sendOutgoingPacket(VNSEthernetPacket packet)  {
		String iface_name = packet.getOutputInterfaceName();
		TopologyModel.Link link = (TopologyModel.Link)mLinks.get(iface_name);
		if(!link.isEnabled)return;
		
		link.fireListeners();
		
		if(mEtherealIsRunning && mEthereal != null)
			mEthereal.addPacket(packet, iface_name + " (out)");

		try {

			if(!mTopologyModel.attemptLocalTransmit(getHost(), link, packet)){
				if(mProtocolManager == null){
					System.err.println("Unable to locally transmit, but we're not connected to VNS");
					return;
				}
				
				if(!mDisconnectForTest)
					mProtocolManager.sendData(packet);
			}
			
			if(mCurrentTest != null)
				mCurrentTest.processPacket(packet);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorReporter.reportError(mDocument.getFramework(), "Error in router SendOutgoingPacket");
		}
	}

	/**
	 * Signals the Router to end processing and disconnect from VNS.
	 *
	 */
    public void stopRouterAndDisconnect() {	mIsDone = true;  }


    /**
     * Used by components to register for a callback once per router processing loop.
     * @param comp component to be polled
     */
    public void registerForPoll(ClackComponent comp) {mPollingPool.add(comp);}
    
    /**
     * Unregisters a component from being polled.
     * @param comp
     */
    public void removeFromPoll(ClackComponent comp){	mPollingPool.remove(comp); }

    /**
     * Retrieve a component by name
     */
    public ClackComponent getComponent(String component_name) {
		return (ClackComponent) mComponents.get(component_name);
    }
    
    /**
     * Add a component to this router's list.
     * @param comp component to be added
     * @return returns whether the addition was successful
     */
    public boolean addComponent(ClackComponent comp) {
    	String name = comp.getName();
    	if(mComponents.get(name) != null) return false;
    	
    	mComponents.put(name, comp);
    	return true;
    }
    
    /**
     * Removes the named component from the Router's list.
     */
    public void removeComponent(String comp_name) {
    	removeComponent((ClackComponent)mComponents.get(comp_name));
    }
    
    /**
     * Removes the specified component from the router's list.
     */
    public void removeComponent(ClackComponent comp){
    	System.out.println("removing component from router: " + comp.getName());
    	mComponents.remove(comp);
    	
		if(comp instanceof IPRouteLookup) 
			mIPRouteLookup = null;
    }
    
    /**
     * Get all components contained within the router.
     */
	public ClackComponent[] getAllComponents() {

		ClackComponent[] allcomps = new ClackComponent[mComponents.size()];
		Enumeration e = mComponents.keys();
		for(int i = 0; e.hasMoreElements(); i++ ) {
			String key = (String)e.nextElement();
			allcomps[i] = (ClackComponent)mComponents.get(key);
		}
		return allcomps;
	}
	
	/**
	 * Sets the routing table for this router, overwriting all previous entries.
	 */
	public void setRoutingTable(RoutingTable table) throws Exception {
	
		for(Enumeration e = mComponents.elements(); e.hasMoreElements(); ){
			ClackComponent comp = (ClackComponent)e.nextElement();
			if(comp instanceof IPRouteLookup) {
				((IPRouteLookup)comp).setRoutingTable(table);
				return;
			}
		}
		throw new Exception("Could not find LookupIPRoute component to set routing table");
	}
	
	/**
	 * Returns the routing table of the IPRouteLookup component in this router 
	 * @return the routing table, or null if not IPRouteLookup component is found. 
	 */
	public RoutingTable getRoutingTable() {
		// hack to speed this up for common case
		if(mIPRouteLookup != null) return mIPRouteLookup.getRoutingTable();
		
		for(Enumeration e = mComponents.elements(); e.hasMoreElements(); ){
			ClackComponent comp = (ClackComponent)e.nextElement();
			if(comp instanceof IPRouteLookup) {
				mIPRouteLookup = (IPRouteLookup)comp;
				return mIPRouteLookup.getRoutingTable();
			}
		}	
		return null;
	}
	
	public void setIPRouteLookup(IPRouteLookup iprl){ mIPRouteLookup = iprl; }
	
    /**
     * Get an array of all Input Interfaces.
     */
    public InterfaceIn[] getInputInterfaces()
    {
        InterfaceIn[] interfaces = new InterfaceIn[mInputInterfaces.size()];
		Enumeration e = mInputInterfaces.keys();
		for(int i = 0; e.hasMoreElements(); i++ ) {
			String key = (String)e.nextElement();
			interfaces[i] = (InterfaceIn)mInputInterfaces.get(key);
		}
		return interfaces;
    }
    
    /**
     * Get an array of all output interfaces.
     */
    public InterfaceOut[] getOutputInterfaces() {
        InterfaceOut[] interfaces = new InterfaceOut[mOutputInterfaces.size()];
		Enumeration e = mOutputInterfaces.keys();
		for(int i = 0; e.hasMoreElements(); i++ ) {
			String key = (String)e.nextElement();
			interfaces[i] = (InterfaceOut)mOutputInterfaces.get(key);
		}
		return interfaces;
    }

    /**
     * Returns the input interface associated with the specified device.
     */
    public Interface getInterfaceByName(String devicename)
    {
    	return (Interface)mInputInterfaces.get(devicename);
    } // -- getInterfaceByName
	
	/**
	 * <p>Get the router's Alerter for registering a one-time callback. </p>
	 * 
	 * <p> The alerter is used any time a callback is needed.  For example,
	 * we use this to implement flashing links.  This is more light-weight than
	 * having each flash spawn its own thread </p>
	 */
    public Alerter getAlerter() { return mAlerter; }
	public VNSProtocolManager getProtocolManager() { return mProtocolManager; }
	public void setTCPStack(TCP t) { mTCPStack = t; }
	
	/**
	 * Access the TCPStack of this router (may be null)
	 */
	public TCP getTCPStack() { return mTCPStack; }
	public void setUDPStack(UDP u) { mUDPStack = u; }
	
	/**
	 * Access the UDPStack of this router (may be null)
	 */
	public UDP getUDPStack() { return mUDPStack; }
	
	/**
	 * indicates whether this router is visible as a full router in the Clack Application
	 */
	public boolean isGUI() { return mIsGUI; }
	
	/**
	 * Set the document this router is associated with
	 */
    public void setDocument(ClackDocument doc) { mDocument = doc;}
    /**
     * Access the document this router is associated with
     */
    public ClackDocument getDocument() { return mDocument; }
    
    
    /**
     * Find out if this router is local (ie: not connected to VNS)
     * This must be tested before any checking any of getTopology(),
     * getHost(), getPort() or getServer().  
     */
    public boolean isLocalRouter() { return mProtocolManager == null; }

    /**
     * Get the topology number for this router
     */
    public int getTopology()   { return mTopologyModel.getTopology(); }
    /**
     * Get this router's name within the topology
     */
    public String getHost()  { return mVHostName; }
    /**
     * Get the port this router is using to connect to the VNS server
     */
    public int getPort() { return mProtocolManager.getPort(); }
    /**
     * Get the VNS server name that this router is connected to
     */
    public String getServer(){ return mProtocolManager.getServer(); }
    
    public TimeManager getTimeManager() { return getDocument().getFramework().getTimeManager(); }
    
    /**
     * Run the specified test on this router.
     * @param test test to run
     * @param disconnect flag indicated if router should disconnect from other routers and VNS for the test
     */
	public void runTest(ClackRouterTest test, boolean disconnect){
		if(test == null) return;
		
		mDisconnectForTest = disconnect;
		mCurrentTest = test;
		test.startTest(this);
	}
	
	/**
	 * signals the end of the current test
	 */
	public void endTest() {
		mDisconnectForTest = false;
		mCurrentTest = null;
	}
	

	
	public void addLocalLinkInfo(InetAddress net, InetAddress mask, InetAddress nh, String iface, boolean is_up, int metric,
			boolean is_routing_iface){
		LocalLinkInfo new_lli = new LocalLinkInfo(net, mask, iface, nh, metric, is_up, is_routing_iface);
		mLocalLinkInfo.put(iface, new_lli);
	}
	
	public Hashtable getLocalLinkInfo() { return mLocalLinkInfo; }
	
	public void addLocalLinkChangeListener(LocalLinkChangedListener l) { mLinkChangedListeners.add(l); }
	public void removeLocalLinkChangeListener(LocalLinkChangedListener l) { mLinkChangedListeners.remove(l); }
	
	public InetAddress getDefaultNextHop() { return default_next_hop; }
	public String getDefaultRouteIface() { return default_route_iface; }
	public void setDefaultRouting(InetAddress next_hop, String iface) { default_next_hop = next_hop; default_route_iface = iface; }
	
    public void updateRouteTableStateInGUI() {
    	if(mIPRouteLookup == null) {
    		System.err.println("route look-up null");
    		return;
    	}else if(getDocument() == null){
    		System.err.println("get document is null");
    		return;
    	}
		mIPRouteLookup.fireListeners(data_change_event);
    	if(getDocument().inTopoRouteTableViewMode()){
    		getDocument().redrawTopoView();
    	}
    }
    
    public void startEthereal() { 
    	mEthereal = new Ethereal(this, this.getName());
    	mEtherealIsRunning = true;
    }

    public void stopEthereal() {
    	mEtherealIsRunning = false;
    }
    
    public void addNeedToNotify(Alarm a) { 
    	need_to_notify.add(a); 
    }
    
    // used to let topology view show when a there has been an error
    // in a router component (otherwise its hidden!)
    public void setPendingError(boolean e) { 
    	pending_error = e; 
    	getDocument().redrawTopoView();

    }
    
    public boolean getPendingError() { return pending_error; }
    
    /**
     * Let's external entities set the IP address and subnet of a particular interface on the router
     * This is a bit tricky, because they need to change both the input and output interfaces, and 
     * repaint them.  We also need to alert anyone who is listening for changes to this configuration
     * (e.g., a dynamic routing protocol).  
     */
    public void configureIPInterface(String iface_name, InetAddress addr, InetAddress mask) throws Exception {
    	InterfaceIn iface_in = (InterfaceIn)mInputInterfaces.get(iface_name);
    	InterfaceOut iface_out = (InterfaceOut)mOutputInterfaces.get(iface_name);
    	if(iface_in == null) throw new Exception("non-existant interface name: " + iface_name);
    	
    	iface_in.setIPAddress(addr);
    	iface_in.setIPSubnet(mask);
    	iface_out.setIPAddress(addr);
    	iface_out.setIPSubnet(mask);
    	
    	iface_in.try_repaint();
    	iface_out.try_repaint();
   // 	getDocument().redrawTopoView();
    	getDocument().redrawTopoView(); // this will redraw whichever
    											// topo view is currently being viewed
    	
		LocalLinkInfo old_lli = (LocalLinkInfo) mLocalLinkInfo.get(iface_name);
		
		if(old_lli != null){
			// note: it is unclear if we should put the link in "down" status when we assign a new link/subnet
			// not doing so, for now.  
			LocalLinkInfo new_lli = new LocalLinkInfo(addr, mask, old_lli.local_iface, 
					old_lli.next_hop, old_lli.metric, old_lli.is_up, old_lli.is_routing_iface );
			mLocalLinkInfo.put(iface_name, new_lli);
		}
    }
    
    // this code is needed to avoid a race when the GUI thread enables/disables a link
    private ArrayList<LinkChangeInfo> link_change_info;
    private class LinkChangeInfo{
    	LocalLinkInfo old_info, new_info;
    }
    public void setIfaceStatus(String iface_name, boolean is_up) {

    	LocalLinkInfo info = (LocalLinkInfo)getLocalLinkInfo().get(iface_name);
    	if(info.is_up == is_up) return;
    		
    	LocalLinkInfo old_info = LocalLinkInfo.copy(info);
    	info.is_up = is_up;
    	LinkChangeInfo change_info = new LinkChangeInfo();
    	change_info.new_info = info;
    	change_info.old_info = old_info;
    	link_change_info.add(change_info);
    }
    
	public void updateLinkMetric(String iface_str, int metric){

		LocalLinkInfo info = (LocalLinkInfo) mLocalLinkInfo.get(iface_str);
		if(info.metric == metric) return;
		
		LocalLinkInfo old_info = LocalLinkInfo.copy(info);
		info.metric = metric;
		
    	LinkChangeInfo change_info = new LinkChangeInfo();
    	change_info.new_info = info;
    	change_info.old_info = old_info;
    	link_change_info.add(change_info);
	}
	
    
    private void informLinkChangeListeners(){

    	while(link_change_info.size() > 0) {
    		LinkChangeInfo lc_info = link_change_info.remove(0);
    		for(int i = 0; i < mLinkChangedListeners.size(); i++){
    			LocalLinkChangedListener listener = (LocalLinkChangedListener)mLinkChangedListeners.get(i);
    			listener.localLinkChanged(lc_info.old_info, lc_info.new_info);
    		}
    	}

    }
    
   
}
