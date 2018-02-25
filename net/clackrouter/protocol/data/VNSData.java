/*
 * Created on 24-Oct-03
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.clackrouter.protocol.data;

/**
 * Generic parent class of add data sent from the VNS server to a client.  
 */
public abstract class VNSData {
	public boolean isBanner() { return false; }
	public boolean isPacket() { return false; }
	public boolean isClose() { return false; }
	public boolean isHWInfo() { return false; }

}
