/*
 * Created on Apr 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.component.base;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JPanel;

import net.clackrouter.chart.ClackOccData;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.QueuePopup;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.graph.QueueView;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.VertexView;

/**
 * Base class for any Clack queue.
 * 
 * <p> This class measures queue occupancy in packets and can contain any type 
 * of packet.  This class provides basic 
 * functionality inherited by more advanced queues </p>  
 */
public class Queue extends ClackComponent
{

	public static int PORT_HEAD = 0;
	public static int PORT_TAIL = 1;
	public static int NUM_PORTS = 2;
	
	public static int DEFAULT_SIZE = 10;
	
	protected int m_max_size;
	protected int m_total_drops;
	protected boolean m_recent_drop;

	protected Date startTime = null;

	protected transient ArrayList m_queue = new ArrayList();
	protected transient ClackOccData mQueueOccData = new ClackOccData();
	
	public Queue(Router router, String name){
		this(router, name, DEFAULT_SIZE);
	}
	
	public Queue(Router router, String name, int start_size)
    {
        super(router, name);

        startTime = new Date(); 
		m_max_size = start_size;
		m_total_drops = 0;
		setupPorts(NUM_PORTS);
		m_recent_drop = false;
    }
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String head_desc = "Port for the head of the queue";
        String tail_desc     = "Port for the tail of the queue";
        m_ports[PORT_HEAD] = new ClackPort(this, PORT_HEAD, head_desc, ClackPort.METHOD_PULL, ClackPort.DIR_OUT, VNSPacket.class);
        m_ports[PORT_TAIL]     = new ClackPort(this, PORT_TAIL, tail_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; }

    /**
     * Enqueues a packet at the tail of the queue, if possible, 
     * or drops it if it is already at max occupancy.
     */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {
        if (port_number == PORT_TAIL)
        { 
        	if(m_queue.size() < m_max_size){
        		m_queue.add(packet); 
        		queueOccupancyChanged();
        	}else {
        		m_recent_drop = true;
        		m_total_drops++;
        	}
	
        }
        else
        { error(" acceptPacket not TAIL_PORT"); }	

    } // -- acceptPacket

    /**
     * Removes a packet from the head of the queue, if possible, and returns it.
     */
    public VNSPacket handlePullRequest(int port_number) { 

        if (port_number == PORT_HEAD)
        { 	
            if(!m_queue.isEmpty()){
                VNSPacket packet = (VNSPacket) m_queue.remove(0);
                queueOccupancyChanged();
                return packet;
            }
        }
        else
        { error(" pull not HEAD_PORT"); }	
        
        return null;
    }
    
    /**
     * Helper method to update graph and GUI queue when occupancy has been 
     * changed.
     */
    protected void queueOccupancyChanged() {
    	if(view != null) 
    		view.clackPaint();

    	
        mQueueOccData.addXYValue("all flows", new Long(new Date().getTime()
                - startTime.getTime()), new
            Integer(getOccupancy()));  	
    }
   

    /** The current occupancy of the queue */
    public int getOccupancy(){ return m_queue.size(); }
    /** The max occupancy of the queue */
    public int getMaxOccupancy(){  return m_max_size; }  
    /** Set the max occupancy of this queue */
    public void setMaxOccupancy(int newMax){ m_max_size = newMax; }
    
    /** Total number of packets dropped by this queue */
    public int getTotalDropped(){  return m_total_drops; }
    
    /** Test whether we have just dropped a packet (used by QueueView). */
    public boolean recentDropTest() {
    	boolean tmp = m_recent_drop;
    	m_recent_drop = false;
    	return tmp;
    }

    /** 
     * Provides data of queue occupancy vs time for real-time graphs.
     */
    public ClackOccData getQueueOccData(){ return mQueueOccData; }

	public JPanel getPropertiesView() {	return new QueuePopup(this);}
	
	/** Override default ClackComponent method that returns a ClackComponentView
	 * since queues are rendered differently within a RouterView.
	 */
	public VertexView getView(JGraph graph, CellMapper mapper) { 
		return new QueueView(getComponentCell(), graph, mapper);
	}
	
    /** Serialize the queue's size for reloading the router */
    public Properties getSerializableProperties(boolean isTransient){
    	Properties props = super.getSerializableProperties(isTransient);
    	props.setProperty("max_size", "" + m_max_size);
    	return props;
    }
    
    /** Load the saved size value */
	public void initializeProperties(Properties props) {
		for(Enumeration e = props.keys(); e.hasMoreElements(); ){
			String key = (String)e.nextElement();
			String value = props.getProperty(key);
			if(key.equals("max_size")){
				try{ 
					m_max_size = Integer.parseInt(value);
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}
	

}
