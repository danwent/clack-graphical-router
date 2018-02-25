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

package net.clackrouter.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

import net.clackrouter.application.ClackShell;
import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.gui.util.CompDoubleClickHandler;
import net.clackrouter.gui.util.RouterSpeedSlider;
import net.clackrouter.jgraph.pad.GPGraphUI;
import net.clackrouter.jgraph.pad.GPStatusBar;
import net.clackrouter.jgraph.pad.Rule;
import net.clackrouter.jgraph.pad.Touch;
import net.clackrouter.jgraph.pad.UndoHandler;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.graph.ComponentCell;
import net.clackrouter.router.graph.RouterGraph;
import net.clackrouter.router.graph.RouterView;
import net.clackrouter.test.ConnectivityTestWindow;
import net.clackrouter.topology.graph.TopoHostCell;
import net.clackrouter.topology.graph.TopologyView;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphUndoManager;
import org.jgraph.plaf.GraphUI;
import org.jgraph.util.JGraphUtilities;


/**
 * <p>A ClackDocument represents a single router instance in Clack. </p>
 * 
 * <p> A router instance is what is placed in a tab, and includes all different
 * views that can be zoomed between (ie: Network View, Router View, Hierarchical View)
 * and manages which one of these views is currently displayed. </p>
 * 
 * <p>The document also deals with a lot of the listening
 * required on the graph. </p>
 * 
 * <p> This class is a modified version of the GPDocument class from JGraphpad. </p>
 */
