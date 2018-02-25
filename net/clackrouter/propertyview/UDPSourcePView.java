package net.clackrouter.propertyview;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.clackrouter.component.extension.Delay;
import net.clackrouter.component.extension.Throttle;
import net.clackrouter.component.extension.UDPSource;

public class UDPSourcePView extends DefaultPropertiesView implements ChangeListener {
    private JSpinner m_rate_spinner;
    
	public UDPSourcePView(UDPSource udp){
		super(udp.getName(), udp);
		SpinnerModel model =
	        new SpinnerNumberModel(udp.getSendingRate(), //initial value
	                               0, //min
	                               10000, //max
	                               1);                //step
       
        m_rate_spinner = new JSpinner(model);
		m_rate_spinner.addChangeListener(this);
		m_rate_spinner.setMaximumSize(new Dimension(40, 30));
    	JPanel main = addMainPanel("UDP Source");
        addHTMLDescription("UDPSource.html");
        main.add(createConfigPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
	}
   
    public void stateChanged(ChangeEvent e) {
        SpinnerNumberModel numberModel = (SpinnerNumberModel)m_rate_spinner.getModel();
        UDPSource delay = (UDPSource)m_model;
        delay.setSendingRate(numberModel.getNumber().intValue());
    }
    
    private JPanel createConfigPanel() {
    	JPanel config = new JPanel();
    //	config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
    	JLabel msg = new JLabel("Sending rate (kbit / sec)");

    	config.add(msg);
    	config.add(Box.createHorizontalStrut(4));
    	config.add(m_rate_spinner);
    	addBorderToPanel(config, "Configure");
    	config.setMaximumSize(new Dimension(330, 95));
    	return config;
    }

	
}
