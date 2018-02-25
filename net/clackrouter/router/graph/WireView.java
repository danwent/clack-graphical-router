
package net.clackrouter.router.graph;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Map;

import javax.swing.SwingUtilities;

import net.clackrouter.gui.ClackPaintable;
import net.clackrouter.gui.ClackView;
import net.clackrouter.router.core.Alerter;
import net.clackrouter.topology.graph.TopoWire;

import org.jgraph.JGraph;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.PortView;
import org.jgraph.plaf.GraphUI;
import org.jgraph.plaf.basic.BasicGraphUI;

/**
 * Represents a view of both a {@link RouterWire} and {@link TopoWire} . 
 * 
 * Based heavily on the EdgeView JGraph class.  
 */

public class WireView extends EdgeView implements  Alerter.Alertable, ClackPaintable{


	public WireRenderer wireRenderer;
	private ClackView mView;
	
	private boolean isHighlighted;

	public CellViewRenderer getRenderer() {
		return wireRenderer;
	}
	
	public ClackView getClackView() { return ((Wire)getCell()).getClackView(); }
	
	public boolean isHighlighted() { return isHighlighted; }
	
	public void startHighlight() { 
		if(isHighlighted){	
	//		System.out.println("ignoring highlight");
			return; 
		}
	
		isHighlighted = true; 

		clackPaint();
		isHighlighted = false;
	//	System.out.println("highlighting wire!");
    	
    	Object cell = this.getCell();
    	if(cell instanceof RouterWire) {
    		((RouterWire)cell).sourceComponent.getRouter().getAlerter().addAlerter(this);
    	}else if(cell instanceof TopoWire){
    		((TopoWire)cell).getTopologyModel().getAlerter().addAlerter(this);
    	}
    		
  	
	}
	
	public void clackPaint(){
		if(getClackView() == null){
			System.err.println("Clack View is null for wire");
			return;
		}
		
		if(!getClackView().isVisibleView()) return;  // check to make sure we are the currently viewed level
										//  in the current type tab
		
		Graphics2D g2 = (Graphics2D)graph.getGraphics();

    	if(g2 == null) 	return;
  
    	double scale = graph.getScale();	
		g2.scale(scale, scale);

    	graph.getUI().paintCell(g2, this, this.getBounds(), false);		
	}

	
	public void alert() {
	//	System.out.println("Wire view alerted");
		isHighlighted = false;
		clackPaint();   	
	}


	/**
	 * Constructs an edge view for the specified model object.
	 * 
	 * @param cell
	 *            reference to the model object
	 */
	public WireView(Object cell, JGraph graph, CellMapper mapper) {
		
		super(cell, graph, mapper);
		wireRenderer = new WireRenderer(this);
		Wire wire = (Wire)cell;
		wire.setWireView(this);
		isHighlighted = false;
	}

	//
	// Data Source
	//

	/**
	 * Overrides the parent method to udpate the cached points, source and
	 * target port. If the source or target is removed, a point is inserted into
	 * the array of points.
	 */
	public void refresh(boolean createDependentViews) {
		super.refresh(createDependentViews);
	
		// Sync Source- and Targetport
		if (points != null) {
			Object modelSource = getModel().getSource(cell);
			Object modelTarget = getModel().getTarget(cell);
			setSource(mapper.getMapping(modelSource, createDependentViews));
			setTarget(mapper.getMapping(modelTarget, createDependentViews));
			// Re-Route
			Edge.Routing routing = GraphConstants.getRouting(allAttributes);
			if (routing != null)
				routing.route(this, points);
		}
	}

