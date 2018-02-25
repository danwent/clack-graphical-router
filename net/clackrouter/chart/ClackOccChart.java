
package net.clackrouter.chart;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.clackrouter.component.base.Queue;
import net.clackrouter.gui.ClackFramework;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.jfree.data.xy.XYDataset;



/**
 * Real-time graphing window, using jfreechart libraries.  
 * 
 * DW: at some point we need to make this non-queue specific
 */
public class ClackOccChart extends JFrame {
	
	
    public ClackOccChart(ClackOccData dataset, Queue queue) {
    	
    		super("Queue Graph");
        	JFreeChart chart = ChartFactory.createXYLineChart(
	            "Per Flow Queue Occupancy",  // title
	            "Time (sec)",             // x-axis label
	            "Occupancy (bytes)",   // y-axis label
	            dataset,            // data
	            PlotOrientation.VERTICAL,
	            true,               // create legend?
	            true,               // generate tooltips?
	            false               // generate URLs?
	        );
	        
	        chart.setBackgroundPaint(Color.white);
	      
	        XYPlot plot = (XYPlot) chart.getPlot();
	     
	        StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES);
	        dataset.setRenderer(renderer);
	        plot.setRenderer(renderer);
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinePaint(Color.white);
	        plot.setRangeGridlinePaint(Color.white);
	        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	        ValueAxis y_axis = plot.getRangeAxis();
	        y_axis.setRange(0.0, queue.getMaxOccupancy() + 100.0);
	        
	        plot.setDomainCrosshairVisible(true);
	        plot.setRangeCrosshairVisible(true);
	        
	        ChartPanel panel = new ChartPanel(chart);
            panel.setPreferredSize(new java.awt.Dimension(500, 350));
            panel.setMouseZoomable(true, false);

        	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	setContentPane(panel);
            pack();
            RefineryUtilities.centerFrameOnScreen(this);
            setVisible(true);
    } 


    
    /*
     * dw: 3-4-2006  i'm commenting this out for convenience to avoid updating it now that we have multiple types of data series.
     * hopefully we can just use multi-series in the long run, but i want to make sure this works first

    public ClackOccChart snapshot()
    {
        ClackOccChart newtest = new ClackOccChart((SingleSeriesOccData)mData.clone(), mHeading, mChartTitle,
        		mDomain, mRange);


        JFreeChart   newchart;
        try{

            newchart = 
                ChartFactory.createXYLineChart(
                        mChartTitle,
                        mDomain, 
                        mRange, 
                        newtest.mData, 
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false
                        );

        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

        ChartPanel chartPanel = new ChartPanel(newchart);
        chartPanel.setMouseZoomable(true);
        newtest.setContentPane(chartPanel);

        return newtest;
    } // -- clone

    private class CreateSnapshot implements ActionListener
    {
        private ClackOccChart mSource = null;

        public CreateSnapshot(ClackOccChart source)
        { mSource = source; }

        public void actionPerformed(ActionEvent event)
        {
            try{
                ClackOccChart demo = (ClackOccChart)mSource.snapshot(); 
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);

                // -- save some cycles as this can get pretty slow!
            //    mSource.mData.updateRealtime(false);
                mSource.setVisible(false);

            }catch(Exception e)
            { }
        }

    } // -- private class CreateClone
    
    */

} // -- QueueOccChart
