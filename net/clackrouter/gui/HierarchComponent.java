
package net.clackrouter.gui;




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


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.MissingResourceException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;

import net.clackrouter.application.ClackApplication;
import net.clackrouter.application.HTTPGetter;
import net.clackrouter.application.MiniWebServer;
import net.clackrouter.application.TCPRedirector;
import net.clackrouter.application.TCPSocket;
import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.tcp.OrderPackets;
import net.clackrouter.component.tcp.ProcessAck;
import net.clackrouter.component.tcp.ProcessSegment;
import net.clackrouter.component.tcp.ReceiveWindowCheck;
import net.clackrouter.component.tcp.Retransmitter;
import net.clackrouter.component.tcp.SendWindowCheck;
import net.clackrouter.component.tcp.SetChecksum;
import net.clackrouter.component.tcp.SockBuffer;
import net.clackrouter.component.tcp.TCB;
import net.clackrouter.component.tcp.TCP;
import net.clackrouter.component.tcp.ValidateChecksum;
import net.clackrouter.jgraph.pad.GPStatusBar;
import net.clackrouter.jgraph.pad.Rule;
import net.clackrouter.jgraph.pad.Touch;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.jgraph.utils.gui.GPSplitPane;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.graph.ComponentCell;
import net.clackrouter.router.graph.RouterGraph;
import net.clackrouter.router.graph.RouterGraphHelper;
import net.clackrouter.router.graph.RouterWire;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.graph.CellView;
import org.jgraph.graph.GraphModel;

/**
 * This component is basically a throw-away experiment.  Do not base anything on it.  
 * 
 * <p>Eventually, it may be salvaged and used as a framework for many hierarchical components,
 * but right now its a mess of old GPDocument and TCP GUI code.  </p>
 * 
 * <p>There must be a better way, but I don't have time to sort it out right now.    </p>
 * 
 * @deprecated - Hierarchical components are not supported for now.
 */
