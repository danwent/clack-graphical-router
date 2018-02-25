
package net.clackrouter.netutils;

/**
 * Allows higher level applications to listen for ICMP events.
 * 
 */
public interface ICMPListener {

	public void receivedEchoReply(int identifier, int seq_num);
	
	public void receivedEchoRequest(int identifier, int seq_num);
}
