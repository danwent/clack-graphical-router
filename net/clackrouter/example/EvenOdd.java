
package net.clackrouter.example;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;

/**
 * A class that examines IP packets and outputs the packet on one of two ports depending
 * on whether the last byte of its IP address is even or odd.   
 */
public class EvenOdd extends ClackComponent {

	public static int PORT_IN = 0, PORT_ODD_OUT = 1, PORT_EVEN_OUT = 2;
	public static int NUM_PORTS = 3;
	
	public EvenOdd(Router r, String name){
		super(r, name);
		setupPorts(NUM_PORTS);
	}
	
    protected void setupPorts(int numPorts){  	
    	super.setupPorts(numPorts);
    	createInputPushPort(PORT_IN,"input", IPPacket.class);
    	createOutputPushPort(PORT_ODD_OUT, "odd packet output", IPPacket.class);
    	createOutputPushPort(PORT_EVEN_OUT, "even packet output", IPPacket.class);
    } 
	
	public void acceptPacket(VNSPacket packet, int port_number){
		if(port_number == PORT_IN){
			IPPacket ippacket = (IPPacket)packet;
			byte[] dst_bytes = ippacket.getHeader().getDestinationAddress().array();
			if((dst_bytes[3] & 0x1) == 0){
				log("Even!");
				m_ports[PORT_EVEN_OUT].pushOut(packet);
			}else {
				log("Odd!");
				m_ports[PORT_ODD_OUT].pushOut(packet);
			}
		}else {
			/* impossible */
		}
	}
	
}
