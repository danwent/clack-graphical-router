/*
 * Created on Nov 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.propertyview;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
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

import org.jfree.ui.RefineryUtilities;


/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class QueuePopup extends DefaultPropertiesView implements ChangeListener {


	
	private JLabel m_current_size, m_total_dropped;
	private VisibleQueue visible_queue;
    private JSpinner m_size_spinner;
    private ClackOccChart time_frame = null;
    private int m_old_occupancy = 0, m_old_dropped = 0; 
	
	public QueuePopup(Queue queue) {
		super(queue.getName(), queue);

        m_current_size = new JLabel("");
        m_total_dropped = new JLabel("");
        visible_queue = new VisibleQueue(200, 100);

    	JPanel main = addMainPanel("Queue");
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
        
		SpinnerModel model =
	        new SpinnerNumberModel(((Queue)m_model).getMaxOccupancy(), //initial value
	                               0, //min
	                               100000, //max
	                               500);                //step
       
        m_size_spinner = new JSpinner(model); 
		m_size_spinner.addChangeListener(this);
		m_size_spinner.setMaximumSize(new Dimension(70, 60));

        JButton chartb = new JButton("Show Queue Size/Time Graph");
        chartb.setMaximumSize(new Dimension(300, 50));
        chartb.addActionListener(new ShowChart());

        info_panel.setMaximumSize(new Dimension(280,200));
        
        info_panel_1.add(m_current_size);
        info_panel_1.setPreferredSize(new Dimension(250, 30));
        info_panel.add(info_panel_1);
        
        info_panel_2.add(m_total_dropped);
        info_panel_2.setPreferredSize(new Dimension(250, 30));
        info_panel.add(info_panel_2);
        
        //info_panel.add(m_current_size);
//        info_panel.add(Box.createHorizontalStrut(2));
        //info_panel.add(m_total_dropped);
        
        
//        info_panel.add(Box.createHorizontalStrut(10));
   //     info_panel.add(visible_queue);
        info_panel_3.add(new JLabel("Queue Size (Bytes): "));
        info_panel_3.add(m_size_spinner);
        info_panel_3.setPreferredSize(new Dimension(250, 30));
        info_panel.add(info_panel_3);
        
//        info_panel.add(Box.createHorizontalStrut(10));
        info_panel_4.add(chartb);
        info_panel_4.setPreferredSize(new Dimension(250, 30));
        info_panel.add(info_panel_4);
                
        addBorderToPanel(info_panel, "Buffer Info");
        
        return info_panel;
    }   

   
    private class ShowChart implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
      
            ClackOccData data = ((Queue)m_model).getQueueOccData();
            
            if ( time_frame == null )
            {
                time_frame = new ClackOccChart( data, (Queue)m_model);
                time_frame.pack();
                RefineryUtilities.centerFrameOnScreen(time_frame);
                time_frame.setVisible(true);
            }

            else{
                data.updateRealtime(true);
                time_frame.show();
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

        if(m_old_occupancy != new_occupancy){
        	m_current_size.setText("Current Queue Occupancy: " + new_occupancy);
        	try {
        		visible_queue.setQueueData(queuecomp.getMaxOccupancy(), new_occupancy);
        	}catch (Exception e){
        		e.printStackTrace(); /* should never fail */
        	}
        	m_old_occupancy = new_occupancy;
        }
     
    }
    
    public void stateChanged(ChangeEvent e) {
        SpinnerNumberModel numberModel = (SpinnerNumberModel)m_size_spinner.getModel();
        Queue queue = (Queue)m_model;
        queue.setMaxOccupancy(numberModel.getNumber().intValue());
    }
    
   

	private class VisibleQueue extends JComponent {
		int m_width, m_height;
		int m_capacity, m_occupancy;
		
		public VisibleQueue(int width, int height){
			m_width = width; m_height = height;
			this.setPreferredSize(new Dimension(m_width, m_height));
			this.setVisible(true);
		}
		
		public void setQueueData(int capacity, int occupancy) 
        {
			m_capacity = capacity; m_occupancy = occupancy; 
			
			repaint();
		}
		
		public void paintComponent(Graphics g){
			this.setBackground(Color.RED);
			g.setColor(Color.GREEN);
			g.fillRect(0, 0, m_width, m_height);
			g.setColor(Color.RED);
			float occupancy_bar_length = (((float)m_width)*((float)m_occupancy / (float)m_capacity));
			g.fillRect(0, 0, (int)occupancy_bar_length, m_height);

		}
		
	}
	
	

		
}
