package net.clackrouter.propertyview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.clackrouter.ethereal.Ethereal;

public class EtherealPopup extends DefaultPropertiesView implements ActionListener {

    public JButton setName, showWindow;
    public JTextField name;
    
	public EtherealPopup(Ethereal model) {
		super(model.getName(), model);

    	JPanel main = addMainPanel("Ethereal");

        main.add(createConfigPanel(model));
        main.add(new JPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
		// TODO Auto-generated constructor stub
	}
	

    public void actionPerformed(ActionEvent e){
    	if (e.getSource() == showWindow)
    		((Ethereal)m_model).showWindow();
    	else if (e.getSource() == setName) {
    		((Ethereal)m_model).setName("Ethereal " + name.getText());
    	}
    }

    private JPanel createConfigPanel(Ethereal ether) {
    	JPanel config = new JPanel();
    	
    	config.add(new JLabel("Compnent Name: Ethereal <your name>"));
    	name = new JTextField(ether.getName().substring("Ethereal".length() + 1));
    	name.setPreferredSize(new Dimension(150, 30));
    	config.add(name);
    	setName = new JButton("Change Name");
    	setName.setPreferredSize(new Dimension(120, 30));
    	setName.addActionListener(this);
    	config.add(setName);
    	showWindow = new JButton("Show Window");
    	showWindow.setPreferredSize(new Dimension(150, 30));
    	showWindow.addActionListener(this);
    	config.add(showWindow);
    	
    	addBorderToPanel(config, "Configure");
    	
    	return config;
    }
}
