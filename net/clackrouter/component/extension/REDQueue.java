
package net.clackrouter.component.extension;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.ComponentDataHandler;
import net.clackrouter.component.base.ComponentVizLauncher;
import net.clackrouter.component.base.Queue;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.DefaultPropertiesView;
import net.clackrouter.propertyview.FlowByteQueuePopup;
import net.clackrouter.propertyview.REDQueuePView;
import net.clackrouter.router.core.Router;


/**
 * Implementation of a Random Early Detection (RED) queue.
 * 
 * <p> TODO: Currently, this implementation is lacking a GUI to set and modify the parameters
 *  in real-time, which is really needed for it to be useful.  Its on the to-do list. </p>
 * 
 */
public class REDQueue extends Queue 
	implements ComponentDataHandler, ComponentVizLauncher {
	
	public static final int PORT_HEAD = 0, PORT_TAIL = 1, NUM_PORTS = 2;
	
	public static double DEFAULT_LEN = 20000;
	public static double DEFAULT_MIN_THRESHOLD = 0.4 * DEFAULT_LEN;
	public static double DEFAULT_MAX_THRESHOLD = 0.8 * DEFAULT_LEN;
	public static double DEFAULT_MAX_PROBABILITY = 0.7;
	public static double DEFAULT_ALPHA = 0.1;
	
	// lazy... save the trouble of writing a bunch of getters/setters
	public double minThresh, maxThresh, maxProb, avgLen, curLen, maxLen, alpha;
	private ArrayList packets;
	private Random rand;
	private long last_nonforced_avg_update;
	public static long MIN_AVG_UPDATE_INTERVAL = 500;
	
	VisibleREDQueue visible_queue;
	
	public REDQueue(Router router, String name){
		this(router, name, DEFAULT_LEN, DEFAULT_MIN_THRESHOLD, DEFAULT_MAX_THRESHOLD,
				DEFAULT_MAX_PROBABILITY, DEFAULT_ALPHA); 	
	}
	
	public REDQueue(Router router, String name, double mLen, double minTh, double maxTh, double mProb,  double a){
		super(router, name);
		minThresh = minTh;
		maxThresh = maxTh;
		maxProb = mProb;
		maxLen = mLen;
		alpha = a;
		avgLen = curLen = 0;
		packets = new ArrayList();
		rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		setupPorts(NUM_PORTS);
		last_nonforced_avg_update = System.currentTimeMillis();
		mRouter.registerForPoll(this);
	}
	
	public void setupPorts(int numPorts){
		super.setupPorts(numPorts);
        m_ports[PORT_HEAD] = new ClackPort(this, PORT_HEAD, "head of RED Queue", ClackPort.METHOD_PULL, ClackPort.DIR_OUT, VNSPacket.class);
        m_ports[PORT_TAIL]     = new ClackPort(this, PORT_TAIL, "tail of RED Queue", ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
	}
	
	/** Receives a packet and sees if RED will drop or enqueue */
	public void acceptPacket(VNSPacket packet, int port_number){
		int size = packet.getByteBuffer().capacity();
		
		if(curLen + size > maxLen) {
			log("Must drop");
			m_recent_drop = true;
			return; // must drop because queue is not large enough
		}
		
		if(avgLen > maxThresh) {
			log("threshold drop");
			m_recent_drop = true;
			return; // RED drop, beyond max threshold
		}
		
		if(avgLen > minThresh && probDropTest()) {
			log("Early Drop");
			m_recent_drop = true;
			return; // Early RED drop
		}
				
		packets.add(packet);
		curLen += size;
		recalculateAvgLen();
		
	}
	
	/** Removes a packet from the RED queue and recalculates the avg len */
	public VNSPacket handlePullRequest(int port_number) {
		if(packets.size() > 0){
			VNSPacket packet = (VNSPacket)packets.remove(0);
			curLen -= packet.getByteBuffer().capacity();
			recalculateAvgLen();
			
			return packet;
		}
		return null;
	}
	
	public void poll() {
		long cur_time = System.currentTimeMillis();
		if(cur_time - last_nonforced_avg_update > MIN_AVG_UPDATE_INTERVAL){
			last_nonforced_avg_update = cur_time;
			recalculateAvgLen();
		}
	}
	
	public int getOccupancy() { return (new Double(curLen)).intValue(); }
	public int getMaxOccupancy() { return (new Double(maxLen)).intValue(); }
	
	private void recalculateAvgLen() {

		avgLen = (1 - alpha) * avgLen + alpha * curLen;
		log("avg len = " + avgLen + " curLen = " + curLen);
    	if(view != null) 
    		view.clackPaint();
    	
    	if(visible_queue != null)
    		visible_queue.repaint();
	}
	
	/** Probabilistically test if this packet should be dropped, based
	 * on the RED algorithm. 
	 */
	private boolean probDropTest() {
		double slope = (maxProb) / (maxThresh - minThresh);
		double prob = (avgLen - minThresh) * slope;
		return (prob > rand.nextDouble());
	}
	
    public boolean isModifying() { return false; }
    
	public Properties getReadHandlerValues(){
		return getWriteHandlerValues(); // just for test
	}
	
	public Properties getWriteHandlerValues(){
		Properties props = new Properties();	
		props.setProperty("Min Threshold", "" + minThresh);
		props.setProperty("Max Threshold", "" + maxThresh);
		props.setProperty("Max Probability", "" + maxProb);
		props.setProperty("Capacity", "" + maxLen);
		props.setProperty("Alpha", alpha + "");
		return props;
	}
	
	public boolean acceptWrite(String key, String value){
		try {
			double d = Double.parseDouble(value);
			if(key.equals("Min Threshold")) minThresh = d;
			if(key.equals("Max Threshold")) maxThresh = d;
			if(key.equals("Max Probability")) maxProb = d;
			if(key.equals("Capacity"))  maxLen = d;
			if(key.equals("Alpha"))  alpha = d;	
			// tell property listener that a value has changed
			fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE, 0));
		}catch (Exception e){
			showErrorDialog("'" + value + "' is an acceptable value for label '" 
							+ key + "'");
			return false;
		}
		return true;
	}
	
	public JPanel getPropertiesView() {
		return new DefaultPropertiesView(m_name, this, this.getTypeName());
	}
	
	public String[] getLauncherStrings() { 
		return new String[] { "View Dynamic Queue" };
	}
	
	public void launch(String s) {
		JFrame frame = new JFrame(m_name);
		if(visible_queue == null) 
			visible_queue = new VisibleREDQueue(200,100,this);
		frame.getContentPane().add(visible_queue);
		frame.setSize(visible_queue.m_width + 40 , visible_queue.m_height + 70);
		frame.setVisible(true);
	}
	
	private class VisibleREDQueue extends JComponent {
		public int m_width, m_height;
		REDQueue m_model;
		int orig_left = 10;
		int orig_top = 10;

		public VisibleREDQueue(int width, int height, REDQueue model){
			m_width = width; m_height = height;
			m_model = model;
			this.setPreferredSize(new Dimension(m_width, m_height));
			this.setVisible(true);
		}
		
		public void paintComponent(Graphics g){
			this.setBackground(Color.WHITE);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(orig_left, orig_top, m_width, m_height);

			int occupancy = m_model.getOccupancy();
			int capacity = m_model.getMaxOccupancy();
			
			int occupancy_bar_length = (new Double(((float)m_width)*((float)occupancy / (float)capacity))).intValue();
			int min_thresh_length = (new Double(((float)m_width)*(m_model.minThresh / (float)capacity))).intValue();
			int max_thresh_length = (new Double(((float)m_width)*(m_model.maxThresh / (float)capacity))).intValue();
			int mov_avg_length = (new Double(((float)m_width)*(m_model.avgLen/ (float)capacity))).intValue();
			if(m_model.recentDropTest())
				g.setColor(Color.RED);
			else
				g.setColor(Color.GREEN);
			if(occupancy > 0)
				g.fillRect(orig_left, orig_top, occupancy_bar_length, m_height);
			g.setColor(Color.BLUE);
			g.drawLine(orig_left + min_thresh_length, orig_top, orig_left + min_thresh_length, m_height + orig_top);
			g.drawLine(orig_left + max_thresh_length, orig_top, orig_left + max_thresh_length, m_height + orig_top);
			g.setColor(Color.MAGENTA);

			g.drawLine(orig_left + mov_avg_length, orig_top, orig_left +  mov_avg_length, m_height + orig_top);
			
			int prob_height = (new Double((1 - m_model.maxProb) * m_height)).intValue();
			g.setColor(Color.ORANGE);
			g.drawLine(orig_left + min_thresh_length, orig_top + m_height, orig_left + max_thresh_length, orig_top + prob_height);
			g.setColor(Color.BLACK);
			
			String msg;
			if(avgLen > maxThresh) {
				msg = "Must drop, above threshold";
			}else if(avgLen > minThresh) {
				double slope = (maxProb) / (maxThresh - minThresh);
				double prob = (avgLen - minThresh) * slope;
				msg = "Drop Probability = " + prob;
			}else {
				msg = "No drops, below threshold";
			}
			g.drawString(msg, orig_left, orig_top + m_height + 20);
		}
		
	}
	

}
