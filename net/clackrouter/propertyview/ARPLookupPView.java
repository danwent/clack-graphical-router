package net.clackrouter.propertyview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.simplerouter.ARPLookup;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;


public class ARPLookupPView extends DefaultPropertiesView {
	
	private DefaultTableModel tableModel = null;
    private JTable rTable = null; 
    private JPanel config = null;
    private JScrollPane scroll_pane = null;

	    JTextField hardware_text = null; 
	    JTextField ip_text = null; 
	    JTextField interface_text = null; 

	    public ARPLookupPView(ARPLookup lookup) {
	    	super(lookup.getName(), lookup);
	        tableModel = new DefaultTableModel();
	        rTable = new JTable(tableModel);
	    	
	        hardware_text = new JTextField("00:00:00:00:00:00");
	        ip_text = new JTextField("0.0.0.0");
	        interface_text = new JTextField("ethX");
	        
	        JPanel main = addMainPanel("ARPLookup");
	        
	        addHTMLDescription("ARPLookup.html");
	        addPanelToTabPane("Ports", m_port_panel);
	        refreshPortTab();
	        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
	        
	        JPanel config_panel = createConfigPanel();
	        main.add(config_panel);
	        addBorderToPanel(config_panel, "Configure ARP Cache");
	        
	        updatePropertiesFrame();
	    }
	    
	    
	    public void componentEvent(ClackComponentEvent e){
	    	super.componentEvent(e);
	    	if(e.mType == ClackComponentEvent.EVENT_DATA_CHANGE)
	    		fillCacheList();
	    }

	    
	    private void fillCacheList(){
	        ARPLookup lookup = (ARPLookup)m_model;
	        Hashtable mapOfMaps = lookup.getARPCache();
	        System.out.println("Filling ARP cache table");
	        
	        if ( lookup == null )
	        { 
	            System.err.println("Incorrect model type for ARPLookupPopup");
	            return;
	        }
	        
	        String[] col_names = new String[]{"IP Address",
    				"Hardware Address",
    				"Iface"};
	        
	        int num_entries = 0;
	        for(Enumeration e_ifaces = mapOfMaps.keys(); e_ifaces.hasMoreElements();  ){
	        	String iface_name = (String)e_ifaces.nextElement();
	        	Hashtable cache = (Hashtable)mapOfMaps.get(iface_name);
	        	num_entries += cache.size();
	        }

	        Object[][] data = new Object[num_entries][col_names.length];
	        
	        int i = 0;
	        for(Enumeration e_ifaces = mapOfMaps.keys(); e_ifaces.hasMoreElements();  ){
	        	String iface_name = (String)e_ifaces.nextElement();
	        	Hashtable cache = (Hashtable)mapOfMaps.get(iface_name);
	        	for(Enumeration e_entry = cache.keys(); e_entry.hasMoreElements(); ){
	        		InetAddress ip = (InetAddress) e_entry.nextElement();
	        		ARPLookup.CacheEntry entry = (ARPLookup.CacheEntry) cache.get(ip);
	        		data[i][0] = ip.getHostAddress();
	        		data[i][1] = entry.mEthAddr.toString();
	        		data[i][2] = iface_name;
	        		i++;
	        	}
	        }
	        if(rTable != null)
	        	scroll_pane.remove(rTable);
	        rTable = new JTable(data, col_names);
	        
	        rTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	        TableColumn column = null;
	        for (int j = 0; j < 3; j++) {
	            column = rTable.getColumnModel().getColumn(j);
	            if (j == 2) {
	                column.setMaxWidth(50); //iface column is smaller
	            } else {
	                column.setMaxWidth(120);
	            }
	        }
	        
	        
	        scroll_pane.setViewportView(rTable);
	        scroll_pane.setPreferredSize(new Dimension(270, (i+2) * 20));
	        this.repaint();

	    } // -- fillRouteList

	    private JPanel createConfigPanel() {
	        config = new JPanel();
	        scroll_pane = new JScrollPane(rTable);
	        scroll_pane.setMaximumSize(new Dimension(120, 75));
	        
	        fillCacheList();
	        JButton addButton =
	            new JButton("Add");
	        addButton.addActionListener(new ItemAdder());
	        JButton deleteButton =
	            new JButton("Delete");
	        deleteButton.addActionListener(new ItemAdder());

	        config.add(Box.createRigidArea(new Dimension(0,5)));
	        config.add(scroll_pane);
	        
	        JPanel del_panel = new JPanel();
	        //del_panel.setLayout(new BoxLayout(del_panel, BoxLayout.LINE_AXIS));
	        del_panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
	        del_panel.add(addButton);
	        del_panel.add(deleteButton);
	        del_panel.add(Box.createHorizontalGlue());
	        del_panel.setMaximumSize(new Dimension(150, 70));
	        
	        JPanel add_panel = createAddPanel(); 
	        add_panel.setMaximumSize(new Dimension(100, 70));
	        
	        //config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));		
	       // config.add(route_panel);
	        config.add(add_panel);
	        config.add(del_panel);

	       // rlist.addKeyListener(new MyKeyListener());

	     //   config.setMaximumSize(new Dimension(280,300));
	        
	        return config;
	    } // -- createConfigPanel

