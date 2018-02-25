/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.propertyview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;

import javax.swing.*;

import net.clackrouter.component.extension.Counter;
import net.clackrouter.component.extension.Throttle;


/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CounterPopup extends DefaultPropertiesView implements ActionListener {
    public JButton reset;
	public JButton setLabel;
	public JTextField myLabel;
	
	public CounterPopup(Counter c){
		super(c.getName(), c);
    	JPanel main = addMainPanel("Counter");
        addHTMLDescription("Counter.html");
        main.add(createConfigPanel(c));
        main.add(new JPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
	}
   
    public void actionPerformed(ActionEvent e){
    	if (e.getSource() == reset)
    		((Counter)m_model).resetCounter();
    	else if (e.getSource() == setLabel) {
    		((Counter)m_model).setCLabel(myLabel.getText());
    		//((Counter)m_model).setLabel(myLabel.getText());
    		((Counter)m_model).getCounterView().clackPaint();
    	}
    }
    
    private JPanel createConfigPanel(Counter c) {
    	JPanel config = new JPanel();
    	
    	config.add(new JLabel("Label: "));
    	myLabel = new JTextField(c.getCLabel());
    	myLabel.setPreferredSize(new Dimension(150, 30));
    	config.add(myLabel);
    	setLabel = new JButton("Change Label");
    	setLabel.setPreferredSize(new Dimension(120, 30));
    	setLabel.addActionListener(this);
    	config.add(setLabel);
    	
		reset = new JButton("Reset Counter");
		reset.setPreferredSize(new Dimension(120, 30));
		reset.addActionListener(this);
    	config.add(reset);
    	//config.setMaximumSize(new Dimension(300,280));
    	addBorderToPanel(config, "Configure");
    	
    	return config;
    }
    

}
