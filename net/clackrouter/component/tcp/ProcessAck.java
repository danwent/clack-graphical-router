
package net.clackrouter.component.tcp;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;


/**
 * TCP subcomponent that updates the TCB's notion of what packets
 * have been received by the opposite end of the connection.
 * 
 */
public class ProcessAck extends ClackComponent {
	
    public static int PORT_IN    = 0;
    public static int PORT_OUT    = 1; 
	public static int NUM_PORTS = 2;
	
	public TCB mTCB;
	
	public ProcessAck(TCB tcb, Router router, String name){
		super(router, name);
		mTCB = tcb;
		setupPorts(NUM_PORTS);
	}
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input_process   = "packets for ack processing";
        String output = "packet output";

        m_ports[PORT_IN]   = new ClackPort(this, PORT_IN,  input_process, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSTCPPacket.class);
        m_ports[PORT_OUT] = new ClackPort(this, PORT_OUT, output, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
       
      
    } // -- setupPorts
    
    public void acceptPacket(VNSPacket packet, int port_num){
    	if(port_num == PORT_IN){
        	if(!(packet instanceof VNSTCPPacket )) {
        		System.err.println("ProcessAck received non-TCP Packet");
        		return;
        	}

        	VNSTCPPacket tcppacket = (VNSTCPPacket) packet;
    		
    		if(mTCB.connection_state == TCB.STATE_SYN_SENT){
    			process_ack_syn_sent(tcppacket);
    		}else if(mTCB.connection_state == TCB.STATE_SYN_RECEIVED){
    			process_ack_syn_received(tcppacket);
    		}else {
    			process_ack_main(tcppacket);
    		}
    		mTCB.fireListeners(mTCB.mDataChangeEvent);
    		m_ports[PORT_OUT].pushOut(packet);
    	}
    	
    }
    
	private void process_ack_main(VNSTCPPacket packet) {
		if(!isValidAck(packet))return;
		mTCB.send_window = packet.getRecvWindowSize();
		mTCB.send_una = packet.getAckNum();

		// end-cases

		if(mTCB.connection_state == TCB.STATE_FIN_WAIT1
			&& mTCB.send_una > mTCB.fin_seq_sent){
			log("Moving from FIN_WAIT1 to FIN_WAIT2");
			mTCB.connection_state = TCB.STATE_FIN_WAIT2;
		}

		if( (mTCB.connection_state == TCB.STATE_CLOSING ||		
				mTCB.connection_state == TCB.STATE_LAST_ACK) &&
			(mTCB.send_una > mTCB.fin_seq_sent)) {
			mTCB.done = true;
			log("Fin acked.. TCP Session Finished");
			mTCB.connection_state = TCB.STATE_CLOSED;
		}
	}
	
	private void process_ack_syn_sent(VNSTCPPacket packet){
		//process ack
		if(packet.ackFlagSet()) {
			mTCB.send_window = packet.getRecvWindowSize();
			mTCB.send_una = packet.getAckNum();
		}
		// we move to ESTABLISHED in ProcessSegment
	}
	
	private void process_ack_syn_received(VNSTCPPacket packet){

		mTCB.send_una = packet.getAckNum();
		mTCB.send_window = packet.getRecvWindowSize();
		log("Moving from SYN_RECEIVED to ESTABLISHED");
		mTCB.connection_state = TCB.STATE_ESTABLISHED;	
}
	
	
	private boolean isValidAck(VNSTCPPacket packet) {
		boolean is_ack = packet.ackFlagSet();
		boolean high_enough = mTCB.send_una <= packet.getAckNum();
		boolean low_enough = packet.getAckNum() <= mTCB.send_nxt;
		return (is_ack && high_enough && low_enough); 
	}	
}
