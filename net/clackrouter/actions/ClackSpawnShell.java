/*
 * Created on Aug 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.actions;

import java.awt.event.ActionEvent;

import net.clackrouter.application.ClackShell;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.util.HostnamePrompter;
import net.clackrouter.router.core.Router;

/**
 * Simple action to show the Clach Shell CLI window.
 */
public class ClackSpawnShell extends AbstractActionDefault {
	/**
	 * Constructor for ViewScaleZoomIn.
	 * @param graphpad
	 */
	public ClackSpawnShell(ClackFramework graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */

		public void actionPerformed(ActionEvent e) {
			HostnamePrompter prompter = new HostnamePrompter(graphpad, null);
			
	        if(prompter.isValidResponse()){
	        	String hostname = prompter.getSelectedHost();
	        	Router r = graphpad.getTopologyManager().getRouterByName(hostname);
	        	if(r == null) {
	        		System.err.println("No router named '" + hostname + "' is reserverd on this topology");
	        		return;
	        	}
	        	
	        	ClackShell sh = new ClackShell(r, graphpad);
	        	(new Thread(sh)).start();
	        	
	        }
		}

}
