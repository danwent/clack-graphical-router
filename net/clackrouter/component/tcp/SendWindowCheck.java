
package net.clackrouter.component.tcp;

import java.nio.ByteBuffer;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;


/**
 * Class that monitors the sending of TCP data segments, and makes sure it is within
 * the valid sending window.  
 * 
 * <p> Since congestion control is not currently implemented, this window is a static 
 * size. </p>  
 */
public class SendWindowCheck extends ClackComponent {
	
	public static final int PORT_NET_OUT = 0;
	public static final int PORT_APP_IN = 1;
	public static final int NUM_PORTS = 2;
	
	private TCB mTCB;

	private boolean app_close_requested = false;
	
	public SendWindowCheck(TCB t, Router router, String name){
		super(router, name);
		router.registerForPoll(this);
		mTCB = t;
		setupPorts(NUM_PORTS);
	}
	
	
	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);

        String net_out_desc = "Outgoing TCP segments";
        String app_in_desc = "Data input from connected socket";
  
        m_ports[PORT_NET_OUT] =  new ClackPort(this, PORT_NET_OUT, net_out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSTCPPacket.class);
        m_ports[PORT_APP_IN] =  new ClackPort(this, PORT_APP_IN, app_in_desc,  ClackPort.METHOD_PULL, ClackPort.DIR_IN, VNSPacket.class);
    } // -- setupPorts
    
	/**
	 * Checks to see if the component can send any data right now, and if data exists and the 
	 * window and TCP state allows, this data is sent.  
	 */
	public void poll() {
		
		if(mTCB == null) return;
			
		if(!(mTCB.connection_state == TCB.STATE_ESTABLISHED || mTCB.connection_state == TCB.STATE_CLOSE_WAIT)){
			return; // can't send data in any other state
		}
		
		if(app_close_requested && mTCB.send_nxt == mTCB.send_una/*retrans.size() == 0*/){
			log("All data acked, sending FIN");
			send_segment(ByteBuffer.allocate(0), VNSTCPPacket.TH_FIN | VNSTCPPacket.TH_ACK);
		
			if(mTCB.connection_state == TCB.STATE_ESTABLISHED) 
				mTCB.connection_state = TCB.STATE_FIN_WAIT1;
			else if(mTCB.connection_state == TCB.STATE_CLOSE_WAIT)
				mTCB.connection_state = TCB.STATE_LAST_ACK;
			mTCB.fireListeners(mTCB.mDataChangeEvent);
		}
	
		int total_window = (mTCB.send_window < TCB.MAX_WINDOW)? mTCB.send_window : TCB.MAX_WINDOW;
		long max_seq_sendable = mTCB.send_una + total_window;
		int data_to_send = (int)(max_seq_sendable - mTCB.send_nxt);
		
		if(data_to_send <= 0) return;
		
		int max_payload_size = /*min*/(data_to_send < TCB.MAX_PAYLOAD) ? data_to_send : TCB.MAX_PAYLOAD;
		log("looking to send " + max_payload_size + " bytes of data");
		mTCB.mSendBuffer.setMaxBytesForPull(max_payload_size);
		
		VNSPacket packet = m_ports[PORT_APP_IN].pullIn();
		if(packet == null) {return;}
		
		send_segment(packet.getByteBuffer(), VNSTCPPacket.TH_ACK);
		log("Sending: " + packet.getByteBuffer().array().length + " bytes");

	}
    /**
     * Creates and sends a packet with no data
     * @param flags header flags for this packet
     */
	public void send_empty_packet(int flags){
		send_segment(ByteBuffer.allocate(0), flags);
	}
	

	private void send_segment(ByteBuffer data, int flags){
		if((flags & VNSTCPPacket.TH_FIN) != 0){
			mTCB.fin_seq_sent = mTCB.send_nxt;
		}

		VNSTCPPacket tcppacket = new VNSTCPPacket(mTCB.local_port,mTCB.remote_port ,mTCB.send_nxt, mTCB.recv_nxt,
					flags, TCB.MAX_WINDOW, data.array());
		tcppacket.setDestIPAddress(mTCB.remote_address);
		tcppacket.setSourceIPAddress(mTCB.local_address);

		int seq_increase = data.capacity();
		if((flags & VNSTCPPacket.TH_FIN)!= 0) seq_increase++;	
		else if((flags & VNSTCPPacket.TH_SYN) != 0) seq_increase++;
		mTCB.send_nxt += seq_increase;

		m_ports[PORT_NET_OUT].pushOut(tcppacket);
	}
	
	/** 
	 * Used to notify the TCP stack that the application has called close on the 
	 * socket.  
	 */
	public void app_close_request() {
		log("Application requests a close");
		app_close_requested = true;
	}
}
