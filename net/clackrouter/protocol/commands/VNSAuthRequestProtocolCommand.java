package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;

import net.clackrouter.protocol.VNSProtocolCharCoder;

public class VNSAuthRequestProtocolCommand extends VNSProtocolCommand {
	
	public VNSAuthRequestProtocolCommand(ByteBuffer commandBuffer) throws Exception {
	    super(commandBuffer);

		m_salt  = extractByteBuffer();
		// Contruct the command from this data
		constructByteBuffer(m_salt);
	}

	public ByteBuffer getSalt() { return m_salt; } 
	
	private ByteBuffer m_salt;
}
