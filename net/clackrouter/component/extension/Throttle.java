
package net.clackrouter.component.extension;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.ThrottlePView;
import net.clackrouter.propertyview.TCPMonitorPopup;
import net.clackrouter.router.core.Router;


/**
 * Component to slow down the packet being passed through it in a user configurable manner.
 * 
 * Delay is specified as a percentage of full router speed.  Thus, 100 is full speed.  
 */
public class Throttle extends ClackComponent {
	
	
	public static int PORT_IN = 0,PORT_OUT = 1, NUM_PORTS = 2;
	
	public static int DEFAULT_DELAY = 100;
	private int mDelayCounter;
	private int mDelay;
	public Throttle(Router r, String name){
		this(r,name, DEFAULT_DELAY);
	}
	
	public Throttle(Router r, String name, int delay)
    {
		super(r, name);
		mDelay = delay;
		setupPorts(NUM_PORTS);
		mDelayCounter = 0;
    }
	
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String in_desc = "Packets entering throttle element";
        String out_desc     = "Packets leaving throttle element";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PULL, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT]     = new ClackPort(this, PORT_OUT, out_desc, ClackPort.METHOD_PULL, ClackPort.DIR_OUT, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; }
    
    public int getDelay() { return mDelay; }
    public void setDelay(int d) { mDelay = d; }
    
    /**
     * Does not buffer components on its own, but rather performs a simple
     * calculation to decide when to pass on the pull request on its output
     * port on to the component connected to its input port.  
     */
	   public VNSPacket handlePullRequest(int port_number) { 

	        if (port_number == PORT_OUT)
	        { 		
	        		mDelayCounter += mDelay;
	        		if(mDelayCounter < 100) return null;
	        		
	        		mDelayCounter -= 100;
	        		return m_ports[PORT_IN].pullIn();
	        }
	        else
	        { error( "Received packet on invalid port: " + port_number); }	
	        
	        return null;
	    }
	   
	    public JPanel getPropertiesView(){	return new ThrottlePView(this); }  
	    
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
