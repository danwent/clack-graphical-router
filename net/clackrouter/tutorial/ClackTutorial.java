/*
 * Created on May 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.tutorial;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.clackrouter.error.ErrorReporter;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;

/**
 * Displays an HTML tutorial in a frame outside of the main Clack router frame.
 */
public class ClackTutorial extends JFrame {

	JEditorPane mEditorPane;
	public static int DEFAULT_HEIGHT = 500;
	public static int DEFAULT_WIDTH = 400;
	
	public ClackTutorial(String url_string){
		super("Clack Tutorial");
		setIconImage(ClackFramework.applicationIcon.getImage());
		mEditorPane = new JEditorPane();
		mEditorPane.setEditable(false);
		mEditorPane.setContentType("text/html");
		mEditorPane.addHyperlinkListener(new HyperlinkListener() {
			       public void hyperlinkUpdate(HyperlinkEvent ev) {
			            try {
			                if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			                	mEditorPane.setPage(ev.getURL());
			                }
			            } catch (IOException ex) {
			                error("Error Loading Tutorial URL = " +  ev.getURL().toString() + " " + ex.getMessage());
			            }
			        }
			    });

		    try {
		    	URL helpURL = new URL(url_string);
		    	mEditorPane.setPage(helpURL);
		    } catch (IOException e) {
		    	error("Error Loading Tutorial URL = " + url_string + " " + e.getMessage());
		    }
	

//		Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(mEditorPane);
		editorScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		editorScrollPane.setMinimumSize(new Dimension(10, 10));

		getContentPane().add(editorScrollPane);
		setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		show();		
	}
	
	private void error(String s)  {
		try {
    		mEditorPane.setPage(new URL(Translator.getString("TUTORIAL_ERROR_URL")));
    		ErrorReporter.reportError(null, s);
    	} catch (Exception e2) { e2.printStackTrace(); }
	}
	
}
