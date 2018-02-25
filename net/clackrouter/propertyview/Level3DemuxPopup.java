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
import javax.swing.JTabbedPane;

import net.clackrouter.component.simplerouter.Level3Demux;


/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Level3DemuxPopup extends DefaultPropertiesView {

    private JLabel m_tcp_count;
    private JLabel m_udp_count;
    private JLabel m_icmp_count;
    private JLabel m_other_count;

    private JTabbedPane  m_tab_pane;
    private JPanel       m_overview_panel;
    private JPanel       m_packets_panel;


    public Level3DemuxPopup(Level3Demux l3demux){
    	super(l3demux.getName(), l3demux); 
        m_tcp_count   = new JLabel();
        m_udp_count   = new JLabel();
        m_icmp_count  = new JLabel();
        m_other_count = new JLabel();

    	JPanel main = addMainPanel("Level3Demux");
        addHTMLDescription("Level3Demux.html");
        main.add(createStatsPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
    }



    private JPanel createStatsPanel()
    {
        JPanel stats = new JPanel();

        //stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));		

        addBorderToPanel(stats,    "Packet Statistics");

        stats.add(m_tcp_count);
        stats.add(m_udp_count);
        stats.add(m_icmp_count);
        stats.add(m_other_count);

        stats.setMaximumSize(new Dimension(150,120));
        
        return stats;
    } // -- createConfigPanel


    /**
     * updates the values displayed in the current Properties frame.
     * right now just puts in new values for packets in/packets out
     */
    protected void updatePropertiesFrame() 
    {
    	super.updatePropertiesFrame();
        Level3Demux l3model = (Level3Demux)m_model;
		
        String tcp_count = ""+l3model.getICMPCount();
        m_tcp_count.setText("Total TCP Packets: " + tcp_count);	
        String udp_count = ""+l3model.getUDPCount();
        m_udp_count.setText("Total UDP Packets: " + udp_count);	
        String icmp_count = ""+l3model.getICMPCount();
        m_icmp_count.setText("Total ICMP Packets: " + icmp_count);	
        String other_count = ""+l3model.getOtherCount();
        m_other_count.setText("Total Other Packets: " + other_count);	

    } // -- updatePropertiesFrame

}
