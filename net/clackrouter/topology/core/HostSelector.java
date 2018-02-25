/*
 * Created on Apr 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.core;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.router.core.RouterConfig;
import net.clackrouter.topology.graph.TopoHostCell;
import net.clackrouter.topology.graph.TopologyView;

/**
 * Was used when people would see a topo and chose what host to select, 
 * which could be useful functionality, so we keep it around (no longer used). 
 * 
 */
public class HostSelector extends JFrame implements ActionListener {  
	
	public static final String ACTION_VIEW_TOPO = "View Topology";

	public static final String ACTION_RESERVE_HOST = "Reserve Host";
	

	private ClackFramework mGraphpad;
	private JTextField topoNum;
	private int mDisplayedTopo;
	private TopologyView mTopologyView;
	private JComboBox mComboBox;
	private Properties mXMLFileMap;
	private JScrollPane mScrollPane;
	
	public HostSelector(ClackFramework gpad) {
		super("topology viewer");

		Container container = this.getContentPane();

		container.setLayout(new BorderLayout());
		//mGraph.setMarqueeHandler(getMarqueeHandler());
		container.add(getControlPane(), BorderLayout.NORTH);
		mGraphpad = gpad;
		this.setSize(1000, 400);
		this.show();
	}
	
	private JPanel getControlPane() {
		JPanel panel = new JPanel();
		topoNum = new JTextField("99", 4);
		panel.add(topoNum);
		JButton viewTopo = new JButton(ACTION_VIEW_TOPO);
		viewTopo.addActionListener(this);
		panel.add(viewTopo);
		JButton reserveHost = new JButton(ACTION_RESERVE_HOST);
		reserveHost.addActionListener(this);
		panel.add(reserveHost);
		panel.add(getXMLComboBox());
		return panel;
	}
	
	private JComboBox getXMLComboBox() {
		mXMLFileMap = new Properties();
		String[] keyStrings = { "3-Interface-Compact", "3-Interface-TCP", "3-Interface-Simple" };
		String[] fileStrings = { "/xml/compact.jgx", "/xml/simpleTCP.jgx", "/xml/simple.jgx" };
		for(int i = 0; i < keyStrings.length; i++){
			mXMLFileMap.setProperty(keyStrings[i], fileStrings[i]);
		}

		mComboBox = new JComboBox(keyStrings);
		mComboBox.setSelectedIndex(0);
		return mComboBox;
	}
	
	public void viewTopology(int topoNum){
	/*	Container container = getContentPane();
		if(mTopologyView != null)
			container.remove(mScrollPane);
		try {

			mTopologyView = new TopologyView(mGraphpad, new GPDocument(), topoNum, null);
			mScrollPane = new JScrollPane();
			mScrollPane.setPreferredSize(new Dimension(500, 500));
			container.add(mScrollPane, BorderLayout.CENTER);
			mTopologyView.layoutTopoGraph();
			mScrollPane.setViewportView(mTopologyView);
			mTopologyView.repaint();
			mScrollPane.repaint();
			container.repaint();
			this.resize(new Dimension(1000, 500));
		} catch (Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Topology Viewing Error", JOptionPane.ERROR_MESSAGE);
		}
		*/
	}

	
	public void actionPerformed(ActionEvent e){
		
		String cmd = e.getActionCommand();
		try {
			if(cmd.equals(ACTION_VIEW_TOPO)){
				int topo_num = Integer.parseInt(topoNum.getText());
				mDisplayedTopo = topo_num;
				viewTopology(topo_num);
			}else if(cmd.equals(ACTION_RESERVE_HOST)){
				if(mTopologyView == null){
					JOptionPane.showMessageDialog(this, "No topology selected","Topology Viewing Error",  JOptionPane.ERROR_MESSAGE);
					return;
				}
				TopoHostCell cell = (TopoHostCell)mTopologyView.getSelectionCell();
				if(cell == null){
					JOptionPane.showMessageDialog(this, "No host selected", "Topology Viewing Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(cell.mHost.reservable) {
					System.out.println("reserving '" + cell.mHost.name + "' on topo " + mDisplayedTopo);
					RouterConfig.ConnectionInfo cInfo = new RouterConfig.ConnectionInfo();
					cInfo.host = cell.mHost.name;
					cInfo.server = Translator.getString("VNS_SERVER_ADDRESS");
					cInfo.port = Integer.parseInt(Translator.getString("VNS_PORT"));
					cInfo.topo_max = cInfo.topo_min = mDisplayedTopo;
					String xmlname = mXMLFileMap.getProperty((String)mComboBox.getSelectedItem());
					System.out.println("loading: " + xmlname);
					InputStream is =  this.getClass().getResource(xmlname).openStream();

					this.dispose();
				}else {
					System.err.println("Host '" + cell.mHost.name + " is not reservable");
				}
			}
		} catch(Exception exception){
			exception.printStackTrace();
			System.err.println("Error performing action ='" + cmd + "'");
		}
	}


}
