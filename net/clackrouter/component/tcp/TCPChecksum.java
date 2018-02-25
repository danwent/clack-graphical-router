
package net.clackrouter.component.tcp;

import java.nio.ByteBuffer;

import net.clackrouter.packets.IPPacket;


/**
 * Helper class to perform the TCP checksum algorithm.  
 */
public class TCPChecksum {
	
	//offset into TCP header where we write the checksum
	public static final int CHECKSUM_OFFSET = 16; 
	
	/**
	 * Calculates the TCP checksum for the supplied IP packet
	 * @param ippacket
	 * @return TCP header checksum
	 */
    public static int calcTCPChecksum(IPPacket ippacket) { 	
    	// code from: http://www.citi.umich.edu/projects/smartcard/webcard/ip7816.java
    	
    	// Calculate TCP checksum
    	int ck = 0, d0 = 0, d1 = 0;
    	byte[] ipheader = ippacket.getHeader().getBytes();
    	for (int i = 12; i < ipheader.length; i += 2) {
    	    d0 += (short) (ipheader[i] & 0xff);
    	    d1 += (short) (ipheader[i+1] & 0xff);
    	}
    	d1 += 6 + ippacket.getLength() - 20;
    	
    	
    	byte[] tcp_all = ippacket.getBodyBytes();
    	for (int i = 0; i < tcp_all.length; i += 2) {
    	    d0 += (short) (tcp_all[i] & 0xff);
    	    if(i+1 < tcp_all.length)
    	    	d1 += (short) (tcp_all[i+1] & 0xff);
    	}
    	ck = (short) ((d0 & 0xff) + ((d1 >> 8) & 0xff));
    	ck = (short) ~(((d0 >> 8) & 0xff) + (d1 & 0xff) + ((ck >> 8) & 0xff) + (ck << 8));
    	return ck;
  
    }
    
    /**
     * Sets the Checksum field in the TCP header.  
     * @param ippacket
     */
    public static void setTCPChecksum(IPPacket ippacket){
    	ippacket.pack();
    	int checksum = calcTCPChecksum(ippacket);
    	ByteBuffer tcp_all = ippacket.getBodyBuffer();
    	tcp_all.rewind();
    	tcp_all.putShort(CHECKSUM_OFFSET, (short) checksum);
    	ippacket.pack();

    }
    /**
     * Verifies the Checksum field in the TCP header.
     * @param ippacket
     * @return true if the checksum is value, false otherwise.
     */
    public static boolean verifyTCPChecksum(IPPacket ippacket) {
    	ByteBuffer tcp_all = ippacket.getBodyBuffer();
    	tcp_all.rewind();
    	int packet_sum = tcp_all.getShort(CHECKSUM_OFFSET);
    	tcp_all.putShort(CHECKSUM_OFFSET, (short)0); // zero out checksum field before calculating checksum
    	ippacket.pack();
    	int calc_sum = calcTCPChecksum(ippacket);
    	return (packet_sum == calc_sum);
    }
}
