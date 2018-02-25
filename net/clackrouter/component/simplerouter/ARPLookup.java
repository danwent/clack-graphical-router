
package net.clackrouter.component.simplerouter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.component.extension.TCPMonitor.TimerCallback;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.*;
import net.clackrouter.propertyview.ARPLookupPView;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.ARPLookupPopup;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.Alerter.Alertable;


/**
 * Contains ARP cache, sends ARP requests, handles ARP replies, and supplies Ethernet Destination
 * addresses based on next hop addresses for packets.
 */
public class ARPLookup extends ClackComponent {
	
	
	
	private class ARPQueueEntry {
		public ARPQueueEntry() { queue = new ArrayList(); }
		long first_entry_sent;
		ArrayList queue;
	}

    public static int PORT_REPLY_IN = 0;
    public static int PORT_IP_IN = 1;
    public static int PORT_OUT = 2;
    public static int NUM_PORTS = 3;

    // -- Queue of packets pending ARP requests 
    private Hashtable mARPQueue = null;
    private Hashtable mARPCache = null;
    private long next_arp_cache_timeout;
    
    public static int DEFAULT_CACHE_TIMEOUT = 50000; // msecs
    public static int DEST_UNREACH_TIMER = 5000; // msecs 
    public static int MAX_QUEUE_PACKETS = 15; 
    
    private Timer mARPTimer;
    
    public ARPLookup(Router router, String name) 
    {
        super(router, name);

        mARPQueue         = new Hashtable();
        mARPCache         = new Hashtable();
        
        setupPorts(NUM_PORTS);
        
        InterfaceIn[] interfaces = router.getInputInterfaces();
        for (int i = 0 ; i < interfaces.length; ++i )
        { mARPCache.put(interfaces[i].getDeviceName(), new Hashtable()); }
        
        next_arp_cache_timeout = getTime() + DEFAULT_CACHE_TIMEOUT;
		mARPTimer = new Timer(DEST_UNREACH_TIMER, new TimerCallback());
		mARPTimer.start();
    }

    protected void setupPorts(int num_ports)
    {
    	super.setupPorts(num_ports);
        String reply_in_desc = "Incoming ARP Reply packets";
        String ip_in_desc  = "Outgoing IP packets needing dest MAC addresses";
        String out_desc    = "Outgoing packets needing Ethernet encapsulation";
        m_ports[PORT_REPLY_IN] = new ClackPort(this, PORT_REPLY_IN, reply_in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSARPPacket.class);
        m_ports[PORT_IP_IN] =  new ClackPort(this, PORT_IP_IN,  ip_in_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);

    } // -- setupPorts
    


    /**
     * Handles both incoming ARP Replies and Outgoing IP packets needing an destination
     * Ethernet address
     */

    public void acceptPacket(VNSPacket packet, int port_number)
    {

        if ( port_number == PORT_REPLY_IN )
        { handleARP((VNSARPPacket)packet); }
        else if ( port_number == PORT_IP_IN )
        { handleIP((IPPacket)packet); }
        else
        { error( "Received packet on invalid port: " + port_number); }

    } // -- acceptPacket	

    /**
     * <p>Lookup IP address in ARP cache, tag packet with outgoing MAC address if found, otherwise
     * send out an ARP request and enqueue the packet awaiting a reply. </p>
     */
    private void handleIP(IPPacket packet)
    {
 
        Interface outgoing =
            mRouter.getInterfaceByName(packet.getOutputInterfaceName());

        InetAddress nexthop = null; 

        try{
            nexthop = InetAddress.getByAddress(packet.getNextHopIPAddress().array());
        }catch( UnknownHostException e)
        {
            error("Could not read Next-Hop address: " +  new String(packet.getNextHopIPAddress().array()));
            return;
        }

        // -- check the ARP cache for the next hop
        EthernetAddress ethaddr = 
            retrieveARPCacheEntry(nexthop, packet.getOutputInterfaceName() );

        
        if (ethaddr != null)
        { // -- have valid arp cache entry
        	packet.setNextHopMacAddress(ByteBuffer.wrap(ethaddr.getBytes()));
            ByteBuffer ip_type = ByteBuffer.allocate(VNSEthernetPacket.TYPE_LEN);
            ip_type.putShort(VNSEthernetPacket.TYPE_IP);
            packet.setLevel2Type(ip_type);

             m_ports[PORT_OUT].pushOut(packet);
            return;
        } 

      
        // no valid entry, must send ARP request!
        sendARPRequest(outgoing, packet.getNextHopIPAddress());

        // enqueue packet to send it later
        ARPQueueEntry queue_entry = (ARPQueueEntry)mARPQueue.get(nexthop.toString());
        if(queue_entry == null){
        	queue_entry = new ARPQueueEntry();
        	queue_entry.first_entry_sent = getTime();
        	mARPQueue.put(nexthop.toString(),queue_entry);
        }

        if(queue_entry.queue.size() <= MAX_QUEUE_PACKETS)
        	queue_entry.queue.add(packet);
        else {
        	signalError();
        	error("Dropping packet because we have reached max queue size for next-hop = " + nexthop.getHostAddress());
        }

    } // -- handleIP

