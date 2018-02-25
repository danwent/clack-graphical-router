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

package net.clackrouter.gui;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.clackrouter.actions.AbstractActionDefault;
import net.clackrouter.actions.ClackAddComponent;
import net.clackrouter.actions.ClackClearErrors;
import net.clackrouter.actions.ClackOpenAssignment;
import net.clackrouter.actions.ClackRunConnectivityTest;
import net.clackrouter.actions.ClackShowConsole;
import net.clackrouter.actions.ClackSpawnShell;
import net.clackrouter.actions.ClackStartEthereal;
import net.clackrouter.actions.ClackStopEthereal;
import net.clackrouter.actions.ClackToggleRouteTableView;
import net.clackrouter.actions.EditCell;
import net.clackrouter.actions.EditDelete;
import net.clackrouter.actions.EditRedo;
import net.clackrouter.actions.EditUndo;
import net.clackrouter.actions.FileClose;
import net.clackrouter.actions.FileConnect;
import net.clackrouter.actions.FileExit;
import net.clackrouter.actions.FileExportGIF;
import net.clackrouter.actions.FileExportPNG;
import net.clackrouter.actions.FileNew;
import net.clackrouter.actions.FileOpen;
import net.clackrouter.actions.FileOpenURL;
import net.clackrouter.actions.FilePrint;
import net.clackrouter.actions.FileSave;
import net.clackrouter.actions.FileSaveAll;
import net.clackrouter.actions.ViewScaleZoomIn;
import net.clackrouter.actions.ViewScaleZoomOut;
import net.clackrouter.application.ApplicationManager;
import net.clackrouter.error.ErrorConsole;
import net.clackrouter.error.ErrorReporter;
import net.clackrouter.jgraph.pad.GPGraphTools;
import net.clackrouter.jgraph.pad.GPLogConsole;
import net.clackrouter.jgraph.pad.GPTabbedPane;
import net.clackrouter.jgraph.pad.resources.ImageLoader;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.jgraph.utils.Utilities;
import net.clackrouter.jgraph.utils.gui.GPFrame;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.TimeManager;
import net.clackrouter.router.graph.RouterGraph;
import net.clackrouter.router.graph.RouterGraphHelper;
import net.clackrouter.topology.core.TopologyManager;

/**
 * Core Clack class, which implements the main Clack panel.  Based on GPGraphad.
 * 
 * <p> A ClackGraphpad is created by the ClackFramework object and contains the menus,
 * as well as the tabbed pane that contains ClackTabs.  </p>
 */
public class ClackFramework extends JPanel implements ComponentListener {

	public static final String PARAM_STRING = "parameters";	

	public static ImageIcon applicationIcon; // Application Icon. From resource file.
	protected static ImageIcon logoIcon; // Application Icon. From resource file.
	protected static String appTitle; // Application Title. From resource file.
	protected static int entrySize = 60; // Default entry size
	
	protected Applet applet; // Pointer to enclosing applet, if any
	protected boolean toolBarsVisible = true; // Boolean for the visible state of the toolbars
	protected static GPGraphTools graphTools = new GPGraphTools(); // Global instance for some graph tool implementations.
	protected GPLogConsole logger = new GPLogConsole(appTitle, false); // Log console for the System in and out messages
	protected GPTabbedPane tabbedPane = new GPTabbedPane(); // tabbed pane to hold ClackTabs
	protected Hashtable doc2tab = new Hashtable(); // Contains the mapping between GPDocument objects and GPInternalFrames.
	protected ClackBarFactory barFactory = new ClackBarFactory(this); //  A factory for the menu, tool and popup bars
	protected JPanel toolBarMainPanel = new JPanel(new BorderLayout()); // The current Toolbar for this graphpad
	protected JPanel toolBarInnerPanel; // The current Toolbar for this graphpad
	protected JMenuBar menubar; // The current Menubar for this graphpad
	protected ActionMap defaultActionMap = new ActionMap(); //ActionMap contains all default Actions for this application
	public JPanel mainPanel = new JPanel(new BorderLayout()); // The main Panel containing the tabbed pane 
	protected ActionMap currentActionMap = defaultActionMap; // ActionMap contains the current ActionMap.
	protected ClackMarqueeHandler marqueeHandler = new ClackMarqueeHandler(this);	
	protected boolean isSimpleGraph = false;
	protected RouterGraphHelper clackGraphHelper;
	protected TopologyManager topologyManager;
	protected ErrorConsole errorConsole;
	protected ApplicationManager mAppManager;
	protected TimeManager mTimeManager;
	protected Hashtable<Integer, ClackDocument> mDocMap;
	protected boolean mDebug;

