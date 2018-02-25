package net.clackrouter.netutils;

import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.packets.VNSUDPPacket;

public class ExtractEncapUtils {

	/**
	 * Takes any packet that is Layer3 or below, and returns a Layer3 packet.
	 * At the present time, that means an IP or ARP packet. 
	 * @param p
	 * @return
	 */
	public static VNSPacket extractToLayer3(VNSPacket p){
		
		if(p instanceof VNSTCPPacket || p instanceof VNSUDPPacket) 
			return p;
		
		VNSPacket l2 = extractToLayer2(p);
		
		if(l2 instanceof IPPacket){
			IPPacket ip = (IPPacket)l2;
    		if(ip.getHeader().getProtocol() == IPPacket.PROTO_TCP){
    			return new VNSTCPPacket(ip.getBodyBuffer());
    		}else if(ip.getHeader().getProtocol() == IPPacket.PROTO_UDP){
    			return new VNSUDPPacket(ip.getBodyBuffer());
    		}
		}
		
		return null;
	}
	
	/**
	 * Takes any packet that is Layer2 or below, and returns a Layer2 packet.
	 * At the present time, that means an IP or ARP packet. 
	 * @param p
	 * @return
	 */
	public static VNSPacket extractToLayer2(VNSPacket p){
		
		if(p instanceof IPPacket || p instanceof VNSARPPacket) return p;
		
		if(p instanceof VNSEthernetPacket) {
			VNSEthernetPacket eth = (VNSEthernetPacket)p;
			if(eth.getType() == VNSEthernetPacket.TYPE_IP){
		    	return new IPPacket(eth.getBodyBuffer());		
			}else if (eth.getType() == VNSEthernetPacket.TYPE_ARP){
				return new VNSARPPacket(eth.getBodyBuffer());
			}

		}

		return null;
	}
	
}
