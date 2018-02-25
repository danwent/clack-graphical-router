package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;

import net.clackrouter.protocol.data.VNSData;
import net.clackrouter.protocol.data.VNSHWInfo;



/**
 * Command for getting hardware configuration information.
 *
 */
public class VNSHWInfoProtocolCommand extends VNSProtocolCommand
{
  public VNSHWInfoProtocolCommand(ByteBuffer commandBuffer) throws Exception
  {
    super(commandBuffer);

    // Get the bytes that will contain hw info tuples
    ByteBuffer hwInfoCommandBuffer = extractByteBuffer();
    
    if( hwInfoCommandBuffer == null )
      throw new VNSInvalidProtocolCommandException("Received hardware info is too short");
    
    m_hwInfo = new VNSHWInfo(hwInfoCommandBuffer);
  }

  public VNSData getData() { return m_hwInfo; }
  
  private VNSHWInfo m_hwInfo;
}
