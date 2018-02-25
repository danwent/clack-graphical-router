package net.clackrouter.propertyview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.simplerouter.IPRouteLookup;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;

public class IPRouteLookupPview extends DefaultPropertiesView {
   
	private DefaultTableModel tableModel = null;
    private JTable rTable = null; 
    private String[] col_names;

    JTextField dest_text1 = null; 
    JTextField mask_text1 = null;
    JTextField next_text1 = null; 
  
    JTextField dest_text2 = null; 
    JTextField mask_text2 = null;
    JTextField if_text2   = null; 
    
    JButton local_add_button;
    JButton remote_add_button;
    
    JPanel config = null;
    JScrollPane route_scroll = null;
    ItemAdder adder;
    
    private static int MAX_NUM_VISIBLE_ROUTES = 7;
    
    public IPRouteLookupPview(IPRouteLookup lookup) {
    	super(lookup.getName(), lookup);

        tableModel = new DefaultTableModel();
        rTable = new JTable(tableModel);
        route_scroll = new JScrollPane();
    	
        adder = new ItemAdder();
        dest_text1 = new JTextField("0.0.0.0",9);
        mask_text1 = new JTextField("255.255.255.255",12);
        next_text1 = new JTextField("0.0.0.0",9);

        dest_text2 = new JTextField("0.0.0.0",10);
        mask_text2 = new JTextField("255.255.255.255",12);
        if_text2   = new JTextField("ethX",5);
        
        JPanel main = addMainPanel("IPRouteLookup");
        addHTMLDescription("IPRouteLookup.html");
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        
        JPanel config_panel = createConfigPanel();
        main.add(config_panel);
        addBorderToPanel(config_panel, "Configure Lookup Table");
    
        updatePropertiesFrame();
    }

    // this function is called anytime a route is changed, which is kind of heavy weight if you 
    // think about a dynamic routing protocol...
    private void fillRouteList()
    {
        IPRouteLookup forwarding = (IPRouteLookup)m_model;
    
        col_names = new String[]{"Destination",
                				"Next Hop",
                				"Net Mask",
                				"Iface"};
        
        RoutingTable table = forwarding.getRoutingTable(); 
        int num_entries = table.numEntries();
        Object[][] data = new Object[num_entries][col_names.length];
     

        for ( int i = 0 ; i < table.numEntries(); ++ i ){ 
        	RoutingEntry entry = table.getEntry(i);
        	data[i][0] = entry.network.getHostAddress();
        	if(entry.nextHop != null)
        		data[i][1] = entry.nextHop.getHostAddress();
        	else 
        		data[i][1] = "*";
            data[i][2] = entry.mask.getHostAddress();
            if(entry.interface_name == null)
            	data[i][3] = "local";
            else 
            	data[i][3] = entry.interface_name;
  
        }
        
        JTable oldtable = rTable;
        rTable = new JTable(data, col_names);
        rTable.setEnabled(false);
        rTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumn column = null;
        for (int i = 0; i < 4; i++) {
            column = rTable.getColumnModel().getColumn(i);
            if (i == 3) {
                column.setMaxWidth(50); //iface column is smaller
            } else if (i == 2){
                column.setMaxWidth(140); // mask is huge
            }else {
            	column.setMaxWidth(100);
            }
        }

        if(oldtable != null)
        	route_scroll.remove(oldtable);
        route_scroll.setViewportView(rTable);
       // int height = num_entries > MAX_NUM_VISIBLE_ROUTES ? MAX_NUM_VISIBLE_ROUTES : num_entries;
        route_scroll.setPreferredSize(new Dimension(370, 75));

        this.repaint();

    } // -- fillRouteList

    private JPanel createConfigPanel()
    {
        config = new JPanel();
 
       // config.add(route_scroll);
        
        fillRouteList();
        
        config.add(route_scroll);   
    
        JButton upButton =
            new JButton("Move Up");
        upButton.addActionListener(adder);
        JButton downButton =
            new JButton("Move Down");
        downButton.addActionListener(adder);
        JButton deleteButton =
            new JButton("Delete");
        deleteButton.addActionListener(adder);

        config.add(Box.createRigidArea(new Dimension(0,5)));

        JPanel del_panel = new JPanel();
        del_panel.setLayout(new BoxLayout(del_panel, BoxLayout.LINE_AXIS));
        del_panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        del_panel.add(deleteButton);
        del_panel.add(upButton);
        del_panel.add(downButton);
        del_panel.add(Box.createHorizontalGlue());

        
        config.add(del_panel);
        JPanel add_remote_panel = createAddRemotePanel(); 
        addBorderToPanel(add_remote_panel, "Add Non-local Route");
        JPanel add_local_panel = createAddLocalPanel();
        addBorderToPanel(add_local_panel, "Add Local Route");
        config.add(add_remote_panel);
        config.add(add_local_panel);


        rTable.addKeyListener(new MyKeyListener());   
        return config;
    } // -- createConfigPanel
    
    public void componentEvent(ClackComponentEvent e){
    	super.componentEvent(e);
    	if(e.mType == ClackComponentEvent.EVENT_DATA_CHANGE)
    		fillRouteList();
    }

