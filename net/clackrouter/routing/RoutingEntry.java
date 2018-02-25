

package net.clackrouter.routing;

import java.net.InetAddress;

import net.clackrouter.netutils.NetUtils;

/**
 * Represents a single routing entry contained by a {@link RoutingTable}.  
 */

public class RoutingEntry implements Comparable
{
	/**
	 * The network portion of the destination address for this entry
	 */
    public InetAddress network;
	/**
	 * The mask portion of the destination address for this entry
	 */
    public InetAddress mask;
	/**
	 * IP Address of the next-hop router for this entry
	 */
    public InetAddress nextHop;
	/**
	 * Name of outgoing interface for the next-hop associated with this entry
	 */
    public String interface_name;
    
	// used for GUI highlighting
	public long last_access = 0;

    public RoutingEntry ( InetAddress dest, InetAddress next, InetAddress net_mask,
            String iface)
    {
        network = dest;
        nextHop     = next;
        mask        = net_mask;
        interface_name   = iface;
    } // -- VNSRoutingEntry

    public InetAddress getDestination()
    { return network; }
    public InetAddress getMask()
    { return mask; }
    public InetAddress getNextHop()
    { return nextHop; }
    public String getInterface()
    { return interface_name; }

    public boolean isDefault()
    { return NetUtils.getIntFromAddr(mask) == 0; }

    public boolean matches(InetAddress destIn)
    {
        int destval = NetUtils.getIntFromAddr(destIn);
        int mydest  = NetUtils.getIntFromAddr(network);
        int mymask  = NetUtils.getIntFromAddr(mask);

        if ( (mydest & mymask) == (destval & mymask) )
        { return true; }

        return false;
    } // --  matches

    public int compareTo(Object obj) 
    {
        RoutingEntry entry = (RoutingEntry)obj;
        if ( obj == null )
        { throw new ClassCastException(); }

        /*dw: BUG FIX, need to be careful with java unsigned-ness! */
        long result = (NetUtils.getLongFromAddr(mask) - NetUtils.getLongFromAddr(entry.mask));

        if(result > 0) return -1;
        if(result < 0) return 1;
        
        return 0;
    } // -- compareTo

    public String toString()
    {
    	String next_hop = "*";
    	if(nextHop != null) next_hop = nextHop.getHostAddress();
    	String iface = "local";
    	if(interface_name != null) iface = interface_name.toString();
        return network.toString().substring(1) + "   " +
               next_hop     + "   " +
               mask.toString().substring(1)        + "   " +
               iface;
    } // -- toString

} // -- public class VNSRoutingEntry
