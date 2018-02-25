package net.clackrouter.ethereal.parser;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.clackrouter.ethereal.EtherealTreeNode;


public abstract class PacketParser {

	public String getSrc(byte[] packet) { return ""; };

	public String getDst(byte[] packet) { return ""; };

	public abstract String getProtocol(byte[] packet) ;

	public abstract String getInfo(byte[] packet) ;

	public abstract EtherealTreeNode getTree(byte[] packet) ;

	protected int typeStart=0;
	protected int typeLen =0;
	
	/** You can override this or just set variables typeStart and typeLen **/
	protected Integer getType(byte[] packet) {
		int ret = 0;
		for (int i=typeStart; i<typeStart + typeLen; i++) {
			ret *= 0x100;
			ret += packet[i];
		}
		return ret;
	}


	protected int dataStart=0;
	protected int dataLen =0;
	
	/** You can override this or just set variables dataStart and dataLen. If dataLen == 0 then it is set to be the packet.length - dataStart **/
	protected byte[] strip(byte[] packet) {
		// Copy it so we don't change the global variable
		int len = dataLen;
		if (len == 0) len = packet.length - dataStart;
		/*
		System.out.println("DataLen = " + dataLen);
		System.out.println("DataStart = " + dataStart);		
		*/
		byte[] ret = new byte[len];
		System.arraycopy(packet, dataStart, ret, 0, ret.length);
		return ret;
	}

	public TableModel getTableModel(byte[] bytes) { 
		// TODO Auto-generated method stub
		int len = (int) Math.ceil(bytes.length / 16.0);
		String[][] data = new String[len][16+1];
		for (int i=0; i < len; i++) {
			for (int j=0; j < 16; j++) {
				if (i*16 + j < bytes.length)
					data[i][j] = hexFormat(bytes[i*16 + j]);
				else 
					data[i][j] = "";
			}
			data[i][16] = charFormat(bytes, i*16, 16);
		}
		
		String[] columns = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "ASCII"};
		
		return new DefaultTableModel(data, columns) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
	}
	
	/* Useful For writing parsers */

	protected String nextProtocol(byte[] packet, String parserName) {
		return Registry.lookup(parserName, getType(packet)).getProtocol(strip(packet));
	}
	protected String nextInfo(byte[] packet, String parserName) {
		return Registry.lookup(parserName, getType(packet)).getInfo(strip(packet));
	}
	protected EtherealTreeNode nextNode(byte[] packet, String parserName, int headerLen) {
		return Registry.lookup(parserName, getType(packet)).getTree(strip(packet)).ofset(headerLen);
	}
	
	protected EtherealTreeNode makeNode(String string, int start, int len) {
		return new EtherealTreeNode(string, start, start + len);
	}
	
	protected EtherealTreeNode makeNode(String string, int value, int start, int len) {
		string += " = " + value;
		return new EtherealTreeNode(string, start, start + len);
	}
	
	protected EtherealTreeNode makeNode(String string, byte[] packet, int start, int len) {
		long num = 0;
		for (int i=start; i < start + len; i++) {
			num *= 0x100;
			num += (int) packet[i];
		}
		string += " = " + num;
		return new EtherealTreeNode(string, start, start + len);
	}
	
	
	
	/* Support methods */
    public final static String hex[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

	public static String MACFormat(byte[] b) { return MACFormat(b, 0, b.length); }
	public static String MACFormat(byte[] b, int ofset, int len) {
		StringBuffer ret = new StringBuffer();
		// Watch out for overflow
		if (ofset+len > b.length) len = b.length - ofset;
		
		for (int i=ofset; i < ofset+len-1; i++) {
			ret.append(hexFormat(b[i]));
			ret.append(":");
		}
		ret.append(hexFormat(b[ofset+len-1]));
		return ret.toString();
	}
	public static String decimalFormat(byte[] b) { return decimalFormat(b, 0, b.length); }
	public static String decimalFormat(byte[] b, int ofset, int len) {
		StringBuffer ret = new StringBuffer();
		// Watch out for overflow
		if (ofset+len > b.length) len = b.length - ofset;
		
		for (int i=ofset; i < ofset+len-1; i++) {
			ret.append(0xFF & b[i]);
			ret.append(".");
		}
		ret.append(0xFF & b[ofset+len-1]);
		return ret.toString();
	}
	public static String hexFormat(byte b) {
	    return hex[(b & 0xF0) >> 4] + hex[b & 0x0F]; 
	}
	public String hexFormat(byte[] b, int ofset, int len) {
		StringBuffer ret = new StringBuffer();
		// Watch out for overflow
		if (ofset+len > b.length) len = b.length - ofset;
		
		for (int i=ofset; i < ofset+len; i++) {
			ret.append(hexFormat(b[i]));
		}
		// TODO Auto-generated method stub
		return ret.toString();
	}
	public static String charFormat(byte[] b) { return charFormat(b, 0, b.length); }
	public static String charFormat(byte[] b, int ofset, int len) {
		StringBuffer ret = new StringBuffer();
		// Watch out for overflow
		if (ofset+len > b.length) len = b.length - ofset;
		
		for (int i=ofset; i < ofset+len; i++) {
			ret.append((char) b[i]);
		}
		return ret.toString();
	}
	
	

}
