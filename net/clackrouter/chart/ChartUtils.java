package net.clackrouter.chart;

import java.net.InetAddress;

import net.clackrouter.netutils.ExtractEncapUtils;
import net.clackrouter.netutils.FilterEntry;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.packets.VNSUDPPacket;

public class ChartUtils {

	public static String NON_FLOW = "NON-TCP-UDP", TCP_FLOW = "TCP ", UDP_FLOW = "UDP "; 
	
	   /**
     * 
     * Finds the ClackOccData object that corresponds to the flow for this particular packet.  
     * Creates a new data object if needed.  If the packet is not UDP or TCP, the NON_FLOW data 
     * object is returned.  
     * 
     * WARNING: This packet must be called with an IP packet or Ethernet packet
     */
    public static String getFlowKeyForPacket(VNSPacket p){
    	
    	VNSPacket l2 = ExtractEncapUtils.extractToLayer2(p);
    	
    	if(!(l2 instanceof IPPacket)) return NON_FLOW;

    	IPPacket ip = (IPPacket)l2;
    	
    	try {
    		String ip_src = InetAddress.getByAddress(ip.getHeader().getSourceAddress().array()).getHostAddress();
    		String ip_dst = InetAddress.getByAddress(ip.getHeader().getDestinationAddress().array()).getHostAddress();
    		
    		if(ip.getHeader().getProtocol() == IPPacket.PROTO_TCP){
    			VNSTCPPacket tcp = new VNSTCPPacket(ip.getBodyBuffer());
    			return TCP_FLOW + " " + ip_src + " : " + tcp.src_port + " -> " 
    				+ ip_dst + " : " + tcp.dst_port;

    		}else if (ip.getHeader().getProtocol() == IPPacket.PROTO_UDP){
    			VNSUDPPacket udp = new VNSUDPPacket(ip.getBodyBuffer());
    			return UDP_FLOW + " " + ip_src + " : " + udp.src_port + " -> "
    				+ ip_dst + " : " + udp.dst_port;
    		}else {
    			return NON_FLOW; // some other L3 protocol... e.g. ICMP, Routing packet, etc		
    		}
    		
    	} catch (Exception e){
    		e.printStackTrace();
    		return NON_FLOW;
    	}
    	
    }

    /**
     * If we are graphing with a queue that is also performing filtering, there is no need
     * to inspect the packet twice for the same fields (once to filter, once to get key).
     * As a result, we provide this convenience method
     */
    public static String getFlowKeyForFilterEntry(FilterEntry.PacketInfo info){
    	
    		if(info.proto == FilterEntry.PROTO_TCP){
    		
    			return TCP_FLOW + " " + info.srcAddr.getHostAddress() + " : " + info.srcPort + " -> " 
    				+ info.dstAddr.getHostAddress() + " : " + info.dstPort;

    		}else if (info.proto == FilterEntry.PROTO_UDP){
    			return UDP_FLOW + " " + info.srcAddr.getHostAddress() + " : " + info.srcPort + " -> " 
				+ info.dstAddr.getHostAddress() + " : " + info.dstPort;
    		}else {
    			return NON_FLOW; // some other L3 protocol... e.g. ICMP, Routing packet, etc		
    		}	
    }
	
}
