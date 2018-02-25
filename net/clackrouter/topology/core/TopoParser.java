
package net.clackrouter.topology.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.netutils.EthernetAddress;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This is a static class that parses the VNS topology file from the VNS server.
 * 
 * <p> Fills a list of provides ArrayLists with a lists of {@link TopologyModel.Host} and {@link TopologyModel.Link}
 * based on the contents of the topology file. </p> 
 */
public class TopoParser {
	
	public static String FIREWALL_NAME = "Gateway";
	public static String INTERNET_NAME = "internet";
	public static String FW_IFACE_ID = "-1";
	public static String INET_IFACE_ID = "-2";
	
	// parses the example for 'topology' and fills in the two arrays 'hosts' and 'links'
	// this method is called by the TopologyModel, which uses the two arrays to build a graph 
	// and model the topology
	// returns true iff we successfully connected
	public static void getHostAndWireLists(Element topo_element, ArrayList hosts, ArrayList links, boolean isLocal) throws Exception {
		
		Hashtable ifaceIDToHostMap = new Hashtable();
	
		NodeList router_elements = topo_element.getElementsByTagName("host");
		for(int i = 0; i < router_elements.getLength(); i++){
			Element elem = (Element) router_elements.item(i);
			TopologyModel.Host h = TopoParser.getHostFromElement(elem, ifaceIDToHostMap);
			hosts.add(h);	
		}

		// biga$$ for-loop to add links
		for(int i = 0; i < hosts.size(); i++){
			TopologyModel.Host host = (TopologyModel.Host) hosts.get(i);
			for(int j = 0; j < host.interfaces.size(); j++){
				TopologyModel.Interface iface = (TopologyModel.Interface)host.interfaces.get(j);
				int id = Integer.parseInt(iface.id);

				for(int k = 0; k < iface.connectedIfaces.size(); k++){

					int neighborId = Integer.parseInt((String)iface.connectedIfaces.get(k));
					if(id < neighborId){ // don't add twice
						TopologyModel.Interface otherIface = null;
						TopologyModel.Host host1 = (TopologyModel.Host)ifaceIDToHostMap.get("" + id);
						TopologyModel.Host host2 = (TopologyModel.Host)ifaceIDToHostMap.get("" + neighborId);
						if(host1 == null || host2 == null){
							System.err.println("Topo parser can't file host connected to " +
									host.name + " that has interface-id = '" + neighborId + "'");
							continue; 
						}
							
						for(int m = 0; m < host2.interfaces.size(); m++){
							TopologyModel.Interface neighborIface =  (TopologyModel.Interface) host2.interfaces.get(m);
							if(neighborIface.id.equals("" + neighborId)) {
								links.add(new TopologyModel.Link(host1, iface, host2, neighborIface, true));
								break;
							}
						}
						
					}
				}
			}
		}
		if(!isLocal){
			TopologyModel.Host fw = (TopologyModel.Host)ifaceIDToHostMap.get(FW_IFACE_ID);
			if(fw != null) { 
				addInternetBlock(hosts, links, fw);
			}
		}
	}
	
	
	//	 add fake internet  "hosts" to the topology if we are not
	// local (ie: we're actually connecting to VNS) and connect it to the firewall
	public static void addInternetBlock(ArrayList hosts, ArrayList links, TopologyModel.Host fw) {
	
		TopologyModel.Host inet = new TopologyModel.Host(INTERNET_NAME, false);
		TopologyModel.Interface inet_face = new TopologyModel.Interface("", INET_IFACE_ID, null, null, null);
		inet.interfaces.add(inet_face);	
		hosts.add(inet);
		links.add(new TopologyModel.Link(inet, inet_face, fw, (TopologyModel.Interface) fw.interfaces.get(1), true));
	}
	
	public static TopologyModel.Host getHostFromElement(Element elem, Hashtable ifaceIDToHostMap)throws Exception {
		String name = elem.getAttribute("name");
		System.out.println("name = " + name);
		String offlimits = elem.getAttribute("offlimits");
		boolean reservable = (!(offlimits.equals("1")));
		TopologyModel.Host host= new TopologyModel.Host(name, reservable);
		
		NodeList vlist = elem.getElementsByTagName("vinterface");
		for(int i = 0; i < vlist.getLength(); i++){

			TopologyModel.Interface iface = getInterfaceFromVirtualIfaceElement((Element) vlist.item(i),  name);
			host.interfaces.add(iface);	
			ifaceIDToHostMap.put(iface.id, host);
		}
		NodeList elist = elem.getElementsByTagName("einterface");
		for(int i = 0; i < elist.getLength(); i++){
			TopologyModel.Interface iface = getInterfaceFromPhysicalIfaceElement((Element) elist.item(i),  name);

			host.interfaces.add(iface);	
			ifaceIDToHostMap.put(iface.id, host);
		}

		if(name.equals(FIREWALL_NAME)){
			TopologyModel.Interface iface = new TopologyModel.Interface("eth0", FW_IFACE_ID, null, null, null);
			host.interfaces.add(iface);
			ifaceIDToHostMap.put(FW_IFACE_ID, host);
		}
		return host;
	}
	
	public static TopologyModel.Interface getInterfaceFromVirtualIfaceElement(Element elem,  String parentName) throws Exception {

		String id = elem.getAttribute("id");
		String name = elem.getAttribute("name");

		InetAddress address = InetAddress.getByName(elem.getAttribute("ip"));
		InetAddress mask = InetAddress.getByName(elem.getAttribute("mask"));
		EthernetAddress eth = null;
		if(elem.hasAttribute("ethaddr")){
			// if this topology file is for a local topology, there will also be an ethernet address
			eth = EthernetAddress.getByAddress(elem.getAttribute("ethaddr"));
		}

		TopologyModel.Interface iface = new TopologyModel.Interface(name, id, address, mask, eth);

		String[] neighbors = elem.getAttribute("neighbors").split(" ");
		for(int i = 0; i < neighbors.length; i++){
			iface.connectedIfaces.add(neighbors[i]);
		}
		return iface;
	}
	
	public static TopologyModel.Interface getInterfaceFromPhysicalIfaceElement(Element elem, String parentName) throws Exception {

	String id = elem.getAttribute("id");
	String name = elem.getAttribute("name");
	InetAddress ip = null;
	
	if(parentName.equals(FIREWALL_NAME)){
		ip = InetAddress.getByName(Translator.getString("FIREWALL_ADDRESS"));
	}else {
		ip = InetAddress.getByName(elem.getAttribute("ip"));
	}
	
	EthernetAddress hw_addr = EthernetAddress.getByAddress(elem.getAttribute("addr"));
	
	if(ip == null){
		System.err.println("Failed to find server IP for: " + parentName + " iface " + name);
	}

	TopologyModel.Interface iface = new TopologyModel.Interface(name, id, ip, null, hw_addr);
	String[] neighbors = elem.getAttribute("neighbors").split(" ");
	for(int i = 0; i < neighbors.length; i++){
		iface.connectedIfaces.add(neighbors[i]);
	}
	return iface;
}





}
