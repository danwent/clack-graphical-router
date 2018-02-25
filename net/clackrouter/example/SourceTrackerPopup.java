/*
 * Created on Sep 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.example;

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.propertyview.DefaultPropertiesView;

/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SourceTrackerPopup extends DefaultPropertiesView {

	private JPanel reporting_panel;
	
    public SourceTrackerPopup(SourceTracker1 tracker) {
    	super(tracker.getName(), tracker);
    	tracker.registerListener(this);
        JPanel main = addMainPanel("SourceTracker");
        
        addHTMLDescription("error.html"); // use this to demo ?
        
        addPanelToTabPane("Ports", m_port_panel);
        refreshPortTab();
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        
        reporting_panel = new JPanel();
	
        reporting_panel.setLayout(new BoxLayout(reporting_panel, BoxLayout.PAGE_AXIS));
        reporting_panel.add(Box.createRigidArea(new Dimension(120, 20)));
        main.add(reporting_panel);
        addBorderToPanel(reporting_panel, "Source Tracker Data");
        
    }
    
    private void refreshTrackerData() {
    	reporting_panel.removeAll();
    	
        reporting_panel.add(Box.createRigidArea(new Dimension(120, 20)));
    	SourceTracker1 tracker = (SourceTracker1)m_model;
    	Hashtable counter = tracker.getCounter();
    	for(Enumeration e = counter.keys(); e.hasMoreElements();){
    		String addr = (String)e.nextElement();
    		int count = ((Integer)counter.get(addr)).intValue();
    		JLabel l = new JLabel(addr + " : " + count);
    		System.out.println("Adding : " + l.getText());
    		reporting_panel.add(l);
    	}

    }
    
    public void componentEvent(ClackComponentEvent e) 
    {  
    	super.componentEvent(e);
    	if(e.mType == ClackComponentEvent.EVENT_DATA_CHANGE){
    		refreshTrackerData();
    	}
    }
}
