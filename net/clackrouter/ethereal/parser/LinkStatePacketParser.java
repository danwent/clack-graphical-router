package net.clackrouter.ethereal.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.clackrouter.ethereal.EtherealTreeNode;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.packets.LinkStatePacket;
import net.clackrouter.packets.RIPRoutingUpdate;

public class LinkStatePacketParser extends PacketParser {

	@Override
	public String getProtocol(byte[] packet) {
		return "OSPF";
	}

	@Override
	public String getInfo(byte[] packet) {
		return "Link State Packet for the OSPF protocol";
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		
		EtherealTreeNode.startParse();	
		EtherealTreeNode root = new EtherealTreeNode("OSPF Link State Packet (" + packet.length +" bytes)", packet);

		LinkStatePacket lsp = new LinkStatePacket(ByteBuffer.wrap(packet));

		int current_offset = 0;
		String r_id = "Router ID = " + lsp.getRouterID() + " ("
					+ NetUtils.intToInetAddress(lsp.getRouterID()).getHostAddress() + ")";
		root.add(makeNode(r_id, current_offset, 4));
		current_offset += 4;
		root.add(makeNode("Age = " + lsp.getAge(), current_offset, 4));
		current_offset += 4;
		root.add(makeNode("Seq Num = " + lsp.getSequenceNumber(), current_offset, 4));
		current_offset += 4;
		root.add(makeNode("Num Links = " + lsp.getLinkLSAs().size(), current_offset, 2));
		current_offset += 2;
		root.add(makeNode("Num Nets = " + lsp.getNetworkLSAs().size(), current_offset, 2));
		current_offset += 2;
		

		ArrayList<LinkStatePacket.Link> link_lsas = lsp.getLinkLSAs();
		for(int i = 0; i < link_lsas.size(); i++){
			LinkStatePacket.Link l = link_lsas.get(i);
			EtherealTreeNode entry_node = makeNode("Link LSA Entry", current_offset, LinkStatePacket.LSA_SIZE_BYTES);
			root.add(entry_node);
			entry_node.add(makeNode("LinkID = " + l.linkID + " (" + 
					NetUtils.intToInetAddress(l.linkID).getHostAddress() + ")", current_offset, 4));
			current_offset += 4;
			entry_node.add(makeNode("Weight = " + l.weight, current_offset, 4));
			current_offset += 4;
		}
		
		ArrayList<LinkStatePacket.Net> net_lsas = lsp.getNetworkLSAs();;
		for(int i = 0; i < net_lsas.size(); i++){
			LinkStatePacket.Net n = net_lsas.get(i);
			EtherealTreeNode entry_node = makeNode("Network LSA Entry", current_offset, LinkStatePacket.LSA_SIZE_BYTES);
			root.add(entry_node);
			entry_node.add(makeNode("Network = " + n.network.getHostAddress() , current_offset, 4));
			current_offset += 4;
			entry_node.add(makeNode("Mask = " + n.mask.getHostAddress(), current_offset, 4));
			current_offset += 4;
		}
		
		return root;
	}

}
