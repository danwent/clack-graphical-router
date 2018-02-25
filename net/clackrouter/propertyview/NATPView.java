package net.clackrouter.propertyview;

import java.awt.BorderLayout;
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
import net.clackrouter.component.extension.NAPT;
import net.clackrouter.component.extension.NAPT.ICMPMapping;
import net.clackrouter.component.extension.NAPT.TransportLevelMapping;
import net.clackrouter.component.simplerouter.IPRouteLookup;

import net.clackrouter.packets.IPPacket;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;

public class NATPView extends DefaultPropertiesView {

	
	private DefaultTableModel tModel = null;
	private DefaultTableModel iModel;
    private JTable tTable = null; 
    private JTable iTable = null;
    
    String[] t_column_names;
    String[] i_column_names;

    JPanel config = null;
    JScrollPane transport_table_pane = null;
    JScrollPane icmp_table_pane = null;
    
    private static int MAX_NUM_VISIBLE_ENTRIES = 7;
    
    public NATPView(NAPT nat) {
    	super(nat.getName(), nat);

        tModel = new DefaultTableModel();
        tTable = new JTable(tModel);
        iModel = new DefaultTableModel();
        iTable = new JTable(iModel);
        transport_table_pane = new JScrollPane();
        icmp_table_pane = new JScrollPane();
        
        t_column_names = new String[]{"Internal IP",
				"Internal Port","External Port","Proto", "TTL"};
        i_column_names = new String[]{"Internal IP",
				"Internal ID","External ID", "TTL"};
        
        
        JPanel main = addMainPanel("NAT");
        addHTMLDescription("NAT.html");
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        
        JPanel config_panel = createConfigPanel();
        main.add(config_panel);
        addBorderToPanel(config_panel, "Translation Tables");
    
        nat.registerListener(this);
        updatePropertiesFrame();
    }

    // this function is called anytime a route is changed, which is kind of heavy weight if you 
    // think about a dynamic routing protocol...
    private void fillTranslationList()
    {
        NAPT nat = (NAPT)m_model;
    
        fillTransportTableList(nat);
        fillICMPTableList(nat);
        this.repaint();

    } 
    
    private void fillTransportTableList(NAPT nat) {

        TransportLevelMapping[] tmaps = nat.getTransportLevelMappings();
        Object[][] data = new Object[tmaps.length][t_column_names.length];

        for ( int i = 0 ; i < tmaps.length; ++ i ){ 

			TransportLevelMapping m = tmaps[i];
			data[i][0] = m.internalIP.getHostAddress();
			data[i][1] = m.internalPort;
			data[i][2] = m.externalPort;
			if (m.proto == IPPacket.PROTO_TCP)
				data[i][3] = "tcp";
			else if (m.proto == IPPacket.PROTO_UDP)
				data[i][3] = "udp";
			else
				data[i][3] = "error";
			data[i][4] = m.expire_ttl;

		}

		JTable oldtable = tTable;
		tTable = new JTable(data, t_column_names);
		tTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn column = null;
		for (int i = 0; i < t_column_names.length; i++) {
			column = tTable.getColumnModel().getColumn(i);
			if (i == 3 || i == 4) {
				column.setMaxWidth(50); // proto column is smaller
			} else {
				column.setMaxWidth(120);
			}
		}

		if (oldtable != null)
			transport_table_pane.remove(oldtable);
		transport_table_pane.setViewportView(tTable);
		int height = tmaps.length > MAX_NUM_VISIBLE_ENTRIES ? MAX_NUM_VISIBLE_ENTRIES
				: tmaps.length;
		transport_table_pane.setPreferredSize(new Dimension(350,
				height * 25 + 30));
    }

    private void fillICMPTableList(NAPT nat) {
    	
        Hashtable icmp_mappings = nat.getICMPMappings();
        Object[][] data = new Object[icmp_mappings.size()][i_column_names.length];

        int j = 0;
        for(Enumeration e = icmp_mappings.keys(); e.hasMoreElements() ; j++){

			String map_str = (String) e.nextElement();
			ICMPMapping mapping = (ICMPMapping) icmp_mappings.get(map_str);
			data[j][0] = mapping.internalIP.getHostAddress();
			data[j][1] = mapping.internalID + "";
			data[j][2] = map_str;
			data[j][3] = mapping.expire_ttl;
		}


		JTable oldtable = iTable;
		iTable = new JTable(data, i_column_names);
		iTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
        
		TableColumn column = null;
		for (int i = 0; i < i_column_names.length; i++) {
			column = iTable.getColumnModel().getColumn(i);
			if(i == 4) column.setMaxWidth(30);
			else column.setPreferredWidth(80);
		}

		if (oldtable != null)
			icmp_table_pane.remove(oldtable);
		icmp_table_pane.setViewportView(iTable);
		int height = icmp_mappings.size() > MAX_NUM_VISIBLE_ENTRIES ? MAX_NUM_VISIBLE_ENTRIES
				: icmp_mappings.size();
		icmp_table_pane.setPreferredSize(new Dimension(280,
				height * 25 + 30));
		
    }

    
    
    
    private JPanel createConfigPanel()
    {
        config = new JPanel();
        config.setLayout(new BoxLayout(config, BoxLayout.PAGE_AXIS));
        fillTranslationList();
        
        config.add(transport_table_pane); 
        config.add(Box.createRigidArea(new Dimension(30,30)));
        config.add(icmp_table_pane);
 
        return config;
    } // -- createConfigPanel
    
    public void componentEvent(ClackComponentEvent e){
    	super.componentEvent(e);
    	if(e.mType == ClackComponentEvent.EVENT_DATA_CHANGE)
    		fillTranslationList();
    }


}
