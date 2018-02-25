/*
 * Created on May 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.graph;

import net.clackrouter.router.graph.Wire;
import net.clackrouter.topology.core.TopologyModel;

/**
 * Represents a wire connecting two interfaces within JGraph
 * GraphModel representing a VNS Topology.  
 */
public class TopoWire extends Wire {
	
	private TopologyModel mTopoModel;
	private TopologyModel.Link mLink;
	
	public TopoWire(String name, TopologyView view, TopologyModel model, TopologyModel.Link link) { 
		super(name, view); 
		mTopoModel = model;
		mLink = link;
	}
	public TopologyModel.Link getLink() { return mLink; }
	public TopologyModel getTopologyModel() { return mTopoModel; }
}
