
package net.clackrouter.component.simplerouter;


import javax.swing.JPanel;

import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.InterfacePopup;
import net.clackrouter.protocol.data.VNSHWInfo;
import net.clackrouter.router.core.Router;
import net.clackrouter.topology.core.TopologyModel;


/**
 * Represents an output Interface, a ToDevice(ethX) component, in a ClackRouter
 */
public class InterfaceOut extends Interface {
	public static int PORT_FROM_EXTERNAL = 0;
	
	protected static int NUM_PORTS = 1;

	
	public InterfaceOut(String name, Router router, VNSHWInfo.InterfaceEntry entry){
		super(name, router, entry);
        setupPorts(NUM_PORTS);
        router.registerForPoll(this);
	}
	
	public InterfaceOut(String name, Router router, TopologyModel.Interface entry){
		super(name, router, entry);
		setupPorts(NUM_PORTS);
		router.registerForPoll(this);
	}

    protected void setupPorts(int numports) {
    	super.setupPorts(numports);
        String external_out_desc = "Port sending all packets to the external wire";
        m_ports[PORT_FROM_EXTERNAL] = new ClackPort(this, PORT_FROM_EXTERNAL, external_out_desc, ClackPort.METHOD_PULL, ClackPort.DIR_IN, VNSEthernetPacket.class);
    }
    
	public JPanel getPropertiesView() {
		return new InterfacePopup(this, InterfacePopup.INTERFACE_OUT);
	}
    
	/**
	 * When polled, this component takes any packet received and hands it off to the router to be 
	 * sent within the network.  
	 */
	public void poll() 
    {
		VNSPacket packet = m_ports[PORT_FROM_EXTERNAL].pullIn();
		if(packet == null) return;

        VNSEthernetPacket e_packet = (VNSEthernetPacket) packet;
     
        e_packet.setOutputInterfaceName(this.getDeviceName());
        mRouter.sendOutgoingPacket(e_packet);
	}
    

    
}

