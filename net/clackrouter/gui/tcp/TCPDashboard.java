
package net.clackrouter.gui.tcp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.clackrouter.component.base.ClackComponentEvent;
import net.clackrouter.component.base.ClackComponentListener;
import net.clackrouter.component.tcp.Retransmitter;
import net.clackrouter.component.tcp.TCB;
import net.clackrouter.component.tcp.TCP;
import net.clackrouter.jgraph.utils.BrowserLauncher;
import net.clackrouter.packets.VNSTCPPacket;

/**
 * A half-finished class to provide a "Dashboard" view of a single TCP connection terminated 
 * at a Clack host.  
 * 
 * <p> The dashboard is meant to be embedded in a TCP Hierarchical component, and "synched" to a
 * TCB of the user's choice in order to let them peak inside whatever active connection they want.
 * </p> 
 */

public class TCPDashboard extends JPanel implements ActionListener, ClackComponentListener {

	public static final int BUF_X = 4;
	public static final int BUF_Y = 15;
	public static final int BUF_WIDTH = 20;
	public static final int BUF_HEIGHT = 30;
	public static final int NUM_BUF_SECTIONS = 10;
	
	JButton graphWindowButton;
	JButton webBrowserButton;
	
	VisibleTCPHeader mPacketIn, mPacketOut;
	TCP mTCP;
	JLabel dataRate, windowSize, timeRemaining ;
	DefaultListModel unAckedListModel;
	TCB mCurrentTCB;
	BufferInPanel bufferIn;
	BufferOutPanel bufferOut;
	
