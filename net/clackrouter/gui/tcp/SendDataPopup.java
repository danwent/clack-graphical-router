
package net.clackrouter.gui.tcp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.clackrouter.component.tcp.SendWindowCheck;
import net.clackrouter.propertyview.DefaultPropertiesView;



/**
 * Class not used, but is a pop-up for the TCP SendData subcomponent.
 */
public class SendDataPopup extends DefaultPropertiesView {

    JTextField port_text = null; 
    JTextField ip_text = null;     

    public SendDataPopup(SendWindowCheck sd) {
    	super(sd.getName(), sd);

        JPanel main = addMainPanel("SendData");
        addHTMLDescription("descr/SendData.html");
        addPanelToTabPane("Ports", m_port_panel);
        refreshPortTab();
        
        JPanel config_panel = createConfigPanel();
        main.add(config_panel);
        addBorderToPanel(config_panel, "Configure TCP Connection Settings");
    
        updatePropertiesFrame();
    }

    


    private JPanel createConfigPanel()
    {
        JPanel config = new JPanel();
        //config.setLayout(new BoxLayout(config, BoxLayout.PAGE_AXIS));
    	
  
        config.add(new JLabel("IP address: "));
        ip_text = new JTextField("192.168.128.133");
        ip_text.setPreferredSize(new Dimension(100,20));
        config.add(ip_text);
        config.add(new JLabel("Port: "));
        port_text = new JTextField("80");
        port_text.setPreferredSize(new Dimension(50,20));
        config.add(port_text);
        JButton connectButton =
            new JButton("Connect");
        connectButton.setPreferredSize(new Dimension(150, 30));
        connectButton.addActionListener(new ItemAdder());
        config.add(connectButton);
        JButton listenButton =
            new JButton("Listen");
        listenButton.setPreferredSize(new Dimension(150, 30));
        listenButton.addActionListener(new ItemAdder());
        config.add(listenButton);
        JButton clientButton =
            new JButton("Show TCP Client");
        clientButton.setPreferredSize(new Dimension(150, 30));
        clientButton.addActionListener(new ItemAdder());
        config.add(clientButton);

        config.setMaximumSize(new Dimension(280,200));
        
        return config;
    } // -- createConfigPanel



    private class ItemAdder implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
        	SendWindowCheck sd = (SendWindowCheck)m_model;          
            if ( sd == null )
            { 
                System.err.println("Incorrect model type for " + this.getClass().getName());
                System.exit(-1);
            }

            
            String cmd =  event.getActionCommand();
            String ip_str = ip_text.getText();
            String port_str = port_text.getText();
            InetAddress ip = null;
            int port = 0;
            try {
            	port = Integer.parseInt(port_str);
            	ip = InetAddress.getByName(ip_str);
            } catch(Exception e){
            	System.err.println("Error parsing ip = '" + ip_str + "' port = '" + port_str + "'");
            	e.printStackTrace();
            }

      /*      if(cmd.equals("Connect")) {
            	InetAddress local_addr = m_model.getRouter().getInputInterfaces()[0].getIPAddress();
            	sd.setLocalInfo(local_addr, 1234);
            	sd.initiateConnection(ip, port);
            	sd.setDataSource(new DataSource());
            	System.out.println("Connection Initiated!");
            }else if(cmd.equals("Listen")){
            	sd.bind(ip, port);
            }else if(cmd.equals("Show TCP Client")){
            	System.err.println("Disabled TCP Client (TCPBlockPopup.java");
            //	SimpleTCPClient frame = new SimpleTCPClient("client" , block);
            //	frame.show();
            }
            */
        }
    } // -- class ItemAdder
}
