
package net.clackrouter.component.tcp;

import java.util.ArrayList;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;



/**
 * TCP subcomponent that handles timeouts and the retransmission of 
 * unacknowledged packets.  
 */
public class Retransmitter extends ClackComponent {
	
    public static int PORT_PROCESS_IN    = 0;
    public static int PORT_STORE_IN = 1;
    public static int PORT_NET_OUT    = 2; 
    public static int PORT_PROCESS_OUT = 3;
	public static int NUM_PORTS = 4;
	
	static final int RTO_MSECS = 2000;
	
	private long nxt_timeout; // time in millisec that next segment on unacked list will timeout
	public ArrayList mRetrans; // holds RetransListEntries of unacked data
	public TCB mTCB;
	
	public Retransmitter(TCB tcb, Router router, String name){
		super(router, name);
		mRouter.registerForPoll(this);
		mTCB = tcb;
		mRetrans = new ArrayList();
		setupPorts(NUM_PORTS);
	}
	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String input_process   = "packets for ack processing";
        String input_store = "packets to be stored for retransmission";
        String output_process = "outgoing packet needing data processing";
        String output_net = "packets being sent out on the network";

        m_ports[PORT_PROCESS_IN]   = new ClackPort(this, PORT_PROCESS_IN,  input_process, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSTCPPacket.class);
        m_ports[PORT_STORE_IN]   = new ClackPort(this, PORT_STORE_IN,  input_store, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSTCPPacket.class);
        m_ports[PORT_NET_OUT] = new ClackPort(this, PORT_NET_OUT, output_net, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
        m_ports[PORT_PROCESS_OUT] = new ClackPort(this, PORT_PROCESS_OUT, output_process, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
      
    } // -- setupPorts
    
    /**
     * Depending on the input port, either removes acknowledged data from its retransmission list,
     * or stores a packet in the retrans list.  
     */
    public void acceptPacket(VNSPacket packet, int port_num){

		VNSTCPPacket tcppacket = (VNSTCPPacket)packet;
		
    	if(port_num == PORT_PROCESS_IN){
    		remove_acked_segments();
    		int segment_size = tcppacket.getSeqCount();
    		if(segment_size > 0)
    			m_ports[PORT_PROCESS_OUT].pushOut(packet);
    	}else if(port_num == PORT_STORE_IN){

    		long seq_data_end = tcppacket.getSeqNum() + tcppacket.getSeqCount() - 1;
    		if(seq_data_end < mTCB.send_una){
    			log("No need for retrans, bytes through " + seq_data_end + " have already been acked");
    			return;
    		}
    		int seq_increase = tcppacket.getDataSize();
    		if(tcppacket.synFlagSet()) seq_increase++;	
    		if(tcppacket.finFlagSet()) seq_increase++;
    		if(!(seq_increase > 0)) {
    			error("Packet sent to Retransmitter has seq_increase of " + seq_increase);
    			return;
    		}
    		log("adding packet with seq = " + tcppacket.getSeqNum() + " and ack = " + tcppacket.getAckNum()
    				+ " to retrans with send_una = " + mTCB.send_una);
    		RetransListEntry entry = new RetransListEntry((VNSTCPPacket)packet);
    		mRetrans.add(entry); // should append
    		set_nxt_timeout();	
    		mTCB.fireListeners(mTCB.mDataChangeEvent);
    		m_ports[PORT_NET_OUT].pushOut(packet);
    	}
    	
    }
    
    /** 
     * Checks to see if any timeouts have occured, in which case it
     * retransmits packets.  
     */
	public void poll() {
		
		if(mRetrans.size() > 0 && nxt_timeout < getTime())
			attempt_retransmit();
		
	}
    
	private void attempt_retransmit() {
		log("Retransmitting with List of size: " + mRetrans.size());
		long current_time = getTime();
		
		for(int i = 0; i < mRetrans.size(); i++) {
			RetransListEntry entry = (RetransListEntry) mRetrans.get(i);
			if(entry.abs_timeout > current_time)
				continue;
			
			log("Retrans time out at " + current_time % 1000000 + " for packet timing out at " + entry.abs_timeout % 1000000);
			long seq_start = entry.packet.seq_num;
			log("Retrans packet has seq_start = " + seq_start + " and seq_end = " + (seq_start + entry.packet.getSeqCount() - 1));
			if(entry.numRetrans >= 5) {
				mTCB.done = true;
				log("*** Max Retransmissions Reached ***");
				mRetrans.remove(entry);
				mTCB.fireListeners(mTCB.mDataChangeEvent);
				return;
			}
			log("Retrans packet.  seq = " + entry.packet.seq_num + " data size = " + entry.packet.getDataSize());
			entry.packet.ack_num = mTCB.recv_nxt;
			
			log("retransmitting packet (" + entry.packet.seq_num + ") of size " + entry.packet.getDataSize() + "\n with data: " + new String(entry.packet.getBodyBuffer().array()));
			// retrans packet!
			m_ports[PORT_NET_OUT].pushOut(entry.packet);
			mTCB.fireListeners(mTCB.mDataChangeEvent);
			entry.numRetrans++;
			entry.abs_timeout = getTime() + RTO_MSECS;
			
		}
		set_nxt_timeout();
		//remove_acked_segments(); // needed?
	
	}
	
	private void set_nxt_timeout() {
		nxt_timeout = 0;	
		for(int i = 0; i < mRetrans.size(); i++) {
			RetransListEntry entry = (RetransListEntry) mRetrans.get(i);
			if(entry.abs_timeout > nxt_timeout) 
				nxt_timeout = entry.abs_timeout;
		}
	}
	
	private void remove_acked_segments() {
		log("Removing all retrans data through " + mTCB.send_una);
		for(int i = 0; i < mRetrans.size(); i++) {

			RetransListEntry entry = (RetransListEntry) mRetrans.get(i);
			VNSTCPPacket packet = entry.packet;
			long highest_seq = packet.getSeqNum() +
							packet.getSeqCount() - 1;
			if(highest_seq < mTCB.send_una) {
				mRetrans.remove(entry);	
				mTCB.fireListeners(mTCB.mDataChangeEvent);
				log("Removing ACKed packet.  seq = " + entry.packet.seq_num + " data size = " + entry.packet.getDataSize());
			}else{
			     	break;
			}
		}
		set_nxt_timeout();
		
	}
	
	
    public class RetransListEntry {
    	public VNSTCPPacket packet;
    	public int numRetrans;
    	public long abs_timeout;
    	public RetransListEntry(VNSTCPPacket p){
    		numRetrans = 0;
    		packet = p;
    		abs_timeout = getTime() + RTO_MSECS;
    	}
    }
	
    
}
