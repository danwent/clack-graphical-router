/**
 * VRICMPPacket.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;


import java.awt.Point;
import java.nio.ByteBuffer;


/**
 * Encapsulates Internet Control Message Protocol packet. Defined in RFC 792.
 */
public final class VNSICMPPacket extends VNSPacket
{

  /**
   * Constructs an IMCP packet from the supplied byte buffer.
   *
   * @param packetBuffer Byte buffer containing an ICMP packet
   */
  public VNSICMPPacket(ByteBuffer packetBuffer)
  {
    super(packetBuffer);

    extractFromByteBuffer();
  }
  
  protected VNSICMPPacket() {
  	// empty constructor, used by static wrappers only
  }
  
  /**
   * Extracts individual fields of an ICMP packet.
   */
  private void extractFromByteBuffer()
  {
    m_packetByteBuffer.rewind();
    
    byte [] type = new byte[TYPE_LEN];
    byte [] code = new byte[CODE_LEN];
    byte [] checksum = new byte[CHECKSUM_LEN];
    int data_len = m_packetByteBuffer.capacity() - TYPE_LEN - CODE_LEN - CHECKSUM_LEN;
    m_data = new byte[data_len];
    // Extract type
    m_packetByteBuffer.get(type, 0, TYPE_LEN);
    m_typeBuffer = ByteBuffer.wrap(type);
    assignType(); // Assigns VRICMPType wrapper

    // Extract code
    m_packetByteBuffer.get(code, 0, CODE_LEN);
    m_codeBuffer = ByteBuffer.wrap(code);

    // Extract checksum
    m_packetByteBuffer.get(checksum, 0, CHECKSUM_LEN);
    m_checksumBuffer = ByteBuffer.wrap(checksum);
    
    m_packetByteBuffer.get(m_data, 0, data_len);
    

    //System.err.println(this);
  }

  public static VNSICMPPacket wrapUnreach(VNSICMPCode code, IPPacket bad_port_packet) throws VNSUnknownICMPCodeException {
  	VNSICMPPacket packet = new VNSICMPPacket();
  	packet.constructByteBufferUnreach(code, bad_port_packet);
  	return packet;
  }
  
  private void constructByteBufferUnreach(VNSICMPCode code, IPPacket ipPacket)
    throws VNSUnknownICMPCodeException
  {
    byte icmpType = VNSICMPType.UNREACH;
    byte icmpCode = code.getValue();

    switch(icmpCode) {

    case VNSICMPCode.UNREACH_HOST:
    case VNSICMPCode.UNREACH_NET:
    case VNSICMPCode.UNREACH_PORT:

      // Obtain the IP packet's header, to be included in the ICMP message
      ByteBuffer ipHeaderBuffer = ipPacket.getHeader().getByteBuffer();
      ipHeaderBuffer.rewind();

      // Obtain the IP packet's body, whose first 8 bytes will be included
      byte [] ipBodyBytes = ipPacket.getBodyBytes();
      int ipBodyLength = Math.min(ipPacket.getBodyLength(), IP_PACKET_BODY_LEN);

      // Allocate local buffer for the ICMP packet
      ByteBuffer packetByteBuffer = ByteBuffer.allocate(TYPE_LEN + CODE_LEN +
							CHECKSUM_LEN +
							UNREACH_UNUSED_LEN + 
							ipPacket.getHeader().getLength() +
							ipBodyLength);
      packetByteBuffer.put(icmpType);       // ICMP type
      packetByteBuffer.put(icmpCode);       // ICMP code
      packetByteBuffer.putShort((short) 0); // Checksum, still unknown
      packetByteBuffer.putInt(0);           // Unused 4 bytes
      packetByteBuffer.put(ipHeaderBuffer); // IP packet's header bytes
      packetByteBuffer.put(ipBodyBytes, 0, ipBodyLength); // IP packets's 8 bytes of body

      // Set the super class's global packet buffer
      this.setByteBuffer(packetByteBuffer);

      // Calculate the checksum for the global buffer
      this.setChecksum();
      
      // Extract individual fields from the global buffer
      this.extractFromByteBuffer();

      break;

    default:
      throw new VNSUnknownICMPCodeException(code + " is not a known code");
    }
  }

