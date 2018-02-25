
package net.clackrouter.topology.create;

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
import net.clackrouter.topology.core.TopoGraph;
import net.clackrouter.topology.core.TopologyModel;
import net.clackrouter.topology.create.*;
import net.clackrouter.topology.graph.*;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.*;




/**
 * Main graph class to resresent a topology, which has hosts as different nodes
 * and ethernet links as edges.  
 */
public class CreateTopologyGraph extends TopoGraph {

    public static int IFACE_LIST_OFFSET = 10;

    private Hashtable mOldLocations = new Hashtable(); // hack to let us see when hosts are moved
    	// so we can update the port locations.  
	
	public CreateTopologyGraph(GraphModel model){
		super(model, null);
		model.addGraphModelListener(this);
	//	this.setSelectionEnabled(false);
		setPortsVisible(true);
		ToolTipManager.sharedInstance().registerComponent(this);
		ToolTipManager.sharedInstance().setInitialDelay(400);
	
	}
	
	public static CreateTopologyGraph initialGraph() {
		CreateTopologyGraph graph = new CreateTopologyGraph(new DefaultGraphModel());
		return graph;
	}
	
	// override default from TopoGraph to instantiate a CreateTopoHostView
	protected VertexView createVertexView(JGraph graph,CellMapper cm, Object v ) {
		
		if(v instanceof TopoHostCell)
				return new CreateTopoHostView((TopoHostCell)v, this, cm);

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
		
		DefaultGraphCell cell  = new TopoHostCell(host,null);

		cell.setAttributes(new GPAttributeMap()); // install custom attribute map
		viewMap.put(cell, map);
		toInsert.add(cell);
		
		// Create Ports
		
		AttributeMap mapPorts = new AttributeMap();
		
		GraphConstants.setBounds(mapPorts, new Rectangle(0, 0, 25, 25));
		GraphConstants.setOpaque(mapPorts, false);
		
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


}
