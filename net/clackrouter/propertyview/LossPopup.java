/*
 * Created on Feb 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.propertyview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.clackrouter.component.extension.Loss;


/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LossPopup extends DefaultPropertiesView implements ChangeListener  {
	
    private JSpinner m_loss_spinner;
    private JLabel m_drops;
    private JButton mDropNext;
    private int mOldTotalDrops;
    
	public LossPopup(Loss loss){
		super(loss.getName(), loss);
		SpinnerModel model =
	        new SpinnerNumberModel((double)loss.getLoss(), //initial value
	                               0, //min
	                               100, //max
	                               .1);                //step
       
        m_loss_spinner = new JSpinner(model);
		m_loss_spinner.addChangeListener(this);
		m_loss_spinner.setMaximumSize(new Dimension(40, 30));
    	m_drops = new JLabel("Total Dropped = 0");
    	mDropNext = new JButton("Drop Next Packet");
    	mDropNext.addActionListener(new DropNextActionListener());
    	JPanel main = addMainPanel("Loss");
        addHTMLDescription("Loss.html");
        main.add(createConfigPanel());
        addPanelToTabPane("Ports", m_port_panel);
        refreshPortTab();
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        updatePropertiesFrame();
	}
   
    public void stateChanged(ChangeEvent e) {
        SpinnerNumberModel numberModel = (SpinnerNumberModel)m_loss_spinner.getModel();
        Loss loss = (Loss)m_model;
        loss.setLoss((int) ((double)10.0 * numberModel.getNumber().doubleValue()));
    }
    
    public void updatePropertiesFrame() {
    	super.updatePropertiesFrame();
    	int newTotalDrops = ((Loss)m_model).getDrops();
    	if(mOldTotalDrops != newTotalDrops)
    		mDropNext.setEnabled(true);
    	m_drops.setText("Total Dropped = " + newTotalDrops);
    	mOldTotalDrops = newTotalDrops;
    }
    
    private JPanel createConfigPanel() {
    	JPanel config = new JPanel();
    //	config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
    	JLabel msg = new JLabel("Set Percentage of Packets to Drop");
    	config.add(msg);
    	config.add(m_loss_spinner);
    	config.add(m_drops);
    	config.add(mDropNext);
    	addBorderToPanel(config, "Configure");
    	return config;
    }
    
    private class DropNextActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		Loss loss = (Loss)m_model;
    		loss.dropNextPacket();
    		mDropNext.setEnabled(false);
    	}
    }

}
