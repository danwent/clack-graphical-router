/*
 * Created on Jan 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.router.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.clackrouter.component.base.Queue;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexRenderer;


/**
 * A specialized component view to show the size of a queue graphically.  
 */
public class QueueView extends DynamicClackView  {


	static {
		queue_renderer = new QueueRenderer();
	}
	
	private static QueueRenderer queue_renderer;
    public static int VIEW_WIDTH = 35;
    public static int VIEW_HEIGHT = 80;
    
	public QueueView(Object cell, JGraph graph, CellMapper cm) {
		super(cell, graph, cm, VIEW_WIDTH, VIEW_HEIGHT );
	}

	// Define the Renderer for an ComponentView
	public static class QueueRenderer extends VertexRenderer {
		public void paint(Graphics g) {

			Graphics2D g2d = (Graphics2D) g;
			QueueView vview = (QueueView) view;
			ComponentCell cell = (ComponentCell) vview.getCell();
			Queue queue = (Queue) cell.getClackComponent();

			if (!vview.getClackView().isVisibleView())
				return; // dont' draw now, its not visible

			float occupancy = queue.getOccupancy();
			float max_size = queue.getMaxOccupancy();

			int width = (int) vview.getBounds().getWidth();
			int height = (int) vview.getBounds().getHeight();
			int usable_height = height - 2 * sBORDER;

			float percent_occupied = occupancy / max_size;
			//System.out.println("Queue is " + percent_occupied + "% occupied");
			int filled_height = (int) (percent_occupied * usable_height);

			// border
			g2d.setColor(Color.GREEN);
			g2d.drawRect(sBORDER - 1, sBORDER - 1, (int) view.getBounds()
					.getWidth()
					- sBORDER, (int) view.getBounds().getHeight() - sBORDER);
			//background
			g2d.setColor(Color.GRAY);
			g.fillRect(sBORDER, sBORDER, width - (2 * sBORDER) + 1, height
					- (2 * sBORDER) + 1);
			//queue size
			if (queue.recentDropTest())
				g2d.setColor(Color.RED);
			else
				g2d.setColor(Color.GREEN);
			g.fillRect(sBORDER, (height - sBORDER - filled_height), width
					- (2 * sBORDER), filled_height);

		} // -- paint

	} 
	
	public CellViewRenderer getRenderer() {
		return queue_renderer;
	}
}