	/**
	 * Update attributes and recurse children.
	 */
	public void update() {
		// begin from AbstractCellView
		updateAllAttributes();
		// Notify Parent
		//groupBounds = null;
		childUpdated();
		
		// end from AbstractCellView
		
		points = GraphConstants.getPoints(allAttributes);
		labelPosition = GraphConstants.getLabelPosition(allAttributes);
		Point[] positions = null;//GraphConstants
				//.getExtraLabelPositions(allAttributes);
		//if (positions != null) {
		//	extraLabelPositions = new Point[positions.length];
		//	for (int i = 0; i < positions.length; i++)
		//		extraLabelPositions[i] = positions[i];
		//} else
		//	extraLabelPositions = null;
		Edge.Routing routing = GraphConstants.getRouting(allAttributes);
		if (routing != null)
			routing.route(this, points);
		// Synchronize Points and PortViews
		if (getModel().getSource(cell) != null)
			setSource(getSource());
		if (getModel().getTarget(cell) != null)
			setTarget(getTarget());
		// Clear cached shapes
		beginShape = null;
		endShape = null;
		lineShape = null;
		wire_view_invalidate();
	}

		
	void wire_view_invalidate() {
		sharedPath = null;
		cachedBounds = null;
		cachedLabelBounds = null;
		//cachedExtraLabelBounds = null;
	}

	/**
	 * Returns the shape of the view according to the last rendering state
	 */
	public Shape getShape() {
		if (sharedPath != null)
			return sharedPath;
		else
			return sharedPath = (GeneralPath) wireRenderer.createShape();
	}

	/**
	 * Returns the bounds of label according to the last rendering state
	 */
/*	public Rectangle2D getLabelBounds() {
		if (cachedLabelBounds != null) {
			return cachedLabelBounds;
		} else {
			return cachedLabelBounds = getEdgeRenderer().getLabelBounds(this);
		}
	}
*/
	/**
	 * Returns the bounds of label according to the last rendering state
	 */
/*	public Rectangle2D getExtraLabelBounds(int index) {
		if (cachedLabelBounds != null && index < cachedExtraLabelBounds.length
				&& cachedExtraLabelBounds[index] != null) {
			return cachedExtraLabelBounds[index];
		} else {
			return cachedExtraLabelBounds[index] = getEdgeRenderer()
					.getExtraLabelBounds(this, index);
		}
	}
*/
	//
	// View Methods
	//

	/**
	 * Returns true if this view intersects the given rectangle.
	 */
	public boolean intersects(Graphics g, Rectangle2D rect) {
		if (!isLeaf()) {
			return super.intersects(g, rect);
		} else {
			Rectangle r = new Rectangle((int) rect.getX(), (int) rect.getY(),
					(int) rect.getWidth(), (int) rect.getHeight());
			return wireRenderer.intersects(g, this, r);
		}
	}

	/**
	 * Returns the location for this portview.
	 */
	public Rectangle2D getBounds() {
		Rectangle2D rect = null; // super.getBounds(); dw: weirdness
		if (rect == null) {
			if (cachedBounds == null) {
				cachedBounds = wireRenderer.getBounds(this);
			}
			rect = cachedBounds;
		}
		return rect;
	}

	/**
	 * Returns the local renderer. Do not access the renderer field directly.
	 * Use this method instead!
	 */
//	public EdgeRenderer getEdgeRenderer() {
//		return (EdgeRenderer) getRenderer();
//	}


	/**
	 * Returns a cell handle for the view.
	 */
	public CellHandle getHandle(GraphContext context) {
		return new EdgeHandle(this, context);
	}

	//
	// Cached Values
	//

	/**
	 * Returns the CellView that represents the source of the edge.
	 */
	public CellView getSource() {
		return source;
	}

	/**
	 * Sets the <code>sourceView</code> of the edge.
	 */
	public void setSource(CellView sourceView) {
		source = sourceView;
		if (source != null)
			points.set(0, source);
		else
			points.set(0, getPoint(0));
		wire_view_invalidate();
	}

	/**
	 * Returns the CellView that represents the target of the edge.
	 */
	public CellView getTarget() {
		return target;
	}

	/**
	 * Sets the <code>targetView</code> of the edge.
	 */
	public void setTarget(CellView targetView) {
		target = targetView;
		int n = points.size() - 1;
		if (target != null)
			points.set(n, target);
		else
			points.set(n, getPoint(n));
		wire_view_invalidate();
	}

