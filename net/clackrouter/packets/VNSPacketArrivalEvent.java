/**
 * VRPacketArrivalEvent.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;

import java.util.EventObject;

/**
 * Encapsulates an event that is sent to all interested parties when a
 * packet arrives at a particular destination, for example the box input port,
 * the box itself, or the box output port.
 *
 * @see VNSPacketArrivalListener
 */
public class VNSPacketArrivalEvent extends EventObject
{
  /**
   * Constructs communcation object event.
   *
   * @param source Object that is sending this event
   * @param packet The packet to be sent
   */
  public VNSPacketArrivalEvent(Object source, VNSPacket packet)
  {
    super(source);

    m_packet = packet;
  }

  /**
   * Returns the packet object associated with the event.
   *
   * @return Packet
   */
  public VNSPacket getPacket() { return m_packet; }

  protected VNSPacket m_packet;
}
