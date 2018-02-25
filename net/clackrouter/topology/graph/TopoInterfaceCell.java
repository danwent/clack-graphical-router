package net.clackrouter.topology.graph;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.Map;

import net.clackrouter.topology.core.TopologyModel;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

/**
 * 
 * Subclass of DefaultPort to implement a component port within a JGraph GraphModel
 * representing an VNS topology.  
 */
public class TopoInterfaceCell extends DefaultPort
{
    public TopologyModel.Interface mInterface = null;
    public double mPosition;
    public static double THRESHOLD = 1;
    public TopoInterfaceView mView;
    
    public TopoInterfaceCell(TopologyModel.Interface iface)
    {
        super(iface.name);
        mInterface = iface;
    }
    
    public void setXYPosition(double x_fraction, double y_fraction){
        Map map = new Hashtable();
 
        GraphConstants.setBounds(map, new Rectangle(0,0,100,100));
        GraphConstants.setBounds(map, new Rectangle(0,0,100,100));
        GraphConstants.setOffset(map, new Point((int) (GraphConstants.PERMILLE * x_fraction),
                   (int) (GraphConstants.PERMILLE * y_fraction)));

        setAttributes(new AttributeMap(map));   	
    }

 

    public TopologyModel.Interface getInterface() { return mInterface; }
   
} // -- VPort
