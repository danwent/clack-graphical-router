/*
 * Created on Nov 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.propertyview;


import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.clackrouter.component.simplerouter.Level2Demux;


/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Level2DemuxPopup extends DefaultPropertiesView {
	   // labels used for properties frame

    private JLabel m_ip_count;
    private JLabel m_arp_count;
    private JLabel m_other_count;
    
    public Level2DemuxPopup(Level2Demux level2demux){
    	super(level2demux.getName(), level2demux);

        m_ip_count    = new JLabel();
        m_arp_count   = new JLabel();
        m_other_count = new JLabel();

    	JPanel main = addMainPanel("Level2Demux");
        addHTMLDescription("Level2Demux.html");
        main.add(getStatsFrame());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        
        updatePropertiesFrame();
    } 
    


    /**
     * creates a JFrame to display the properties of the Interface in a separate window
     */

    public JPanel getStatsFrame()
    {
    	JPanel count_panel = new JPanel();
        addBorderToPanel(count_panel,      "Packet Types");
        count_panel.add(m_arp_count);
        count_panel.add(m_ip_count);
        count_panel.add(m_other_count);
        count_panel.setMaximumSize(new Dimension(280,100));
        return count_panel;
    
    } 

    /**
     * updates the values displayed in the current Properties frame.
     * right now just puts in new values for packets in/packets out
     */
    protected void updatePropertiesFrame() 
    {
        super.updatePropertiesFrame();		
        Level2Demux demux = (Level2Demux)m_model;
        m_ip_count.setText("Total IP Packets: " + demux.getIPCount());
        m_arp_count.setText("Total ARP Packets: " + demux.getARPCount());	
        m_other_count.setText("Total \"Other\" Packets: " + demux.getOtherCount());	
    }

}
