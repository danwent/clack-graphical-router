/*
 * Created on Jan 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.packets;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Represents a Transmission Control Protocol (TCP) Packet.  
 * 
 * <p> Options are not supported right now, because they are a pain in the butt.
 * Perhaps I will support them in the future.  </p>
 */
public class VNSTCPPacket extends VNSPacket {
	
	public static final int DEFAULT_HEADER_LEN = 20;

	public static final int TH_FIN = 0x01;
	public static final int TH_SYN = 0x02;
	public static final int TH_RST = 0x04;
	public static final int TH_PSH = 0x08;
	public static final int TH_ACK = 0x10;
	public static final int TH_URG = 0x20;
	
	public long seq_num, ack_num;
	public int src_port, dst_port, recv_window;
	public int control_flags, data_offset;
	
	public int m_header_len_bytes;
    private ByteBuffer m_bodyBuffer;
    private InetAddress m_dest_address;
    private InetAddress m_src_address;
	
		/* creates a TCP packet from bytes representing a full
		 * 	TCPPacket (ie: packets from the network)
		 */
	  public VNSTCPPacket(ByteBuffer packetBuffer)
	  {
	    super(packetBuffer);

	    extractFromByteBuffer();
	  }
	  
	  public VNSTCPPacket(int src_port, int dst_port, long seq_num,
	  			long ack_num, int flags, int recv_window, byte[] data) {
	  		this.src_port = src_port;
	  		this.dst_port = dst_port;
	  		this.seq_num = seq_num;
	  		this.ack_num = ack_num;
	  		this.control_flags = flags;
	  		this.recv_window = recv_window;
	  		this.data_offset = DEFAULT_HEADER_LEN / 4;
	  		this.m_bodyBuffer = ByteBuffer.wrap(data);
	  }
	  	
	  /**
	   * Extracts individual fields of an TCP packet.
	   */
	  private void extractFromByteBuffer()
	  {
	 // 	System.out.println("Extracting TCP from stream: \n" + getByteString(m_packetByteBuffer.array()));
	    byte [] array = new byte[4];
	    m_packetByteBuffer.rewind();
	//    System.out.println("Dumping Recieved Header bytes");
	//    for(int i = 0; i < m_packetByteBuffer.capacity(); i++){
	//    	System.out.println("byte #" + i + ": " + (0x000000FF & m_packetByteBuffer.get()));
	//    }
	    
	    m_packetByteBuffer.rewind();
	 //   m_length = m_packetByteBuffer.capacity();
	  
	    m_packetByteBuffer.get(array, 0, 2);
	    src_port = get16bit(array);
	    m_packetByteBuffer.get(array, 0, 2);
	    dst_port = get16bit(array);       
	    m_packetByteBuffer.get(array, 0 , 4);
        seq_num  = get32bit(array);
	    m_packetByteBuffer.get(array, 0 , 4);
        ack_num  = get32bit(array);

	    // get unsigned misc_bits
       
	    m_packetByteBuffer.get(array, 0, 2);
        int misc_bits = get16bit(array);
        data_offset = (misc_bits >> 12);
        m_header_len_bytes = (4 * data_offset);
       
        control_flags = (0x0000003F & misc_bits);
	  
	    // get unsigned window 
	    m_packetByteBuffer.get(array, 0, 2);
	    recv_window = (int) get16bit(array);
	//    System.out.println("Header Len: " + m_header_len_bytes);
	//    System.out.println("Body Len: " + (m_length - m_header_len_bytes));
	    
	 //   System.out.println("Exctracting TCP Packet of size: " + m_length);
	    
	    // throw away the header, keep the body
	    byte[] junk = new byte[m_header_len_bytes];
	    byte[] body = new byte[m_length - m_header_len_bytes];
	    m_packetByteBuffer.rewind();
	    m_packetByteBuffer.get(junk, 0, junk.length);
	    m_packetByteBuffer.get(body, 0, body.length);
	    m_bodyBuffer = ByteBuffer.wrap(body);
	    m_packetByteBuffer.rewind();
	  }
	  
