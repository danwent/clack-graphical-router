package net.clackrouter.ethereal;

import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import net.clackrouter.packets.VNSPacket;

public class EtherealTableModel extends AbstractTableModel implements ListSelectionListener, TreeSelectionListener {
	
	private JTree tree;
	private HexTable hexTable;
	private TableSorter sorter = null;

	EtherealTableModel(JTree tree, HexTable hexTable) {
		this.tree = tree;
		this.hexTable = hexTable;
	}
	
	final public static String[] columnNames = {"Num", "Time", "Source", "Destination", "Protocol", "Info", "Interface"};
	private ArrayList<EtherealPacket> packets = new ArrayList<EtherealPacket>();
	EtherealPacket curPacket;
	
	public String getColumnName(int i) { return columnNames[i]; }
	public void setSorter(TableSorter s) { sorter = s; }
	
	public void addPacket(VNSPacket packet, long cur_time){
		addPacket(packet, "*", cur_time);  // call other addPacket, with unknown iface
	}
	
	/** This will take in any generic VNSPacket and cast it to the proper packet and call makeStringArray on it **/
	public void addPacket(VNSPacket packet, String iface_name, long cur_time) {
		EtherealPacket p = new EtherealPacket(packet, iface_name, cur_time);
		packets.add(p);
		fireTableRowsInserted(packets.size()-1, packets.size()-1);
	}
	
	public int getRowCount() {
		return packets.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}
	
	public Class<?> getColumnClass(int column) {
		switch(column) {
		case 0: return Integer.class;
		case 1: return Double.class;
		case 2: return String.class;
		case 3: return String.class;
		case 4:
			return String.class;
		case 5:
			return String.class;
		case 6: 
			return String.class; // dw: added for interface name
		}
		return Object.class;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return packets.get(rowIndex).getRowData(columnIndex);
	}

	public void valueChanged(ListSelectionEvent e) {
		//if (e.getValueIsAdjusting() == true) return;
		
		
		if (e.getSource() != hexTable.getSelectionModel() && e.getSource() != hexTable.getColumnModel().getSelectionModel()) {
			int index = ((ListSelectionModel)e.getSource()).getMinSelectionIndex();
			if (index == -1) return;
			
			if (sorter != null) index = sorter.modelIndex(index);
			
			curPacket = packets.get(index);
			TreeNode rootNode = curPacket.getTree();
			TreeModel model = new DefaultTreeModel(rootNode);

			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			
			int level = 0;
			if (node != null)
				level = node.getLevel();
			
			tree.setModel(model);

			TableModel tableModel = curPacket.getTableModel();
			hexTable.setModel(tableModel);
			hexTable.clearSelection();

	 	   	Object[] hexLongValues = {"00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "ZZZZZZZZZZZZZZZZ"};
	 	   	Ethereal.initColumnSizes(hexTable, hexLongValues);
			
		} else {

			int col = hexTable.getColumnModel().getSelectionModel().getMinSelectionIndex();
			if (col == 16) return;
			
			
			int row = hexTable.getSelectionModel().getMinSelectionIndex();
			curPacket.hexSelectionChanged(row, col, tree, hexTable);
			tree.scrollPathToVisible(tree.getSelectionPath());
			tree.revalidate();
		}

	}

	public void valueChanged(TreeSelectionEvent e) {
		EtherealTreeNode node = (EtherealTreeNode) tree.getLastSelectedPathComponent();
		if (node != null)
			curPacket.treeSelectionChanged(node, hexTable);
	}

}
