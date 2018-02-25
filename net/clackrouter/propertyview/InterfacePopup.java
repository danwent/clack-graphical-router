/*
 * Created on Nov 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.propertyview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.clackrouter.component.base.Interface;
import net.clackrouter.component.simplerouter.ARPLookup;
import net.clackrouter.netutils.EthernetAddress;

/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class InterfacePopup extends DefaultPropertiesView {
    

    // -- Fields for IP/MAC configuration information
    private JTextField m_ipaddr_text;
    private JTextField m_subnet_text;
    private JTextField m_macaddr_text;
    
    public static int INTERFACE_OUT = 0;
    public static int INTERFACE_IN = 1;
    
    public static String SET_VALUES = "Set New IP configuration";
    
	public InterfacePopup(Interface iface, int type){
		super("Component " + iface.getName() + " Properties", iface);

        m_ipaddr_text = new JTextField("0.0.0.0", 15);
        m_subnet_text = new JTextField("0.0.0.0", 15);
        m_macaddr_text = new JTextField("00:00:00:00:00:00", 17);
        m_macaddr_text.setEditable(false);
        
        if ( iface.getMACAddress() != null ) 
        { m_macaddr_text.setText(iface.getMACAddress().toString()); }

        if ( iface.getIPAddress() != null ) 
        { m_ipaddr_text.setText(iface.getIPAddress().getHostAddress()); }

        if ( iface.getIPSubnet() != null ) 
        { m_subnet_text.setText(iface.getIPSubnet().getHostAddress()); }
        
        String name = null;
        if(type == INTERFACE_IN) name = "FromDevice";
        else if(type == INTERFACE_OUT) name = "ToDevice";
        
    	JPanel main = addMainPanel(name);
        addHTMLDescription(name + ".html");
        main.add(createConfigPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
	}
   

    private JPanel createConfigPanel()
    {
        JPanel config = new JPanel();

        //config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));		

        JPanel ip_panel     = new JPanel();
        ip_panel.setPreferredSize(new Dimension(280,65));
        JPanel subnet_panel = new JPanel();
        subnet_panel.setPreferredSize(new Dimension(280,65));
        JPanel mac_panel    = new JPanel();
        mac_panel.setPreferredSize(new Dimension(280,65));
 
        config.add(mac_panel);
        config.add(ip_panel);
        config.add(subnet_panel);


        addBorderToPanel(ip_panel,    "IP address");
        addBorderToPanel(subnet_panel, "IP subnet");
        addBorderToPanel(mac_panel,   "MAC address");

        ip_panel.add(m_ipaddr_text);
        subnet_panel.add(m_subnet_text);
        mac_panel.add(m_macaddr_text);
        
        JButton addButton = new JButton(InterfacePopup.SET_VALUES);
        config.add(addButton);
        addButton.addActionListener(new IPChanger());
        
        return config;
    }
    
    private class IPChanger implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            Interface iface = (Interface)m_model;          
            String cmd = event.getActionCommand();
  
            if(cmd.equals(InterfacePopup.SET_VALUES)) {
            	try {
            		InetAddress new_ip = InetAddress.getByName(m_ipaddr_text.getText());
            		InetAddress new_mask = InetAddress.getByName(m_subnet_text.getText());
            		iface.getRouter().configureIPInterface(iface.getDeviceName(), new_ip, new_mask);
            		
            	}catch (Exception e){
            		iface.showErrorDialog("Error reconfiguring interface: " + e.getMessage());
            		e.printStackTrace();
            	}
            	
            	
            }else {
            	System.err.println("unknown command: " + cmd);
            }
        }
    } // -- class ItemAdder
	
}
