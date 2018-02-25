package net.clackrouter.topology.create;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.clackrouter.topology.core.TopologyModel;
import net.clackrouter.topology.graph.TopoHostCell;

public class AddLinkWindow extends JFrame implements ActionListener {
	
	public  int DEFAULT_HEIGHT = 150;
	public  int DEFAULT_WIDTH = 350;
	JComboBox[] hostnames;
	JTextField[] ifacenames;
	CreateTopologyView mTopologyView;
	public static String ADD = "Add Link";
	
	public AddLinkWindow(CreateTopologyView v) {
		super("Add Link to Topology");
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mTopologyView = v;
		hostnames = new JComboBox[2];
		ifacenames = new JTextField[2];
		
		JPanel master = new JPanel();
		master.setLayout(new BorderLayout());
		master.add( createPanel(0), BorderLayout.EAST);
		master.add( createPanel(1), BorderLayout.WEST);
		JButton fin_button = new JButton(ADD);
		fin_button.addActionListener(this);
		master.add(fin_button, BorderLayout.SOUTH);
		this.setContentPane(master);
		setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		this.setVisible(true);
	}
	
	JPanel createPanel(int host_num){
		
		ArrayList hosts = mTopologyView.getTopologyModel().getHosts();
		ArrayList<String> names = new ArrayList<String>();
		int j = 0;
		for(int i = 0; i < hosts.size(); i++) {
			TopologyModel.Host h = (TopologyModel.Host)hosts.get(i);
			if(h.reservable) // only virtual hosts can add interfaces
				names.add(h.name);
		}
		hostnames[host_num] = new JComboBox(names.toArray());
		ifacenames[host_num] = new JTextField("ethX", 9);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JLabel("Host " + host_num + ": "));
		panel.add(hostnames[host_num]);
		panel.add(new JLabel("Interface Name: "));
		panel.add(ifacenames[host_num]);
		return panel; 
	}
	
	public void actionPerformed(ActionEvent e){
		
		String cmd = e.getActionCommand();
		if(cmd.equals(ADD)) {
			String host1 = (String)hostnames[0].getSelectedItem();
			String host2 = (String)hostnames[1].getSelectedItem();
			String iface1 = ifacenames[0].getText();
			String iface2 = ifacenames[1].getText();
			
			System.out.println("trying to add link between " + host1 + "(" + iface1 + ") and " +
					host2 + "(" + iface2 + ")");
			
			TopoHostCell host1_cell = null, host2_cell = null;
			Object[] roots = mTopologyView.getGraph().getRoots();
			for(int i = 0; i < roots.length; i++){

				if(roots[i] instanceof TopoHostCell){
					TopoHostCell test = (TopoHostCell)roots[i];
					if(test.mHost.name.equals(host1))
						host1_cell = test;
					else if( test.mHost.name.equals(host2))
						host2_cell = test;
				}
			}
			if(host1_cell == null || host2_cell == null){
				System.err.println("Invalid names given to addlink: '" +
						host1 + "' and '" + host2 + "'");
				return;
			}
			
			if(!host1_cell.mHost.reservable || !host2_cell.mHost.reservable){
				System.err.println("Invalid Link, cannot add link to a physical host: '" +
						host1 + "' and '" + host2 + "'");
				return;
			}	
			
			// check to see if there's an interface name clash, if so, return
			if(!isValidInterfaceName(host1_cell.mHost, iface1)) return;
			if(!isValidInterfaceName(host2_cell.mHost, iface2)) return;
			
			mTopologyView.addLink(host1_cell,iface1, host2_cell, iface2);  
			
			// close, otherwise the JComboBoxes might become stale if they add
			// another host. We should really update them when a host is added. 
			this.dispose();
		
		}else {
			System.err.println("unknown command: " + cmd);
		}
	}
	
	boolean isValidInterfaceName(TopologyModel.Host h, String iface_name){
		for(int i = 0; i < h.interfaces.size(); i++){
			TopologyModel.Interface test = (TopologyModel.Interface) 
								h.interfaces.get(i);
			if(test.name.equals(iface_name)){
				System.err.println("Error: " + h.name + " already " +
						"has an interface named " + iface_name);
				return false;
			}
		}
		return true;
	}
	
}
