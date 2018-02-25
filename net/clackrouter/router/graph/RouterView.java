/*
 * Created on Apr 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.router.graph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackView;

import org.jgraph.JGraph;




/**
 * The view class for a Router, which is a split screen of a RouterGraph
 * and the hideable property view window.  
 */
public class RouterView implements ClackView {
	
	public static String ACTION_CLOSE = "Close View";
	public static String ACTION_UNDOCK = "Undock View";
	
	//	 the fraction of the space initially taken when the pview 
	// side bar first opens
	public static double SPLIT_RATIO = .35; 
							
	
	private boolean isVisible = false;
	
	private JGraph mGraph;
	private ClackDocument mDocument;
	protected JTabbedPane mPropertyViewsTabs;
	protected JSplitPane mSplit;
	
	public RouterView(JGraph graph, ClackDocument parent) { 
		mGraph = graph; 
		mDocument = parent;

		// setup property views
		JPanel propViewPanel = new JPanel();
		propViewPanel.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JButton close = new JButton(ACTION_CLOSE);
		JButton undock = new JButton(ACTION_UNDOCK);
		buttonPanel.add( close);
		buttonPanel.add( undock);
		PViewListener pvl = new PViewListener();
		close.addActionListener(pvl);
		undock.addActionListener(pvl);
		propViewPanel.add(BorderLayout.NORTH, buttonPanel);
		mPropertyViewsTabs = new JTabbedPane();
		mPropertyViewsTabs.setMinimumSize(new Dimension(0, 0));
		mPropertyViewsTabs.setTabPlacement(JTabbedPane.TOP);
		propViewPanel.add(BorderLayout.CENTER, mPropertyViewsTabs);
		
		// setup split pane for whole router view
		mSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, propViewPanel, mGraph);
		
		mSplit.setName("DocumentMain");
		mSplit.setOneTouchExpandable(true);
		mSplit.setDividerLocation(0);

	}
	
	
	public void addToPropertyViewTab(String tab_name, JPanel panel){
	//	VTextIcon textIcon = new VTextIcon(mPropertyViewsTabs, tab_name, VTextIcon.ROTATE_LEFT);
	//	mPropertyViewsTabs.addTab(null, textIcon, panel);
		for(int i = 0; i < mPropertyViewsTabs.getTabCount(); i++){
			if(mPropertyViewsTabs.getTitleAt(i).equals(tab_name)){
				mPropertyViewsTabs.setSelectedIndex(i);
				openSideView();
				return;
			}
		}
		mPropertyViewsTabs.addTab(tab_name, panel);
		mPropertyViewsTabs.setSelectedComponent(panel);
		openSideView();
	}
	
	private void openSideView() {
		int oldLoc = mSplit.getDividerLocation();
		mSplit.setDividerLocation(SPLIT_RATIO);
		int newLoc = mSplit.getDividerLocation();
		if(oldLoc > newLoc)
			mSplit.setDividerLocation(oldLoc);
	}
	
	public JGraph getGraph() { return mGraph; }
	public Component getComponent() { return mSplit; }
	public boolean isVisibleView() { return isVisible && mDocument.isClackVisible(); }
	
	public void setIsVisibleView(boolean b) { isVisible = b; }
	
	private class PViewListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			int curIndex = mPropertyViewsTabs.getSelectedIndex();
			if (curIndex == -1) return; // Nothing is there
			if(e.getActionCommand().equals(ACTION_CLOSE)){
				mPropertyViewsTabs.remove(curIndex);
			}else if(e.getActionCommand().equals(ACTION_UNDOCK)){
				Component comp = mPropertyViewsTabs.getComponent(curIndex);
				mPropertyViewsTabs.remove(curIndex);		
				JFrame frame = new JFrame();
				frame.getContentPane().add(comp);
				frame.setSize(new Dimension(350, 420));
				frame.show();
			}
			if(mPropertyViewsTabs.getSelectedComponent() == null)
				mSplit.setDividerLocation(0.0);
		}
	}
}
