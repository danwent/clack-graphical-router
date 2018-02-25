/**
 * VREthernetPacket.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;

import java.nio.ByteBuffer;


/**
 * Encapsulates an Ethernet frame.
 */
public final class VNSEthernetPacket extends VNSPacket
{
  /**
   * Constructs an Ethernet frame from the supplied byte buffer.
   *
   * @param packetByteBuffer Buffer of bytes representing an Ethernet frame
   */
  public VNSEthernetPacket(ByteBuffer packetByteBuffer)
  {
    super(packetByteBuffer);

    extractFromByteBuffer();

    //System.err.println(this);
  }

  private void extractFromByteBuffer()
  {
    byte [] dstAddr = new byte[ADDR_LEN];
    byte [] srcAddr = new byte[ADDR_LEN];
    byte [] type = new byte[TYPE_LEN];
    byte [] body = new byte[m_length - 2*ADDR_LEN - TYPE_LEN];

    m_packetByteBuffer.rewind();
    
    // Extract destination address
    m_packetByteBuffer.get(dstAddr, 0, ADDR_LEN);
    m_dstAddr = ByteBuffer.wrap(dstAddr);
    
    // Extract source address
    m_packetByteBuffer.get(srcAddr, 0, ADDR_LEN);
    m_srcAddr = ByteBuffer.wrap(srcAddr);
    
    // Extract type
    m_packetByteBuffer.get(type, 0, TYPE_LEN);
    m_type = ByteBuffer.wrap(type);
    
    // Extract body
    m_packetByteBuffer.get(body);
    m_body = ByteBuffer.wrap(body);
  }

  /**
   * Constructs an Ethernet frame from individual fields.
   *
   *
   * @param packet Any packet except an Ethernet frame
   * @param dstAddr Destination hardware address
   * @param srcAddr Source hardware address
   * @param type Type of the higher-level protocol
   */
  private VNSEthernetPacket(VNSPacket packet, ByteBuffer dstAddr,
			   ByteBuffer srcAddr, ByteBuffer type)
  {
    constructByteBuffer(packet.getByteBuffer(), dstAddr, srcAddr, type);
  }

  private void constructByteBuffer(ByteBuffer body, ByteBuffer dstAddr,
				   ByteBuffer srcAddr, ByteBuffer type)
  {
    // Allocate memory for the entire contents of the packet
    ByteBuffer packetBuffer = ByteBuffer.allocate(dstAddr.capacity() +
						  srcAddr.capacity() +
						  TYPE_LEN +
						  body.capacity());

    // Assign member variables
    m_dstAddr = dstAddr;
    m_srcAddr = srcAddr;
    m_type = type;
    m_body = body;
    
    m_dstAddr.rewind();
    m_srcAddr.rewind();
    m_type.rewind();
    m_body.rewind();

    // Put fields in the packet buffer
    packetBuffer.put(m_dstAddr);
    packetBuffer.put(m_srcAddr);
    packetBuffer.put(m_type);
    packetBuffer.put(m_body);

    // Set super class's packet buffer
    setByteBuffer(packetBuffer);
  }

  public void pack()
  {
    // Allocate memory for the entire contents of the packet
    ByteBuffer packetBuffer = ByteBuffer.allocate(m_dstAddr.capacity() +
						  m_srcAddr.capacity() +
						  m_type.capacity() +
						  m_body.capacity());
    m_dstAddr.rewind();
    m_srcAddr.rewind();
    m_type.rewind();
    m_body.rewind();

    // Put fields in the packet buffer
    packetBuffer.put(m_dstAddr);
    packetBuffer.put(m_srcAddr);
    packetBuffer.put(m_type);
    packetBuffer.put(m_body);

    // Set super class's packet buffer
    setByteBuffer(packetBuffer);
  }

  /**
   * Returns destination hardware address.
   *
   * @return Byte buffer containing the destination hardware address
   */
  public ByteBuffer getDestinationAddressBuffer() { return m_dstAddr; }

  /**
   * Returns source hardware address.
   *
   * @return Byte buffer containing the source hardware address
   */
  public ByteBuffer getSourceAddressBuffer() { return m_srcAddr; }

  /**
   * Returns type of the higher-level protocol.
   *
   * @return Byte buffer containing the type of the higher-level protocol
   *
   * @see #getType
   */
  public ByteBuffer getTypeBuffer() { return m_type; }

  /**
   * Returns the body of the Ethernet frame.
   *
   * @return Byte buffer containing the body of the Ethernet frame
   */
  public ByteBuffer getBodyBuffer() { return m_body; }