	    private class MyKeyListener extends KeyAdapter {
	        public void keyPressed(KeyEvent evt) {
	            // Check for key codes.
	            if (evt.getKeyCode() == KeyEvent.VK_DELETE) 
	            { 
	            	deleteSelectedEntry();
	            }
	        }
	    }
	
	    
	    private void deleteSelectedEntry(){
	    	
	          ARPLookup lookup = (ARPLookup)m_model;

	          if ( lookup == null )
	          { 
	              System.err.println("Incorrect model type for ARPLookupComponent");
	              System.exit(-1);
	          }
	 
	    	
           	ListSelectionModel lsm = rTable.getSelectionModel();

            if (lsm.isSelectionEmpty()) {
                System.err.println("No entry selected!");
                return;
            }
              
        	
            try {
                int selectedRow = lsm.getMinSelectionIndex();
                String next_hop_str = (String)rTable.getModel().getValueAt(selectedRow, 0);
                String iface_str = (String)rTable.getModel().getValueAt(selectedRow, 2);
        		
            	InetAddress next_hop = InetAddress.getByName(next_hop_str);
            	boolean success = lookup.removeARPCacheEntry(iface_str,next_hop);
            	
            	if(!success) System.err.println("Failed to Delete ARP entry for iface = " 
            			+ iface_str + " and next_hop = " + next_hop_str);
            }catch (Exception e){
            	e.printStackTrace();
            }
            
          fillCacheList();
	    }

	    
	    private JPanel createAddPanel()
	    {
	        JPanel addp   = new JPanel();

	        addp.setLayout(new SpringLayout()); 

	        JLabel hw = new JLabel("Hardware Address");
	        JLabel ip = new JLabel("IP Address");
	        JLabel iface = new JLabel("Interface");
	        addp.add(ip);
	        addp.add(hw);
	        addp.add(iface);
	        addp.add(ip_text);
	        addp.add(hardware_text);
	        addp.add(interface_text);

	       SpringUtilities.makeCompactGrid(addp,
	                2, 3, //rows, cols
	                5, 5, //initialX, initialY
	                5, 5);//xPad, yPad
	                
	        return addp;
	    } // -- createAddPanel

	    protected void updatePropertiesFrame(){
	    	super.updatePropertiesFrame();
	    	fillCacheList();
	    }
	    

	    private class ItemAdder implements ActionListener 
	    {
	        public void actionPerformed(ActionEvent event) 
	        {
	            ARPLookup lookup = (ARPLookup)m_model;          
	            if ( lookup == null )
	            { 
	                System.err.println("Incorrect model type for " + this.getClass().getName());
	                System.exit(-1);
	            }

	            Hashtable mapOfMaps = lookup.getARPCache();
	            String cmd =  event.getActionCommand();
	            

	            if(cmd.equals("Add")) {
	            	String hardware = hardware_text.getText();
	            	String ip_str = ip_text.getText();
	            	String iface = interface_text.getText();
	            	if ( hardware.equals("") ||	ip_str.equals("") || iface.equals("") )
	            	{ 
	            		System.out.println(" At least one field empty");
	            		return; 
	            	}

	            	try {
	            		Hashtable iface_cache = (Hashtable)mapOfMaps.get(iface);
	            		if(iface_cache == null) {
	            			System.err.println("Adding arp entry with unknown interface");
	            			return;
	            		}
	            		EthernetAddress hw = EthernetAddress.getByAddress(hardware);
	            		InetAddress ip = InetAddress.getByName(ip_str);
	            		
	            		lookup.addARPCacheEntry(iface,ip, hw);
	            		
	            	}catch(UnknownHostException e)
					{ System.err.println(e); e.printStackTrace(); }

	            }else if(cmd.equals("Delete")){
	            	deleteSelectedEntry();
	            }
         	 	fillCacheList();
	        }
	    } // -- class ItemAdder
	   
	
}
