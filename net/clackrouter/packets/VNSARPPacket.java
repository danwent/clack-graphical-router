/**
 * VRARPPacket.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;

import java.nio.ByteBuffer;



/**
 * Encapsulates Address Resolution Protocol packet.
 */
public class VNSARPPacket extends VNSPacket
{
  /**
   * Constructs ARP packet from the supplied bytes.
   *
   * @param packetByteBuffer Buffer containing packet's bytes
   */
  public VNSARPPacket(ByteBuffer packetByteBuffer)
  {
    super(packetByteBuffer);

    extractFromByteBuffer();
  }

  /**
   * Creates ARP packet from the supplied parameters.
   *
   * @param hwType Hardware type
   * @param protocolType Protocol type
   * @param hLen Hardware address length
   * @param pLen Protocol address length
   * @param operation Operation (e.g. Request)
   * @param sourceHWAddress Hardware address of the source host
   * @param sourceProtocolAddress Protocol address of the source host (e.g. IP address)
   * @param destinationHWAddress Harware address of the destination host
   * @param destinationProtocolAddress Protocol address of the destination host
   */
  public VNSARPPacket(short hwType, short protocolType, byte hLen,
		     byte pLen, short operation, 
		     ByteBuffer sourceHWAddress, ByteBuffer sourceProtocolAddress,
		     ByteBuffer destinationHWAddress, ByteBuffer destinationProtocolAddress)
  {
    super();
    
    contructByteBuffer(hwType, protocolType, hLen,
		       pLen, operation, 
		       sourceHWAddress, sourceProtocolAddress,
		       destinationHWAddress, destinationProtocolAddress);
  }

  private void extractFromByteBuffer()
  {
    byte [] hwType = new byte[HW_TYPE_LEN];
    byte [] protocolType = new byte[PROTOCOL_TYPE_LEN];
    byte [] hLen = new byte[HLEN_LEN];
    byte [] pLen = new byte[PLEN_LEN];
    byte [] operation = new byte [OPERATION_LEN];
    byte [] sourceHWAddr = new byte[HW_ADDR_LEN];
    byte [] sourceProtocolAddr = new byte[PROTOCOL_ADDR_LEN];
    byte [] destinationHWAddr = new byte[HW_ADDR_LEN];
    byte [] destinationProtocolAddr = new byte[PROTOCOL_ADDR_LEN];

    m_packetByteBuffer.rewind();

    // Extract hardware type
    m_packetByteBuffer.get(hwType, 0, HW_TYPE_LEN);
    m_hwType = ByteBuffer.wrap(hwType);

    // Extract protocol type
    m_packetByteBuffer.get(protocolType, 0, PROTOCOL_TYPE_LEN);
    m_protocolType = ByteBuffer.wrap(protocolType);

    // Extract HLEN
    m_packetByteBuffer.get(hLen, 0, HLEN_LEN);
    m_hLen = ByteBuffer.wrap(hLen);

    // Extract PLEN
    m_packetByteBuffer.get(pLen, 0, PLEN_LEN);
    m_pLen = ByteBuffer.wrap(pLen);

    // Extract operation
    m_packetByteBuffer.get(operation, 0, OPERATION_LEN);
    m_operation = ByteBuffer.wrap(operation);

    // Extract source hardware address
    m_packetByteBuffer.get(sourceHWAddr, 0, HW_ADDR_LEN);
    m_sourceHWAddr = ByteBuffer.wrap(sourceHWAddr);

    // Extract source protocol address
    m_packetByteBuffer.get(sourceProtocolAddr, 0, PROTOCOL_ADDR_LEN);
    m_sourceProtocolAddr = ByteBuffer.wrap(sourceProtocolAddr);

    // Extract destination hardware address
    m_packetByteBuffer.get(destinationHWAddr, 0, HW_ADDR_LEN);
    m_destinationHWAddr = ByteBuffer.wrap(destinationHWAddr);

    // Extract destination protocol address
    m_packetByteBuffer.get(destinationProtocolAddr, 0, PROTOCOL_ADDR_LEN);
    m_destinationProtocolAddr = ByteBuffer.wrap(destinationProtocolAddr);

    //System.err.println(this);
  }

  private void contructByteBuffer(short hwType, short protocolType, byte hLen,
				  byte pLen, short operation, 
				  ByteBuffer sourceHWAddress,
				  ByteBuffer sourceProtocolAddress,
				  ByteBuffer destinationHWAddress,
				  ByteBuffer destinationProtocolAddress)
  {
      sourceHWAddress.rewind();
      sourceProtocolAddress.rewind();
      destinationHWAddress.rewind();
      destinationProtocolAddress.rewind();

      ByteBuffer packetByteBuffer = ByteBuffer.allocate(TOTAL_LEN);

      packetByteBuffer.putShort(hwType);
      packetByteBuffer.putShort(protocolType);
      packetByteBuffer.put(hLen);
      packetByteBuffer.put(pLen);
      packetByteBuffer.putShort(operation);
      packetByteBuffer.put(sourceHWAddress);
      packetByteBuffer.put(sourceProtocolAddress);
      packetByteBuffer.put(destinationHWAddress);
      packetByteBuffer.put(destinationProtocolAddress);

      // Set the super class's global packet buffer
      this.setByteBuffer(packetByteBuffer);

      // Extract individual fields from the global buffer
      this.extractFromByteBuffer();
  }

  /**
   * Presents ARP packet in a readable format, by field.
   *
   * @return Fields of the ARP packet
   */
  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();

