package net.clackrouter.topology.graph;



import net.clackrouter.gui.ClackDocument;
import net.clackrouter.topology.core.TopologyModel;

import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphCell;


/**
 * Simple class to represent a virtual host within
 * the Jgraph GraphModel that represents a VNS topology.  
 */
public class TopoHostCell extends DefaultGraphCell
{
    public TopologyModel.Host mHost = null;
    public boolean mIsReservable;
    public ClackDocument mClackDocument;
    public CellView mView = null;

    public TopoHostCell(TopologyModel.Host host, ClackDocument doc){
        super(host);
        mHost = host;
        mIsReservable = host.reservable;
        mClackDocument = doc;
    }
    

};

