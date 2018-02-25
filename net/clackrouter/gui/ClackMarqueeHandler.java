/*
 * Copyright (C) 2001-2004 Gaudenz Alder
 * JGraphpad is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGraphpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGraphpad; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package net.clackrouter.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import net.clackrouter.jgraph.pad.GPGraphUI;
import net.clackrouter.router.graph.ComponentCell;
import net.clackrouter.router.graph.RouterGraph;

import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.PortView;


/**
 * <p> ClackMarqueeHandler deals with user events for the RouterGraph. </p>
 * 
 * <p> The main role of this class is to handle mouse actions on the Router graph,
 * keeping track of dragging and clicking behavior to implement different functionality </p>
 * 
 * <p> This class is modifed from GPMarqueeHandler from JGraphpad </p>
 */
public class ClackMarqueeHandler extends BasicMarqueeHandler {
	
	public static final int CELL_VERTEX_ELLIPSE = 1;
	public static final int CELL_VERTEX_DEFAULT = 2;
	public static final int CELL_VERTEX_IMAGE = 3;
	public static final int CELL_VERTEX_TEXT = 4;
	public static final int CELL_PORT_DEFAULT = 5;
	public static final int CELL_EDGE_DEFAULT = 6;
	
	private int m_XDifference, m_YDifference, dx, dy;
	
	private boolean m_dragging;
	private PortView mRightClickedComponent;
	private Container c;
	
	/** A reference to the graphpad object
	 */
	protected ClackFramework graphpad;
	
	/** The default color for borders
	 */
	protected Color defaultBorderColor = Color.black;
	
	
	protected Point2D start, current;
	
	protected Rectangle2D bounds;
	protected JComboBox currentComponentType;
	protected PortView port, firstPort, lastPort;
	
	/**
	 * Constructor for GPMarqueeHandler.
	 */
	public ClackMarqueeHandler(ClackFramework graphpad) {
		super();
		this.graphpad = graphpad;
		ButtonGroup grp = new ButtonGroup();

	}
	
	/**
	 * X-Y location of the last mouse click
	 */
	public Point getLastClick() { return new Point(m_XDifference, m_YDifference); }
	
	/** Return true if this handler should be preferred over other handlers. */
	public boolean isForceMarqueeEvent(MouseEvent e) {
		return  isClackPopupTrigger(e)
		|| super.isForceMarqueeEvent(e);
	}
	
	/** implements new definition of a right-click */
	protected boolean isClackPopupTrigger(MouseEvent e) {
		if (e==null) return false;
		return SwingUtilities.isRightMouseButton(e) && !e.isShiftDown();
	}
	
	/**
	 * Handles mousePressed event.
	 * 
	 * <p> We check to see if it is a pop-up trigger, in which case we keep note of the 
	 * component that has been right-clicked </p>
	 * 
	 * <p> We also pass non-popup events to the super-class and take care of 
	 * updating the graph's selected cell </p>
	 */
	public void mousePressed(MouseEvent event) {

		m_XDifference = event.getX();
		m_YDifference = event.getY();

		m_dragging = false;// assume false, overlay will set it true if needed
		dx = 0;
		dy = 0;
		 if(!event.isConsumed()) {
			start = graphpad.getCurrentRouterGraph().snap(event.getPoint());
			firstPort = port;
			if (port != null) {
				start =
					graphpad.getCurrentRouterGraph().toScreen(
							port.getLocation(null));
			}
			if(isClackPopupTrigger(event)){
				// start of edge creation section
				mRightClickedComponent = getPortViewAt(
					event.getX(),
					event.getY(),
					!event.isShiftDown());
				firstPort = mRightClickedComponent; // stupid, stupid
			
				if (firstPort != null)
					start =
					graphpad.getCurrentRouterGraph().toScreen(
							firstPort.getLocation(null));
				// end of section
			}
			event.consume();
		}

		if ( !isClackPopupTrigger(event)) {
			super.mousePressed(event); 
			graphpad.getCurrentRouterGraph().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			event.consume();
		}
		else {
			boolean selected = false;
			Object[] cells = graphpad.getCurrentRouterGraph().getSelectionCells();
			for (int i = 0; i < cells.length && !selected; i++)
				selected =
					graphpad.getCurrentRouterGraph().getCellBounds(
							cells[i]).contains(
									event.getPoint());
			if (!selected)
				graphpad.getCurrentRouterGraph().setSelectionCell(
						graphpad.getCurrentRouterGraph().getFirstCellForLocation(
								event.getX(),
								event.getY()));
			event.consume();
		}
	}
	
