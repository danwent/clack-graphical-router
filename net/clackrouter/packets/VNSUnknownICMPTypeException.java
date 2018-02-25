/**
 * VRUnknownICMPTypeException.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;


/**
 * Encapsulates an exception that is thrown when a supplied ICMP type is
 * not defined by the ICMP protocol.
 */
public class VNSUnknownICMPTypeException extends Exception
{
  /**
   * Constucts an unknown ICMP type exception.
   */
  public VNSUnknownICMPTypeException()
  {
    super();
  }
  
  /**
   * Constucts an unknown ICMP type exception.
   *
   * @param message A message pertaining to the exception
   */
  public VNSUnknownICMPTypeException(String message)
  {
    super(message);
  }
}
