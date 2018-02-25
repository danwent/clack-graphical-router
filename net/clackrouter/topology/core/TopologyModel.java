
package net.clackrouter.topology.core;


import java.awt.geom.Rectangle2D;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import net.clackrouter.gui.ClackDocument;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Alerter;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.RouterConfig;
import net.clackrouter.topology.graph.TopoWire;
import net.clackrouter.topology.graph.TopologyView;

import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.layout.JGraphLayoutAlgorithm;
import org.jgraph.layout.SugiyamaLayoutAlgorithm;
import org.w3c.dom.Element;


/**
 * Represents the data of a single topology, combining information from a VNS topology file,
 * and information provided by saved config files.
 * 
 * <p> We get connectivity information from the VNS topology file, as well as host names.
 * We get the graphical position of the components in the graph from the saved XML files. </p>
 * 
 * <p> A topology model can create a {@link TopoGraph} to represent this topo. </p>
 * 
 * <p> The topology model also implements the "VSwitch" functionality, that checks to see if a packet
 * being transmitted by a host on a Clack network can be handed directly to another clack host running
 * locally, or whether it must be sent back all the way to the VNS server.  This improves speed for 
 * larger clack topologies, as we cut out the round-trip to VNS infrastructure (i.e., stanford) 
 * for communication between two virtual hosts.  </p>  
 */
public class TopologyModel {
	
	public static final int ALERTER_LENGTH_MSEC = 100;

	
	private Hashtable linkMap; // has map from host:iface string to listener for this link, and both Router + interfaces.  
								// used both to light-up links, and disable links.  
	private int mTopology;
	private boolean isLocalTopology;

	private ArrayList mHosts, mLinks;
	private Hashtable mHostBoundsRects;
	private Alerter mAlerter;
	private Element mTopologyElement; // used to save <topology> XML between load & serialization
	
	public TopologyModel(int topology, ArrayList hosts, ArrayList links, boolean isLocal,
			Element topo_element){
		mTopology = topology;
		mHosts = hosts;
		mLinks = links;	

		mAlerter = new Alerter(ALERTER_LENGTH_MSEC);
		isLocalTopology = isLocal;
		mTopologyElement = topo_element;
	}
	
	public boolean isLocalTopology() { return isLocalTopology; }
	public Element getTopologyElement() { return mTopologyElement; }
	
	public ArrayList getLinks() { return mLinks; }
	public ArrayList getHosts() { return mHosts; }
	public Alerter getAlerter() { return mAlerter; }
	
	public void setHostBoundsRects(Hashtable rects) { mHostBoundsRects = rects;}
	
	public TopoGraph createTopoGraph(TopologyView view,  ClackDocument doc) {  
		Hashtable ifaceMap = new Hashtable();
		TopoGraph graph = new TopoGraph(new DefaultGraphModel(), doc);
		Rectangle2D bounds = null;
		
		for(int i = 0; i < mHosts.size(); i++){
			Host h = (Host)mHosts.get(i);
			if(mHostBoundsRects != null){
				String bounds_str = (String)mHostBoundsRects.get(h.name);
				if(bounds_str == null){
					System.err.println("error, could not bounds for " + h.name);
					continue;
				}
				bounds = RouterConfig.getRectangleFromString(bounds_str);
			}
			graph.insertHostIntoGraph(h, ifaceMap, bounds);
		}

		TopoWire inet_to_fw = null;
		for(int i = 0; i < mLinks.size(); i++){
			Link link = (Link)mLinks.get(i);
			TopoWire w = graph.connectInterfaces(view, this,link, ifaceMap);
			if(link.host1.name.equals(TopoParser.INTERNET_NAME))
				inet_to_fw = w;
		}
		
		if(!isLocalTopology) {
			// ach.  this is the hack so that Internet -> FW link lights up
			// we do this by simply lighting up whenever FW -> first router lights up
			Hashtable hash = getLinksForHost(TopoParser.FIREWALL_NAME);
			if(hash.size() > 1) { 
				System.err.println("error, multiple interfaces for firewall, ignoring all but first");			
			}
			TopologyModel.Link firewall_link = (TopologyModel.Link)hash.values().toArray()[0];
			firewall_link.addListener(inet_to_fw);
		}
		
		if(mHostBoundsRects == null){
			// no layout from XML, use an automatic layout
			JGraphLayoutAlgorithm layout = new SugiyamaLayoutAlgorithm();
			layout.run(graph, graph.getRoots());
		}


		return graph;
	}
	
	
	public Hashtable getLinksForHost(String hostname){
		Hashtable hash = new Hashtable();
		
		for(int i = 0; i < mLinks.size(); i++){
			Link link = (Link)mLinks.get(i);
			if(link.host1.name.equals(hostname))
				hash.put(link.iface1.name, link);
			if(link.host2.name.equals(hostname))
				hash.put(link.iface2.name, link);
		}
		return hash;
	}
	
