package net.clackrouter.component.extension;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JPanel;

import net.clackrouter.chart.ChartUtils;
import net.clackrouter.chart.ClackOccData;
import net.clackrouter.component.base.Queue;
import net.clackrouter.netutils.FilterEntry;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.FlowByteQueuePopup;
import net.clackrouter.router.core.Router;
import net.clackrouter.topology.core.TopologyModel;


public class FlowByteQueue extends Queue {
	
	public static int DEFAULT_MAX_SIZE = 10000;
	private int cur_occupancy_bytes;

	protected Date startTime = null;

	protected ArrayList m_queue = new ArrayList();
	protected ClackOccData m_occ_flowdata = new ClackOccData(); 
	protected Hashtable m_flowsize_map = new Hashtable();
    private Hashtable m_lastseen_hash = new Hashtable();
	
	private long last_avg = 0, last_draw = 0;
	public static int AVG_INTERVAL_MSEC = 20;
	public static int UPDATE_INTERVAL_MSEC = 200;
	
	
    // ivars used to get rid of any series that hasn't 
    // been updated in a while
    private long last_stale_check = 0;
    public static int STALE_CHECK_INTERVAL = 8000; // 8 seconds
    public static int DEFAULT_MAX_IDLE_TIME = 30000; // 30 seconds
    
    public static int VISIBLE_TIME_INTERVAL = 80000; // 80 seconds 
    private long m_last_shift_check = 0;
    
    private boolean has_filters = false;
    private ArrayList filters = new ArrayList();
	
	public FlowByteQueue(Router router, String name){
		this(router, name, DEFAULT_MAX_SIZE);
		cur_occupancy_bytes = 0;
	}
	
	public FlowByteQueue(Router router, String name, int max_bytes)
    {
        super(router, name, max_bytes);

        startTime = new Date(); 
		m_total_drops = 0;
		setupPorts(NUM_PORTS);
		m_recent_drop = false;
		m_last_shift_check = startTime.getTime();
    }
	
	/** Add packet to the queue if the additional bytes can be stored without
	 * exceeding max size, otherwise drop. */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {
    	
        if (port_number == PORT_TAIL)
        { 
        	if(cur_occupancy_bytes + packet.getLength() <= m_max_size){
        		m_queue.add(packet); 
        		cur_occupancy_bytes += packet.getLength();
        		packet_added(packet, false);
        	}else {
        		m_total_drops++;
        		log("Packet Drop: " + new Date());
        		m_recent_drop = true;
        		packet_added(packet, true);
        	}
	
        }
        else
        { error( "Received packet on invalid port: " + port_number); }	

    } // -- acceptPacket
    
    /** Pull a packet from the head of the queue 
     * @return a single packet, or null if queue is empty 
     */
    public VNSPacket handlePullRequest(int port_number) { 
    	
    	// check to remove stale entries, shift graph or 
    	// write data to the plot
    	do_timer_tasks();
    	
    	if (port_number == PORT_HEAD)
    	{ 	

    		if(!m_queue.isEmpty()){
                	
    			VNSPacket packet = (VNSPacket) m_queue.remove(0);
    			cur_occupancy_bytes -= packet.getLength();
    			packet_removed(packet);                 
    			return packet;
    		}

        }
        else  { error("Pull request on invalid port: " + port_number); }	
            
    	return null;
    }
    
    /**
     * Do a bunch of stuff that we only do every INTERVAL seconds.  
     * Currently we do the following (1) Removes any stale series from the plot
     * (2) Check to see if we need to write data to the occ data object and
     * (3) See if we need to remove old entries in the data plot. 
     *
     */
    private void do_timer_tasks(){
    	long cur_time = System.currentTimeMillis();
    	
        // remove any stale series, but not if we are already using a filter
        if(cur_time - last_stale_check > STALE_CHECK_INTERVAL){
        	for(Enumeration e = m_flowsize_map.keys(); e.hasMoreElements(); ){
        		String k = (String)e.nextElement();
        		Long last = (Long)m_lastseen_hash.get(k);
        		if(last == null)continue;

        		boolean remove_me = false;
        		if(has_filters){
        			if(cur_time - last.longValue() > VISIBLE_TIME_INTERVAL) remove_me = true;
        		} else if(cur_time - last.longValue() > DEFAULT_MAX_IDLE_TIME) remove_me = true;
        		
        		if(remove_me){
        	
        			m_lastseen_hash.remove(k);
        			m_flowsize_map.remove(k);
        			m_occ_flowdata.removeSeries(k);
        		}
        	}
        }
        
        // actually send points to our graph to draw a new data point
    	if(cur_time - last_avg > AVG_INTERVAL_MSEC){
    		boolean draw = cur_time - last_draw > UPDATE_INTERVAL_MSEC;
    		
    		last_avg = cur_time;
        	long cur_timediff = cur_time - startTime.getTime();
        	Double time_diff = new Double((double)cur_timediff / 1000);
        	
    		for(Enumeration e = m_flowsize_map.keys(); e.hasMoreElements(); ){
    			String key = (String)e.nextElement();
    			Double inst_size = (Double)m_flowsize_map.get(key);
    			if(draw){
    				m_occ_flowdata.addXYValue(key, time_diff, inst_size);
    				last_draw = cur_time;
    			}
    		}
    	}
    	
    	if(cur_time - m_last_shift_check > VISIBLE_TIME_INTERVAL){
			double min_x_to_allow = ((double)(m_last_shift_check - startTime.getTime())) / 1000;
       		for(int i = 0; i < m_occ_flowdata.getSeriesCount();i++ ){
    			String key = m_occ_flowdata.getSeriesKey(i);
    			m_occ_flowdata.remove_all_with_x_lessthan(key, min_x_to_allow);
       		}
    		m_last_shift_check = cur_time;
    	}
  	
    }
    
