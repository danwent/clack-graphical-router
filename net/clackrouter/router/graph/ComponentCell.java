package net.clackrouter.router.graph;

import java.util.Vector;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.gui.ClackView;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

/**
 * Representation of a ClackComponent for use with a JGraph GraphModel.  
 */
public class ComponentCell extends DefaultGraphCell 
{
	protected ClackComponent mComponent = null;
    protected Vector      mPorts = null;
    private RouterView mRouterView;

    public ComponentCell(ClackComponent comp, RouterView view){
        super(comp.getName());
        mComponent = comp;
        mComponent.setComponentCell(this);
        mRouterView = view;
    }
    
    public ClackView getClackView() { return mRouterView; }

 
    public ClackComponent getClackComponent() { return mComponent; }
    public void setClackComponent(ClackComponent c) { 
    	mComponent = c; 
    	this.userObject = mComponent.getName(); 
    	mComponent.setComponentCell(this);
    }
    
	public Object clone() {
		DefaultGraphCell c = (DefaultGraphCell) super.clone();
		c.setAttributes((AttributeMap) attributes.clone());
		c.setUserObject(GraphConstants.getValue(c.getAttributes()));
		/* TODO: Need to clone the ClackComponent */
		return c;
	}

};

