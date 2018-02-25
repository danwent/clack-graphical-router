
package net.clackrouter.topology.core;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ToolTipManager;

import net.clackrouter.component.base.Interface;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.jgraph.pad.GPAttributeMap;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.graph.Wire;
import net.clackrouter.router.graph.WireView;
import net.clackrouter.topology.graph.TopoInterfaceCell;
import net.clackrouter.topology.graph.TopoInterfaceView;
import net.clackrouter.topology.graph.TopoPhysicalHostView;
import net.clackrouter.topology.graph.TopoHostCell;
import net.clackrouter.topology.graph.TopoVirtualHostView;
import net.clackrouter.topology.graph.TopoWire;
import net.clackrouter.topology.graph.TopologyView;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;




/**
 * Main graph class to resresent a topology, which has hosts as different nodes
 * and ethernet links as edges.  
 */
public class TopoGraph extends JGraph implements GraphModelListener {

    public static int IFACE_LIST_OFFSET = 10;
    private ClackDocument mClackDocument;
    private Hashtable mOldLocations = new Hashtable(); // hack to let us see when hosts are moved
    	// so we can update the port locations.  
	
	public TopoGraph(GraphModel model, ClackDocument doc){
		super(model);
		model.addGraphModelListener(this);
	//	this.setSelectionEnabled(false);
		setPortsVisible(true);
		ToolTipManager.sharedInstance().registerComponent(this);
		ToolTipManager.sharedInstance().setInitialDelay(400);
		mClackDocument = doc;
	}

	protected PortView createPortView(JGraph graph, CellMapper mapper, Object cell) {
		if(cell instanceof TopoInterfaceCell){
			return new TopoInterfaceView(cell, graph, mapper);
		}
		
		return super.createPortView(graph, mapper, cell);
	}
	
	protected EdgeView createEdgeView(JGraph graph, CellMapper cm, Object v){
		if(v instanceof TopoWire){
			return new WireView((Wire)v, this, cm);
		}
		return super.createEdgeView(graph, cm, v);
	}
	
	/**
	 * Creates a view to represent either a physical or virtual host
	 * in the topology ( if mIsReservable is true, the host is virtual). 
	 *
	 * @return the default <code>GraphView</code>
	 */
	protected VertexView createVertexView(JGraph graph,CellMapper cm, Object v ) {
		
		
		if(v instanceof TopoHostCell){
			TopoHostCell cell = (TopoHostCell) v;
			if(cell.mIsReservable) 
				return new TopoVirtualHostView(cell, this, cm);
			else 
				return new TopoPhysicalHostView(cell, this, cm);
				
		}
		// shouldn't happen?
		System.err.println("createVertexView received non TopoHostCell cell");
		return super.createVertexView(graph, cm, v);
	}
	
	public void insertHostIntoGraph(TopologyModel.Host host, Hashtable ifaceMap, Rectangle2D bounds){
		Map viewMap = new Hashtable();	
		AttributeMap map = new AttributeMap();
		
		if(bounds == null)
			bounds = new Rectangle(20, 20, 140, 60 + IFACE_LIST_OFFSET * host.interfaces.size());
		
		GraphConstants.setBounds(map, bounds);
		GraphConstants.setOpaque(map, false);
		
		List toInsert = new LinkedList();
		
		DefaultGraphCell cell = new TopoHostCell(host, mClackDocument);

		cell.setAttributes(new GPAttributeMap()); // install custom attribute map
		viewMap.put(cell, map);
		toInsert.add(cell);
		
		// Create Ports
		
		AttributeMap mapPorts = new AttributeMap();
		
		GraphConstants.setBounds(mapPorts, new Rectangle(0, 0, 25, 25));
		GraphConstants.setOpaque(mapPorts, false);
		
		int u = GraphConstants.PERMILLE;
		for(int i = 0; i < host.interfaces.size(); i++){
			TopologyModel.Interface iface = (TopologyModel.Interface)host.interfaces.get(i);
			TopoInterfaceCell port = new TopoInterfaceCell(iface);
			cell.add(port);
			toInsert.add(port);
			ifaceMap.put(iface.id, port);
			//cell.setAttributes(new GPAttributeMap());
			viewMap.put(port, mapPorts);
		}
		
		getModel().insert(
				toInsert.toArray(),
				viewMap,
				null,
				null,
				null);
		
		if(host.reservable) {
			this.setSelectionCell(cell); 
		}
	}
	
