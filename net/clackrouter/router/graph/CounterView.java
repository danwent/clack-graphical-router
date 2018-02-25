
package net.clackrouter.router.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.clackrouter.component.extension.Counter;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexRenderer;


/**
 * A specialized view to paint an updating counter value instead of 
 * the standard view.  
 */
public class CounterView extends DynamicClackView  {
    static {
        counter_renderer = new CounterRenderer();
    }
    
    private static CounterRenderer counter_renderer;
    public static int VIEW_WIDTH = 70;
    public static int VIEW_HEIGHT = 30;
    
    // Constructor for Superclass
    public CounterView(Object cell, JGraph graph, CellMapper cm) 
    {
        super(cell, graph, cm, VIEW_WIDTH, VIEW_HEIGHT);
    	Counter comp = (Counter)((ComponentCell)cell).getClackComponent(); 	
    	comp.setCounterView(this);
    } // -- Constructor
    


    // Define the Renderer for an ComponentView
    public static class CounterRenderer extends VertexRenderer {
        
    	public void paint(Graphics g)  {
        	
            Graphics2D g2d = (Graphics2D)g;
            CounterView vview = (CounterView)view;
            ComponentCell cell = (ComponentCell)vview.getCell();
            Counter counter = (Counter)cell.getClackComponent();
            
        	if(!vview.getClackView().isVisibleView())
        		return; // dont' draw now, its not visible
                               
            int width  = (int)vview.getBounds().getWidth();
            int height = (int)vview.getBounds().getHeight();
            String label = counter.getCLabel();

            g2d.setColor(vview.mBorderColor);
            g2d.setStroke(vview.mStroke);
            g2d.drawRect(sBORDER - 1, sBORDER - 1, 
            		(int)view.getBounds().getWidth()  - sBORDER,
					(int)view.getBounds().getHeight() - sBORDER);
            g.setColor(vview.mFillColor);
            g.fillRect(sBORDER, sBORDER, 
            		width  - (2*sBORDER)+1,
					height - (2*sBORDER)+1);
            g.setColor(Color.BLACK);
	
            g.setFont(vview.mFont);
            int strwidth = g.getFontMetrics().stringWidth(label);
	          
            g.drawString(label, (int)(width/2) - (strwidth/2),(int) (height/4) + vview.mFontSize/2);  
           	String count = counter.getPacketCountIn() + "";
           	int ip_strwidth = g.getFontMetrics().stringWidth(count);
           	g.drawString(count, (int)(width/2) - (ip_strwidth/2), (int) (3*height)/4 + vview.mFontSize/2);
        } // -- paint

    } 
    
    // Returns the Renderer for this View
    public CellViewRenderer getRenderer() {
       return counter_renderer;
    }
    
}
