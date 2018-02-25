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

public class DelayPView extends DefaultPropertiesView implements ChangeListener {
    private JSpinner m_delay_spinner;
    
	public DelayPView(Delay delay){
		super(delay.getName(), delay);
		SpinnerModel model =
	        new SpinnerNumberModel(delay.getDelay(), //initial value
	                               0, //min
	                               2000, //max
	                               20);                //step
       
        m_delay_spinner = new JSpinner(model);
		m_delay_spinner.addChangeListener(this);
		m_delay_spinner.setMaximumSize(new Dimension(40, 30));
    	JPanel main = addMainPanel("Delay");
        addHTMLDescription("Delay.html");
        main.add(createConfigPanel());
        addPanelToTabPane("Ports", m_port_panel);
        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
        refreshPortTab();
        updatePropertiesFrame();
	}
   
    public void stateChanged(ChangeEvent e) {
        SpinnerNumberModel numberModel = (SpinnerNumberModel)m_delay_spinner.getModel();
        Delay delay = (Delay)m_model;
        delay.setDelay(numberModel.getNumber().intValue());
    }
    
    private JPanel createConfigPanel() {
    	JPanel config = new JPanel();
    //	config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
    	JLabel msg = new JLabel("Delay added to packets (msec)");

    	config.add(msg);
    	config.add(Box.createHorizontalStrut(4));
    	config.add(m_delay_spinner);
    	addBorderToPanel(config, "Configure");
    	config.setMaximumSize(new Dimension(330, 95));
    	return config;
    }

	
}
