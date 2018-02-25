package net.clackrouter.netutils;

import java.net.InetAddress;

import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.topology.core.TopologyModel;

public class FilterEntry {
	
	private static class FilterEntryException extends Exception {
		public FilterEntryException(String s){
			super(s);
		}
	}
	public static int ANY_PORT = -1;

	public static int PROTO_TCP = 1;
	public static int PROTO_UDP = 2;
	public static int PROTO_ANY = 3;
	
	
	private int mPort;
	private InetAddress mAddr;
	private int mProtocol;
	
	//	 just for serialization
	private String mIface; 
	private String mVhostName;
	
	public FilterEntry(InetAddress a, int port, int proto, String vhostname){
		this(a, "none", port, proto, vhostname);
	}
	
	public FilterEntry(InetAddress a, String iface, int port, int proto, String vhostname){
		mPort = port;
		mAddr = a;
		mProtocol = proto;
		mIface = iface;
		mVhostName = vhostname;
	}
	
	// single match for now...
	public boolean matches(FilterEntry.PacketInfo info){
		if(mProtocol != PROTO_ANY && info.proto != mProtocol) return false;
		
		if(mPort != info.srcPort && mPort != info.dstPort)return false;
		
		if(!mAddr.equals(info.srcAddr) && !mAddr.equals(info.dstAddr)) return false;
				
		return true;
	}
	
	/**
	 * Return this filter as a configuration string (used for serialization)
	 * Note: the IP address is not saved, since this is topology dependent
	 */
	public String toString() {
		String proto_str = "any";
		if(mProtocol == PROTO_TCP) proto_str = "tcp";
		else if(mProtocol == PROTO_UDP) proto_str = "udp";
		
		StringBuffer sb = new StringBuffer(30);
		sb.append(mVhostName).append(":").append(mIface).append(":").append(mPort);
		sb.append(":").append(proto_str);
		return sb.toString();
	}

	/** 
	 * Creates a Filter entry from a configuration string.  String format for a filter entry is:
	 *[vhost name]:[iface name]:[port]:[proto]
	 * port should be any integer (-1 means any port)
	 * proto should be either "tcp", "udp", or "any"
	 * 
	 */
	public static FilterEntry createFromTopoName(String s, TopologyModel topo_model) throws Exception {
		String[] parts = s.split(":");
		if(parts.length == 4){
			TopologyModel.Host h = topo_model.getHost(parts[0]);
			if(h != null){
				for(int i = 0; i < h.interfaces.size(); i++){
					TopologyModel.Interface iface = (TopologyModel.Interface)h.interfaces.get(i);
					if(iface.name.equals(parts[1])){
						int proto = 0;
						if(parts[3].equalsIgnoreCase("tcp")) proto = PROTO_TCP;
						else if(parts[3].equalsIgnoreCase("udp")) proto = PROTO_UDP;
						else if(parts[3].equalsIgnoreCase("any")) proto = PROTO_ANY;
						if(proto != 0)
							return new FilterEntry(iface.address, parts[1], Integer.parseInt(parts[2]), proto, parts[0]);
					}
				}

			}
		}
		throw new FilterEntryException("Bad Filter: " + s);
	}
	
	public static class PacketInfo {
		public InetAddress srcAddr;
		public InetAddress dstAddr;
		public int srcPort;
		public int dstPort;
		public int proto;
	}
	
    public static PacketInfo getPacketInfo(VNSPacket p){
    	
    	VNSPacket l2 = ExtractEncapUtils.extractToLayer2(p);
    	
    	if(!(l2 instanceof IPPacket)) return null;

    	IPPacket ip = (IPPacket)l2;
    	
    	try {
    		PacketInfo p_info = new PacketInfo();
    		p_info.srcAddr = InetAddress.getByAddress(ip.getHeader().getSourceAddress().array());
    		p_info.dstAddr = InetAddress.getByAddress(ip.getHeader().getDestinationAddress().array());
    		
    		if(ip.getHeader().getProtocol() == IPPacket.PROTO_TCP){
    			VNSTCPPacket tcp = new VNSTCPPacket(ip.getBodyBuffer());
    			p_info.dstPort = tcp.dst_port;
    			p_info.srcPort = tcp.src_port;
    			p_info.proto = FilterEntry.PROTO_TCP;
    			return p_info;

    		}else if (ip.getHeader().getProtocol() == IPPacket.PROTO_UDP){
    			VNSUDPPacket udp = new VNSUDPPacket(ip.getBodyBuffer());
    			p_info.dstPort = udp.dst_port;
    			p_info.srcPort = udp.src_port;
    			p_info.proto = FilterEntry.PROTO_UDP;
    			return p_info; 
    		}else {
    			return null; // some other L3 protocol... e.g. ICMP, Routing packet, etc		
    		}
    		
    	} catch (Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	
    }

}
