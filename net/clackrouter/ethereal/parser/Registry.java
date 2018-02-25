package net.clackrouter.ethereal.parser;

import java.util.HashMap;

import net.clackrouter.ethereal.Pair;
import net.clackrouter.packets.IPPacket;

public class Registry {

	private static HashMap<Pair, Class<? extends PacketParser>> registry = new HashMap<Pair, Class<? extends PacketParser>>();
	
	public static void init() {
		registry.put(new Pair("Ethernet", null), EthernetParser.class);
		
		registry.put(new Pair("IPv4", null), IPv4Parser.class);
		registry.put(new Pair("Ethernet", 0x0800), IPv4Parser.class);
		//registry.put(new Pair("Ethernet", 0x86DD), IPv6Parser.class);
		
		registry.put(new Pair("ARP", null), ARPParser.class);
		registry.put(new Pair("Ethernet", 0x0806), ARPParser.class);

		registry.put(new Pair("ICMP", null), ICMPParser.class);
		registry.put(new Pair("IPv4", 1), ICMPParser.class);
		
		registry.put(new Pair("IPv4", 4), IPv4Parser.class); // Ip in IP

		registry.put(new Pair("TCP", null), TCPParser.class);
		registry.put(new Pair("IPv4", 6), TCPParser.class);
		
		registry.put(new Pair("RIP", null), RIPUpdateParser.class);
		registry.put(new Pair("IPv4", (int)IPPacket.PROTO_CLACK_RIP), RIPUpdateParser.class);
		
		registry.put(new Pair("OSPF", null), LinkStatePacketParser.class);
		registry.put(new Pair("IPv4", (int)IPPacket.PROTO_CLACK_OSPF), LinkStatePacketParser.class);

		registry.put(new Pair("UDP", null), UDPParser.class);
		registry.put(new Pair("IPv4", 17), UDPParser.class);

		registry.put(new Pair("ICMP", 0), ICMPEchoReplyParser.class);
		registry.put(new Pair("ICMP", 8), ICMPEchoRequestParser.class);

		registry.put(new Pair("TCP", 21), FTPParser.class);
		registry.put(new Pair("TCP", 80), HTTParser.class);
	}
	
	
	public static PacketParser lookup(String a, Integer b) { return lookup(new Pair(a, b)); }
	public static PacketParser lookup(Pair p) {
		try {
			Class<? extends PacketParser> c = registry.get(p);
			if (c == null) c = DefaultParser.class;
			return getInstance(c);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	

	private static HashMap<Class<? extends PacketParser>, PacketParser> myInstnaces = new HashMap<Class<? extends PacketParser>, PacketParser>();
	
	public static PacketParser getInstance(Class<? extends PacketParser> p) {
		if (! myInstnaces.containsKey(p))
			try {
				myInstnaces.put(p, p.newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return myInstnaces.get(p);
	}
}
