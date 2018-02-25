
package net.clackrouter.application;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.tcp.TCB;
import net.clackrouter.component.tcp.TCP;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;


;

/**
 * Implementation of a TCP Socket for use by Clack applications.
 * 
 * <p> Note: public methods exist both from application side interaction
 * and for use of the TCPSocket ClackComponent within a ClackGraph. </p>
 */
public class TCPSocket extends ClackComponent {

	/**
	 * Port to send outgoing packets to the TCP/IP stack components
	 */
	public static final int PORT_NET_OUT = 0;
	/**
	 * Port to receive incoming packets from the TCP/IP stack components
	 */
	public static final int PORT_NET_IN = 1;
	public static final int NUM_PORTS = 2;
	

	private ClackApplication currentApplication;
	private TCB mTCB;
	private boolean isClosed;
	
	public TCPSocket(TCB t, Router router, String name){
		super(router, name);
		mTCB = t;
		setupPorts(NUM_PORTS);
		mRouter.registerForPoll(this);
		isClosed = false;

	}

	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
        String in_desc = "Data being received by socket";
        String out_desc = "Data being sent by the socket";
  
        m_ports[PORT_NET_IN] = new ClackPort(this, PORT_NET_IN, in_desc, ClackPort.METHOD_PULL, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_NET_OUT] =  new ClackPort(this, PORT_NET_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);

    } // -- setupPorts
	
	
	/**
	 * Callback to be called once per router-processing loop.
	 * 
	 * <p> Checks if the application has closed this socket, and if all data has been sent
	 * notifies the socket to send a FIN </p>
	 */
	public void poll() {
		
		if(isClosed){
			// close() has been called, and we have no more data, so notify of close 
			// TCP will send a FIN once all data has been acked. 
			if(mTCB.mSendBuffer.getOccupancy() == 0)
				mTCB.mSendWinCheck.app_close_request();
		}
		
		/*if(mTCB == null) return;
		
		//if(mTCB.connection_state == TCB.STATE_CLOSED){
		if(mTCB.done == true){
			mRouter.removeFromPoll(this);
			mTCB.mParent.removeTCB(mTCB);
		}if(mTCB.connection_state == TCB.STATE_CLOSE_WAIT) {
			boolean close = mTCB.mCloseWaitStart + TCB.CLOSE_WAIT_MILLIS < System.currentTimeMillis();
			if(close) mTCB.connection_state = TCB.STATE_CLOSED;
			mTCB.done = true;
		}
		*/
	}

	
	
	/**
	 * Default bind behavior selects a random port in the range between 1024 and 2024
	 * and binds to the first listed interface on the router (often eth0)
	 * @throws Exception
	 */
	public void bind() throws Exception {
		Random rand = new Random();
		int port = rand.nextInt(1000) + 1024;
		bind(mRouter.getInputInterfaces()[0].getIPAddress(), port);
	}

	/**
	 * Binds the socket to a specified address and port.
	 * 
	 * <p> At this time, no restrictions are placed on what addresses or ports 
	 * may be bound to.  
	 * 
	 * @param local_address address to bind as source 
	 * @param local_port port to bind as source
	 * @throws Exception
	 */
	public void bind(InetAddress local_address, int local_port) throws Exception {
		mTCB.local_address = local_address;
		mTCB.local_port = local_port;
		mTCB.mParent.createMapping(mTCB);
		mTCB.mParent.fireListeners(new TCP.UpdateEvent(TCP.UpdateEvent.TCP_SOCKET_CREATED, this));
	
	}
	
	/**
	 * Puts the socket into the LISTENING state, ready to receive incoming connections as a server.
	 * @throws Exception
	 */
	public void listen() throws Exception {
		mTCB.mParent.setupListenMapping(mTCB);
	}
	
	/**
	 * Create a client connection to a remote host.
	 * 
	 * Automatically binds the socket if it is not already bound.  
	 * 
	 * @param remote_address remote destination address
	 * @param remote_port remote destination port
	 * @throws Exception
	 */
	public void connect(InetAddress remote_address, int remote_port) throws Exception {
		if(mTCB.local_address == null || mTCB.local_port == 0)
			bind(); // automatically bind if they haven't
		mTCB.mParent.setupClientMapping(mTCB);
		mTCB.initiateConnection(remote_address, remote_port);
		mTCB.mParent.fireListeners(new TCP.UpdateEvent(TCP.UpdateEvent.TCP_TCB_CREATED, mTCB));
	}
	
	/**
	 * Read in data from this socket. 
	 * 
	 * @param max_size largest amount of data to be read
	 * @param msec_timeout millisecs to wait before returning an empty buffer
	 * @return buffer of data, empty buffer if no data was available, or null if the connection has been closed remotely
	 */
	public ByteBuffer recv(int max_size, long msec_timeout){
		mTCB.mRecvBuffer.setMaxBytesForPull(max_size);
		for(int i = 0; i < 10; i++){
			VNSPacket packet = m_ports[PORT_NET_IN].pullIn();
			if(packet != null)
				return packet.getByteBuffer();
			try{
				Thread.sleep(msec_timeout / 10);
			} catch(Exception e) { /* ignore */ }
		}
		if(mTCB.finReceived) {
			log("fin received");
			return null; // signals fin received
		}
		// return empty byte buffer
		return ByteBuffer.allocate(0);	
	}
	
	/**
	 * Returns a client socket once an incoming client connection has been received.
	 * 
	 * <p> This call loops until a client connection is recieved, which it returns.
	 * This is effectively a blocking socket. </p>
	 * @return client socket created
	 */
	public TCPSocket accept(){
		while(true){
			try {
				TCB newTCB = mTCB.mParent.serverAcceptCall(mTCB);
				if(newTCB != null){
					mTCB.mParent.fireListeners(new TCP.UpdateEvent(TCP.UpdateEvent.TCP_TCB_CREATED, newTCB));
					return newTCB.mSocket;
				}
				Thread.sleep(500);
			}catch (Exception e){
				e.printStackTrace();
			}
		}	

	}
	/**
	 * Sends a buffer of data
	 */
	public int send(ByteBuffer buf_out) throws Exception {
		if(isClosed)
			throw new Exception("Socket is closed.  Cannot send more data");
		buf_out.rewind();
		m_ports[PORT_NET_OUT].pushOut(new VNSPacket(buf_out));
		return buf_out.capacity();
	}
	
	/**
	 * Closes this end of the socket connection, meaning no more data can be sent. 
	 */
	public void close(){
		isClosed = true;
		mTCB.fireListeners(mTCB.mDataChangeEvent);
	}
	
	/**
	 * Set the Clack application that is the parent of this application (used internally)
	 */
	public void setCurrentApplication(ClackApplication app) { currentApplication = app; }
	/**
	 * the application using this TCPSocket
	 */
	public ClackApplication getCurrentApplication() { return currentApplication; }
	/**
	 * The transmission control block (TCB) associated with this socket
	 */
	public TCB getTCB() { return mTCB; }

}