  public static VNSICMPPacket wrapTimeExceeded(VNSICMPCode code, IPPacket expired_packet) throws VNSUnknownICMPCodeException {
  	VNSICMPPacket icmp = new VNSICMPPacket();
  	icmp.constructByteBufferTimeExceeded(code, expired_packet);
  	return icmp;
  }
  
  private void constructByteBufferTimeExceeded( VNSICMPCode code, IPPacket ipPacket)
    throws VNSUnknownICMPCodeException
  {
    byte icmpType = VNSICMPType.TIME_EXCEEDED;
    byte icmpCode = code.getValue();

    switch(icmpCode) {

    case VNSICMPCode.TIME_EXCEEDED_IN_TRANSIT:

      // Obtain the IP packet's header, to be included in the ICMP message
      ByteBuffer ipHeaderBuffer = ipPacket.getHeader().getByteBuffer();
      ipHeaderBuffer.rewind();

      // Obtain the IP packet's body, whose first 8 bytes will be included
      byte [] ipBodyBytes = ipPacket.getBodyBytes();
      int ipBodyLength = Math.min(ipPacket.getBodyLength(), IP_PACKET_BODY_LEN);

      // Allocate local buffer for the ICMP packet
      ByteBuffer packetByteBuffer = ByteBuffer.allocate(TYPE_LEN + CODE_LEN +
							CHECKSUM_LEN +
							TIME_EXCEEDED_UNUSED_LEN +
							ipPacket.getHeader().getLength() +
							ipBodyLength);
      packetByteBuffer.put(icmpType);       // ICMP type
      packetByteBuffer.put(icmpCode);       // ICMP code
      packetByteBuffer.putShort((short) 0); // Checksum, still unknown
      packetByteBuffer.putInt(0);           // Unused 4 bytes
      packetByteBuffer.put(ipHeaderBuffer); // IP packet's header bytes
      packetByteBuffer.put(ipBodyBytes, 0, ipBodyLength); // IP packets's 8 bytes of body

      // Set the super class's global packet buffer
      this.setByteBuffer(packetByteBuffer);

      // Calculate the checksum for the global buffer
      this.setChecksum();
      
      // Extract individual fields from the global buffer
      this.extractFromByteBuffer();

      break;

    default:
      throw new VNSUnknownICMPCodeException(code + " is not a known code");
    }
  }
  public static VNSICMPPacket wrapParamProblem(VNSICMPCode code, IPPacket expired_packet, byte paramProblem)
  	throws VNSUnknownICMPCodeException {
  	VNSICMPPacket icmp = new VNSICMPPacket();
  	icmp.constructByteBufferParamProblem(code, expired_packet, paramProblem);
  	return icmp;
  }
  
  private void constructByteBufferParamProblem(VNSICMPCode code,IPPacket ipPacket,  byte paramProblem)
    throws VNSUnknownICMPCodeException
  {
    byte icmpType = VNSICMPType.PARAMETER_PROBLEM;
    byte icmpCode = code.getValue();

    switch(icmpCode) {

    case VNSICMPCode.PARAMETER_PROBLEM_OP_TABLE_SENT:

      // Obtain the IP packet's header, to be included in the ICMP message
      ByteBuffer ipHeaderBuffer = ipPacket.getHeader().getByteBuffer();
      ipHeaderBuffer.rewind();

      // Obtain the IP packet's body, whose first 8 bytes will be included
      byte [] ipBodyBytes = ipPacket.getBodyBytes();
      int ipBodyLength = Math.min(ipPacket.getBodyLength(), IP_PACKET_BODY_LEN);

      // Allocate local buffer for the ICMP packet
      ByteBuffer packetByteBuffer = ByteBuffer.allocate(TYPE_LEN + CODE_LEN +
							CHECKSUM_LEN +
							POINTER_LEN +
							PARAM_PROBLEM_UNUSED_LEN +
							ipPacket.getHeader().getLength() +
							ipBodyLength);
      packetByteBuffer.put(icmpType);       // ICMP type
      packetByteBuffer.put(icmpCode);       // ICMP code
      packetByteBuffer.putShort((short) 0); // Checksum, still unknown
      packetByteBuffer.put(paramProblem);   // Pointer to the problem in IP packet
      for(int unusedByteIndex = 0; unusedByteIndex <
	    PARAM_PROBLEM_UNUSED_LEN; unusedByteIndex++)
	packetByteBuffer.put((byte)0);      // Unused 3 bytes
      packetByteBuffer.put(ipHeaderBuffer); // IP packet's header bytes
      packetByteBuffer.put(ipBodyBytes, 0, ipBodyLength); // IP packets's 8 bytes of body

      // Set the super class's global packet buffer
      this.setByteBuffer(packetByteBuffer);

      // Calculate the checksum for the global buffer
      this.setChecksum();
      
      // Extract individual fields from the global buffer
      this.extractFromByteBuffer();

      break;

    default:
      throw new VNSUnknownICMPCodeException(code + " is not a known code");
    }
  }
  
