
package net.clackrouter.component.base;

import java.awt.Color;

import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;

/**
 *
 * <p> A simple component used to convert from a pull connection to a push connection.</p>
 * <p>For example, it could be placed to pull objects out of a queue and send them to a component
 * that only has a push input.  The must significant use is for testing, but in some cases full router
 * configurations could benefit need such functionality.  </p>
 */
public class PullToPush extends ClackComponent {

	public static int PORT_IN = 0;
	public static int PORT_OUT = 1;
	public static int NUM_PORTS = 2;
	
	public PullToPush(Router r, String name){
		super(r, name);
		setupPorts(NUM_PORTS);
		r.registerForPoll(this);
	}
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input   = "input from pull port";
        String output = "output to push port";

        m_ports[PORT_IN]   = new ClackPort(this, PORT_IN,  input, ClackPort.METHOD_PULL, ClackPort.DIR_IN, VNSPacket.class); 
        m_ports[PORT_OUT] = new ClackPort(this, PORT_OUT, output, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
          
    } // -- setupPorts
    
    public boolean isModifying() { return false; }
    
    /**
     * Everytime there is a poll() call, push any packet out our push port
     */
    public void poll(){
    	VNSPacket p = m_ports[PORT_IN].pullIn();
    	
    	if(p != null) {
    	//	System.out.println("PullToPush found packet with length = " + p.getLength());
    		m_ports[PORT_OUT].pushOut(p);
    	}
    }
    
    public Color getColor() { return Color.ORANGE; /*why not?? */ }
}
