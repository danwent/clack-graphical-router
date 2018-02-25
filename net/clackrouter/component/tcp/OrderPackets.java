
package net.clackrouter.component.tcp;

import java.util.ArrayList;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;



/**
 * A TCP subcomponent to put received packets in the correct sequencing order. 
 */
public class OrderPackets extends ClackComponent {

	   public static int PORT_IN    = 0;
	    public static int PORT_OUT    = 1; 
		public static int NUM_PORTS = 2;
		
		private ArrayList out_of_order;
		public TCB mTCB;
		
		public OrderPackets(TCB tcb, Router router, String name){
			super(router, name);
			mTCB = tcb;
			out_of_order = new ArrayList();
			setupPorts(NUM_PORTS);
		}
		
	    protected void setupPorts(int numports)
	    {
	    	super.setupPorts(numports);
	        String input   = "unordered packets for data processing";
	        String output = "ordered packet for data processing";

	        m_ports[PORT_IN]   = new ClackPort(this, PORT_IN,  input, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSTCPPacket.class); 
	        m_ports[PORT_OUT] = new ClackPort(this, PORT_OUT, output, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
	          
	    } // -- setupPorts
	    
	    public void acceptPacket(VNSPacket packet, int port_num){
	    	if(!(packet instanceof VNSTCPPacket)){
	    		log("received non-tcp packet");
	    	}
	    	VNSTCPPacket tcppacket = (VNSTCPPacket)packet;
	    	long incoming_seq = tcppacket.getSeqNum();
    		if(incoming_seq != mTCB.recv_nxt && mTCB.connection_state != TCB.STATE_LISTEN
    				&& mTCB.connection_state != TCB.STATE_SYN_SENT) {
    				log("Out of order! recv_next = " + mTCB.recv_nxt + " got = " + incoming_seq + "(" + tcppacket.getSeqCount() + ")");
    				out_of_order.add(packet);
    				fireListeners(mTCB.mDataChangeEvent);
    				return; // nothing to push
    		}else {
    			log("Packet correctly ordered (" + tcppacket.getSeqNum() + "(" + tcppacket.getSeqCount() + ")");
    			m_ports[PORT_OUT].pushOut(packet);
    			check_resequence_list();
    		}

	    }
	    
		private void check_resequence_list() {
			for(int i = 0; i < out_of_order.size(); i++) {
				VNSTCPPacket packet = (VNSTCPPacket) out_of_order.get(i);
				if(packet.getSeqNum() < mTCB.recv_nxt) {
					// dw: should be equals, right?
					out_of_order.remove(packet);
					mTCB.fireListeners(mTCB.mDataChangeEvent);
					log("Packet is now in correct order.  Use it!");
					m_ports[PORT_OUT].pushOut(packet);
					check_resequence_list(); // keep doing it until we find no usable packets
				}
			}
		}

}
