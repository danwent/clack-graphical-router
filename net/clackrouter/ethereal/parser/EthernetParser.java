package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class EthernetParser extends PacketParser {

	public EthernetParser() {
		typeStart=12;
		typeLen =2;
		dataStart=14;
		dataLen =0;
	}
		
	@Override
	public String getSrc(byte[] packet) {
		// I won't know this. Hopefully a downlink will
		return Registry.lookup("Ethernet", getType(packet)).getSrc(strip(packet));
	}


	@Override
	public String getDst(byte[] packet) {
		// I won't know this. Hopefully a downlink will
		return Registry.lookup("Ethernet", getType(packet)).getDst(strip(packet));
	}

	@Override
	public String getProtocol(byte[] packet) {
		return "Ethernet (" + Registry.lookup("Ethernet", getType(packet)).getProtocol(strip(packet)) + ")";
	}

	@Override
	public String getInfo(byte[] packet) {
		// I won't know this. Hopefully a downlink will
		return Registry.lookup("Ethernet", getType(packet)).getInfo(strip(packet));
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		EtherealTreeNode root = new EtherealTreeNode("Ethernet Packet (" + packet.length +" bytes)", packet);
		
		EtherealTreeNode header = new EtherealTreeNode("Ethernet Packet Header (14 bytes)", 0, 14);
		root.add(header);

		header.add(new EtherealTreeNode("Src MAC Address = " + MACFormat(packet, 6, 6), 6, 12));
		header.add(new EtherealTreeNode("Dst MAC Address = " + MACFormat(packet, 0, 6), 0, 6));
		String type = null;
		switch(getType(packet)) {
		case 0x0800 :	type = "Internet Protocol, Version 4 (IPv4)"; break;
		case 0x0806 :	type = "Address Resolution Protocol (ARP)"; break;
		case 0x8035 :	type = "Reverse Address Resolution Protocol (RARP)"; break;
		case 0x809b :	type = "AppleTalk (Ethertalk)"; break;
		case 0x80f3 :	type = "AppleTalk Address Resolution Protocol (AARP)"; break;
		case 0x8100 :	type = "IEEE 802.1Q-tagged frame"; break;
		case 0x8137 :	type = "Novell IPX (alt)"; break;
		case 0x8138 :	type = "Novell"; break;
		case 0x86DD :	type = "Internet Protocol, Version 6 (IPv6)"; break;
		case 0x8847 :	type = "MPLS unicast"; break;
		case 0x8848 :	type = "MPLS multicast"; break;
		case 0x8863 :	type = "PPPoE Discovery Stage"; break;
		case 0x8864 :	type = "PPPoE Session Stage"; break;
		}
		header.add(new EtherealTreeNode("Type = " + type, 12, 14));
		
		root.add(Registry.lookup("Ethernet", getType(packet)).getTree(strip(packet)).ofset(14));
		/*
		EtherealTreeNode footer = new EtherealTreeNode("Ethernet Packet Footer (4 bytes)", packet.length - 4, packet.length);
		root.add(footer);
		
		footer.add(new EtherealTreeNode("Checksum = 0x" + hexFormat(packet, packet.length - 4, 4), packet.length - 4, packet.length));
		*/
		return root;
	}



}
