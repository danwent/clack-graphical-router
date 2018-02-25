package net.clackrouter.component.base;

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.clackrouter.gui.ClackPaintable;
import net.clackrouter.gui.ClackView;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.DefaultPropertiesView;
import net.clackrouter.router.core.Alarm;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.graph.ComponentCell;
import net.clackrouter.router.graph.ComponentView;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.VertexView;

/**  
 * ClackComponent is the abstract base class for all component blocks that comprise router functionality.  
 *
 * <p> Each ClackComponent has a collection of {@link ClackPort} objects, each of which represents either a packet input or 
 *  output for the packet processing block.  ClackPort objects handle the actual passing of packets between ClackComponents.</p>
 * 
 * <p> There are two main methods of transfering packets: </p> 
 * 
 * <p>The first is "pushing" packets, where components receive packets via the {@link #acceptPacket(VNSPacket, int) } 
 * method. Packets are pushed out ports using the {@link ClackPort#pushOut(VNSPacket)} method of ClackPort objects.
 * </p>
 * 
 * <p>
 * The second mechanism is "pulling" packets, in which a component uses the {@link ClackPort#pullIn()} to ask a connected
 * component if it has a packet available for processing.  ClackComponents respond to these queries using the 
 * {@link #handlePullRequest(int)} method.
 * </p>
 * <p>
 * The {@link ClackComponentListener} class can be used to keep track of component events by registering as a listener
 * to any class extending ClackComponent.
 * </p>	  
 */



public abstract class ClackComponent implements Alarm {
	
	/** Unique counter */
	public static int UNIQUE_COUNT = 0; 
	
	public static final int SIGNAL_ERROR_LEN_MSEC = 300;
	
	/** Contains all ports used by this components */
	protected ClackPort[] m_ports; 
	/** Number of ports used by this component */
	protected int m_num_ports;
	/** Component's name, unique within a single router */
	protected String m_name;
	
	protected ClackPaintable view;
	
	/** Total number of packets that have arrived at component input ports (modified by ClackPort object)*/
	public int m_packetcount_in;
	/** Total number of packets that have exited component output ports (modified by ClackPort object)*/
	public int m_packetcount_out;
	
	/** Text log console visible from the components properties view
	 *  Text is added by both the log() and error() methods
	 *  */
	protected JTextArea m_log;
	/** Router containing this component */
    protected Router mRouter = null;
    /** The JGraph component cell representing this ClackComponent */
	protected ComponentCell componentCell;
    /** List of components implementing the 
     * @see ClackComponentListener#componentEvent(ClackComponentEvent) method 
     * */
	protected ArrayList mListeners; // array of VNSComponentListener classes
	
	protected boolean m_has_error; // flash red, but briefly
	protected boolean pendingError; // be outlined in red until someone looks at pview

	/**
	 * Constructor, should be called by all sub-classes to set-up the component
	 * 
	 * @param r the router hosting this component
	 * @param name of the component, unique within the router
	 */
	public ClackComponent(Router r, String name) 
    {
		mRouter = r;
	    m_packetcount_in = 0;
	    m_packetcount_out = 0;	
	    mListeners = new ArrayList();
        m_name = name;
        
        // initialize log
        m_log = new JTextArea();
        m_log.setEditable(false);
        m_log.setRows(100);
        m_log.setColumns(40);
        
  //      m_timer_expires = NO_TIMER;
    }

	/**
	 * Allocates an array of ClackPort objects to be used by this component
	 * 
	 * @param num_ports the number of ports to create
	 */
	protected void setupPorts(int num_ports){
		m_num_ports = num_ports;
		m_ports = new ClackPort[num_ports];
	}

	/**
	 * <p>Called when a neighboring component wishes to "pull" a packet from this component.</p>
	 * 
	 * <p>This function should only be valid for port numbers that are defined as pull ports.
	 * This is enforced by the {@link ClackPort } class.  By default, we return nothing. </p> 
	 * 
	 * @param port_number The port that a packet is being requested on
	 * @return A packet if available for pull, or null
	 */
    public VNSPacket handlePullRequest(int port_number) { return null; }
    
    /**
     * <p>The starting point for packet-processing when packets are "pushed" to this component.</p>
     * 
     * <p>The default implementation warns that the packet is being ignored.</p>
     * 
     * @param packet  The pushed packet
     * @param port_number  The number of the port that this packet is arriving on
     */
    public void acceptPacket(VNSPacket packet, int port_number)  { error(" acceptPacket called but not overridden"); }
    
    /**
     * <p>Callback method to implement component functionality that is not initiated by a packet push.</p>
     * 
     * <p>The pull method is often used to implement a "pull" component, or to serve as a packet source.  
     * The Router calls poll() once per processing loop if the component has been registered using the 
     * {@link Router#registerForPoll(ClackComponent) } method.  </p>
     * 
     * 
     */
	public void poll() { 
			error("This component does not implement poll() : " + this.m_name);	
	}
	
