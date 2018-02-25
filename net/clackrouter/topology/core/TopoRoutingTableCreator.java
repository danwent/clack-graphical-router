/*
 * Created on May 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.core;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import net.clackrouter.component.simplerouter.InterfaceIn;
import net.clackrouter.router.core.Router;
import net.clackrouter.routing.LocalLinkInfo;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;


/**
 * This creates niave routing tables based on individual hosts, subnets, in a topology.
 * 
 * <p> This niave algorithm is basically dijkstra's shortest path, used to automatically
 * populate routing tables. </p> 
 */
public class TopoRoutingTableCreator {

	public static void setRouterLinkInfo(TopologyModel.Host host, ArrayList links) {
		Router router = host.document.getRouter(host.name);
		if(router == null) {
			System.err.print("SetRouterLinkInfo called, but router is null");
			return;		
		}
		
		for(int i = 0; i < links.size(); i++){
			TopologyModel.Link link = (TopologyModel.Link)links.get(i);

			if(link.host1.name.equals(host.name)) {
				boolean is_routing_iface = (link.host1.interfaces.size() > 1)
										&& (link.host2.interfaces.size() > 1);
				router.addLocalLinkInfo(link.iface1.address, link.iface1.mask, 
						link.iface2.address, link.iface1.name, link.isEnabled, Router.DEFAULT_ROUTE_METRIC,
						is_routing_iface);
				
				// also check to see if we need to add a default route
				if(link.host2.name.equals(TopoParser.FIREWALL_NAME)){
					router.setDefaultRouting(link.iface2.address, link.iface1.name);
				}
				
			}
			if(link.host2.name.equals(host.name)) {
				boolean is_router = link.host1.reservable;
				router.addLocalLinkInfo(link.iface2.address, link.iface2.mask,
						link.iface1.address, link.iface2.name, link.isEnabled, Router.DEFAULT_ROUTE_METRIC,
						is_router);
				
				// also check to see if we need to add a default route
				if(link.host1.name.equals(TopoParser.FIREWALL_NAME)){
					router.setDefaultRouting(link.iface1.address, link.iface2.name);
				}
				
			}
		}		
	}
	
	/**
	 * Takes a host and adds a default route out eth0
	 */
	public static void setDefaultRoutingForHost(TopologyModel.Host host){
		System.out.println("setting default route for host: " + host.name); 
		Router router = host.document.getRouter(host.name);
		if(router == null) return;	
		
		Hashtable local_link_info = router.getLocalLinkInfo();
		LocalLinkInfo eth0 = (LocalLinkInfo)local_link_info.get("eth0");
		if(eth0 == null){
			System.err.println("Error: default routing requested but no eth0 is present");
			return;
		}	
		RoutingTable table = router.getRoutingTable();
		try {
			RoutingEntry new_entry = new RoutingEntry(InetAddress.getByName("0.0.0.0"),
				eth0.next_hop, InetAddress.getByName("0.0.0.0"), null);
			table.addRemoteEntry(new_entry);
		} catch (Exception e){
			e.printStackTrace();
		}		

	}
	