	// i'm not going to lie... i really don't know what the JGraph people are doing here...
	public void mouseDragged(MouseEvent event) {
		if (!event.isConsumed() && mRightClickedComponent != null) {
			Graphics g = graphpad.getCurrentRouterGraph().getGraphics();
			Color bg = graphpad.getCurrentRouterGraph().getBackground();
			Color fg = Color.black;
			g.setColor(fg);
			g.setXORMode(bg);
			overlay(g);
			current = graphpad.getCurrentRouterGraph().snap(event.getPoint());
				port =
					getPortViewAt(
							event.getX(),
							event.getY(),
							!event.isShiftDown());
				if (port != null) {
					if (port != firstPort)
					current =
						graphpad.getCurrentRouterGraph().toScreen(
								port.getLocation(null));
				}
			
			if (start != null && current != null) {
				Point tempStart = new Point((int)start.getX(),(int)start.getY());
				Point tempCurrent = new Point((int)current.getX(),(int)current.getY());
				bounds = new Rectangle(tempStart).union(new Rectangle(tempCurrent));
				g.setColor(bg);
				g.setXORMode(fg);
				overlay(g);
			}
			event.consume();
		} else if (
				!event.isConsumed()
				&& isForceMarqueeEvent(event)
				&& isClackPopupTrigger(event)) {
			c = graphpad.getCurrentRouterGraph().getParent();
			if (c instanceof JViewport) {
				JViewport jv = (JViewport) c;
				Point p = jv.getViewPosition();
				int newX = p.x - (event.getX() - m_XDifference);
				int newY = p.y - (event.getY() - m_YDifference);
				dx += (event.getX() - m_XDifference);
				dy += (event.getY() - m_YDifference);
				
				int maxX =
					graphpad.getCurrentRouterGraph().getWidth() - jv.getWidth();
				int maxY =
					graphpad.getCurrentRouterGraph().getHeight() - jv.getHeight();
				if (newX < 0)
					newX = 0;
				if (newX > maxX)
					newX = maxX;
				if (newY < 0)
					newY = 0;
				if (newY > maxY)
					newY = maxY;
				
				jv.setViewPosition(new Point(newX, newY));
				event.consume();
			}
		}
		if ( !event.isConsumed() ) {
			super.mouseDragged(event);
			event.consume();
		}
	}
	
	// Default Port is at index 0
	public PortView getPortViewAt(int x, int y, boolean jump) {
		Point2D sp = graphpad.getCurrentRouterGraph().fromScreen(new Point(x, y));
		PortView port = graphpad.getCurrentRouterGraph().getPortViewAt(sp.getX(), sp.getY());
		// Shift Jumps to "Default" Port (child index 0)
		if (port == null && jump) {
			Object cell =
				graphpad.getCurrentRouterGraph().getFirstCellForLocation(x, y);
			if (graphpad.getCurrentRouterGraph().isVertex(cell)) {
				Object firstChild =
					graphpad.getCurrentRouterGraph().getModel().getChild(cell, 0);
				CellView firstChildView =
					graphpad
					.getCurrentRouterGraph()
					.getGraphLayoutCache()
					.getMapping(
							firstChild,
							false);
				if (firstChildView instanceof PortView)
					port = (PortView) firstChildView;
			}
		}
		return port;
	}
	
