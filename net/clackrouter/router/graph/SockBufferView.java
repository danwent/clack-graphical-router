
package net.clackrouter.router.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import net.clackrouter.component.tcp.SockBuffer;
import net.clackrouter.gui.ClackPaintable;
import net.clackrouter.gui.ClackView;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;


/**
 * A modified QueueView to represent a SocketBuffer.
 * 
 * @deprecated class is not yet complete
 */
public class SockBufferView extends VertexView implements ClackPaintable {
	
    public static int  sBORDER           = 2;
    public static int  smDefaultFontSize = 10; 
    public static Font smDefaultFont     = new Font(null, Font.BOLD , smDefaultFontSize);
    public static int  swPAD = 18; 
    public static int  shPAD = 16; 
    
    public static int DEFAULT_HEIGHT = 20;
    public static int DEFAULT_WIDTH = 30;

    protected boolean mHasImage;
    protected Image mImage = null;
    protected Color  mBorderColor = null;
    protected Color  mFillColor   = null;

    protected Font   mFont     = null;
    protected int    mFontSize = smDefaultFontSize;

    protected String mName     = null;

    protected BasicStroke mStroke = null;
    
    public ClackView getClackView() { return ((ComponentCell)getCell()).getClackView(); }
    
    public boolean hasImage() {
    	return mHasImage;
    }
    
    public Image getImage() { 
    	return mImage;
    }

    // Constructor for Superclass
    public SockBufferView(Object cell, JGraph graph, CellMapper cm) 
    {
        super(cell, graph, cm);
        ComponentCell ccell = (ComponentCell) cell;
        AttributeMap map = ccell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(map);
   		bounds =  new Rectangle((int)bounds.getX(), (int) bounds.getY(), 80, 35 );
   		GraphConstants.setBounds(map, bounds);

        mFont = smDefaultFont; 
        mName = ((ComponentCell)cell).toString();

        mBorderColor = new Color(92, 133, 38); // -- kinda pea green
        mFillColor   = new Color(255,255,153); // -- kinda yellowish
  
        mStroke = new BasicStroke(sBORDER / 3, 
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL, 10, null, 0);
	
    } // -- Constructor
    
    
    public void clackPaint() {
    	Graphics2D g2 = (Graphics2D)graph.getGraphics();

		if(g2 == null){		return;	}
		double scale = graph.getScale();
		g2.scale(scale, scale);
    	graph.getUI().paintCell(g2, this, getBounds(), false);

    }

    // -----------------------------------------------------------------------
    //                             Rendering
    // -----------------------------------------------------------------------

    // Returns the Renderer for this View
    public CellViewRenderer getRenderer() {
       return renderer;
    }

    public static SockBufferRenderer renderer = new SockBufferRenderer();

    // Define the Renderer for an ComponentView
    public static class SockBufferRenderer extends VertexRenderer {
        public void paint(Graphics g) 
        {
        	
            Graphics2D g2d = (Graphics2D)g;
            SockBufferView vview = (SockBufferView)view;
            ComponentCell cell = (ComponentCell)vview.getCell();
            SockBuffer buffer = (SockBuffer)cell.getClackComponent();
            
        	if(!vview.getClackView().isVisibleView())
        		return; // dont' draw now, its not visible
            
            
            float occupancy = buffer.getOccupancy();
            float max_size = buffer.getMaxOccupancy();
            
            int width  = (int)vview.getBounds().getWidth();
            int height = (int)vview.getBounds().getHeight();
            int usable_width = width - 2 * sBORDER;
            
            	float percent_occupied = occupancy / max_size;
            	//System.out.println("Queue is " + percent_occupied + "% occupied");
            	int filled_width = (int)(percent_occupied * usable_width);
            	
            	// border
	            g2d.setColor(Color.RED);
	            g2d.drawRect(sBORDER - 1, sBORDER - 1, 
	                    (int)view.getBounds().getWidth()  - sBORDER,
	                    (int)view.getBounds().getHeight() - sBORDER);
	            //background
	            g2d.setColor(Color.GRAY);
	            g.fillRect(sBORDER, sBORDER, 
	                    width  - (2*sBORDER) + 1,
	                    height - (2*sBORDER) + 1);
	            //queue size

	            g2d.setColor(Color.GREEN);
	            //g.fillRect(sBORDER, (height - sBORDER - filled_height),
	          	//	width - (2 * sBORDER), filled_height);
	            g.fillRect((width - sBORDER - filled_width), sBORDER, height - (2 * sBORDER), filled_width );
	

      
        } // -- paint

    } 
}
