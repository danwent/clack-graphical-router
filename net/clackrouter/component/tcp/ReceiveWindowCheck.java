
package net.clackrouter.component.tcp;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;



/**
 * TCP subcomponent that enforces the receive window for a TCP client, either
 * trimming packets to fit within the valid window, or entirely dropping those
 * that are not within the window.  
 * 
 * <p> Also enforces that the receiver is in a valid state to receive data </p>.
 */
public class ReceiveWindowCheck extends ClackComponent {
	   public static int PORT_IN    = 0;
	    public static int PORT_OUT    = 1; 
		public static int NUM_PORTS = 2;
		
		public TCB mTCB;
		
		public ReceiveWindowCheck(TCB tcb, Router router, String name){
			super(router, name);
			mTCB = tcb;
			setupPorts(NUM_PORTS);
		}
		
	    protected void setupPorts(int numports)
	    {
	    	super.setupPorts(numports);
	        String input   = "untrimmed packets for data processing";
	        String output = "trimmed packet for data processing";

	        m_ports[PORT_IN]   = new ClackPort(this, PORT_IN,  input, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSTCPPacket.class); 
	        m_ports[PORT_OUT] = new ClackPort(this, PORT_OUT, output, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
	          
	    } // -- setupPorts
	    
	    public void acceptPacket(VNSPacket packet, int port_num){
	    	if(port_num != PORT_IN){
	    		log("invalid input port");
	    		return;
	    	}
	    	
	    	if(!(packet instanceof VNSTCPPacket)){
	    		log("received non-tcp packet");
	    		return;
	    	}
	    	VNSTCPPacket tcppacket = (VNSTCPPacket)packet;

			boolean can_receive_data_or_syn = (
					mTCB.connection_state == TCB.STATE_ESTABLISHED
					|| mTCB.connection_state == TCB.STATE_FIN_WAIT1
					|| mTCB.connection_state == TCB.STATE_FIN_WAIT2
					|| mTCB.connection_state == TCB.STATE_SYN_SENT);

			
			log("Received Packet!");
			log("recv_nxt = " + mTCB.recv_nxt + " recv_win = " + TCB.MAX_WINDOW);
			log("segment seq = " + tcppacket.getSeqNum() + "(" + tcppacket.getSeqCount() + ")");
			
			long segment_data_end = tcppacket.getSeqNum() + tcppacket.getSeqCount() - 1;
			if(mTCB.recv_nxt == -1){
				m_ports[PORT_OUT].pushOut(tcppacket);
			}else if(segment_data_end < mTCB.recv_nxt) {
				log("duplicate for seq = " + tcppacket.getSeqNum());
				return;
			}else if(tcppacket.getSeqNum() > mTCB.recv_nxt + TCB.MAX_WINDOW){
				log("packet not yet in window = " + tcppacket.getSeqNum());
				return;
			}else if(!can_receive_data_or_syn){ 
				log("cannot receive packets in state " + TCB.getStateString(mTCB.connection_state));
			}else {
				trim_packet_to_ideal(tcppacket);
				m_ports[PORT_OUT].pushOut(tcppacket);
			}
	    }
	    
		private void trim_packet_to_ideal(VNSTCPPacket packet) {
			if(mTCB.recv_nxt == -1) return;
			
			long seq_data_start = packet.getSeqNum();
			long seq_data_end = seq_data_start + packet.getSeqCount() - 1;
			
			long rcv_win_start = mTCB.recv_nxt;
			long rcv_win_end = mTCB.recv_nxt + TCB.MAX_WINDOW;

			if(seq_data_start < rcv_win_start) {
				int extra_data = (int)(rcv_win_start - seq_data_start);
				packet.trimDataFront(extra_data);
				packet.seq_num = seq_data_start + extra_data;
				log("Removing " + extra_data + " bytes of data from front of packet");
			}

			seq_data_end = packet.seq_num + packet.getDataSize() - 1;

			if(seq_data_end > rcv_win_end) {
				packet.trimDataEnd((int)(seq_data_end - rcv_win_end));
				log("removing " + (int)(seq_data_end - rcv_win_end) + " bytes from the end of packet");
			}
		}
}
