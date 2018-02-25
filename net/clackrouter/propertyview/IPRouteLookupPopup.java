/*
 * Created on Nov 28, 2004
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.clackrouter.component.simplerouter.IPRouteLookup;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;




/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IPRouteLookupPopup extends DefaultPropertiesView {


    private DefaultListModel sampleModel = null;
    JList rlist = null; 

    JTextField dest_text = null; 
    JTextField next_text = null; 
    JTextField mask_text = null; 
    JTextField if_text   = null; 

    public IPRouteLookupPopup(IPRouteLookup lookup) {
    	super(lookup.getName(), lookup);

        sampleModel = new DefaultListModel();
        rlist = new JList(sampleModel);
    	
        dest_text = new JTextField("0.0.0.0");
        next_text = new JTextField("0.0.0.0");
        mask_text = new JTextField("255.255.255.255");
        if_text   = new JTextField("ethX");
        
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

    
    private void fillRouteList()
    {
        IPRouteLookup forwarding = (IPRouteLookup)m_model;

        if ( forwarding == null )
        { 
            System.err.println("Incorrect model type for LookupIPRoutePopup");
            System.exit(-1);
        }

        sampleModel.clear();

        RoutingTable table = forwarding.getRoutingTable(); 
        for ( int i = 0 ; i < table.numEntries(); ++ i )
        { sampleModel.addElement(table.getEntry(i)); }

    } // -- fillRouteList

    private JPanel createConfigPanel()
    {
        JPanel config = new JPanel();

        fillRouteList();

        // -- Routing Table List
        
        rlist.setVisibleRowCount(sampleModel.getSize());
        
        rlist.setAlignmentX(CENTER_ALIGNMENT);
     //   JScrollPane scrollableList = new JScrollPane(rlist);
    //    scrollableList.setPreferredSize(new Dimension(300, 70));
        
        JButton addButton =
            new JButton("Add");
        addButton.addActionListener(new ItemAdder());
        JButton upButton =
            new JButton("Move Up");
        upButton.addActionListener(new ItemAdder());
        JButton downButton =
            new JButton("Move Down");
        downButton.addActionListener(new ItemAdder());
        JButton deleteButton =
            new JButton("Delete");
        deleteButton.addActionListener(new ItemAdder());

        //JPanel route_panel = new JPanel();
        //route_panel.setLayout(new BoxLayout(route_panel, BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel("Destination  Next Hop Mask Interface");
        config.add(label);
        config.add(Box.createRigidArea(new Dimension(0,5)));
        config.add(rlist);
      //  config.add(scrollableList);
        //route_panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel del_panel = new JPanel();
        del_panel.setLayout(new BoxLayout(del_panel, BoxLayout.LINE_AXIS));
        del_panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        del_panel.add(addButton);
        del_panel.add(deleteButton);
        del_panel.add(upButton);
        del_panel.add(downButton);
        del_panel.add(Box.createHorizontalGlue());

        JPanel add_panel = createAddPanel(); 

        //config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));		
        //config.add(route_panel);
        config.add(add_panel);
        config.add(del_panel);

        rlist.addKeyListener(new MyKeyListener());
        
        config.setMaximumSize(new Dimension(400,400));
        
        return config;
    } // -- createConfigPanel

    private class MyKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent evt) {
            // Check for key codes.
            if (evt.getKeyCode() == KeyEvent.VK_DELETE) 
            { 
                RoutingEntry entry  = (RoutingEntry)rlist.getSelectedValue();

                if ( entry == null )
                {
                    System.out.println("No entry selected");
                    return;
                }

                IPRouteLookup forwarding = (IPRouteLookup)m_model;

                if ( forwarding == null )
                { 
                    System.err.println("Incorrect model type for GUI_IPForwarding");
                    System.exit(-1);
                }

                RoutingTable rtable = forwarding.getRoutingTable();

                rtable.deleteEntry(entry);
                fillRouteList();
            }
        }
    }

    private JPanel createAddPanel()
    {
        JPanel addp   = new JPanel();

        addp.setLayout(new SpringLayout()); 

        JLabel destl = new JLabel("Destination");
        JLabel nextl = new JLabel("Next Hop");
        JLabel maskl = new JLabel("Mask");
        JLabel ifl   = new JLabel("Interface");

        addp.add(destl);
        addp.add(nextl);
        addp.add(maskl);
        addp.add(ifl);

        addp.add(dest_text);
        addp.add(next_text);
        addp.add(mask_text);
        addp.add(if_text);

       SpringUtilities.makeCompactGrid(addp,
                2, 4, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
                
        return addp;
    } // -- createAddPanel



    private class ItemAdder implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            IPRouteLookup forwarding = (IPRouteLookup)m_model;          
            if ( forwarding == null )
            { 
                System.err.println("Incorrect model type for GUI_IPForwarding");
                System.exit(-1);
            }

            RoutingTable rtable = forwarding.getRoutingTable();
            String cmd =  event.getActionCommand();

            if(cmd.equals("Add")) {
            	if ( dest_text.getText().trim().equals("") ||
            			next_text.getText().trim().equals("") ||
						mask_text.getText().trim().equals("") ||
						if_text.getText().trim().equals("") )
            	{ 
            		System.out.println(" At least one field empty");
            		return; 
            	}

            	try {
            		RoutingEntry new_entry = new RoutingEntry(InetAddress.getByName(dest_text.getText()),
            				InetAddress.getByName(next_text.getText()),
            				InetAddress.getByName(mask_text.getText()),
            				if_text.getText());
            		rtable.addEntry(new_entry);
            	}catch(UnknownHostException e)
				{ System.err.println(e); e.printStackTrace(); }

                dest_text.setText("0.0.0.0");
                next_text.setText("0.0.0.0");
                mask_text.setText("255.255.255.255");
                if_text.setText("ethX");
            } else {
            	RoutingEntry entry  = (RoutingEntry)rlist.getSelectedValue();
            	if(entry == null){
            		System.err.println("No Entry Selected!");
            		return;
            	}
            		
                if(cmd.equals("Move Down")){ 
                	rtable.moveEntryDown(entry);
                }else if(cmd.equals("Move Up")){
                		rtable.moveEntryUp(entry);           	
                }else if(cmd.equals("Delete")){
                	rtable.deleteEntry(entry);
                }
            	
            	
            }

            fillRouteList();
 
            
            
        }
    } // -- class ItemAdder
    


}
