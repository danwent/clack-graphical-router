package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class ICMPParser extends PacketParser {

	ICMPParser() {
		typeStart = 0;
		typeLen = 1;
		dataStart = 4;
		dataLen = 0;
	}
	
	public String getTypeName(byte[] packet) {
		String type = null;
		switch(getType(packet)) {
		case  0	 : type = "Echo reply"; break;
		case  1	 : type = "Reserved"; break;
		case  2	 : type = "Reserved"; break;
		case  3	 : type = "Destination unreachable"; break;
		case  4	 : type = "Source quench"; break;
		case  5	 : type = "Redirect"; break;
		case  6	 : type = "Alternate Host Address"; break;
		case  7	 : type = ""; break;
		case  8	 : type = "Echo request"; break;
		case  9	 : type = "Router advertisement"; break;
		case  10	 : type = "Router solicitation"; break;
		case  11	 : type = "Time exceeded"; break;
		case  12	 : type = "Parameter problem"; break;
		case  13	 : type = "Timestamp request"; break;
		case  14	 : type = "Timestamp reply"; break;
		case  15	 : type = "Information request"; break;
		case  16	 : type = "Information reply"; break;
		case  17	 : type = "Address mask request"; break;
		case  18	 : type = "Address mask reply"; break;
		case  19	 : type = "Reserved (for security)"; break;
		case  20	 :
		case  21:
		case 22:
		case 23:
		case 24:
		case 25:
		case 26:
		case 27:
		case 28:
		case  29	 : type = "Reserved (for robustness experiment)"; break;
		case  30	 : type = "Traceroute"; break;
		case  31	 : type = "Conversion error"; break;
		case  32	 : type = "Mobile Host Redirect"; break;
		case  33	 : type = "IPv6 Where-Are-You"; break;
		case  34	 : type = "IPv6 I-Am-Here"; break;
		case  35	 : type = "Mobile Registration Request"; break;
		case  36	 : type = "Mobile Registration Reply"; break;
		case  37	 : type = "Domain Name request"; break;
		case  38	 : type = "Domain Name reply"; break;
		case  39	 : type = "SKIP Algorithm Discovery Protocol"; break;
		case  40	 : type = "Photuris, Security failures"; break;
		case  41	 : type = "Experimental mobility protocols"; break;
		}
		return type;
	}
	
	private String getCodeName(byte[] packet){
		
		if(getType(packet) == 3){ // check for dest-unreachable
			switch(packet[1]){
			case 0 : return "network unreachable";
			case 1 : return "host unreachable";
			case 2 : return "protocol unreachable";
			case 3 : return "port unreachable";
			case 4 : return "fragmentation needed but DF bit set";
			case 5 : return "source route not reachable";
			}
		}
		return "" + packet[1];
	}
	
	@Override
	public EtherealTreeNode getTree(byte[] packet) {

		EtherealTreeNode root = new EtherealTreeNode("ICMP Packet (" + packet.length +" bytes)", packet);
				
		EtherealTreeNode header = new EtherealTreeNode("ICMP Packet Header (4 bytes)", 0, 4);
		root.add(header);
		
		EtherealTreeNode.startParse();
		
		header.add(new EtherealTreeNode("Type = " + getTypeName(packet), 1));
		
		header.add(new EtherealTreeNode("Code = " + getCodeName(packet), 1));
		header.add(new EtherealTreeNode("Checksum = 0x" + hexFormat(packet, 2, 2), 2));

		root.add(Registry.lookup("ICMP", getType(packet)).getTree(strip(packet)).ofset(4));
		
		return root;
	}

	@Override
	public String getProtocol(byte[] packet) {
		return "ICMP";
	}

	@Override
	public String getInfo(byte[] packet) {
		if(packet[0] == 3)
			return "ICMP (" + getTypeName(packet) + " - " + getCodeName(packet) + ")";
		return "ICMP (" + getTypeName(packet) + ")";
	}
	
}
