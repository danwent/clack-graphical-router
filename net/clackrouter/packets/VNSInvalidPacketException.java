/**
 * VRInvalidPacketException.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;


/**
 * Encapsulates an exception that is thrown when a packet supplied to a
 * constuctor or method is not of corrent type.
 */
public class VNSInvalidPacketException extends Exception
{
  /**
   * Constucts an invalid packet exception.
   */
  public VNSInvalidPacketException()
  {
    super();
  }

  /**
   * Constucts an invalid packet exception.
   *
   * @param message A message pertaining to the exception
   */
  public VNSInvalidPacketException(String message)
  {
    super(message);
  }
}
