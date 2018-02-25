package net.clackrouter.actions;

import java.awt.event.ActionEvent;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.util.HostnamePrompter;
import net.clackrouter.router.core.Router;

public class ClackStopEthereal extends AbstractActionDefault {

	public ClackStopEthereal(ClackFramework graphpad) {
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
	        	
	        	r.stopEthereal();  	
	        }
		}

}
