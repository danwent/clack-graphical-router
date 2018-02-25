package net.clackrouter.topology.graph;

/* Based on:
 * @(#)MyPortView	1.0.5 06/01/03
 *
 * Copyright (C) 2003 Gaudenz Alder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

import net.clackrouter.jgraph.pad.resources.ImageLoader;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.PortRenderer;
import org.jgraph.graph.PortView;

/**
 * 
 * View of an interface for the Topology Graph.  
 */
public class TopoInterfaceView extends PortView {

	protected static ImageIcon portIcon = null;
	
	public static int  smDefaultFontSize = 10; 
    public static Font smDefaultFont     = new Font(null, Font.BOLD , smDefaultFontSize);

    protected Font mFont     = null;
    protected String mName   = null;
    
	public static int WIDTH = 40;
	public static int HEIGHT = 20;
	
    public static int  sIconWidth  = 14;
    public static int  sIconHeight = 14;
	
	protected static TopoInterfaceRenderer renderer = new TopoInterfaceRenderer();
	
	static {
		portIcon = ImageLoader.getImageIcon("port.gif");
	}
	

    public TopoInterfaceView(Object cell, JGraph graph, CellMapper cm) {
        super(cell, graph, cm);
        
        ((TopoInterfaceCell)cell).mView = this;
        mFont = smDefaultFont; 
        
        mName = ((TopoInterfaceCell)cell).mInterface.name;
    }

	/** 
	* Returns the bounds for the port view. 
	*/
	public Rectangle2D getBounds() 
    {  
		/// comment this out jon
		
        if (portIcon != null) {
            Rectangle2D bounds = getAttributes().createRect(getLocation(null));
            int width = portIcon.getIconWidth();
            int height = portIcon.getIconHeight();

            bounds.setFrame(
                    (int)bounds.getX() - width / 2,
                    (int)bounds.getY() - height / 2,
                    (int)width + 25,
                    (int)height + 25);

            return bounds;
        }
        
        
	/*	Rectangle2D bounds = getAttributes().createRect(getLocation(null));
        bounds.setFrame(
                (int)bounds.getX() - WIDTH / 2,
                (int)bounds.getY() - HEIGHT / 2,
                (int)WIDTH,
                (int)HEIGHT);
        return bounds;
        */
        return super.getBounds();
        
    } // -- getBounds()

	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public static class TopoInterfaceRenderer extends PortRenderer 
    {

		public void paint(Graphics g) {
			//g.setColor(graph.getBackground());
			Dimension d = getSize();
			//g.setXORMode(graph.getBackground());
			if (preview) {
				
				g.setColor(Color.red);
				g.drawRect(1, 1, d.width - 3, d.height - 3);
				g.drawRect(2, 2, d.width - 5, d.height - 5);
			} 
			else {
				g.setColor(Color.BLACK);	
				TopoInterfaceCell cell = (TopoInterfaceCell)view.getCell();
				TopoInterfaceView vview = (TopoInterfaceView)view;

				portIcon.paintIcon(graph, g, 0, 0);
			/*	g.setColor(Color.WHITE);
	            g.fillRect(2, 2, 
	                    (int)view.getBounds().getWidth() - 4,
	                    (int)view.getBounds().getHeight() - 4);
	            g.setColor(Color.BLACK);
				g.drawString(cell.mInterface.name,10, 12);
				*/
	            //g.setFont(vview.mFont);
	            //int strwidth = g.getFontMetrics().stringWidth(vview.mName);

	            int width  = (int)vview.getBounds().getWidth();
	            int height = (int)vview.getBounds().getHeight();
				
	            g.setColor(Color.BLACK);
	            g.setFont(vview.mFont);
	            
	            int strwidth = g.getFontMetrics().stringWidth(vview.mName);

	            g.drawString(vview.mName, sIconWidth, sIconHeight + 3); // a bit lower
	            /*
	            for(int i = 0; i < cell.mHost.interfaces.size(); i++){
	            	String interfaceString = ((TopoParser.Interface) cell.mHost.interfaces.get(i)).name+": "+((TopoParser.Interface) cell.mHost.interfaces.get(i)).address.toString();
	            	strwidth = g.getFontMetrics().stringWidth(interfaceString);
	            	g.drawString(interfaceString, (width/2) - (strwidth/2), sIconHeight + shPAD + 10*(i+1));
	            }
	            */
	            
			}
		}

	} // -- public static class VPortRenderer
}
