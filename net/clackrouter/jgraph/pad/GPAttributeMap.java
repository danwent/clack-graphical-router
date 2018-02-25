/*
 * Created on 15.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.jgraph.pad;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphConstants;

/**
 * @author Gaudenz Alder
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class GPAttributeMap extends AttributeMap {

	/**
	 * Returns a clone of the user object with the new value.
	 */
	public Object valueChanged(Object value) {
		Object userObject = get(GraphConstants.VALUE);
		if (userObject instanceof GPUserObject) {
			GPUserObject user = (GPUserObject) ((GPUserObject) userObject)
					.clone(); // clone for undo
			user.setValue(value);
			return user;
		}
		return super.valueChanged(value);
	}

	/**
	 * Returns a clone of <code>map</code>, from keys to values. If the map
	 * contains bounds or points, these are cloned as well. References to
	 * <code>PortViews</code> are replaces by points.
	 */
	public Object clone() {
		AttributeMap newMap = (AttributeMap) super.clone();
		// Clone the user object
		Object user = GraphConstants.getValue(newMap);
		if (user instanceof GPUserObject) {
			GraphConstants.setValue(newMap, ((GPUserObject) user).clone());
		}
		return newMap;
	}

}
