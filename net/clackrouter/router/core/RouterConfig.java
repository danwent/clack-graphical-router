
package net.clackrouter.router.core;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import net.clackrouter.application.ApplicationManager;
import net.clackrouter.application.ClackApplication;
import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.component.simplerouter.InterfaceIn;
import net.clackrouter.component.simplerouter.InterfaceOut;
import net.clackrouter.router.graph.ComponentCell;
import net.clackrouter.router.graph.RouterGraphHelper;

import org.jgraph.graph.GraphConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



/**
 * Configures a route based on the context of a saved xml file that details what components
 * are included in the router, how they are configured, and where they are located on the 
 * screen.  
 */
public class RouterConfig {
		
	protected static Hashtable componentCellMap; // holds mapping from component name to ComponentCell object
	protected static Hashtable clackComponentMap; // holds mapping from component name to ClackComponent object
		

	
	/*
	 * This is the mechanism for getting a RouterConfig object representing the 
	 * XML file given as input
	 */
	public static synchronized void parseRouterConfigXML(Router router, InputStream is)
			throws Exception {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setValidating(false);

		DocumentBuilder db = null;
		db = dbf.newDocumentBuilder();

		Document doc = db.parse(is);
		Element router_elem =  doc.getDocumentElement();	
		configRouterFromElement(router, router_elem);
	}
		
		
	public static synchronized void configRouterFromElement(Router router, Element router_node) throws Exception {
	
		
		NodeList component_list = router_node.getElementsByTagName("components");
		NodeList wire_list = router_node.getElementsByTagName("wires");
		if(component_list.getLength() != 1 || wire_list.getLength() != 1){
			// this will make a recursive call back to this function, but with a default router element
			default_configure(router);
			return;
		}
		
		addAllInterfaces(router);
			
		NodeList comps = ((Element)component_list.item(0)).getElementsByTagName("component");
		NodeList wires = ((Element)wire_list.item(0)).getElementsByTagName("wire");
		if(router.getDocument().getFramework().isDebug()){
			System.out.println("loading router '" + router.getHost() + "' with " + comps.getLength() 
				+ " components and " + wires.getLength() + " wires");	
		}
		addComponents(router, comps);

		addWires(router, wires);
		
		NodeList app_list = router_node.getElementsByTagName("applications");
		if(app_list.getLength() == 1){
			NodeList apps = ((Element)app_list.item(0)).getElementsByTagName("application");
			addApplications(apps, router);
		}
		
	}
	
	// if no internal configuration is specified for a router, we use a 
	// default XML file depending on the number of ifaces the router has
	public static void default_configure(Router router){
		int num_ifaces = router.getInputInterfaces().length;
		String xml_filename = "";

		if(num_ifaces == 1) xml_filename = "/xml/one_interface.xml";
		else if(num_ifaces == 2) xml_filename = "/xml/two_interface.xml";
		else if(num_ifaces == 3)xml_filename = "/xml/three_interface.xml";
		else if(num_ifaces == 4)xml_filename = "/xml/four_interface.xml";
		else {
				System.err.println("No default router config for routers with " + num_ifaces + " interfaces.  Tell Dan to add one!");
				return;
		}
		
		try {
			URL url = router.getClass().getResource(xml_filename);
			URLConnection conn = url.openConnection();
			// configure router, now that we have some XML to work with
			parseRouterConfigXML(router, conn.getInputStream());
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	// simply adds the default interfaces to a router graph.  
	public static synchronized void addAllInterfaces(Router router){
		componentCellMap = new Hashtable();
		clackComponentMap = new Hashtable();
		
    	InterfaceIn[] ifaces_in = router.getInputInterfaces();
    	InterfaceOut[] ifaces_out = router.getOutputInterfaces();
    	
    	for(int i = 0; i < ifaces_in.length; i++){
    		String in_name = ifaces_in[i].getName();
    		String out_name = ifaces_out[i].getName();
    		clackComponentMap.put(in_name, ifaces_in[i]);
    		clackComponentMap.put(out_name, ifaces_out[i]);
   
    		RouterGraphHelper clackHelper = router.getDocument().getFramework().getClackGraphHelper();
    		Rectangle bounds1 = new Rectangle(300 + 100 * i, 20, 50, 50);
    		ComponentCell cell1 = (ComponentCell)clackHelper.addRouterComponentCell(router, ifaces_in[i], bounds1);
    		componentCellMap.put(in_name, cell1);
    		Rectangle bounds2 = new Rectangle(300 + 100 * i, 400, 50, 50);
    		ComponentCell cell2 = (ComponentCell)clackHelper.addRouterComponentCell(router, ifaces_out[i], bounds2);
    		componentCellMap.put(out_name, cell2);

    	}
    	

	}
	
	private static void addComponents(Router router, NodeList components) throws Exception {

		// we want to initialize clack components only after all components have been created.  this 
		// avoids race-condition where the initialization of one component requires a reference to another
		Hashtable<ClackComponent,Properties> comp_properties = new Hashtable<ClackComponent,Properties>();
		
		
		for (int i = 0; i < components.getLength(); i++) {
			Element comp = (Element) components.item(i);

				String class_name = comp.getAttribute("class");
				String comp_name = comp.getAttribute("name");
				String bounds_str = comp.getAttribute("bounds");
				Rectangle2D bounds = getRectangleFromString(bounds_str);
				Class class_obj = Class.forName(class_name);
				ClackComponent clack_comp = null;
			//	System.out.println("loading " + comp_name + " of type " + class_name);
				if (router.isGUI()) {

					ComponentCell cell = null;
					cell = (ComponentCell) componentCellMap.get(comp_name);
					if (cell != null) {
						//handle interfaces which have already been added, just
						// need to set bounds
						GraphConstants.setBounds(cell.getAttributes(), bounds);
					}
				}

	
				
				// interfaces are automatically added to the router upon reservation
				if (Interface.class.isAssignableFrom(class_obj)) {
					// if we're local, we may need to configure from file...
					if(router.isLocalRouter()) {
						Properties props = getClackProperties(comp);
						clack_comp = router.getComponent(comp.getAttribute("name"));
						//clack_comp.initializeProperties(props);
						comp_properties.put(clack_comp, props);
					}	

					continue; 
				}

				try {
					Constructor constructor = class_obj
							.getConstructor(new Class[] { String.class });
					clack_comp = (ClackComponent) constructor
							.newInstance(new Object[] { comp_name });
				} catch (NoSuchMethodException e) {
					/* do not report exception, this is normal*/
					Constructor constructor = class_obj
							.getConstructor(new Class[] { Router.class,
									String.class });
					clack_comp = (ClackComponent) constructor
							.newInstance(new Object[] { router, comp_name });
				}
				router.addComponent(clack_comp);
				Properties props = getClackProperties(comp);
				comp_properties.put(clack_comp, props);
				//clack_comp.initializeProperties(props);
				clackComponentMap.put(comp_name, clack_comp);

				RouterGraphHelper clackHelper = router.getDocument()
							.getFramework().getClackGraphHelper();
				ComponentCell cell = (ComponentCell) clackHelper
							.addRouterComponentCell(router, clack_comp, bounds);
				componentCellMap.put(comp_name, cell);
	
		}
		
		// now initialize everything
		for(Enumeration<ClackComponent> e = comp_properties.keys(); e.hasMoreElements(); ){
			ClackComponent comp = e.nextElement();
			Properties props = comp_properties.get(comp);
			comp.initializeProperties(props);
		}
		
	}
		
		private static Properties getClackProperties(Element clackElem){
			Properties props = new Properties();
			NodeList list = clackElem.getElementsByTagName("property");
			for(int i = 0; i < list.getLength(); i++){
				Element p = (Element) list.item(i);
				props.setProperty(p.getAttribute("key"), p.getAttribute("value"));		
			}
			return props;
		}
		
		public static Rectangle2D getRectangleFromString(String s){
			String[] dims = s.split(":");
			return new Rectangle(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]), 
					Integer.parseInt(dims[2]), Integer.parseInt(dims[3]));	
		}
		
