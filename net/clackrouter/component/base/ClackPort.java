
package net.clackrouter.component.base;


import java.util.ArrayList;

import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.graph.RouterWire;
import net.clackrouter.test.ErrorChecker;

/** 
 * <p>The ClackPort handles the actual transfer of packets between different components that have been connected. </p>
 * 
 * <p>The direction will be either "in" or "out".  two ports can be connected only if their directions are opposite
 * The type will be either "push" or "pull".  two ports can be connected only if their types are the same. </p>
 * 
 * <p>The port classes do the actual work of transfering packets between two components.  See methods {@link #pushOut(VNSPacket)},
 * {@link #pullIn()}, {@link #acceptPushRequest(VNSPacket)}, and {@link #acceptPullRequest()} for details </p>
 * 
 * <p>Ports are connected using the static {@link ClackPort#connectPorts(ClackPort, ClackPort)} method </p>
 * 
 * <p>Ports have a type, which is the Java {@link Class} object that represents a type of {@link VNSPacket}.  A port can
 * accept packets of any class that is a sub-type of its specified type, thus VNSPacket
 * is the most general type and can accept any packet.  </p>
 * 
 * <p> For push ports, many output ports can be connected to a single input port, but a single output port can only 
 * be connected to a single input port.  For pull ports, each input port can only be connected to a single output port, 
 * but an output port can be connected to multiple input ports.  </p>
 */

public class ClackPort {
	
	// ints defining type and direction values
	public static int METHOD_PUSH = 0;
	public static int METHOD_PULL = 1;
	public static int DIR_IN = 3;
	public static int DIR_OUT = 4;

	private ClackComponent m_owner; // the clack component owning this port
	private RouterWire mListeningWire; // the wire connected to this port
	private int m_port_number; // the value of this port in the parent component's m_ports[] array.
	private int m_method; // push or pull 
	private int m_direction; // in or out
	private Class m_type;  // describes the type of packets that can be passed by the port
	private String m_text_description; // textual description of what this port "means" to the component
	private ArrayList m_connected_ports; // port that this component is connected to
	
	public ClackPort(ClackComponent owner, int port_num, String description ,int method, int direction, Class type) {
		m_owner = owner;
		m_port_number = port_num;
		m_method = method;
		m_direction = direction;
		m_text_description = description;
		m_type = type;
		m_connected_ports = new ArrayList();
	}
    
	/** 
	 * <p>The preferred method for connecting two ports.</p>
	 * 
	 * <p> Performs checks to assure that the connection is valid, returning an error String if it encounters a problem.
	 * We check both basic issues like method, direction and number of connections, and also call the more advanced type
	 * checking algorithm to assure that this connection does not allow any type failures
	 * Returning null denotes success </p>
	 * 
	 * @param source the output port originating packets in this connection
	 * @param dest the input port receiving packet in this connection
	 * @return error message, or null on success
	 */
	public static String connectPorts(ClackPort source, ClackPort dest){
		if(source == null || dest == null) return "cannot connect null ports";
		if(source.isConnectedTo(dest) || dest.isConnectedTo(source)) return "ports already connected";
		if(source.getDirection() == dest.getDirection()) return "ports have same direction (IN or OUT)";
		if(source.getMethod() != dest.getMethod()) return "ports must have same method (PUSH or PULL)";
		if(source.getNumConnectedPorts() > 0 && source.getMethod() == METHOD_PUSH) return "output push ports can only have one connection";
		if(dest.getNumConnectedPorts() > 0 && dest.getMethod() == METHOD_PULL) return "input pull ports can only have one connection";
		
		source.addPort(dest);
		dest.addPort(source);
		
		String error = ErrorChecker.checkRouter(source.getOwner().getRouter());
		if(error != null){
			source.removePort(dest);
			dest.removePort(source);
			return error;
		}
		
		return null;
	}
	
	/**
	 * Disconnects two ports
	 */
	public static void disconnectPorts(ClackPort source, ClackPort dest){
		source.removePort(dest);
		dest.removePort(source);
	}
	


	/**
	 * <p>Called by the parent component when it wants to send (push) a packet out this port.</p>
	 * 
	 * <p>It simply calls the acceptPushRequest() method on the port it is connected to.
	 * Also performs run-time checks to make sure port semantics are not violated. </p>
	 * 
	 * <p> As a source port, we also take care of enforcing the router speed delay and 
	 * highlighting the RouterWire </p>
	 */
	public void pushOut(VNSPacket packet) {

		if(0 == m_connected_ports.size()){
			m_owner.error("Error:  " + m_owner.m_name + " pushed packet out unconnected port " + m_port_number + 
					"(" + this.getTextDescription() + ")");
			return; // not connected to anything
		}
		
		if(m_method != METHOD_PUSH || m_direction != DIR_OUT){
			m_owner.error("Error:  " + m_owner.m_name + " pushed packet out invalid port #" + this.m_port_number);
			return; 
		} 
		if(!m_type.isAssignableFrom(packet.getClass())){
			m_owner.error("Error:  " + m_owner.m_name + " pushing out packet of invalid type (" + 
					ErrorChecker.getClassNameOnly(packet.getClass()) + " on port " + m_port_number);
			return;
		}
		m_owner.getRouter().getTimeManager().tick();
		// this is the source port, so check if we need to notify a wire
		if(packet != null && mListeningWire != null)
			mListeningWire.wireUsed();
		
		ClackPort connected_port = (ClackPort)m_connected_ports.get(0); // can only be connected to one output
		m_owner.m_packetcount_out++;
		fireListenersPacketOut(m_port_number);
		connected_port.acceptPushRequest(packet);	
	}
	
