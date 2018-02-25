
package net.clackrouter.gui.tcp;

import java.net.InetAddress;

import net.clackrouter.component.extension.TCPMonitor;
import net.clackrouter.component.tcp.TCB;
import net.clackrouter.packets.VNSTCPPacket;



/**
 * Used to analyze a TCP flow that is going through the router.  
 * 
 * <p> Used by a TCP monitor, one per flow, to keep track of the 
 * behavior of a flow. </p> 
 */
public class TCPStateAnalyzer {

	private static final String SERVER = "Server";
	private static final String CLIENT = "Client";
	
	private TCPMonitor.TCPFlow mParentFlow;
	
	private StringBuffer mHistory;
	private int mState;
	private String mRole;
	private InetAddress mAddress;
	private int mPort;
	private long mSynSeqNum = 0;
	private long mFinSeqNum = 0;
	
	private long mMaxSeqSent = -1;
	private int mPacketSize = 0;
	private int mDataSize = 0;
	private long mFirstUnacked = 0;
	private boolean mIsRetrans = false;
	private int mRecvWindow = 0;
	private int mDuplicateSends = 0;
	private int mPacketsSent = 0;
	private int mPacketSeqCount = 0;
	private int mBytesOutstanding = 0;
	
//	public SingleSeriesOccData mFlowOccData;

	public long seq_num;
	public long ack_num;
	public boolean syn, fin, ack;
	

	
	public TCPStateAnalyzer(TCPMonitor.TCPFlow parent, InetAddress myAddr, int port) {
		mParentFlow = parent;
		mAddress = myAddr;
		mState = TCB.STATE_UNKNOWN;
		mRole = "Unknown";
		mHistory = new StringBuffer(800);
		mPort = port;
//		mFlowOccData = new SingleSeriesOccData("Instantaneous Data Outstanding with Source A");
		
	}
	
	public int getPort() { return mPort; }
	public InetAddress getAddress() { return mAddress; }
	public String getHistory() { return mHistory.toString(); }
	public String getState() { return TCB.getStateString(mState); }
	public String getRole() { return mRole; }
	public void setRole(String r) { mRole = r; }
	public int getLastPacketSize() { return mPacketSize; }
	public int getLastDataSize() { return mDataSize; }
	public long getTotalBytesAcked() { 
		if(mState == TCB.STATE_SYN_SENT || mState == TCB.STATE_SYN_RECEIVED) return 0;
		return (mFirstUnacked - mSynSeqNum);
	}
	public boolean isRetrans() { return mIsRetrans; }
	public int getRecvWindow() { return mRecvWindow; }
	public int getDuplicateSends() { return mDuplicateSends; }
	public int getPacketsSent() { return mPacketsSent; }
	public int getPacketSeqCount() { return mPacketSeqCount; }
	public int getBytesOutStanding() { return mBytesOutstanding; }
	
	
	
	public void analyzePacket(VNSTCPPacket update){

			
		if(update.getSourceIPAddress().equals(mAddress)){
			
			// from us
			mPacketsSent++;
			mPacketSeqCount = update.getSeqCount();
			seq_num = update.getSeqNum();
			if(seq_num > mMaxSeqSent)
				mMaxSeqSent = seq_num;
			else if(fin || syn || update.getDataSize() > 0)
				mDuplicateSends++;
			
			ack_num = update.getAckNum();
			mPacketSize = update.getByteBuffer().capacity();
			mDataSize = update.getDataSize();
			mRecvWindow = update.getRecvWindowSize();
			fin = update.finFlagSet();
			syn = update.synFlagSet();
			ack = update.ackFlagSet();
			if(mMaxSeqSent < update.getSeqNum()){
				mIsRetrans = false; 
				mMaxSeqSent = update.getSeqNum();
			}else {
				mIsRetrans = true;
			}
		}else {
			// to us
			mPacketSeqCount = 0;
			if(update.ackFlagSet())
				mFirstUnacked = update.getAckNum();
		}
		
		mBytesOutstanding = (int)(seq_num + getPacketSeqCount() - mFirstUnacked);
		if(mBytesOutstanding >= 0 && mBytesOutstanding <= 100000) {
			Long delta = new Long(System.currentTimeMillis() - mParentFlow.startTime);
		//	mFlowOccData.addXYValue(delta, new Integer(mBytesOutstanding));
		}
		
		analyzeFlowState(update);
	}
	
