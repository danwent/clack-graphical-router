package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;

import net.clackrouter.protocol.data.VNSData;


public abstract class VNSProtocolCommand
{
  /**
   * Constructor for sending the command. A subsequent call to
   * contructBytes is necessary to build a valid command in preparation
   * for sending it to the server.
   */
  protected VNSProtocolCommand(int type)
  {
    m_type = type;
  }

  /**
   * Constructor for receiving the command. A subsequent call to
   * extractByteBuffer is necessary to allow the subclass to extract the
   * specific information contained in the command.
   */
  protected VNSProtocolCommand(ByteBuffer commandBuffer)
  {
    m_commandBuffer = commandBuffer;

    m_commandBuffer.rewind();
    m_length = m_commandBuffer.getInt();
    m_type = m_commandBuffer.getInt();
  }

  /**
   * Contruct the bytes that will be ready to be sent to the server by
   * prepending length and type to the data passed in from the subclass.
   */
  protected final void constructByteBuffer(ByteBuffer commandBuffer)
  {
    commandBuffer.rewind();

    m_length = LENGTH_LEN + TYPE_LEN + commandBuffer.capacity();
    m_commandBuffer = ByteBuffer.allocate(m_length);

    m_commandBuffer.putInt(m_length);
    m_commandBuffer.putInt(m_type);
    m_commandBuffer.put(commandBuffer);
  }

  /**
   * Contruct the bytes that will be ready to be sent to the server by
   * prepending the length and the type. This methods should be called
   * by a subclass that has no payload to send the the server.
   */
  protected final void constructByteBuffer()
  {
    m_length = LENGTH_LEN + TYPE_LEN;
    m_commandBuffer = ByteBuffer.allocate(m_length);

    m_commandBuffer.putInt(m_length);
    m_commandBuffer.putInt(m_type);
  }

  /**
   * Extract the bytes that are relevant to the subclass. This means that the
   * returned byte buffer will not contain the length and type information.
   */
  protected final ByteBuffer extractByteBuffer()
  {
    int length = m_length - LENGTH_LEN - TYPE_LEN;
    ByteBuffer commandBuffer;

    m_commandBuffer.rewind();

    // If no data is going to the subclass, return null
    if( length <= 0 )
      commandBuffer = null;
    else {
      byte [] commandBytes = new byte[length];
      m_commandBuffer.position(LENGTH_LEN + TYPE_LEN);
      m_commandBuffer.get(commandBytes, 0, length);
      commandBuffer = ByteBuffer.wrap(commandBytes);
    }
    
    return commandBuffer;
  }
  
  /**
   * Return the type of the command.
   *
   * @see #TYPE_OPEN
   * @see #TYPE_CLOSE
   * @see #TYPE_PACKET
   * @see #TYPE_BANNER
   * @see #TYPE_HWINFO
   */
  public int getType() { return m_type; }

  /**
   * Return the length of the command, in bytes, which is the length of the
   * bytes sent by the subclass plus "length length" plus "type length".
   *
   */
  public int getLength() { return m_length; }

  /**
   * Get all the bytes of this command. The returned bytes are ready to be
   * transmitted to the server.
   */
  public ByteBuffer getByteBuffer() { return m_commandBuffer; }
  
  public VNSData getData() {
  	return null;
  }

  public String toString()
  {
    StringBuffer commandStringBuffer = new StringBuffer();
    
    m_commandBuffer.rewind();

    commandStringBuffer.append("[ ");
    for(int i=0; i<m_commandBuffer.capacity(); i++)
      commandStringBuffer.append(m_commandBuffer.get() + " ");
    commandStringBuffer.append("]");

    return commandStringBuffer.toString();
  }

  public static final int LENGTH_LEN = 4;
  public static final int TYPE_LEN   = 4;

  // All supported command types
  public static final int TYPE_INVALID = 0; // Not supported, for error checking only
  public static final int TYPE_OPEN    = 1;
  public static final int TYPE_CLOSE   = 2;
  public static final int TYPE_PACKET  = 4;
  public static final int TYPE_BANNER  = 8;
  public static final int TYPE_HWINFO  = 16;
  public static final int TYPE_RTABLE  = 32;
  public static final int TYPE_OPENTEMPLATE  = 64;
  public static final int TYPE_AUTHREQUEST  = 128;
  public static final int TYPE_AUTHREPLY  = 256;
  public static final int TYPE_AUTHSTATUS  = 512;
  
  protected int m_type;               // Type of the command, listed above
  private ByteBuffer m_commandBuffer; // Content of the command
  private int m_length;               // Length of the buffer, in bytes
  
}
