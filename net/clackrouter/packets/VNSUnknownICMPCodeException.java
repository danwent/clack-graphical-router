/**
 * VRUnknownICMPCodeException.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;


/**
 * Encapsulates an exception that is thrown when a supplied ICMP code is
 * not defined by the ICMP protocol.
 */
public class VNSUnknownICMPCodeException extends Exception
{
  /**
   * Constucts an unknown ICMP code exception.
   */
  public VNSUnknownICMPCodeException()
  {
    super();
  }
  
  /**
   * Constucts an unknown ICMP code exception.
   *
   * @param message A message pertaining to the exception
   */
  public VNSUnknownICMPCodeException(String message)
  {
    super(message);
  }
}