	public TopoWire connectInterfaces(TopologyView view, TopologyModel model, TopologyModel.Link link, Hashtable ifaceMap){
		AttributeMap map = new AttributeMap();

		DefaultPort port1 = (DefaultPort) ifaceMap.get(link.iface1.id);
		DefaultPort port2 = (DefaultPort) ifaceMap.get(link.iface2.id);
	
		CellView[] views = getGraphLayoutCache().getMapping(new DefaultGraphCell[] { (DefaultGraphCell)port1, (DefaultGraphCell)port2 }, true );

		PortView view1 = (PortView)views[0];
    	PortView view2 = (PortView)views[1];
			
		Point2D temp1 = view1.getLocation(null);
		Point p = new Point((int) temp1.getX(), (int) temp1.getY());
		Point2D temp2 = view2.getLocation(null);
		Point p2 = new Point((int) temp2.getX(), (int) temp2.getY());
		
		ArrayList list = new ArrayList();
		list.add(p);
		list.add(p2);
		GraphConstants.setPoints(map, list);
		GraphConstants.setEndFill(map, false);
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_NONE);
		GraphConstants.setLineWidth(map, (float)2.5);
		
		Map viewMap = new Hashtable();

		TopoWire edge = new TopoWire("", view, model, link);
		link.addListener(edge);
		edge.setAttributes(new GPAttributeMap()); // install custom attribute map

		viewMap.put(edge, map);
		Object[] insert = new Object[] { edge};
		ConnectionSet cs = new ConnectionSet();
		
		//System.out.println("Connecting " + source +  " to " + target);
		cs.connect(edge, view1.getCell(), true);
		cs.connect(edge, view2.getCell(), false);
		getModel().insert(
			insert,
			viewMap,
			cs,
			null,
			null);	
		
