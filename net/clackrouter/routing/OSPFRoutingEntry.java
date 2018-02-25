package net.clackrouter.routing;

import java.net.InetAddress;

import net.clackrouter.packets.LinkStatePacket;


/**
 * Extension of {@link RoutingEntry} to also hold OSPF data within a {@link RoutingTable}.  
 */

public class OSPFRoutingEntry extends RoutingEntry  {

		public boolean isLocal;
		public int cost;
		
		public OSPFRoutingEntry( InetAddress dest, InetAddress next, InetAddress mask,  
	            String iface, int c, boolean local){
			super(dest, next, mask, iface);
			isLocal = local;
			cost = c;
		}
		
	
}
