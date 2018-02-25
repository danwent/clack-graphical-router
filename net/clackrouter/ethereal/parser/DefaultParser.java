package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class DefaultParser extends PacketParser {

	@Override
	public String getSrc(byte[] packet) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getDst(byte[] packet) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getProtocol(byte[] packet) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getInfo(byte[] packet) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		// TODO Auto-generated method stub
		if (packet.length == 0) return new EtherealTreeNode();
		return new EtherealTreeNode("Unknown Packet (" + packet.length + " bytes)", packet);
	}

}