  public static VNSICMPPacket wrapEchoReply(VNSICMPPacket echo_request){
  	VNSICMPPacket icmp = new VNSICMPPacket();
  	Point p = parseEcho(echo_request);
  	icmp.constructByteBufferEcho(VNSICMPType.ECHO_REPLY, p.x, p.y);
  	return icmp;
  }
/*
  private void constructByteBufferEchoReply(VNSICMPPacket icmpPacket)
  {
    byte icmpType = VNSICMPType.ECHO_REPLY;
    byte icmpCode = VNSICMPCode.NONE;
    
    int dataLen = icmpPacket.getLength() - (TYPE_LEN + CODE_LEN +
					    CHECKSUM_LEN);
    ByteBuffer oldPacketByteBuffer = icmpPacket.getByteBuffer();
    oldPacketByteBuffer.rewind();
    oldPacketByteBuffer.position(TYPE_LEN + CODE_LEN + CHECKSUM_LEN);

    // Data is original identifier + original sequence number + original data
    byte [] data = new byte[dataLen];
    oldPacketByteBuffer.get(data, 0, dataLen);

    ByteBuffer packetByteBuffer =
      ByteBuffer.allocate(icmpPacket.getLength());

    packetByteBuffer.put(icmpType);       // ICMP type
    packetByteBuffer.put(icmpCode);       // ICMP code
    packetByteBuffer.putShort((short) 0); // Checksum, still unknown
    packetByteBuffer.put(data);
    
    // Set the super class's global packet buffer
    this.setByteBuffer(packetByteBuffer);
    
    // Calculate the checksum for the global buffer
    this.setChecksum();
    
    // Extract individual fields from the global buffer
    this.extractFromByteBuffer();
  }
  
 */ 
  
  public static Point parseEcho(VNSICMPPacket icmp_packet){
  	byte[] data =  icmp_packet.getData();
    int firstByte = (0x000000FF & ((int)data[0]));
    int secondByte = (0x000000FF & ((int)data[1]));
    int identifier = (firstByte << 8 | secondByte);	
    
    firstByte = (0x000000FF & ((int)data[2]));
    secondByte = (0x000000FF & ((int)data[3]));
    int seq_num = (firstByte << 8 | secondByte);	
    return new Point(identifier, seq_num);
  }
  
  public static VNSICMPPacket wrapEcho(byte type, int identifier, int seq_num) {
  	VNSICMPPacket icmp = new VNSICMPPacket();
  	icmp.constructByteBufferEcho(type, identifier, seq_num);
  	return icmp;
  }

  public void constructByteBufferEcho(byte type, int identifier, int seq_num) {

    byte icmpCode = VNSICMPCode.NONE;

    ByteBuffer packetByteBuffer =  ByteBuffer.allocate(TYPE_LEN + CODE_LEN +
		    CHECKSUM_LEN + 4);

    packetByteBuffer.put(type);       // ICMP type
    packetByteBuffer.put(icmpCode);       // ICMP code
    packetByteBuffer.putShort((short) 0); // Checksum, still unknown
    packetByteBuffer.putShort((short) identifier);
    packetByteBuffer.putShort((short) seq_num);
    
    // Set the super class's global packet buffer
    this.setByteBuffer(packetByteBuffer);
    
    // Calculate the checksum for the global buffer
    this.setChecksum();
    
    // Extract individual fields from the global buffer
    this.extractFromByteBuffer();  	
  }
  /**
   * Sets the checksum of the ICMP packet to the supplied value.
   *
   * @param checksum Checksum for this packet.
   */
  private void setChecksum(short checksum)
  {
    m_packetByteBuffer.putShort(2*CHECKSUM_WORD_OFFSET, checksum);
  }