	/**
	 * Returns a point that describes the position of the label.
	 */
//	public Point2D getExtraLabelPosition(int index) {
//		return extraLabelPositions[index];
//	}

	/**
	 * Returns a point that describes the position of the label.
	 */
	public Point2D getLabelPosition() {
		return labelPosition;
	}

	/**
	 * Sets the description of the label position.
	 */
	public void setLabelPosition(Point2D pos) {
		labelPosition.setLocation(pos);
		wire_view_invalidate();
	}

	/**
	 * Sets the description of the label position.
	 */
//	public void setExtraLabelPosition(int index, Point2D pos) {
//		extraLabelPositions[index].setLocation(pos);
//		invalidate();
//	}

	//
	// Points
	//

	/**
	 * Returns the points.
	 * 
	 * @return java.util.List
	 */
	public java.util.List getPoints() {
		return points;
	}

	/**
	 * Returns the number of point for this edge.
	 */
	public int getPointCount() {
		return points.size();
	}

	/**
	 * Returns the cached points for this edge.
	 */
	public Point2D getPoint(int index) {
		Object obj = points.get(index);
		if (obj instanceof PortView)
			// Port Location Seen From This Edge
			return ((PortView) obj).getLocation(this);
		else if (obj instanceof CellView) {
			// Should not happen
			Rectangle2D r = ((CellView) obj).getBounds();
			return getAttributes().createPoint(r.getX(), r.getY());
		} else if (obj instanceof Point2D)
			// Regular Point
			return (Point2D) obj;
		return null;
	}

	/**
	 * Sets the point at <code>index</code> to <code>p</code>.
	 */
	public void setPoint(int index, Point2D p) {
		points.set(index, p);
		wire_view_invalidate();
	}

	/**
	 * Adds <code>p</code> at position <code>index</code>.
	 */
	public void addPoint(int index, Point2D p) {
		points.add(index, p);
		wire_view_invalidate();
	}

	/**
	 * Removes the point at position <code>index</code>.
	 */
	public void removePoint(int index) {
		points.remove(index);
		wire_view_invalidate();
	}

	//
	// Routing
	//

	public static double getLength(CellView view) {
		double cost = 1;
		if (view instanceof EdgeView) {
			EdgeView edge = (EdgeView) view;
			Point2D last = null, current = null;
			for (int i = 0; i < edge.getPointCount(); i++) {
				current = edge.getPoint(i);
				if (last != null)
					cost += last.distance(current);
				last = current;
			}
		}
		return cost;
	}

	//
	// Handle
	//

	// This implementation uses the point instance to make the change. No index
	// is used for the current point because routing could change the index
	// during
	// the move operation.
	public static class EdgeHandle implements CellHandle, Serializable {

		protected JGraph graph;

		/* Pointer to the edge and its clone. */
		protected EdgeView edge, orig;

		/*
		 * Boolean indicating whether the source, target or label is being
		 * edited.
		 */
		protected boolean label = false, source = false, target = false;

		/**
		 * Holds the index of the current (editing) label or point.
		 */
		protected int currentLabel = -1, currentIndex = -1;
		
		/* Pointer to the currently selected point. */
		protected Point2D currentPoint;

		/* Array of control points represented as rectangles. */
		protected transient Rectangle2D[] r;

		/* A control point for the label position. */
		protected transient Rectangle2D loc;
		
		protected transient Rectangle2D[] extraLabelLocations;

		protected boolean firstOverlayCall = true;

		protected boolean isEdgeConnectable = true;

		protected EdgeView relevantEdge = null;

		public EdgeHandle(EdgeView edge, GraphContext ctx) {
			this.graph = ctx.getGraph();
			this.edge = edge;
			loc = new Rectangle();
			String[] labels = null; //GraphConstants.getExtraLabels(edge.getAllAttributes());
			if (labels != null) {
				extraLabelLocations = new Rectangle[labels.length];
				for (int i=0; i<extraLabelLocations.length; i++)
					extraLabelLocations[i] = new Rectangle();
			}
			orig = (EdgeView) graph.getGraphLayoutCache().getMapping(
					edge.getCell(), false);
			reloadPoints(orig);
			isEdgeConnectable = GraphConstants.isConnectable(edge
					.getAllAttributes());
		}

