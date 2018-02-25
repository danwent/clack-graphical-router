package net.clackrouter.protocol.commands;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import net.clackrouter.protocol.VNSProtocolCharCoder;

public class VNSAuthReplyProtocolCommand extends VNSProtocolCommand {
	
	public VNSAuthReplyProtocolCommand(String username, ByteBuffer salt, String auth_key) throws Exception {
		super(VNSProtocolCommand.TYPE_AUTHREPLY);

		MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte [] salt_array = salt.array(); 

        md.update(salt_array);
        md.update(auth_key.getBytes(), 0, auth_key.length()); 
        byte[] sha1hash = md.digest();
        
		// Contruct the command from this data
        byte[] username_arr = username.getBytes(); 
        byte[] auth_key_arr = auth_key.getBytes(); 
        
        ByteBuffer replyCommandByteBuffer = 
            ByteBuffer.allocate(4 + username_arr.length + sha1hash.length); 

        replyCommandByteBuffer.putInt(username.length());
        replyCommandByteBuffer.put(username_arr);
        replyCommandByteBuffer.put(sha1hash);

        // Contruct the command from this data
        constructByteBuffer(replyCommandByteBuffer);
	}

}
