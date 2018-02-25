/*
 * Created on Jun 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.core;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.clackrouter.jgraph.utils.gui.LocaleChangeAdapter;



/**
 * Simple popup window for host objects in a topology graph, which lets the 
 * user copy each of the host's IP addresses to the clipboard.
 */
public class TopoPopup extends JPopupMenu {
	/** creates a popup menu for the specified key.
	 */
	public TopoPopup(TopologyModel.Host h) {

		JMenu copyMenu = new JMenu("Copy To Clipboard");
		add(copyMenu);
		CopyActionListener cal = new CopyActionListener();
		for(int i = 0; i < h.interfaces.size(); i++){
			TopologyModel.Interface iface = (TopologyModel.Interface)h.interfaces.get(i);
			if(iface == null)continue;
			JMenuItem item = new JMenuItem(iface.address.getHostAddress());
			item.addActionListener(cal);
			copyMenu.add(item);
		}
		LocaleChangeAdapter.updateContainer(this);
		

	}

	private class CopyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			System.out.println("trying to copy: " + cmd);
		    StringSelection stringSelection = new StringSelection( cmd);
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents( stringSelection, null );
		}
	}
}
