package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import net.clackrouter.protocol.VNSProtocolCharCoder;
import net.clackrouter.protocol.data.VNSClose;
import net.clackrouter.protocol.data.VNSData;



public class VNSCloseProtocolCommand extends VNSProtocolCommand
{
  public VNSCloseProtocolCommand(String errorMessage) throws Exception
  {
    super(VNSProtocolCommand.TYPE_CLOSE);

    m_close = new VNSClose(errorMessage);

    ByteBuffer closeCommandBuffer = ByteBuffer.allocate(ERRORMSG_LEN);

     closeCommandBuffer.put(VNSProtocolCharCoder.encode(errorMessage));

    
    // Contruct the command from this data
    constructByteBuffer(closeCommandBuffer);
  }

 public VNSCloseProtocolCommand(ByteBuffer commandBuffer)
  {
    super(commandBuffer);

    // Get the bytes that will contain the error message
    ByteBuffer closeCommandBuffer = extractByteBuffer();

    // Create the error message from the bytes
    if( closeCommandBuffer == null )
      m_close = new VNSClose("");
    else
      try {
      	m_close =  new VNSClose(VNSProtocolCharCoder.decode(closeCommandBuffer, true));
      } catch(CharacterCodingException cce) {
      	m_close = new VNSClose(DEFAULT_ERROR_MSG);
      }
  }

  public VNSData getData() { return m_close; }

  private static final int ERRORMSG_LEN = 256;

  public static final int MAX_LEN = ERRORMSG_LEN;

  private static final String DEFAULT_ERROR_MSG = "Unknown close message.";

  private VNSClose m_close;
}
