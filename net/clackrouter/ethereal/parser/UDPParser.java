package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class UDPParser extends PacketParser {

	UDPParser() {
		dataLen = 0;
		dataStart = 8;
		typeLen = 2;
		typeStart = 2;
	}
	
	
	
	@Override
	public String getProtocol(byte[] packet) {
		// Look inside 
		return "UDP (" + nextProtocol(packet, "UDP") + ")";
	}

	@Override
	public String getInfo(byte[] packet) {
		int src_port = ((packet[0] & 0xFF) << 8) | ((packet[1] & 0xFF));
		int dst_port = ((packet[2] & 0xFF) << 8) | ((packet[3] & 0xFF));
		return "(src port = " + src_port + " dest port " + dst_port + ") " + nextInfo(packet, "UDP");
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		EtherealTreeNode root = new EtherealTreeNode("UDP Packet (" + packet.length +" bytes)", packet);
	
		EtherealTreeNode header = new EtherealTreeNode("UDP Packet Header (8 bytes)", 0, 8);
		root.add(header);

		int src_port = ((packet[0] & 0xFF) << 8) | ((packet[1] & 0xFF));
		int dst_port = ((packet[2] & 0xFF) << 8) | ((packet[3] & 0xFF));
		header.add(makeNode("Src Port = " + src_port, 0, 2));
		header.add(makeNode("Dst Port = " + dst_port, 2, 2));
		int len = ((packet[3] & 0xFF) << 8) | (packet[4] & 0xFF);
		header.add(makeNode("Length = " + len, 4, 2));
		header.add(makeNode("Checksum 0x" + hexFormat(packet, 6, 2), 6, 2));
		
		root.add(nextNode(packet, "UDP", 8));
		return root;
	}

}
