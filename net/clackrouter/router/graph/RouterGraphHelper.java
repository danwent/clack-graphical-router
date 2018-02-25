/*
 * Created on Feb 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.router.graph;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.util.PortConnectionDialog;
import net.clackrouter.jgraph.pad.GPAttributeMap;
import net.clackrouter.router.core.Router;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.PortView;
import org.jgraph.util.JGraphParallelEdgeRouter;
import org.jgraph.util.JGraphUtilities;


/**
 * Main GUI class to handle the JGraph end of components being added, removed, and connected within Clack.
 * 
 * <p>Every ClackGraphpad has a reference to a ClackGraphHelper </p>
 */
public class RouterGraphHelper {
	
	private ClackFramework graphpad;
	
	public RouterGraphHelper(ClackFramework gp){
		graphpad = gp;
	}
	
	/**
	 * Adds a ComponentCell to the RouterGraph representing the router
	 * that was passed to the ClackComponent when it was created. 
	 * 
	 * <p> This keeps the graph in synch with the router when a component is
	 * added.  Components are added with the specified bounds, or at the location
	 * of the last click if no bounds are provided. 
	 * 
	 * @param component - component added to the router
	 * @param bounds - location in RouterGraph to add componentCell
	 * @return the created cell
	 */
	public ComponentCell addRouterComponentCell(Router router,
			ClackComponent component,
			Rectangle2D bounds) {
		
		if(bounds == null){
			Point p = graphpad.getMarqueeHandler().getLastClick();
			bounds = new Rectangle(p.x, p.y, 60, 25);
		}
		
        int strwidth = graphpad.getGraphics().getFontMetrics().stringWidth(component.getTypeName());
    	if(bounds.getWidth() < strwidth + 35){ // make sure bounds are large enough for text
    		bounds = new Rectangle((int)bounds.getX(), (int)bounds.getY(), strwidth + 35, (int)bounds.getHeight());   
    	}
		
		Map viewMap = new Hashtable();
		RouterGraph graph = (RouterGraph) router.getDocument().getRouterGraph(router.getHost());
		GraphModel model = graph.getModel();
		
		AttributeMap map = new AttributeMap();
		
		GraphConstants.setBounds(map, bounds);
		GraphConstants.setOpaque(map, false);
		
		List toInsert = new LinkedList();
		
		ComponentCell cell = new ComponentCell(component, router.getDocument().getRouterView(router.getHost()));
		
	
		cell.setAttributes(new GPAttributeMap()); // install custom attribute map
		viewMap.put(cell, map);
		toInsert.add(cell);
		
		// Create Ports
		int u = GraphConstants.PERMILLE;
		//DefaultPort port;
		Object port;
		
		// Floating Center Port (Child 0 is Default)
		port = new DefaultPort("Center");
		((DefaultMutableTreeNode)cell).add((DefaultPort)port);
		toInsert.add(port);
		
		model.insert(
				toInsert.toArray(),
				viewMap,
				null,
				null,
				null);
		return cell;
	}
	
	/**
	 * Adds a RouterWire to the graph between two component ports that have been connected.
	 * 
	 * @param sourceCell source component cell
	 * @param src_port_num source port number
	 * @param targetCell target component cell
	 * @param target_port_num target port number
	 */
	public void addRouterWire(ComponentCell sourceCell, int src_port_num,
			ComponentCell targetCell, int target_port_num){
		
		ClackComponent src = sourceCell.getClackComponent();
		ClackComponent tgt = targetCell.getClackComponent();
		
		Router r = src.getRouter();
	
		RouterGraph graph = (RouterGraph)r.getDocument().getRouterGraph(r.getHost());
		
    	// there must be a better way to do this, i just don't know how
		// loop through all PortView's to get the view associated with these ports
    	PortView[] allPorts = graph.getGraphLayoutCache().getPorts(); 
    	PortView source_port_view = null, target_port_view = null;
    	for(int i = 0; i < allPorts.length; i++){
    		ComponentCell parent = (ComponentCell)((DefaultGraphCell)allPorts[i].getCell()).getParent();
    		if(parent == sourceCell) source_port_view = allPorts[i];
    		if(parent == targetCell) target_port_view = allPorts[i];
    	}
    	
    	// get port locations as initial edge endpoints
		Point2D temp1 = source_port_view.getLocation(null);
		Point p = new Point((int) temp1.getX(), (int) temp1.getY());
		Point2D temp2 = target_port_view.getLocation(null);
		Point p2 = new Point((int) temp2.getX(), (int) temp2.getY());
		
		// set edge attribute map
		ArrayList list = new ArrayList();
		list.add(p);
		list.add(p2);
		AttributeMap map = new AttributeMap();
		GraphConstants.setPoints(map, list);
		GraphConstants.setEndFill(map, true);
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_CLASSIC);
		
		
		Map viewMap = new Hashtable();
		
