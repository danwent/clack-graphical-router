
package net.clackrouter.component.extension;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSICMPPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.router.core.Router;

/**
 * A simple class to take a packet on a single input and duplicate it and send
 * the packet out two outputs.  
 * 
 * <p> This class currently only works for packets of known types, including: </p>
 * <ul> 
 * <li> VNSEthernetPacket
 * <li> VNSARPPacket
 * <li> VNSIPPacket
 * <li> VNSICMPPacket
 * <li> VNSTCPPacket
 * <li> VNSUDPPacket
 * </ul>
 */
public class Tee extends ClackComponent {
	public static int PORT_IN = 0;
	public static int PORT_OUT1 = 1;
	public static int PORT_OUT2 = 2;
	public static int NUM_PORTS = 3;
	
	public Tee(Router router, String name){
		super(router, name); 
		setupPorts(NUM_PORTS);
	}
    
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String in_desc = "Tee input";
        String out1_desc = "Tee output #1";
        String out2_desc = "Tee output #2";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT1] =  new ClackPort(this, PORT_OUT1,  out1_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
        m_ports[PORT_OUT2] =  new ClackPort(this, PORT_OUT2,  out2_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; } 
    
    public void acceptPacket(VNSPacket packet, int port_number)
    {
        
        if ( port_number == PORT_IN ){
        	VNSPacket duplicate = null;
        	/* urgh this is ugly.. maybe packets should be able to duplicate themselves?
        	 * or should I just use reflection???
        	 *  */
        	if(packet instanceof VNSEthernetPacket)
        		duplicate = new VNSEthernetPacket(packet.getByteBuffer());
        	else if(packet instanceof VNSARPPacket)
        		duplicate = new VNSARPPacket(packet.getByteBuffer());
        	else if(packet instanceof IPPacket)
        		duplicate = new IPPacket(packet.getByteBuffer());
        	else if(packet instanceof VNSICMPPacket)
        		duplicate = new VNSICMPPacket(packet.getByteBuffer());   
        	else if(packet instanceof VNSTCPPacket)
        		duplicate = new VNSTCPPacket(packet.getByteBuffer());
        	else if(packet instanceof VNSUDPPacket)
        		duplicate = new VNSUDPPacket(packet.getByteBuffer());
        	else {
        		error("Tee can't duplicate packet of type " + packet.getClass().getName());
        		return;
        	}
        	
        	m_ports[PORT_OUT1].pushOut(packet); 
        	m_ports[PORT_OUT2].pushOut(duplicate);
        } else
        { error( "Received packet on invalid port: " + port_number); }

    } // -- acceptPacket
	

}
