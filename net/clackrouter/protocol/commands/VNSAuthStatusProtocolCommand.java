package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;

import net.clackrouter.protocol.VNSProtocolCharCoder;

public class VNSAuthStatusProtocolCommand extends VNSProtocolCommand {
	
	public VNSAuthStatusProtocolCommand(ByteBuffer commandBuffer) throws Exception {
	    super(commandBuffer);
		ByteBuffer b = extractByteBuffer();
		b.rewind();
		// This ignores that the first byte should be indicating success/failure.
		// Right now, that doesn't matter because the VNS server implementation appears
		// to send a VNSClose, not a VNSAuthStatus if authentication fails.  As a result, 
		// we can assume that any VNSAuthStatus message is a positive one.  
		m_msg = new String(b.array()); 

	}

	public String getMessage() { return m_msg; } 
	
	private String m_msg; 
}
