package net.clackrouter.ethereal;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.clackrouter.ethereal.parser.PacketParser;
import net.clackrouter.ethereal.parser.Registry;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSICMPPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.packets.VNSUDPPacket;

public class EtherealPacket {

	/* Cached Display Data */
	private Object[] headerData = new Object[EtherealTableModel.columnNames.length];
	private EtherealTreeNode[] corelation;
	
	DefaultMutableTreeNode tree = null;

	TableModel hexTable = null;

	private VNSPacket packet;

	private PacketParser parser;

	private static int curNum = 0;

	private static long startTime = 0;
	
	public EtherealPacket(VNSPacket p, String ifacename, long curTime) {
		int num;
		double time;
		this.packet = p;
		num = curNum++;
		if (startTime == 0)
			startTime = curTime;
		time = (curTime - startTime) / 1000.0;
		headerData[0] = num;
		headerData[1] = time;

		// Asume we are an ethernet packet for now
		//parser = Registry.lookup("Ethernet", null);
		if (packet instanceof VNSEthernetPacket) {
			parser = Registry.lookup("Ethernet", null);
		} else if (packet instanceof IPPacket) {
			parser = Registry.lookup("IPv4", null);
		} else if (packet instanceof VNSARPPacket) {
			parser = Registry.lookup("ARP", null);
        } else if (packet instanceof VNSICMPPacket) {
			parser = Registry.lookup("ICMP", null);
        } else if (packet instanceof VNSTCPPacket) {
			parser = Registry.lookup("TCP", null);
        } else if (packet instanceof VNSUDPPacket) {
			parser = Registry.lookup("UDP", null);
        } else {
			parser = Registry.lookup(null, null);
        }

		headerData[2] = parser.getSrc(getArray());
		headerData[3] = parser.getDst(getArray());
		headerData[4] = parser.getProtocol(getArray());
		headerData[5] = parser.getInfo(getArray());
		headerData[6] = ifacename;
		
	}

	public byte[] getArray() {
		return packet.getByteBuffer().array();
	}

	public Object getRowData(int columnIndex) {
		return headerData[columnIndex];
	}

	public TableModel getTableModel() {
		if (hexTable == null)
			hexTable = parser.getTableModel(getArray());
		return hexTable;
	}

	public TreeNode getTree() {
		// TODO Auto-generated method stub
		if (tree == null) {
			tree = new EtherealTreeNode("Packet (size " + packet.getLength()
					+ " bytes)", getArray());
			MutableTreeNode n = parser.getTree(getArray());
			if (n != null)
				tree.add(n);
		}
		
		corelation = new EtherealTreeNode[packet.getLength()];
		Enumeration en = tree.breadthFirstEnumeration();
		while (en.hasMoreElements()) {
			EtherealTreeNode node = (EtherealTreeNode) en.nextElement();
			for (int i=node.hexStart; i<node.hexEnd; i++) {
				corelation[i] = node;
			}
		}
		
		return tree;
	}

	public void hexSelectionChanged(int row, int col, JTree tree, HexTable hexTable) {
		if (row == -1 || col == -1) return;
		if (row * 16 + col >= packet.getLength()) return;
		
		EtherealTreeNode node = corelation[row * 16 + col];
		TreePath path = new TreePath(((DefaultTreeModel) tree.getModel() ).getPathToRoot(node));
		
		tree.setSelectionPath(path);
		
		treeSelectionChanged(node, hexTable);
	}

	public void treeSelectionChanged(EtherealTreeNode node, HexTable hexTable) {

		//hexTable.clearSelection();
		hexTable.resetRenderers();

		int row1 = node.getHexStart() / 16;
		int col1 = node.getHexStart() - 16 * row1;
		hexTable.setCellRenderer(row1, col1);
		int row2 = node.getHexEnd() / 16;
		int col2 = node.getHexEnd() - 16 * row2;

		if (row1 == row2)
			for (int i = col1; i < col2; i++)
				hexTable.setCellRenderer(row2, i);
		else {

			for (int i = col1; i < 16; i++)
				hexTable.setCellRenderer(row1, i);

			for (int i = row1 + 1; i < row2; i++)
				for (int j = 0; j < 16; j++)
					hexTable.setCellRenderer(i, j);

			for (int i = 0; i < col2; i++)
				hexTable.setCellRenderer(row2, i);
		}

	}

}
