/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.component.extension;


import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.CounterPopup;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.graph.CounterView;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.VertexView;



/**
 * <p>Simple component that transparently passes packets on to the next component
 * , but graphically displays the number of packets that have 
 * passed through it. </p> 
 */
public class Counter extends ClackComponent {

	private long last_update;
	private long update_wait;
	public static int PORT_IN = 0;
	public static int PORT_OUT = 1;
	public static int DEFAULT_WAIT_MILLIS = 500;
	private CounterView view;
	
	private int m_unique_count;
	public static int LAST_UNIQUE_COUNT = 0;
	private String cLabel; 
	
	public Counter(Router router, String name){
		super(router, name); 
		last_update = 0;
		update_wait = DEFAULT_WAIT_MILLIS;
		m_unique_count = ++LAST_UNIQUE_COUNT;
		cLabel = this.getTypeName() + m_unique_count;
		setupPorts(2);
	}
	
	public void setCounterView(CounterView v){view = v;}
	
	public CounterView getCounterView(){return view;}
	
	public int getMyUniqueCount() {return m_unique_count;}
	
	/** set the label for the counter */
	public void setCLabel(String cLabel) {
		this.cLabel = cLabel;
	}
	
	public String getCLabel() {	return cLabel;}
	
	
    protected void setupPorts(int numPorts)
    {
    	super.setupPorts(numPorts);
        String in_desc = "Counter input";
        String out_desc = "Counter output";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; }
    
    public void acceptPacket(VNSPacket packet, int port_number)
    {
        
        if ( port_number == PORT_IN ){
        	// note: we STILL want user perceived time here, not clack time
        	long current = System.currentTimeMillis();
        	// if we haven't updated in the last update_wait msecs, redraw
        	if(current - last_update > update_wait) {
        		last_update = current;
        		view.clackPaint();
        	}
        	m_ports[PORT_OUT].pushOut(packet); 
        } else
        { error( "Received packet on invalid port: " + port_number); }

    } // -- acceptPacket
    
    public void resetCounter() {
    	m_packetcount_in = 0;
    	view.clackPaint();
    }
    
    public JPanel getPropertiesView() {
    	return new CounterPopup(this);
    }

    /**
     * Override ClackComponent's method to provide for special drawing
     * of the Counter with a CounterView object.
     */
	public VertexView getView(JGraph graph, CellMapper mapper) { 
		return new CounterView(getComponentCell(), graph, mapper);
	}
}