    public int getOccupancy(){
        return cur_occupancy_bytes;
    }
    
    /**
     * updates graph and GUI queue when packet has been added to the queue
     */
    protected void packet_added(VNSPacket p, boolean is_drop) {
    	if(view != null)
    		view.clackPaint();
    	
    	String key = "";
    	if(has_filters){
    		boolean is_match = false;
    		FilterEntry.PacketInfo p_info = FilterEntry.getPacketInfo(p);
    		if(p_info != null){
    			for(int i = 0; i < filters.size(); i++){
    				FilterEntry entry = (FilterEntry)filters.get(i);
    				if(entry.matches(p_info)){
    					is_match = true;
    					break;
    				}
    			}
    		}
    		if(!is_match) return;

    		key = ChartUtils.getFlowKeyForFilterEntry(p_info);

    	}else {
    		key = ChartUtils.getFlowKeyForPacket(p);
    	}
    	

    	m_lastseen_hash.put(key, new Long(System.currentTimeMillis()));
    	if(is_drop) return;
    	
    	Double cur_size = (Double)m_flowsize_map.get(key);
    	if(cur_size == null){
    		cur_size = new Double(0);
    	}
    	Double new_size = new Double(cur_size.intValue() + p.getLength());
		m_flowsize_map.put(key, new_size);
    }
 
    /**
     * updates graph and GUI queue when packet has been removed from the queue
     */
    protected void packet_removed(VNSPacket p) {
    	if(view != null)
    		view.clackPaint();
    	
       	String key = ChartUtils.getFlowKeyForPacket(p);
    	
    	Double cur_size = (Double)m_flowsize_map.get(key);
    	if(cur_size == null){
    		return;
    	}
    	Double new_size = new Double(cur_size.intValue() - p.getLength());
		m_flowsize_map.put(key, new_size);
        m_lastseen_hash.put(key, new Long(System.currentTimeMillis()));
    }
    
 
    /** 
     * Provides data of queue occupancy vs time for real-time graphs.
     */
    public ClackOccData getMultiSeriesOccData(){ return m_occ_flowdata; }

	public JPanel getPropertiesView() {	return new FlowByteQueuePopup(this);}
	
    /** Serialize the queue's size for reloading the router */
    public Properties getSerializableProperties(boolean isTransient){
    	Properties props = super.getSerializableProperties(isTransient);		
    	for(int i = 0; i < filters.size(); i++){
    		FilterEntry entry = (FilterEntry)filters.get(i);
    		props.setProperty("filter" + i, entry.toString());
    	}
    	return props;
    }
    
    /** Load the saved size value */
	public void initializeProperties(Properties props) {
		super.initializeProperties(props);
		
		int i = 0;
		while(true){
			String filter_str = props.getProperty("filter" + i);
			if(filter_str == null)break;
			has_filters = true; 
			System.out.println("Loading FlowByteQueueFilter: " + filter_str);
			try {
				TopologyModel model = getRouter().getDocument().getFramework().getTopologyManager().getTopologyModel(getRouter().getTopology(), null);
				FilterEntry entry = FilterEntry.createFromTopoName(filter_str,model );
				filters.add(entry);
			}catch (Exception e){
				e.printStackTrace();
			}
			i++;
		}
	}	
	
	public void clearAllFlowInformation(){
		m_occ_flowdata.removeEverySeries();
		m_flowsize_map.clear();
	    m_lastseen_hash.clear();
	}


}