	/**
	 * Creates a new instance with the parsed parameters and parent applet.
	 */
	public ClackFramework(Applet applet) {
		super(true);
		
		clackGraphHelper = new RouterGraphHelper(this);
		this.addComponentListener(this);
		this.applet = applet;
		topologyManager = new TopologyManager(this);
		mAppManager = new ApplicationManager();
		mTimeManager = new TimeManager();
		mDocMap = new Hashtable<Integer, ClackDocument>();
		
		// use and fill the action map
		fillDefaultActionMap();

		// set my reference to the actions
		setMe4Actions();
	}
	
	public void addAdditionalComponent(String menuName, String fullClassName){
		ClackAddComponent adder = (ClackAddComponent) getCurrentActionMap().get("ClackAddComponent");
		adder.addMapping(menuName, fullClassName);
	}
	
	public void initializeAndShow() {
		// set the look and feel
		try {
			// Try GTK exlicitley for Java < 1.4.2
			//TODO: where should these 2 lines go, given that they seem to work both here and in next try/catch block?
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			
			// Paul keeps getting null pointers from the above one
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (Exception exc) {
			try {
				// Run with system look and feel (Java 1.4.2 returns GTK as
				// appropriate)

				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());

			} catch (Exception exc2) {
				System.err.println("Error loading L&F: " + exc);
			}
		}

		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());

		// build the menu and the toolbar
		menubar = barFactory.createMenubar();

		getCurrentActionMap().get("ClackShowConsole").setEnabled(true);
		toolBarInnerPanel = barFactory.createToolBars(toolBarMainPanel);
		setToolBarsVisible(true);

		add(BorderLayout.NORTH, menubar);
		add(BorderLayout.CENTER, mainPanel);

