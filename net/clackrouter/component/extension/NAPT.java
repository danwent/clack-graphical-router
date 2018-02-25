
package net.clackrouter.component.extension;

import java.awt.Color;
import java.awt.Point;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.tcp.TCPChecksum;
import net.clackrouter.netutils.ExtractEncapUtils;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.packets.VNSICMPPacket;
import net.clackrouter.packets.VNSICMPType;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.propertyview.IPRouteLookupPview;
import net.clackrouter.propertyview.NATPView;
import net.clackrouter.router.core.Router;


/**
 * VERY basic implementation of NAT.  Only handles TCP/UDP for now, no ICMP.
 * Defaults to using the first interface (usually eth0) of the current router as the external IP, and 10.1.1.0/24 
 * as the private network (assumes that it has interfaces in the private networks, so it
 * hears about them from routing).  
 */
public class NAPT extends ClackComponent {
	public static final int PORT_IN = 0;
	public static final int PORT_OUT = 1;
	public static final int NUM_PORTS = 2;
	
	public static final int DEST_PORT = 1;
	public static final int SRC_PORT = 2;
	
	
	// for clearing out old flows
	public static int TRANSPORT_MAX_TTL = 30;
	public static int ICMP_MAX_TTL = 5;
	
	InetAddress externalIP;
	InetAddress internalNet;
	InetAddress internalMask;
	
	private Hashtable<String, TransportLevelMapping> internalTransportMap;
	private Hashtable<String, TransportLevelMapping> externalTransportMap;
	
	private Hashtable<String, ICMPMapping> ICMPMap;	
	
	private int next_external_port = 1000;
	private int next_external_echo_id = 200;
	
	public NAPT(Router router, String name){
		super(router, name);
		setupPorts(NUM_PORTS);
		
		internalTransportMap = new Hashtable<String, TransportLevelMapping>();
		externalTransportMap = new Hashtable<String, TransportLevelMapping>();
		ICMPMap = new Hashtable<String, ICMPMapping>();
		
		try {
			externalIP = getRouter().getInterfaceByName("eth0").getIPAddress();
			internalNet = InetAddress.getByName("10.1.1.0");
			internalMask = InetAddress.getByName("255.255.255.0");

		}catch (Exception e){
			e.printStackTrace();
		}
		setAlarm(1000);
	}
	

	
	public class TransportLevelMapping {
		public InetAddress internalIP;
		public int internalPort, externalPort, proto, expire_ttl;
	}
	
