
package net.clackrouter.component.tcp;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.gui.ClackView;
import net.clackrouter.gui.tcp.TCPView;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;


/**
 * A class representing a simplified TCP stack on a local host.  
 * 
 * <p> This TCP stack can respresent multiple
 * IP addresses, and Transmission Control Blocks (TCBs) for ports on each of those 
 * addresses. This component takes care of demultiplexing packets for processing by each
 * TCB, and also performs the TCP checksum.  Some portions of TCP behavior are simplified
 * from that of a real TCP stack.  For example, sender or receiver side congestion control 
 * is not implemented, and reset packets are ignored.  </p>
 */
public class TCP extends ClackComponent {

	public static int TYPE_UNKNOWN = 0;
	public static int TYPE_SERVER = 1;
	public static int TYPE_CLIENT = 2;

	public static int PORT_IN = 0;
	public static int PORT_NET_OUT = 1;
	public static int PORT_UNREACH_OUT = 2;
	public static int PORT_INTERNAL_TO_NET = 3;
	public static int NUM_PORTS = 4;
	
	private Hashtable mPortEntries;
//	private HierarchComponent mHierarchComp;

	public TCP( Router r, String name){
		super(r,name);
		setupPorts(NUM_PORTS);
		mPortEntries = new Hashtable();
		r.setTCPStack(this);
	}
	/*
	public void setHierarchComp(HierarchComponent hc) { 
		mHierarchComp = hc; 
	}
	public HierarchComponent getHierarchComp() { return mHierarchComp; }
	*/
	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
        String to_desc = "Packets destined for a TCP socket";
        String from_desc = "Packets sent from a TCP Socket";
        String unreach_desc = "Packet sent to unreachable ports";
        String internal_desc = "Internal port: packets sent by internal components to reach net";
        
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, to_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_NET_OUT] =  new ClackPort(this, PORT_NET_OUT,  from_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_UNREACH_OUT] = new ClackPort(this, PORT_UNREACH_OUT, unreach_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_INTERNAL_TO_NET] =  new ClackPort(this, PORT_INTERNAL_TO_NET,  internal_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
    } // -- setupPorts
	
	public boolean isHierarchical() { return true; }

	public ClackView getHierarchicalView() { return new TCPView(this, getRouter().getDocument()); }
	
	/**
	 * Strips the IP header and handles the packet for one of the following three cases:
	 * <ul>
	 * <li> If the port is a simple client-end of a connection, it hands the packet to this TCB.
	 * <li> If the port is a listening server, it attempts to hand the packet to the TCB associated with this
	 * remote host and port.  If no TCB is found, the packet is added to this port's wait queue.  
	 * <li> If no mapping exists for this port, the packet is pushed out the "unreachable port" to 
	 * be used to generate an ICMP port unreachable message.  
	 * </ul>
	 */
	public void acceptPacket(VNSPacket packet, int port_num){
	
		if(port_num == PORT_IN){

			IPPacket ippacket = (IPPacket)packet;
			VNSTCPPacket tcppacket = new VNSTCPPacket(ippacket.getBodyBuffer());
			try {
				tcppacket.setSourceIPAddress(InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array()));
				tcppacket.setDestIPAddress(InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array()));
			}catch (Exception e){ 
				e.printStackTrace();
				return; // don't recover from parsing bad addr, just drop packet
			}
			String key = makeKey(tcppacket.getDestIPAddress(), tcppacket.getDestinationPort());
			PortEntry entry = (PortEntry)mPortEntries.get(key);
			if(entry == null || entry.type == TYPE_UNKNOWN){
	        	signalError();
	        	error("Packet to unused TCP port " + tcppacket.getDestinationPort()); 
				m_ports[PORT_UNREACH_OUT].pushOut(packet);
				return;
			}else if(entry.type == TYPE_CLIENT){
				// there should be exactly one TCB in the list
				TCB tcb = (TCB)entry.active_tcbs.get(0);
				tcb.acceptPacket(packet, TCB.PORT_FROM_NET);
			}else if(entry.type == TYPE_SERVER){
				// sent to a port with a server.  we must find the child 
				// socket associated with this remote address/port
				for(int i = 0; i < entry.active_tcbs.size(); i++){
					TCB tcb = (TCB)entry.active_tcbs.get(i);
					boolean same_addr = tcb.remote_address.equals(tcppacket.getSourceIPAddress());
					boolean same_port = tcb.remote_port == tcppacket.getSourcePort();
					if(same_addr && same_port){
						tcb.acceptPacket(packet, TCB.PORT_FROM_NET);
						return;
					}
				}
				// no match, must be new connection attempt
				if(tcppacket.synFlagSet()){
					entry.queued_conns.add(tcppacket);
				}
			}
		} else if( port_num == PORT_INTERNAL_TO_NET){
			m_ports[PORT_NET_OUT].pushOut(packet);
		}
	}
	
	/*
	 * Is this function needed for anything??
	public ArrayList getAllActiveTCBS() {
		ArrayList list = new ArrayList();
		for(Enumeration e = mPortEntries.elements(); e.hasMoreElements();){
			PortEntry entry = (PortEntry) e.nextElement();
			for(int i = 0; i < entry.active_tcbs.size(); i++){
				list.add(entry.active_tcbs.get(i));
			}
		}
		return list;
	}
	*/
	
	/**
	 * Method used when the server port identified by this TCB is calling accept()
	 */ 
	public TCB serverAcceptCall(TCB tcb) throws Exception {
		String key = makeKey(tcb.local_address, tcb.local_port);
		PortEntry entry = (PortEntry)mPortEntries.get(key);
		if(entry.type != TYPE_SERVER) throw new Exception("Cannot call Accept() on a non-listening socket");
		if(entry.queued_conns.size() > 0){
			VNSTCPPacket packet = (VNSTCPPacket)entry.queued_conns.remove(0);
			TCB newTCB = new TCB(this, mRouter, tcb.getName() + " (child " + (getTime() % 10000) + ")");
			newTCB.connect_no_gui();
			ClackPort.connectPorts(newTCB.getPort(TCB.PORT_TO_NET), this.getPort(TCP.PORT_INTERNAL_TO_NET));
			entry.active_tcbs.add(newTCB);
			newTCB.notifySynReceived(packet);
			return newTCB;
		}
		return null;
	}
	
	/**
	 * Used to create a Transmission Control Block (TCB) to be associated with a socket.  
	 * @param name a name identifying the TCB and socket.  
	 * @return created TCB
	 */
	public TCB createTCB(String name) {
		return new TCB(this, mRouter, name);
	}
	/**
	 * method called when a socket is bound to a local address with bind() 
	 */ 
	public void createMapping(TCB tcb)throws Exception {
		String key = makeKey(tcb.local_address, tcb.local_port);
		if(mPortEntries.get(key) != null)
			throw new Exception(tcb.local_address + ":" + tcb.local_port + " is already bound to an application");
		log("creating mapping for key = " + key);
		PortEntry entry = new PortEntry(TYPE_UNKNOWN); //unknown until connect() or listen()
		mPortEntries.put(key, entry);
	}
	
	/**
	 * Sets the type of this TCB to LISTENING and creates a mapping for later look-ups. 
	 * @param tcb
	 * @throws Exception
	 */
	public void setupListenMapping(TCB tcb)throws Exception {
		tcb.connection_state = TCB.STATE_LISTEN;
		String key = makeKey(tcb.local_address, tcb.local_port);
		PortEntry entry = (PortEntry)mPortEntries.get(key);
		entry.type = TYPE_SERVER;
	}
	
	/**
	 * Sets the type of this TCB to CLIENT and creates a mapping for later look-ups.  
	 * @param tcb
	 */
	public void setupClientMapping(TCB tcb){
		String key = makeKey(tcb.local_address, tcb.local_port);
		log("client mapping for key = " + key);
		PortEntry entry = (PortEntry)mPortEntries.get(key);
		entry.type = TYPE_CLIENT;
		entry.active_tcbs.add(tcb);
		ClackPort.connectPorts(tcb.getPort(TCB.PORT_TO_NET), this.getPort(TCP.PORT_INTERNAL_TO_NET));
	}
	
	/**
	 * Not currently used, but removes a mapping once a socket no longer exists on a port.
	 * TODO: when a socket is destroyed, free the assoicated port.  
	 * @param tcb
	 */
	public void removeTCB(TCB tcb){
		String key = makeKey(tcb.local_address, tcb.remote_port);
		log("Removing TCB with key: " + key);
		mPortEntries.remove(key);
		//fireListeners(new TCBUpdateEvent(TCBUpdateEvent.TYPE_REMOVED, tcb));
	}
	
	/**
	 * create a key for this IP/Port pair to be used for hashtable lookups.  
	 * @param addr
	 * @param port
	 * @return
	 */
	private String makeKey(InetAddress addr, int port){
		return addr.toString() + ":" + port;
	}
	
	/**
	 * 
	 * @author Dan
	 *
	 * Data structure to represent TCP state for a single port.  
	 * 
	 * Types are SERVER, CLIENT or UNKNOWN (ie:
	 * the user has bound to this port, but has not yet called connect() or listen())
	 * 
	 * Has listed both for all active TCBs (a server port can have many, a client only one),
	 * and for queued connections (applies only to server ports).  
	 */
	private class PortEntry {
		public PortEntry(int t) { type = t; }
		public int type;
		public ArrayList active_tcbs = new ArrayList();
		public ArrayList queued_conns = new ArrayList(); 
	}
	
	/**
	 * TCP specific class to provide more information to a GUI object about
	 * changes to the TCP data.  
	 */
	public static class UpdateEvent extends ClackComponentEvent {
		public static int TCP_SOCKET_CREATED = getUniqueInt();
		public static int TCP_TCB_CREATED = getUniqueInt();
		public Object mObj;
		public UpdateEvent(int type, Object o){
			super(type);
			mObj = o;
		}
	
	}
	
	
}
