/*
 * Created on Feb 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.propertyview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.clackrouter.component.extension.Capture;

/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CapturePopup extends DefaultPropertiesView implements ActionListener {
	
	JTextField mSaveFile;
	JButton mSaveButton, mStartButton, mStopButton;
	
	public CapturePopup(Capture c){
		super(c.getName(), c);

    	JPanel main = addMainPanel("Capture");
        addHTMLDescription("Capture.html");
        main.add(createConfigPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
	}
   
    public void actionPerformed(ActionEvent e){
    	Capture capture = (Capture)m_model;
    	String cmd = e.getActionCommand();
    	if(cmd.equals("Stop")) capture.stopCapture();
    	if(cmd.equals("Start")) capture.startCapture();
    	if(cmd.equals("Save")){
    		try {
    			File f = new File(mSaveFile.getText());
    			System.out.println("Saving file to " + f.getAbsolutePath());
    			capture.saveToFile(f.getAbsolutePath());
    		}catch (Exception ex){
    			ex.printStackTrace();
				JOptionPane.showMessageDialog(m_model.getRouter().getDocument().getFramework(),"Could not save capture to file: " + mSaveFile.getText(),
						"Error", JOptionPane.ERROR_MESSAGE);
    		}
    	}
    }
    
    private JPanel createConfigPanel() {
    	JPanel config = new JPanel();
    	config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
    	mSaveFile = new JTextField("save.pcap", 20);
    	config.add(new JLabel("Save File:"));
    	config.add(mSaveFile);
		mStartButton = new JButton("Start");
		mStopButton = new JButton("Stop");
		mSaveButton = new JButton("Save");
		config.add(mStartButton); config.add(mStopButton); config.add(mSaveButton);
		mStartButton.addActionListener(this); mStopButton.addActionListener(this); mSaveButton.addActionListener(this);
    	
    	addBorderToPanel(config, "Packet Capture Controls");
    	return config;
    }
    
}
