
package net.clackrouter.topology.core;


import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.clackrouter.error.ErrorReporter;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.util.TopologyPrompter;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.RouterConfig;
import net.clackrouter.router.core.RouterManager;
import net.clackrouter.router.graph.RouterGraph;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Controls loading routers from different topology files and cleaning up on close. 
 * 
 * <p>  Owns a hashtable of different {@link TopologyModel} objects, associated with 
 * their respective topology number.  Can parse a topology node, adds each router
 * specified by the file. </p> 
 */
public class TopologyManager {
	

	public static int NO_TOPOLOGY = -1;
	
	private Hashtable mTopologyModels;
	private ClackFramework mFramework;
	private RouterManager mRouterManager;
	private Hashtable mSavedClackNodeMap; /* saves the clack node in the topo file for each topo, 
	 										used when we are saving changes to the topo to file */
	private Element saved_topo_node; // save the XML from loading the config file
	private Hashtable mRouterHash = new Hashtable();
	
	public TopologyManager(ClackFramework gp) {	
		mTopologyModels = new Hashtable();
		mSavedClackNodeMap = new Hashtable();
		mFramework = gp;
	}
	
	public TopologyModel getTopologyModel(int topology, Element clack_element) throws Exception { 
		TopologyModel model = (TopologyModel) mTopologyModels.get(new Integer(topology));	
		if(model != null) return model;
		
		model = getTopologyModelFromXML(topology , clack_element, this.mFramework.isDebug());
		
		mTopologyModels.put(new Integer(topology), model);	
		return model;
	}
		
	public static TopologyModel getTopologyModelFromXML(int topology, 
					Element clack_element, boolean debug) throws Exception {
		ArrayList hosts = new ArrayList(), links = new ArrayList();
		
		Element topo_element;
		boolean isLocal;
		if(clack_element.getElementsByTagName("topology").getLength() == 1){
			isLocal = true;
			if(debug)
				System.out.println("Using local topology configuration");
			topo_element = (Element)clack_element.getElementsByTagName("topology").item(0);
		}else {
			isLocal = false;
			if(debug)
				System.out.println("Connecting to VNS to download topology information");
			String vns_server = clack_element.getAttribute("server");
			topo_element = getTopologyElementFromVNS(vns_server, topology);
		}
		TopoParser.getHostAndWireLists(topo_element, hosts, links, isLocal);
		TopologyModel model = new TopologyModel(topology, hosts, links, isLocal, topo_element);
		System.out.println("topo model: " + hosts.size() + " hosts " + links.size() + " links");
		return model;
	}
	
	public static Element getTopologyElementFromVNS(String vns_server, int topology) throws Exception {

			DocumentBuilderFactory dbf =DocumentBuilderFactory.newInstance();
        	dbf.setValidating(false);
        	DocumentBuilder db = dbf.newDocumentBuilder();
    		String url = "http://" + vns_server + "/topology=" + topology;
			System.out.println("Downloading topology information at: " + url + " ...");
			URLConnection topoURL = (new URL(url)).openConnection();
			InputStream is = topoURL.getInputStream();
			Document doc  = db.parse(is);
			return doc.getDocumentElement();
	}
	


	public void configureTopoModelFromXML(InputStream is)throws Exception {
        DocumentBuilderFactory dbf =
    		DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
    
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc  = db.parse(is);
        parseTopoNode((Element)doc.getDocumentElement());
  
	}
	
	
	private void parseTopoNode(Element topo_node) throws Exception {
		parseTopoNode(topo_node, NO_TOPOLOGY);
	}
	
	public void reloadTopologyFile(int new_topo) throws Exception {
		if(saved_topo_node != null)
			parseTopoNode(saved_topo_node, new_topo);
		else {
			RouterManager.ConnectFrame cFrame = new RouterManager.ConnectFrame(mFramework);
			cFrame.show();
			cFrame.updateText("Cannot load new topology.  No topology file has been loaded yet.\n");
		}
	}
	
