/*
 * Created on Apr 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.create;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.ClackView;
import net.clackrouter.jgraph.pad.GPAttributeMap;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.RouterConfig;
import net.clackrouter.routing.LocalLinkInfo;
import net.clackrouter.topology.core.TopoGraph;
import net.clackrouter.topology.core.TopoParser;
import net.clackrouter.topology.core.TopoPopup;
import net.clackrouter.topology.core.TopologyManager;
import net.clackrouter.topology.core.TopologyModel;
import net.clackrouter.topology.core.TopologyModel.Host;
import net.clackrouter.topology.core.TopologyModel.Link;
import net.clackrouter.topology.graph.*;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;
import org.jgraph.layout.JGraphLayoutAlgorithm;
import org.jgraph.layout.SugiyamaLayoutAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * ClackView for the graph showing a VNS topology using a {@link TopoGraph}.  
 */
public class CreateTopologyView extends JPanel implements ClackView {


	private CreateTopologyGraph mGraph;
	private TopologyModel mTopologyModel;
	private Hashtable boundsRects;
	private Hashtable ifaceMap; 
	private int iface_id_counter; 
	
	public CreateTopologyView(String xml_file) throws Exception {
		boundsRects = new Hashtable();
		ifaceMap = new Hashtable();

		
		mTopologyModel = getModelFromFile(xml_file);
		mGraph = configureCreateTopologyGraph(mTopologyModel);
	
		iface_id_counter = ifaceMap.size() + 1; 
		
		this.add(mGraph);
		this.setBackground(Color.WHITE);
		
		mGraph.setEditable(false);

		CreateTopologyMouseListener listen = 
				new CreateTopologyMouseListener(mGraph);
		mGraph.addMouseListener(listen);
		
		mGraph.updateUI();
	}
	
	public Hashtable getInterfaceMap() { return ifaceMap; }
	
	public CreateTopologyGraph configureCreateTopologyGraph(TopologyModel model) {  

		CreateTopologyGraph graph = CreateTopologyGraph.initialGraph();
		Rectangle2D bounds = null;;
		
		ArrayList hosts = model.getHosts();
		ArrayList links = model.getLinks();	
		
		// add the internet block
		for(int i = 0; i < hosts.size(); i++){
			Host h = (Host)hosts.get(i);
			if(h.name.equals(TopoParser.FIREWALL_NAME)){
				TopoParser.addInternetBlock(hosts, links, h);
				break;
			}
		}
		
		for(int i = 0; i < hosts.size(); i++){
			Host h = (Host)hosts.get(i);
	
			if(boundsRects != null){
				String bounds_str = (String)boundsRects.get(h.name);
				if(bounds_str == null){
					System.err.println("error, could not bounds for " + h.name);
					continue;
				}
				bounds = RouterConfig.getRectangleFromString(bounds_str);

			}
			graph.insertHostIntoGraph(h, ifaceMap, bounds);
		}

		
		for(int i = 0; i < links.size(); i++){
			Link link = (Link)links.get(i);
			//making this call with a null view is fine, b/c this wire will
			// never be highlighted
			graph.connectInterfaces(null, model,link, ifaceMap);
		}
		


		return graph;
	}
	
