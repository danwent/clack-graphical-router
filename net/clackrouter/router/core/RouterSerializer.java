
package net.clackrouter.router.core;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.jgraph.pad.GPAttributeMap;
import net.clackrouter.router.graph.ComponentCell;
import net.clackrouter.router.graph.RouterGraph;
import net.clackrouter.router.graph.RouterWire;
import net.clackrouter.routing.RIPRoutingEntry;
import net.clackrouter.routing.RoutingTable;

import org.apache.crimson.tree.XmlDocument;
import org.jgraph.graph.GraphConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Serializes the current components within a router to a file, including their settings (if non-transient)
 * and their location on their screen.  
 * 
 * Note:  Routers can only be saved via the GUI.. at least for now.  
 */
public class RouterSerializer {
	
	
	/**
	 * Creates the whole XML doc object in memory representing the current state
	 * Creates the root node and appends all the  children to it.
	*/
	public static Element createRouterXMLElement(Document doc, Router router, RouterGraph graph, boolean saveTransient) {
		
		Element root = doc.createElement("router");
		Object[] all = graph.getAll();
		
		root.setAttribute("name", router.getHost());
		String isGUI = (router.isGUI()) ? "1" : "0";
		root.setAttribute("isgui", isGUI);
		root.setAttribute("setuprouting", router.setup_routing_key);

		
		Element components_element = buildComponentsElement(doc, graph.getVertices(all), saveTransient);
		Element connections_element = buildWiresElement(doc, graph.getEdges(all));
		
		root.appendChild(components_element);
		root.appendChild(connections_element);
		return root;
	}
	

	
	private static Element buildComponentsElement(Document doc, Object[] allComponents, boolean saveTransient){
		Element compRoot = doc.createElement("components");
		for(int i = 0; i < allComponents.length; i++){
			ComponentCell cell = (ComponentCell) allComponents[i];
			GPAttributeMap map = (GPAttributeMap)cell.getAttributes();
			ClackComponent clackComp = cell.getClackComponent();
			String bounds_str = getStringForRectangle(GraphConstants.getBounds(map));

			Element compElem = doc.createElement("component");
			compElem.setAttribute("class", clackComp.getClass().getName());
			compElem.setAttribute("name", clackComp.getName());
			compElem.setAttribute("bounds", bounds_str);
			compRoot.appendChild(compElem);
			
			createPropertiesElements(clackComp, compElem ,doc, saveTransient);
		}
		return compRoot;
	}
	
	private static void createPropertiesElements(ClackComponent comp, Element compElem, Document doc, boolean saveTransient){
		Properties props = comp.getSerializableProperties(saveTransient);
		for(Enumeration e = props.keys(); e.hasMoreElements(); ){
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			Element propElem = doc.createElement("property");
			propElem.setAttribute("key", key);
			propElem.setAttribute("value", value);
			compElem.appendChild(propElem);
		}
	}
	
	public static String getStringForRectangle(Rectangle2D rect){
		return (int)rect.getX() + ":" + (int)rect.getY() + ":" + (int) rect.getWidth() + ":" + (int) rect.getHeight() ;
	}
	
	private static Element buildWiresElement(Document doc, Object[] allWires){
		Element wireRoot = doc.createElement("wires");
		for(int i = 0; i < allWires.length; i++){
			RouterWire wire = (RouterWire) allWires[i];

			Element wireComp = doc.createElement("wire");
			wireComp.setAttribute("source_name", wire.sourceComponent.getName());
			wireComp.setAttribute("source_port", wire.sourcePortNum + "");
			wireComp.setAttribute("target_name", wire.targetComponent.getName());
			wireComp.setAttribute("target_port", wire.targetPortNum + "");			
			wireRoot.appendChild(wireComp);
		}
		return wireRoot;
	}
	
}
