package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class ICMPEchoReplyParser extends PacketParser {

	@Override
	public String getProtocol(byte[] packet) {
		return "Echo Reply";
	}

	@Override
	public String getInfo(byte[] packet) {
		return getProtocol(packet);
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {

		EtherealTreeNode root = new EtherealTreeNode("ICMP Echo Reply (" + packet.length +" bytes)", packet);
		EtherealTreeNode header = new EtherealTreeNode("ICMP Echo Reply Header (4 bytes)", 0, 4);
		root.add(header);
		
		EtherealTreeNode.startParse();
		
		header.add(new EtherealTreeNode("Identifier = 0x" + hexFormat(packet, 0, 2), 2));
		header.add(new EtherealTreeNode("Sequence Number = 0x" + hexFormat(packet, 2, 2), 2));

		root.add(new EtherealTreeNode("Data (Used to pad to minimum packet size)", packet.length - 4));

		
		return root;
		
	}

}
