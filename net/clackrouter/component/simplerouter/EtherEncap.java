
package net.clackrouter.component.simplerouter;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;


/**
 * Encapsulates data in an Ethernet frame
 */
public class EtherEncap extends ClackComponent {

    public static int PORT_IN = 0; 
    public static int OUTSTART    = 1;
    public int mNumPorts  = 0; // -- set dynamically
    
    // Map outgoing port number to interface name
    private Hashtable mPortsToInterface = null;

    public EtherEncap(Router router, String name)
    {
    	super(router, name);
    	
        mPortsToInterface = new Hashtable();

        InterfaceIn[] interfaces = mRouter.getInputInterfaces();
        mNumPorts = interfaces.length + 1;
        setupPorts(mNumPorts);

    }
    
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input_desc = "Packet to be encapsulated in Ethernet";
        String out_desc    = "Ethernet packets exiting interface ";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, input_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        
        // -- add a port per interface and
        // -- create mapping from interface names to ports
        InterfaceIn[] interfaces = mRouter.getInputInterfaces();
        for(int i = 0 ; i < interfaces.length; ++i)
        {
            String iname = interfaces[i].getDeviceName();
            mPortsToInterface.put(iname, new Integer(i + OUTSTART));
            m_ports[OUTSTART + i] = new ClackPort(this, OUTSTART + i, out_desc+iname, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSEthernetPacket.class);
        }	
    } // -- setupPorts

    /**
     * Encapsulate a higher-level packet in Ethernet.  
     * 
     * <p>Sets ethernet header using
     * the data specified by packet.getNextHopMacAddress(), packet.getOutputInterfaceName(),
     * and packet.getLevel2Type(). </p>
     */
    public void acceptPacket(VNSPacket packet, int port_number)   {

        String outgoingIface = packet.getOutputInterfaceName();
        Interface ifaceout = mRouter.getInterfaceByName(outgoingIface);
        
        Integer outport = (Integer)mPortsToInterface.get(outgoingIface);
      

        try {
            VNSEthernetPacket e_packet = 
                VNSEthernetPacket.wrap(packet, packet.getNextHopMacAddress(), 
                		ByteBuffer.wrap(ifaceout.getMACAddress().getAddress()), packet.getLevel2Type());
            m_ports[outport.intValue()].pushOut(e_packet);

        } catch (Exception e){
        	e.printStackTrace();
        }

    } // -- acceptPacket
    
    public Color getColor() { return new Color(139,105,20); }  // strip/encap group color
}
