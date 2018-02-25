package net.clackrouter.ethereal;


import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.ethereal.parser.Registry;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.EtherealPopup;
import net.clackrouter.router.core.Router;

public class Ethereal extends ClackComponent {

	public static int PORT_IN = 0, PORT_OUT = 1;
	public static int NUM_PORTS = 2;
	private static int ethernum = 0;
	
	private EtherealTableModel model;
	private JFrame frame;
	
	public Ethereal(Router r, String name){		
		//super(r, name + " " + ethernum);

		super(r, name);
	
		setupPorts(NUM_PORTS);
		Registry.init();
		
		String header = "Ethereal: " + r.getHost();
		makeFrame(header);
	//	setName("Ethereal " + ethernum);
		
		ethernum+=1;
		
	}
	
    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    public static void initColumnSizes(JTable table, Object[] longValues) {
        TableModel model = table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < longValues.length; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 table, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 table, longValues[i],
                                 false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    
    private void makeFrame(String name) {

    	
    	JTable table = new JTable();
 	   	JScrollPane scroll1 = new JScrollPane(table);
    	
    	JTree tree = new JTree((TreeModel)null);
    	
    	JScrollPane scroll2 = new JScrollPane(tree);
    	HexTable hexTable = new HexTable();
    	JScrollPane scroll3 = new JScrollPane(hexTable);

    	
    	// Without sorter
    	//table.setModel(model);

    	model = new EtherealTableModel(tree, hexTable);
    	
    	// With sorter
    	TableSorter sorter = new TableSorter(model);
    	table.setModel(sorter);
    	sorter.setTableHeader(table.getTableHeader());
    	
    	model.setSorter(sorter);
    	
    	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	table.getSelectionModel().addListSelectionListener(model);
    	 	
    	
		tree.addTreeSelectionListener(model);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		hexTable.setRowSelectionAllowed(true);
		hexTable.setColumnSelectionAllowed(true);
		hexTable.setCellSelectionEnabled(true);
    	hexTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	hexTable.getSelectionModel().addListSelectionListener(model);
    	hexTable.getColumnModel().getSelectionModel().addListSelectionListener(model);
    	

 	   	Object[] longValues = {1000, 100000, "255.255.255.255", "255.255.255.255", "Ethernet(IPv4(TCP(FTP)))", "A really long explanation of the data set to pad out the column"};
 	   	initColumnSizes(table, longValues);
 	   	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 	   	scroll1.setPreferredSize(table.getPreferredSize());
 	   	

 	   	
    	
    	frame = new JFrame(name); 
    	JSplitPane top = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll1, scroll2);
    	JSplitPane bottem = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, scroll3);
    	top.setDividerLocation(300);
    	bottem.setDividerLocation(600);
    	
    	//frame.add(scroll1, BorderLayout.NORTH);
    	//frame.add(scroll2, BorderLayout.CENTER);
    	//frame.add(scroll3, BorderLayout.SOUTH);
    	frame.getContentPane().add(bottem);  // bug fix for mac
    	frame.pack();
    	frame.setVisible(true);
    	//dw:  don't minimize at first
    	// frame.setState(JFrame.ICONIFIED);
	}

	protected void setupPorts(int numPorts)
    {
    	super.setupPorts(numPorts);
    	createInputPushPort(PORT_IN,"Input (to be analysed)", VNSPacket.class);
    	createOutputPushPort(PORT_OUT, "Output (Same as input - This ia a passthrough component)", VNSPacket.class);
    }
    
    
    public void acceptPacket(VNSPacket packet, int port_number){
		if(port_number == PORT_IN){
			model.addPacket(packet, getTime());
			m_ports[PORT_OUT].pushOut(packet);
		}else {
			/* impossible */
			System.err.println("Ethereal Received packet on invalid port");
		}
	}
    
    public void addPacket(VNSPacket packet, String iface_name){
    	model.addPacket(packet, iface_name, getTime());
    }
    
    public void setName(String name) {
    	super.setName(name);
    	frame.setTitle(name);
    }
    
    public boolean isModifying() { return false; }
    
    public void showWindow() {
    	frame.setVisible(true);
    }
    
    public JPanel getPropertiesView() {
    	return new EtherealPopup(this);
    	//frame.setVisible(true);
		//return null;
    }
    
}
