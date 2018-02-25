package net.clackrouter.topology.create;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.RouterSerializer;
import net.clackrouter.router.graph.RouterGraph;
import net.clackrouter.topology.core.TopoSerializer;
import net.clackrouter.topology.core.TopologyModel;
import net.clackrouter.topology.core.TopologyModel.Host;
import net.clackrouter.topology.core.TopologyModel.Interface;

import org.apache.crimson.tree.XmlDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CTopoSerializer {

	public static void serializeCTopo(CreateTopologyView cview, File save_file){
		try { 
		System.out.println("saving topolayout to: " + save_file.getName());
		
		// write it out to XML
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();

		Element clack_element = doc.createElement("clack");
		doc.appendChild(clack_element);
		Element topolayout_element = TopoSerializer.buildTopolayoutElement(doc,
				cview.getGraph());
		clack_element.appendChild(topolayout_element);
		
		TopologyModel model = cview.getTopologyModel();
		clack_element.appendChild(buildTopologyElement(doc, model));
		
		// while we are at it, why not print out some <router/> elements so
		// that the user could also load and run this file locally, without
		// contacting VNS.  As of now, these topos will not function fully 
		// b/c some interfaces will have address 0.0.0.0, but the user could 
		// change that manually
		ArrayList hosts = model.getHosts();
		for (int i = 0; i < hosts.size(); i++) {
			TopologyModel.Host h = (TopologyModel.Host)hosts.get(i);
			if(h.name.equals("firewall")) continue;
			if(h.name.equals("internet")) continue; 
			Element r_elem = doc.createElement("router");
			r_elem.setAttribute("name", h.name);
			r_elem.setAttribute("isgui", "1");
			clack_element.appendChild(r_elem);
		}	
		
		Writer out = new OutputStreamWriter(new FileOutputStream(
				save_file));
		((XmlDocument) doc).write(out, "UTF-8"); // XMLDoc knows how to write itself				
		out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	// unlike cloneTopologyElement, this rebuilds a topology element from scratch.
	// This is needed by the create topology code, which may change the make-up of
	// the topology
	public static Element buildTopologyElement(Document doc, TopologyModel model){
		Element topo_elem = doc.createElement("topology");
		ArrayList hosts = model.getHosts();
		

		for(int i = 0; i < hosts.size(); i++){
			Host h = (Host)hosts.get(i);
			if(h.name.equals("internet"))
				continue;
			
			String iface_type = "vinterface";
			Element new_elem = doc.createElement("host");
			topo_elem.appendChild(new_elem);
			new_elem.setAttribute("name", h.name);
			if(h.name.equals("firewall")){
				new_elem.setAttribute("offlimits", "1");
				iface_type = "einterface";
			}
			for(int j = 0; j < h.interfaces.size(); j++) {
				Interface iface = (Interface)h.interfaces.get(j); 
				if(iface.id.equals("-1"))
					continue; // this is a fake link btw firewall and internet
				Element new_iface = doc.createElement("vinterface");

				new_iface.setAttribute("id", "" + iface.id);
				new_iface.setAttribute("name", iface.name);
				if(iface.address != null)
					new_iface.setAttribute("ip", iface.address.getHostAddress());
				if(iface.mask != null)
					new_iface.setAttribute("mask", iface.mask.getHostAddress());
				if(iface.hw_addr != null)
					new_iface.setAttribute("ethaddr", iface.hw_addr.toString());
				String neighbor_str = "";
				for(int k = 0; k < iface.connectedIfaces.size(); k++){
					if(k != 0)
						neighbor_str += " "; 
					neighbor_str += iface.connectedIfaces.get(k);
				}
				new_iface.setAttribute("neighbors", neighbor_str);
				new_elem.appendChild(new_iface);
			}
		}
		return topo_elem;
	}
	

}