public class HierarchComponent 
extends 	JPanel
 {
	
	/**
	 */
	protected boolean enableTooltips;
	
	/** Filename for the current document.
	 * Null if never saved or opened.
	 *
	 */
	protected File file;
	
	/** A reference to the top level
	 *  component
	 */
	protected ClackFramework graphpad;
	
	/** Splitpane between the
	 * libraries and the graph
	 */
	protected GPSplitPane splitPane;
	
	
	/** Container for the graph so that you
	 *  can scroll over the graph
	 */
	protected JScrollPane scrollPane;
	
	/** The joint graph for this document
	 */
	protected RouterGraph graph;
	
	/** The overview Dialog for this document.
	 *  Can be <tt>null</tt>.
	 *
	 */
	protected JDialog overviewDialog;
	
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
//	protected GraphUndoManager graphUndoManager;
	
	/** The graphUndoManager handler for the current document.
	 * Each document has his own handler.
	 * So you can make an graphUndoManager seperate for each document.
	 *
	 */
//	protected UndoHandler undoHandler;
	
	/** On the fly layout
	 *
	 */
	protected Touch touch;
	
	/** True if this documents graph model
	 *  was modified since last save. */
	protected boolean modified = false;
	
	/** true if the current graph is Metric.
	 *  default is true.
	 */
	protected static boolean isMetric = true;
	
	/** true if the library expand is expanded
	 *  default is true
	 */
	protected static boolean libraryExpanded = true;
	
	/** true if the ruler show is activated
	 */
	protected static boolean showRuler = true;
	
	/** Action used for fitting the size
	 */
	protected Action fitAction;
	
	/** contains the find pattern for this document
	 */
	protected String findPattern;
	
	/** contains the last found object
	 */
	protected Object lastFound;
	
	/** a reference to the internal Frame
	 */
	
	protected ClackTab tab;
	
	protected Router router;
	
	//start diff
	protected TCB currentTCB;
	protected TCB dummyTCB;
	protected TCP mTCP;
	protected Hashtable componentCells = new Hashtable();
    protected JTextField ip_text = null;     
    protected JTextField port_text = null;
	// end diff
	
	protected GPStatusBar statusbar;

	
	/**
	 * Constructor for GPDocument.
	 */
	public HierarchComponent(
			ClackFramework graphpad,
			RouterGraph gpGraph,
			GraphModel model, TCP t) {
		super(true);
		
		this.graphpad = graphpad;

		
		// create the graph
		graph = gpGraph;
		touch = new Touch(graph);

		
		mTCP = t;
		router = mTCP.getRouter();
		dummyTCB = new TCB(mTCP, router, "dummyTCP");
		currentTCB = dummyTCB;


		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());
	
	//	Component comp = createScrollPane();
	//	add(BorderLayout.CENTER, comp);
		add(BorderLayout.CENTER, graph);
		createActionPanel();
		
		statusbar = new GPStatusBar();
		
		addTCBComponentsToGraph(dummyTCB);
		update();
	}
	
	private void createActionPanel() {
		JPanel panel = new JPanel();
	
		ip_text = new JTextField("yuba.stanford.edu");
		port_text = new JTextField("80");
		panel.add(ip_text);
		panel.add(port_text);
		
		ButtonPressed bp = new ButtonPressed();
		JButton httpGetterButton = new JButton(ButtonPressed.B_HTTPREQUESTER);
		panel.add( httpGetterButton);
		httpGetterButton.addActionListener(bp);
		JButton miniWebButton = new JButton(ButtonPressed.B_MINIWEBSERVER);
		panel.add(miniWebButton);
		miniWebButton.addActionListener(bp);
		JButton portforwarderButton = new JButton(ButtonPressed.B_PORTFORWARDER);
		panel.add(portforwarderButton);
		portforwarderButton.addActionListener(bp);
		
		
		add(BorderLayout.NORTH, panel);
	}
	
	public void setCurrentTCB(TCB newTCB){
		if(newTCB == null) newTCB = dummyTCB;
		switchDisplayedTCB(currentTCB, newTCB);	
		currentTCB = newTCB;
		CellView[] views = graph.getGraphLayoutCache().getMapping(new ComponentCell[] { currentTCB.mSocket.getComponentCell() }, true );
		CellView socketView = views[0];
		graph.getUI().paintCell(graph.getGraphics(), socketView , socketView.getBounds(), false);
	}
	
	private void switchDisplayedTCB(TCB oldTCB, TCB newTCB){
		System.out.println("switching TCBs.  Found " + componentCells.size() + " componentCells to switch");			
		swapInComponent(oldTCB.mSocket,newTCB.mSocket, (ComponentCell) componentCells.get(getTCPCompTypename(oldTCB.mSocket)));
		swapInComponent(oldTCB.mOrderPackets ,newTCB.mOrderPackets, (ComponentCell)componentCells.get(getTCPCompTypename(oldTCB.mOrderPackets)));
		swapInComponent(oldTCB.mProcessAck,newTCB.mProcessAck, (ComponentCell)componentCells.get(getTCPCompTypename(oldTCB.mProcessAck)));
		swapInComponent(oldTCB.mProcessSegment,newTCB.mProcessSegment,(ComponentCell) componentCells.get(getTCPCompTypename(oldTCB.mProcessSegment)));
		swapInComponent(oldTCB.mRecvWinCheck,newTCB.mRecvWinCheck, (ComponentCell)componentCells.get(getTCPCompTypename(oldTCB.mRecvWinCheck)));
		swapInComponent(oldTCB.mRetransmitter,newTCB.mRetransmitter,(ComponentCell) componentCells.get(getTCPCompTypename(oldTCB.mRetransmitter)));
		swapInComponent(oldTCB.mSendWinCheck ,newTCB.mSendWinCheck,(ComponentCell) componentCells.get(getTCPCompTypename(oldTCB.mSendWinCheck)));
		swapInComponent(oldTCB.mSetChecksum ,newTCB.mSetChecksum, (ComponentCell)componentCells.get(getTCPCompTypename(oldTCB.mSetChecksum)));
		swapInComponent(oldTCB.mValidateChecksum ,newTCB.mValidateChecksum, (ComponentCell)componentCells.get(getTCPCompTypename(oldTCB.mValidateChecksum)));	
	}

	private void swapInComponent(ClackComponent oldComp, ClackComponent newComp, ComponentCell cell){
		if(!(oldComp.getClass().getName().equals(newComp.getClass().getName()))){
			System.err.println("Error, two ClackComponents given to swapInComponent are not of same class type");
		}
		
		cell.setClackComponent(newComp);
		for(int i = 0; i < oldComp.getNumPorts(); i++){
			ClackPort oldPort = oldComp.getPort(i);
			RouterWire wire = oldPort.getListeningWire();
			oldPort.setListeningWire(null);
			newComp.getPort(i).setListeningWire(wire);
		}
	}
	
	private void addTCBComponentsToGraph(TCB tcb) {

		RouterGraphHelper helper = graphpad.getClackGraphHelper();
		ComponentCell vcCell = (ComponentCell) helper.addRouterComponentCell(router, tcb.mValidateChecksum, new Rectangle(3, 139, 132, 29));
		componentCells.put(getTCPCompTypename(tcb.mValidateChecksum), vcCell);
		ComponentCell paCell = (ComponentCell)helper.addRouterComponentCell(router,tcb.mProcessAck, new Rectangle(9, 94, 110, 29));
		componentCells.put(getTCPCompTypename(tcb.mProcessAck), paCell);
		ComponentCell rCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mRetransmitter, new Rectangle(155, 94, 110, 29));
		componentCells.put(getTCPCompTypename(tcb.mRetransmitter), rCell);
		ComponentCell swCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mSendWinCheck, new Rectangle(141, 37, 133, 29));
		componentCells.put(getTCPCompTypename(tcb.mSendWinCheck), swCell);
		ComponentCell rwCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mRecvWinCheck, new Rectangle(290, 93, 144, 29));
		componentCells.put(getTCPCompTypename(tcb.mRecvWinCheck), rwCell);
		ComponentCell opCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mOrderPackets,new Rectangle(463, 91, 113, 29));
		componentCells.put(getTCPCompTypename(tcb.mOrderPackets), opCell);
		ComponentCell psCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mProcessSegment, new Rectangle(603, 93, 121, 29));
		componentCells.put(getTCPCompTypename(tcb.mProcessSegment), psCell);
		ComponentCell sCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mSocket, new Rectangle(441, 19, 140, 49));
		componentCells.put(getTCPCompTypename(tcb.mSocket), sCell);
		ComponentCell scCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mSetChecksum, new Rectangle(370, 146, 113, 29));
		componentCells.put(getTCPCompTypename(tcb.mSetChecksum), scCell);
		
		ComponentCell sbCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mSendBuffer, new Rectangle(330, 30, 110, 40));
		componentCells.put(getTCPCompTypename(tcb.mSendBuffer), sbCell);
		ComponentCell rbCell =(ComponentCell)helper.addRouterComponentCell(router,tcb.mRecvBuffer, new Rectangle(620, 30, 110, 40));
		componentCells.put(getTCPCompTypename(tcb.mRecvBuffer), rbCell);	
		
		helper.addRouterWire(tcb.mValidateChecksum.getComponentCell(), ValidateChecksum.PORT_TCP_VALID,tcb.mProcessAck.getComponentCell(), ProcessAck.PORT_IN);
		helper.addRouterWire(tcb.mProcessAck.getComponentCell(), ProcessAck.PORT_OUT, tcb.mRetransmitter.getComponentCell(), Retransmitter.PORT_PROCESS_IN);
		helper.addRouterWire(tcb.mRetransmitter.getComponentCell(), Retransmitter.PORT_PROCESS_OUT, tcb.mRecvWinCheck.getComponentCell(), ReceiveWindowCheck.PORT_IN);
		helper.addRouterWire(tcb.mRecvWinCheck.getComponentCell(), ReceiveWindowCheck.PORT_OUT, tcb.mOrderPackets.getComponentCell(), OrderPackets.PORT_IN);
		helper.addRouterWire(tcb.mOrderPackets.getComponentCell(), OrderPackets.PORT_OUT, tcb.mProcessSegment.getComponentCell(), ProcessSegment.PORT_NEWDATA_IN);
		helper.addRouterWire(tcb.mProcessSegment.getComponentCell(), ProcessSegment.PORT_APP_OUT, tcb.mRecvBuffer.getComponentCell(), SockBuffer.PORT_TAIL);
		helper.addRouterWire(tcb.mRecvBuffer.getComponentCell(), SockBuffer.PORT_TAIL, tcb.mSocket.getComponentCell(), TCPSocket.PORT_NET_IN);
		helper.addRouterWire(tcb.mProcessSegment.getComponentCell(), ProcessSegment.PORT_ACK_OUT, tcb.mSetChecksum.getComponentCell(), SetChecksum.PORT_TCP_IN);
		helper.addRouterWire(tcb.mRetransmitter.getComponentCell(), Retransmitter.PORT_NET_OUT, tcb.mSetChecksum.getComponentCell(), SetChecksum.PORT_TCP_IN);		
		helper.addRouterWire(tcb.mSocket.getComponentCell(), TCPSocket.PORT_NET_OUT,tcb.mSendBuffer.getComponentCell(), SockBuffer.PORT_TAIL);
		helper.addRouterWire(tcb.mSendBuffer.getComponentCell(), SockBuffer.PORT_HEAD, tcb.mSendWinCheck.getComponentCell(), SendWindowCheck.PORT_APP_IN);
		helper.addRouterWire(tcb.mSendWinCheck.getComponentCell(), SendWindowCheck.PORT_NET_OUT, tcb.mRetransmitter.getComponentCell(), Retransmitter.PORT_STORE_IN);
	}
	
	private static String getTCPCompTypename(ClackComponent comp){ return comp.getName().split(":")[1]; }
	
	/**
	 * Returns the filename.
	 * @return String
	 */
	public File getFilename() {
		return file;
	}
	
	public Router getRouter() {
		return router;
	}
	
	public JGraph getGraph() { return graph;}

	
	public GPStatusBar getStatusBar() {
		return statusbar;
	}
	

	/**
	 * Sets the filename.
	 * @param filename The filename to set
	 */
	public void setFilename(File filename) {
		this.file = filename;
		updateTabTitle();
	}


	
	protected JScrollPane createScrollPane() {
		scrollPane = new JScrollPane(graph);
		JViewport port = scrollPane.getViewport();
		try {
			String vpFlag = Translator.getString("ViewportBackingStore");
			Boolean bs = new Boolean(vpFlag);
			if (bs.booleanValue()) {
				port.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
			} else {
				port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			}
			// deprecated
			// port.setBackingStoreEnabled(bs.booleanValue());
		} catch (MissingResourceException mre) {
			// just use the viewport default, do not report
		}
		columnRule = new Rule(Rule.HORIZONTAL, isMetric, graph);
		rowRule = new Rule(Rule.VERTICAL, isMetric, graph);
		if (showRuler) {
			scrollPane.setColumnHeaderView(columnRule);
			scrollPane.setRowHeaderView(rowRule);
		}
		return scrollPane;
	}

	public void setModified(boolean modified) {
		this.modified = modified;

		updateTabTitle();
		graphpad.update();
	}
	
	/* Return the title of this document as a string. */
	protected String getDocumentTitle() { return "Internal TCP View"; }
	

	/* Return the scale of this document as a string. */
	protected String getDocumentScale() {
		return Integer.toString((int) (graph.getScale() * 100)) + "%";
	}
	
	
	// GraphModelListener
	public void graphChanged(GraphModelEvent e) {
		modified = true;
		touch.resetDamper();
		update();
		GraphModelEvent.GraphModelChange change = e.getChange();
		Object[] removed = change.getRemoved();
		if(removed != null) {
			for(int i = 0; i < removed.length; i++){

				if(removed[i] instanceof RouterWire){
					// if a wire is removed, we should disconnect underlying connectivity
					RouterWire wire = (RouterWire) removed[i];
					ClackPort source_port = wire.getSourceComponent().getPort(wire.getSourcePortNum());
					ClackPort target_port = wire.getTargetComponent().getPort(wire.getTargetPortNum());
					ClackPort.disconnectPorts(source_port, target_port);

				}
				if(removed[i] instanceof ComponentCell){
					// remove component from router, remove any connected edges
					ComponentCell cell =  (ComponentCell)removed[i];
					ClackComponent comp = cell.getClackComponent();
					Object[] edges = graph.getEdges(graph.getAll());
					router.removeComponent(comp);
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
		}
		//System.out.println("Change:\n"+buttonEdge.getChange().getStoredAttributeMap());
	}
	
	protected void update() {
		updateTabTitle();

		graphpad.update();
	//	graphpad.getStatusBar().setMessage(this.getDocumentStatus());
	//	getStatusBar().setScale(this.getDocumentScale());
	}
	
	
	/**
	 * Returns the graphpad.
	 * @return GPGraphpad
	 */
	public ClackFramework getGraphpad() {
		return graphpad;
	}
	
	/**
	 * Sets the graphpad.
	 * @param graphpad The graphpad to set
	 */
	public void setGraphpad(ClackFramework graphpad) {
		this.graphpad = graphpad;
	}
	
	/**
	 * Returns the touch.
	 * @return Touch
	 */
	public Touch getTouch() {
		return touch;
	}
	
	/**
	 * Sets the touch.
	 * @param touch The touch to set
	 */
	public void setTouch(Touch touch) {
		this.touch = touch;
	}
	


	/**
	 * Returns the splitPane.
	 * @return JSplitPane
	 */
	public GPSplitPane getSplitPane() {
		return splitPane;
	}
	
	/**
	 * Sets the splitPane.
	 * @param splitPane The splitPane to set
	 */
	public void setSplitPane(GPSplitPane splitPane) {
		this.splitPane = splitPane;
	}
	
	/**
	 * Returns the scrollPane.
	 * @return JScrollPane
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	/**
	 * Sets the scrollPane.
	 * @param scrollPane The scrollPane to set
	 */
	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}
	
	/**
	 * Returns the columnRule.
	 * @return Rule
	 */
	public Rule getColumnRule() {
		return columnRule;
	}
	
	/**
	 * Returns the rowRule.
	 * @return Rule
	 */
	public Rule getRowRule() {
		return rowRule;
	}
	
	/**
	 * Sets the columnRule.
	 * @param columnRule The columnRule to set
	 */
	public void setColumnRule(Rule columnRule) {
		this.columnRule = columnRule;
	}
	
	/**
	 * Sets the rowRule.
	 * @param rowRule The rowRule to set
	 */
	public void setRowRule(Rule rowRule) {
		this.rowRule = rowRule;
	}
	
	/**
	 * Returns the enableTooltips.
	 * @return boolean
	 */
	public boolean isEnableTooltips() {
		return enableTooltips;
	}
	
	/**
	 * Sets the enableTooltips.
	 * @param enableTooltips The enableTooltips to set
	 */
	public void setEnableTooltips(boolean enableTooltips) {
		this.enableTooltips = enableTooltips;
		
		if (this.enableTooltips)
			ToolTipManager.sharedInstance().registerComponent(graph);
		else
			ToolTipManager.sharedInstance().unregisterComponent(graph);
	}
	

	/**
	 * Returns the internalFrame.
	 * @return GPInternalFrame
	 */
	/*
	public GPInternalFrame getInternalFrame() {
		return internalFrame;
	}
	*/
	

	
	
	protected void updateTabTitle() {
		if (this.tab != null) {
			this.tab.setTitle(getDocumentTitle());
		}
	}

	

	

	
	
    private class ButtonPressed implements ActionListener 
    {
        public static final String B_HTTPREQUESTER = "HTTP Requester";
        public static final String B_MINIWEBSERVER = "Mini Webserver";
        public static final String B_PORTFORWARDER = "Portforwarder";
        public void actionPerformed(ActionEvent event) 
        {
            String cmd =  event.getActionCommand();
            
        	TCB newTCB = mTCP.createTCB(cmd);
        	TCPSocket socket = newTCB.mSocket;
        	Router router = newTCB.getRouter();
            String ip_str = ip_text.getText();
            String port_str = port_text.getText();
   
        	int port = Integer.parseInt(port_str);
        	
            try {
            	ClackApplication app = null;
 
            	if(cmd.equals(B_HTTPREQUESTER)){
            		String args[] = new String[]{ ip_str /*remote*/,  router.getInputInterfaces()[0].getIPAddress() + "" /*local*/ };
            		app = new HTTPGetter();
            		app.configure("httpgetter", mTCP.getRouter(),args );
            	}else if(cmd.equals(B_MINIWEBSERVER)){
            		String[] args = new String[] { router.getInputInterfaces()[0].getIPAddress().getHostAddress() };
            		app = new MiniWebServer();
            		app.configure("miniweb", mTCP.getRouter(), args);
            		return;
            	}else if(cmd.equals(B_PORTFORWARDER)){
            		app = new TCPRedirector( );
            		app.configure("PortForwarder", router,new String[] { ip_str, "" + port });
            	}else {
            		System.err.println("Command '" + cmd + "' not recognized");
            		return;
            	}
            	if(app != null){
            		socket.setCurrentApplication(app);
            		app.start();
            	}
            	
            } catch(Exception e){
            	e.printStackTrace();
            }

        }
    } // -- class ItemAdder
	
}