	  public void pack() {
  		m_packetByteBuffer = ByteBuffer.allocate(DEFAULT_HEADER_LEN + m_bodyBuffer.capacity());
  		m_packetByteBuffer.putShort((short)src_port);
  		m_packetByteBuffer.putShort((short)dst_port);
  		m_packetByteBuffer.putInt((int)seq_num);
  		m_packetByteBuffer.putInt((int)ack_num);
  		int misc = ((data_offset << 12) | control_flags);
  		m_packetByteBuffer.putShort((short)misc);
  		m_packetByteBuffer.putShort((short)recv_window);
  		m_packetByteBuffer.putShort((short)0); // set checksum to zero at first
  		m_packetByteBuffer.putShort((short)0); // urgent = 0
  		m_packetByteBuffer.put(m_bodyBuffer);
  		m_bodyBuffer.rewind(); // in case we need to do another pack() (bug: resend gives garbage data)

	  }
	  
	  public void trimDataFront(int numBytes){
	  	System.out.println("Trimming " + numBytes + " from start of TCP data");
	  	byte[] garbage = new byte[numBytes];
	  	byte[] temp = new byte[m_bodyBuffer.capacity() - numBytes];
	  	m_bodyBuffer.rewind();
	  	m_bodyBuffer.get(garbage, 0, numBytes - 1);
	  	m_bodyBuffer.get(temp, 0,temp.length - 1);
	  	m_bodyBuffer = ByteBuffer.wrap(temp);
	  }
	  
	  public void trimDataEnd(int numBytes){
	  	System.out.println("Trimming " + numBytes + " from end of TCP data");
	  	byte[] temp = new byte[m_bodyBuffer.capacity() - numBytes];
	  	m_bodyBuffer.rewind();
	  	m_bodyBuffer.get(temp, 0, temp.length - 1);
	  	m_bodyBuffer = ByteBuffer.wrap(temp);
	  }
	 
	  
	  public int getSourcePort() {  return src_port;}  
	  public int getDestinationPort() { return dst_port; } 
	  public long getSeqNum() {	return seq_num;} 
	  public long getAckNum() {	return ack_num; } 
	  public int getRecvWindowSize() { return recv_window; }
	  public boolean synFlagSet() { return ((control_flags & TH_SYN) != 0); }
	  public boolean ackFlagSet() { return ((control_flags & TH_ACK) != 0); }
	  public boolean finFlagSet() { return ((control_flags & TH_FIN) != 0); }
	  public ByteBuffer getBodyBuffer() { return m_bodyBuffer; }	
	  public int getDataSize() { return m_bodyBuffer.capacity(); }
	  public int getSeqCount() {
	  	int count = getDataSize();
	  	if(synFlagSet()) count++;
	  	if(finFlagSet()) count++;
	  	return count;
	  }
	  
      public String toString()
      {
          StringBuffer stringBuffer = new StringBuffer(200);

          stringBuffer.append("SEQ: " + seq_num+ "\n");
          stringBuffer.append("ACK: " + ack_num+ "\n");
          stringBuffer.append("SRC Port: " + src_port + "\n");
          stringBuffer.append("DST Port: " + dst_port + "\n");
          stringBuffer.append("Flags: ");
          if(synFlagSet()) stringBuffer.append("(SYN) ");
          if(ackFlagSet()) stringBuffer.append("(ACK) ");
          if(finFlagSet()) stringBuffer.append("(FIN)");
          stringBuffer.append("\n");
          stringBuffer.append("Header Length: " + (data_offset * 4) + " bytes\n");
          stringBuffer.append("Data length: " + m_bodyBuffer.capacity() + " bytes\n");
          stringBuffer.append("Data: " + getByteString(m_bodyBuffer.array()) + " (" + new String(m_bodyBuffer.array()) + ")");
       
          return stringBuffer.toString();
      }
      
  	public static String getByteString(byte[] arr){
		StringBuffer buf = new StringBuffer(2 * arr.length);
		for(int i = 0; i < arr.length; i++){
			buf.append(" " + (0xFF & arr[i]));
		}
		return buf.toString();
	}
  	
  	public void setDestIPAddress(InetAddress addr) { m_dest_address = addr; }
  	public InetAddress getDestIPAddress() { return m_dest_address; }
  	public void setSourceIPAddress(InetAddress addr) { m_src_address = addr; }
  	public InetAddress getSourceIPAddress() { return m_src_address; }
}
