/**
 * VRPacketDepartureListener.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;


/**
 * Listener inteface for receiving an event when a packet departs from a
 * box. The GUI is the natural listener for the packet departure events if
 * it wants to do an animation of packet departures, for example.
 *
 * @see VNSPacketArrivalListener
 */
public interface VNSPacketDepartureListener
{
  /**
   * Invoked when a packet departs from a box.
   *
   * @param pde Packet arrival event
   */
  public void packetDeparted(VNSPacketDepartureEvent pde);
}
