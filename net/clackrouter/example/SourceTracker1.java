/*
 * Created on Sep 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.example;

import java.net.InetAddress;
import java.util.Hashtable;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;

/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SourceTracker1 extends ClackComponent {
	public static int PORT_IN = 0, PORT_OUT = 1;
	public static int NUM_PORTS = 2;
	
	private Hashtable counter = new Hashtable();
	private ClackComponentEvent data_changed = new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE);
	
	public SourceTracker1(Router r, String name){
		super(r, name);
		setupPorts(NUM_PORTS);
	}
	
    protected void setupPorts(int numPorts){  	
    	super.setupPorts(numPorts);
    	createInputPullPort(PORT_IN,"tracker input", VNSEthernetPacket.class);
    	createOutputPullPort(PORT_OUT, "tracker output", VNSEthernetPacket.class);
    } 
    
    public Hashtable getCounter() { return counter; }
	
	public VNSPacket handlePullRequest(int port_number){
		
			VNSEthernetPacket e_packet = (VNSEthernetPacket)m_ports[PORT_IN].pullIn();
			if(e_packet != null && e_packet.getType() == VNSEthernetPacket.TYPE_IP){
				IPPacket ippacket = new IPPacket(e_packet.getBodyBuffer());
				try {
					InetAddress src = InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array());
					Integer count = (Integer)counter.get(src.getHostAddress());
					if(count == null) 
						count = new Integer(1);
					else 
						count = new Integer(count.intValue() + 1);
					counter.put(src.getHostAddress(), count);
					fireListeners(data_changed);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			return e_packet;
	}
	
	public JPanel getPropertiesView() {
		return new SourceTrackerPopup(this);
	}
}
