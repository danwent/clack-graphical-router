/*
 * Created on May 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.GPAttributeMap;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.RouterSerializer;
import net.clackrouter.router.graph.RouterGraph;
import net.clackrouter.topology.graph.TopoHostCell;

import org.apache.crimson.tree.XmlDocument;
import org.jgraph.graph.GraphConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Static class used to save an enitirely new topology file.  
 * 
 * <p> Note: At this point, it will only save what routers are loaded 
 * (either visible or invisible routers).  Currently, we have a clack router
 * load every router in a topology, which means this is fine.  </p>
 */
public class TopoSerializer {	
	
	public static void serializeTopology(ClackFramework graphpad,  int topo_num, File file_to_save, boolean saveTransient) throws Exception {

			Writer out = new OutputStreamWriter(new FileOutputStream(
					file_to_save));
			TopologyManager manager = graphpad.getTopologyManager();
			TopologyModel model = manager.getTopologyModel(topo_num, null);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			dbf.setValidating(false);

			DocumentBuilder db = dbf.newDocumentBuilder();

			Element old_clack_node = manager.getSavedClackNode(topo_num);
			
			Document doc = db.newDocument();
			Element clack_element = doc.createElement("clack");
			String[] attrs = { "number", "server", "port", "username", "auth_key" }; 
			for(int i = 0; i < attrs.length; i++) { 
				String val = old_clack_node.getAttribute(attrs[i]); 
				if(val != null)
					clack_element.setAttribute(attrs[i], val);
			}
			doc.appendChild(clack_element);
			
			// for now we save the topo-view location based on the current document
			// not great, but it should work
			ClackDocument curDoc = graphpad.getCurrentDocument();
			TopoGraph topo_graph = (TopoGraph)curDoc.getTopologyView().getGraph();
			Element topolayout_elem = buildTopolayoutElement(doc, topo_graph);
			clack_element.appendChild(topolayout_elem);
			
			if(model.isLocalTopology()){
				// saves topology for future file load
				clack_element.appendChild(cloneTopologyElement(doc, model.getTopologyElement()));
			}
			
			ArrayList hosts = model.getHosts();
			for (int i = 0; i < hosts.size(); i++) {
				TopologyModel.Host h = (TopologyModel.Host)hosts.get(i);
				if(h.document != null){
					Router r = h.document.getRouter(h.name);
					RouterGraph graph = h.document.getRouterGraph(h.name);
					Element router = RouterSerializer.createRouterXMLElement(doc, r, graph, saveTransient);
					clack_element.appendChild(router);
				}
			}

			((XmlDocument) doc).write(out, "UTF-8"); // XMLDoc knows how to write itself				
			out.close();

			System.out.println("XML successfully written: "+ file_to_save.getName());

	}
	
	private static Element cloneIfaceElem(Document doc, String new_type, Element old_iface){
		Element new_iface = doc.createElement("vinterface");

		new_iface.setAttribute("id", old_iface.getAttribute("id"));
		new_iface.setAttribute("neighbors", old_iface.getAttribute("neighbors"));
		new_iface.setAttribute("ip", old_iface.getAttribute("ip"));
		String ethaddr = old_iface.getAttribute("ethaddr");
		if(ethaddr.length() > 0)
			new_iface.setAttribute("ethaddr", ethaddr);
		String mask = old_iface.getAttribute("mask");
		if(mask.length() > 0)
			new_iface.setAttribute("mask",mask );
		new_iface.setAttribute("name", old_iface.getAttribute("name"));
		return new_iface;
	}
	
	// we can't just use the old element, turns out we have to copy it to a new one 
	// created by this document object.  LAME.
	public static Element cloneTopologyElement(Document doc, Element old_topo_elem){
		Element topo_elem = doc.createElement("topology");
		NodeList host_elements = old_topo_elem.getElementsByTagName("host");
	
		for(int i = 0; i < host_elements.getLength(); i++){
			Element old_elem = (Element) host_elements.item(i);
			Element new_elem = doc.createElement("host");
			topo_elem.appendChild(new_elem);
			new_elem.setAttribute("name", old_elem.getAttribute("name"));
			
			// now, we need to copy each old iface element (of type "vinterface" 
			// or "einterface" to a new element of type "vinterface"
			// NOTE: since these topo files will be used only for local configs,
			// there is no point to maintaining the vinterface vs. einterface distinction
			NodeList viface_elems = old_elem.getElementsByTagName("vinterface");	
			for(int j = 0; j < viface_elems.getLength(); j++){
				Element old_iface = (Element)viface_elems.item(j);
				Element new_iface = cloneIfaceElem(doc, "vinterface", old_iface);
				new_elem.appendChild(new_iface);
			}
			
			NodeList eiface_elems = old_elem.getElementsByTagName("einterface");
			for(int j = 0; j < eiface_elems.getLength(); j++){
				Element old_iface = (Element)eiface_elems.item(j);
				Element new_iface = cloneIfaceElem(doc, "vinterface", old_iface);
				new_elem.appendChild(new_iface);
			}
		}
		
		return topo_elem;
	}
	

	public static Element buildTopolayoutElement(Document doc, TopoGraph graph){		
		Object[] all = graph.getDescendants(graph.getRoots());
		Object[] allcomponents = graph.getVertices(all);
		Element viewRoot = doc.createElement("topolayout");
		for(int i = 0; i < allcomponents.length; i++){
			if(allcomponents[i] instanceof TopoHostCell) {
				TopoHostCell cell = (TopoHostCell)allcomponents[i];
				Element elem = doc.createElement("host");
				GPAttributeMap map = (GPAttributeMap)cell.getAttributes();
				String bounds_str = RouterSerializer.getStringForRectangle(GraphConstants.getBounds(map));
				elem.setAttribute("name", cell.mHost.name);
				elem.setAttribute("bounds", bounds_str);
				viewRoot.appendChild(elem);
			}
		}
		return viewRoot;
	}
	

}
