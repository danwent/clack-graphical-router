package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;

import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.protocol.VNSProtocolCharCoder;
import net.clackrouter.protocol.data.VNSData;





public class VNSPacketProtocolCommand extends VNSProtocolCommand
{

  /**
   * Constructor for sending packets.
   *
   */
  public VNSPacketProtocolCommand(VNSPacket packet) throws Exception
  {
    super(VNSProtocolCommand.TYPE_PACKET);

    m_packet = packet;

    // Allocate memory for the command
    ByteBuffer packetCommandBuffer = ByteBuffer.allocate(IFACE_NAME_LEN +
							 m_packet.getLength());

    // Position the packet to be sent to the beginning
    ByteBuffer packetBuffer = packet.getByteBuffer();
    packetBuffer.rewind();


      packetCommandBuffer.put(VNSProtocolCharCoder.encode(packet.getOutputInterfaceName()));
      packetCommandBuffer.position(IFACE_NAME_LEN);
      packetCommandBuffer.put(packetBuffer);

    // Contruct the command from this data
    constructByteBuffer(packetCommandBuffer);
  }

  /**
   * Contructor for receiving packets.
   *
   */
  public VNSPacketProtocolCommand(ByteBuffer commandBuffer) throws Exception
  {
    super(commandBuffer);

    // Get the bytes that will contain interface name and the packet
    ByteBuffer packetCommandBuffer = extractByteBuffer();

    if( packetCommandBuffer == null || packetCommandBuffer.capacity() < IFACE_NAME_LEN )
      throw new VNSInvalidProtocolCommandException("Received packet is too short");

    // Prepare for reading from the beginning
    packetCommandBuffer.rewind();

    // Create interface name from appropriate bytes
    byte [] inputInterfaceBytes = new byte[IFACE_NAME_LEN];
    packetCommandBuffer.get(inputInterfaceBytes, 0, IFACE_NAME_LEN);

    String inputInterface = "";

      inputInterface = // Trim the interface name when decoding
	VNSProtocolCharCoder.decode(ByteBuffer.wrap(inputInterfaceBytes), true);


    // Create the packet from the rest of the bytes
    byte [] packetBytes = new byte[packetCommandBuffer.capacity() - IFACE_NAME_LEN];
    packetCommandBuffer.get(packetBytes);
    
    ByteBuffer packetBuffer = ByteBuffer.wrap(packetBytes);

    // Should really check that an Ethernet packet was received thru type lookup
    m_packet = new VNSEthernetPacket(packetBuffer);
    m_packet.setInputInterfaceName(inputInterface);
  }
  
  public VNSData getData() { return m_packet; }

  private static final int IFACE_NAME_LEN = 16;

  public static final int MAX_LEN = IFACE_NAME_LEN + VNSEthernetPacket.MAX_LEN;

	private VNSPacket m_packet;
}
