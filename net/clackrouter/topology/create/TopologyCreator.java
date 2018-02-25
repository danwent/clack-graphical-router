/*
 * Created on Oct 14th, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.create;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.crimson.tree.XmlDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.router.core.RouterConfig;
import net.clackrouter.topology.core.TopoSerializer;
import net.clackrouter.topology.core.TopologyModel;
import net.clackrouter.topology.graph.TopoHostCell;
import net.clackrouter.topology.graph.TopologyView;

/**
 * Used to design a topology  
 * 
 */
public class TopologyCreator extends JFrame implements ActionListener {  
	
	public static final String SUBMIT = "Submit Topology";
	public static final String ADD_HOST = "Add Host";
	public static final String ADD_LINK = "Add Link";
	public static final String SAVE = "Save Config";
	int host_counter; 

	private CreateTopologyView mTopologyView;
	private JComboBox mComboBox;
	private JScrollPane mScrollPane;
	
	public TopologyCreator(String xml_file) {
		super("topology creator");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container container = this.getContentPane();

		container.setLayout(new BorderLayout());
		container.add(getControlPane(), BorderLayout.NORTH);
		addTopology(xml_file);
		this.setSize(900, 600);
		this.show();
		
		host_counter = 0;
	}
	
	private JPanel getControlPane() {
		JPanel panel = new JPanel();
		JButton viewTopo = new JButton(SUBMIT);
		viewTopo.addActionListener(this);
		panel.add(viewTopo);
		JButton add_host = new JButton(ADD_HOST);
		add_host.addActionListener(this);
		panel.add(add_host);
		JButton add_link = new JButton(ADD_LINK);
		add_link.addActionListener(this);
		panel.add(add_link);
		JButton save = new JButton(SAVE);
		save.addActionListener(this);
		panel.add(save);
		return panel;
	}
	
	
	public void addTopology(String xml_file){
		Container container = getContentPane();
		if(mTopologyView != null)
			container.remove(mScrollPane);
		try {

			mTopologyView = new CreateTopologyView(xml_file);
			mScrollPane = new JScrollPane();
			mScrollPane.setPreferredSize(new Dimension(500, 500));
			container.add(mScrollPane, BorderLayout.CENTER);
			mTopologyView.layoutTopoGraph();
			mScrollPane.setViewportView(mTopologyView);
			mTopologyView.repaint();
			mScrollPane.repaint();
			container.repaint();
		//	this.resize(new Dimension(1000, 500));
		} catch (Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Topology Viewing Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}

	
	public void actionPerformed(ActionEvent e){
		
		String cmd = e.getActionCommand();
		try {
			if(cmd.equals(SUBMIT)){
				
				TopologyModel model = mTopologyView.getTopologyModel();
				ArrayList hosts = model.getHosts();
				ArrayList links = model.getLinks();
				//	TODO:  submit topology data to VNS.  The topology
				// model should contain what you need
				
			} else if(cmd.equals(ADD_HOST)){
							
				TopologyModel.Host h = new TopologyModel.Host("vhost " + host_counter, true);
				++host_counter;
				TopologyModel model = mTopologyView.getTopologyModel();
				model.getHosts().add(h);
				CreateTopologyGraph graph = mTopologyView.getGraph();
				
				// LAME: hardcoding insertion point at (20,20)
				// it would be better to have the user right-click and 
				// select "add host" so they can specify where to add it. 
				Rectangle bounds = new Rectangle(20, 20, 120, 100);
				graph.insertHostIntoGraph(h, mTopologyView.getInterfaceMap(), bounds);
				
			} else if(cmd.equals(ADD_LINK)){
				AddLinkWindow win = new AddLinkWindow(mTopologyView);
			}else if(cmd.equals(SAVE)){
				
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(chooser.FILES_ONLY);
				chooser.setSelectedFile(new File("network1.topo"));
				chooser.setDialogTitle("Save Config File");
				int result = chooser.showSaveDialog(this);
				if (result == JFileChooser.CANCEL_OPTION)
					return;

				File file = chooser.getSelectedFile();
				CTopoSerializer.serializeCTopo(mTopologyView, file);

			}
		} catch(Exception exception){
			exception.printStackTrace();
			System.err.println("Error performing action ='" + cmd + "'");
		}
	}
	


	public static void main(String[] args){
		
		if(args.length != 1){
			System.out.println("usage: <topology config file> ");
			System.exit(1);
		}
		TopologyCreator c = new TopologyCreator(args[0]);
	}

}