	public void setAlarm(long msecs_from_now){
		mRouter.getTimeManager().setAlarm(this, msecs_from_now);
	}
	
	public void notifyAlarm() {
		error("This component does not implement notifyAlarm() : " + this.m_name);
	}
  
	/**
	 * <p>Used to get all values the component would like to serialize to file. </p> 
	 * 
	 * <p>These values are stored as String -> String pairs in a {@link java.util.Properties} object.  These
	 * properties will be passed to {@link #initializeProperties(Properties)} when the Component is recreated. </p>
	 * 
	 * <p>Serialization can be transient or not, which determines whether all or just some of the internal component state
	 * is saved.  Transient serialization means that some properties will be saved which may make this serialized file
	 * invalid for use on different network or router set-ups.  For example, saving info specific to the IP addresses
	 * of a certain topology would only happen if transient serialization was used.  However, it can be desireable to save
	 * these properties when setting up exact demonstrations. </p>
	 * 
	 * <p> By default we have no properties to serialize, so we return an empty object </p>
	 * 
	 * @param isTransient flag indicating if serialization is transient
	 * @return all property key-value pairs to serialize
	 */
	public Properties getSerializableProperties(boolean isTransient){ return new Properties(); }
	
	/**
	 * <p>Method used to initialize a component based on serialized properties values saved to a file.</p>
	 * 
	 * <p> By default, no action is performed </p>
	 * 
	 * @param props all property values serialized for this component
	 */
	public void initializeProperties(Properties props) { /*nothing done by default */}
	
	/**
	 * Method for getting the properties view associated with this component
	 * 
	 * Property views are used to display more detailed information about internal component
	 * state.  This method creates a default property view, and can be overriden to provide
	 * custom properties views
	 * 
	 * @return this component's property view
	 */
	public JPanel getPropertiesView() {
		return new DefaultPropertiesView(m_name, this, this.getTypeName());
	}
	/**
	 * Classes that want to be updated on component events implement the {@link ClackComponentListener}
	 * interface and send a reference of themselves to this method.  
	 * 
	 * @param listener listener to be added
	 */
	public void registerListener(ClackComponentListener listener) { mListeners.add(listener); }
	
	/**
	 * Removes component from list of objects receiving notifications about this component
	 * 
	 * @param listener listener to be removed
	 */
	public void unregisterListener(ClackComponentListener listener) { mListeners.remove(listener); }
	
	/**
	 * for each reportable event, this calls the {@link ClackComponentListener#componentEvent(ClackComponentEvent)} method of each listener
	 * 
	 * @param e describing what kind of an event occured, which is passed to each listener
	 */
	public void fireListeners(ClackComponentEvent e) 
    {
		for(int i = 0; i < mListeners.size(); i++)
        { ((ClackComponentListener)mListeners.get(i)).componentEvent(e); }
	}
	
	/**
	 * Get the total number of ports (input + output) for this component
	 */
	public int getNumPorts() { return m_num_ports; }	
	
	/**
	 * Get a reference to a specific port, by number
	 * @param port_num
	 */
	public ClackPort getPort(int port_num){ return m_ports[port_num];}

	/**
	 * Tells whether this component is hierarchical, that is, capable of being 
	 * zoomed into within a Clack Router.
	 * 
	 * @return default value is false
	 */
	public boolean isHierarchical() { return false; }
	
	
	/**
	 * Supplies hierarchical view that implements {@link ClackView} if {@link #isHierarchical()} is true
	 * 
	 */
	public ClackView getHierarchicalView() { return null; }
	
	/**
	 * Used to set the {@link ComponentCell} object representing this component in the Router Graph
	 * 
	 * @param cell - the corresponding ComponentCell
	 */
	public void setComponentCell(ComponentCell cell) { componentCell = cell; }
	
	/**
	 * Retrieve the {@link ComponentCell} object representing this component in the Router Graph
	 */
	public ComponentCell getComponentCell() { return componentCell; }
	
