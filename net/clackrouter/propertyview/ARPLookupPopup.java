/*
 * Created on Jan 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
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
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.simplerouter.ARPLookup;
import net.clackrouter.netutils.EthernetAddress;




/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ARPLookupPopup extends DefaultPropertiesView {
    
	  private DefaultListModel sampleModel = null;
	    JList rlist = null; 

	    JTextField hardware_text = null; 
	    JTextField ip_text = null; 
	    JTextField interface_text = null; 

	    public ARPLookupPopup(ARPLookup lookup) {
	    	super(lookup.getName(), lookup);
	        sampleModel = new DefaultListModel();
	        rlist = new JList(sampleModel);
	    	
	        hardware_text = new JTextField();
	        ip_text = new JTextField();
	        interface_text = new JTextField();
	        
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

	    
	    private void fillCacheList()
	    {
	        ARPLookup lookup = (ARPLookup)m_model;
	        Hashtable mapOfMaps = lookup.getARPCache();
	        
	        if ( lookup == null )
	        { 
	            System.err.println("Incorrect model type for ARPLookupPopup");
	            System.exit(-1);
	        }

	        sampleModel.clear();
	        
	        for(Enumeration e_ifaces = mapOfMaps.keys(); e_ifaces.hasMoreElements();  ){
	        	String iface_name = (String)e_ifaces.nextElement();
	        	Hashtable cache = (Hashtable)mapOfMaps.get(iface_name);
	        	for(Enumeration e_entry = cache.keys(); e_entry.hasMoreElements(); ){
	        		InetAddress ip = (InetAddress) e_entry.nextElement();
	        		ARPLookup.CacheEntry entry = (ARPLookup.CacheEntry) cache.get(ip);
	        		sampleModel.addElement(entry.toString() + " " + iface_name);
	        	}
	        }

	    } // -- fillRouteList

	    private JPanel createConfigPanel()
	    {
	        JPanel config = new JPanel();
	     
	        fillCacheList();

	        // -- Routing Table List
	        rlist.setMaximumSize(new Dimension(70, 70));
	        rlist.setVisibleRowCount(4);
	        rlist.setAlignmentX(LEFT_ALIGNMENT);
	        JScrollPane scrollableList = new JScrollPane(rlist);
	        scrollableList.setMaximumSize(new Dimension(75, 75));
	        
	        JButton addButton =
	            new JButton("Add");
	        addButton.addActionListener(new ItemAdder());
	        JButton deleteButton =
	            new JButton("Delete");
	        deleteButton.addActionListener(new ItemAdder());

	        //JPanel route_panel = new JPanel();
	        //route_panel.setLayout(new FlowLayout());
	        
	        JLabel label = new JLabel("IP Address / Hardware Address / Interface");
	        config.add(label);
	        config.add(Box.createRigidArea(new Dimension(0,5)));
	        config.add(scrollableList);
	        //config.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	        //config.setMaximumSize(new Dimension(150, 70));
	        
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

	        rlist.addKeyListener(new MyKeyListener());

	        config.setMaximumSize(new Dimension(280,300));
	        
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
            String entry  = (String)rlist.getSelectedValue();

            if ( entry == null )
            {
                System.out.println("No entry selected");
                return;
            }

            ARPLookup lookup = (ARPLookup)m_model;

            if ( lookup == null )
            { 
                System.err.println("Incorrect model type for ARPLookupComponent");
                System.exit(-1);
            }
            
            String[] tokens = entry.split(" ");
            boolean success = false;
            try {
            	success = lookup.removeARPCacheEntry(tokens[3], InetAddress.getByName(tokens[0]));
            } catch(Exception e){
            	e.printStackTrace();
            }

            if(success == false) System.out.println("Failed to remove ARP entry");
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



	    // -- 
	    // this implements the VNSComponentListener interface
	    // whenever the model is changed this function will be called.
	    // -- 

	    public void componentEvent(ClackComponentEvent e) 
	    {  
	    	super.componentEvent(e);
	    	if(e.mType == ClackComponentEvent.EVENT_DATA_CHANGE){
	    		fillCacheList();
	    	}
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

	            	hardware_text.setText("");
	            	ip_text.setText("");
	            	interface_text.setText("");

	            }else if(cmd.equals("Delete")){
	            	deleteSelectedEntry();
	            }
           	 	fillCacheList();
	        }
	    } // -- class ItemAdder
	   
	    

}