public class ClackDocument
extends 	JPanel
implements
GraphSelectionListener,
ComponentListener,
Printable,
GraphModelListener,
PropertyChangeListener,
Observer {
	
	/**
	 */
	protected boolean enableTooltips;
	
	
	/** A reference to the top level
	 *  component
	 */
	protected ClackFramework mFramework;
	
	/** The column rule for the graph
	 */
	protected Rule columnRule;
	
	/** The row rule for the graph
	 */
	protected Rule rowRule;
	
	/** The graphUndoManager manager for the joint graph.
	 *
	 *  @see #graph
	 */
	protected GraphUndoManager graphUndoManager;
	
	/** The graphUndoManager handler for the current document.
	 * Each document has his own handler.
	 * So you can make an graphUndoManager seperate for each document.
	 *
	 */
	protected UndoHandler undoHandler;
	
	/** Action used for fitting the size
	 */
	protected Action fitAction;
	protected GPStatusBar statusbar;	
	/** a reference to the tab holding this document
	 */
	protected ClackTab tab;
	/** the scrollpane containg the current view */
	protected JScrollPane currentScroll;
	/** the topology view for this router */
	protected TopologyView topologyView;

	protected ClackView currentView;
	/** flag telling whether this document is currently visible (ie: is the selected tab) */
	//protected boolean isClackVisible = true;
	
	protected boolean inTopoRouteTableViewMode = false;
	
	private long last_topo_view_draw = 0;
	private static long TOPO_VIEW_DRAW_INTERVAL_MSEC = 200;
	
	
	int topology; 
	boolean modified = false;
	Stack zoomStack;
	String m_doc_title;
	

	private class RouterBundle {
		ClackShell shell;
		protected Touch touch;
		protected Router router;	
		protected RouterView routerView; 	
		protected RouterGraph graph;
	}
	
	RouterBundle currentBundle;
	private Hashtable<String, RouterBundle> m_routers;
	boolean isClackVisible = true;
	RouterSpeedSlider speed_slider;
	ConnectivityTestWindow conn_test_win;
	
	// hack:  used to size the document based on the size of the graphpad
	private static final int GRAPHPAD_BUFFER_WIDTH = 35;
	private static final int GRAPHPAD_BUFFER_HEIGHT = 90;
	
	
	/**
	 * Constructor for ClackDocument.
	 */
	public ClackDocument(ClackFramework graphpad, int topology) {
		super(true);
		this.mFramework = graphpad;
		System.out.println("creating ClackDocument for topology " + topology); 
		setDocumentTitle("topology " + topology);
		m_routers = new Hashtable<String, RouterBundle>();
		undoHandler = new UndoHandler(this);
		graphUndoManager = new GraphUndoManager();
		setBorder(BorderFactory.createEtchedBorder());	
		this.topology = topology;
		zoomStack = new Stack();
		
		// Add this as a listener for undoable edits.

		
		setPreferredSize(new Dimension(graphpad.getWidth()-GRAPHPAD_BUFFER_WIDTH, graphpad.getHeight()-GRAPHPAD_BUFFER_HEIGHT));		
		
		setLayout(new BorderLayout());	
		
		try {
			topologyView = new TopologyView(graphpad, this, topology);
			pushNewView(topologyView);
			topologyView.layoutTopoGraph();
		} catch(Exception e){
			JOptionPane.showMessageDialog(this, "Could not view topology: \n" + e.getMessage(),
					"Topology Viewing Error", JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		}

		speed_slider = new RouterSpeedSlider(this);
		statusbar = new GPStatusBar();
		add(BorderLayout.SOUTH, statusbar);
		add(BorderLayout.EAST, speed_slider);
		addComponentListener(this);		
		
		update();				
	}
	
	public int getTopology() { return topology; }
	
	public void addRouter(Router router){
		RouterBundle rb = new RouterBundle();
		rb.router = router;
		router.setDocument(this);
		rb.graph = new RouterGraph(new DefaultGraphModel());
		rb.graph.getModel().addUndoableEditListener(undoHandler);
		rb.routerView = new RouterView(rb.graph, this);
		rb.touch = new Touch(rb.graph);
		registerListeners(rb.graph);

		setEnableTooltips(true, rb.graph);
			
		rb.graph.setSelectNewCells(true);
		rb.graph.setInvokesStopCellEditing(true);
		rb.graph.setCloneable(true);
		rb.graph.setMarqueeHandler(mFramework.getMarqueeHandler());
		rb.graph.setDisconnectable(false);  
		
		m_routers.put(router.getHost(), rb);
	}
	

	public void zoomIntoRouter(String router_name) {
		RouterBundle rb = m_routers.get(router_name);
		if(rb == null){
			System.err.println("could not find bundle for router name = " + router_name);
			return;
		}
		currentBundle = rb;
		setDocumentTitle(getDocumentTitle() + "  >>  " + router_name);
		rb.router.setPendingError(false); // we're zooming in now, clear error...
		pushNewView(rb.routerView);		

	}
	
	/**
	 * Handles zoom in behavior for any view in this document.
	 * 
	 * <p> If the current view is the topology, we simply zoom into the router view corresponding to the router zoomed in on </p>
	 * 
	 * <p> Otherwise, we get the current graph and using the {@link CompDoubleClickHandler} to 
	 * fine the right behavior for zooming into this component </p>
	 *
	 */
	public void zoomIn() {
		Object cell = getCurrentGraph().getSelectionCell();
		if(currentView == topologyView){
			if(cell instanceof TopoHostCell){
				zoomIntoRouter(((TopoHostCell)cell).mHost.name);
			}
		}else {

			if(cell instanceof ComponentCell){
				ClackComponent comp = ((ComponentCell)cell).getClackComponent();
				CompDoubleClickHandler.handleDoubleClick(this, comp);
			}
			
		}
		System.out.println("trying to zoom into cell: " + cell);
	}

	/**
	 * Returns the graph associated with the {@link ClackView} that the document is currently displaying
	 */
	public JGraph getCurrentGraph() {
		if(currentView != null) return currentView.getGraph();
		return null;
	}
	
	/**
	 * Used to implement zoom in using the zoomStack.
	 * 
	 * <p>Pushes the current view onto the stack and displays the specified view in the document. </p>
	 * @param newView
	 */
	public void pushNewView(ClackView newView){
		if(currentScroll != null){
			this.remove(currentScroll);
			currentView.setIsVisibleView(false);
			zoomStack.push(currentView);
		}
		currentView = newView;
		currentView.setIsVisibleView(true);
		Component graph = newView.getComponent();
		
		currentScroll = new JScrollPane(graph, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		currentScroll.setMinimumSize(new Dimension(300, 300));

		add(BorderLayout.CENTER, currentScroll);

		mFramework.getCurrentActionMap().get("ViewScaleZoomOut").setEnabled(true);
		mFramework.updateUI();
	}
	
	/**
	 * Implements zoom out based on the contents of the zoomStack.
	 * 
	 * <p>Pops the last view off of the stack and puts it as the current view for this document</p>
	 *
	 */
	public void zoomOut() {
		if(zoomStack.size() == 0)return;
		
		if(currentView instanceof RouterView)
			currentBundle = null;
		
		remove(currentScroll);
		currentView.setIsVisibleView(false);
		currentView = (ClackView)zoomStack.pop();
		currentView.setIsVisibleView(true);
		currentScroll = new JScrollPane(currentView.getComponent(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		currentScroll.setMinimumSize(new Dimension(300, 300));

		setDocumentTitle("topology " + topology);
		add(BorderLayout.CENTER, currentScroll);
		if(zoomStack.size() == 0)
			mFramework.getCurrentActionMap().get("ViewScaleZoomOut").setEnabled(false);
		mFramework.updateUI();
	}
	

	/* Add this documents listeners to the specified graph. */
	protected void registerListeners(JGraph graph) {
		graph.getSelectionModel().addGraphSelectionListener(this);
		graph.getModel().addGraphModelListener(this);
		graph.getGraphLayoutCache().addObserver(this);
		graph.addPropertyChangeListener(this);
	}
	
	/* Remove this documents listeners from the specified graph. */
	protected void unregisterListeners(JGraph graph) {
		graph.getSelectionModel().removeGraphSelectionListener(this);
		graph.getModel().removeGraphModelListener(this);
		graph.removePropertyChangeListener(this);
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
		updateTabTitle();
		mFramework.update();
	}
	
	/* Return the title of this document as a string. */
	protected String getDocumentTitle() {
			return m_doc_title;
	}
	
	protected void setDocumentTitle(String s) { 
		m_doc_title = s; 
		updateTabTitle();
	}
	
	/** Sets the attributes of the selected cells. */
	public void setSelectionAttributes(Map map) {
	//	map = new Hashtable(map);
	//	map.remove(GraphConstants.BOUNDS);
	//	map.remove(GraphConstants.POINTS);
	//	JGraphUtilities.editCells(graph, graph.getSelectionCells(), map);
	}
	
	

	/** <p>this is a hack to take care of the bug when the router window
	 * and scrollbar would not properly resize after resizing the outer window. </p>
	 * 
	 * <p> unfortunately this does not work in Java 1.5 </p>
	 */
	public void gpgraphWindowResized(int newWidth, int newHeight){

		int newDocWidth = newWidth-GRAPHPAD_BUFFER_WIDTH;
		int newDocHeight = newHeight-GRAPHPAD_BUFFER_HEIGHT;
		setPreferredSize(new Dimension(newDocWidth, newDocHeight ));	
		this.remove(currentScroll);
		this.add(BorderLayout.CENTER, currentScroll);  //TODO: doesn't work for Java 1.5
	}
	
	
	//-----------------------------------------------------------------
	// Component Listener
	//-----------------------------------------------------------------
	public void setResizeAction(AbstractAction e) {
		fitAction = e;
	}
	
	public void componentHidden(ComponentEvent e) {
	}
	
	public void componentMoved(ComponentEvent e) {
	}
	
	public void componentResized(ComponentEvent e) {
		if (fitAction != null)
			fitAction.actionPerformed(null);
	}
	
	public void componentShown(ComponentEvent e) {
		componentResized(e);
	}
	
	/*public void setScale(double scale) {
		scale = Math.max(Math.min(scale, 16), .01);
		graph.setScale(scale);
		componentResized(null);
	}*/
	
	//----------------------------------------------------------------------
	// Printable
	//----------------------------------------------------------------------
	
	/** not from Printable interface, but related
	 */
	public void updatePageFormat() {
/*		PageFormat f = graph.getPageFormat();
		columnRule.setActiveOffset((int) (f.getImageableX()));
		rowRule.setActiveOffset((int) (f.getImageableY()));
		columnRule.setActiveLength((int) (f.getImageableWidth()));
		rowRule.setActiveLength((int) (f.getImageableHeight()));
		if(graph.isPageVisible()) {
			int w = (int) (f.getWidth());
			int h = (int) (f.getHeight());
			graph.setMinimumSize(new Dimension(w + 5, h + 5));
		}
		invalidate();
		// Execute fitAction...
		componentResized(null);
		graph.repaint();
		*/
	}
	
	/**
	 * Prints the router graph
	 */
	public int print(Graphics g, PageFormat pF, int page) {
	/*	int pw = (int) pF.getImageableWidth();
		int ph = (int) pF.getImageableHeight();
		int cols = (int) (graph.getWidth() / pw) + 1;
		int rows = (int) (graph.getHeight() / ph) + 1;
		int pageCount = cols * rows;
		if (page >= pageCount)
			return NO_SUCH_PAGE;
		int col = page % cols;
		int row = page % rows;
		g.translate(-col*pw, -row*ph);
		g.setClip(col*pw, row*ph, pw, ph);
		graph.paint(g);
		g.translate(col*pw, row*ph);
		return PAGE_EXISTS;
		*/
		return NO_SUCH_PAGE;
	}
	
	//
	// Listeners
	//
	
	// PropertyChangeListener
	public void propertyChange(PropertyChangeEvent evt) {
		if (mFramework != null)
			update();
	}
	
	// GraphSelectionListener
	public void valueChanged(GraphSelectionEvent e) {
		if(currentBundle == null) return;
		
		if (!currentBundle.graph.isSelectionEmpty())
			currentBundle.touch.setDamper(0);
		update();
	}
	
	// View Observer
	public void update(Observable obs, Object arg) {
		if(currentBundle == null) return;
		
		modified = true;
		currentBundle.touch.resetDamper();
		update();
	}
	
	/**
	 * Important method that responds to removals of components or wires from the graph
	 *  by informing the underlying router and components
	 */
	public void graphChanged(GraphModelEvent e) {
		if(currentBundle == null)return;
		
		modified = true;
		currentBundle.touch.resetDamper();
		update();
		GraphModelEvent.GraphModelChange change = e.getChange();
		Object[] removed = change.getRemoved();
		if(removed != null) {
			for(int i = 0; i < removed.length; i++){
				mFramework.getClackGraphHelper().cellRemovedFromModel((DefaultGraphCell)removed[i]);
			}
		}
	}
	
	/** update this graphpad and tab title */
	protected void update() {
		updateTabTitle();
		mFramework.update();
	}
	
	/**
	 * Returns the graphUndoManager.
	 * @return GraphUndoManager
	 */
	public GraphUndoManager getGraphUndoManager() {
		return graphUndoManager;
	}
	
	/**
	 * Returns the graphpad.
	 * @return GPGraphpad
	 */
	public ClackFramework getFramework() {	return mFramework;}
	
	public boolean isEnableTooltips() {return enableTooltips;}
	
	/**
	 * Sets the enableTooltips.
	 * @param enableTooltips The enableTooltips to set
	 */
	public void setEnableTooltips(boolean enableTooltips, RouterGraph graph) {
		this.enableTooltips = enableTooltips;
		
		if (this.enableTooltips)
			ToolTipManager.sharedInstance().registerComponent(graph);
		else
			ToolTipManager.sharedInstance().unregisterComponent(graph);
	}
	
	public void setIsClackVisible(boolean b) { isClackVisible = b; }
	public boolean isClackVisible() { return isClackVisible; }
	public TopologyView getTopologyView() { return topologyView; }
	
	
	public RouterView getCurrentRouterView() { 
		if(currentBundle == null) return null;
		return currentBundle.routerView; 
	}
	
	public Router getCurrentRouter() {
		if(currentBundle == null) return null;
		return currentBundle.router;
	}

	public RouterGraph getCurrentRouterGraph() {	
		if(currentBundle == null) return null;
		return currentBundle.graph;
	}	
	
	public Router getRouter(String name){ 
		RouterBundle rb = m_routers.get(name);
		if(rb != null) return rb.router;
		return null;
	}
	
	public RouterGraph getRouterGraph(String name){ 
		RouterBundle rb = m_routers.get(name);
		if(rb != null) return rb.graph;
		return null;
	}
	
	public RouterView getRouterView(String name){ 
		RouterBundle rb = m_routers.get(name);
		if(rb != null) return rb.routerView;
		return null;
	}
	
	public ClackTab getGPTab() {return tab;	}
	public void setTab(ClackTab tab) {	this.tab = tab; }
	
	public boolean inTopoRouteTableViewMode() { return inTopoRouteTableViewMode; }
	
	public void toogleTopoRouteTableViewMode(){
		inTopoRouteTableViewMode = !inTopoRouteTableViewMode;
		redrawTopoView();
	}
		

	
	public void redrawTopoView() {
		// need to redraw
		if(!topologyView.isVisibleView()) {
			return ;  // only redraw if we're currently being shown
		}
		
	//	long cur_time  = System.currentTimeMillis();
	//	if(cur_time - last_topo_view_draw < TOPO_VIEW_DRAW_INTERVAL_MSEC){
	//		return;
	//	}
	//	last_topo_view_draw = cur_time;
		
		Graphics2D g2 = (Graphics2D)topologyView.getGraph().getGraphics();

    	if(g2 == null) 	return;

    	double scale = topologyView.getGraph().getScale();	
		g2.scale(scale, scale);
		GraphUI ui = topologyView.getGraph().getUI();
		Object[] roots = topologyView.getGraph().getRoots();
		for(int i = 0; i < roots.length; i++) {
			if(roots[i] instanceof TopoHostCell){
				TopoHostCell cell = (TopoHostCell)roots[i];
				if(cell.mView != null)
					ui.paintCell(g2, cell.mView, cell.mView.getBounds(), false);
			}
		}
	}
	
	public ConnectivityTestWindow getConnectivityTestWin() { return conn_test_win; }
	public void setConnectivityTestWin(ConnectivityTestWindow win) {
		conn_test_win = win;
	}
	

	protected void updateTabTitle() {
		if (tab != null) {
			tab.setTitle(getDocumentTitle());
		}	
	}
	


	
}