		// should be called after addComponents()
		private static void addWires(Router router, NodeList wires) throws Exception {
		
			String error = null;
			for(int i = 0; i < wires.getLength(); i++){
				Element wireComp = (Element) wires.item(i);
					String source_name = wireComp.getAttribute("source_name");
					int source_port = Integer.parseInt(wireComp.getAttribute("source_port"));
					String target_name = wireComp.getAttribute("target_name");
					int target_port = Integer.parseInt(wireComp.getAttribute("target_port"));
					
					ClackComponent source = (ClackComponent)clackComponentMap.get(source_name);
					ClackComponent dest = (ClackComponent)clackComponentMap.get(target_name);
					if(source == null || dest == null){
						System.err.println("source: " + source + " dest: " + dest);
						continue;
					}

					if(source_port < source.getNumPorts() || target_port < dest.getNumPorts()){	
						ClackPort sourcePort = source.getPort(source_port);
						ClackPort destPort = dest.getPort(target_port);
						error = ClackPort.connectPorts(sourcePort, destPort);
					}else {
						error = "invalid port number specified";
					}
					if(error != null){
						RouterGraphHelper.showConnectionErrorDialog(router.getDocument(),error);
						continue;
					}
					ComponentCell source_cell = (ComponentCell) componentCellMap.get(source_name);
					ComponentCell target_cell = (ComponentCell) componentCellMap.get(target_name);	
					if(source_cell == null){
						RouterGraphHelper.showConnectionErrorDialog(router.getDocument(),"Error: Could not find component " + source_name + 
							". Skipping addition of wire connecting it to " + target_name);
						continue;
					}
					if(target_cell == null){
						RouterGraphHelper.showConnectionErrorDialog(router.getDocument(),"Error: Could not find component " + target_name + 
							". Skipping addition of wire connecting it to " + source_name);
						continue;
					}
		
					RouterGraphHelper clackHelper = router.getDocument().getFramework().getClackGraphHelper();
					if(error == null)
						clackHelper.addRouterWire(source_cell, source_port, target_cell, target_port);
					else 
						RouterGraphHelper.showConnectionErrorDialog(router.getDocument(), error);

			}		
		}
		
		/**
		 * Starts any clack applications specified by the config file
		 * @param app_nodes
		 * @param router
		 */
		private static void addApplications(NodeList app_nodes, Router router){
			for(int i = 0; i < app_nodes.getLength(); i++){
					Element comp = (Element) app_nodes.item(i);
					String[] args = comp.getAttribute("cmd").split(" ");				
					try {
						System.out.println("Loading application: " + comp.getAttribute("cmd"));
						ApplicationManager app_manager = router.getDocument().getFramework().getApplicationManager();
						app_manager.runApplication(router, args, null); // clack shell is null
					} catch (Exception e){
						e.printStackTrace();
					}
			}
		}
	
		
	/**
	 * Simple struct to hold information about an attempt to connect to the VNS server.  
	 */
	public static class ConnectionInfo {
		public String server;
		public int port;
		public String host;
		public int topo_min;
		public int topo_max;
	}

}
