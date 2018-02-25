
package net.clackrouter.component.tcp;

import java.net.InetAddress;

import net.clackrouter.application.TCPSocket;
import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.router.core.Router;


/**
 * A Transmission Control Block (TCB) represents the state of a single
 * TCP connection.  
 * 
 * <p> One TCB exists per (src address, source port, 
 * destination address, destination port) tuple.  The relationship between
 * TCBs and sockets is one-to-one, though it is a bit confusing in the case of
 * server sockets, since a TCB exists despite the fact that no data is actually
 * ever sent over a server socket.  </p>  
 */
public class TCB extends ClackComponent {
	
	// TCP Connection States
	public static final int STATE_CLOSED = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_SYN_SENT = 2;
	public static final int STATE_SYN_RECEIVED = 3;
	public static final int STATE_ESTABLISHED = 4;
	public static final int STATE_FIN_WAIT1 = 5;
	public static final int STATE_FIN_WAIT2 = 6;
	public static final int STATE_CLOSE_WAIT = 7;
	public static final int STATE_CLOSING = 8;
	public static final int STATE_LAST_ACK = 9;
	public static final int STATE_TIME_WAIT = 10;
	public static final int STATE_UNKNOWN = 11;
	
	
	// static round-trip timeout for now.  
	// A more elegant solution is to estimate the RTT
	public static final int RTO_MSECS = 2000;
	// Max Sending and Receiving Window 
	public static final int MAX_WINDOW  = 3072;
	// Max Per-Packet Payload
	public static final int MAX_PAYLOAD = 536;
	// Number of retransmission attempts before a connection
	// is assumed to be down.  
	public static final int TOTAL_RETRANS_ATTEMPTS  = 5;
	
	private int poll_counter = 0;
	
	// ivars for a TCB
	
	public int connection_state; // current connection state
	public long send_nxt; // seq # of next byte to send
	public long recv_nxt; // seq # of next byte to receive
	public long send_una; // seq # of lowest unacked byte
	
	public int send_window;  // window from remote peer
	public long fin_seq_sent; // seq# of fin we send
	public long initial_sequence_num; // our ISN
	
	public boolean finReceived = false;
	public long rto_msec; // retransmission timeout in msecs
	public boolean done;  // flag that we no longer need this block	
	public long mCloseWaitStart; // time we entered into close_wait
	public static final long CLOSE_WAIT_MILLIS = 20000;
	
	public int local_port = 0, remote_port = 0;
	public InetAddress local_address = null, remote_address = null;
	
	public TCP mParent;
	
	public static int PORT_FROM_NET = 0;
	public static int PORT_TO_NET = 1; 
	public static int PORT_INTERNAL_FROM_NET = 2;//TODO: should not be globally visible
	public static int PORT_INTERNAL_TO_NET = 3;//TODO: should not be globally visible
	public static int NUM_PORTS = 4;
	
	public TCPSocket mSocket; // socket associated with this TCB
	
	// TCP subcomponents holding the specific state of this TCB
	public OrderPackets mOrderPackets;
	public ProcessAck mProcessAck;
	public ProcessSegment mProcessSegment;
	public ReceiveWindowCheck mRecvWinCheck;
	public Retransmitter mRetransmitter;
	public SendWindowCheck mSendWinCheck;
	public SetChecksum mSetChecksum;
	public ValidateChecksum mValidateChecksum;
	public SockBuffer mSendBuffer, mRecvBuffer;
	
	public ClackComponentEvent mDataChangeEvent;
	