    /**
     * Assert that a packet is a valid REPLY, the parse the reply, add it to the 
     * ARP cache, and send any packets that were enqueued and waiting this reply.  
     */
    private void handleARP(VNSARPPacket packet)
    {

        if(packet.getOperation() != VNSARPPacket.OPERATION_REPLY) {
        	System.err.println("ARPLookup received non-Reply ARP Packet");
        	return;
        }
        InetAddress src_ip_addr = null;
        EthernetAddress ethaddr = null;
        try{
            src_ip_addr = 
                InetAddress.getByAddress(packet.getSourceProtocolAddress().array());    	
            ethaddr =  
            	EthernetAddress.getByAddress(packet.getSourceHWAddress().array());
        }catch(UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        updateARPCache(src_ip_addr, ethaddr, packet.getInputInterfaceName());
        log("Added "+src_ip_addr+ ":"+ethaddr+" to arp cache");

        // 
        ARPQueueEntry queue_entry = (ARPQueueEntry)mARPQueue.get(src_ip_addr.toString());

        if (queue_entry == null){
            log(" Received ARP reply from " + src_ip_addr.getHostAddress() + " but have no packets queued for it");   
            return;
        }

        // send all packets that were queued waiting for this ARP reply 
        while(queue_entry.queue.size() > 0)
            handleIP((IPPacket)queue_entry.queue.remove(0));      

        mARPQueue.remove(src_ip_addr.toString());
        fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE)); // item added to ARP cache      
    } // -- handleARP

    /** 
     * Creates an ARP Request packet and sends it
     * @param iface the outgoing interface for the ARP Request
     * @param next_hop the IP address we need
     */
    private void sendARPRequest(Interface iface, ByteBuffer next_hop) 
    {
    	try {


        VNSARPPacket request_packet = new VNSARPPacket(VNSARPPacket.HW_TYPE_ETHERNET,
                VNSARPPacket.PROTOCOL_TYPE_IP,
                (byte)VNSARPPacket.HW_ADDR_LEN,
                (byte)VNSARPPacket.PROTOCOL_ADDR_LEN,
                VNSARPPacket.OPERATION_REQUEST,
                ByteBuffer.wrap(iface.getMACAddress().getAddress()),
                ByteBuffer.wrap(iface.getIPAddress().getAddress()),
                ByteBuffer.wrap(EthernetAddress.getByAddress("0:0:0:0:0:0").getAddress()),
                next_hop);

        ByteBuffer bcast = ByteBuffer.wrap(EthernetAddress.getByAddress("ff:ff:ff:ff:ff:ff").getAddress());
        request_packet.setNextHopMacAddress(bcast);
        
        ByteBuffer arp_type = ByteBuffer.allocate(VNSEthernetPacket.TYPE_LEN);
        arp_type.putShort(VNSEthernetPacket.TYPE_ARP);
        request_packet.setLevel2Type(arp_type);
        
        request_packet.setOutputInterfaceName(iface.getDeviceName());

        m_ports[PORT_OUT].pushOut(request_packet);
    	} catch (Exception e){
    		e.printStackTrace();
    	}

    } // -- sendARPRequest
    
    public JPanel getPropertiesView(){	return new ARPLookupPView(this); /* new ARPLookupPopup(this); */ }
    public Hashtable getARPCache(){    return mARPCache; }
    
    /** update the ARP cache with a IP/MAC address pair */
    private void updateARPCache(InetAddress ip, EthernetAddress hw, String   iface)
    {
        Hashtable  arpcache = (Hashtable)mARPCache.get(iface);
        if (arpcache == null)    {
            error("Updating ARP cache that doesn't exist on interface " +  iface);
            return;
        }

        arpcache.put(ip, new CacheEntry(ip,hw, DEFAULT_CACHE_TIMEOUT));
        fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE));
    } // -- updateARPCache
    
    
    /** Find the Ethernet Address assocated with an IP on a particular interface */
    private EthernetAddress retrieveARPCacheEntry(InetAddress ip, String  iface)
    {
        Hashtable  arpcache = (Hashtable)mARPCache.get(iface);
        if (arpcache == null) {
            error("No ARP cache exists for interface " +  iface);
            return null;
        }

        CacheEntry entry = (CacheEntry)arpcache.get(ip);
        if ( entry == null ) { return null; }
     
        return entry.mEthAddr;
    } // -- retrieveARPCacheEntry
    
    /**
     * Removes an entry in the specified ARP cache
     * @param interface_name interface of the entry to remove
     * @param ip IP address of entry to remove
     * @return true if an actual entry was removed, false otherwise.
     */
    public boolean removeARPCacheEntry(String interface_name, InetAddress ip){
    	//System.out.println("Trying to remove: " + ip.toString() + " on " + interface_name);
    	Hashtable cache = (Hashtable)mARPCache.get(interface_name);
    	if(cache == null) return false;
    	CacheEntry entry = (CacheEntry)cache.remove(ip);
    	return entry != null;
    }
    
    /** Adds an entry to the ARP cache */
    public void addARPCacheEntry(String interface_name, InetAddress ip, EthernetAddress hw){
        Hashtable  arpcache = (Hashtable)mARPCache.get(interface_name);

        if (arpcache == null) {
        	error("No ARP cache exists for interface " +  interface_name);
            return;
        }
    	arpcache.put(ip, new CacheEntry(ip,hw, DEFAULT_CACHE_TIMEOUT));
    	fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE));
    }
    
    
    public Color getColor() { return new Color(255, 254, 167); } // ARP group color
    
    /** callback for the thread to time-out entries in the ARP cache */
	private class TimerCallback implements ActionListener {
	      public void actionPerformed(ActionEvent evt) {
	    	  long cur_time_msec = getTime();
	    	  long queue_timeout_msec = DEST_UNREACH_TIMER ;
	    	  
	    	  for(Enumeration e = mARPQueue.keys(); e.hasMoreElements(); ) {
	    		  String next_hop = (String)e.nextElement();
	    		  ARPQueueEntry entry = (ARPQueueEntry) mARPQueue.get(next_hop);
	    		  if(cur_time_msec - entry.first_entry_sent > queue_timeout_msec){
    				  error("Did not get ARP response for next-hop: " + next_hop);
    				  signalError();
	    			  while(entry.queue.size() > 0){
	    				  try {
	    					  IPPacket p = (IPPacket)entry.queue.remove(0);
	    					  InetAddress addr = InetAddress.getByAddress(p.getHeader().getSourceAddress().array());
	    					  error("Should send dest unreachable to " + addr.getHostAddress());
	    				  } catch (Exception ex) { ex.printStackTrace(); }
	    			  }
		    		  mARPQueue.remove(next_hop);
	    		  }
	    	  }
	    	  
	    	  if(next_arp_cache_timeout < cur_time_msec) {
	    		  next_arp_cache_timeout = cur_time_msec + DEFAULT_CACHE_TIMEOUT;
	    		  for(Enumeration valEnum = mARPCache.elements(); valEnum.hasMoreElements(); ){
	    			  Hashtable perIfaceCache = (Hashtable)valEnum.nextElement();
	    			  for(Enumeration cacheEnum = perIfaceCache.elements(); cacheEnum.hasMoreElements(); ){
	    				  CacheEntry entry = (CacheEntry) cacheEnum.nextElement();
	    				  if(entry.timedOut()){
	    					  signalError();
	    					  error("Timed out ARP cache entry for: " + entry.mIPAddr.getHostAddress()); 
	    					  perIfaceCache.remove(entry.mIPAddr);
	    				  }
	    			  }
	    		  }
	      	  }
	      }
	}



    
    /** Data structure representing a single ARP Cache entry */
    public class CacheEntry {
		public InetAddress mIPAddr;
		public EthernetAddress mEthAddr;
		public int mStayTime;
		public long mLastUpdate;

		public CacheEntry(InetAddress ipa, EthernetAddress ea, int stay) {
			mIPAddr = ipa;
			mEthAddr = ea;
			mStayTime = stay;
			mLastUpdate = getTime();
		}

		public String toString() {
			return mIPAddr.toString().substring(1) + "  " + mEthAddr.toString();
		}

		/** tests if this entry is times out */
		public boolean timedOut() {
			if (mStayTime >= 0
					&& (int) ((getTime() - mLastUpdate) / 1000.0) >= mStayTime) {
				return true;
			}
			return false;
		}

		/** set last updated time to current time */
		public void update() {	mLastUpdate = getTime();}
	} // -- private class CacheEntry
}