	public class ICMPMapping {
		public InetAddress internalIP;
		public int internalID, expire_ttl;
	}

	
	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
        String in_desc = "Data for NAPT processing";
        String out_desc = "Data after NAPT processing";
  
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        
    } // -- setupPorts
	
	public void acceptPacket(VNSPacket packet, int port_num){

		IPPacket ippacket = (IPPacket)packet;
		
		try {
			InetAddress src = InetAddress.getByAddress(ippacket.getHeader().getSourceAddress().array());
			InetAddress dst = InetAddress.getByAddress(ippacket.getHeader().getDestinationAddress().array());
			int protocol = ippacket.getHeader().getProtocol();
		
			if(NetUtils.isNetworkMatch(src, internalNet, internalMask)){
				// this is a packet heading out from our internal network
				
				if(protocol == IPPacket.PROTO_ICMP){				
					handleICMPInternal(ippacket, src, dst);
					return;
				}

				int internal_src_port = getTransportLevelPort(packet, SRC_PORT);
				String key = makeInternalKey(src, internal_src_port, protocol);
				TransportLevelMapping tmap = internalTransportMap.get(key);
				if(tmap == null){
					tmap = addTransportMapping(src, internal_src_port, protocol);
				}
				tmap.expire_ttl = TRANSPORT_MAX_TTL;
				ByteBuffer externalIP_bytebuffer = ByteBuffer.wrap(externalIP.getAddress());
				ippacket.getHeader().setSrcAddr(externalIP_bytebuffer);
				
				System.out.println("NAT is mapping port of outgoing packet from " + internal_src_port + " to " + tmap.externalPort);
				setTransportLevelPort(ippacket, SRC_PORT, tmap.externalPort);
			
		        ippacket.getHeader().setChecksum(ippacket.getHeader().calculateChecksum());
		        ippacket.pack();
		        int test_port = getTransportLevelPort(ippacket, SRC_PORT);
		        System.out.println("Checking source-port before we send it out: " + test_port);
				m_ports[PORT_OUT].pushOut(ippacket);
			}
			else if(dst.equals(externalIP) ){
				// this is a packet coming back...
						
				if(protocol == IPPacket.PROTO_ICMP){
					handleICMPExternal(ippacket);
					return;
				}
				
				int external_dest_port = getTransportLevelPort(packet, DEST_PORT);
				String  key = makeExternalKey(external_dest_port, protocol);
				TransportLevelMapping tmap = externalTransportMap.get(key);
				if(tmap == null){
					error("dropping TCP/UDP packet to external IP address because there's no mapping for external port = "
								+ external_dest_port);
					return;
				}
				tmap.expire_ttl = TRANSPORT_MAX_TTL;
				ByteBuffer internal_ip_bytebuffer = ByteBuffer.wrap(tmap.internalIP.getAddress());
				ippacket.getHeader().setDestAddr(internal_ip_bytebuffer);
				setTransportLevelPort(ippacket, DEST_PORT, tmap.internalPort);

		        ippacket.getHeader().setChecksum(ippacket.getHeader().calculateChecksum());
		        ippacket.pack();
				m_ports[PORT_OUT].pushOut(ippacket);
			
			}else {
				error("Dropping packet sent to NAT, because it's addresses don't make sense");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void handleICMPInternal(IPPacket ippacket, InetAddress src, InetAddress dst){
		VNSICMPPacket icmp_packet = new VNSICMPPacket(ippacket.getBodyBuffer());
		
		if(icmp_packet.getType().getValue() == VNSICMPType.ECHO_REQUEST){
			Point echo_point = VNSICMPPacket.parseEcho(icmp_packet);
			
			int seq_num = echo_point.y;
			ICMPMapping map = new ICMPMapping();
			int external_id = next_external_echo_id++;
			map.internalID = echo_point.x;
			map.expire_ttl = ICMP_MAX_TTL;
			map.internalIP = src;
			ICMPMap.put("" + external_id, map);
			this.fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE));
			
			VNSICMPPacket new_echo = VNSICMPPacket.wrapEcho(VNSICMPType.ECHO_REQUEST, external_id, seq_num);
			ippacket.setBodyBuffer(new_echo.getByteBuffer());
		}
		// otherwise just send it out.... but don't set-up mapping to let anything back in
		System.out.println("Changing IP source to " + externalIP.getHostAddress());
		ByteBuffer externalIP_bytebuffer = ByteBuffer.wrap(externalIP.getAddress());
		ippacket.getHeader().setSrcAddr(externalIP_bytebuffer);	
        ippacket.getHeader().setChecksum(ippacket.getHeader().calculateChecksum());
        ippacket.pack();
		m_ports[PORT_OUT].pushOut(ippacket);
	}
	
	private void handleICMPExternal(IPPacket ippacket){
		VNSICMPPacket icmp_packet = new VNSICMPPacket(ippacket.getBodyBuffer());
		
		if(icmp_packet.getType().getValue() != VNSICMPType.ECHO_REPLY){
			error("Dropping non-echo reply packet from external net");
			return; // drop
		}

		Point echo_point = VNSICMPPacket.parseEcho(icmp_packet);
		int identifier = echo_point.x;
		int seq_num = echo_point.y;
		
		ICMPMapping mapping = ICMPMap.get("" + identifier);
		if(mapping == null){
			error("Dropping ICMP echo reply with unknown id");
			return; // drop		
		}
		
		VNSICMPPacket new_echo = VNSICMPPacket.wrapEcho(VNSICMPType.ECHO_REPLY, mapping.internalID, seq_num);
		ippacket.setBodyBuffer(new_echo.getByteBuffer());

		ByteBuffer dest_bytebuffer = ByteBuffer.wrap(mapping.internalIP.getAddress());
		ippacket.getHeader().setDestAddr(dest_bytebuffer);
        ippacket.getHeader().setChecksum(ippacket.getHeader().calculateChecksum());
        ippacket.pack();
		m_ports[PORT_OUT].pushOut(ippacket);
	}
	
	
	
	private TransportLevelMapping addTransportMapping(InetAddress internal_ip, int internal_port, int protocol){
	
		TransportLevelMapping tmap = new TransportLevelMapping();
		tmap.internalIP = internal_ip;
		tmap.internalPort = internal_port;
		tmap.proto = protocol;
		tmap.externalPort = next_external_port++;  // make up new external port number
		tmap.expire_ttl = TRANSPORT_MAX_TTL;
		
		String external_key = makeExternalKey(tmap.externalPort, tmap.proto);
		String internal_key = makeInternalKey(tmap.internalIP, tmap.internalPort, tmap.proto);
		internalTransportMap.put(internal_key, tmap);
		externalTransportMap.put(external_key, tmap);
		
		fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE));
		
		return tmap;
	}
	
	
	private String makeExternalKey(int external_port, int proto){
		return external_port + "," + proto;
	}
	
	private String makeInternalKey(InetAddress internalIP, int internal_port, int proto){
		return internalIP.getHostAddress() + "," + internal_port + "," + proto;
	}
	
	private int getTransportLevelPort(VNSPacket packet, int port_type) throws Exception {

		VNSPacket l3 = ExtractEncapUtils.extractToLayer3(packet);
		if(l3 == null){
			throw new Exception("Failed to extract packet to l3...");
		}
		if(l3 instanceof VNSTCPPacket){
			VNSTCPPacket tcppacket = (VNSTCPPacket) l3;
			if(port_type == SRC_PORT)
				return tcppacket.src_port;
			else
				return tcppacket.dst_port;
		}else if(l3 instanceof VNSUDPPacket){
			VNSUDPPacket udppacket = (VNSUDPPacket)l3;
			if(port_type == SRC_PORT)
				return udppacket.src_port;
			else
				return udppacket.dst_port;
		}else {	
			throw new Exception("only handling TCP or UDP for now");
		}	
	}

	private void setTransportLevelPort(IPPacket ippacket, int port_type, int port_value) throws Exception {

		// does this get us a new packet?  
		VNSPacket l3 = ExtractEncapUtils.extractToLayer3(ippacket);
		if(l3 == null){
			throw new Exception("Failed to extract packet to l3...");
		}
		if(l3 instanceof VNSTCPPacket){
			VNSTCPPacket tcppacket = (VNSTCPPacket) l3;
			if(port_type == SRC_PORT)
				tcppacket.src_port = port_value;
			else 
				tcppacket.dst_port = port_value;
			
			tcppacket.pack(); 
			ippacket.setBodyBuffer(tcppacket.getByteBuffer());
			TCPChecksum.setTCPChecksum(ippacket);
		}else if(l3 instanceof VNSUDPPacket){
			VNSUDPPacket udppacket = (VNSUDPPacket)l3;
			if(port_type == SRC_PORT)
				udppacket.src_port = port_value;
			else
				udppacket.dst_port = port_value;
			udppacket.pack(); // this calculates the UDP checksum too
			ippacket.setBodyBuffer(udppacket.getByteBuffer());
		}else {	
			throw new Exception("only handling TCP or UDP for now");
		}	
	}
	
	public void notifyAlarm(){
		
		for(Enumeration e = internalTransportMap.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			TransportLevelMapping t = internalTransportMap.get(key);
			if(t.expire_ttl == 1)
				internalTransportMap.remove(key);
			else 
				t.expire_ttl--;
		}
		
		for(Enumeration e = ICMPMap.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			ICMPMapping i = ICMPMap.get(key);
			if(i.expire_ttl == 1)
				ICMPMap.remove(key);
			else 
				i.expire_ttl--;
		}		
		
		fireListeners(new ClackComponentEvent(ClackComponentEvent.EVENT_DATA_CHANGE));
		setAlarm(1000);
	}
	
	public void setExternalIPAddress(InetAddress new_addr){
		externalIP = new_addr;
	}
	
	public void setInternalNetwork(InetAddress net, InetAddress mask){
		internalNet = net;
		internalMask = mask;
	}
	
	public TransportLevelMapping[] getTransportLevelMappings() {
		return internalTransportMap.values().toArray(new TransportLevelMapping[] {});
	}
	
	public Hashtable getICMPMappings() { return ICMPMap; }
	
    public JPanel getPropertiesView(){ return new NATPView(this);   }
    
    public Color getColor() { return new Color(74,118,110); }
	

}
