/*
 * Copyright (C) 2001-2004 Gaudenz Alder
 *
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

package net.clackrouter.router.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.jgraph.pad.GPGraphUI;
import net.clackrouter.jgraph.pad.GPUserObject;
import net.clackrouter.jgraph.pad.resources.Translator;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.Port;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;
import org.jgraph.util.JGraphUtilities;


/**
 * 
 * The extension of JGraph to display a modular Clack router.
 * 
 * <p> This is essentially a GPGraph from JGraphpad with some minor modifications.  
 * There is little Clack specific code in here, save tooltips </p>
 */

public class RouterGraph extends JGraph {
	
	protected boolean pagevisible = false;
	protected boolean displayelements = true;
	protected transient PageFormat pageFormat = new PageFormat();	
	protected Image background = null;	
	protected transient Color defaultBorderColor = Color.black;
	
	public RouterGraph() {	this(null);	}
	
	public RouterGraph(GraphModel model) {
		this(model, null);
	}
	
	public RouterGraph(GraphModel model, GraphLayoutCache view) {
		super(model, view, null);
		// TODO: Remove this code when VM is fixed on Mac OS X
		if (System.getProperty("os.name").equals("Mac OS X")) {
			String s = Translator.getString("doubleBufferedOnMacOSX");
			if (s != null)
				setDoubleBuffered(Boolean.getBoolean(s));
		}
		setDragEnabled(false);
		updateUI();	
	}
	
	public static GPUserObject getGPUserObject(Object cell) {
		if (cell instanceof DefaultMutableTreeNode) {
			Object user = ((DefaultMutableTreeNode) cell).getUserObject();
			if (user instanceof GPUserObject)
				return (GPUserObject) user;
		}
		return null;
	}
	
	public static void addSampleData(GraphModel model) {}
	
	public void setPageFormat(PageFormat format) {	pageFormat = format;}
	public PageFormat getPageFormat() {	return pageFormat;	}	
	public void setDisplayElements(boolean flag){	displayelements = flag;}	
	public boolean isDisplayElements(){ return displayelements; }	
	
	public void setPageVisible(boolean flag) {
		pagevisible = flag;
	}
	
	public boolean isPageVisible() {
		return pagevisible;
	}	
	public void setBackgroundImage(Image img) {
		background = img;
	}
	
	public Image getBackgroundImage() {
		return background;
	}
	
	protected EdgeView createEdgeView(JGraph graph, CellMapper mapper, Object cell) {
		if (cell instanceof RouterWire)
			return new WireView(cell, graph, mapper);
		
		return null;
	}
	
	/**
	 * Creates and returns a default <code>GraphView</code>.
	 *
	 * @return the default <code>GraphView</code>
	 */
	protected VertexView createVertexView(JGraph graph,CellMapper cm, Object v ) {
	
		if(v instanceof ComponentCell){
			ClackComponent comp = (ClackComponent)((ComponentCell)v).getClackComponent();
			return comp.getView(graph, cm);
		}
		
		return super.createVertexView(graph, cm, v);
	}
	
	public static VertexRenderer renderer = new ScaledVertexRenderer();
	
	public class ScaledVertexView extends VertexView {
		
		public ScaledVertexView(Object v, JGraph graph, CellMapper cm) {
			super(v, graph, cm);
		}
		
		public CellViewRenderer getRenderer() {
			return RouterGraph.renderer;
		}
		
	}
	
	public static class ScaledVertexRenderer extends VertexRenderer {
		
		public void paint(Graphics g) {
			Icon icon = getIcon();
			setIcon(null);
			Dimension d = getSize();
			Image img = null;
			if (icon instanceof ImageIcon)
				img = ((ImageIcon) icon).getImage();
			if (img != null)
				g.drawImage(img, 0, 0, d.width - 1, d.height - 1, graph);
			super.paint(g);
		}
		
	}
	
	//
	// Defines Semantics of the Graph
	//
	
	/**
	 * Returns true if <code>object</code> is a vertex, that is, if it
	 * is not an instance of Port or Edge, and all of its children are
	 * ports, or it has no children.
	 */
	public boolean isGroup(Object cell) {
		// Map the Cell to its View
		CellView view = getGraphLayoutCache().getMapping(cell, false);
		if (view != null)
			return !view.isLeaf();
		return false;
	}
	
	/**
	 * Returns true if <code>object</code> is a vertex, that is, if it
	 * is not an instance of Port or Edge, and all of its children are
	 * ports, or it has no children.
	 */
	public boolean isVertex(Object object) {
		if (!(object instanceof Port) && !(object instanceof Edge))
			return !isGroup(object) && object != null;
		return false;
	}
	
	public boolean isPort(Object object) {
		return (object instanceof Port);
	}
	
	public boolean isEdge(Object object) {
		return (object instanceof Edge);
	}
	
	public Object[] getSelectionVertices() {
		Object[] tmp = getSelectionCells();
		Object[] all =
			DefaultGraphModel.getDescendants(getModel(), tmp).toArray();
		return getVertices(all);
	}
	
	public Object[] getVertices(Object[] cells) {
		if (cells != null) {
			ArrayList result = new ArrayList();
			for (int i = 0; i < cells.length; i++)
				if (isVertex(cells[i]))
					result.add(cells[i]);
			return result.toArray();
		}
		return null;
	}
	
	public Object[] getSelectionEdges() {
		return getEdges(getSelectionCells());
	}
	
	public Object[] getAll() {
		return getDescendants(getRoots());
	}
	
