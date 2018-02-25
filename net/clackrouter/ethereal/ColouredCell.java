package net.clackrouter.ethereal;

import java.awt.Color;

import javax.swing.table.DefaultTableCellRenderer;

public class ColouredCell extends DefaultTableCellRenderer {

	private ColouredCell() { super(); }
	
	private static ColouredCell instance; 
	
	public static ColouredCell getInstnace() {
		if (instance == null) {
			instance = new ColouredCell();
			instance.setBackground(Color.CYAN);
		}
		return instance;
	}
	
	
	
}
