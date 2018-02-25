package net.clackrouter.propertyview;


	import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.clackrouter.chart.ClackOccChart;
import net.clackrouter.chart.ClackOccData;
import net.clackrouter.component.base.Queue;
import net.clackrouter.component.extension.FlowByteQueue;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;


	/**
	 * @author Dan
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Generation - Code and Comments
	 */
public class FlowByteQueuePopup extends DefaultPropertiesView implements ChangeListener {


		
		private JLabel m_current_size, m_total_dropped;
	    private JSpinner m_size_spinner;

	    private int m_old_occupancy = 0, m_old_dropped = 0; 
		
		public FlowByteQueuePopup(FlowByteQueue queue) {
			super(queue.getName(), queue);

	        m_current_size = new JLabel("");
	        m_total_dropped = new JLabel("");

	    	JPanel main = addMainPanel("FlowByteQueue");
	        addHTMLDescription("Queue.html");
	        main.add(createInfoPanel());
	        addPanelToTabPane("Ports", m_port_panel);
	        refreshPortTab();
	        addPanelToTabPane("Log", new JScrollPane(m_model.getLog()));
	        updatePropertiesFrame();
		}
		

	    // returns the JFrame used to display/change the properties of the underlying
	    // This function builds the frame, then uses updatePropertiesFrame() to fill
	    // in relavent data.  

	    public JPanel createInfoPanel()
	    {	

	        JPanel info_panel    = new JPanel();
	        JPanel info_panel_1  = new JPanel();
	        JPanel info_panel_2  = new JPanel();
	        JPanel info_panel_3  = new JPanel();
	        JPanel info_panel_4  = new JPanel();
	        JPanel info_panel_5  = new JPanel();
	        
			SpinnerModel model =
		        new SpinnerNumberModel(((Queue)m_model).getMaxOccupancy(), //initial value
		                               0, //min
		                               100000, //max
		                               500);                //step
	       
	        m_size_spinner = new JSpinner(model); 
			m_size_spinner.addChangeListener(this);
			m_size_spinner.setMaximumSize(new Dimension(70, 60));

	        JButton chartb = new JButton("Show Queue Size vs. Time Chart");
	        chartb.setMaximumSize(new Dimension(300, 50));
	        chartb.addActionListener(new ShowChart());
	        
	        JButton clear_button = new JButton("Clear Chart");
	        clear_button.setMaximumSize(new Dimension(100, 50));
	        clear_button.addActionListener(new ShowChart());

	        info_panel.setMaximumSize(new Dimension(280,200));
	        
	        info_panel_1.add(m_current_size);
	        info_panel_1.setPreferredSize(new Dimension(250, 30));
	        info_panel.add(info_panel_1);
	        
	        info_panel_2.add(m_total_dropped);
	        info_panel_2.setPreferredSize(new Dimension(250, 30));
	        info_panel.add(info_panel_2);
	        

	        info_panel_3.add(new JLabel("Queue Size (Bytes): "));
	        info_panel_3.add(m_size_spinner);
	        info_panel_3.setPreferredSize(new Dimension(250, 30));
	        info_panel.add(info_panel_3);
	        
	        info_panel_4.add(chartb);
	        info_panel_4.setPreferredSize(new Dimension(250, 30));
	        info_panel.add(info_panel_4);
	        
	        
	        info_panel_5.add(clear_button);
	        info_panel_5.setPreferredSize(new Dimension(250, 30));
	        info_panel.add(info_panel_5);
	                
	        addBorderToPanel(info_panel, "Buffer Info");
	        
	        return info_panel;
	    }   

	   
	    private class ShowChart implements ActionListener 
	    {
	        public void actionPerformed(ActionEvent event) 
	        {
	        	String cmd = event.getActionCommand();
	        	FlowByteQueue model = (FlowByteQueue)m_model;
	        	if(cmd.equalsIgnoreCase("Clear Chart")){
	        		model.clearAllFlowInformation();
	        	}else {
	        		ClackOccChart chart = new ClackOccChart(model.getMultiSeriesOccData(), model);  
	        	}
	        }
	    }
	    
	    
	    protected void updatePropertiesFrame()
	    {
	    	super.updatePropertiesFrame();
	        Queue queuecomp = (Queue)m_model;
	        
	        int new_occupancy = queuecomp.getOccupancy();
	        int new_dropped = queuecomp.getTotalDropped();
	        
	        if(m_old_dropped != new_dropped){
	        	m_total_dropped.setText("Total Dropped: " + new_dropped);
	        	m_old_dropped = new_dropped;
	        }


	     
	    }
	    
	    public void stateChanged(ChangeEvent e) {
	        SpinnerNumberModel numberModel = (SpinnerNumberModel)m_size_spinner.getModel();
	        Queue queue = (Queue)m_model;
	        queue.setMaxOccupancy(numberModel.getNumber().intValue());
	    }
			
	}


