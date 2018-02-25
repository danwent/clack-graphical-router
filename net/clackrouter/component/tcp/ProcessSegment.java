
package net.clackrouter.component.tcp;


import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;



/**
 * TCP subcomponent that processes data and send acknowledgements.  
 * 
 * <p> Also processes the FIN flag and notifies the socket when all data has been
 * received. </p>
 */
public class ProcessSegment extends ClackComponent {

	public static final int PORT_ACK_OUT = 0;
	public static final int PORT_NEWDATA_IN = 1;
	public static final int PORT_APP_OUT = 2;
	public static final int NUM_PORTS = 3;
	
	private TCB mTCB;

	
	public ProcessSegment(TCB t, Router router, String name){
		super(router, name);
		mTCB = t;
		setupPorts(NUM_PORTS);
	}

	
    public void reset(TCB t){
    	mTCB = t;
    }
   
	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
        String ack_out_desc = "Outgoing Ack packets";
        String newdata_in_desc = "Packets potentially containing new data";
        String app_out_desc = "Data destined to the application";
        m_ports[PORT_ACK_OUT] = new ClackPort(this, PORT_ACK_OUT, ack_out_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
        m_ports[PORT_NEWDATA_IN] =  new ClackPort(this, PORT_NEWDATA_IN,  newdata_in_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSTCPPacket.class);
        m_ports[PORT_APP_OUT] = new ClackPort(this, PORT_APP_OUT, app_out_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
       
    } // -- setupPorts
	
	
	public void acceptPacket(VNSPacket packet, int port_num){
    	
    	VNSTCPPacket tcppacket = (VNSTCPPacket) packet;
    	
    	if(port_num == PORT_NEWDATA_IN){

    		if(mTCB.connection_state == TCB.STATE_SYN_SENT){
    			recv_syn_sent(tcppacket);

    		} else if(mTCB.connection_state == TCB.STATE_SYN_RECEIVED){
    			recv_syn_received(tcppacket);

    		} else {
    		
    			int seq_size = tcppacket.getSeqCount();
    			long seq = tcppacket.getSeqNum();
    			log("Processing segment: " + (seq % 10000) + " to " + ((seq + seq_size) % 10000));
    			if(seq_size > 0) {
    				process_data_main(tcppacket);	
    				process_fin_main(tcppacket);
    			}
    		}
    		
    		// always send ack
    		mTCB.fireListeners(mTCB.mDataChangeEvent);
    		m_ports[PORT_ACK_OUT].pushOut(getAckPacket());
    	}  	
	}

	private void recv_syn_received(VNSTCPPacket packet) {
		
		if(packet.finFlagSet()){
			//TODO tell app fin was received
			log("Moving from SYN_RECEIVED to CLOSE_WAIT");
			mTCB.connection_state = TCB.STATE_CLOSE_WAIT;
			mTCB.recv_nxt++;
		}
	}
	
	private void recv_syn_sent(VNSTCPPacket packet){
		if(packet.synFlagSet()){
			mTCB.recv_nxt = packet.getSeqNum() + 1;
			log("Moving from SYN_SENT To ESTABLISHED");
			mTCB.connection_state = TCB.STATE_ESTABLISHED;	
		}
		
		
	}
	
	private VNSTCPPacket getAckPacket() {
		VNSTCPPacket packet =  new VNSTCPPacket(mTCB.local_port,mTCB.remote_port ,mTCB.send_nxt, mTCB.recv_nxt,
				VNSTCPPacket.TH_ACK, 5000,new byte[] {});
		packet.setDestIPAddress(mTCB.remote_address);
		packet.setSourceIPAddress(mTCB.local_address);
		return packet;
	}
	
	
	private void process_data_main(VNSTCPPacket packet) {

    	if(!(packet instanceof VNSTCPPacket)){
    		log("received non-tcp packet");
    	}
    	VNSTCPPacket tcppacket = (VNSTCPPacket)packet;	

		int data_size = tcppacket.getDataSize();
		// send data to socket buffer
		log("Passing " + data_size + " bytes to the application");
		m_ports[PORT_APP_OUT].pushOut(new VNSPacket(tcppacket.getBodyBuffer()));
		
		mTCB.recv_nxt += data_size;
	}
	
	private void process_fin_main(VNSTCPPacket packet) {
		if(!packet.finFlagSet()) return;

		log("Fin received, notifying application \n");
		mTCB.finReceived = true;

		mTCB.recv_nxt++;
		log("Preparing to Ack FIN with ACK = " + mTCB.recv_nxt);

		if(mTCB.connection_state == TCB.STATE_ESTABLISHED){
			log("Moving from ESTABLISHED to CLOSE_WAIT");
			mTCB.connection_state = TCB.STATE_CLOSE_WAIT;
			mTCB.mCloseWaitStart = getTime();
		} else if(mTCB.connection_state == TCB.STATE_FIN_WAIT1) {
			if(packet.ackFlagSet() && mTCB.send_una > mTCB.fin_seq_sent) {
				log("FinWait1 to close.  TCP session finished\n");
				mTCB.connection_state = TCB.STATE_CLOSED;
				mTCB.done = true;
			} else {
				log("Moving from FIN_WAIT1 to CLOSING");
				mTCB.connection_state = TCB.STATE_CLOSING;
			}
		} else if(mTCB.connection_state == TCB.STATE_FIN_WAIT2) {
			log("FinWait2 to close.  TCP Session Finished");
			mTCB.connection_state = TCB.STATE_CLOSED;
			mTCB.done = true;
		}		
	}
	
	
	


}
