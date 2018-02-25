
package net.clackrouter.gui;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;

import net.clackrouter.jgraph.pad.GPTabbedPane;
import net.clackrouter.jgraph.utils.gui.LocaleChangeAdapter;
import net.clackrouter.jgraph.utils.gui.PositionManager;


/**
 * This is a wrapper class representing a single tab in a ClackGraphpad.
 * 
 * <p> A ClackTab contains a single {@link ClackDocument} </p>
 */
public class ClackTab extends JPanel {
	
	/** A link to the Graphpad Document of this frame
	 */
	
	public static final int TAB_WIDTH = 1000;
	public static final int TAB_HEIGHT = 650;
	ClackDocument document;
	GPTabbedPane tabbedPane;
	int indexNum;
	
	public ClackTab(ClackDocument document, GPTabbedPane tabbedPane, int index) {
		super();
		this.document = document;
		this.document.setTab(this);
		this.add(document);
		this.tabbedPane = tabbedPane;
		this.indexNum = index;
		this.addVetoableChangeListener(new GPVetoableListner(document)); 
		tabbedPane.addChangeListener(new TabChangeListener());
		
	}


	/**
	 * Returns the document.
	 * @return GPDocument
	 */
	public ClackDocument getDocument() {
		return document;
	}

	/**
	 * Sets the document.
	 * @param document The document to set
	 */
	public void setDocument(ClackDocument document) {
		this.remove(this.document);
		this.document = document;
		this.add(this.document);
		//this.pack();
	}
	
	/**
	 * 
	 * Cleans up references that cause memory leaks
	 * Maintainance fix, rather than proper fix
	 */
	public void cleanUp(){
		this.document = null;
		LocaleChangeAdapter.removeContainer(this);
		PositionManager.removeComponent(this);
	}

	public void setTitle(String newTitle) {
		if (tabbedPane.getTabCount() > indexNum)
			tabbedPane.setTitleAt(indexNum, newTitle);
	}
	
	private class TabChangeListener implements ChangeListener {
	    // This method is called whenever the selected tab changes
	    public void stateChanged(ChangeEvent evt) {
	        JTabbedPane pane = (JTabbedPane)evt.getSource();

	        // Get current tab
	        int sel = pane.getSelectedIndex();
	        if(document == null) return;
	        document.setIsClackVisible(indexNum == sel);
	    }
	}
}




class GPVetoableListner implements VetoableChangeListener {

	ClackDocument document;

	GPVetoableListner(ClackDocument doc) {
		this.document = doc;
	}
	/**
	 * @see javax.swing.event.InternalFrameListener#internalFrameClosing(InternalFrameEvent)
	 */
	public void vetoableChange(PropertyChangeEvent evt)
		throws PropertyVetoException {
		
		if (((Boolean)evt.getNewValue()).booleanValue()){
				document.getFramework().removeDocument(document);
		} else {
			throw new PropertyVetoException("Can't close the Internal Frame", evt) ;
		}
	}

	

}
