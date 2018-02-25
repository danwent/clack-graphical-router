package net.clackrouter.routing;

import java.net.InetAddress;


/**
 * Extension of {@link RoutingEntry} to also hold RIP data within a {@link RoutingTable}.  
 */

public class RIPRoutingEntry extends RoutingEntry {
	
	public static int INFINITE_COST = 16;
	public static short MAX_TTL = 20;
	public static short NO_TTL = -1;

	/**
	 * the time-to-live of this routing entry
	 */
	public short ttl;
	/**
	 * the cost of reaching this network
	 */
	public int cost;
	/**
	 * whether this network is local (i.e., connected) to this router
	 */
	public boolean isLocal;
	
	//TODO: this is backwards from how other RoutingEntry constructors work
	//TODO: make this private
	public RIPRoutingEntry( InetAddress dest, InetAddress mask, InetAddress next, 
            String iface, short t, int c, boolean local){
		super(dest, next, mask, iface);
		cost = c;
		ttl = t;
		isLocal = local;
	}
	
	public static RIPRoutingEntry createLocalRIPRoutingEntry(InetAddress network,
							InetAddress mask, String local_interface, int cost) {
		return new RIPRoutingEntry(network, mask, null, local_interface, NO_TTL, cost, true);
	}
	
	public static RIPRoutingEntry createNonLocalRIPRoutingEntry(InetAddress network,
							InetAddress mask, InetAddress nextHop, String interface_name,
							short ttl, int cost) {
		return new RIPRoutingEntry(network, mask, nextHop, interface_name, ttl,cost, false);
	}
	
}
