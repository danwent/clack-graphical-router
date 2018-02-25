

package net.clackrouter.routing;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.Interface;
import net.clackrouter.component.simplerouter.IPRouteLookup;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.router.core.Router;

/**
 * Represents an simple routing table that can perform lookups
 * based on a destination IP address.  By default, entries are
 * ordered for longest-prefix match.  
 */

public class RoutingTable {
	
    private Vector mRtable = null;
    private IPRouteLookup mLookupComp;
    private ClackComponentEvent changed; // used to fire listener of associated lookup-comp

    public RoutingTable(IPRouteLookup l)
    {
        mRtable = new Vector();
        mLookupComp = l;
        changed = new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE);
    }
    
    public RoutingEntry getRouteForPrefix(InetAddress net, InetAddress mask){
    	int masked_net1 = NetUtils.getIntFromAddr(net) & NetUtils.getIntFromAddr(mask);
    	int mask1 = NetUtils.getIntFromAddr(mask);
        for ( int i = 0 ; i < mRtable.size() ; ++i)
        {
            RoutingEntry entry = (RoutingEntry)mRtable.get(i);
            int masked_net2 = NetUtils.getIntFromAddr(entry.network) & NetUtils.getIntFromAddr(entry.mask);
            int mask2 = NetUtils.getIntFromAddr(entry.mask);
            if(masked_net1 == masked_net2 && mask1 == mask2) return entry;
        }  	
        return null; // no entry found
    }
    
    // dw: this is a little dirty, b/c someone *could* change the routing table 
    // behind our back. Don't do that!
    public RoutingEntry getEntry(int i){
    	if(i < mRtable.size()){
    		return (RoutingEntry) mRtable.get(i);
    	}
    	System.err.println("Routing table changed behind our back! (want= " + i
    				+ " have=" + mRtable.size() + ")");
    	return null;
    }
    
    public int numEntries() { return mRtable.size(); }

    public void clear(){
        mRtable.clear();
        mLookupComp.fireListeners(changed);
    }

    /**
     * 
     * You probably want to use either addRemoteEntry() or addLocalEntry() instead
     * of this function.  
     * 
     * Note: this function re-sorts the routing table to perform longest prefix match,
     * invalidating any previous calls to moveEntryUp or moveEntryDown.
     * 
     */
    public void addEntry(RoutingEntry entry) 
    {
        mRtable.add(entry);
        Collections.sort(mRtable); 
        mLookupComp.fireListeners(changed);
		mLookupComp.getRouter().getDocument().redrawTopoView();
    }
    
    public void addLocalEntry(RoutingEntry new_entry) throws Exception {
    		Router r = mLookupComp.getRouter();
			if(new_entry.interface_name != null && r.getInterfaceByName(new_entry.interface_name) == null){
				throw new Exception("Error:  no device named '" + new_entry.interface_name + "'");
			}
			new_entry.nextHop = null; // just for sure
			addEntry(new_entry);

    }
    
    /**
     * Must first lookup to make sure we have a route to the next-hop address already, if not, throw an error.
     * Otherwise, add new routing entry, using the interface specified for the next hop.  
     * @param net
     * @param mask
     * @param next_hop
     * @throws Exception
     */
    public void addRemoteEntry(RoutingEntry new_entry) throws Exception {
			
			RoutingEntry nh_entry = longestPrefixMatch(new_entry.nextHop);
			if(nh_entry == null)
				throw new Exception("Error: no route to reach next-hop = " + new_entry.nextHop.getHostAddress() + "\n");
			
			new_entry.interface_name = nh_entry.interface_name;
			addEntry(new_entry);
    }
    
    
    public void moveEntryUp(RoutingEntry entry){
    	int index = mRtable.indexOf(entry);
    	if(index > 0 && index < mRtable.size()){
        	mRtable.remove(index);
        	mRtable.add(index - 1, entry);
    	}
        mLookupComp.fireListeners(changed);
		mLookupComp.getRouter().getDocument().redrawTopoView();
    }
    
    public void moveEntryDown(RoutingEntry entry){
    	int index = mRtable.indexOf(entry);
    	if(index >= 0 && index < mRtable.size() - 1){
        	mRtable.remove(index);
    		mRtable.add(index + 1, entry);  	
    	}
        mLookupComp.fireListeners(changed);
		mLookupComp.getRouter().getDocument().redrawTopoView();
    }

    public void deleteEntry(RoutingEntry entry)
    {
        for ( int i = 0 ; i < mRtable.size() ; ++i)
        {
            if ( (RoutingEntry)mRtable.get(i) == entry )
            {
                mRtable.remove(i);
                mLookupComp.fireListeners(changed);
        		mLookupComp.getRouter().getDocument().redrawTopoView();
                return;
            }
        }
       
    }

    public RoutingEntry longestPrefixMatch(InetAddress in)
    {

        for ( int i = 0 ; i < mRtable.size() ; ++i)
        {
        	RoutingEntry entry = (RoutingEntry)mRtable.get(i);
            if (entry.matches(in) ){

            	return ((RoutingEntry)mRtable.get(i)); 
            }
        }

        return null;
    } 
    
    public IPRouteLookup getLookupComponent() { return mLookupComp; }

}  // -- VNSRoutingTable
