package net.clackrouter.component.extension;

import java.awt.Color;
import java.util.*;

import javax.swing.JPanel;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.DelayPView;
import net.clackrouter.router.core.Router;
import net.clackrouter.component.base.*;

public class Delay extends ClackComponent {
	public static int PORT_IN = 0,PORT_OUT = 1, NUM_PORTS = 2;
	
	public static int DEFAULT_DELAY = 0;
	private int mDelay;
	private Vector mPacketlist;
	
	private class DelayPair {
		public DelayPair(VNSPacket p, long t) { packet = p; time = t; }
		long time;
		VNSPacket packet;
	}
	
	public Delay(Router r, String name){
		this(r,name, DEFAULT_DELAY);
	}
	
	public Delay(Router r, String name, int delay)
    {
		super(r, name);
		mDelay = delay;
		setupPorts(NUM_PORTS);
		mPacketlist = new Vector();
		r.registerForPoll(this);
    }
	
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String in_desc = "Packets entering delay element";
        String out_desc     = "Packets leaving delay element";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PULL, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT]     = new ClackPort(this, PORT_OUT, out_desc, ClackPort.METHOD_PULL, ClackPort.DIR_OUT, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; }
    
    public int getDelay() { return mDelay; }
    public void setDelay(int d) { mDelay = d; }
    
    /**
 
     */
	   public VNSPacket handlePullRequest(int port_number) { 

	        if (port_number == PORT_OUT)
	        { 		
	        	
	        	if(mDelay == 0) return m_ports[PORT_IN].pullIn();
	        	
	        	if(mPacketlist.size() == 0) return null;
	        	long cur_time = System.currentTimeMillis();

	        	DelayPair pair = (DelayPair)mPacketlist.get(0);
	        	if(pair.time < cur_time){
	        		mPacketlist.remove(0);
	        		return pair.packet;
	        	}
	        }
	        else
	        { error( "Received packet on invalid port: " + port_number); }	
	        
	        return null;
	    }
	   
	   public void poll() {
		   if(mDelay > 0) {
			   VNSPacket p = m_ports[PORT_IN].pullIn();
			   if(p != null){
				   mPacketlist.add(new DelayPair(p, System.currentTimeMillis() + mDelay));	  
			   }
		   }
	   }
	   
	   public JPanel getPropertiesView(){	return new DelayPView(this); }  
	    
	    /** Serialize the delay value for reloading the router */
	    public Properties getSerializableProperties(boolean isTransient){
	    	Properties props = super.getSerializableProperties(isTransient);
	    	props.setProperty("delay", "" + mDelay);
	    	return props;
	    }
	    
	    /** Load the saved delay value */
		public void initializeProperties(Properties props) {
			for(Enumeration e = props.keys(); e.hasMoreElements(); ){
				String key = (String)e.nextElement();
				String value = props.getProperty(key);
				if(key.equals("delay")){
					try{ 
						mDelay = Integer.parseInt(value);
					}catch (Exception ex){
						ex.printStackTrace();
					}
				}
			}
		}
	    public Color getColor() { return Color.PINK; }
}