	/*
	 * This function is a hack to implement a "VSwitch" that attempts to transmit packets directly to 
	 * another local Clack router, thereby bypassing VNS entirely and skipping out on the RTT.  This 
	 * aims to give much lower latencies across networks that contain multiple clack routers.  
	 * 
	 * The function returns true if the packet was transmitted locally, in which case it should not be
	 * given to the protocol manager for sending.  If the packet cannot be transmitted locally, the 
	 * function returns false and the protocol manager must be used to communicate with VNS. 
	 * 
	 */
	public boolean attemptLocalTransmit(String routerName, Link activeLink,  VNSPacket packet){
		Host sendingHost = getHost(routerName);
		if(activeLink.host1 == sendingHost && activeLink.host2.document != null){
			Router remoteRouter = activeLink.host2.document.getRouter(activeLink.host2.name);
			packet.setOutputInterfaceName("");
			packet.setInputInterfaceName(activeLink.iface2.name);
			remoteRouter.handlePacket(packet);
			return true;
		}else if(activeLink.host2 == sendingHost && activeLink.host1.document != null){
			Router remoteRouter = activeLink.host1.document.getRouter(activeLink.host1.name);
			packet.setOutputInterfaceName("");
			packet.setInputInterfaceName(activeLink.iface1.name);
			remoteRouter.handlePacket(packet);
			return true;
		}

		return false;
	}
	
	
	public int getTopology() { return mTopology; }
	
	public Host getHost(String name){
		for(int i = 0; i < mHosts.size(); i++){
			Host h = (Host)mHosts.get(i);
			if(h.name.equals(name)) return h;
		}
		return null;
	}
	
	public EthernetAddress getFirewallHWAddr(){
		for(int i = 0; i < mHosts.size(); i++){
			Host h = (Host)mHosts.get(i);
			if(h.name.equals(TopoParser.FIREWALL_NAME)){
				TopologyModel.Interface iface = (TopologyModel.Interface)h.interfaces.get(0);
				return iface.hw_addr;
			}
		}
		return null;
	}
	
	public Link getLink(String router1, String router2){
 		Hashtable hash1 = getLinksForHost(router1);
 		for(Enumeration e = hash1.keys(); e.hasMoreElements(); ){
 			String ethname = (String) e.nextElement();
 			Link l = (Link) hash1.get(ethname);
 			if(l.host1.name.equals(router1) && l.host2.name.equals(router2))
 				return l;
 			if(l.host2.name.equals(router1) && l.host1.name.equals(router2))
 				return l;
 		}
 		return null;
	}
	
	/**
	 * Used to enable or disable a link.  Just a wrapper around the main
	 * setLinkStatus function. 
	 * 
	 * @param doc
	 * @param router_name1
	 * @param router_name2
	 * @param is_enabled
	 */
	public void setLinkStatus(ClackDocument doc, String router_name1, 
			String router_name2, boolean is_enabled){
 			Link l = getLink(router_name1, router_name2);
 			if(l == null){
 				System.err.println("Error, couldn't find link between " + router_name1 +
	 						" and " + router_name2);
	 			return;
 			}
 			setLinkStatus(doc, l, is_enabled);
	}
	
	/**
	 * Enable or disable the specified link, updating the GUI
	 * @param doc
	 * @param link
	 * @param is_enabled
	 */
	public void setLinkStatus(ClackDocument doc, Link link, boolean is_enabled){

 		link.isEnabled = is_enabled;
		link.fireListeners();
 		
 		Router r1 = doc.getRouter(link.host1.name);
 		Router r2 = doc.getRouter(link.host2.name);
 		
		r1.setIfaceStatus(link.iface1.name, is_enabled);
 		r2.setIfaceStatus(link.iface2.name, is_enabled);
		
	}

	
	/**
	 * Represents a single host within a topology.  
	 */
	public static class Host {
		public boolean reservable;
		public String name;
		public ArrayList interfaces;
		public ClackDocument document;
		
		public Host(String n, boolean r) { 
			name = n; 
			reservable = r; 
			interfaces = new ArrayList();
		}
		
	}
	
	/**
	 * Represents an interface on a host within a topology.  
	 */
	public static class Interface {
		public String id;
		public String name;
		public ArrayList connectedIfaces;
		public InetAddress address, mask;
		public EthernetAddress hw_addr;
		
		public Interface(String n, String i, InetAddress addr, InetAddress m, EthernetAddress hw){
			id = i;
			name = n;
			address = addr;
			mask = m;
			hw_addr = hw;
			connectedIfaces = new ArrayList();
		}
	}

	/**
	 * Represents an link between two hosts inside a topology.  
	 */
	public static class Link {
		public Host host1, host2;
		public Interface iface1, iface2;
		public boolean isEnabled;
		public ArrayList listeners;
		public Link(Host h1, Interface i1, Host h2, Interface i2, boolean up){
			host1 = h1; iface1 = i1; host2 = h2; iface2 = i2; isEnabled = up;
			listeners = new ArrayList();
		}
		public void addListener(TopoWire w) { listeners.add(w); }
		public void fireListeners() {
			for(int i = 0; i < listeners.size(); i++){
				TopoWire twire = (TopoWire)listeners.get(i);
				twire.wireUsed();
			}
		}
		public void alert() {
			for(int i = 0; i < listeners.size(); i++){
				// DW:  what was this going to be used for???
				// i added this code a long time ago, but apparently 
				// never finished.  hrmmmm.... 
			}
		}
	}
	

	


}