	/**
	 * Called by a connected port that is pushing a packet to us.  The port just
	 * passes the packet on to its parent component after performing error checking.
	 */
	public void acceptPushRequest(VNSPacket packet){
		if(m_method != METHOD_PUSH || m_direction != DIR_IN || m_connected_ports.size() < 1){
			m_owner.error("Packet pushed in invalid port #" + m_port_number);
			return; 
		}
		if(!m_type.isAssignableFrom(packet.getClass())){
			m_owner.error("Received packet of invalid type (" + ErrorChecker.getClassNameOnly(packet.getClass()) + " on port " + m_port_number);
			return;
		}
		fireListenersPacketIn(m_port_number);
		m_owner.m_packetcount_in++;
		m_owner.acceptPacket(packet, m_port_number);
	}
	
	/**
	 * Called by the parent component when it wants to try and pull a packet in through this port.
	 * 
	 * <p>This method performs error checking and simply calls acceptPullRequest() on the port it is connected to
	 * only valid if this port is a pull port. </p>
	 */
	public VNSPacket pullIn(){
		if(m_method != METHOD_PULL || m_direction != DIR_IN ) {
			m_owner.error("Attempted to pull in packet on invalid port #" + m_port_number);
			return null;
		}

		if(m_connected_ports.size() > 0) {
			ClackPort connected_port = (ClackPort)m_connected_ports.get(0);
			VNSPacket packet = connected_port.acceptPullRequest();
			if(packet == null) return null;
			
			if(!m_type.isAssignableFrom(packet.getClass())){
				m_owner.error("Pulling in packet of invalid type (" + ErrorChecker.getClassNameOnly(packet.getClass()) + " on port " + m_port_number);
				return null;
			}
			fireListenersPacketIn(m_port_number);
			m_owner.m_packetcount_in++;
			return packet;
		}
		return null;
	}

	/**
	 * Called by a connected port that is trying to pull a packet from this port.
	 * <p> This port asks its parent component if it has any packets to be pulled on this port, and
	 * then then sends a packet to our connected port if there is a packet. </p>
	 * 
	 * <p> As a source port, we also take care of enforcing the router speed delay and 
	 * highlighting the RouterWire </p>
	 */
	public VNSPacket acceptPullRequest(){				
		if(m_method != METHOD_PULL || m_direction != DIR_OUT ) {
			m_owner.error("Received pull request on invalid port #" + m_port_number);
			return null;
		}	
		VNSPacket packet = m_owner.handlePullRequest(m_port_number);
		if(packet == null) return null;
		
		if(!m_type.isAssignableFrom(packet.getClass())){
			m_owner.error("Gave pulled packet of invalid type (" + ErrorChecker.getClassNameOnly(packet.getClass()) + " on port " + m_port_number);
			return null;
		}
		
		m_owner.getRouter().getTimeManager().tick();
		
		// this is the source port, so check if we need to notify a wire
		if( mListeningWire != null )
			mListeningWire.wireUsed();
		
		fireListenersPacketOut(m_port_number);
		m_owner.m_packetcount_out++;
		
		return packet;
	}
	
	// helper method to fire owner component's listeners
	private void fireListenersPacketIn(int port){
		m_owner.fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_PACKET_IN, new Integer(port)));
	}
	//	 helper method to fire owner component's listeners
	private void fireListenersPacketOut(int port){
		m_owner.fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_PACKET_OUT, new Integer(port)));
	}
	
	
	/**
	 * Adds a port to this port's list of connected ports and fires an event.  This operation is idempotent.
	 */
	private void addPort(ClackPort other_port){
		m_connected_ports.add(other_port);
		m_owner.fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_CONNECTION_CHANGE));
	}
	
	/**
	 * removes a port from this port's list of connected ports and fires an event.  This operation is idempotent.
	 */
	private void removePort(ClackPort port){
		m_connected_ports.remove(port);
		m_owner.fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_CONNECTION_CHANGE));
	}
	
	public Class getType() { return m_type; }
	public int getMethod() { return m_method; }
	public int getDirection() { return m_direction; }
	public String getTextDescription() { return m_text_description; }
	public String getMethodString() { return (m_method == METHOD_PUSH) ? "PUSH" : "PULL"; }
	public String getDirString() { return (m_direction == DIR_IN) ? "IN" : "OUT";	}
	public ClackComponent getOwner() { return m_owner; }
	public ClackPort getConnectedPort(int index) { 
		if(index < m_connected_ports.size())
			return (ClackPort)m_connected_ports.get(index); 
		return null;
	}
	public int getNumConnectedPorts() { return m_connected_ports.size(); }
	public int getPortNumber() { return m_port_number; }
	/**
	 * Determine if this port is already connected to another port.
	 */
	public boolean isConnectedTo(ClackPort p) { return m_connected_ports.contains(p); } 
	
    // this could be a more general listening
    // setup similar to components, but since it is 
    // called so often i will keep it simple
	/**
	 * Set RouterWire that is connected to this port (used for RouterWire highlighting).
	 */
    public void setListeningWire(RouterWire w){	mListeningWire = w; }
    /**
     * Get RouterWire that is connected to this port (used for RouterWire highlighting).
     */
    public RouterWire getListeningWire() { return mListeningWire; }
	
}
