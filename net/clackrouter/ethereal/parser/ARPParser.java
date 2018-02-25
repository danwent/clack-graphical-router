package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class ARPParser extends PacketParser {
	
	@Override
	public String getSrc(byte[] packet) {
		byte hwlen = packet[4];
		byte protolen = packet[5];
		return decimalFormat(packet, 8+hwlen, protolen);
	}

	@Override
	public String getDst(byte[] packet) {
		byte hwlen = packet[4];
		byte protolen = packet[5];
		return decimalFormat(packet, 8+hwlen+protolen+hwlen, protolen);
	}

	@Override
	public String getProtocol(byte[] packet) {
		// TODO Auto-generated method stub
		return "ARP";
	}

	@Override
	public String getInfo(byte[] packet) {
		// TODO Auto-generated method stub
		switch (packet[7]) {
		case 0x01 :
			return "ARP Request";
		case 0x02 :
			return "ARP Reply";
		}
		return "";
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		// TODO Auto-generated method stub
		EtherealTreeNode root = new EtherealTreeNode("ARP Packet (" + packet.length +" bytes)", packet);
		
		switch (packet[1]) {
		case 1: root.add(new EtherealTreeNode("Hardware Type: Ethernet", 0, 2));
		}
		String type = null;
		switch((packet[2] * 0x100 + packet[3])) {
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
		root.add(new EtherealTreeNode("Protocol Type = " + type, 2, 4));

		byte hwlen = packet[4];
		byte protolen = packet[5];
		root.add(new EtherealTreeNode("Hardware Address Lengh = " + hwlen, 4, 5));
		root.add(new EtherealTreeNode("Protocol Address Lengh = " + protolen, 5, 6));
		
		type = null;
		switch (packet[7]) {
		case 0x01 : type = "ARP Request";
		case 0x02 : type = "ARP Reply";
		}
		root.add(new EtherealTreeNode("Opcode = " + type, 6, 8));
		
		root.add(new EtherealTreeNode("Src Hardware Address = " + MACFormat(packet, 8, hwlen), 8, 8+hwlen));
		root.add(new EtherealTreeNode("Src Protocol Address = " + decimalFormat(packet, 8+hwlen, protolen), 8+hwlen, 8+hwlen+protolen));
		root.add(new EtherealTreeNode("Dst Hardware Address = " + MACFormat(packet, 8+hwlen+protolen, hwlen), 8+hwlen+protolen, 8+hwlen+protolen+hwlen));
		root.add(new EtherealTreeNode("Dst Protocol Address = " + decimalFormat(packet, 8+hwlen+protolen+hwlen, protolen), 8+hwlen+protolen+hwlen, 8+hwlen+protolen+hwlen+protolen));
	
		root.add(new EtherealTreeNode("Padding (Minimum ethernet packet size is 60 bytes = 64 bytes - 4 byte checksum)", 8+hwlen+protolen+hwlen+protolen, packet.length));
		return root;
	}

}
