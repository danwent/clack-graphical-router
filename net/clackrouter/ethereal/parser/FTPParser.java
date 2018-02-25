package net.clackrouter.ethereal.parser;

import net.clackrouter.ethereal.EtherealTreeNode;

public class FTPParser extends PacketParser {
	
	@Override
	public String getProtocol(byte[] packet) {
		return "FTP";
	}
	
	@Override
	public String getInfo(byte[] packet) {
		String ret = "";
		if (packet.length == 0) return "FTP Empty Packet";
		if (Character.isDigit(packet[0])) {
			ret += "FTP Reply: ";
			int code = (packet[0] - '0') * 100 + (packet[1] - '0') * 10 + (packet[2] - '0');
			
			switch(code) {case 110 : ret += "Restart marker reply."; break;
			case 120 : ret += "Service ready in nnn minutes."; break;
			case 125 : ret += "Data connection already open; transfer starting."; break;
			case 150 : ret += "File status okay; about to open data connection."; break;
			case 200 : ret += "Command okay."; break;
			case 202 : ret += "Command not implemented, superfluous at this site."; break;
			case 211 : ret += "System status, or system help reply."; break;
			case 212 : ret += "Directory status."; break;
			case 213 : ret += "File status."; break;
			case 214 : ret += "Help message."; break;
			case 215  : ret += "NAME system type."; break;
			case 220  : ret += "Service ready for new user."; break;
			case 221  : ret += "Service closing control connection."; break;
			case 225  : ret += "Data connection open; no transfer in progress."; break;
			case 226  : ret += "Closing data connection."; break;
			case 227  : ret += "Entering Passive Mode <h1,h2,h3,h4,p1,p2>."; break;
			case 228  : ret += "Entering Long Passive Mode."; break;
			case 229  : ret += "Extended Passive Mode Entered."; break;
			case 230  : ret += "User logged in, proceed."; break;
			case 250  : ret += "Requested file action okay, completed."; break;
			case 257  : ret += "PATHNAME created."; break;
			case 331  : ret += "User name okay, need password."; break;
			case 332  : ret += "Need account for login."; break;
			case 350  : ret += "Requested file action pending further information."; break;
			case 421  : ret += "Service not available, closing control connection."; break;
			case 425  : ret += "Can't open data connection."; break;
			case 426  : ret += "Connection closed; transfer aborted."; break;
			case 450  : ret += "Requested file action not taken."; break;
			case 451  : ret += "Requested action aborted. Local error in processing."; break;
			case 452  : ret += "Requested action not taken."; break;
			case 500  : ret += "Syntax error, command unrecognized."; break;
			case 501  : ret += "Syntax error in parameters or arguments."; break;
			case 502  : ret += "Command not implemented."; break;
			case 503  : ret += "Bad sequence of commands."; break;
			case 504  : ret += "Command not implemented for that parameter."; break;
			case 521  : ret += "Supported address families are <af1, .., afn>"; break;
			case 522  : ret += "Protocol not supported."; break;
			case 530  : ret += "Not logged in."; break;
			case 532  : ret += "Need account for storing files."; break;
			case 550 : ret += "Requested action not taken."; break;
			case 551 : ret += "Requested action aborted. Page type unknown."; break;
			case 552 : ret += "Requested file action aborted."; break;
			case 553 : ret += "Requested action not taken."; break;
			case 554 : ret += "Requested action not taken: invalid REST parameter."; break;
			case 555 : ret += "Requested action not taken: type or stru mismatch."; break;
			}
		} else {
			ret += "FTP Request: ";
			String req = new String(packet, 0, 4); 
			System.out.println(req);
			if (req.equals("ABOR")) ret += "Abort.";
			if (req.equals("ACCT")) ret += "Account.";
			if (req.equals("ADAT")) ret += "Authentication/Security Data.";
			if (req.equals("ALLO")) ret += "Allocate.";
			if (req.equals("APPE")) ret += "Append.";
			if (req.equals("AUTH")) ret += "Authentication/Security Mechanism.";
			if (req.equals("CCC ")) ret += "Clear Command Channel.";
			if (req.equals("CDUP")) ret += "Change to parent directory.";
			if (req.equals("CONF")) ret += "Confidentiality Protected Command.";
			if (req.equals("CWD ")) ret += "Change working directory.";
			if (req.equals("DELE")) ret += "Delete.";
			if (req.equals("ENC ")) ret += "Privacy Protected Command.";
			if (req.equals("EPRT")) ret += "Extended Data port.";
			if (req.equals("EPSV")) ret += "Extended Passive.";
			if (req.equals("FEAT")) ret += "Feature.";
			if (req.equals("HELP")) ret += "Help.";
			if (req.equals("LANG")) ret += "Language negotiation.";
			if (req.equals("LIST")) ret += "List.";
			if (req.equals("LPRT")) ret += "Long data port.";
			if (req.equals("LPSV")) ret += "Long passive.";
			if (req.equals("MIC ")) ret += "Integrity Protected Command.";
			if (req.equals("MKD ")) ret += "Make directory.";
			if (req.equals("MODE")) ret += "Transfer mode.";
			if (req.equals("NLST")) ret += "Name list.";
			if (req.equals("NOOP")) ret += "No operation.";
			if (req.equals("OPTS")) ret += "Options.";
			if (req.equals("PASS")) ret += "Password.";
			if (req.equals("PASV")) ret += "Passive.";
			if (req.equals("PBSZ")) ret += "Protection Buffer Size.";
			if (req.equals("PORT")) ret += "Data port.";
			if (req.equals("PROT")) ret += "Data Channel Protection Level.";
			if (req.equals("PWD ")) ret += "Print working directory.";
			if (req.equals("QUIT")) ret += "Logout.";
			if (req.equals("REIN")) ret += "Reinitialize.";
			if (req.equals("REST")) ret += "Restart.";
			if (req.equals("RETR")) ret += "Retrieve.";
			if (req.equals("RMD ")) ret += "Remove directory.";
			if (req.equals("RNFR")) ret += "Rename from.";
			if (req.equals("RNTO")) ret += "Rename to.";
			if (req.equals("SITE")) ret += "Site parameters.";
			if (req.equals("SMNT")) ret += "Structure mount.";
			if (req.equals("STAT")) ret += "Status.";
			if (req.equals("STOR")) ret += "Store.";
			if (req.equals("STOU")) ret += "Store unique.";
			if (req.equals("STRU")) ret += "File structure.";
			if (req.equals("SYST")) ret += "System.";
			if (req.equals("TYPE")) ret += "Representation type.";
			if (req.equals("USER")) ret += "User name.";
			if (req.equals("XCUP")) ret += "Change to the parent of the current working directory.";
			if (req.equals("XMKD")) ret += "Make a directory.";
			if (req.equals("XPWD")) ret += "Print the current working directory.";
			if (req.equals("XRCP")) ret += " ";
			if (req.equals("XRMD")) ret += "Remove the directory.";
			if (req.equals("XRSQ")) ret += " ";
			if (req.equals("XSEM")) ret += "Send, Mail if cannot.";
			if (req.equals("XSEN")) ret += "Send to terminal.";
		}
		return ret;
	}
	
	@Override
	public EtherealTreeNode getTree(byte[] packet) {
		// TODO Auto-generated method stub
		EtherealTreeNode root = new EtherealTreeNode("FTP Packet (" + packet.length +" bytes)", packet);
		
		if (packet.length > 3) { 
			int len = 4;
			if (packet[3] != ' ') len = 5; // A 4 character command
			root.add(makeNode(getInfo(packet), 0, len));
		
		
			root.add(makeNode("Command Arguments: \"" + new String(packet, len, packet.length - len) + "\"", len, packet.length - len));
		}
		
		return root;
	}
	
}