  /**
   * Returns type of the higher-level protocol.
   *
   * @return Type of the higher-level protocol
   *
   * @see #getTypeBuffer
   */
  public short getType()
  {
    m_type.rewind();
    return m_type.getShort();
  }

  /**
   * Sets the body of the Ethernet frame to the supplied byte buffer.
   *
   * @param body Byte buffer containing the body of the Ethernet frame
   *
   * @see #getBodyBuffer
   */
  public void setBodyBuffer(ByteBuffer body)
  {
    body.rewind();
    m_body.clear();
    m_body = body;
  }

  /**
   * Sets the source hardware address of the Ethernet frame to the
   * supplied byte buffer.
   *
   * @param srcAddr Byte buffer containing the source hardware address of the
   *                Ethernet frame
   */
  public void setSourceAddressBuffer(ByteBuffer srcAddr)
  {
    srcAddr.rewind();
    m_srcAddr.clear();
    m_srcAddr = srcAddr;
  }

  /**
   * Sets the destination hardware address of the Ethernet frame to the
   * supplied byte buffer.
   *
   * @param dstAddr Byte buffer containing the destination hardware address of the
   *                Ethernet frame
   */
  public void setDestinationAddressBuffer(ByteBuffer dstAddr)
  {
    dstAddr.rewind();
    m_dstAddr.clear();
    m_dstAddr = dstAddr;
  }

  /**
   * Constructs an Ethernet packet from its individual fields.
   *
   * @param packet Any packet except an Ethernet frame
   * @param dstAddr Destination hardware address
   * @param srcAddr Source hardware address
   * @param type Type of the higher-level protocol   *
   */
  public static VNSEthernetPacket wrap(VNSPacket packet, ByteBuffer dstAddr,
				      ByteBuffer srcAddr, ByteBuffer type)
    throws VNSInvalidPacketException
  {
    if( packet instanceof VNSEthernetPacket )
      throw new VNSInvalidPacketException("Ethernet packet cannot wrap itself");

    return new VNSEthernetPacket(packet, dstAddr, srcAddr, type);
  }

  /**
   *  Presents an Ethernet frame in a readable format, by field.
   *
   * @return The fields of the Ethernet frame
   */
  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();

    stringBuffer.append("Ethernet packet:" + "\n");
    stringBuffer.append("Length: " + m_length + "\n");
    stringBuffer.append("Dest addr: " +
			getStringBuffer(m_dstAddr).toString() + "\n");
    stringBuffer.append("Source addr: " +
			getStringBuffer(m_srcAddr).toString() + "\n");
    stringBuffer.append("Type: " +
			getStringBuffer(m_type).toString() + "\n");
    stringBuffer.append("Body: " +
			getStringBuffer(m_body).toString() + "\n");
    stringBuffer.append(super.toString());

    return stringBuffer.toString();
  }

  public static ByteBuffer getBroadcastAddress()
  {
    ByteBuffer byteBuffer = ByteBuffer.allocate(ADDR_LEN);
    
    for(int byteIndex = 0; byteIndex < ADDR_LEN; byteIndex++)
      byteBuffer.put((byte) 255);

    return byteBuffer;
  }

  // Fields of this Ethernet frame

  /**
   * Length of source and destination address fields.
   */
  public static final int ADDR_LEN = 6;

  /**
   * Length of the type field.
   */
  public static final int TYPE_LEN = 2;

  /**
   * Length of the body.
   */
  public static final int BODY_LEN = 1500;

  // Total length of this Ethernet packet
  /**
   * Total maximum length of an Ethernet frame.
   */
  public static final int MAX_LEN = 2*ADDR_LEN + TYPE_LEN + BODY_LEN;

  // Values that the type field can take
  /**
   * Value of the type field if higher-level protocol is IP.
   */
  public static final short TYPE_IP  = 0x800;

  /**
   * Value of the type field if higher-level protocol is ARP.
   */
  public static final short TYPE_ARP = 0x806;

  private ByteBuffer m_dstAddr; // Destination address
  private ByteBuffer m_srcAddr; // Source address
  private ByteBuffer m_type;    // Type of higher-level protocol, e.g. IP
  private ByteBuffer m_body;    // The payload of this packet, e.g. IP packet

  
  public VNSPacket getPayload() 
  {
      if (getType() == TYPE_IP){
          IPPacket newip = new IPPacket(getBodyBuffer());
          newip.setParentHeader(this);
          return newip; 
      }else if(getType() == TYPE_ARP){
          VNSARPPacket newarp = new VNSARPPacket(getBodyBuffer());
          newarp.setParentHeader(this);
          return newarp; 
      }else{
          return null;
      }
  } // -- getPayload(..)

}
