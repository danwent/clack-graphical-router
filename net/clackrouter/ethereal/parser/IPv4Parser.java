package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class IPv4Parser extends PacketParser{

	public IPv4Parser() {
		typeStart=9;
		typeLen =1;
		dataStart=32;
		dataLen =0;
	}
	
	@Override
	public String getSrc(byte[] packet) {
		return decimalFormat(packet, 12, 4);
	}

	@Override
	public String getDst(byte[] packet) {
		return decimalFormat(packet, 16, 4);
	}

	@Override
	public String getProtocol(byte[] packet) {
		int len = ((packet[0] & 0x0F) * 4);
		dataStart = len;
		return "IPv4 (" + Registry.lookup("IPv4", getType(packet)).getProtocol(strip(packet)) + ")";
	}

	@Override
	public String getInfo(byte[] packet) {
		// Not much interesting in an IP packet
		int len = ((packet[0] & 0x0F) * 4);
		dataStart = len;
		return Registry.lookup("IPv4", getType(packet)).getInfo(strip(packet));
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		EtherealTreeNode root = new EtherealTreeNode("IPv4 Packet (" + packet.length +" bytes)", packet);
		
		int len = ((packet[0] & 0x0F) * 4);
		dataStart = len;
		
		EtherealTreeNode header = new EtherealTreeNode("IPv4 Packet Header (" + len + " bytes)", 0, len);
		root.add(header);
		
		EtherealTreeNode.startParse();
		header.add(new EtherealTreeNode("Version = " + ((packet[0] & 0xF0) >> 4), 1));
		EtherealTreeNode.rewindParse(1);
		int hdr_len = 4 * (packet[0] & 0x0F); 
		header.add(new EtherealTreeNode("Header Length = " + hdr_len, 1));

		int tos = packet[1];
		String tosString = "";
		switch (tos & 0xE0 >> 5) {
		case 0 :	tosString += "Routine"; break;
		case 1 :	tosString += "Priority"; break;
		case 2 :	tosString += "Immediate"; break;
		case 3 :	tosString += "Flash"; break;
		case 4 :	tosString += "Flash override"; break;
		case 5 :	tosString += "CRITIC/ECP"; break;
		case 6 :	tosString += "Internetwork control"; break;
		case 7 :	tosString += "Network control"; break;
		}
		tosString += ", ";
		switch (tos & 0x10 >> 4) {
		case 0 :	tosString += "Normal delay"; break;
		case 1 :	tosString += "Low delay"; break;
		}
		tosString += ", ";
		switch (tos & 0x08 >> 3) {
		case 0 :	tosString += "Normal throughput"; break;
		case 1 :	tosString += "High throughput"; break;
		}
		tosString += ", ";
		switch (tos & 0x04 >> 2) {
		case 0 :	tosString += "Normal reliability"; break;
		case 1 :	tosString += "High reliability"; break;
		}
		tosString += ", ";
		switch (tos & 0x02 >> 1) {
		case 0 :	tosString += "Normal monetary cost"; break;
		case 1 :	tosString += "Minimize monetary cost"; break;
		}
		
		header.add(new EtherealTreeNode("Terms of Service = " + tosString, 1));
		int total_len = ((packet[2] & 0xFF) << 8) | (packet[3] & 0xFF);
		header.add(new EtherealTreeNode("Total Length = " + total_len /*(packet[2] * 0x100 + packet[3])*/, 2));
		header.add(new EtherealTreeNode("Identification = 0x" + hexFormat(packet, 4, 2) /*(packet[4] * 0x100 + packet[5])*/, 2));

		int flags = packet[6] & 0x60 >> 5;
		String flagString = "";
		switch (flags & 0x02 >> 1) {
		case 0 :	flagString += "Fragment if necessary"; break;
		case 1 :	flagString += "Do not fragment"; break;
		}
		flagString += ", ";
		switch (flags & 0x01) {
		case 0 :	flagString += "This is the last fragment"; break;
		case 1 :	flagString += "More fragments follow this fragment"; break;
		}
		header.add(new EtherealTreeNode("Flags = " + flagString, 1));
		EtherealTreeNode.rewindParse(1);
		header.add(new EtherealTreeNode("Fragment Offset = " + (packet[6] & 0x1F) * 0x10 + packet[7], 2));

		header.add(new EtherealTreeNode("TTL (Time to Live) = " + packet[8], 1));
		
		String proto = "";
		switch (packet[9]) {
		case 0 : proto += "HOPOPT, IPv6 Hop-by-Hop Option"; break;
		case 1 : proto += "ICMP, Internet Control Message Protocol"; break;
		case 2	   : proto += "IGAP, IGMP for user Authentication Protocol"; break;
		case 3	  : proto += "GGP, Gateway to Gateway Protocol"; break;
		case 4	  : proto += "IP in IP encapsulation"; break;
		case 5	  : proto += "ST, Internet Stream Protocol"; break;
		case 6	  : proto += "TCP, Transmission Control Protocol"; break;
		case 7	  : proto += "UCL, CBT"; break;
		case 8	  : proto += "EGP, Exterior Gateway Protocol"; break;
		case 9	  : proto += "IGRP, Interior Gateway Routing Protocol"; break;
		case 10	  : proto += "BBN RCC Monitoring"; break;
		case 11	  : proto += "NVP, Network Voice Protocol"; break;
		case 12	  : proto += "PUP"; break;
		case 13	  : proto += "ARGUS"; break;
		case 14	  : proto += "EMCON, Emission Control Protocol"; break;
		case 15	  : proto += "XNET, Cross Net Debugger"; break;
		case 16	  : proto += "Chaos"; break;
		case 17	  : proto += "UDP, User Datagram Protocol"; break;
		case 18	  : proto += "TMux, Transport Multiplexing Protocol"; break;
		case 19	  : proto += "DCN Measurement Subsystems"; break;
		case 20	  : proto += "HMP, Host Monitoring Protocol"; break;
		case 21	  : proto += "Packet Radio Measurement"; break;
		case 22	  : proto += "XEROX NS IDP"; break;
		case 23	  : proto += "Trunk-1"; break;
		case 24	  : proto += "Trunk-2"; break;
		case 25	  : proto += "Leaf-1"; break;
		case 26	  : proto += "Leaf-2"; break;
		case 27	  : proto += "RDP, Reliable Data Protocol"; break;
		case 28	  : proto += "IRTP, Internet Reliable Transaction Protocol"; break;
		case 29	  : proto += "ISO Transport Protocol Class 4"; break;
		case 30	  : proto += "NETBLT, Network Block Transfer"; break;
		case 31	  : proto += "MFE Network Services Protocol"; break;
		case 32	  : proto += "MERIT Internodal Protocol"; break;
		case 33 	 : proto += "SEP, Sequential Exchange Protocol"; break;
		case 34 	 : proto += "Third Party Connect Protocol"; break;
		case 35	  : proto += "IDPR, Inter-Domain Policy Routing Protocol"; break;
		case 36	  : proto += "XTP, Xpress Transfer Protocol"; break;
		case 37	  : proto += "Datagram Delivery Protocol"; break;
		case 38	  : proto += "IDPR, Control Message Transport Protocol"; break;
		case 39	  : proto += "TP++ Transport Protocol"; break;
		case 40	  : proto += "IL Transport Protocol"; break;
		case 41 	 : proto += "IPv6 over IPv4"; break;
		case 42 	 : proto += "SDRP, Source Demand Routing Protocol"; break;
		case 43 	 : proto += "IPv6 Routing header"; break;
		case 44 	 : proto += "IPv6 Fragment header"; break;
		case 45 	 : proto += "IDRP, Inter-Domain Routing Protocol"; break;
		case 46 	 : proto += "RSVP, Reservation Protocol"; break;
		case 47 	 : proto += "GRE, General Routing Encapsulation"; break;
		case 48 	 : proto += "MHRP, Mobile Host Routing Protocol"; break;
		case 49 	 : proto += "BNA"; break;
		case 50 	 : proto += "ESP, Encapsulating Security Payload"; break;
		case 51 	 : proto += "AH, Authentication Header"; break;
		case 52 	 : proto += "Integrated Net Layer Security TUBA"; break;
		case 53 	 : proto += "IP with Encryption"; break;
		case 54	  : proto += "NARP, NBMA Address Resolution Protocol"; break;
		case 55	  : proto += "Minimal Encapsulation Protocol"; break;
		case 56	  : proto += "TLSP, Transport Layer Security Protocol using Kryptonet key management"; break;
		case 57	  : proto += "SKIP"; break;
		case 58	  : proto += "ICMPv6, Internet Control Message Protocol for IPv6"; break;
		case 59	  : proto += "IPv6 No Next Header"; break;
		case 60	  : proto += "IPv6 Destination Options"; break;
		case 61 	 : proto += "Any host internal protocol"; break;
		case 62 	 : proto += "CFTP"; break;
		case 63 	 : proto += "Any local network"; break;
		case 64 	 : proto += "SATNET and Backroom EXPAK"; break;
		case 65 	 : proto += "Kryptolan"; break;
		case 66 	 : proto += "MIT Remote Virtual Disk Protocol"; break;
		case 67 	 : proto += "Internet Pluribus Packet Core"; break;
		case 68 	 : proto += "Any distributed file system"; break;
		case 69 	 : proto += "SATNET Monitoring"; break;
		case 70 	 : proto += "VISA Protocol"; break;
		case 71 	 : proto += "Internet Packet Core Utility"; break;
		case 72 	 : proto += "Computer Protocol Network Executive"; break;
		case 73 	 : proto += "Computer Protocol Heart Beat"; break;
		case 74 	 : proto += "Wang Span Network"; break;
		case 75 	 : proto += "Packet Video Protocol"; break;
		case 76 	 : proto += "Backroom SATNET Monitoring"; break;
		case 77 	 : proto += "SUN ND PROTOCOL-Temporary"; break;
		case 78 	 : proto += "WIDEBAND Monitoring"; break;
		case 79	  : proto += "WIDEBAND EXPAK"; break;
		case 80	  : proto += "ISO-IP"; break;
		case 81	  : proto += "VMTP, Versatile Message Transaction Protocol"; break;
		case 82	  : proto += "SECURE-VMTP"; break;
		case 83	  : proto += "VINES"; break;
		case 84	  : proto += "TTP"; break;
		case 85 	 : proto += "NSFNET-IGP"; break;
		case 86 	 : proto += "Dissimilar Gateway Protocol"; break;
		case 87 	 : proto += "TCF"; break;
		case 88 	 : proto += "EIGRP"; break;
		case 89	  : proto += "OSPF, Open Shortest Path First Routing Protocol"; break;
		case 90	  : proto += "Sprite RPC Protocol"; break;
		case 91	  : proto += "Locus Address Resolution Protocol"; break;
		case 92	  : proto += "MTP, Multicast Transport Protocol"; break;
		case 93 	 : proto += "AX.25"; break;
		case 94 	 : proto += "IP-within-IP Encapsulation Protocol"; break;
		case 95 	 : proto += "Mobile Internetworking Control Protocol"; break;
		case 96 	 : proto += "Semaphore Communications Sec.Pro"; break;
		case 97 	 : proto += "EtherIP"; break;
		case 98 	 : proto += "Encapsulation Header"; break;
		case 99 	 : proto += "Any private encryption scheme"; break;
		case 100 	 : proto += "GMTP"; break;
		case 101 	 : proto += "IFMP, Ipsilon Flow Management Protocol"; break;
		case 102 	 : proto += "PNNI over IP"; break;
		case 103 	 : proto += "PIM, Protocol Independent Multicast"; break;
		case 104 	 : proto += "ARIS"; break;
		case 105 	 : proto += "SCPS"; break;
		case 106 	 : proto += "QNX"; break;
		case 107 	 : proto += "Active Networks"; break;
		case 108 	 : proto += "IPPCP, IP Payload Compression Protocol"; break;
		case 109 	 : proto += "SNP, Sitara Networks Protocol"; break;
		case 110 	 : proto += "Compaq Peer Protocol"; break;
		case 111 	 : proto += "IPX in IP"; break;
		case 112 	 : proto += "VRRP, Virtual Router Redundancy Protocol"; break;
		case 113 	 : proto += "PGM, Pragmatic General Multicast"; break;
		case 114 	 : proto += "any 0-hop protocol"; break;
		case 115 	 : proto += "L2TP, Level 2 Tunneling Protocol"; break;
		case 116 	 : proto += "DDX, D-II Data Exchange"; break;
		case 117 	 : proto += "IATP, Interactive Agent Transfer Protocol"; break;
		case 118 	 : proto += "ST, Schedule Transfer"; break;
		//case 119 	 : proto += "SRP, SpectraLink Radio Protocol"; break;
		case 119 	 : proto += "Clack RIP, Clack Router Distance Vector Routing"; break;
		case 120	  : proto += "UTI"; break;
		case 121	  : proto += "SMP, Simple Message Protocol"; break;
		case 122	  : proto += "SM"; break;
		case 123	  : proto += "PTP, Performance Transparency Protocol"; break;
		case 124	  : proto += "ISIS over IPv4"; break;
		case 125	  : proto += "FIRE"; break;
		case 126	  : proto += "CRTP, Combat Radio Transport Protocol"; break;
		case 127	  : proto += "CRUDP, Combat Radio User Datagram"; break;
		case 128 - 256	  : proto += "SSCOPMCE"; break;
		case 129 - 256	  : proto += "IPLT"; break;
		case 130 - 256	  : proto += "SPS, Secure Packet Shield"; break;
		case 131 - 256	  : proto += "PIPE, Private IP Encapsulation within IP"; break;
		case 132 - 256	  : proto += "SCTP, Stream Control Transmission Protocol"; break;
		case 133 - 256	  : proto += "Fibre Channel"; break;
		case 134 - 256	  : proto += "RSVP-E2E-IGNORE"; break;
		case 135 - 256	  : proto += "Mobility Header"; break;
		case 136 - 256	  : proto += "UDP-Lite, Lightweight User Datagram Protocol"; break;
		case 137 - 256	  : proto += "MPLS in IP"; break;
		case 254 - 256	  : proto += "Experimentation and testing"; break;
		case 255 - 256	  : proto += "reserved"; break;
		}
		header.add(new EtherealTreeNode("Protocol = " + proto, 1));
		

		header.add(new EtherealTreeNode("Header Checksum = 0x" + hexFormat(packet, 10, 2), 2));

		header.add(new EtherealTreeNode("Src IP Address = " + decimalFormat(packet, 12, 4), 4));
		header.add(new EtherealTreeNode("Dst IP Address = " + decimalFormat(packet, 16, 4), 4));
		
		if (len -20 > 0)
			header.add(new EtherealTreeNode("Options and Padding (" + (len - 20) + " bytes)", len - 20));

		root.add(Registry.lookup("IPv4", getType(packet)).getTree(strip(packet)).ofset(len));
		return root;
	}

}
