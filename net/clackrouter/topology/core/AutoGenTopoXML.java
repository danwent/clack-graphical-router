package net.clackrouter.topology.core;

import java.io.*;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.clackrouter.jgraph.pad.resources.Translator;

import org.apache.crimson.tree.XmlDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class AutoGenTopoXML {
	
	// a simple utility to print out the topology information (IP's, connected ifaces, etc)
	// based on high-level network connectivity.  
	// WARNING: this function assumes point to point links (ie: /31's) between each host
	public static void main(String[] args){
		
		if(args.length != 1) { 
			System.out.println("Please give a file of <host1:host2> pairs describing the network");
			return;
		}
		
	    try {
	         FileInputStream fin =  new FileInputStream(args[0]);
	          
	           BufferedReader myInput = new BufferedReader
	               (new InputStreamReader(fin));
	           
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setValidating(false);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.newDocument();
				
				Element topology_element = doc.createElement("topology");
				doc.appendChild(topology_element);
				
				Hashtable<String, Element> elem_map = new Hashtable<String, Element>();
				
				
				String ip_prefix = "171.64.1.";
				String ether_prefix = "10:20:30:40:50:"; // we just append the iface id
				int current_hostnum = 2;
				int current_ifaceid = 1;
				
		        String line;
		        while ((line = myInput.readLine()) != null) { 
		        	String[] names = line.split(":");
		        	if(names.length != 2) {
		        		System.err.println("malformed line: " + line);
		        		return;
		        	}
		        	Element elem1 = elem_map.get(names[0]);
		        	if(elem1 == null){
		        		elem1  = doc.createElement("host");
		        		elem1.setAttribute("name", names[0]);
		        		topology_element.appendChild(elem1);
		        		elem_map.put(names[0], elem1);
		        	}
		        	Element elem2 = elem_map.get(names[1]);
		        	if(elem2 == null){
		        		elem2  = doc.createElement("host");
		        		elem2.setAttribute("name", names[1]);
		        		topology_element.appendChild(elem2);
		        		elem_map.put(names[1], elem2);
		        	}
		        	
		        	// now, lets add a vinterface for each to represent this 
		        	// link.  for now, assume all links are point-to-point
		        	// e.g. <vinterface id="4" neighbors="3" ip="10.1.1.149" ethaddr="10:20:30:40:50:20" mask="255.255.255.252" name="eth0" />
		 
		        	String id1 = "" + current_ifaceid++;
		        	String id2 = "" + current_ifaceid++;
		        	String ip1 = ip_prefix + current_hostnum++;
		        	String ip2 = ip_prefix + current_hostnum++;
		        	
		        	Element vinterface1 = doc.createElement("vinterface");
		        	vinterface1.setAttribute("id",  id1);
		        	vinterface1.setAttribute("neighbors", id2 );
		        	vinterface1.setAttribute("ip", ip1);
		        	vinterface1.setAttribute("ethaddr", ether_prefix + id1);
		        	vinterface1.setAttribute("mask", "255.255.255.254");
		        	int iface_count = elem1.getElementsByTagName("vinterface").getLength();
		        	vinterface1.setAttribute("name", "eth" + iface_count);
		        	elem1.appendChild(vinterface1);
		        	
		        	Element vinterface2 = doc.createElement("vinterface");
		        	vinterface2.setAttribute("id",  id2);
		        	vinterface2.setAttribute("neighbors", id1 );
		        	vinterface2.setAttribute("ip", ip2);
		        	vinterface2.setAttribute("ethaddr", ether_prefix + id2);
		        	vinterface2.setAttribute("mask", "255.255.255.254");
		        	iface_count = elem2.getElementsByTagName("vinterface").getLength();
		        	vinterface2.setAttribute("name", "eth" + iface_count);
		        	elem2.appendChild(vinterface2);		        	

		        	
		        }

			
				PrintWriter writer = new PrintWriter(System.out);
				//((XmlDocument) doc).write(, "UTF-8"); // XMLDoc knows how to write itself
	            ((XmlDocument)doc).write(writer);

	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	}


}