	public boolean analyzeFlowState(VNSTCPPacket update) {
		
		if(mState == TCB.STATE_ESTABLISHED){
			if(!update.finFlagSet()) return false;
			
			if(update.getSourceIPAddress().equals(mAddress)){
				// we sent FIN
				mState = TCB.STATE_FIN_WAIT1;
				mFinSeqNum = update.getSeqNum();
				mHistory.append("Application requested a close.  Sending FIN and moving to FIN WAIT-1\n");
			}else {
				// we are receiving FIN
				mState = TCB.STATE_CLOSE_WAIT;
				mHistory.append("Received FIN packet.  Moving to CLOSE WAIT\n");
			}
			
		}else if(mState == TCB.STATE_UNKNOWN){
			if(!update.synFlagSet())return false;
			if(!update.getSourceIPAddress().equals(mAddress))return false;
			
			//only handle packet when we sent them
				
			mSynSeqNum = update.getSeqNum();
			if(update.ackFlagSet()){
				// this is a syn-ack
				if(!mRole.equals(SERVER)) 
					System.out.println("Error assigning TCPState roles");
				
				mState = TCB.STATE_SYN_RECEIVED;
				mHistory.append("Recieved SYN packet.  Moving from LISTEN to SYN RECEIVED\n");
			}else {
				// just a syn packet
				mRole = CLIENT;
				mParentFlow.getOtherTCPStateAnalyzer(this).setRole(SERVER);
				mState = TCB.STATE_SYN_SENT;
				mHistory.append("Active Open: Sending SYN Packet and moving to SYN SENT\n");
			}

		}else if(mState == TCB.STATE_SYN_SENT){
			if(update.getSourceIPAddress().equals(mAddress)) return false;
			if(update.synFlagSet() && update.ackFlagSet() && update.getAckNum() > mSynSeqNum){
				mState = TCB.STATE_ESTABLISHED;
				mHistory.append("SYN-ACK received.  Moving to ESTABLISHED\n");
			}
			
		}else if(mState == TCB.STATE_SYN_RECEIVED){
			if(update.getSourceIPAddress().equals(mAddress)) return false;
			if(update.ackFlagSet() && update.getAckNum() > mSynSeqNum){
				mState = TCB.STATE_ESTABLISHED;
				mHistory.append("ACK of SYN receieved.  Moving to ESTABLISHED\n");
			}
			
		}else if(mState == TCB.STATE_FIN_WAIT1){
			if(update.getSourceIPAddress().equals(mAddress))return false;
			
			if(update.ackFlagSet() && update.getAckNum() > mFinSeqNum){
				//acking our FIN 
				mState = TCB.STATE_FIN_WAIT2;
				mHistory.append("Received ACK of FIN packet.  Moving to FIN WAIT-2\n");
				analyzeFlowState(update); // in case we also received FIN in this packet
			}
			
			if(update.finFlagSet()){
				mState = TCB.STATE_CLOSING;
				mHistory.append("Received FIN packet.  Moving to CLOSING\n");
			}
			
		}else if(mState == TCB.STATE_FIN_WAIT2){
			if(update.getSourceIPAddress().equals(mAddress))return false;
			
			if(update.finFlagSet()){
				mState = TCB.STATE_TIME_WAIT;
				mHistory.append("FIN packet received.  Moving to TIME WAIT\n");
			}
		}else if(mState == TCB.STATE_CLOSING){
			if(update.getSourceIPAddress().equals(mAddress))return false;
			
			if(update.ackFlagSet() && update.getAckNum() > mFinSeqNum){
				mState = TCB.STATE_TIME_WAIT;
				mHistory.append("Received ACK for FIN.  Moving to TIME WAIT\n");
			}
		}else if(mState == TCB.STATE_TIME_WAIT){
			if(!update.getSourceIPAddress().equals(mAddress))return false;
			
			if(update.ackFlagSet())
				mHistory.append("TIME WAIT is resending the ACK for the received FIN packet\n");
		}else if(mState == TCB.STATE_CLOSE_WAIT){
			if(!update.getSourceIPAddress().equals(mAddress))return false;
			
			if(update.finFlagSet()){
				mFinSeqNum = update.getSeqNum();
				mState = TCB.STATE_LAST_ACK;
				mHistory.append("Application requested a close.  Sending FIN and moving to LAST-ACK\n");
			}
		}else if(mState == TCB.STATE_LAST_ACK){
			if(update.getSourceIPAddress().equals(mAddress))return false;
			
			if(update.ackFlagSet() && update.getAckNum() > mFinSeqNum){
				mState = TCB.STATE_CLOSED;
				mHistory.append("Fin packet is acked.  Moving to CLOSED\n");
			}
		}
		

		return true;
	}
	

	public String getControlString(){
		StringBuffer buf = new StringBuffer(40);
		buf.append("Control Bits Set : ");
		if(syn) buf.append("SYN ");
		if(fin) buf.append("FIN ");
		if(ack) buf.append("ACK");
		return buf.toString();
	}
	
	
	
}
