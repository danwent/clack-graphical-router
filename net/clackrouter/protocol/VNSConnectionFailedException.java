/**
 * VRConnectionFailedException.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.protocol;

/**
 * Encapsulates an exception that is thrown when a connection with the VNS
 * server fails.
 */
public class VNSConnectionFailedException extends Exception
{
  /**
   * Constructs connection failed exception.
   */
  public VNSConnectionFailedException()
  {
    super();
  }

  /**
   * Constructs connection failed exception.
   *
   * @param message Message pertaining to the exception
   */
  public VNSConnectionFailedException(String message)
  {
    super(message);
  }
}