	public void addLink(TopoHostCell host1_cell, String iface_name1, TopoHostCell host2_cell, 
												String iface_name2){
		
		// add the interfaces
	
		List toInsert = new LinkedList();
		
		AttributeMap mapPorts = new AttributeMap();
		
		GraphConstants.setBounds(mapPorts, new Rectangle(0, 0, 25, 25));
		GraphConstants.setOpaque(mapPorts, false);
		
		String id1 = "" + iface_id_counter++;
		String id2 = "" + iface_id_counter++;
		TopologyModel.Interface iface1 = new TopologyModel.Interface(iface_name1, id1, null, null, null);
		iface1.connectedIfaces.add(id2);
		host1_cell.mHost.interfaces.add(iface1);
		TopologyModel.Interface iface2 = new TopologyModel.Interface(iface_name2,id2 , null, null, null);
		iface2.connectedIfaces.add(id1);
		host2_cell.mHost.interfaces.add(iface2);
		
		TopoInterfaceCell port1 = new TopoInterfaceCell(iface1);
		host1_cell.add(port1);
		TopoInterfaceCell port2 = new TopoInterfaceCell(iface2);
		host2_cell.add(port2);
		
		toInsert.add(port1);
		toInsert.add(port2);
		
		ifaceMap.put(iface1.id, port1);
		ifaceMap.put(iface2.id, port2);
		
		Map viewMap = new Hashtable();	
		viewMap.put(port1, mapPorts);
		viewMap.put(port2, mapPorts);
		
		
		// now add the link
		
		TopologyModel.Link link = new TopologyModel.Link(host1_cell.mHost, iface1, host2_cell.mHost,
														iface2, true);
		
		AttributeMap map = new AttributeMap();

	
		CellView[] views = mGraph.getGraphLayoutCache().getMapping(
				new DefaultGraphCell[] { (DefaultGraphCell)port1, (DefaultGraphCell)port2 }, true );

		PortView view1 = (PortView)views[0];
    	PortView view2 = (PortView)views[1];
			
		Point2D temp1 = view1.getLocation(null);
		Point p = new Point((int) temp1.getX(), (int) temp1.getY());
		Point2D temp2 = view2.getLocation(null);
		Point p2 = new Point((int) temp2.getX(), (int) temp2.getY());
		
		ArrayList list = new ArrayList();
		list.add(p);
		list.add(p2);
		GraphConstants.setPoints(map, list);
		GraphConstants.setEndFill(map, false);
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_NONE);
		GraphConstants.setLineWidth(map, (float)2.5);
		

		TopoWire edge = new TopoWire("",null, mTopologyModel, link);
		link.addListener(edge);
		edge.setAttributes(new GPAttributeMap()); // install custom attribute map

		viewMap.put(edge, map);
		toInsert.add(edge);
	
		ConnectionSet cs = new ConnectionSet();
		
		//System.out.println("Connecting " + source +  " to " + target);
		cs.connect(edge, view1.getCell(), true);
		cs.connect(edge, view2.getCell(), false);
		
		// insert into graph
		
		mGraph.getModel().insert(
				toInsert.toArray(),
				viewMap,
				cs,
				null,
				null);
		
		mGraph.placePorts();
	}
	
	
	private TopologyModel getModelFromFile(String fname){
		
		try {
		FileInputStream fis = new FileInputStream(fname);
		
        DocumentBuilderFactory dbf =
    		DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
    
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc  = db.parse(fis);
        Element clack_node = (Element)doc.getDocumentElement();
		TopologyModel model = TopologyManager.getTopologyModelFromXML(TopologyManager.NO_TOPOLOGY, 
											clack_node, true);	
		
		// also parse the XML to record the Bounding Rectangle for each host 
		NodeList topolayout_nodes = clack_node.getElementsByTagName("topolayout");
		if(topolayout_nodes.getLength() > 0) {

			NodeList host_nodes = ((Element)topolayout_nodes.item(0)).getElementsByTagName("host");
			
			for(int i = 0; i < host_nodes.getLength(); i++){
				Element hnode = (Element)host_nodes.item(i);
				boundsRects.put(hnode.getAttribute("name"), hnode.getAttribute("bounds"));
			}

		}
		
		
		return model;
		
		} catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		return null; // never reached
	}
	

	public CreateTopologyGraph getGraph() { return mGraph; }
	public Component getComponent() { return this; }
	
	public void layoutTopoGraph()throws Exception {
       
		mGraph.placePorts();
	}
	

	public Object getSelectionCell() { return mGraph.getSelectionCell(); }
	
	public boolean isVisibleView() { return true; }
	
	public void setIsVisibleView(boolean b) { /*ignore */; }
	
	public TopologyModel getTopologyModel() { return mTopologyModel; }
}