	// top function for parsing a topo-config file.  
	// uses TopoParser to get a topology description from the vns-server
	// and build a TopologyModel (including a TopoGraph)
	// Then we parse the 'host' elements, which can configure individual routers
	// in the topology.  
	private void parseTopoNode(Element clack_node, int user_requested_topo) throws Exception {

		if(mRouterManager == null){
			// note: for local topologies, its fine if these values are invalid
			int vns_port = 0; 
			String port_str = clack_node.getAttribute("port");
			if(port_str.length() > 0)
				vns_port = Integer.parseInt(port_str);
			String vns_server = clack_node.getAttribute("server");
			if (vns_server.length() == 0 )
				vns_server = "0.0.0.0";
			// as far as I know, this username and password is ignored
			mRouterManager = new RouterManager(mFramework,vns_server, 
										vns_port, "clack", "pass");
		}		
        saved_topo_node = clack_node;
		
		int low, high;
		low = high = user_requested_topo;
		String topo_range_str = clack_node.getAttribute("number");
		if(topo_range_str.length() == 0)
			topo_range_str = "0-0"; // just fake it, as we will be ignoring it
		
		if(user_requested_topo == NO_TOPOLOGY) {
		
			if(topo_range_str.equals("*")){
				TopologyPrompter prompter = new TopologyPrompter(this.mFramework.getFrame(), false);
				if(!prompter.isValid())
					throw new Exception("Invalid topology value");
				int value = prompter.getTopology();
				topo_range_str = value + "-" + value;
			}
        
			String[] range = topo_range_str.split("-");
			low = Integer.parseInt(range[0]);
			high = Integer.parseInt(range[1]);
		}
        
		int topology = low;
		while(true){
			if(topology > high){

	        	TopologyPrompter prompter = new TopologyPrompter(mFramework.getFrame(), true);
	        	if(!prompter.isValid())
	        		throw new Exception("Invalid topology connect attempt");
	      
	        	high = topology = prompter.getTopology();
				//throw new Exception("Could not connect to a topology in range ");
			}
			try {
				
				TopologyModel model = getTopologyModel(topology, clack_node);
		
				NodeList topolayout_nodes = clack_node.getElementsByTagName("topolayout");
				if(topolayout_nodes.getLength() > 0) {
					NodeList host_nodes = ((Element)topolayout_nodes.item(0)).getElementsByTagName("host");
					Hashtable boundsRects = new Hashtable();
					for(int i = 0; i < host_nodes.getLength(); i++){
						Element hnode = (Element)host_nodes.item(i);
						boundsRects.put(hnode.getAttribute("name"), hnode.getAttribute("bounds"));
					}
					model.setHostBoundsRects(boundsRects);
				}
		
				// now configure the routers
				NodeList router_nodes = clack_node.getElementsByTagName("router");
				for(int i = 0; i < router_nodes.getLength(); i++){
					addRouter(model, (Element)router_nodes.item(i), clack_node);
				}
				if(this.mFramework.isDebug())
					System.out.println("Routers on topo " + topology + " successfully loaded");
				break;
			}catch (Exception e){
				e.printStackTrace();
				/* let it go */
			}
			topology++;
		}// end while
		
		mSavedClackNodeMap.put(new Integer(topology), clack_node); // remember range for this topology
	}
	// adds a new router, configuring if from a file if requested.
	// if isGUI == true, then we add a new document to Clack for this router.  
	private Router addRouter(TopologyModel model, Element router_elem, Element clack_node)  throws Exception { 

		boolean isGUI = router_elem.getAttribute("isgui").equals("1");
		String hostname = router_elem.getAttribute("name");

		
		TopologyModel.Host host_model = (TopologyModel.Host)model.getHost(hostname);
		Router router;
		if(model.isLocalTopology()){
			router = new Router(null, model, isGUI);
			router.initializeNonVNSRouter(host_model);
		}else {
			String vns_username = clack_node.getAttribute("username"); 
			String vns_auth_key = clack_node.getAttribute("auth_key"); 	
			if(vns_username.length() == 0 || vns_auth_key.length() == 0){ 
				throw new Exception("No VNS username or auth_key specified for non-local topology"); 
			}
			router = mRouterManager.connectToVNS(model, hostname, isGUI, vns_username, vns_auth_key);
		}
		mRouterHash.put(hostname, router);

		
		try {
			int speed = Integer.parseInt(router_elem.getAttribute("speed"));
			router.getTimeManager().setDelay(speed);  //TODO: change, as speed is now topo wide
		}catch (Exception e){ /* ignore, this attribute is optional and may not be present */ }
		
		ClackDocument document = mFramework.getDocumentForTopology(router.getTopology());
		document.addRouter(router);
		host_model.document = document;
		RouterGraph graph = document.getRouterGraph(router.getHost());

		try {
			// this must be before elements are added to the router
			TopoRoutingTableCreator.setRouterLinkInfo(model.getHost(hostname), model.getLinks());
			
			if(isGUI){ 							
				graph.setDisplayElements(false);
			}
			RouterConfig.configRouterFromElement(router, router_elem);
		
			if(isGUI){
				graph.setDisplayElements(true);
				graph.repaint();
			}
			
			document.redrawTopoView();
			
			router.setup_routing_key = router_elem.getAttribute("setuprouting"); // remember this for when we serialize
			if(router.setup_routing_key.equals("1")) {
				TopoRoutingTableCreator.setRouterTable(model.getHost(hostname), model.getLinks());
			}else if(router.setup_routing_key.equals("d")){
				TopoRoutingTableCreator.setDefaultRoutingForHost(model.getHost(hostname));
			}
			
		} catch (Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(router.getDocument().getFramework(),"Error Configuring Router " + router.getHost());
		}	
		if(router.getProtocolManager() != null)
			router.getProtocolManager().start();
		router.start();
		return router;

	}
	
	public void closeAllConnections(){
		if(this.mFramework.isDebug())
			System.out.println("Closing all connections to VNS");
		Collection c = mTopologyModels.values();
		for(Iterator i  = c.iterator(); i.hasNext(); ){
			TopologyModel model = (TopologyModel)i.next();
			ArrayList allHosts = model.getHosts();
			for(int j = 0; j < allHosts.size(); j++){
				TopologyModel.Host h = (TopologyModel.Host)allHosts.get(j);
				if(h.document == null) continue;
				Router router = h.document.getRouter(h.name);
				if(router == null){
					System.err.println("error, could not find router " + router.getHost() + " to shutdown ");
					continue;
				}
				try {
					router.stopRouterAndDisconnect();
					router.join();
				}catch (Exception e){
					e.printStackTrace();
					ErrorReporter.reportError(router.getDocument().getFramework(), "Error Stopping Router");
				}
				
			}
		}
	}
	

	

	/**
	 * Gives a string representation of the topology range used to load a given topology
	 * from the configuration file (this is used when serializing topologies).  
	 * 
	 */
	public Element getSavedClackNode(int topology){
		return (Element)mSavedClackNodeMap.get(new Integer(topology));
	}

	public Router getRouterByName(String name){
		return (Router)mRouterHash.get(name);
	}
	

	
}