	public Object[] getEdges(Object[] cells) {
		if (cells != null) {
			ArrayList result = new ArrayList();
			for (int i = 0; i < cells.length; i++)
				if (isEdge(cells[i]))
					result.add(cells[i]);
			return result.toArray();
		}
		return null;
	}
	
	public Object getNeighbour(Object edge, Object vertex) {
		Object source = getSourceVertex(edge);
		if (vertex == source)
			return getTargetVertex(edge);
		else
			return source;
	}
	
	public Object getSourceVertex(Object edge) {
		Object sourcePort = graphModel.getSource(edge);
		return graphModel.getParent(sourcePort);
	}
	
	public Object getTargetVertex(Object edge) {
		Object targetPort = graphModel.getTarget(edge);
		return graphModel.getParent(targetPort);
	}
	
	public CellView getSourceView(Object edge) {
		Object source = getSourceVertex(edge);
		return getGraphLayoutCache().getMapping(source, false);
	}
	
	public CellView getTargetView(Object edge) {
		Object target = getTargetVertex(edge);
		return getGraphLayoutCache().getMapping(target, false);
	}
	
	public Object[] getEdgesBetween(Object vertex1, Object vertex2) {
		ArrayList result = new ArrayList();
		Set edges =
			DefaultGraphModel.getEdges(graphModel, new Object[] { vertex1 });
		Iterator it = edges.iterator();
		while (it.hasNext()) {
			Object edge = it.next();
			Object source = getSourceVertex(edge);
			Object target = getTargetVertex(edge);
			if ((source == vertex1 && target == vertex2)
					|| (source == vertex2 && target == vertex1))
				result.add(edge);
		}
		return result.toArray();
	}
	
	/**
	 * Overrides <code>JComponent</code>'buttonSelect <code>getToolTipText</code>
	 * method in order to allow the graph controller to create a tooltip
	 * for the topmost cell under the mousepointer. This differs from JTree
	 * where the renderers tooltip is used.
	 * <p>
	 * NOTE: For <code>JGraph</code> to properly display tooltips of its
	 * renderers, <code>JGraph</code> must be a registered component with the
	 * <code>ToolTipManager</code>.  This can be done by invoking
	 * <code>ToolTipManager.sharedInstance().registerComponent(graph)</code>.
	 * This is not done automatically!
	 * @param event the <code>MouseEvent</code> that initiated the
	 * <code>ToolTip</code> display
	 * @return a string containing the  tooltip or <code>null</code>
	 * if <code>event</code> is null
	 */
	public String getToolTipText(MouseEvent event) {

		if (event != null) {
			Object cell = getFirstCellForLocation(event.getX(), event.getY());
			if (cell != null) {
				CellView view = getGraphLayoutCache().getMapping(cell, false);
				StringBuffer sb = new StringBuffer(40);
				sb.append("<html>");

				java.util.List points = GraphConstants.getPoints(view.getAttributes());

				if (cell instanceof ComponentCell) {
					ComponentCell compCell = (ComponentCell)cell;
					sb.append("<strong>" + compCell.getClackComponent().getName() + "</strong>");
				} else if (cell instanceof RouterWire) {
					RouterWire wire = (RouterWire) cell;
					ClackComponent src = wire.sourceComponent, dst = wire.targetComponent;	
					sb.append("<strong>Source:</strong> " + src.getTypeName()+ " (" + src.getPort(wire.sourcePortNum).getTextDescription() + ")<br>");
					sb.append("<strong>Target:</strong> " + dst.getTypeName()+ " (" + dst.getPort(wire.targetPortNum).getTextDescription() + ")<br>");
				}
				sb.append("</html>");
				return sb.toString();
			}
		}
		return null;
	}
	

	
	/**
	 * Notification from the <code>UIManager</code> that the L&F has changed.
	 * Replaces the current UI object with the latest version from the
	 * <code>UIManager</code>. Subclassers can override this to support
	 * different GraphUIs.
	 * @see JComponent#updateUI
	 *
	 */
	public void updateUI() {
		setUI(new GPGraphUI());
		invalidate();
	}
	
	
	/** Returns true if the given vertices are conntected by a single edge
	 * in this document.
	 */
	public boolean isNeighbour(Object v1, Object v2) {
		return JGraphUtilities.isNeighbour(this, v1, v2);
	}
	
	
	/* Inserted from old JGraph code, this scales the views before searching  */
	public CellView getNextSelectableViewAt(CellView c, double x, double y) {
		CellView[] cells = getGraphLayoutCache().getMapping(
				getSelectionModel().getSelectables(), false);

			boolean leafsOnly = false;
			if (cells != null) {
				x /= scale;
				y /= scale;
				Rectangle2D r = new Rectangle2D.Double(x - tolerance,
						y - tolerance, 2 * tolerance, 2 * tolerance);
				// Iterate through cells and switch to active
				// if current is traversed. Cache first cell.
				CellView first = null;
				boolean active = (c == null);
				Graphics g = getGraphics();
				for (int i = 0; i < cells.length; i++) {
					if (cells[i] != null && cells[i].intersects(g, r) &&
						(!leafsOnly || cells[i].isLeaf())) {
						// TODO: This behaviour is specific to selection and
						// should be parametrized (it only returns a group with
						// selected children if no other portview is available)
						if (active && !selectionModel.isChildrenSelected(cells[i]
																				.getCell()))
							return cells[i];
						else if (first == null)
							first = cells[i];
						active = active | (cells[i] == c);
					}
				}
				if (first != null)
					return first;
			}
			return null;
	
	}
	

}
