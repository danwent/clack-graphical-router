package net.clackrouter.packets;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * This is the packet type for a simple distance-vector 
 * protocol.  Packets have no header and consist only of
 * a list of 14-byte entries:
 * < dest ip (4-bytes)> < dest mask (4-bytes) > < cost (4-bytes) > < ttl (2 - bytes) >.
 * @author Dan
 *
 */

public class RIPRoutingUpdate extends VNSPacket {
	
	public static int RIP_ENTRY_SIZE_BYTES = 14;
	private ArrayList m_entries = new ArrayList();
	
	public class Entry {
		/** network portion of the prefix being advertised */
		public InetAddress net;
		/** mask portion of the prefix being advertised */
		public InetAddress mask;
		/** metric cost to reach the advertised prefix, according to the update creator */
		public int metric; 
		/** time-to-live of this update entry, according to the update creator */
		public short ttl;
	}


	/**
	 * Create an empty RIP routing update, for use when building an
	 * update from scratch.  
	 *
	 */
	public RIPRoutingUpdate() {
		super();
	}

	
	  /**
	   * Constructs an RIP packet from the supplied byte buffer, for use
	   * when extracting an update from a received IP packet
	   *
	   * @param packetBuffer Byte buffer containing a RIP packet
	   */
	  public RIPRoutingUpdate(ByteBuffer packetBuffer)
	  {
	    super(packetBuffer);

	    extractFromByteBuffer();
	  }
	
	
	  // method has not yet been tested. 1/8/2005
	  private void extractFromByteBuffer()
	  {
	  	int firstByte, secondByte, thirdByte, fourthByte;
	    byte [] array = new byte[4];
	    
	    m_packetByteBuffer.rewind();
	    
	    if(m_packetByteBuffer.capacity() % RIP_ENTRY_SIZE_BYTES != 0){
	    	System.err.println("Error, DVR update has " + m_packetByteBuffer.capacity() + " of data");
	    	return;
	    }
	    int num_entries = m_packetByteBuffer.capacity() / RIP_ENTRY_SIZE_BYTES;
	  //  System.out.println("Extracting " + num_entries + " entries from DVR packet");
	    
	    for(int i = 0; i < num_entries; i++){
	    	Entry entry = new Entry();
	    	try {
	    		m_packetByteBuffer.get(array, 0, 4);
	    		entry.net = InetAddress.getByAddress(array);
	    		m_packetByteBuffer.get(array, 0, 4);
	    		entry.mask = InetAddress.getByAddress(array);
	    		m_packetByteBuffer.get(array, 0, 4);
	    		firstByte = (0x000000FF & ((int)array[0]));
	    		secondByte = (0x000000FF & ((int)array[1]));
	    		thirdByte = (0x000000FF & ((int)array[2]));
	    		fourthByte = (0x000000FF & ((int)array[3]));
	    		entry.metric = (int) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte);	 
	    		
	    		m_packetByteBuffer.get(array, 0, 2);
	    		firstByte = (0x000000FF & ((int)array[0]));
	    		secondByte = (0x000000FF & ((int)array[1]));
	    		entry.ttl = (short) (firstByte << 8 | secondByte);
		     
	    /*		System.out.println("DVR entry : " + entry.net.getHostAddress() + " " +  
	    					entry.mask.getHostAddress() + " cost = " + entry.cost + " ttl = " + entry.ttl);
	    					*/
	    		m_entries.add(entry);
	    	} catch (Exception e){
	    		e.printStackTrace();
	    	}
	    }
  
	  }
	  
	  /**
	   * Adds a single entry to the list of prefixes contained in this update
	   * @param dest
	   * @param mask
	   * @param cost
	   * @param ttl
	   */
	  public void addEntry(InetAddress dest, InetAddress mask, int cost, short ttl){
		  Entry entry = new Entry();
		  entry.net = dest;
		  entry.metric = cost;
		  entry.mask = mask;
		  entry.ttl = ttl;
		  m_entries.add(entry);
		  pack(); // b/c students might forget otherwise
	  }
	  
	  /**
	   * Retrieve all prefix entries contained within this update
	   * @return arraylist of {@link RIPRoutingUpdate.Entry} objects
	   */
	  public ArrayList getAllEntries() { return m_entries; }
	
	  private void pack()
	  {
	      // Allocate local buffer for the DVR packet
	  	  int length = RIP_ENTRY_SIZE_BYTES * m_entries.size();
	      ByteBuffer packetByteBuffer = ByteBuffer.allocate(length);

	      for(int i = 0; i < m_entries.size(); i++){
	    	  Entry entry = (Entry)m_entries.get(i);
	    	  packetByteBuffer.put(entry.net.getAddress());
	    	  packetByteBuffer.put(entry.mask.getAddress());
	    	  packetByteBuffer.putInt(entry.metric);
	    	  packetByteBuffer.putShort(entry.ttl);
	      }

	      // Set the super class's global packet buffer
	      this.setByteBuffer(packetByteBuffer);

	  }
	  

	
}
