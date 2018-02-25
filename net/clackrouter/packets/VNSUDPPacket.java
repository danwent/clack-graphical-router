
package net.clackrouter.packets;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * A User Datagram Protocol (UDP) Packet.
 * 
 */
public class VNSUDPPacket extends VNSPacket {
	
	public static int UDP_HEADER_LEN = 8;
	
	public int src_port, dst_port, length, checksum;
	protected ByteBuffer m_bodyBuffer;
	protected InetAddress mDestAddress, mSrcAddress;

	
	  public VNSUDPPacket(int sport, int dport, ByteBuffer body)
	  {
	    super();
	    src_port = sport;
	    dst_port = dport;
	    m_bodyBuffer = body;
	    pack();
	  }


	  /**
	   * Constructs an IMCP packet from the supplied byte buffer.
	   *
	   * @param packetBuffer Byte buffer containing an ICMP packet
	   */
	  public VNSUDPPacket(ByteBuffer packetBuffer)
	  {
	    super(packetBuffer);

	    extractFromByteBuffer();
	  }
	
	
	  // method has not yet been tested. 1/8/2005
	  private void extractFromByteBuffer()
	  {
	  	int firstByte, secondByte;
	    byte [] array = new byte[2];
	    
	    m_packetByteBuffer.rewind();
	    
	    // get unsigned src port
	    m_packetByteBuffer.get(array, 0, 2);
        firstByte = (0x000000FF & ((int)array[0]));
        secondByte = (0x000000FF & ((int)array[1]));
	    src_port = (int) (firstByte << 8 | secondByte);
	    
	    // get unsigned dest port
	    m_packetByteBuffer.get(array, 0, 2);
        firstByte = (0x000000FF & ((int)array[0]));
        secondByte = (0x000000FF & ((int)array[1]));
	    dst_port = (int) (firstByte << 8 | secondByte);
        
	    m_packetByteBuffer.get(array, 0 , 2);
        firstByte = (0x000000FF & ((int)array[0]));
        secondByte = (0x000000FF & ((int)array[1]));

	    length = (int) (firstByte << 8 | secondByte);
	    
	    m_packetByteBuffer.get(array, 0 , 2);
        firstByte = (0x000000FF & ((int)array[0]));
        secondByte = (0x000000FF & ((int)array[1]));

        checksum = (int) (firstByte << 8 | secondByte);
        //TODO: should validate checksum if non-zero
        
        byte[] data = new byte[m_packetByteBuffer.remaining()];
        m_packetByteBuffer.get(data, 0, data.length);
        
        m_bodyBuffer = ByteBuffer.wrap(data);

  
	  }
	  
	  public ByteBuffer getBodyBuffer() { return m_bodyBuffer; }
	  public void setBodyBuffer(ByteBuffer buf) { m_bodyBuffer = buf; }
	
	  public void pack()
	  {
	      // Allocate local buffer for the UDP packet
	  	int length = UDP_HEADER_LEN + m_bodyBuffer.capacity();
	      ByteBuffer packetByteBuffer = ByteBuffer.allocate(length);
	     
	       
	      packetByteBuffer.putShort((short) src_port); 
	      packetByteBuffer.putShort((short) dst_port);           
	      packetByteBuffer.putShort((short)length);
	      if(mSrcAddress != null)
	      	packetByteBuffer.putShort((short)calculateChecksum()); // checksum
	      else 
	      	packetByteBuffer.putShort((short)0);
	      packetByteBuffer.put(m_bodyBuffer); // UDP Packet data

	      // Set the super class's global packet buffer
	      this.setByteBuffer(packetByteBuffer);

	  }
	  
      public int calculateChecksum()
      {
          int sum = 0;
          m_bodyBuffer.rewind();
          byte[] body = m_bodyBuffer.array();
          
          ByteBuffer calcBuffer = ByteBuffer.allocate(body.length + 4 + 8 + 2 + 2 + 2);


          calcBuffer.put(mSrcAddress.getAddress());
          calcBuffer.put(mDestAddress.getAddress());
          calcBuffer.putShort((short)src_port);
          calcBuffer.putShort((short)dst_port);
          calcBuffer.putShort((short)(17));
          calcBuffer.putShort((short) (body.length + 8));
          calcBuffer.putShort((short) (body.length + 8));
          calcBuffer.put(body);
          calcBuffer.rewind();
          
          if(calcBuffer.capacity() % 2 != 0) {
          	ByteBuffer tmp = ByteBuffer.allocate(calcBuffer.capacity() + 1);
          	tmp.put(calcBuffer);
          	tmp.put((byte)0);
          	tmp.rewind();
          	calcBuffer = tmp;
          }
          
          for(int wordCount = 0; wordCount < calcBuffer.capacity() / 2; wordCount++) {

              int word = (short) calcBuffer.getShort();
              word &= 0xffff; // Convert into an unsigned short
              
              sum += word;
              // If overflow occured
              if( (sum & 0xffff0000) != 0 ) {
                  sum &= 0xffff;
                  sum++;
              }
          }

          return  ~(sum & 0xffff) & 0xffff;
      }
	 
	  /*
      public int calculateChecksum()
      {
          int sum = 0;
          short proto_udp = VNSIPPacket.PROTO_UDP;
	
          m_bodyBuffer.rewind();
          byte[] body = m_bodyBuffer.array();
          
          for(int wordCount = 0; wordCount < body.length / 2; wordCount++) {

              int word = ((body[2 * wordCount]<<8)&0xFF00)+(body[2 * wordCount +1]&0xFF);
              word &= 0xffff; // Convert into an unsigned short
              sum += word;
          }
          // get odd last byte
          if(body.length % 2 == 1)
          	sum += (0xff) & body[body.length - 1];
          
          byte[] src = mSrcAddress.getAddress();
          for(int wordCount = 0; wordCount < src.length / 2; wordCount++){
          	int word = ((src[2 * wordCount]<<8)&0xFF00)+(src[2 * wordCount +1]&0xFF);
            word &= 0xffff; // Convert into an unsigned short
            sum += word;
          }
          byte[] dst = mDestAddress.getAddress();
          for(int wordCount = 0; wordCount < dst.length / 2; wordCount++){
          	int word = ((dst[2 * wordCount]<<8)&0xFF00)+(dst[2 * wordCount +1]&0xFF);
            word &= 0xffff; // Convert into an unsigned short
            sum += word;
          }
          System.out.println("calculating udp sum for source " + mSrcAddress + " to " + mDestAddress);
          sum = sum + proto_udp + m_packetByteBuffer.capacity();
          
          if( (sum & 0xffff0000) != 0 ) {
            sum &= 0xffff;
            sum++;
          }  

          return  (~(sum & 0xffff) & 0xffff);
      }
*/
	  
	  public int getDestinationPort() { return dst_port; }
	  public int getSourcePort() { return src_port; }
	  
	  public void setDestinationAddress(InetAddress dest){ mDestAddress = dest; }
	  public InetAddress getDestinationAddress() { return mDestAddress; }
	  
	  public void setSourceAddress(InetAddress source) { mSrcAddress = source; }
	  public InetAddress getSourceAddress() { return mSrcAddress; }

}