    stringBuffer.append("ARP packet length: " + m_length + "\n");
    stringBuffer.append("Hardware type: " +
			getStringBuffer(m_hwType).toString() + "\n");
    stringBuffer.append("Protocol type: " +
			getStringBuffer(m_protocolType).toString() + "\n");
    stringBuffer.append("HLEN: " +
			getStringBuffer(m_hLen).toString() + "\n");
    stringBuffer.append("PLEN: " +
			getStringBuffer(m_pLen).toString() + "\n");
    stringBuffer.append("Operation: " +
			getStringBuffer(m_operation).toString() + "\n");
    stringBuffer.append("Source hw addr: " +
			getStringBuffer(m_sourceHWAddr).toString() + "\n");
    stringBuffer.append("Source protocol addr: " +
			getStringBuffer(m_sourceProtocolAddr).toString() + "\n");
    stringBuffer.append("Destination hw addr: " +
			getStringBuffer(m_destinationHWAddr).toString() + "\n");
    stringBuffer.append("Destination protocol addr: " +
			getStringBuffer(m_destinationProtocolAddr).toString());

    return stringBuffer.toString();
  }

  /**
   * Returns hardware type.
   *
   * @return Hardware type
   */
  public short getHWType()
  {
    m_hwType.rewind();
    return m_hwType.getShort();
  }

  /**
   * Returns protocol type.
   *
   * @return Protocol type
   */
  public short getProtocolType()
  {
    m_protocolType.rewind();
    return m_protocolType.getShort();
  }

  /**
   * Returns hardware address length.
   *
   * @return Hardware address length
   */
  public byte getHWAddressLength()
  {
    m_hLen.rewind();
    return m_hLen.get();
  }

  /**
   * Returns protocol address length.
   *
   * @return Protocol address length
   */
  public byte getProtocolAddressLength()
  {
    m_pLen.rewind();
    return m_pLen.get();
  }

  /**
   * Returns operation (Request, Reply, etc.)
   *
   * @return Operation
   */
  public short getOperation()
  {
    m_operation.rewind();
    return m_operation.getShort();
  }

  /**
   * Returns source protocol address.
   *
   * @return Source protocol address
   */
  public ByteBuffer getSourceProtocolAddress()
  {
    m_sourceProtocolAddr.rewind();
    return m_sourceProtocolAddr;
  }
  /**
   * Returns destination protocol address.
   *
   * @return Destination protocol address
   */
  public ByteBuffer getDestinationProtocolAddress()
  {
    m_destinationProtocolAddr.rewind();
    return m_destinationProtocolAddr;
  }
  
  /**
   * Returns source hardware address.
   *
   * @return Source hardware address
   */
  public ByteBuffer getSourceHWAddress()
  {
    m_sourceHWAddr.rewind();
    return m_sourceHWAddr;
  }

  /**
   * Returns destination hardware address.
   *
   * @return Destination hardware address
   */
  public ByteBuffer getDestinationHWAddress()
  {
    m_destinationHWAddr.rewind();
    return m_destinationHWAddr;
  }
  
  // Lengths of fields of the ARP packet

  /**
   * Length of the harware type field, in bytes.
   */
  public static final int HW_TYPE_LEN       = 2; // Hardware type

  /**
   * Length of the protocol type field, in bytes.
   */
  public static final int PROTOCOL_TYPE_LEN = 2; // Protocol type

  /**
   * Length of the harware address length field, in bytes.
   */
  public static final int HLEN_LEN          = 1; // Hardware address length

  /**
   * Length of the protocol address length field, in bytes.
   */
  public static final int PLEN_LEN          = 1; // Protocol address length

  /**
   * Length of the operation field, in bytes.
   */
  public static final int OPERATION_LEN     = 2; // Operation (Request, Reply, etc.)

  /**
   * Length of the harware address field, in bytes.
   */
  public static final int HW_ADDR_LEN       = 6; // Source, destination hardware address

  /**
   * Length of the protocol address field, in bytes.
   */
  public static final int PROTOCOL_ADDR_LEN = 4; // Source, destination Protocol address

  /**
   * Total length of the ARP packet, in bytes.
   */
  public static final int TOTAL_LEN         =
    HW_TYPE_LEN + PROTOCOL_TYPE_LEN + HLEN_LEN + PLEN_LEN +
    OPERATION_LEN + 2*HW_ADDR_LEN + 2*PROTOCOL_ADDR_LEN;

  // Operations
  /**
   * Value of the operation fieled of ARP request.
   */
  public static final short OPERATION_REQUEST         = 1;

  /**
   * Value of the operation fieled of ARP reply.
   */
  public static final short OPERATION_REPLY           = 2;

  /**
   * Value of the operation fieled of RARP request.
   */
  public static final short OPERATION_REVERSE_REQUEST = 3;

  /**
   * Value of the operation fieled of RARP reply.
   */
  public static final short OPERATION_REVERSE_REPLY   = 4;

  /**
   * Value of the hardware type field of Ethernet.
   */
  public static final short HW_TYPE_ETHERNET = 1;
  
  /**
   * Value of the protocol type field of IP.
   */
  public static final short PROTOCOL_TYPE_IP = 0x800;

  // Fields of the ARP packet
  private ByteBuffer m_hwType;                  // Hardware type
  private ByteBuffer m_protocolType;            // Protocol type
  private ByteBuffer m_hLen;                    // Hardware address length
  private ByteBuffer m_pLen;                    // Protocol address length
  private ByteBuffer m_operation;               // Operation
  private ByteBuffer m_sourceHWAddr;            // Source hardware address
  private ByteBuffer m_sourceProtocolAddr;      // Source protocol address
  private ByteBuffer m_destinationHWAddr;       // Destination hardware address
  private ByteBuffer m_destinationProtocolAddr; // Destination protocol address
}
