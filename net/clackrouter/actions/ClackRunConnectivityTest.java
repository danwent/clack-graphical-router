package net.clackrouter.actions;

import java.awt.event.ActionEvent;

import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.test.ConnectivityTestThread;
import net.clackrouter.test.ConnectivityTestWindow;
import net.clackrouter.topology.core.TopologyModel;

public class ClackRunConnectivityTest extends AbstractActionDefault {

	/**
	 * Constructor for ViewScaleZoomIn.
	 * @param graphpad
	 */
	public ClackRunConnectivityTest(ClackFramework graphpad) {
		super(graphpad);
	}
	

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */

		public void actionPerformed(ActionEvent e) {
			ClackDocument doc = this.graphpad.getCurrentDocument();
			int topo = doc.getTopology();
			System.out.println("Running connectivity test for topo = " + topo);
			try {
				TopologyModel model = this.graphpad.getTopologyManager().getTopologyModel(topo, null);
				
				
				ConnectivityTestWindow	win = doc.getConnectivityTestWin();
				if(win == null) {	
					win = new ConnectivityTestWindow(model);
					doc.setConnectivityTestWin(win);
				}else {
					win.toFront();
				}

				
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
}
