/*
 * Created on Jan 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.propertyview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.event.*;

import net.clackrouter.component.extension.*;



/**
 * @author Dan
 *
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ThrottlePView extends DefaultPropertiesView implements ChangeListener {

    private JSpinner m_delay_spinner;
    
	public ThrottlePView(Throttle delay){
		super(delay.getName(), delay);
		SpinnerModel model =
	        new SpinnerNumberModel(delay.getDelay(), //initial value
	                               0, //min
	                               100, //max
	                               5);                //step
       
        m_delay_spinner = new JSpinner(model);
		m_delay_spinner.addChangeListener(this);
		m_delay_spinner.setMaximumSize(new Dimension(40, 30));
    	JPanel main = addMainPanel("Throttle");
        addHTMLDescription("Throttle.html");
        main.add(createConfigPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
	}
   
    public void stateChanged(ChangeEvent e) {
        SpinnerNumberModel numberModel = (SpinnerNumberModel)m_delay_spinner.getModel();
        Throttle delay = (Throttle)m_model;
        delay.setDelay(numberModel.getNumber().intValue());
    }
    
    private JPanel createConfigPanel() {
    	JPanel config = new JPanel();
    //	config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
    	JLabel msg = new JLabel("Throttle packet rate to this");
    	JLabel msg2 = new JLabel("percentage of the max rate");
    	config.add(msg);
    	config.add(Box.createHorizontalStrut(4));
    	config.add(msg2);
    	config.add(Box.createHorizontalStrut(4));
    	config.add(m_delay_spinner);
    	addBorderToPanel(config, "Configure");
    	config.setMaximumSize(new Dimension(330, 95));
    	return config;
    }

	
}
