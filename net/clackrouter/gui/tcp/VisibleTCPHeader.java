
package net.clackrouter.gui.tcp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.clackrouter.packets.VNSTCPPacket;

/**
 * A simple object to graphically show the contents of a TCP header. 
 */
public class VisibleTCPHeader extends JPanel  {

	protected JLabel src_port, dst_port, seq_num, ack_num, offset, flags, win;
	
	public VisibleTCPHeader(String name) {

		Border blackline = BorderFactory.createLineBorder(Color.black);
        TitledBorder border = BorderFactory.createTitledBorder(name);
        border.setTitleJustification(TitledBorder.CENTER);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(border);
		setPreferredSize(new Dimension(200, 200));
    
		JPanel row1 = new JPanel(new GridLayout(1,2));
		src_port = new JLabel("");
		src_port.setBorder(blackline);
		row1.add(src_port);
		dst_port = new JLabel("");
		dst_port.setBorder(blackline);
		row1.add(dst_port);
		JPanel row2 = new JPanel(new GridLayout(1,1));
		seq_num = new JLabel("");
		seq_num.setBorder(blackline);
		row2.add(seq_num);
		JPanel row3 = new JPanel(new GridLayout(1,1));
		ack_num = new JLabel("");
		ack_num.setBorder(blackline);
		row3.add(ack_num);
		JPanel row4 = new JPanel(new GridLayout(1,3));
		offset = new JLabel("");
		offset.setBorder(blackline);
		row4.add(offset);
		flags = new JLabel("");
		flags.setBorder(blackline);
		row4.add(flags);
		win = new JLabel("");
		win.setBorder(blackline);
		row4.add(win);        
    
		add(row1);
		add(row2);
		add(row3);
		add(row4);
	}
	
	public void synchToTCPPacket(VNSTCPPacket packet){
		src_port.setText("SRC: " + packet.getSourcePort());
		dst_port.setText("DST: " + packet.getDestinationPort());
		seq_num.setText("SEQ: " + packet.getSeqNum());
		ack_num.setText("ACK: " + packet.getAckNum());
		offset.setText("SIZE: " + packet.getDataSize());
		
		String flagsStr = "";
		if(packet.synFlagSet()) flagsStr += "SYN ";
		if(packet.finFlagSet()) flagsStr += "FIN ";
		if(packet.ackFlagSet()) flagsStr += "ACK";
		flags.setText(flagsStr);
		win.setText("WIN: " + packet.getRecvWindowSize());
		
		
	}
	
	
	
}
