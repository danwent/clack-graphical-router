
package net.clackrouter.component.extension;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.LossPopup;
import net.clackrouter.router.core.Router;


/**
 * Component that randomly drops packets based on a user-specified drop rate.
 */
public class Loss extends ClackComponent {
	
	private float mLoss;
	public static int PORT_IN = 0;
	public static int PORT_OUT = 1;
	public static int DEFAULT_LOSS = 0;
	private Random rand;
	private int m_drops = 0;
	private boolean mDropNextPacket;
	
	public Loss(Router r, String name){
		this(r,name, DEFAULT_LOSS);
	}
	
	public Loss(Router r, String name, int loss)
    {
		super(r, name);
		mDropNextPacket = false;
		mLoss = loss;
		setupPorts(2);
		rand = new Random();
    }
	
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String in_desc = "Packets entering loss element";
        String out_desc     = "Packets leaving loss element";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT]     = new ClackPort(this, PORT_OUT, out_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; } 
    
    public float getLoss() { return mLoss; }
    public void setLoss(float l) { mLoss = l; }
    
    /**
     * Randomly test to see if the packet should be dropped, otherwise forward it unmodified 
     */
	   public void acceptPacket(VNSPacket packet, int port_number) { 

	        if (port_number == PORT_IN)
	        { 	
                	
                	if(mDropNextPacket){
                		mDropNextPacket = false;
                		this.signalError();
                		m_drops++;
	        			return; // dropped packet
                	}
  	
	        		float test = rand.nextFloat() * 100;
	        		if(test < mLoss){
	        			m_drops++;
	        			return; // dropped packet
	        		}
	        			
	                m_ports[PORT_OUT].pushOut(packet);
	        }
	        else
	        { error( "Received packet on invalid port: " + port_number);}	

	    }
	   
	    public JPanel getPropertiesView(){
	    	return new LossPopup(this);
	    }  
	    
	    public int getDrops() { return m_drops; }
	    
	    /** Save the current loss value */
	    public Properties getSerializableProperties(boolean isTransient){
	    	Properties props = super.getSerializableProperties(isTransient);
	    	props.setProperty("loss", "" + mLoss);
	    	return props;
	    }
	    
	    /** Set the current loss rate based on serialized properties */
		public void initializeProperties(Properties props) {
			for(Enumeration e = props.keys(); e.hasMoreElements(); ){
				String key = (String)e.nextElement();
				String value = props.getProperty(key);

				if(key.equals("loss")){
					try{ 
						mLoss = Float.parseFloat(value);
					}catch (Exception ex){
						ex.printStackTrace();
					}
				}
			}
		}
		
		public void dropNextPacket() { mDropNextPacket = true; }
		
	    public Color getColor() { return Color.PINK; }
}
