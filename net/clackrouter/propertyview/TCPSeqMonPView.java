package net.clackrouter.propertyview;


	import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.clackrouter.chart.ClackOccData;
import net.clackrouter.component.extension.TCPSeqMon;

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
public class TCPSeqMonPView extends DefaultPropertiesView {

		
		public TCPSeqMonPView(TCPSeqMon tcpseqmon) {
			super(tcpseqmon.getName(), tcpseqmon);


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
	        JPanel info_panel_4  = new JPanel();
	        
	        JButton chartb = new JButton("Show TCP Sequence Number Graph");
	        chartb.setMaximumSize(new Dimension(300, 50));
	        chartb.addActionListener(new ShowChart());

	        info_panel.setMaximumSize(new Dimension(280,200));
	        
	        info_panel_4.add(chartb);
	        info_panel_4.setPreferredSize(new Dimension(250, 30));
	        info_panel.add(info_panel_4);
	                
	        addBorderToPanel(info_panel, "TCP Sequence Number Info");
	        
	        return info_panel;
	    }   

	   
	    private class ShowChart implements ActionListener 
	    {
	        public void actionPerformed(ActionEvent event) 
	        {
	        	TCPSeqMon model = (TCPSeqMon)m_model;
	            ClackOccData series = model.getOccData();
	            
	            	ChartPanel chart = new ChartPanel(createChart(series, model));
	            
	                chart.setPreferredSize(new java.awt.Dimension(400, 600));
	                chart.setMouseZoomable(true, false);
	            	JFrame time_frame = new JFrame("TCP Sequence Graph");
	            	time_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	            	time_frame.setContentPane(chart);
	                time_frame.pack();
	                RefineryUtilities.centerFrameOnScreen(time_frame);
	                time_frame.setVisible(true);

	            
	        }
	    }
	    
	    private static JFreeChart createChart(XYDataset dataset, TCPSeqMon model) {

	        JFreeChart chart = ChartFactory.createXYLineChart(
	            "TCP Sequence Num vs Time",  // title
	            "Time (sec)",             // x-axis label
	            "TCP Sequence Num Increase (bytes)",   // y-axis label
	            dataset,            // data
	            PlotOrientation.VERTICAL,
	            true,               // create legend?
	            true,               // generate tooltips?
	            false               // generate URLs?
	        );
	        
	        chart.setBackgroundPaint(Color.white);
	      
	        XYPlot plot = (XYPlot) chart.getPlot();
	     
	        plot.setRenderer(new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES));
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinePaint(Color.white);
	        plot.setRangeGridlinePaint(Color.white);
	        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));	        
	        plot.setDomainCrosshairVisible(true);
	        plot.setRangeCrosshairVisible(true);
	        
	        ValueAxis y_axis = plot.getRangeAxis();
	        y_axis.setAutoRangeMinimumSize(50.0);
	        ValueAxis x_axis = plot.getDomainAxis();
	        x_axis.setAutoRangeMinimumSize(15.0);
	        
	        return chart;

	    }

			
	}