		JFrame frame = createFrame();
		frame.setIconImage(applicationIcon.getImage());
		frame.setTitle(appTitle);
		frame.getContentPane().add(this);
		frame.addWindowListener(new AppCloser(frame));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		frame.show();		
	}
	
	

	/**
	 * The initializes the common values
	 *  
	 */
	static {

			String vers = System.getProperty("java.version");
			if (vers.compareTo("1.4") < 0) {

				JOptionPane.showMessageDialog(null,"Clack must be run with a Java VM version 1.4 or higher",
						"Error", JOptionPane.ERROR_MESSAGE);
				ErrorReporter.reportError(null, "BAD VERSION: Clack run with java VM version = " + vers);
			}
			appTitle = Translator.getString("Title");

			// Logo
			String iconName = Translator.getString("Logo");
			logoIcon = ImageLoader.getImageIcon(iconName);

			// Icon
			iconName = Translator.getString("Icon");
			applicationIcon = ImageLoader.getImageIcon(iconName);


	}
	

	/**
	 * Creates a frame for this Graphpad panel
	 *  
	 */
	protected JFrame createFrame() {

		GPFrame gpframe = new GPFrame();
		return gpframe;
	}
	
	public RouterGraphHelper getClackGraphHelper() { return clackGraphHelper; }
	public TopologyManager getTopologyManager() { return topologyManager; }
	public boolean isSimpleGraph() { return isSimpleGraph; }
	public void setIsSimpleGraph(boolean b) { isSimpleGraph = b; }
	
	public void addClackApplication(String cmd, String classname){
		mAppManager.addApplicationMapping(cmd, classname);
	}
	/**
	 */
	public void fillDefaultActionMap() {
		for (int i = 0; i < defaultActions.length; i++) {
			defaultActionMap.put(defaultActions[i].getValue(Action.NAME),
					defaultActions[i]);
		}
	}

	protected void setMe4Actions() {
		Object[] keys;
		keys = defaultActionMap.allKeys();
		for (int i = 0; i < keys.length; i++) {
			Action a = defaultActionMap.get(keys[i]);
			if (a instanceof AbstractActionDefault) {
				AbstractActionDefault ad = (AbstractActionDefault) a;
				if (ad.getGraphpad() == null)
					ad.setGraphpad(this);
			}
		}

		if (currentActionMap == defaultActionMap)
			return;

		keys = currentActionMap.allKeys();
		for (int i = 0; i < keys.length; i++) {
			Action a = currentActionMap.get(keys[i]);
			if (a instanceof AbstractActionDefault) {
				AbstractActionDefault ad = (AbstractActionDefault) a;
				if (ad.getGraphpad() == null)
					ad.setGraphpad(this);
			}
		}
	}

	/**
	 * Returns the current Action Map
	 */
	public ActionMap getCurrentActionMap() {
		return currentActionMap;
	}

	/**
	 * To shutdown when run as an application. This is a fairly lame
	 * implementation. A more self-respecting implementation would at least
	 * check to see if a save was needed.
	 */
	protected final class AppCloser extends WindowAdapter {

		Frame frame;

		AppCloser(Frame f) {
			frame = f;
		}

		public void windowClosing(WindowEvent e) {

			// run the action
			currentActionMap.get(
					Utilities.getClassNameWithoutPackage(FileExit.class))
					.actionPerformed(null);

			//System.exit(0);
		}
	}

	/**
	 * Find the hosting frame, for the file-chooser dialog.
	 */
	public Frame getFrame() {
		for (Container p = getParent(); p != null; p = p.getParent()) {
			if (p instanceof Frame) {
				return (Frame) p;
			}
		}
		return null;
	}

	public JMenuBar getMenubar() {
		return menubar;
	}
	
	public ApplicationManager getApplicationManager() {
		return mAppManager;
	}

	/**
	 * Show a dialog with the given error message.
	 */
	public void error(String message) {
		JOptionPane.showMessageDialog(this, message, appTitle,
				JOptionPane.ERROR_MESSAGE);
	}

	// --- actions -----------------------------------

	/**
	 * Actions defined by the ClackGraphpad class
	 * This is much reduced from the huge number of actions available in JGraphpad
	 */
	protected Action[] defaultActions = { 
			
			new EditCell(this),new ClackShowConsole(this), new ClackSpawnShell(this),
			new ClackStartEthereal(this), new ClackStopEthereal(this),
			 new EditDelete(this), new ClackToggleRouteTableView(this),
			new EditRedo(this), new EditUndo(this), new FileClose(this),
			new FileExit(this), new FileExportGIF(this),
			new FileExportPNG(this),  new FileNew(this),
			new FileOpen(this),  new FilePrint(this), new FileSave(this),
			new FileConnect(this), new FileOpenURL(this),
			new FileSaveAll(this), new ViewScaleZoomIn(this),
			new ViewScaleZoomOut(this), new ClackRunConnectivityTest(this),
			new ClackAddComponent(this), new ClackClearErrors(this) };

	/**
	 * Returns the current graph, that is, the one in the current document.  
	 * 
	 * @return GPGraph
	 */
	public RouterGraph getCurrentRouterGraph() {
		ClackDocument doc = getCurrentDocument();
		if (doc == null)
			return null;
		return doc.getCurrentRouterGraph();
	}

	/**
	 * Returns the currently active and visible ClackTab.
	 * @return ClackTab
	 */
	public ClackTab getCurrentTab() {
		ClackTab tab = (ClackTab) tabbedPane.getSelectedComponent();
		if (tab == null) {
	
			if (tabbedPane.getTabCount() > 0) {
				tabbedPane.setSelectedIndex(0);
				tab = (ClackTab) tabbedPane.getSelectedComponent();
			}
			
		}
		return tab;
	}

	/**
	 * Returns the currently selected document. If no one is selected, then the
	 * first one will be select.
	 * 
	 * @return ClackDocument
	 */
	public ClackDocument getCurrentDocument() {

		ClackTab tab = getCurrentTab();
		if (tab == null)
			return null;
		return tab.getDocument();
	}

	/**
	 * Returns all of the documents.
	 * 
	 * @return ClackDocument[] or <code>null</code> if no documents
	 */
	public ClackDocument[] getAllDocuments() {

			ArrayList docs = new ArrayList();
			for (Enumeration e = mDocMap.elements(); e.hasMoreElements(); ) {
				docs.add((ClackDocument) e.nextElement());
			}
			return (ClackDocument[]) docs.toArray(new ClackDocument[docs.size()]);
	}

	
    public void componentHidden(ComponentEvent e) {  }

    public void componentMoved(ComponentEvent e) { }

    /**
     * Attempts to update the doc based on a resize of the entire frame.
     * 
     * TODO: This doesn't work in Java 1.5, find out why
     */
    public void componentResized(ComponentEvent e) {
            Component c = e.getComponent();
            
    		ClackDocument doc = getCurrentDocument();
    		if (doc == null)
    			return;
    		doc.gpgraphWindowResized(c.getSize().width, c.getSize().height);       
    }

    public void componentShown(ComponentEvent e) {

    }

	/**
	 * Returns the undoAction.
	 * 
	 * @return UndoAction
	 */
	public AbstractActionDefault getEditUndoAction() {
		return (AbstractActionDefault) currentActionMap.get(Utilities
				.getClassNameWithoutPackage(EditUndo.class));
	}

	/**
	 * Returns the redoAction.
	 * 
	 * @return RedoAction
	 */
	public AbstractActionDefault getEditRedoAction() {
		return (AbstractActionDefault) currentActionMap.get(Utilities
				.getClassNameWithoutPackage(EditRedo.class));
	}

	public GPLogConsole getLogConsole() {
		return logger;
	}

	public boolean isToolBarsVisible() {
		return this.toolBarsVisible;
	}

	public void setToolBarsVisible(boolean state) {
		this.toolBarsVisible = state;

		if (state == true) {
			mainPanel.remove(tabbedPane);
			toolBarInnerPanel.add(BorderLayout.CENTER, tabbedPane);
			mainPanel.add(BorderLayout.CENTER, toolBarMainPanel);
		} else {
			mainPanel.remove(toolBarMainPanel);
			toolBarInnerPanel.remove(tabbedPane);
			mainPanel.add(BorderLayout.CENTER, tabbedPane);
		}
		tabbedPane.repaint();
	}

	/**
	 * Adds a new ClackTab to the ClackGraphpad
	 */

	
	public void addClackTab(ClackTab t) {
		tabbedPane.addTab("Router", t);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
		doc2tab.put(t.getDocument(), t);
	}
	



	public void removeClackTab(ClackTab t) {
		if (t == null)
			return;
		tabbedPane.remove(t);
		doc2tab.remove(t.getDocument());
		t.cleanUp();
		
		if (tabbedPane.getTabCount() > 0) {
			tabbedPane.setSelectedIndex(0);
		}
	}
	
	
	/**
	 * Called on exit by FileExit action
	 *  
	 */
	public void exit(boolean full_exit) {
		topologyManager.closeAllConnections();
		mTimeManager.killMe();
		if (!isApplet() && full_exit) {
			System.exit(0);
		} else {
			getFrame().dispose();
		}
	}

	/**
	 * Adds a new Document based on the GraphModelProvider.
	 *  
	 */
	public boolean isApplet() {
		return (applet != null);
	}


	/**
	 * adds a new document in a ClackTab
	 *  
	 */
	public ClackDocument getDocumentForTopology(int topo){
		ClackDocument document = mDocMap.get(topo);
		if(document == null){
			document = new ClackDocument(this, topo); 
	    	mDocMap.put(topo, document);
			ClackTab newTab = new ClackTab(document, tabbedPane, tabbedPane.getTabCount());

			addClackTab(newTab);
		}
		return document;
	}
	
	/**
	 * Remove a ClackDocument
	 *  
	 */
	public void removeDocument(ClackDocument doc) {
		ClackTab tab = (ClackTab) doc2tab.get(doc);
		removeClackTab(tab);
	}

	public void update() {
		ClackDocument currentDoc = getCurrentDocument();

		Object[] keys = currentActionMap.keys();
		for (int i = 0; i < keys.length; i++) {
			Action a = currentActionMap.get(keys[i]);
			if (a instanceof AbstractActionDefault) {
				((AbstractActionDefault) a).update();
			} else {
				if (currentDoc == null) {
					a.setEnabled(false);
				} else {
					a.setEnabled(true);
				}
			}
		}
		
		if(isSimpleGraph){
			getCurrentActionMap().get("FileOpen").setEnabled(false);
			getCurrentActionMap().get("FilePrint").setEnabled(false);
			getCurrentActionMap().get("FileNew").setEnabled(false);
			getCurrentActionMap().get("FileClose").setEnabled(false);
			getCurrentActionMap().get("FileSave").setEnabled(false);
			getCurrentActionMap().get("FileSaveAs").setEnabled(false);
		}
		
		getCurrentActionMap().get("ClackShowConsole").setEnabled(true);

		if (currentActionMap == defaultActionMap)
			return;

		keys = defaultActionMap.keys();
		for (int i = 0; i < keys.length; i++) {
			Action a = defaultActionMap.get(keys[i]);
			if (a instanceof AbstractActionDefault) {
				((AbstractActionDefault) a).update();
			}
		}
	}

	/**
	 * Returns the barFactory.
	 * 
	 * @return GPBarFactory
	 */
	public ClackBarFactory getBarFactory() {
		return barFactory;
	}

	/**
	 * Sets the barFactory.
	 * 
	 * @param barFactory
	 *            The barFactory to set
	 */
	public void setBarFactory(ClackBarFactory barFactory) {
		this.barFactory = barFactory;
	}
	
	public ErrorConsole getErrorConsole() { return errorConsole; }

	/*
	public JInternalFrame[] getAllFrames() {
		return desktop.getAllFrames();
	}
	*/
	
	public ClackTab[] getAllTabs() {
		return tabbedPane.getAllTabs();
	}

	public void setParentActionMap(ActionMap map) {
		defaultActionMap.setParent(map);
	}

	/**
	 * Returns the applicationIcon.
	 * 
	 * @return ImageIcon
	 */
	public static ImageIcon getApplicationIcon() {
		return applicationIcon;
	}

	/**
	 * Sets the applicationIcon.
	 * 
	 * @param applicationIcon
	 *            The applicationIcon to set
	 */
	public static void setApplicationIcon(ImageIcon applicationIcon) {
		ClackFramework.applicationIcon = applicationIcon;
	}

	/**
	 * Returns the marqueeHandler.
	 * 
	 * @return GPMarqueeHandler
	 */
	public ClackMarqueeHandler getMarqueeHandler() {	return marqueeHandler; }
	
	public TimeManager getTimeManager() { return mTimeManager; }

	public void addTabbedPaneContainerListener(ContainerListener listener) {
		tabbedPane.addContainerListener(listener);
	}

	public void removeTabbedPaneContainerListener(ContainerListener listener) {
		tabbedPane.removeContainerListener(listener);
	}
	
	public boolean isDebug() { return mDebug; }
	public void setDebug(boolean b) { mDebug = b; }
}
