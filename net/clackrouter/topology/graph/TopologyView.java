/*
 * Created on Apr 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.topology.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.ClackView;
import net.clackrouter.router.core.Router;
import net.clackrouter.routing.LocalLinkInfo;
import net.clackrouter.topology.core.TopoGraph;
import net.clackrouter.topology.core.TopoPopup;
import net.clackrouter.topology.core.TopologyModel;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;


/**
 * ClackView for the graph showing a VNS topology using a {@link TopoGraph}.  
 */
public class TopologyView extends JPanel implements ClackView {

	private boolean mIsVisible = false; 
	private TopologyModel mTopoModel;
	private ClackFramework mGraphpad;
	private TopoGraph mGraph;
	private ClackDocument mDocument;
	
	public TopologyView(ClackFramework pad, ClackDocument doc, int topology) throws Exception {
		mDocument = doc;

		mTopoModel = pad.getTopologyManager().getTopologyModel(topology, null);
		mGraphpad = pad;

		mGraph = mTopoModel.createTopoGraph(this, doc);

		this.add(mGraph);
		this.setBackground(Color.WHITE);
		//mGraph.setPreferredSize(new Dimension(900, 600));
		
		// only add the mouse listener if this is part of the hierarchical
		// view, not with the HostSelector
		mGraph.setEditable(false);


		mGraph.addMouseListener(new TopoMouseListener());
		
		mGraph.updateUI();
	}
	
	public TopologyModel getModel() { return mTopoModel; }
	public JGraph getGraph() { return mGraph; }
	public Component getComponent() { return this; }
	
	public void layoutTopoGraph()throws Exception {
       
		mGraph.placePorts();
	}
	
	//public String getServer() { return SERVER_NAME; }
	public Object getSelectionCell() { return mGraph.getSelectionCell(); }
	
	public boolean isVisibleView() { return mIsVisible && mDocument.isClackVisible(); }
	
	public void setIsVisibleView(boolean b) { mIsVisible = b; }
	private class TopoMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent event){
			
			try {
			CellView view = mGraph.getNextSelectableViewAt(null, (double)event.getX(), (double)event.getY());
			if(view == null) return;
			
			if(event.getClickCount() == 2){
		
				if(view.getCell() instanceof TopoHostCell){
					TopoHostCell tvhcell = (TopoHostCell)view.getCell();
					mGraphpad.getCurrentDocument().zoomIntoRouter(tvhcell.mHost.name);

				}else if(view.getCell() instanceof TopoWire){
					// enable / disable a link!
					TopoWire topoWire = (TopoWire)view.getCell();
					TopologyModel.Link link = topoWire.getLink();
						
					mTopoModel.setLinkStatus(mDocument, link, !link.isEnabled);

				}
			}else if(SwingUtilities.isRightMouseButton(event) && !event.isShiftDown()){
				if(view.getCell() instanceof TopoHostCell){
					TopoHostCell tvhcell = (TopoHostCell)view.getCell();
					if(tvhcell.mHost == null) return;
					
					TopoPopup tp = new TopoPopup(tvhcell.mHost);
					tp.show(mGraph,event.getX(),event.getY());
				}
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		
		}
			
		
	}
	

}
