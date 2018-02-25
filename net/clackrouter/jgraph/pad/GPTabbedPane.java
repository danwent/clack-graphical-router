/*
 * Created on Apr 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.jgraph.pad;

import java.util.ArrayList;

import javax.swing.JTabbedPane;

import net.clackrouter.gui.ClackTab;

/**
 * @author Jonathan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GPTabbedPane extends JTabbedPane {
		
	public GPTabbedPane() {
		super();
	}

	public ClackTab[] getAllTabs() {
		int numTabs = this.getTabCount();
		
		if (numTabs >= 0) {
			ArrayList tabs = new ArrayList();
			for (int i = 0; i < numTabs; i++) {
				tabs.add((ClackTab) this.getComponentAt(i));
			}
			return (ClackTab[]) tabs.toArray(new ClackTab[tabs.size()]);
		}
		else 
			return null;
	}

	

}
