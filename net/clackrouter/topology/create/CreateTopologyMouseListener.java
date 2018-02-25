package net.clackrouter.topology.create;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import net.clackrouter.topology.core.TopologyModel;
import net.clackrouter.topology.graph.TopoHostCell;
import net.clackrouter.topology.graph.TopoWire;
import org.jgraph.graph.CellView;


public class CreateTopologyMouseListener  extends MouseAdapter {
	
	CreateTopologyGraph mGraph;
	
	public CreateTopologyMouseListener(CreateTopologyGraph g){
		mGraph = g;
	}
	
	public void mousePressed(MouseEvent event){

		if(event.isPopupTrigger())
			System.out.println("right click ");
		
		try {
			CellView view = mGraph.getNextSelectableViewAt(null, 
				(double)event.getX(), (double)event.getY());
			if(view == null) return; 
					
			if (event.getClickCount() == 2){ 	
				System.out.println("double click");
				if(view.getCell() instanceof TopoHostCell){
					TopoHostCell tvhcell = (TopoHostCell)view.getCell();
					TopologyModel.Host host = tvhcell.mHost;
					System.out.println(" double-click on host: " + host.name);

				}else if(view.getCell() instanceof TopoWire){

					TopoWire topoWire = (TopoWire)view.getCell();
					TopologyModel.Link link = topoWire.getLink();
					
					System.out.println(" double-click on link between " + link.host1.name
							+ " and " + link.host2.name);

				}
			}
			mGraph.setSelectionCell(null);

		}catch(Exception e){
			e.printStackTrace();
		}
	
	}
		
	
}

