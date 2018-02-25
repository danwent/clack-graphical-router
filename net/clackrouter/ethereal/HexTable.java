package net.clackrouter.ethereal;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class HexTable extends JTable {

	ArrayList<Point> cells = new ArrayList<Point>();
	
	public void setCellRenderer(int row, int column) {
		cells.add(new Point(row, column));
		this.repaint();
	}
	
	@Override
	public void clearSelection() {
		// TODO Auto-generated method stub
		super.clearSelection();
		resetRenderers();
	}
	
	public void resetRenderers() {
		if (cells != null) cells.clear();
	}
	
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (cells.contains(new Point(row, column))) return ColouredCell.getInstnace();
        return super.getCellRenderer(row, column);
    }
	
}