		protected void reloadPoints(EdgeView edge) {
			relevantEdge = edge;
			r = new Rectangle[edge.getPointCount()];
			for (int i = 0; i < r.length; i++)
				r[i] = new Rectangle();
			invalidate();
		}

		// Update and paint control points
		public void paint(Graphics g) {
			invalidate();
			if (!edge.isLeaf())
				return;
			for (int i = 0; i < r.length; i++) {
				if (isEdgeConnectable)
					g.setColor(graph.getHandleColor());
				else
					g.setColor(graph.getLockedHandleColor());
				g.fill3DRect((int) r[i].getX(), (int) r[i].getY(), (int) r[i]
						.getWidth(), (int) r[i].getHeight(), true);
				CellView port = null;
				if (i == 0 && edge.getSource() != null)
					port = edge.getSource();
				else if (i == r.length - 1 && edge.getTarget() != null)
					port = edge.getTarget();
				if (port != null) {
					g.setColor(graph.getLockedHandleColor());
					Point2D tmp = GraphConstants.getOffset(port
							.getAllAttributes());
					if (tmp != null) {
						g.drawLine((int) r[i].getX() + 1,
								(int) r[i].getY() + 1,
								(int) (r[i].getX() + r[i].getWidth()) - 3,
								(int) (r[i].getY() + r[i].getHeight()) - 3);
						g.drawLine((int) r[i].getX() + 1,
								(int) (r[i].getY() + r[i].getHeight()) - 3,
								(int) (r[i].getX() + r[i].getWidth()) - 3,
								(int) r[i].getY() + 1);
					} else
						g.drawRect((int) r[i].getX() + 2,
								(int) r[i].getY() + 2,
								(int) r[i].getWidth() - 5, (int) r[i]
										.getHeight() - 5);
				}
			}
		}

		public void overlay(Graphics g) {
			if (edge != null && !firstOverlayCall && edge.isLeaf()) {
				//g.setColor(graph.getBackground()); // JDK 1.3
				g.setColor(graph.getForeground());
				g.setXORMode(graph.getBackground().darker());
				Graphics2D g2 = (Graphics2D) g;
				AffineTransform oldTransform = g2.getTransform();
				g2.scale(graph.getScale(), graph.getScale());
				graph.getUI().paintCell(g, edge, edge.getBounds(), true);
				g2.setTransform(oldTransform);
				if (isSourceEditing() && edge.getSource() != null)
					paintPort(g, edge.getSource());
				else if (isTargetEditing() && edge.getTarget() != null)
					paintPort(g, edge.getTarget());
			}
			firstOverlayCall = false;
		}

		protected void paintPort(Graphics g, CellView p) {
			boolean offset = (GraphConstants.getOffset(p.getAllAttributes()) != null);
			Rectangle2D r = (offset) ? p.getBounds() : p.getParentView()
					.getBounds();
			r = graph.toScreen((Rectangle2D) r.clone());
			int s = 3;
			r.setFrame(r.getX() - s, r.getY() - s, r.getWidth() + 2 * s, r
					.getHeight()
					+ 2 * s);
			graph.getUI().paintCell(g, p, r, true);
		}

		protected boolean snap(boolean source, Point2D point) {
			boolean connect = graph.isConnectable() && isEdgeConnectable;
			Object port = graph.getPortForLocation(point.getX(), point.getY());
			if (port != null && connect) {
				CellView portView = graph.getGraphLayoutCache().getMapping(
						port, false);
				if (GraphConstants.isConnectable(portView.getParentView()
						.getAllAttributes())) {
					Object cell = edge.getCell();
					if (source
							&& edge.getGraph().getModel().acceptsSource(cell,
									port)) {
						if (edge.getSource() != portView) {
							overlay(graph.getGraphics());
							edge.setSource(portView);
							edge.update();
							overlay(graph.getGraphics());
						}
						return true;
					} else if (!source
							&& edge.getGraph().getModel().acceptsTarget(cell,
									port)) {
						if (edge.getTarget() != portView) {
							overlay(graph.getGraphics());
							edge.setTarget(portView);
							edge.update();
							overlay(graph.getGraphics());
						}
						return true;
					}
				}
			}
			return false;
		}

