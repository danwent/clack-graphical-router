
package net.clackrouter.gui;

import java.awt.Component;

import org.jgraph.JGraph;

/**
 * Interface for different views of Clack that can be placed inside a {@link ClackDocument}
 */
public interface ClackView {
	
	/** The JGraph associated with this view */
	public JGraph getGraph();
	
	/** The component associated with this view */
	public Component getComponent();
	
	/** indicates if this view is currently visible to the user */
	public boolean isVisibleView();
	
	/** set flag indicating if this view is current visible to the user */
	public void setIsVisibleView(boolean b);

}