    //  Temp disabled now b/c of move to JTable from JList
    private class MyKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent evt) {
            // Check for key codes.
            if (evt.getKeyCode() == KeyEvent.VK_DELETE) 
            { 
            	System.out.println("delete pressed");
            	RoutingEntry entry = getSelectedTableEntry();

                if ( entry == null )
                {
                    System.out.println("No entry selected");
                    return;
                }

                IPRouteLookup forwarding = (IPRouteLookup)m_model;

                if ( forwarding == null )
                { 
                    System.err.println("Incorrect model type for GUI_IPForwarding");
                    return;
                }

                RoutingTable rtable = forwarding.getRoutingTable();

                rtable.deleteEntry(entry);
                fillRouteList();
            }
        }
    }
   

    private JPanel createAddRemotePanel()
    {
    	JPanel outer = new JPanel();
    	outer.setLayout(new BorderLayout()); 
    	
        JPanel addp   = new JPanel();
        addp.setLayout(new SpringLayout()); 

        addp.add(new JLabel("Destination"));
        addp.add(new JLabel("Mask"));
        addp.add(new JLabel("Next Hop"));

        addp.add(dest_text1);
        addp.add(mask_text1);
        addp.add(next_text1);

       SpringUtilities.makeCompactGrid(addp,
                2, 3, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
     
      	outer.add(addp, BorderLayout.CENTER);
       	remote_add_button = new JButton("Add");
       	
       	outer.add(remote_add_button, BorderLayout.EAST);
        remote_add_button.addActionListener(adder);
        return outer;
    } // -- createAddPanel

    private JPanel createAddLocalPanel()
    {
    	JPanel outer = new JPanel();
    	outer.setLayout(new BorderLayout());
    	
        JPanel addp   = new JPanel();
        
        addp.setLayout(new SpringLayout()); 

        addp.add(new JLabel("Destination"));
        addp.add(new JLabel("Mask"));
        addp.add(new JLabel("Interface"));
        
        addp.add(dest_text2);
        addp.add(mask_text2);
        addp.add(if_text2);
        

       SpringUtilities.makeCompactGrid(addp,
                2, 3, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
                
       	outer.add(addp, BorderLayout.CENTER);
       	local_add_button = new JButton("Add");
        local_add_button.addActionListener(adder);
       	outer.add(local_add_button, BorderLayout.EAST);
        return outer;
    } // -- createAddPanel
    

    private class ItemAdder implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            IPRouteLookup forwarding = (IPRouteLookup)m_model;          
            if ( forwarding == null )
            { 
                System.err.println("Incorrect model type for GUI_IPForwarding");
                return;
            }

            RoutingTable clack_routing_table = forwarding.getRoutingTable();
            String cmd =  event.getActionCommand();

            if(local_add_button == event.getSource()) {

            	try {
            		InetAddress net = InetAddress.getByName(dest_text2.getText());
            		InetAddress mask = InetAddress.getByName(mask_text2.getText());
            		RoutingEntry new_entry = new RoutingEntry(net, null, mask, if_text2.getText());
            		forwarding.getRoutingTable().addLocalEntry(new_entry);
            			
            	}catch(Exception e){ 
            		m_model.showErrorDialog(e.getMessage());
            		e.printStackTrace(); 
				}
            }else if(remote_add_button == event.getSource()) {

                	try {
                		InetAddress net = InetAddress.getByName(dest_text1.getText());
                		InetAddress mask = InetAddress.getByName(mask_text1.getText());
                		InetAddress next_hop = InetAddress.getByName(next_text1.getText());
                		RoutingEntry new_entry = new RoutingEntry(net,next_hop, mask, null);
                		forwarding.getRoutingTable().addRemoteEntry(new_entry);
                			
                	}catch(Exception e){ 
                		m_model.showErrorDialog(e.getMessage());
                		e.printStackTrace(); 
    				}

            } else {
            		RoutingEntry entry = getSelectedTableEntry();

                	if(entry == null){
                		m_model.showErrorDialog("No entry is currently selected");
                		return;
                	}
            		
                	if(cmd.equals("Move Down")){ 
                		clack_routing_table.moveEntryDown(entry);
                	}else if(cmd.equals("Move Up")){
                		clack_routing_table.moveEntryUp(entry);           	
                	}else if(cmd.equals("Delete")){
                		clack_routing_table.deleteEntry(entry);
                	}
            	
            }

            fillRouteList();           
            
        }
    } // -- class ItemAdder
    
	private RoutingEntry getSelectedTableEntry() {
        IPRouteLookup forwarding = (IPRouteLookup)m_model;          
        if ( forwarding == null ) { 
            System.err.println("Incorrect model type for IPRouteLookupPview");
            return null;
        }

        RoutingTable clack_routing_table = forwarding.getRoutingTable();
    	ListSelectionModel lsm = rTable.getSelectionModel();

        if (lsm.isSelectionEmpty()) {
            System.err.println("No entry selected!");
            return null;
        }
            	
        try {
            int selectedRow = lsm.getMinSelectionIndex();
            String net_str = (String)rTable.getModel().getValueAt(selectedRow, 0);
            String mask_str = (String)rTable.getModel().getValueAt(selectedRow, 2);
        	InetAddress net = InetAddress.getByName(net_str);
        	InetAddress mask = InetAddress.getByName(mask_str);
        	return clack_routing_table.getRouteForPrefix(net, mask);
        } catch(Exception e){
        	e.printStackTrace();
        }
        return null;
	}

}