	public TCPDashboard(TCP tcp) {
		super(new BorderLayout());
		mTCP = tcp;
		mTCP.registerListener(this);
		setupDashboardCenter();
		mCurrentTCB = null;
		mPacketIn = new VisibleTCPHeader("Most Recent Packet In");
		mPacketOut = new VisibleTCPHeader("Most Recent Packet Out");
		
		add(mPacketIn, BorderLayout.WEST);
		add(mPacketOut, BorderLayout.EAST);
		try {
			Class browserloader = Class.forName("net.clackrouter.jgraph.utils.BrowserLauncher");
			System.out.println("Loaded browser class");
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void setCurrentTCB(TCB tcb){
		if(mCurrentTCB != null)
			mCurrentTCB.unregisterListener(this);
		mCurrentTCB = tcb;
		if(mCurrentTCB != null){
			mCurrentTCB.registerListener(this);
		}
	}
	
	private void setupDashboardCenter() {
		Border blackline = BorderFactory.createLineBorder(Color.black);
		

        TitledBorder border2 = BorderFactory.createTitledBorder("Current Packet Flow");
        border2.setTitleJustification(TitledBorder.CENTER);

        JPanel dash = new JPanel(new BorderLayout());
        dash.setBorder(border2);
        JPanel dashLeft = new JPanel(new BorderLayout());

        JPanel dashTopPanel = new JPanel(new FlowLayout());
        JPanel dashTopLeft = new JPanel();
        dashTopLeft.setLayout(new BoxLayout(dashTopLeft, BoxLayout.Y_AXIS));
        dashTopLeft.setPreferredSize(new Dimension(300, 80));
        
        JPanel dataRateRow = new JPanel();
        JLabel dataRateLbl = new JLabel("Data Rate:");
        dataRate = new JLabel("0");
        dataRateRow.add(dataRateLbl);
        dataRateRow.add(dataRate);
        
        JPanel windowSizeRow = new JPanel();
        JLabel windowSizeLbl = new JLabel("Window Size:");
        windowSize = new JLabel("0");
        windowSizeRow.add(windowSizeLbl);
        windowSizeRow.add(windowSize);
        
        JPanel graphWindowRow = new JPanel();
        graphWindowButton = new JButton("Graph Window");
        graphWindowButton.addActionListener(this);
        graphWindowRow.add(graphWindowButton);
        
        dashTopLeft.add(dataRateRow);
        dashTopLeft.add(windowSizeRow);
        dashTopLeft.add(graphWindowRow);

        JPanel dashTopRight = new JPanel();
        dashTopRight.setLayout(new BoxLayout(dashTopRight, BoxLayout.Y_AXIS));
        dashTopRight.setPreferredSize(new Dimension(300, 80));
        
        JPanel timeRemRow = new JPanel();
        JLabel timeRemLbl = new JLabel("Time Remaining:");
        timeRemaining = new JLabel("0");
        timeRemRow.add(timeRemLbl);
        timeRemRow.add(timeRemaining);
        
        JPanel retransRow = new JPanel();
        JLabel retransLbl = new JLabel("Retransmit");
        retransRow.add(retransLbl);
        
        JPanel webBrowserRow = new JPanel();
        webBrowserButton = new JButton("Web Browser");
        webBrowserButton.addActionListener(this);
        webBrowserRow.add(webBrowserButton);
        
        dashTopRight.add(timeRemRow);
        dashTopRight.add(retransRow);
        dashTopRight.add(webBrowserRow);
        
        dashTopPanel.add(dashTopLeft);
        dashTopPanel.add(dashTopRight);
        
        JPanel buffersPanel = new JPanel(new FlowLayout());
        
        bufferIn = new BufferInPanel();
        bufferIn.setLayout(new BoxLayout(bufferIn, BoxLayout.Y_AXIS));
        bufferIn.setPreferredSize(new Dimension(300, 80));
        JLabel bufferInLbl = new JLabel("SocketBufferIn");
        bufferIn.add(bufferInLbl);
        
        bufferOut = new BufferOutPanel();
        bufferOut.setLayout(new BoxLayout(bufferOut, BoxLayout.Y_AXIS));
        bufferOut.setPreferredSize(new Dimension(300, 80));
        JLabel bufferOutLbl = new JLabel("SocketBufferOut");
        bufferOut.add(bufferOutLbl);
        
        buffersPanel.add(bufferIn);
        buffersPanel.add(bufferOut);
      
        dashLeft.add(dashTopPanel, BorderLayout.NORTH);
        dashLeft.add(buffersPanel, BorderLayout.SOUTH);
        

        unAckedListModel = new DefaultListModel();
        unAckedListModel.addElement("1-200");
        JList unacks = new JList(unAckedListModel);
        unacks.setPreferredSize(new Dimension(80, 80));
        JScrollPane unacksPane = new JScrollPane(unacks, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        JPanel unacksPanel = new JPanel();
        unacksPanel.setLayout(new BoxLayout(unacksPanel, BoxLayout.Y_AXIS));
        JLabel unackLbl = new JLabel("Unacked");
        unacksPanel.add(unackLbl);
        unacksPanel.add(unacksPane);
        
        
        dash.add(dashLeft, BorderLayout.WEST);
        dash.add(unacksPanel, BorderLayout.EAST);	
		add(dash, BorderLayout.CENTER);	
	}

	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == graphWindowButton) {
			//graphWindow button clicked
			System.out.println("graphWindow button clicked");
		}
		else if (e.getSource() == webBrowserButton) {
			try {
				InetAddress addr = mTCP.getRouter().getInputInterfaces()[0].getIPAddress();
				BrowserLauncher.openURL("http://" + addr.getHostAddress());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
				
		}
		
	}
	

	private void updateDashboard(TCB tcb){
		dataRate.setText("0");//tcb.getDataRate();
		windowSize.setText("0"); // tcb.getWindowSize();
		timeRemaining.setText("0"); //tcb.getTimeToTimeout();
		ArrayList unacked = tcb.mRetransmitter.mRetrans;	
		unAckedListModel.clear();
		for(int i = 0; i < unacked.size(); i++){
			Retransmitter.RetransListEntry entry = (Retransmitter.RetransListEntry)unacked.get(i);
			VNSTCPPacket packet = entry.packet;
			String label = packet.getSeqNum() + "(" + packet.getSeqCount() + ")";
			unAckedListModel.addElement(label);
		}
		System.out.println("updating dash " + System.currentTimeMillis());
	}
	
	public void componentEvent(ClackComponentEvent event){
	//	System.out.println("Dashboard got event: " + event);
		if(!(event instanceof TCP.UpdateEvent))
			return;
		
		TCP.UpdateEvent tcpevent = (TCP.UpdateEvent)event;
		if(tcpevent.mType == TCP.UpdateEvent.EVENT_PACKET_IN) {
			mPacketIn.synchToTCPPacket((VNSTCPPacket)tcpevent.mObj);
		}else if(tcpevent.mType == TCP.UpdateEvent.EVENT_PACKET_OUT) {
			mPacketOut.synchToTCPPacket((VNSTCPPacket)tcpevent.mObj);
		}else if(tcpevent.mType == TCP.UpdateEvent.EVENT_DATA_CHANGE){
			updateDashboard((TCB)tcpevent.mObj);
		}
	}
	


	public class BufferInPanel extends JPanel {
		private int numElems = 2;
				
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
        
    		//draws black borders
    		g.setColor(Color.BLACK);
    		for (int i = 0; i < NUM_BUF_SECTIONS; i++) {    		
    			g.drawRect(BUF_X+i*(BUF_WIDTH+1)-1, BUF_Y-1, BUF_WIDTH+1, BUF_HEIGHT+1);
    		}
    		//draws filled, yellow colored elements
    		g.setColor(Color.YELLOW);
    		for (int i = 0; i < numElems; i++) {
    			g.fillRect(BUF_X+i*(BUF_WIDTH+1), BUF_Y, BUF_WIDTH, BUF_HEIGHT);
    		}
    	}
    	
    	//adds 1 to the num of colored buffer elements drawn
    	public void addElem() {
    		numElems++;
    		if (numElems > NUM_BUF_SECTIONS)
    			numElems = NUM_BUF_SECTIONS;
    	}
    	
    	//adds a given number to how many colored buffer elements are drawn
    	public void addElems(int numToAdd) {
    		numElems += numToAdd;
    		if (numElems > NUM_BUF_SECTIONS)
    			numElems = NUM_BUF_SECTIONS;
    	}
    	
    	//removes 1 from num of colored buffer elements drawn
    	public void removeElem() {
    		numElems--;
    		if (numElems < 0)
    			numElems = 0;
    	}
    	
    	//removes a given number from how many colored buffer elements are drawn
    	public void removeElems(int numToRemove) {
    		numElems -= numToRemove;
    		if (numElems < 0)
    			numElems = 0;
    	}
    	
	}

	public class BufferOutPanel extends JPanel {
		private int numElems = 5;
				
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
        
    		g.setColor(Color.BLACK);
    		for (int i = 0; i < NUM_BUF_SECTIONS; i++) {    		
    			g.drawRect(BUF_X+i*(BUF_WIDTH+1)-1, BUF_Y-1, BUF_WIDTH+1, BUF_HEIGHT+1);
    		}
    		
    		g.setColor(Color.YELLOW);
    		for (int i = 0; i < numElems; i++) {
    			g.fillRect(BUF_X+i*(BUF_WIDTH+1), BUF_Y, BUF_WIDTH, BUF_HEIGHT);
    		}
    	}
    	
    	//adds 1 to the num of colored buffer elements drawn
    	public void addElem() {
    		numElems++;
    		if (numElems > NUM_BUF_SECTIONS)
    			numElems = NUM_BUF_SECTIONS;
    	}
    	
    	//adds a given number to how many colored buffer elements are drawn
    	public void addElems(int numToAdd) {
    		numElems += numToAdd;
    		if (numElems > NUM_BUF_SECTIONS)
    			numElems = NUM_BUF_SECTIONS;
    	}
    	
    	//removes 1 from num of colored buffer elements drawn
    	public void removeElem() {
    		numElems--;
    		if (numElems < 0)
    			numElems = 0;
    	}
    	
    	//removes a given number from how many colored buffer elements are drawn
    	public void removeElems(int numToRemove) {
    		numElems -= numToRemove;
    		if (numElems < 0)
    			numElems = 0;
    	}    	
	}
	
}
