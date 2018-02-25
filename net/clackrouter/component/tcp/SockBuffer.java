/*
 * Created on Apr 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.component.tcp;

import java.nio.ByteBuffer;
import java.util.Vector;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.gui.ClackPaintable;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.graph.SockBufferView;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.VertexView;


/**
 * Buffer to hold application data going to or from an application socket.  
 */
public class SockBuffer extends ClackComponent {

	public static int PORT_HEAD = 0;
	public static int PORT_TAIL = 1;
	public static int NUM_PORTS = 2;
	public static int DEFAULT_MAX_OCCUPANCY = 1000;
	
	private Vector dataChunks;
	private int numBytes;
	private TCB mTCB;
	private int mMaxBytesForPull;
	private ClackPaintable view;
	private int mMaxOccupancy;
	
	public SockBuffer(TCB tcb, Router router, String name){
		super(router, name);
		mTCB = tcb;
		numBytes = 0;
		dataChunks = new Vector();
		mMaxBytesForPull = 0;
		mMaxOccupancy = DEFAULT_MAX_OCCUPANCY;
		setupPorts(NUM_PORTS);
	}

	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String head_desc = "Port for the head of the buffer";
        String tail_desc     = "Port for the tail of the buffer";
        m_ports[PORT_HEAD] = new ClackPort(this, PORT_HEAD, head_desc, ClackPort.METHOD_PULL, ClackPort.DIR_OUT, VNSPacket.class);
        m_ports[PORT_TAIL]     = new ClackPort(this, PORT_TAIL, tail_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
    } // -- setupPorts
	
	public VertexView getView(JGraph graph, CellMapper mapper) { 
		JGraph hierarch_graph = getRouter().getDocument().getCurrentGraph();
		SockBufferView sbview = new SockBufferView(getComponentCell(), graph, mapper);
		view = sbview;
		return sbview;
	}
	
	public void setMaxBytesForPull(int max) { mMaxBytesForPull = max; }
	
	public VNSPacket handlePullRequest(int port_num) {
		if(port_num != PORT_HEAD) return null;
	
		// combine data chunks into a single byte buffer
		if(dataChunks.size() > 1) {
			ByteBuffer alldata = ByteBuffer.allocate(numBytes);
			System.out.println("SockBuffer: combining " + dataChunks.size() + " chunks into 1 for " + numBytes + " bytes");
			while(dataChunks.size() > 0){
				// this starts at the each chunk buffer's starting position
				alldata.put((ByteBuffer)dataChunks.remove(0));
			}
			alldata.rewind();
			dataChunks.add(alldata);
		}
		
		if(dataChunks.size() > 0){
		//	System.out.println("SockBuffer has " + numBytes + " bytes");			
			if(numBytes > mMaxBytesForPull){
				//we have more data to send than TCP window allows
			//	System.out.println("Buffer puller wants only " + mMaxBytesForPull + " of data");
				ByteBuffer alldata = (ByteBuffer)dataChunks.get(0); // don't remove!
				byte[] max_to_send = new byte[mMaxBytesForPull];
				alldata.get(max_to_send);
				numBytes -= mMaxBytesForPull;
				if(view != null)
					view.clackPaint();
				mTCB.fireListeners(mTCB.mDataChangeEvent);
				return new VNSPacket(ByteBuffer.wrap(max_to_send));
			}else if (numBytes > 0){
				// TCP window can take all the data we have
			//	System.out.println("pull wants all of sockbuffer's data!");

				ByteBuffer front = (ByteBuffer)dataChunks.remove(0);// remove
				byte[] remaining_data = new byte[numBytes];
				front.get(remaining_data);
				numBytes = 0; 
				if(view != null)
					view.clackPaint();
				mTCB.fireListeners(mTCB.mDataChangeEvent);
				return new VNSPacket(ByteBuffer.wrap(remaining_data)); 
			}
		}
		return null;
	}
	
	public void acceptPacket(VNSPacket packet, int port_number){
		if(port_number != PORT_TAIL) return;
		ByteBuffer buf = packet.getByteBuffer();
		dataChunks.add(buf);
		numBytes += buf.capacity();
		mTCB.fireListeners(mTCB.mDataChangeEvent);
	}
	
	public int getOccupancy() { return numBytes; }
	public int getMaxOccupancy() { return mMaxOccupancy; }
	
}