	public void mouseReleased(MouseEvent event) {
		GraphModel model = graphpad.getCurrentRouterGraph().getModel();

		if (isClackPopupTrigger(event) && !m_dragging) {
			// if we're not dragging, its a popup
			mouseReleasedPopup(event);

		} else if (event != null && !event.isConsumed()
				&& bounds != null && mRightClickedComponent != null) {
			// otherwise, see if this makes a port connection
			graphpad.getCurrentRouterGraph().fromScreen(bounds);
			bounds.setRect(bounds.getX(),
					bounds.getY(),
					bounds.getWidth() + 1,
					bounds.getHeight() + 1);
			if (firstPort != null && port != null && firstPort != port) {
				graphpad.getClackGraphHelper().attemptPortConnection(
						(DefaultPort) firstPort.getCell(),(DefaultPort) port.getCell() );
				event.consume();
			}
	  
		}

		if ( event != null ) {
			if ( !event.isConsumed() ) {
				super.mouseReleased(event);
			}
		}
		// reset everything that were were keeping track of since
		// the mouse has been release
		firstPort = null;
		port = null;
		start = null;
		current = null;
		bounds = null;
		m_dragging = false; // just released
		mRightClickedComponent = null;
			graphpad.getCurrentRouterGraph().setCursor(
					new Cursor(Cursor.DEFAULT_CURSOR));
		graphpad.updateUI(); // refreshes the screen
	}
	
	protected void mouseReleasedPopup(MouseEvent event) {
		if (Math.abs(dx) < graphpad.getCurrentRouterGraph().getTolerance()
				&& Math.abs(dy) < graphpad.getCurrentRouterGraph()
						.getTolerance()) {
			Object cell = graphpad.getCurrentRouterGraph()
					.getFirstCellForLocation(event.getX(), event.getY());
			if(cell != null && !(cell instanceof ComponentCell)){
				return;
			}

			if (cell == null)
				graphpad.getCurrentRouterGraph().clearSelection();
			

			JPopupMenu pop = graphpad.getBarFactory().createGraphPopupMenu((ComponentCell)cell);
			System.out.println("Showing popup");
			pop.show(graphpad.getCurrentRouterGraph(), event.getX(), event
					.getY());
	

		}
		event.consume();
	}
	
	public void mouseMoved(MouseEvent event) {
		if (!event.isConsumed()) {
			event.consume();

			if(mRightClickedComponent != null) {
				PortView oldPort = port;
				PortView newPort =
					getPortViewAt(
							event.getX(),
							event.getY(),
							!event.isShiftDown());
				if (oldPort != newPort) {
					Graphics g = graphpad.getCurrentRouterGraph().getGraphics();
					Color bg = graphpad.getCurrentRouterGraph().getBackground();
					Color fg = graphpad.getCurrentRouterGraph().getMarqueeColor();
					g.setColor(fg);
					g.setXORMode(bg);
					overlay(g);
					port = newPort;
					g.setColor(bg);
					g.setXORMode(fg);
					overlay(g);
				}
			}
		}
		super.mouseMoved(event);
	}
	
	public void overlay(Graphics g) {
		super.overlay(g);
		RouterGraph gpgraph= graphpad.getCurrentRouterGraph();
		if ( gpgraph != null )
		{
			// Sometimes when loading a graph something gets
			// out of sequence and the GPGraph object hasn't
			// been created at this point.  Missing the 
			// PaintPort() call is minor compared to the NPE
			paintPort(gpgraph.getGraphics());
		}
		
		if (bounds != null && start != null) {
			m_dragging = true;
			if (mRightClickedComponent != null &&  current != null) {
				g.drawLine((int)start.getX(),
						(int)start.getY(),
						(int)current.getX(),
						(int)current.getY());
			}else 
				g.drawRect((int)bounds.getX(),
						(int)bounds.getY(),
						(int)bounds.getWidth(),
						(int)bounds.getHeight());
		}
	}
	
	protected void paintPort(Graphics g) {
		if (port != null) {
			boolean offset =
				(GraphConstants.getOffset(port.getAllAttributes()) != null);
			Rectangle2D r = 
				(offset) ? port.getBounds() : port.getParentView().getBounds();
				r = (graphpad.getCurrentRouterGraph().toScreen(new Rectangle(r.getBounds())));
				int s = 3;
				Rectangle tempRect = (Rectangle)r;
				tempRect.translate(-s, -s);
				r.setRect((int)r.getX(),
						(int)r.getY(),
						(int)(r.getWidth() + 2 * s),
						(int)(r.getHeight() + 2 * s));
				GPGraphUI ui = (GPGraphUI) graphpad.getCurrentRouterGraph().getUI();
				ui.paintCell(g, port, r, true);
		}
	}
	

}