		// code below handles multiple edges between a pair of vertices
		// Count directed parallel edges only
		Object[] edges = JGraphUtilities.getEdgesBetweenPorts(graph,
				source_port_view.getCell(), target_port_view.getCell()); // dw: this was passed a boolean "true"
		if (edges != null && edges.length > 0) {
			Map tmpMap = new Hashtable();
			GraphConstants.setRouting(tmpMap, JGraphParallelEdgeRouter.sharedInstance);
			GraphConstants.setLineStyle(tmpMap, GraphConstants.STYLE_BEZIER);
			for (int i=0; i<edges.length; i++) {
				EdgeView view = (EdgeView) graph.getGraphLayoutCache().getMapping(edges[i], false);
				if (view != null && GraphConstants.getRouting(view.getAllAttributes()) == null &&
						view.getPointCount() < 3)
				viewMap.put(edges[i], tmpMap);
			}
			GraphConstants.setRouting(map, JGraphParallelEdgeRouter.sharedInstance);
			GraphConstants.setLineStyle(map, GraphConstants.STYLE_BEZIER);
		}

		ClackPort source_to_connect = src.getPort(src_port_num);
		
		RouterWire wire = new RouterWire("wire " + RouterWire.getUniqueCount(), r.getDocument().getRouterView(r.getHost()),
				src, src_port_num, tgt, target_port_num );
		wire.setAttributes(new GPAttributeMap()); // install custom attribute map
		
		viewMap.put(wire, map);
		Object[] insert = new Object[] { wire };
		ConnectionSet cs = new ConnectionSet();
		
		source_to_connect.setListeningWire(wire); // this is for wire-highlighting
		
		cs.connect(wire, source_port_view.getCell(), true);
		cs.connect(wire, target_port_view.getCell(), false);
		graph.getModel().insert(
			insert,
			viewMap,
			cs,
			null,
			null);	
	}
	
	/**
	 * Show connection dialog to connect two ports and attempt to make the connection
	 * @param source_port
	 * @param target_port
	 */
	public void attemptPortConnection(DefaultPort source_port, DefaultPort target_port){
		
		ComponentCell source_cell = (ComponentCell) source_port.getParent();
		ClackComponent source = source_cell.getClackComponent();
		ComponentCell target_cell = (ComponentCell) target_port.getParent();
		ClackComponent target = target_cell.getClackComponent();
		
		// show connection dialog to let user pick which ports to try and connect
		PortConnectionDialog connectDialog = new PortConnectionDialog(graphpad.getFrame(),  source, target);
		
        if(connectDialog.isValid()){
        	int source_port_num = connectDialog.getSourcePort();
        	int target_port_num = connectDialog.getTargetPort();
        	
			String error = ClackPort.connectPorts(source_cell.getClackComponent().getPort(source_port_num),
					target_cell.getClackComponent().getPort(target_port_num));
			if(error == null){
				// connection is valid
	        	graphpad.getClackGraphHelper().addRouterWire(source_cell ,source_port_num , target_cell, target_port_num);
			}else {
				// connection is invalid
				RouterGraphHelper.showConnectionErrorDialog(graphpad, error);
			}
        }
	
        graphpad.updateUI(); // refreshes the screen
	}
	
	
	/**
	 * Called when {@link ClackDocument#graphChanged(GraphModelEvent)} recognizes that something
	 * has been removed from the RouterGraph.
	 * 
	 * <p>  If a RouterWire has been removed, we disconnect the underlying ports </p>
	 * 
	 * <p> If a component has been removed, we delete all edges connected to this component </p> 
	 * @param cell - cell that has been removed from the graph
	 */
	public void cellRemovedFromModel(DefaultGraphCell cell){
		if(cell instanceof RouterWire){
			// if a wire is removed, we should disconnect underlying connectivity
			RouterWire wire = (RouterWire) cell;
			ClackPort source_port = wire.getSourceComponent().getPort(wire.getSourcePortNum());
			ClackPort target_port = wire.getTargetComponent().getPort(wire.getTargetPortNum());
			ClackPort.disconnectPorts(source_port, target_port);

		}
		if(cell instanceof ComponentCell){
			// remove component from router, remove any connected edges
			ComponentCell compCell =  (ComponentCell)cell;
			ClackComponent comp = compCell.getClackComponent();
		
			Router r = comp.getRouter();
			r.removeComponent(comp);
			RouterGraph graph = (RouterGraph)r.getDocument().getRouterGraph(r.getHost());
			Object[] edges = graph.getEdges(graph.getAll());
			ArrayList edgesToRemove = new ArrayList();
			for(int j = 0; j < edges.length; j++){
				RouterWire w = (RouterWire)edges[j];
				if(w.sourceComponent == comp || w.targetComponent == comp){
					edgesToRemove.add(w);
				}
			}
			if(edgesToRemove.size() > 0)
				graph.getModel().remove(edgesToRemove.toArray());
		}
	}
	
	public static void showConnectionErrorDialog(Component comp, String msg){
		JOptionPane.showMessageDialog(comp,
			    msg,
			    "Port Connection Error",
			    JOptionPane.ERROR_MESSAGE);
	}
	

}
