/**
 * VRPacketArrivalListener.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;

/**
 * Listener inteface for receiving an event when a packet arrives at a
 * box. The GUI is the natural listener for the packet arrival events if
 * it wants to do an animation of packet arrivals, for example.
 *
 * @see VNSPacketDepartureListener
 */
public interface VNSPacketArrivalListener
{
  /**
   * Invoked when a packet arrives at a box.
   *
   * @param pae Packet arrival event
   */
  public void packetArrived(VNSPacketArrivalEvent pae);
}

