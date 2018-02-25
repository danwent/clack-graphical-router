/**
 * 
 */
package net.clackrouter.routing;

import java.net.InetAddress;

/**
 * Data structure to hold information about a single link (i.e., an interface) that is directly
 * connected to a router. 
 * @author danwent
 *
 */

public class LocalLinkInfo {
	public LocalLinkInfo(InetAddress net, InetAddress msk, String i, InetAddress nh, int m, boolean up,
			boolean is_routing){
		local_iface = i; next_hop = nh; metric = m; is_up = up;
		address = net; subnet_mask = msk; is_routing_iface = is_routing;
	}
	/** IP address of this link's interface */
	public InetAddress address;
	/** IP subnet mask for this link */
	public InetAddress subnet_mask;
	/** name of the local interface */
	public String local_iface;
	/** IP address of the directly connected neighbor router interface */
	public InetAddress next_hop;
	/** routing metric cost for using this link to reach its neighbor (note: this value is one-way) */
	public int metric;
	/** tells whether this interface is currently active or down */
	public boolean is_up;
	/** tells whether the router should be running a routing protocol with its neighbor on this link */
	public boolean is_routing_iface;
	
	public static LocalLinkInfo copy(LocalLinkInfo old){
		return new LocalLinkInfo(old.address, old.subnet_mask, old.local_iface, 
				old.next_hop, old.metric, old.is_up, old.is_routing_iface );
	}

}