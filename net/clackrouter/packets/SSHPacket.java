package net.clackrouter.packets;

import java.nio.ByteBuffer;

// warning, this is just the skeleton of a SSH packet,
// not the whole thing

public class SSHPacket extends VNSPacket {
	
	public static final int SSH_PORT = 22;
	
	public static final int MSG_CODE_PUBLIC_KEY = 2;
	public static final int MSG_CODE_SESSION_KEY = 3;
	public static final int MSG_CODE_NONE = -1;
	
	private int msg_code;
	
	public SSHPacket(ByteBuffer buf) throws Exception {
		super(buf);
		msg_code = MSG_CODE_NONE;
		
	}
	
	private void extractFromByteBuffer() throws Exception {
		m_packetByteBuffer.rewind();
		if(m_packetByteBuffer.capacity() == 0) return ;
		
		byte [] array = new byte[4];
	    m_packetByteBuffer.get(array, 0 , 4);
        int length  = (int)get32bit(array);	
		int padding_len = getPaddingLength(length);
		for(int i = 0; i < padding_len; i++){
			m_packetByteBuffer.getChar(); // throw away padding
		}
		m_packetByteBuffer.get(array, 0, 1);
		int msg_code = 0xFF & array[0];
		System.out.println("len = " + length 
					+ " padding len = " + padding_len 
					+ "msg_code = " + msg_code);
		byte[] data = new byte[length - 1];
		for(int i = 0; i < data.length; i++){
			System.out.println("byte[" + i + "] = " 
					+ data[i]);
		}
	}
	
	
	private int getPaddingLength(int length){
		return (8 - (length % 8));
	}
}