  /**
   * Sets the checksum to the internally calculated value.
   */
  private void setChecksum()
  {
    setChecksum(calculateChecksum());
  }

  /**
   * Calculates the checksum of this ICMP packet.
   *
   * The checksum field is the 16 bit one's complement of the one's
   * complement sum of all 16 bit words in the header.  For purposes of
   * computing the checksum, the value of the checksum field is zero.
   *
   * @return Checksum
   */
  public short calculateChecksum()
  {
    int sum = 0;
    
    m_packetByteBuffer.rewind();
    
    for(int wordCount = 0; wordCount < m_packetByteBuffer.capacity() / 2; wordCount++) {
      
      int word = (short) m_packetByteBuffer.getShort();
      word &= 0xffff; // Convert into an unsigned short
      
      // Ignore the checksum field
      if( wordCount == CHECKSUM_WORD_OFFSET )
	continue;
      
      sum += word;
      
      // If overflow occured
      if( (sum & 0xffff0000) != 0 ) {
	sum &= 0xffff;
	  sum++;
      }
    }
    
    return (short) ~(sum & 0xffff);
  }

  private void assignType()
  {
    m_typeBuffer.rewind();
    byte type = m_typeBuffer.get();
    m_type = VNSICMPType.convert(type);
  }

  /**
   * Returns the type of the ICMP packet.
   *
   * @return Type of the ICMP packet
   */
  public VNSICMPType getType(){  return m_type;}
  
  /** Returns any data from this ICMP packet */
  public byte[] getData() { return m_data; }
  
  public VNSICMPCode getCode() { return m_code; }

  /**
   * Represents the ICMP packet in a readable format, by field.
   *
   * @return Interface information in a readable format, by field
   */
  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();

    stringBuffer.append("ICMP packet length: " + m_length + "\n");
    stringBuffer.append("Type: " +
			getStringBuffer(m_typeBuffer).toString() + "\n");
    stringBuffer.append("Code: " +
			getStringBuffer(m_codeBuffer).toString() + "\n");
    stringBuffer.append("Checksum: " +
			getStringBuffer(m_checksumBuffer).toString());
      
    return stringBuffer.toString();
  }

  private ByteBuffer m_typeBuffer;
  private ByteBuffer m_codeBuffer;
  private ByteBuffer m_checksumBuffer;
  private VNSICMPType m_type;
  private VNSICMPCode m_code;
  private byte[] m_data;

  private static final int CHECKSUM_WORD_OFFSET = 1;
  
  private static final short TYPE_LEN           = 1;
  private static final short CODE_LEN           = 1;
  private static final short CHECKSUM_LEN       = 2;
  private static final short POINTER_LEN        = 1;
  private static final short GATEWAY_INET_ADDR  = 4;
  private static final short IDENTIFIER_LEN     = 2;
  private static final short SEQUENCE_NUM_LEN   = 2;
  private static final short ORIG_TIMESTAMP_LEN = 4;
  private static final short RECV_TIMESTAMP_LEN = 4;
  private static final short XMIT_TIMESTAMP_LEN = 4;

  private static final short UNREACH_UNUSED_LEN       = 4;
  private static final short TIME_EXCEEDED_UNUSED_LEN = 4;
  private static final short PARAM_PROBLEM_UNUSED_LEN = 3;

  private static final short IP_PACKET_BODY_LEN       = 8;

  public static final byte PARAM_PROBLEM_VER_AND_IHL = 0;
  public static final byte PARAM_PROBLEM_TOS         = 1;
  public static final byte PARAM_PROBLEM_TOTAL_LEN   = 2;
  public static final byte PARAM_PROBLEM_ID          = 4;
  public static final byte PARAM_PROBLEM_TTL         = 8;
  public static final byte PARAM_PROBLEM_PROTOCOL    = 9;
  public static final byte PARAM_PROBLEM_CHECKSUM    = 10;
}