		public boolean isConstrainedMoveEvent(MouseEvent e) {
			GraphUI ui = graph.getUI();
			if (ui instanceof BasicGraphUI)
				return ((BasicGraphUI) ui).isConstrainedMoveEvent(e);
			return false;
		}

		/**
		 * Returning true signifies a mouse event adds a new point to an edge.
		 */
		public boolean isAddPointEvent(MouseEvent event) {
			return SwingUtilities.isRightMouseButton(event);
		}

		/**
		 * Returning true signifies a mouse event removes a given point.
		 */
		public boolean isRemovePointEvent(MouseEvent event) {
			return SwingUtilities.isRightMouseButton(event);
		}

		protected boolean isSourceEditing() {
			return source;
		}

		protected boolean isTargetEditing() {
			return target;
		}

		/*
		 * Returns true if either the source, target, label or a point is being
		 * edited.
		 */
		protected boolean isEditing() {
			//return source || target || label || currentLabel >= 0
					//|| currentPoint != null;
			return false; // dw: we never want edges edited
		}

		/**
		 * Invoked when the mouse pointer has been moved on a component (with no
		 * buttons down).
		 */
		public void mouseMoved(MouseEvent event) {
			for (int i = 0; i < r.length; i++)
				if (r[i].contains(event.getPoint())) {
					graph.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					event.consume();
					return;
				}
			if (loc.contains(event.getPoint())
					&& graph.isMoveable()
					&& GraphConstants.isMoveable(edge.getAllAttributes())) {
				graph.setCursor(new Cursor(Cursor.HAND_CURSOR));
				event.consume();
			}
			if (extraLabelLocations != null && graph.isMoveable()
					&& GraphConstants.isMoveable(edge.getAllAttributes())) {
				for (int i=0; i<extraLabelLocations.length; i++) {
					if (extraLabelLocations[i].contains(event.getPoint())) {
						graph.setCursor(new Cursor(Cursor.HAND_CURSOR));
						event.consume();					
					}
				}
			}
		}

		// Handle mouse pressed event.
		public void mousePressed(MouseEvent event) {
			/* INV: currentPoint = null; source = target = label = false; */
			if (!edge.isLeaf())
				return;
			boolean bendable = graph.isBendable()
					&& GraphConstants.isBendable(edge.getAllAttributes());
			boolean disconnectable = graph.isDisconnectable()
					&& GraphConstants.isDisconnectable(orig.getAllAttributes());
			int x = event.getX();
			int y = event.getY();
			// Detect hit on control point
			int index = 0;
			for (index = 0; index < r.length; index++) {
				if (r[index].contains(x, y)) {
					currentPoint = edge.getPoint(index);
					currentIndex = index;
					source = index == 0
							&& (edge.getSource() == null || (disconnectable && GraphConstants
									.isDisconnectable(edge.getSource()
											.getParentView().getAllAttributes())));
					target = index == r.length - 1
							&& (edge.getTarget() == null || (disconnectable && GraphConstants
									.isDisconnectable(edge.getTarget()
											.getParentView().getAllAttributes())));
					break;
				}
			}
			// Detect hit on label
			if (!isEditing() && graph.isMoveable()
					&& GraphConstants.isMoveable(edge.getAllAttributes())
					&& loc != null && loc.contains(x, y)
					&& !isAddPointEvent(event) && !isRemovePointEvent(event)) {
				label = true;
			}
			// Detect hit on extra labels
			else if (extraLabelLocations != null && !isEditing() && graph.isMoveable() 
					&& GraphConstants.isMoveable(edge.getAllAttributes())
					&& !isAddPointEvent(event) && !isRemovePointEvent(event)) {
					for (int i=0; i<extraLabelLocations.length; i++) {
						if (extraLabelLocations[i] != null && extraLabelLocations[i].contains(x, y)) {
							currentLabel = i;
							break;
						}
					}
			}
			// Remove Point
			if (isRemovePointEvent(event) && currentPoint != null && !source
					&& !target && bendable) {
				edge.removePoint(index);
				mouseReleased(event);
				// Add Point
			} else if (isAddPointEvent(event) && !isEditing() && bendable) {
				int s = graph.getHandleSize();
				Rectangle2D rect = graph.fromScreen(new Rectangle(x - s, y - s,
						2 * s, 2 * s));
				if (edge.intersects(graph.getGraphics(), rect)) {
					Point2D point = graph.fromScreen(graph.snap(new Point(event
							.getPoint())));
					double min = Double.MAX_VALUE, dist = 0;
					for (int i = 0; i < edge.getPointCount() - 1; i++) {
						Point2D p = edge.getPoint(i);
						Point2D p1 = edge.getPoint(i + 1);
						dist = new Line2D.Double(p, p1).ptLineDistSq(point);
						if (dist < min) {
							min = dist;
							index = i + 1;
						}
					}
					edge.addPoint(index, point);
					currentPoint = point;
					reloadPoints(edge);
					paint(graph.getGraphics());
				}
			}
			if (isEditing())
				event.consume();
		}

