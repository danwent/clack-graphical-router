
package net.clackrouter.router.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.gui.ClackPaintable;
import net.clackrouter.gui.ClackView;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexView;


/**
 * Abstract base class that should be extended in order to create a
 * dynamic Clack component.  
 * 
 * <p> Subclasses must define a static class extending JGraph's
 * VertexRenderer, that implements the paint(Graphics g) method
 * to draw the component. </p>
 * 
 * <p> See {@link QueueView} for an example.  </p>
 */
public abstract class DynamicClackView extends VertexView implements ClackPaintable {

    public static CellViewRenderer renderer;
	
    public static int  sBORDER           = 2;
    protected Color  mBorderColor = null;
    protected Color  mFillColor   = null;

    protected Font   mFont     = ComponentView.smDefaultFont;
    protected int    mFontSize = ComponentView.smDefaultFontSize;
    protected String mName     = null;
    protected BasicStroke mStroke = null;
    
 
    // Constructor for Superclass
    public DynamicClackView(Object cell, 
    		JGraph graph, CellMapper cm, int width, int height)  {
        super(cell, graph, cm);
        
        ComponentCell ccell = (ComponentCell) cell;
        AttributeMap map = ccell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(map);
   		bounds =  new Rectangle((int)bounds.getX(), (int) bounds.getY(), width, height);
   		GraphConstants.setBounds(map, bounds);

        mName = ((ComponentCell)cell).toString();

        mBorderColor = new Color(92, 133, 38); // -- kinda pea green
        mFillColor   = new Color(255,255,153); // -- kinda yellowish
  
        mStroke = new BasicStroke(sBORDER / 3, 
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL, 10, null, 0);
        
		ClackComponent comp = ((ComponentCell) cell).getClackComponent();
		comp.setView(this);

    } // -- Constructor  
    
    public ClackView getClackView() { return ((ComponentCell)getCell()).getClackView(); }
	
    
    public void clackPaint() {
    	Graphics2D g2 = (Graphics2D)graph.getGraphics();

		if(g2 == null){			return;		}
		double scale = graph.getScale();
		g2.scale(scale, scale);
    	graph.getUI().paintCell(g2, this, getBounds(), false);
    	//System.out.println("Trying to repaint " + ((ComponentCell)this.getCell()).getClackComponent().getName());
    }
    
    // Returns the Renderer for this View
    public CellViewRenderer getRenderer() {
       return renderer;
    }
}
