/*
 * Created on 29-Oct-03
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.clackrouter.router.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;

import net.clackrouter.error.ErrorReporter;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.protocol.VNSProtocolManager;
import net.clackrouter.protocol.data.VNSClose;
import net.clackrouter.topology.core.TopologyModel;



/**
 * The RouterManager class handles the creation of Routers, including the attempt to reserver the VNS host.    
 * 
 * It is initialized with connection information, and its main
 * method is the reserveVirtualHost(topoID, virtual_router_id) method, which returns an instance of the Router class.
 */
public class RouterManager implements Serializable
{
	private ClackFramework m_framework;
	private String m_serverName;
	private int m_serverPort;
	private String m_username;
	private String m_password;

	// This sets the connection info that the router manager will use
	public RouterManager(
		ClackFramework framework,
		String serverName,
		int serverPort,
		String username,
		String password) {
		m_framework = framework;
		m_serverName = serverName;
		m_serverPort = serverPort;
		m_username = username;
		m_password = password;
	}

    public String getServer()
    { return m_serverName; }

    public int getPort()
    { return m_serverPort; }
    

	

	// contacts the VNS and returns a Router object corresponding the to requested topology and
	// virtual host ID's.  Throws an exception if the Router cannot be created.  
    
	public Router reserveVirtualHost(TopologyModel topoModel, String virtualRouterID, boolean isGUI, 
			String username, String auth_key) throws Exception 
    {
        VNSProtocolManager proto_manager = new VNSProtocolManager(m_serverName,m_serverPort);
    
        String failure_message = "";
        try 
        {
        	proto_manager.doAuth(username, auth_key); 
            proto_manager.sendOpenCommand((short)topoModel.getTopology(), virtualRouterID, m_username);
            
            VNSClose close = proto_manager.checkForVNSCloseCommand();
            
            if (close == null && proto_manager.isConnected() ) {
                return new Router(proto_manager, topoModel, isGUI);
            }
            	
            failure_message = "VNS server has disconnected (" + close.toString() + ")";
        } catch (Exception e)  { 
        	e.printStackTrace();
        	failure_message = e.getMessage(); 
        }
        throw new Exception("VNS Connection failed: " + failure_message);
    } // -- reserveVirtualHost(..)
	
	

	
	
	// connects a router, without performing any of the actions to add a router graphically
	// or start it.  if the GUI is showing, we give the user info about the connection process
	public Router connectToVNS(TopologyModel topoModel, String host, boolean isGUI, String username, String auth_key)
			throws Exception {

		ConnectFrame cFrame = null;
		try {
			System.out.println("Trying to connect to topo " + topoModel.getTopology());
			System.out.println("using host = " + host + " server = " 
					+ getServer() + " and port = " + getPort());

			if (isGUI) {
				cFrame = new ConnectFrame(m_framework);
				cFrame.show();
				cFrame.updateConnectText(getServer(),
						getPort(), host, topoModel.getTopology());
			}
			System.out.println("about to reserve");
			Router router = reserveVirtualHost(topoModel,host, isGUI, username, auth_key);
			System.out.println("done reserving");
			if (!router.initializeVNSConnectedRouter()) {
				System.out.println("initialization failed");
				throw new Exception("Could not initialize router '" + host
						+ "' on topology " + topoModel.getTopology());
			}
			System.out.println("done initializing"); 
			if (isGUI) {
				cFrame.hide();
				cFrame.dispose();
			}
			System.out.println("Successfully connected to '" + router.getHost()
					+ "' on topo " + router.getTopology());
			return router;
		} catch (Exception e) {
			
			if (isGUI)
				cFrame.dispose();
			throw e;
		}

	}
	
	
	public static class ConnectFrame extends JFrame {
		
		private JLabel m_message;
		public ConnectFrame(ClackFramework pad) {
			super();
			m_message = new JLabel("");
			getContentPane().add(m_message);
			this.setSize(450,50);
			this.setLocationRelativeTo(pad);
			this.setVisible(true);
		}
		
		public void updateConnectText(String server, int port, String host, int topo){
			m_message.setText("Connecting to " + server + ":" + port + " to reserve '" + host + "' on topology " + topo);
		}
		public void updateText(String s){
			m_message.setText(s);
		}		
		
	}
	


}