	/**
	 * Returns the View object used to render this component in the Router Graph.
	 * 
	 * By default, we use a ComponentView, which renders the component as a simple colored
	 * box with the component type written inside of it.  More advanced component sub-classes that 
	 * wish to be displayed differently should override this method to return a custom VertexView sub-class
	 * 
	 * @param graph the Router Graph
	 * @param mapper the CellMapper associated with this Router Graph
	 * @return the VertexView for rendering this component
	 */
	public VertexView getView(JGraph graph, CellMapper mapper) { return new ComponentView(componentCell, graph, mapper);}

   
    /**
     * Clones the component using reflection to create a deep copy.
     * 
     * Transfers all non-transient serializable properties to the new component
     * 
     * @return a copy of this component
     */
    public ClackComponent createCopy() {
    	
		try {	
			Constructor constructor = this.getClass().getConstructor(new Class[] { Router.class, String.class} );
			ClackComponent newcomp = (ClackComponent)constructor.newInstance(new Object[] { mRouter,  this.getName() + "(2)"  });
			newcomp.initializeProperties(this.getSerializableProperties(false)); // setup same internal state
			return newcomp;
		} catch(Exception e){
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * Writes an error message to the component console log and prints it to Standard Error
     * @param s the error message to print
     */
    public void error(String s) { 
    	signalError();
    	System.err.println(mRouter.getHost() + ": " + s);
    	log(s); 
    	setPendingError(true);
    	mRouter.setPendingError(true); // will be cleared once user inspects the error
    }
    
    /**
     * Writes a message to the log console
     * @param s the log message
     */
    public void log(String s) { 
        m_log.append(s + "\n");  
        m_log.setCaretPosition(m_log.getDocument().getLength());
    
    }
	/**
	 * Reports whether this component modifies packets passed through it
	 * 
	 * This value is used by the static checking algorithm used to make sure port
	 * connections are valid. Sub-classes that do not modify packets should override 
	 * this method
	 *  
	 * @return by default, we assume the component modifies packets
	 */
	public boolean isModifying() { return true; }
	
	/**
	 * set component's name
	 */
    public void setName(String name){	m_name = name; }
    
    /**
     * Get an integer identifier unique for this type of component
     */
	public static int getUniqueCount() {	return UNIQUE_COUNT++;}
	
	/**
	 * Get component name, which is unique within a single router
	 */
    public String getName(){    return m_name; }   
    
    /**
     * Get the router that contains this component
     */
    public Router getRouter() { return mRouter; }	
    
    /**
     * The total number of packets to have entered this component
     */
	public int getPacketCountIn() { return m_packetcount_in; }
	
	/**
	 * The total number of packets to leave this component
	 */
	public int getPacketCountOut() { return m_packetcount_out; }
	
	/**
	 * Get a reference to the console log of this component
	 */
    public JTextArea getLog() { return m_log; }
    
    /**
     * <p>The color this component should be rendered in, assuming a standard component rendering by the 
     * {@link net.clackrouter.router.graph.ComponentView.ComponentRenderer} class.  </p>
     * 
     * <p> Other renderers may ignore this value.</p>
     * @return the rendering color
     */
    public Color getColor() { return new Color(255,255,153); } // -- default color.. kinda yellowish 

    /**
     * The simple name that identifies this type of component. 
     * 
     * <p> default implementation is simply the class name with no package information </p>
     * 
     * @return the component's type name
     */
	public String getTypeName() {
		String[] arr = this.getClass().getName().split("\\.");
		if(arr.length < 1) return "Unknown";
		else return arr[arr.length - 1];
	}
	
	/**
	 * Tells GUI is this component recently experienced an error
	 */
	public boolean hasError() { return m_has_error; }
	protected void signalError() {
		m_has_error = true;
		try_repaint();
		Waiter w = new Waiter(SIGNAL_ERROR_LEN_MSEC);
		w.start();
	}
	
	public void try_repaint() {
    	if(view != null)
    		view.clackPaint();		
	}
	
	// simple class to wait a certain number of milliseconds
	protected class Waiter extends Thread {
		long millisec;
		public Waiter(long millis) { millisec = millis; }
		public void run() {
			try { 
				sleep(millisec); 
				m_has_error = false; 
		    	if(view != null)
		    		view.clackPaint();
			} catch (Exception e) { /* ignore */ }
		}
	}

	
    /**
     * Set the view associate with this ClackComponent
     */
    public void setView(ClackPaintable v){ 	view = v;  }
	
	// PORT CREATION HELPER METHODS
	
	public void createInputPushPort(int port_num, String descr, Class type){
		m_ports[port_num] = new ClackPort(this, port_num, descr,
							ClackPort.METHOD_PUSH, ClackPort.DIR_IN, type);
	}

	public void createInputPullPort(int port_num, String descr, Class type){
		m_ports[port_num] = new ClackPort(this, port_num, descr,
				ClackPort.METHOD_PULL, ClackPort.DIR_IN, type);		
	}
	
	public void createOutputPushPort(int port_num, String descr, Class type){
		m_ports[port_num] = new ClackPort(this, port_num, descr,
							ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, type);
	}

	public void createOutputPullPort(int port_num, String descr, Class type){
		m_ports[port_num] = new ClackPort(this, port_num, descr,
				ClackPort.METHOD_PULL, ClackPort.DIR_OUT, type);		
	}	
	
	public void showErrorDialog(String e){
		JOptionPane.showMessageDialog(this.getRouter().getDocument().getFramework(),
			    e,
			    "Clack Component Error",
			    JOptionPane.ERROR_MESSAGE);
	}
	
	public long getTime() { return mRouter.getTimeManager().getTimeMillis(); }
	
	public boolean getPendingError() { return pendingError; }
	public void setPendingError(boolean e) {	pendingError = e; }
	
	/** Conveniance method to push a packet out a particular port */
	protected void sendOutPort(VNSPacket p, int port_num){
		if(port_num >= m_ports.length){
			error("Tried to push packet out invalid port # " + port_num);
			return;
		}
		m_ports[port_num].pushOut(p);
	}
	

} // -- class ClackComponent
