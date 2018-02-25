/**
 * VRClose.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.protocol.data;



/**
 * Encapsulates a Close message sent from the server to the client. Upon
 * receiving this message, the client must shut down.
 *
 * @see net.clackrouter.protocol.commands.VNSCloseProtocolCommand
 */
public class VNSClose extends VNSData
{
	
	public boolean isClose() { return true; }
	
  /**
   * Constructs a Close message, which forces the client to shut down.
   *
   * @param message An error message from the server
   */
  
  public VNSClose(String message)
  {
    super();

    m_message = message;
  }

  /**
   * Returns the server message.
   *
   * @return Server message
   */
  public String toString()
  {
    return m_message;
  }

  private String m_message;
}
