package net.clackrouter.actions;


	import java.awt.event.ActionEvent;

import net.clackrouter.error.ErrorUtils;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.topology.core.TopologyModel;


	public class ClackClearErrors extends AbstractActionFile {
		
		public ClackClearErrors(ClackFramework graphpad) {
			super(graphpad);
		}
		
		public void actionPerformed(ActionEvent e) {
			try {
				ClackDocument doc = this.graphpad.getCurrentDocument();
				int topo = doc.getTopology();
				TopologyModel model = this.graphpad.getTopologyManager().getTopologyModel(topo, null);
				ErrorUtils.ClearAllErrors(model);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