		return edge;
	}
	
	public void placePorts(){
		Object[] roots = getRoots();
		Hashtable already_done = new Hashtable();
		
		for(int i = 0; i < roots.length; i++){
			// note this method looks at both virtual and physical hosts  
			if(roots[i] instanceof TopoHostCell){
				TopoHostCell cell = (TopoHostCell)roots[i];
				Rectangle2D bounds = GraphConstants.getBounds(cell.getAttributes());
				mOldLocations.put(cell.mHost.name, bounds);
				
				List children = cell.getChildren();
				for(int j = 0; j < children.size(); j++){
					TopoInterfaceCell iface = (TopoInterfaceCell)children.get(j);
					Object[] edges = iface.getEdges().toArray();
					for(int k = 0; k < edges.length; k++){
						Edge e = (Edge)edges[k];
						if(already_done.get(e) != null) continue;
						
						already_done.put(e, e); // make note that we have done the interfaces on this edge
						TopoInterfaceCell sourcePort = (TopoInterfaceCell)e.getSource();
						TopoInterfaceCell targetPort = (TopoInterfaceCell)e.getTarget();
						placeSinglePort(sourcePort, targetPort);
						placeSinglePort(targetPort, sourcePort);
	
					}
				}
			}
		}
	
		Object[] all = getDescendantList(getRoots());
		getModel().toFront(all); // important, needed to redraw ports
	}
	
	private void placeSinglePort(TopoInterfaceCell port, TopoInterfaceCell neighbor){
	
		TopoHostCell cell1 = (TopoHostCell) port.getParent();
		TopoHostCell cell2 = (TopoHostCell) neighbor.getParent();
		Rectangle2D bounds1 = (Rectangle2D)cell1.getAttributes().get(GraphConstants.BOUNDS);
		Rectangle2D bounds2 = (Rectangle2D) cell2.getAttributes().get(GraphConstants.BOUNDS);
		
		Point2D.Double topleft = new Point2D.Double(bounds1.getX(), bounds1.getY());
		Point2D.Double topright = new Point2D.Double(bounds1.getX() + bounds1.getWidth(), bounds1.getY());
		Point2D.Double bottomleft = new Point2D.Double(bounds1.getX(), bounds1.getY() + bounds1.getHeight());
		Point2D.Double bottomright = new Point2D.Double(bounds1.getX() + bounds1.getWidth(), bounds1.getY() + bounds1.getHeight());
		ArrayList lines = new ArrayList();
		lines.add(new Line2D.Double(topleft, topright));
		lines.add(new Line2D.Double(bottomleft, bottomright));
		lines.add(new Line2D.Double(bottomleft, topleft));
		lines.add(new Line2D.Double(topright, bottomright));
		
		Line2D.Double centerToCenter = new Line2D.Double(bounds1.getCenterX(), bounds1.getCenterY(),
				bounds2.getCenterX(), bounds2.getCenterY());
		
		for(int i = 0; i < lines.size(); i++){
			Line2D.Double l = (Line2D.Double)lines.get(i);
			Point2D.Double isec = getIntersectionPoint(l, centerToCenter);
			if(isec != null){	
				Point2D.Double scaled = new Point2D.Double(isec.getX() - bounds1.getX(), isec.getY() - bounds1.getY());
				port.setXYPosition(scaled.getX() / bounds1.getWidth(), scaled.getY() / bounds1.getHeight());
				return;
			}		
		}
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
	
	public String getToolTipText(MouseEvent event) {

		if (event != null) {
			Object port = this.getPortForLocation(event.getX(), event.getY());
			if(port != null)
				return getToolTipForCell(port);
			Object cell = getFirstCellForLocation(event.getX(), event.getY());
			if (cell != null) {
				return getToolTipForCell(cell);
			}
		}
		return null;
	}
	

	protected String getToolTipForCell(Object cell) {
		
		String s = "<html>";
		
		if (cell instanceof TopoInterfaceCell) {
			TopoInterfaceCell tic = (TopoInterfaceCell)cell; 
			if(tic.getParent() instanceof TopoHostCell) {
				TopoHostCell vhost_cell = (TopoHostCell)tic.getParent();
				ClackDocument doc = vhost_cell.mClackDocument;
				if(doc == null) return "";

			
				Router r = doc.getRouter(vhost_cell.mHost.name);
				
				if(r == null) 
					return "<html><strong> " + tic.mInterface.name + "</strong></html>";
				
			
				Interface iface = r.getInterfaceByName(tic.mInterface.name);
				if(iface != null){
					try {
						InetAddress net = NetUtils.applyNetMask(iface.getIPAddress(), iface.getIPSubnet());
						String ip_and_mask = NetUtils.NetworkAndMaskToString(net, iface.getIPSubnet());
						s += "<strong>" + tic.mInterface.name + ": "  + iface.getIPAddress().getHostAddress() + 
							"<br>" + ip_and_mask + "</strong>";
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}

		} else if (cell instanceof TopoHostCell) {
			
			TopoHostCell host_cell = (TopoHostCell)cell;
			
			s += "<strong>" + host_cell.mHost.name + "</strong>";
			if(host_cell.mHost.reservable && host_cell.mClackDocument != null) { 
				// virtual host, get current info (it may have been updated since loading topo)  
				Router r = host_cell.mClackDocument.getRouter(host_cell.mHost.name);
				if(r == null){
					System.err.println("Error: couldn't find router for: " + host_cell.mHost.name);
					return ""; 
				}
				Interface[] all_ifaces = r.getInputInterfaces();

				for(int i = 0; i < all_ifaces.length; i++){
					Interface iface =  all_ifaces[i];        
					if(iface.getIPAddress() != null)
						s += "<br>" + iface.getDeviceName()	+": "+ iface.getIPAddress().getHostAddress();
				}
			
			}else {
				// physical host (data unchanged since loading topo) 
				for(int i = 0; i < host_cell.mHost.interfaces.size(); i++){
					TopologyModel.Interface iface = 
						(TopologyModel.Interface)host_cell.mHost.interfaces.get(i);
						s += "<br>" + iface.name;
						if(iface.address != null && host_cell.mClackDocument != null)
							s += ": " + iface.address.getHostAddress();
				}
			}
		} else if(cell instanceof Edge){
			Edge edge = (Edge) cell;
			Object source = graphModel.getSource(edge);
			if (source != null) {

				TopologyModel.Interface sourceIface = ((TopoInterfaceCell)source).mInterface;
				TopoHostCell parent = (TopoHostCell)graphModel.getParent(source);
				ClackDocument doc = ((TopoHostCell)parent).mHost.document;
				if(doc == null || doc.getRouter(parent.mHost.name) != null)
					s += "<strong>" + sourceIface.name + "</strong> <br>";
				else {
					Router sr = doc.getRouter(parent.mHost.name);
					if(sourceIface.address != null)
						s = s + "<strong>" + parent.mHost.name + ":</strong> "  + sourceIface.name + "(" + 
							sr.getInterfaceByName(sourceIface.name).getIPAddress().getHostAddress() + ")<br>";
				}
			}
			Object target = graphModel.getTarget(edge);
			if (target != null) {
				TopologyModel.Interface targetIface = ((TopoInterfaceCell)target).mInterface;
				TopoHostCell parent = (TopoHostCell)graphModel.getParent(target);
				ClackDocument doc = ((TopoHostCell)parent).mHost.document;
				if(doc == null || doc.getRouter(parent.mHost.name) != null)
					s += "<strong>" + targetIface.name + "</strong> <br>";
				else {
					Router tr = doc.getRouter(parent.mHost.name);
					if(targetIface.address != null)
						s = s + "<strong>" + parent.mHost.name + ":</strong> "  + targetIface.name
						+ "(" + tr.getInterfaceByName(targetIface.name).getIPAddress().getHostAddress() + ")";
				}
			}
		}
		return s + "</html>";
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
	
	public boolean isVertex(Object object) {
		if (!(object instanceof Port) && !(object instanceof Edge))
			return !isGroup(object) && object != null;
		return false;
	}
	
	public boolean isGroup(Object cell) {
		// Map the Cell to its View
		CellView view = getGraphLayoutCache().getMapping(cell, false);
		if (view != null)
			return !view.isLeaf();
		return false;
	}
	
	// dw: ripped from: http://forum.java.sun.com/thread.jspa?threadID=179628&messageID=747497
	  public Point2D.Double getIntersectionPoint(Line2D.Double line1, Line2D.Double line2) {

	    if (! line1.intersectsLine(line2) ) return null;
	      double px = line1.getX1(),
	            py = line1.getY1(),
	            rx = line1.getX2()-px,
	            ry = line1.getY2()-py;
	      double qx = line2.getX1(),
	            qy = line2.getY1(),
	            sx = line2.getX2()-qx,
	            sy = line2.getY2()-qy;
	 
	      double det = sx*ry - sy*rx;
	      if (det == 0) {
	        return null;
	      } else {
	        double z = (sx*(qy-py)+sy*(px-qx))/det;
	        if (z==0 ||  z==1) return null;  // intersection at end point!
	        return new Point2D.Double((px+z*rx), (py+z*ry));
	      }
	 } // end intersection line-line
	  
	  
	  // this is a hack to get the ports to place themselves everytime we move a Host in the 
	  // topo view.  we need to see if a host is actually moved, since we hit an infinite loop
	  // placing ports on any graph change, since moving the ports changes the graph. 
	  public void graphChanged(GraphModelEvent event){
	  		GraphModelChange change = event.getChange();
	  		Object[] cells = change.getChanged();
	  		
	  		for(int i = 0; i < cells.length; i++) {
	  			if(cells[i] == null || !(cells[i] instanceof TopoHostCell))continue;
	  			TopoHostCell tvhcell = (TopoHostCell)cells[i];
	  			Rectangle2D current = GraphConstants.getBounds(tvhcell.getAttributes());
	  			Rectangle2D old = (Rectangle2D)mOldLocations.get(tvhcell.mHost.name);
	  			if(old == null || current == null) continue;
	  			double x_diff = Math.abs(current.getX() - old.getX());
	  			double y_diff = Math.abs(current.getY() - old.getY());
	  			if(x_diff > 10 || y_diff > 10){
	  				placePorts();
	  				return;
	  			}
	  			
	  		}
	
	  }

}
