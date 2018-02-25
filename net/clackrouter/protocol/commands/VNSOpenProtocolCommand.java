package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;

import net.clackrouter.protocol.VNSProtocolCharCoder;




public class VNSOpenProtocolCommand extends VNSProtocolCommand
{
  public VNSOpenProtocolCommand(short topologyId, String virtualHostId,
			       String userId) throws Exception 
  {
    super(VNSProtocolCommand.TYPE_OPEN);

    m_topologyId = topologyId;
    m_pad = 0;
    m_virtualHostId = virtualHostId;
    m_userId = userId;

    ByteBuffer openCommandByteBuffer = 
      ByteBuffer.allocate(MAX_LEN);

      openCommandByteBuffer.putShort(m_topologyId);
      openCommandByteBuffer.putShort(m_pad);
      openCommandByteBuffer.put(VNSProtocolCharCoder.encode(m_virtualHostId));
      openCommandByteBuffer.position(TOPOLOGY_ID_LEN + PAD_LEN + VHOST_ID_LEN);
      openCommandByteBuffer.put(VNSProtocolCharCoder.encode(m_userId));
      // password field can be empty

    // Contruct the command from this data
    constructByteBuffer(openCommandByteBuffer);
  }

  private static final int TOPOLOGY_ID_LEN = 2;
  private static final int PAD_LEN         = 2;
  private static final int VHOST_ID_LEN    = 32;
  private static final int USER_ID_LEN     = 32;
  private static final int PASSWORD_LEN     = 32;
  
  public static final int MAX_LEN =
    TOPOLOGY_ID_LEN + PAD_LEN + VHOST_ID_LEN + USER_ID_LEN + PASSWORD_LEN;
  
  private short m_topologyId;
  private short m_pad;
  private String m_virtualHostId;
  private String m_userId;
}