	public static void setRouterTable(TopologyModel.Host host, ArrayList links) throws Exception {

		Router router = host.document.getRouter(host.name);
		if(router == null) return;
		
		RoutingTable table = router.getRoutingTable();
		table.clear(); // get rid of anything in it
		
		
		ArrayList queue = new ArrayList();
		Properties ifaceToNextHop = new Properties();
		
		// seed our breadth-first search queue with our nearest neighbors
		for(int i = 0; i < links.size(); i++){
			TopologyModel.Link link = (TopologyModel.Link)links.get(i);
			if(!link.isEnabled)
				continue;
			if(link.host1.name.equals(host.name)){
				ifaceToNextHop.setProperty(link.iface1.name, link.iface2.address.getHostAddress());
				queue.add(new RoutePair(link.iface1, link.host2));
			}
			if(link.host2.name.equals(host.name)){
				ifaceToNextHop.setProperty(link.iface2.name, link.iface1.address.getHostAddress());
				queue.add(new RoutePair(link.iface2, link.host1));
			}
		}
		
		// search loop
		while(queue.size() > 0){
			
			RoutePair pair = (RoutePair)queue.remove(0);
			if(hostExistsInRoutingTable(pair.nextHopHost, table))
				continue; // we already have a better route, forget it!
			
			// add a route to each interface on this host
			for(int i = 0; i < pair.nextHopHost.interfaces.size(); i++){
				TopologyModel.Interface iface = (TopologyModel.Interface) pair.nextHopHost.interfaces.get(i);
				
				if(pair.nextHopHost.name.equals(TopoParser.FIREWALL_NAME) ){
					// only add this route once
					if(table.longestPrefixMatch(InetAddress.getByName("0.0.0.0")) == null){
						RoutingEntry new_entry = new RoutingEntry(InetAddress.getByName("0.0.0.0"),
								InetAddress.getByName(ifaceToNextHop.getProperty(pair.iface.name)), 
								InetAddress.getByName("0.0.0.0"), pair.iface.name);
						table.addEntry(new_entry);
						
						// Special case: 
						// The router with the link to the Gateway will have a default route to the 
						// gateway, which is used for traffic to all hosts other than those hosts that
						// exist in the topolgoy.  However, there is an odd case when VNS gives us a 
						// prefix that contains IPs that do not correspond to hosts.  Stanford will route
						// such a packet to the VNS topology, then the VNS topology will route the packet back
						// to stanford because it matches only the default route to the Internet. 
						// To handle this, we add a null route (i.e., drop the packet) for the entire prefix.
						// This is less specific than the hosts routes, and thus only impacts unused IPs.  
						try {
							new_entry = new RoutingEntry(pair.iface.address,null, pair.iface.mask, null); 
							table.addLocalEntry(new_entry);
						} catch (Exception e){
							e.printStackTrace();
						}
					}
				}else {
					RoutingEntry new_entry = new RoutingEntry(iface.address, InetAddress.getByName(ifaceToNextHop.getProperty(pair.iface.name)),
							InetAddress.getByName("255.255.255.255"), pair.iface.name);
					table.addEntry(new_entry);
				}
			}
			
			// note in this for-loop that the iface we use is the one on
			// the router for which we are filling a table!
			// thus, it is the same iface that led us to add a route entry above
			for(int i = 0; i < links.size(); i++){
				TopologyModel.Link link = (TopologyModel.Link)links.get(i);
				if(!link.isEnabled)
					continue;
				if(link.host1.name.equals(pair.nextHopHost.name) && !link.host2.name.equals(host.name)&& !link.host2.name.equals(TopoParser.INTERNET_NAME))
					queue.add(new RoutePair(pair.iface, link.host2));
				if(link.host2.name.equals(pair.nextHopHost.name)&& !link.host1.name.equals(host.name) && !link.host1.name.equals(TopoParser.INTERNET_NAME))
					queue.add(new RoutePair(pair.iface, link.host1));			
			}

		}
		
		router.setRoutingTable(table);
	}
	
	
	private static boolean hostExistsInRoutingTable(TopologyModel.Host h, RoutingTable table){
		for(int i = 0; i < table.numEntries(); i++){
			RoutingEntry entry = (RoutingEntry)table.getEntry(i);
			for(int j = 0; j < h.interfaces.size(); j++){
				TopologyModel.Interface iface = (TopologyModel.Interface)h.interfaces.get(j);
				if(entry.network.equals(iface.address))
					return true;
			}
		}
		return false;
	}
	
	private static class RoutePair {
		TopologyModel.Interface iface;
		TopologyModel.Host nextHopHost;
		public RoutePair(TopologyModel.Interface i, TopologyModel.Host h) { iface = i; nextHopHost = h; }
	}

}
