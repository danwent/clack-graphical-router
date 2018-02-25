package net.clackrouter.error;

import java.util.ArrayList;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.router.core.Router;
import net.clackrouter.topology.core.TopologyModel;

public class ErrorUtils {
	
	public static void ClearAllErrors(TopologyModel model){
		ArrayList hosts = model.getHosts();
		ClackDocument clack_doc = ((TopologyModel.Host)hosts.get(0)).document;

		for(int i = 0; i < hosts.size(); i++) {
			TopologyModel.Host host = (TopologyModel.Host)hosts.get(i);
			Router r = clack_doc.getRouter(host.name);
			r.setPendingError(false);
			
			ClackComponent[] all_comps = r.getAllComponents();
			for(int j = 0; j < all_comps.length; j++){
				ClackComponent c = all_comps[j];
				c.setPendingError(false);
				c.try_repaint();
			}
		}
		clack_doc.redrawTopoView();
	}

}
