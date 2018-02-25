/*
 * Created on Mar 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.gui.tcp;

import java.awt.Component;

import javax.swing.JPanel;

import net.clackrouter.application.TCPSocket;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackComponentListener;
import net.clackrouter.component.tcp.TCB;
import net.clackrouter.component.tcp.TCP;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackView;

import org.jgraph.JGraph;


/**
 * A defunct class, used with the TCP hiearchical component.  
 * 
 * <p> This is the view of a single Clack TCP stack, including 
 * the tree view, the dashboard, and a Graph view of each of the 
 * TCP subcomponents.  </p>
 * 
 */
public class TCPView extends JPanel implements ClackComponentListener, ClackView  {
	
	//private HierarchComponent hierarch;
	TCPTreeView treeView;
	private TCPDashboard dashboard;
	private boolean mIsVisible = false;
	private ClackDocument mDocument; 
	
	public TCPView(TCP tcp, ClackDocument parent) {
	/*	super();	
		tcp.registerListener(this);
		setLayout(new BorderLayout());
		mDocument = parent;
		hierarch = getHierarchicalComponent(tcp);
	//	hierarch.setPreferredSize(new Dimension(500, 100));
		add(hierarch, BorderLayout.CENTER);
		
		tcp.setHierarchComp(hierarch);
		treeView = new TCPTreeView(this);
		add(treeView, BorderLayout.WEST);
		
		dashboard = new TCPDashboard(tcp);
		add(dashboard, BorderLayout.SOUTH);
		
		setSize(new Dimension(1200, 500));
		setVisible(true);
		*/
	}
	
	public JGraph getGraph() { return null ; } //return hierarch.getGraph(); }
	public Component getComponent() { return this; }
	/*
	private HierarchComponent getHierarchicalComponent(TCP tcp){

		ClackGraphpad graphpad = tcp.getRouter().getDocument().getGraphpad();
		
		GraphModel model = new DefaultGraphModel();
		RouterGraph gpGraph = new RouterGraph(model);
	
		gpGraph.setSelectNewCells(true);
		gpGraph.setInvokesStopCellEditing(true);
		gpGraph.setCloneable(true);
		gpGraph.setMarqueeHandler(graphpad.getMarqueeHandler());
		return new HierarchComponent(graphpad, gpGraph, model, tcp);
		
	}*/

	public void componentEvent(ClackComponentEvent event){
		if(event instanceof TCP.UpdateEvent){
			TCP.UpdateEvent update = (TCP.UpdateEvent)event;
		
			if(update.mType == TCP.UpdateEvent.TCP_SOCKET_CREATED){
				treeView.socketBound((TCPSocket)update.mObj);
			}else if(update.mType == TCP.UpdateEvent.TCP_TCB_CREATED){
				treeView.tcbCreated((TCB)update.mObj);
			}
		}
	}
	
	public void TCBSelected(TCB theTCB) {
	//	hierarch.setCurrentTCB(theTCB);	
	//	dashboard.setCurrentTCB(theTCB);
	}
	
	public boolean isVisibleView() { return mIsVisible && mDocument.isClackVisible(); }
	
	public void setIsVisibleView(boolean b) { mIsVisible = b; }

}
