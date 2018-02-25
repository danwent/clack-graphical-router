/*
 * Created on Jan 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.propertyview;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.clackrouter.component.extension.TCPMonitor;
import net.clackrouter.gui.tcp.TCPStateAnalyzer;





/**
 * @author Dan
 *
 * This class is currently broken and outdated, do NOT use it.  
 * 
 * <p> It needs to be updated before it is 
 * functional again, but was designed to display information about TCP flows going through
 * a router, as reported by a TCPMonitor component.  <p>
 */
public class TCPMonitorPopup extends DefaultPropertiesView implements ActionListener {

		private static final String SERVER = "Server";
		private static final String CLIENT = "Client";
	
	
	    private DefaultListModel sampleModel = null;
	    protected Hashtable mTCPPanels;
	    protected JPanel m_flow_panel;
	    protected JPanel mFlowPanel;
	    protected JPanel mRadioPanel;
	    private Hashtable mButtonToDataMap;

	    
	    public TCPMonitorPopup(TCPMonitor comp) {
	    	super(comp.getName(), comp);
			setSize(new Dimension(700, 700));
	    	mButtonToDataMap = new Hashtable();
	    	mTCPPanels = new Hashtable();
	    	m_flow_panel = new JPanel();
	    	m_flow_panel.setMaximumSize(new Dimension(500, 500));
	    	m_flow_panel.setLayout(new BorderLayout());
	    	JScrollPane flow_scroll = new JScrollPane(m_flow_panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	    			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    	flow_scroll.setMaximumSize(new Dimension(500, 500));
	    	mButtonToDataMap = new Hashtable();
	        addMainPanel("TCPMonitor");

	        updatePropertiesFrame();


	        mFlowPanel = new JPanel();
	        mFlowPanel.setMaximumSize(new Dimension (500, 500));
	        mRadioPanel = new JPanel();
	        mRadioPanel.setMaximumSize(new Dimension (500, 500));
	    	mRadioPanel.setLayout(new BoxLayout(mRadioPanel, BoxLayout.Y_AXIS));
	    	m_flow_panel.add(mRadioPanel, BorderLayout.NORTH);
	    	m_flow_panel.add(mFlowPanel, BorderLayout.SOUTH);

	        refreshRadioOptions();
	        

	        addHTMLDescription("TCPMonitor.html");
	        addPanelToTabPane("Ports", m_port_panel);
	        refreshPortTab();
	        addPanelToTabPane("Flows",flow_scroll);
	        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
	    }
	    
	    private void refreshRadioOptions() {
	    	System.out.println("refreshing radio options");
	        // add all flows that already exist
	        Hashtable flow_map = ((TCPMonitor)m_model).getFlowMap();
	       
	        mRadioPanel.removeAll();
	        ButtonGroup group = new ButtonGroup();
	        for(Enumeration e = flow_map.keys(); e.hasMoreElements();){
	        	String key = ((String) e.nextElement());
	        	TCPMonitor.TCPFlow flow = (TCPMonitor.TCPFlow) flow_map.get(key);
	        	JRadioButton button = new JRadioButton(key);
	        	group.add(button);
	        	button.addActionListener(this);
	        	mRadioPanel.add(button);
	        }	
	        mRadioPanel.repaint();

	    }
	    
	    public void addTCPFlow(TCPMonitor.TCPFlow flow){
	    	refreshRadioOptions();
	    }
	    
	    public void removeTCPFlow(TCPMonitor.TCPFlow flow){
	    	refreshRadioOptions();
	    }

	    public void actionPerformed(ActionEvent event) {
	    	String key = (String)event.getActionCommand();
	    	if(key == null) {
	    		return;
	    	}
	    	Hashtable flow_map = ((TCPMonitor)m_model).getFlowMap();
	    	TCPMonitor.TCPFlow flow = (TCPMonitor.TCPFlow) flow_map.get(key);
	    	if(flow == null){
	    		System.err.println("Could not find flow for key: " + key);
	    		return;
	    	}
	    		
	    	mFlowPanel.removeAll();
	    	mFlowPanel.add(new TCPFlowPanel(flow));
	    	mFlowPanel.repaint();
	    }
	    
	    public static String getLastPacketString(TCPStateAnalyzer a){

	        StringBuffer b = new StringBuffer(40);
	        b.append("Total Bytes: " + a.getLastPacketSize() + "\n");
	        b.append("Total Data Bytes: " + a.getLastDataSize() + "\n");
	        b.append("Flags: " + a.getControlString() + "\n");
	        b.append("SEQ Num: " + a.seq_num + "\n");
	        b.append("Ack Num: " + a.ack_num + "\n");
	        return b.toString();
	    }
	    
	    public static String getFlowString(TCPStateAnalyzer a){
	        StringBuffer b = new StringBuffer(40);
	        b.append("Port: " + a.getPort() + "\n");
	        b.append("Role: " + a.getRole() + "\n");
	        b.append("Packets Sent: " + a.getPacketsSent() + "\n");
	        b.append("Packets Resent: " + a.getDuplicateSends() + "\n");
	        b.append("Bytes Sent and Acked: " + a.getTotalBytesAcked() + "\n"); 
	        return b.toString();
	    }
	    
	    private class TCPFlowPanel extends JPanel implements TCPMonitor.TCPFlowListener {
	    	private JLabel a_out, b_out;
	    	private JLabel a_port;
	    	private JTextArea a_state, b_state, a_flow, b_flow, a_last, b_last;
	    	private TCPMonitor.TCPFlow mFlow;
		    private JButton mShowA, mShowB;
		    private JPanel aPanel, bPanel;
		    private JScrollPane a_flow_scroll, b_flow_scroll, a_last_scroll, b_last_scroll, 
								a_state_scroll, b_state_scroll;
		    
		    
	    	public TCPFlowPanel(TCPMonitor.TCPFlow f)  {
	    		super();
	    		
	    		
	    		String a_role = f.mStateA.getRole();
	    		
	    		setLayout(new BorderLayout());
	    		setMaximumSize(new Dimension(600, 600));
				
	    		aPanel = new JPanel();
		    	aPanel.setLayout(new BoxLayout(aPanel, BoxLayout.Y_AXIS));
		    	aPanel.setMaximumSize(new Dimension(500, 600));
	    		bPanel = new JPanel();
	    		bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.Y_AXIS));
	    		bPanel.setMaximumSize(new Dimension(500, 600));
	    		
	    		if (a_role.equals(SERVER)) {
		    		add(aPanel, BorderLayout.EAST);
		    		add(bPanel, BorderLayout.WEST);
	    		}
	    		else {
		    		add(bPanel, BorderLayout.EAST);
		    		add(aPanel, BorderLayout.WEST);
	    		}	    			

		    		    	
	    		addBorderToPanel(aPanel, "Host: " + f.mStateA.getAddress());
	    		addBorderToPanel(bPanel, "Host: " + f.mStateB.getAddress());
		    	mShowA = new JButton("Show Bytes Outstanding Graph");
		    	mShowA.addActionListener(new ShowChart());
		    	mShowB = new JButton("Show Bytes Outstanding Graph");
		    	mShowB.addActionListener(new ShowChart());	
		    	
		    	JLabel imgClient = new JLabel(createImageIcon("client.gif", 100, 100));
		    	aPanel.add(imgClient);
		    	
	    		aPanel.add(new JLabel("Flow Information:"));
	    		
	    		a_flow = new JTextArea();
	    		a_flow.setMaximumSize(new Dimension(500,300));
	    		a_flow.setLineWrap(true);
	    		a_flow.setWrapStyleWord(true);
	    		a_flow_scroll = new JScrollPane(a_flow);
	    		a_flow_scroll.setMaximumSize(new Dimension(500, 300));
	    		
	    		aPanel.add(a_flow_scroll);
	    		aPanel.add(new JLabel("Last Packet:"));
	    		
	    		a_last = new JTextArea();
	    		a_last.setMaximumSize(new Dimension(500,300));
	    		a_last.setLineWrap(true);
	    		a_last.setWrapStyleWord(true);
	    		
	    		a_last_scroll = new JScrollPane(a_last, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    		a_last_scroll.setMaximumSize(new Dimension(500, 300));
	    		
	    		aPanel.add(a_last_scroll);
	    		aPanel.add(new JLabel("State Information: "));
	    		a_state = new JTextArea();
	    		a_state.setMaximumSize(new Dimension(500, 300));
	    		a_state.setLineWrap(true);
	    		a_state.setWrapStyleWord(true);
	    		
	    		a_state_scroll = new JScrollPane(a_state, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    		a_state_scroll.setMaximumSize(new Dimension(500, 300));
	    		aPanel.add(a_state_scroll);
	    		aPanel.add(a_out = new JLabel(""));
	    		aPanel.add(mShowA);
	    		
	    		JLabel imgServer = new JLabel(createImageIcon("server.gif", 100, 100));
		    	bPanel.add(imgServer);
	    		
	    		bPanel.add(new JLabel("Flow Information:"));
	    		
	    		b_flow = new JTextArea();
	    		b_flow.setMaximumSize(new Dimension(500, 300));
	    		b_flow.setLineWrap(true);
	    		b_flow.setWrapStyleWord(true);
	    		b_flow_scroll = new JScrollPane(b_flow, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    		b_flow_scroll.setMaximumSize(new Dimension(500, 300));
	    		
	    		bPanel.add(b_flow_scroll);
	    		bPanel.add(new JLabel("Last Packet:"));
	    		
	    		b_last = new JTextArea();
	    		b_last.setMaximumSize(new Dimension(500, 300));
	    		b_last.setLineWrap(true);
	    		b_last.setWrapStyleWord(true);
	    		b_last_scroll = new JScrollPane(b_last, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    		b_last_scroll.setMaximumSize(new Dimension(500, 300));
	    		
	    		bPanel.add(b_last_scroll);
	    		bPanel.add(new JLabel("State Information: "));
	    		b_state = new JTextArea();
	    		b_state.setMaximumSize(new Dimension(500, 300));
	    		b_state.setLineWrap(true);
	    		b_state.setWrapStyleWord(true);
	    		b_state_scroll = new JScrollPane(b_state, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    		b_state_scroll.setMaximumSize(new Dimension(500, 300));
	    		
	    		bPanel.add(b_state_scroll);
	    		bPanel.add(b_out = new JLabel(""));
	    		bPanel.add(mShowB);
	    		mFlow = f;

//	    		mButtonToDataMap.put(mShowA, mFlow.mStateA.mFlowOccData);
//	    		mButtonToDataMap.put(mShowB, mFlow.mStateB.mFlowOccData);
	    		mFlow.registerListener(this);
		    	flowUpdated();
	    	}
	    	
	    	/** Returns an ImageIcon, or null if the path was invalid. */
	    	protected ImageIcon createImageIcon(String path, int width, int height) {
	    		java.net.URL imgURL = TCPFlowPanel.class.getResource(path);
	    		if (imgURL != null) {
	    			ImageIcon tempIcon = new ImageIcon(imgURL);
	    			return new ImageIcon(tempIcon.getImage().getScaledInstance(width,height,Image.SCALE_DEFAULT));
	    		} else {
	    			JOptionPane.showMessageDialog(null, "Couldn't find file: "+path, "File Not Found", JOptionPane.ERROR_MESSAGE);
	    			//System.err.println("Couldn't find file: " + path);
	    			return null;
	    		}
	    	}	
	    	
	    	public void flowUpdated(){
	    		TCPStateAnalyzer aState = mFlow.mStateA;
	    		TCPStateAnalyzer bState = mFlow.mStateB;
	    		a_flow.setText(getFlowString(mFlow.mStateA));
	    		b_flow.setText(getFlowString(mFlow.mStateB));
	    		a_out.setText("Data outstanding sent by A : " + mFlow.mStateA.getBytesOutStanding() + " bytes");
	    		b_out.setText("Data outstanding sent by B " + mFlow.mStateB.getBytesOutStanding() + " bytes");
	    		a_state.setText(mFlow.mStateA.getHistory());
	    		b_state.setText(mFlow.mStateB.getHistory());
	    		a_last.setText(getLastPacketString(mFlow.mStateA));
	    		b_last.setText(getLastPacketString(mFlow.mStateB));
	    		repaint();
	    	}
	    	


	    }
	    
	    private class ShowChart implements ActionListener 
	    {
	        public void actionPerformed(ActionEvent event) 
	        {
	      
	/*            
	            JButton button = (JButton)event.getSource();
	            SingleSeriesOccData data = (SingleSeriesOccData) mButtonToDataMap.get(button);
	            
	            	
	            	ClackOccChart time_frame = new ClackOccChart(data, "TCP Flow Graph", "TCP Flow Data Outstanding", "Time (milliseconds)", "Data Outstanding (bytes)");
	                time_frame.pack();
	                RefineryUtilities.centerFrameOnScreen(time_frame);
	                time_frame.setVisible(true);
	 
	 */           
	        }
	    }

	
}