		public void mouseDragged(MouseEvent event) {
			Point2D p = graph.fromScreen(new Point(event.getPoint()));
			// Move Label
			if (label || currentLabel >= 0) {
				Rectangle2D r = edge.getBounds();
				if (r != null) {
					Point2D p0 = edge.getPoint(0);
					Point2D pe = edge.getPoint(edge.getPointCount() - 1);
					double vx = p.getX() - r.getX();
					if (p0.getX() > pe.getX())
						vx = r.getX() + r.getWidth() - p.getX();
					double vy = p.getY() - r.getY();
					if (p0.getY() > pe.getY())
						vy = r.getY() + r.getHeight() - p.getY();
					double xunit = 1;
					if (r.getWidth() != 0)
						xunit = GraphConstants.PERMILLE / r.getWidth();
					double yunit = 1;
					if (r.getHeight() != 0)
						yunit = GraphConstants.PERMILLE / r.getHeight();
					p = new Point2D.Double(vx * xunit, vy * yunit);
					overlay(graph.getGraphics());
				//	if (label)
				//		edge.setLabelPosition(p);
				//	else
					//	edge.setExtraLabelPosition(currentLabel, p);
					edge.update();
					overlay(graph.getGraphics());
				}
			} else if (isEditing()) {
				// Find Source/Target Port
				if (!((source && snap(true, event.getPoint())) || (target && snap(false, event.getPoint())))) {
					// Else Use Point
					boolean acceptSource = source
							&& (edge.getGraph().getModel().acceptsSource(
									edge.getCell(), null) || edge.getGraph()
									.isPreviewInvalidNullPorts());
					boolean acceptTarget = target
							&& (edge.getGraph().getModel().acceptsTarget(
									edge.getCell(), null) || edge.getGraph()
									.isPreviewInvalidNullPorts());
					if (acceptSource || acceptTarget || !(source || target)) {
						overlay(graph.getGraphics());
						p = graph.fromScreen(graph.snap(new Point(event
								.getPoint())));
						// Constrained movement
						if (isConstrainedMoveEvent(event) && currentIndex >= 0) {
							// Reset Initial Positions
							EdgeView orig = (EdgeView) graph
									.getGraphLayoutCache().getMapping(
											edge.getCell(), false);
							Point2D origPoint = orig.getPoint(currentIndex);
							double totDx = p.getX() - origPoint.getX();
							double totDy = p.getY() - origPoint.getY();
							if (Math.abs(totDx) < Math.abs(totDy))
								p.setLocation(origPoint.getX(), p.getY());
							else
								p.setLocation(p.getX(), origPoint.getY());
						}
						// Do not move into negative space
						p.setLocation(Math.max(0, p.getX()), Math.max(0, p
								.getY()));
						currentPoint.setLocation(p);
						if (source) {
							edge.setPoint(0, p);
							edge.setSource(null);
						} else if (target) {
							edge.setPoint(edge.getPointCount() - 1, p);
							edge.setTarget(null);
						}
						edge.update();
						overlay(graph.getGraphics());
					}
				}
			}
		} // Handle mouse released event

