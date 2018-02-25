/**
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;

import java.util.EventObject;

/**
 * Encapsulates an event that is sent to all interested parties when a
 * packet leaves a particular destination, for example the box input port,
 * the box itself, or the box output port.
 *
 * @see VNSPacketDepartureListener
 */
public class VNSPacketDepartureEvent extends EventObject
{
  /**
   * Constructs communcation object event.
   *
   * @param source Object that is sending this event
   * @param packet The packet to be sent
   */
  public VNSPacketDepartureEvent(Object source, VNSPacket packet)
  {
    super(source);

    m_packet = packet;
  }

  /**
   * Returns the packet associated with the event.
   *
   * @return Pakcet
   */
  public VNSPacket getPacket() { return m_packet; }

  protected VNSPacket m_packet;
}
