package net.clackrouter.packets;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.clackrouter.netutils.NetUtils;

/**
 * Packet type for a simple link-state routing protocol. 
 * 
 * OSPF packet format
 *
 * | routerid (4 bytes) | age (4 bytes)| seq num  (4 bytes) | num link entries (2 bytes) | num net entries (2 bytes) |
 * | list of link entries (8 bytes each)  |  list of net entries (8 bytes each) |
 * @author Dan
 *
 */

public class LinkStatePacket extends VNSPacket {
	public static int LSA_SIZE_BYTES = 8;
	
	private long routerID, age, sequence_number;
	
	public static class Link {
		public long linkID;
		public int weight;
		
	}
	
	public static class Net {
		public InetAddress network, mask;
		public boolean equals(Object b) {
			if(!(b instanceof Net)) 	return false;

			boolean same_net = network.equals(((Net)b).network);
			boolean same_mask = mask.equals(((Net)b).mask);
			return  same_net && same_mask;
		}
	}
	
	protected ArrayList<Link> m_link_entries = new ArrayList<Link>();
	protected ArrayList<Net> m_net_entries = new ArrayList<Net>();
	
	public LinkStatePacket(long id, long a, long seq) {
		super();
		routerID = id;
		age = a;
		sequence_number = seq;
	}

	
	  /**
	   * Constructs an OSPF packet from the supplied byte buffer.
	   *
	   * @param packetBuffer Byte buffer containing an OSPF packet
	   */
	  public LinkStatePacket(ByteBuffer packetBuffer)
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
	    
		m_packetByteBuffer.get(array, 0, 4);	
		routerID = get32bit(array);
		m_packetByteBuffer.get(array, 0, 4);
		age = get32bit(array);
		m_packetByteBuffer.get(array, 0, 4);
		sequence_number = get32bit(array); 
		
		// get number of announcements
		m_packetByteBuffer.get(array, 0, 2);
		int link_lsa_count = get16bit(array);
		m_packetByteBuffer.get(array, 0, 2);
	    int net_lsa_count = get16bit(array);
	    
	    int expected_capacity = 16 + LSA_SIZE_BYTES * (link_lsa_count + net_lsa_count);
	    
	    if(m_packetByteBuffer.capacity() != expected_capacity){
	    	System.err.println("Error, LSP has " + m_packetByteBuffer.capacity() + " of data, expected " + expected_capacity);
	    	return;
	    }
	    
	    for(int i = 0; i < link_lsa_count; i++){
	    	Link link_lsa = new Link();
	    	try {
	    		// linkID, weight 
	    		m_packetByteBuffer.get(array, 0, 4);
	    		link_lsa.linkID = get32bit(array);
	    		m_packetByteBuffer.get(array, 0, 4);
	    		link_lsa.weight = (int)get32bit(array);  		
	    		m_link_entries.add(link_lsa);
	    	} catch (Exception e){
	    		e.printStackTrace();
	    	}
	    }
	    
	    for(int i = 0; i < net_lsa_count; i++){
	    	Net net_lsa = new Net();
	    	try {
	    		//  network, mask   		
	    		m_packetByteBuffer.get(array, 0, 4);
	    		net_lsa.network = InetAddress.getByAddress(array);
	    		m_packetByteBuffer.get(array, 0, 4);
	    		net_lsa.mask = InetAddress.getByAddress(array);  		
	    		m_net_entries.add(net_lsa);
	    	} catch (Exception e){
	    		e.printStackTrace();
	    	}
	    }
  
	  }
	  
	  public void addLinkLSA( long linkID, int weight){
		  Link link_lsa = new Link();
		  link_lsa.linkID = linkID;
		  link_lsa.weight = weight;
		  m_link_entries.add(link_lsa);
		  pack(); // b/c students might forget otherwise
	  }
	  
	  public void addNetworkLSA(InetAddress network, InetAddress mask){
		  Net network_lsa = new Net();
		  network_lsa.network = network;
		  network_lsa.mask = mask;
		  m_net_entries.add(network_lsa);
		  pack(); // b/c students might forget otherwise
	  }	  
	  
	  public ArrayList getLinkLSAs() { return m_link_entries; }
	  public ArrayList getNetworkLSAs() { return m_net_entries; }
	
	  public void pack()
	  {
	  	  int length = LSA_SIZE_BYTES * (m_link_entries.size() + m_net_entries.size());
	      ByteBuffer packetByteBuffer = ByteBuffer.allocate(length + 16);

    	  packetByteBuffer.putInt((int)routerID);
    	  packetByteBuffer.putInt((int)age);
    	  packetByteBuffer.putInt((int)sequence_number);
	      
	      packetByteBuffer.putShort((short)m_link_entries.size());
	      packetByteBuffer.putShort((short)m_net_entries.size());
	      for(int i = 0; i < m_link_entries.size(); i++){
	    		// linkID, weight 
	    	  Link link_lsa = (Link)m_link_entries.get(i);
	    	  packetByteBuffer.putInt((int)link_lsa.linkID);
	    	  packetByteBuffer.putInt((int)link_lsa.weight);
	      }
	      for(int i = 0; i < m_net_entries.size(); i++){
	    		// routerID, age, sequence_number, network, mask
	    	  Net net_lsa = (Net)m_net_entries.get(i);
	    	  packetByteBuffer.put(net_lsa.network.getAddress());
	    	  packetByteBuffer.put(net_lsa.mask.getAddress());
	      }
	      // Set the super class's global packet buffer
	      this.setByteBuffer(packetByteBuffer);

	  }
	  public long getRouterID() { return routerID; }
	  public long getAge() { return age; }
	  public void setAge(long l) { age = l; } // b/c we use packets to store data in linkstate-database
	  public long getSequenceNumber() { return sequence_number; }
	  
	    /**
	     * Tests to see if the link and network announcement in two packets are identical
	     * This function does not compares routerID, age, or sequence number attributes
	     * @param p1
	     * @param p2
	     * @return
	     */
	    public static boolean LSAContentIsIdentical(LinkStatePacket p1, LinkStatePacket p2){
			if(p1.getLinkLSAs().size() != p2.getLinkLSAs().size()
					|| p1.getNetworkLSAs().size() != p2.getNetworkLSAs().size()) 
				return false;
			
			// check link announcements
			for(int i = 0; i < p1.getLinkLSAs().size(); i++){
				LinkStatePacket.Link p1_annc = ((LinkStatePacket.Link)p1.getLinkLSAs().get(i));
				boolean link_found = false;
				for(int j = 0; j < p2.getLinkLSAs().size(); j++) {
					LinkStatePacket.Link p2_annc = ((LinkStatePacket.Link)p2.getLinkLSAs().get(i));
					if(p1_annc.linkID == p2_annc.linkID && p1_annc.weight == p2_annc.weight) {
						link_found = true;
						break;
					}
				}
				if(!link_found) return false;
			}
			
			// check net announcements
			for(int i = 0; i < p1.getNetworkLSAs().size(); i++){
				LinkStatePacket.Net p1_annc = ((LinkStatePacket.Net)p1.getNetworkLSAs().get(i));
				boolean link_found = false;
				for(int j = 0; j < p2.getNetworkLSAs().size(); j++) {
					LinkStatePacket.Net p2_annc = ((LinkStatePacket.Net)p2.getNetworkLSAs().get(i));
					if(p1_annc.network.equals(p2_annc.network) && p1_annc.mask.equals(p2_annc.mask)) {
						link_found = true;
						break;
					}
				}
				if(!link_found) return false;
			}
			
	    	return true;
	    }
}
