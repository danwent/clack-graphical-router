
package net.clackrouter.gui;



/**
 * Interface to be implemented by objects that can be dynamically painted by 
 * Clack.  For example, {@link net.clackrouter.router.graph.WireView}
 */
public interface ClackPaintable {
	
	public void clackPaint();
	
	public ClackView getClackView();
}
