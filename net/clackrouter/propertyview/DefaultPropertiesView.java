
package net.clackrouter.propertyview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackComponentListener;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.ComponentDataHandler;
import net.clackrouter.component.base.ComponentVizLauncher;
import net.clackrouter.component.extension.Counter;
import net.clackrouter.error.ErrorReporter;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;
import net.clackrouter.test.ErrorChecker;




/**
 * Default class to handle functionality commonly implemented by Property Views for 
 * Clack components.
 * 
 * <p>  A DefaultPropertiesView can be used to create a default view, if the view
 * represents just a vanilla component, or it can be used as a parent class for 
 * more compmlex component popops, like {@link IPRouteLookupPopup}.  </p>
 */
public class DefaultPropertiesView extends JPanel implements ClackComponentListener , ActionListener {

	    protected JTabbedPane  m_tab_pane;
	    protected JPanel m_port_panel;
	    protected ClackComponent m_model;
	    protected JLabel mPacketsIn, mPacketsOut;
	    
	    private DataHandlerTable handlerTable; 
	    private JScrollPane handlerScroll;
	    
	    public static String DESCR_PATH_ROOT = "/net/clackrouter/descr/";

	    public DefaultPropertiesView(String name, ClackComponent model)
	    { 
	        super(); 
	   
	     //   setSize(new Dimension(350, 420));
	        m_model = model;
	        m_model.registerListener(this);
	        m_tab_pane = new JTabbedPane(JTabbedPane.BOTTOM);
	        m_tab_pane.setPreferredSize(new Dimension(100, 100));
	        m_port_panel = new JPanel();
	        refreshPortTab(); 
	        
	        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));		
	        add(m_tab_pane);
	        mPacketsIn  = new JLabel("Input: 0");
	        mPacketsOut = new JLabel("Output: 0");

	    } // -- constructor (..)
	    
	    // this is the constructor for when a component is not sub-classing
	    // DefaultPopup and is instead using only the functionality DefaultPopup provides
	    public DefaultPropertiesView(String name, ClackComponent model, String class_name){
	    	this(name, model);
	        addMainPanel(class_name);
	        addHTMLDescription(class_name + ".html");
	        addPanelToTabPane("Ports", m_port_panel);
	        addPanelToTabPane("Log", new JScrollPane(model.getLog()));
	        refreshPortTab();
	        updatePropertiesFrame();
	    }

	    public synchronized void addPanelToTabPane(String name, Component panel_in)
	    {
	        m_tab_pane.addTab(name, null, panel_in, "");
	    } // -- addPanelToTabPane(..)
	    
	    public synchronized void removePanelFromTabPane(JPanel panel_out){
	    	m_tab_pane.remove(panel_out);
	    }

	    protected JPanel addMainPanel(String name)
	    {
	        JPanel content            = new JPanel();

	        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));		

	        JPanel name_panel = new JPanel();
	        JPanel packets_panel = new JPanel();
	       

	        content.add(name_panel);
	        content.add(packets_panel);
	        
	    	if(m_model instanceof ComponentDataHandler){

	    		handlerScroll = new JScrollPane();
	    		content.add(handlerScroll);
	    		refreshDataHandlerTable();

	    	}
	    	
	    	if(m_model instanceof ComponentVizLauncher){
	    		JPanel launcher_panel = new JPanel();
	    		ComponentVizLauncher l = (ComponentVizLauncher)m_model;
	    		String[] names = l.getLauncherStrings();
	    		for(int i = 0; i < names.length; i++){
	    			JButton b = new JButton(names[i]);
	    			b.addActionListener(this);
	    			launcher_panel.add(b);
	    		}
	    		content.add(launcher_panel);
	    	}

	        name_panel.setMaximumSize(new Dimension(280,55));
	        packets_panel.setMaximumSize(new Dimension(280,55));
	        
	        addBlankBorderToPanel(name_panel, "");
	        addBorderToPanel(packets_panel, "Packets Processed");

	        JLabel header_label = new JLabel(name);
	        header_label.setFont(new Font("Serif", Font.BOLD, 20));
	        name_panel.add(header_label);

	        packets_panel.add(mPacketsIn, BorderLayout.WEST);
	        packets_panel.add(new Box.Filler(new Dimension(40,20),
	                                    new Dimension(40,20), 
	                                    new Dimension(40,20)));
	        packets_panel.add(mPacketsOut, BorderLayout.EAST);
	        
	        addPanelToTabPane("Properties", content);
	        return content;
	    }

	    public synchronized JScrollPane addHTMLDescription(String urlname)
	    {
	        //Create an editor pane.
	    	URL url = DefaultPropertiesView.class.getResource(DESCR_PATH_ROOT + urlname) ;
	        JEditorPane editorPane = createEditorPane(url);
	        JScrollPane editorScrollPane = new JScrollPane(editorPane);
	        editorScrollPane.setVerticalScrollBarPolicy(
	                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	        editorScrollPane.setPreferredSize(new Dimension(250, 300));
	        editorScrollPane.setMinimumSize(new Dimension(10, 10));


	        addPanelToTabPane("Description",editorScrollPane);
	        return editorScrollPane;
	    } // -- addDescription

	    private JEditorPane createEditorPane(URL url) 
	    {
	    	try {
	    		JEditorPane editorPane = new JEditorPane(url);
	            editorPane.setEditable(false);
	            return editorPane;
	    	} catch (IOException e) {
	    		/* ignore, a correct url may not exist */
	        }
	    	
	    	try {
	        	URL errorurl = DefaultPropertiesView.class.getResource(DESCR_PATH_ROOT + "error.html") ;
	    		JEditorPane editorPane = new JEditorPane(errorurl);
	            editorPane.setEditable(false);
	            return editorPane;
	    	} catch (IOException e) {
				e.printStackTrace();
				ErrorReporter.reportError(m_model.getRouter().getDocument().getFramework(), "Attempted to read a bad Default ERROR URL: " + e.getMessage());
	        }

	            return null; // todo: should return an error
	    } // -- createEditorPane


	    private void refreshDataHandlerTable() {
	    	
	    	if(!(m_model instanceof ComponentDataHandler))return;
	    	
	    	// just the data please!!
	    	ComponentDataHandler h = (ComponentDataHandler)m_model;
	        String[] col_names = new String[]{"Label", "Value" };

	        Properties props = h.getReadHandlerValues(); 
	        int num_entries = props.size();
	        Object[][] data = new Object[num_entries][col_names.length];

	        int i = 0;
	        for ( Enumeration e = props.keys(); e.hasMoreElements(); i++){ 
	        	String key = (String)e.nextElement();
	        	String value = props.getProperty(key);
	        	data[i][0] = key;
	        	data[i][1] = value;
	        }

	        JTable oldtable = handlerTable;
	        handlerTable = new DataHandlerTable(data, h);

	        if(handlerScroll != null ) {
	        	if(oldtable != null)
	        		handlerScroll.remove(oldtable);
	        	handlerScroll.setViewportView(handlerTable);
	        }

	        handlerScroll.setPreferredSize(new Dimension(350, i * 5));
	        this.repaint();		
	    }
	    
	    public void actionPerformed(ActionEvent e){
	    		// assume this is from a launcher..
	    		// subclasses should handle anything else
	    	
	    		if(!(m_model instanceof ComponentVizLauncher)) return;
	    		
	    		ComponentVizLauncher l = (ComponentVizLauncher)m_model;
	    		l.launch(e.getActionCommand());
	    	
	    }
	    
	    
	    
	    public synchronized void refreshPortTab() {
	    	
	    	m_port_panel.removeAll();
	    	
	    	m_port_panel.setLayout(new BoxLayout(m_port_panel, BoxLayout.Y_AXIS));
	        for(int i = 0; i < m_model.getNumPorts(); i++){
	        	try {
	            JPanel singlePortPanel   = new JPanel(); 
	            singlePortPanel.setLayout(new BoxLayout(singlePortPanel, BoxLayout.Y_AXIS));
	        	ClackPort p = m_model.getPort(i);
	            addBorderToPanel(singlePortPanel, "Port " + i + ": " + p.getTextDescription());
	            JTextPane textPane = new JTextPane();
	            textPane.setBackground(null);
	            StyledDocument doc = (StyledDocument)textPane.getDocument();
	            Style style = doc.addStyle("StyleName", null);
	            StyleConstants.setFontFamily(style, "SansSerif");
	            StyleConstants.setBold(style, true);           
	            appendBold(doc,"Type: ", style);
	            appendNormal(doc,ErrorChecker.getClassNameOnly(p.getType()) + "\n", style);
				appendBold(doc, "Direction: ", style);
				appendNormal(doc, p.getDirString() + "\n", style);
				appendBold(doc, "Method: ", style);
				appendNormal(doc, p.getMethodString() + "\n", style);
	            appendBold(doc, "Connected To:", style);	
	        	int numConnected = p.getNumConnectedPorts();
	        	if(numConnected == 0){
	        		appendNormal(doc,"\nNone", style);
	        	}else {
	        		for(int j = 0; j < numConnected; j++){
	        			ClackPort remote = p.getConnectedPort(j);
	        			String descr = "\nPort " + remote.getPortNumber() + " on " + remote.getOwner().getName()
							+ " (" + remote.getTextDescription() + ")";
	        			appendNormal(doc, descr, style);
	        		}
	        	}
	        	singlePortPanel.add(textPane);

	        	m_port_panel.add(singlePortPanel);
	        	} catch (Exception e){
	        		e.printStackTrace();
	        	}
	    
	        	
	        }


	    	
	    }
	    
	    private void appendNormal(StyledDocument doc, String text, Style style) throws Exception {
	    	StyleConstants.setBold(style, false);
	    	doc.insertString(doc.getLength(),text, style);
	    }
	    private void appendBold(StyledDocument doc, String text, Style style) throws Exception {
	    	StyleConstants.setBold(style, true);
	    	doc.insertString(doc.getLength(),text, style);
	    }
		
	    // method used in the creation of properties frames, included with GUI_Component
	    // b/c many subclasses will use it.
	    protected void addBorderToPanel(JPanel panel, String title) { 
	        panel.setBorder(BorderFactory.createTitledBorder(
	                        BorderFactory.createEtchedBorder(), title, 
	                        TitledBorder.TOP, TitledBorder.CENTER)); 
	    }
	    
	    protected void addBlankBorderToPanel(JPanel panel, String title) { 
	        panel.setBorder(BorderFactory.createTitledBorder(
	                        BorderFactory.createEmptyBorder(), title, 
	                        TitledBorder.TOP, TitledBorder.CENTER)); 
	    }
		
	    /**
	     * updates the values displayed in the current Properties frame.
	     * right now just puts in new values for packets in/packets out
	     */
	    protected void updatePropertiesFrame() 
	    {
	    	// DW: sometimes this gets called b/4 they are initialized??
	    	if(mPacketsOut == null && mPacketsIn == null)  return;
	    	if(m_model == null)return;
	    	
	        mPacketsOut.setText("Output: "+ m_model.getPacketCountOut());
	        mPacketsIn.setText("Input: "+ m_model.getPacketCountIn());
	    } // -- updatePropertiesFrame(..)

	    public void componentEvent(ClackComponentEvent event) {
	        if( event.mType == ClackComponentEvent.EVENT_CONNECTION_CHANGE){
	        	refreshPortTab();
	        } else if (event.mType == ClackComponentEvent.EVENT_DATA_CHANGE) {
	        	refreshDataHandlerTable();
	        } else {
	        	updatePropertiesFrame(); 
	        }
	    }
}