		public void mouseReleased(MouseEvent e) {
			boolean clone = e.isControlDown() && graph.isCloneable();
			GraphModel model = edge.getGraph().getModel();
			Object source = (edge.getSource() != null) ? edge.getSource()
					.getCell() : null;
			Object target = (edge.getTarget() != null) ? edge.getTarget()
					.getCell() : null;
			if (model.acceptsSource(edge.getCell(), source)
					&& model.acceptsTarget(edge.getCell(), target)) {
				ConnectionSet cs = createConnectionSet(edge, edge.getCell(),
						clone);
				Map nested = GraphConstants.createAttributes(
						new CellView[] { edge }, null);
				if (clone) {
					Map cellMap = graph.cloneCells(graph.getDescendants(new Object[]{edge
							.getCell() }));
					nested = GraphConstants.replaceKeys(cellMap, nested);
					cs = cs.clone(cellMap);
					Object[] cells = cellMap.values().toArray();
					graph.getGraphLayoutCache().insert(cells, nested, cs, null, null);
				} else
					graph.getGraphLayoutCache().edit(nested, cs, null, null);
			} else {
				overlay(graph.getGraphics());
			}
			currentPoint = null;
			this.label = false;
			currentLabel = -1;
			currentIndex = -1;
			firstOverlayCall = true;
			e.consume();
		}

		protected ConnectionSet createConnectionSet(EdgeView view, Object edge,
				boolean verbose) {
			ConnectionSet cs = new ConnectionSet();
			Object sourcePort = null, targetPort = null;
			if (view.getSource() != null)
				sourcePort = view.getSource().getCell();
			if (view.getTarget() != null)
				targetPort = view.getTarget().getCell();
			if (verbose
					|| sourcePort != view.getGraph().getModel().getSource(edge))
				cs.connect(edge, sourcePort, true);
			if (verbose
					|| targetPort != view.getGraph().getModel().getTarget(edge))
				cs.connect(edge, targetPort, false);
			return cs;
		}

		// Update control points
		protected void invalidate() {
			EdgeView e = relevantEdge;
			int handlesize = graph.getHandleSize();
			WireRenderer er = (WireRenderer) edge.getRenderer();
			for (int i = 0; i < r.length; i++) {
				Point2D p = graph.toScreen(edge.getAttributes().createPoint(
						e.getPoint(i)));
				r[i].setFrame(p.getX() - handlesize, p.getY() - handlesize,
						2 * handlesize, 2 * handlesize);
				p = null; //graph.toScreen((Point2D) er.getLabelPosition(e).clone());
				Dimension d = null; //er.getLabelSize(e);
				if (p != null && d != null) {
					Point2D s = graph.toScreen(new Point2D.Double(d.width,
							d.height));
					loc.setFrame(p.getX() - s.getX() / 2, p.getY() - s.getY()
							/ 2, s.getX(), s.getY());
				}
			}
			if (extraLabelLocations != null) {
			/*	for (int i=0; i<extraLabelLocations.length; i++) {
					Point2D p = er.getExtraLabelPosition(e, i);
					if (p != null) {
						p = graph.toScreen((Point2D) p.clone());
						Dimension d = er.getExtraLabelSize(e, i);
						if (d != null) {
							Point2D s = graph.toScreen(new Point2D.Double(d.width,
									d.height));
							extraLabelLocations[i].setFrame(p.getX() - s.getX() / 2, p.getY()
									- s.getY() / 2, s.getX(), s.getY());
						}
					}
				}
				*/
			}
		}

	}
}
