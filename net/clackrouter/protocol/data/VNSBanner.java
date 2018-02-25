/**
 * VRBanner.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.protocol.data;


/**
 * Encapsulates a message sent from the server to the client.
 *
 * @see net.clackrouter.protocol.commands.VNSBannerProtocolCommand
 */
public class VNSBanner extends VNSData
{
	
	public boolean isBanner() { return true; }
	
  /**
   * Constucts a banner message.
   *
   * @param message Information or warning message from the server
   */
  public VNSBanner(String message)
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
