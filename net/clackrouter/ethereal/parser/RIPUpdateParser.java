package net.clackrouter.ethereal.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.clackrouter.ethereal.EtherealTreeNode;
import net.clackrouter.packets.RIPRoutingUpdate;

public class RIPUpdateParser extends PacketParser {

	@Override
	public String getProtocol(byte[] packet) {
		return "RIP";
	}

	@Override
	public String getInfo(byte[] packet) {
		return "Routing Update Packet for the RIP protocol";
	}

	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		
		EtherealTreeNode root = new EtherealTreeNode("RIP Update Packet (" + packet.length +" bytes)", packet);

		RIPRoutingUpdate update = new RIPRoutingUpdate(ByteBuffer.wrap(packet));
		
		int current_offset = 0;
		int entry_len = RIPRoutingUpdate.RIP_ENTRY_SIZE_BYTES;
		ArrayList entry_list = update.getAllEntries();
		EtherealTreeNode.startParse();
		for(int i = 0; i < entry_list.size(); i++){
			RIPRoutingUpdate.Entry rip_entry = (RIPRoutingUpdate.Entry) entry_list.get(i);
		
	  		EtherealTreeNode entry_node = makeNode("RIP Entry", current_offset, entry_len);
			root.add(entry_node);
			entry_node.add(makeNode("Dest Network = " + rip_entry.net.getHostAddress(),  current_offset, 4)); 
			current_offset += 4;
			entry_node.add(makeNode("Mask = " + rip_entry.mask.getHostAddress(), current_offset, 4));
			current_offset += 4;
			entry_node.add(makeNode("Cost = " + rip_entry.metric,  current_offset, 4));
			current_offset += 4;
			entry_node.add(makeNode("TTL = " + rip_entry.ttl, current_offset, 2));
			current_offset += 2; 

		}
		
		return root;
	}

}