	public TCB(TCP parent, Router router, String name){
		super(router, name);
		generate_initial_seq_num();
		recv_nxt = -1; // set up after we get syn or syn-ack
		send_window = 0; // same
		mParent = parent;
		setupPorts(NUM_PORTS);
		mDataChangeEvent = new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE);
		mSocket = new TCPSocket(this, router, name + ":Socket");
		mOrderPackets = new OrderPackets(this, router, name + ":OrderPackets");
	    mProcessAck = new ProcessAck(this, router, name + ":ProcessAck");
		mProcessSegment = new ProcessSegment(this, router, name + ":ProcessSegment");
		mRecvWinCheck = new ReceiveWindowCheck(this, router, name + ":ReceiveWindowCheck");
		mRetransmitter = new Retransmitter(this, router, name + ":Retransmitter");
		mSendWinCheck = new SendWindowCheck(this, router, name + ":SendWindowCheck");
		mSetChecksum = new SetChecksum(this, router, name + ":SetChecksum");
		mValidateChecksum = new ValidateChecksum(this, router, name + ":ValidateChecksum");
		mSendBuffer = new SockBuffer(this, router, name + ":SendBuffer");
		mRecvBuffer = new SockBuffer(this, router, name + ":ReceiveBuffer");
		connectComponents();
		connect_no_gui();
	}

	
	/**
	 * Method to connect all TCP subcomponents for this TCB.  
	 * 
	 * <p> These components are usually connected in HierarchicalComponent, but that is only if we are using the GUI.
	 * For testing, we may want to connect them using this method. </p>
	 */
	public void connect_no_gui(){

		ClackPort.connectPorts(mValidateChecksum.getPort(ValidateChecksum.PORT_TCP_VALID),mProcessAck.getPort(ProcessAck.PORT_IN));
		ClackPort.connectPorts(mProcessAck.getPort(ProcessAck.PORT_OUT), mRetransmitter.getPort(Retransmitter.PORT_PROCESS_IN));
		ClackPort.connectPorts(mRetransmitter.getPort(Retransmitter.PORT_PROCESS_OUT), mRecvWinCheck.getPort(ReceiveWindowCheck.PORT_IN));
		ClackPort.connectPorts(mRecvWinCheck.getPort(ReceiveWindowCheck.PORT_OUT), mOrderPackets.getPort(OrderPackets.PORT_IN));
		ClackPort.connectPorts(mOrderPackets.getPort(OrderPackets.PORT_OUT), mProcessSegment.getPort(ProcessSegment.PORT_NEWDATA_IN));
		ClackPort.connectPorts(mProcessSegment.getPort(ProcessSegment.PORT_APP_OUT), mRecvBuffer.getPort(SockBuffer.PORT_TAIL));
		ClackPort.connectPorts(mRecvBuffer.getPort(SockBuffer.PORT_HEAD), mSocket.getPort(TCPSocket.PORT_NET_IN));
		ClackPort.connectPorts(mProcessSegment.getPort(ProcessSegment.PORT_ACK_OUT), mSetChecksum.getPort(SetChecksum.PORT_TCP_IN));
		ClackPort.connectPorts(mRetransmitter.getPort(Retransmitter.PORT_NET_OUT), mSetChecksum.getPort(SetChecksum.PORT_TCP_IN));		
		ClackPort.connectPorts(mSocket.getPort(TCPSocket.PORT_NET_OUT), mSendBuffer.getPort(SockBuffer.PORT_TAIL));
		ClackPort.connectPorts(mSendBuffer.getPort(SockBuffer.PORT_HEAD), mSendWinCheck.getPort(SendWindowCheck.PORT_APP_IN));
		ClackPort.connectPorts(mSendWinCheck.getPort(SendWindowCheck.PORT_NET_OUT), mRetransmitter.getPort(Retransmitter.PORT_STORE_IN));	

	}
	
	
	private void connectComponents() {
		// connect TCB ports to internal components
		ClackPort.connectPorts(this.getPort(TCB.PORT_INTERNAL_FROM_NET), mValidateChecksum.getPort(ValidateChecksum.PORT_IP_IN));
		ClackPort.connectPorts(mSetChecksum.getPort(SetChecksum.PORT_IP_OUT),this.getPort(TCB.PORT_INTERNAL_TO_NET));
	}
	
	public void acceptPacket(VNSPacket packet, int port_num){
		
		if(port_num == PORT_FROM_NET) m_ports[PORT_INTERNAL_FROM_NET].pushOut(packet);
		if(port_num == PORT_INTERNAL_TO_NET) m_ports[PORT_TO_NET].pushOut(packet);
	}
	
	/**
	 * Used to start a client connection to a remote host/port pair.  
	 * @param r_addr
	 * @param r_port
	 */
	public void initiateConnection(InetAddress r_addr, int r_port){
		remote_port = r_port;
		remote_address = r_addr;
		connection_state = TCB.STATE_SYN_SENT;
		fireListeners(mDataChangeEvent);
		log("Sending SYN Packet");
		mSendWinCheck.send_empty_packet(VNSTCPPacket.TH_SYN);
	}

	/**
	 * For now we generate a predictable sequence number of 1 so
	 * that the TCP stack is easier to debug. 
	 * 
	 * TODO: Add in random ISN
	 */
	private void generate_initial_seq_num()
	{
		// predictable ISN for now
	    
		initial_sequence_num = 1;
	/*
	    struct timeval seed;
	    gettimeofday(&seed, NULL);

	    srandom(seed.tv_usec);
	    uint32_t rand = (random() % 256);
	    our_dprintf("ISN = %d \n", rand);
	    ctx->initial_sequence_num = rand;
	    */

	    send_nxt = initial_sequence_num;
	    send_una = initial_sequence_num;
	}
	
	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
        String from_desc = "Packets destined for a TCP socket";
        String to_desc = "Packets sent from a TCP Socket";
        String internal_from_desc = "sends packets to internal TCP components";
        String internal_to_desc = "receives packets from internal TCP components";
  
        m_ports[PORT_FROM_NET] = new ClackPort(this, PORT_FROM_NET, from_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_TO_NET] =  new ClackPort(this, PORT_TO_NET,  to_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_INTERNAL_FROM_NET] = new ClackPort(this, PORT_INTERNAL_FROM_NET, internal_from_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[PORT_INTERNAL_TO_NET] =  new ClackPort(this, PORT_INTERNAL_TO_NET,  internal_to_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
    } // -- setupPorts
	
	/**
	 * Conveniance method to get a string value representing a TCP state integer value. 
	 * @param state
	 */
	public static String getStateString(int state){
		
		if(state == TCB.STATE_ESTABLISHED){return "Established"; }	
		else if(state == TCB.STATE_CLOSED){ return "Closed";	}
		else if(state == TCB.STATE_SYN_SENT){ return "Syn Sent"; }
		else if(state == TCB.STATE_SYN_RECEIVED){ return "Syn Received"; }
		else if(state == TCB.STATE_FIN_WAIT1){ return "Fin Wait-1"; }
		else if(state == TCB.STATE_FIN_WAIT2){ return "Fin Wait-2"; }
		else if(state == TCB.STATE_CLOSING){ return "Closing"; }
		else if(state == TCB.STATE_TIME_WAIT){ return "Time Wait"; }
		else if(state == TCB.STATE_LAST_ACK){ return "Last-Ack"; }
		else if(state == TCB.STATE_LISTEN) { return "Listen"; }
		else if(state == TCB.STATE_CLOSE_WAIT) { return "Close Wait"; }
		
		return "Unknown";
	}
	
	/**
	 * Initializes the state of a TCB for a server port based on the contents of a
	 * newly received SYN packet. 
	 * 
	 * <p>  This TCB is the result of an accept() call on a server socket, creating
	 * a new child socket. </p>
	 * @param packet
	 */
	public void notifySynReceived(VNSTCPPacket packet){
		local_address = packet.getDestIPAddress();
		local_port = packet.getDestinationPort();
		remote_address = packet.getSourceIPAddress();
		remote_port = packet.getSourcePort();
		log("Accept() on remote IP = " + remote_address
					+ " remote port = " + remote_port);

		recv_nxt = packet.getSeqNum() + 1;
		send_una = initial_sequence_num;
		log("Moving from LISTEN to SYN_RECIEVED");
		send_window = packet.getRecvWindowSize();
		connection_state = TCB.STATE_SYN_RECEIVED;
		mSendWinCheck.send_empty_packet(VNSTCPPacket.TH_SYN | VNSTCPPacket.TH_ACK);
	}	


    
}
