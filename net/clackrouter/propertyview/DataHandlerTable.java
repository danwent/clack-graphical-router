package net.clackrouter.propertyview;


import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.clackrouter.component.base.ComponentDataHandler;

public class DataHandlerTable extends JTable {
	
	public TableModel model;
	public ComponentDataHandler handler;
	
	public DataHandlerTable(Object[][] data, ComponentDataHandler h) {
        super(new HandlerTableModel(data));	
        model = this.getModel();
        TableColumn col = getColumnModel().getColumn(1);
        col.setCellEditor(new HandlerCellEditor());
        handler = h;
	}
	
	public static class HandlerTableModel extends AbstractTableModel {
		String[] col_names = new String[]{"Label", "Value" };
		Object[][] data;
		
		public HandlerTableModel(Object[][] d){
			super();
			data = d;
		}
		
		public String getColumnName(int i){
			return col_names[i];
		}
	    public int getRowCount() { return data.length; }
	    public int getColumnCount() { return col_names.length; }
	    public Object getValueAt(int row, int col) {
	        return data[row][col];
	    }

	    public void setValueAt(Object value, int row, int col) {
	        data[row][col] = value;
	        fireTableCellUpdated(row, col);
	    }
		
		public boolean isCellEditable(int row, int col) {
			if(col == 1) return true;
			return false;
		}
	}
	
    public class HandlerCellEditor extends AbstractCellEditor implements TableCellEditor {
       
        JTextField component = new JTextField();
        int col,row;
        
        // This method is called when a cell value is edited by the user.
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row_index, int col_index) {

            col = col_index;
            row = row_index;
    
            component.setText((String)value);
            return component;
        }
    
        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        public Object getCellEditorValue() {
            return component.getText();
        }
    
        // This method is called just before the cell value
        // is saved. If the value is not valid, false should be returned.
        public boolean stopCellEditing() {
            String s = (String)getCellEditorValue();
            System.out.println("edit value " + s + " at row = "
            			+ row + " and col " + col);

            String label = (String)model.getValueAt(row,0);
            if(handler.acceptWrite(label, s)) return true;
            
            component.setText((String)model.getValueAt(row,col));
            return false;
        }
    }
 

}
