
package net.clackrouter.gui.util;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.gui.ClackDocument;



/**
 * Handles when a user double-clicks on a component.
 * 
 * <p> Takes the functionality of a double click on a component,
 * which is used in several places, and puts it in a single helper 
 * class. </p>
 * 
 * <p> If the component is hierarchical, this "zooms in".  If the graph is 
 * simple, we pop-up the properties view in a new frame, otherwise we add
 * it to the properties view tab.  </p>
 */
public class CompDoubleClickHandler {
	
	public static void handleDoubleClick(ClackDocument doc, ClackComponent comp){
		if(comp.isHierarchical()){
			doc.pushNewView(comp.getHierarchicalView());
		}else {
			comp.setPendingError(false); // we've explored the error now
			JPanel pview = comp.getPropertiesView();
			// If the component doesn't want a panel, just quit
			if (pview == null) return;
			String name = comp.getName();
			if(doc.getFramework().isSimpleGraph()){
				JFrame frame = new JFrame(name);
				frame.getContentPane().add(pview);
				frame.setSize(new Dimension(350, 420));
				frame.show();
			}else {
				doc.getCurrentRouterView().addToPropertyViewTab(name, pview);
			}
		}
	}

}
