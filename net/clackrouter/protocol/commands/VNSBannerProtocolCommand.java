package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import net.clackrouter.protocol.VNSProtocolCharCoder;
import net.clackrouter.protocol.data.VNSBanner;
import net.clackrouter.protocol.data.VNSData;




public class VNSBannerProtocolCommand extends VNSProtocolCommand 
{
  public VNSBannerProtocolCommand(String errorMessage)throws Exception
  {
    super(VNSProtocolCommand.TYPE_BANNER);

    m_banner = new VNSBanner(errorMessage);

    ByteBuffer bannerCommandBuffer = ByteBuffer.allocate(ERRORMSG_LEN);


    bannerCommandBuffer.put(VNSProtocolCharCoder.encode(errorMessage));

    
    // Contruct the command from this data
    constructByteBuffer(bannerCommandBuffer);
  }

  public VNSBannerProtocolCommand(ByteBuffer commandBuffer)
  {
    super(commandBuffer);

    // Get the bytes that will contain the error message
    ByteBuffer bannerCommandBuffer = extractByteBuffer();

    // Create the error message from the bytes
    if( bannerCommandBuffer == null )
      m_banner = new VNSBanner("");
    else
      try {
	m_banner = 
	  new VNSBanner(VNSProtocolCharCoder.decode(bannerCommandBuffer, true));
      } catch(CharacterCodingException cce) {
	m_banner = new VNSBanner(DEFAULT_ERROR_MSG);
      }
  }

  public VNSData getData() { return m_banner; }

  private static final int ERRORMSG_LEN = 256;

  public static final int MAX_LEN = ERRORMSG_LEN;

  private static final String DEFAULT_ERROR_MSG = "Unknown banner message.";

  private VNSBanner m_banner;
}
