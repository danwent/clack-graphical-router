package net.clackrouter.chart;

import java.awt.Color;
import java.awt.Paint;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Represents a collection of (x,y) values that contain instanteneuous and
 * average queue size. This collection should be constantly updated by a
 * box and it is used by a chart to read the (x,y) values and display
 * them. When a new (x,y) value is added to the dataset, it fires an event
 * that informs a chart to update itself.
 */
public class ClackOccData extends AbstractSeriesDataset implements XYDataset,  Serializable
{

	public static Color[] colors = new Color[]{ Color.RED, Color.CYAN, Color.ORANGE, Color.GREEN, Color.BLACK };
	private Hashtable color_map; // maps a series name to its current color

    private Hashtable m_all_series_hash; // holds all series info for quick lookup
    private ArrayList m_series_name_list;
    private boolean m_updateRealtime;
    
    // ivars used to manage the fact that we don't draw for every point
    private long last_graph_draw = 0;
    public static int MIN_DRAW_INTERVAL = 100; // only update every 1/10 of a second
    private StandardXYItemRenderer renderer;
    
    /**
     * Constructs queue size dataset.
     */
    public ClackOccData() {

        m_updateRealtime = true;  // changed

        m_all_series_hash = new Hashtable();
        m_series_name_list = new ArrayList();
        color_map = new Hashtable();
    }

    public void removeSeries(String key){
    	
		m_all_series_hash.remove(key);
		int removed_index = m_series_name_list.indexOf(key);
		m_series_name_list.remove(key);
		color_map.remove(key);
		
		if(renderer != null){
			for(int i = 0; i < m_series_name_list.size(); i++){
					String k = (String)m_series_name_list.get(i);
					Color c = (Color)color_map.get(k);
					renderer.setSeriesPaint(i, c);
			}
		}
		
		notifyListeners(new DatasetChangeEvent(this, null));
    }
    
    public void setRenderer(StandardXYItemRenderer r) { renderer = r; }
    
    public DomainOrder getDomainOrder() { return DomainOrder.ASCENDING; }
   
    /**
     * Adds the supplied (x,y) tuple to the instantaneous series,
     * calculates the running average and adds (x, average) tuple to the
     * average series.
     *
     * @param x Domain value, can be absolute or relative time
     * @param y Number of packets in the queue
     */
    public synchronized void addXYValue(String key, Number x, Number y)
    {
    	
        try {
           	long cur_time = System.currentTimeMillis();
        	
        	XYSeries series = (XYSeries)m_all_series_hash.get(key);
        	if(series == null){
        		series = new XYSeries(key);
        	
        		m_all_series_hash.put(key, series);
        		m_series_name_list.add(key);
        		Color new_color = getFreshColor();
        		color_map.put(key, new_color);
        		if(renderer != null){
        			int new_index = (m_series_name_list.size() - 1);
        			renderer.setSeriesPaint(new_index, new_color);
        		}
        	}
            // Insert the instantaneous queue size
            series.add(x, y);
            
            // Notify the chart of dataset change only if requeste, to save cycles
            if( m_updateRealtime ){
            	
            	if(cur_time - last_graph_draw > MIN_DRAW_INTERVAL){
            		last_graph_draw = cur_time;
            		notifyListeners(new DatasetChangeEvent(this, null));
            	}
            }

    //        if( series.getItemCount() > (4 * ITEM_WINDOW) ) {
    //            translate(key, series);
     //       }
           

        } catch(Exception e) {
             e.printStackTrace();
        }
    }
   
    /**
     * Useful for timing out values older than a certain x value
     * @param key
     * @param x_thresh
     */
    public void remove_all_with_x_lessthan(String key, double x_thresh){
    	XYSeries series = (XYSeries)m_all_series_hash.get(key);
    	
    	XYSeries new_series = new XYSeries(key);
        int itemCount = series.getItemCount();
        // dw: are these ordered?  i'm not sure, so i'll assume not
        for(int item = 0; item < itemCount ;  item++) {
        	Number xval = series.getX(item);
        	if(xval.doubleValue() > x_thresh)
        		new_series.add(series.getX(item), series.getY(item));
        }

        // Assign the new series and let the garbage collector remove old instances
        m_all_series_hash.put(key, new_series);
    }

    /**
     * Returns the x value for the specified series and item.  Series are
     * numbered 0, 1, ...
     *
     * @param series The index (zero-based) of the series
     * @param item The index (zero-based) of the required item
     * @return The x value for the specified series and item
     */
    public synchronized Number getX(int series_int, int item)
    {
        Number xValue = new Double(0.0);
        String key = (String)m_series_name_list.get(series_int);   
    	XYSeries series = (XYSeries)m_all_series_hash.get(key);
    	
        int itemCount = series.getItemCount();
        if( item >= itemCount )
            item = item % itemCount;

        return series.getX(item);
    }
    
    public synchronized double getXValue(int series, int item){
    	Number n = getX(series, item);
    	return n.doubleValue();
    }

    /**
     * Returns the y value for the specified series and item.  Series are
     * numbered 0, 1, ...
     *
     * @param series The index (zero-based) of the series
     * @param item The index (zero-based) of the required item
     * @return The y value for the specified series and item
     */
    public synchronized Number getY(int series_int, int item)
    {
        Number yValue = new Double(0.0);
        String key = (String)m_series_name_list.get(series_int);   
    	XYSeries series = (XYSeries)m_all_series_hash.get(key);
    	
        int itemCount = series.getItemCount();
        if( item >= itemCount )
            item = item % itemCount;

        return series.getY(item);
    }

    public synchronized double getYValue(int series, int item){
    	Number n = getY(series, item);
    	return n.doubleValue();
    }
    /**
     * Returns the number of series in the data source.
     *
     * @return The number of series in the data source.
     */
    public synchronized int getSeriesCount() {
        return m_series_name_list.size();
    }

    /**
     * Returns the name of the series.
     *
     * @param series The index (zero-based) of the series
     * @return The name of the series
     */
    public synchronized String getSeriesKey(int series) {
        return (String)m_series_name_list.get(series);
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param series The index (zero-based) of the series
     * @return The number of items in the specified series
     */
    public synchronized int getItemCount(int series) {
    	XYSeries s = (XYSeries)m_all_series_hash.get(getSeriesKey(series));
    	return s.getItemCount();
    }

    /**
     * Specifies if the dataset should notify listeners of changes. This
     * method should be called with updateRealtime = true when the
     * corresponding chart is displayed to the user. Otherwise
     * updateRealtime should be reset to false to reduce processing.
     *
     * @param updateRealtime True if changes should be immediately reflected
     *                       in the dataset
     */
    public synchronized void updateRealtime(boolean updateRealtime)
    {
        m_updateRealtime = updateRealtime;
    }
    
    public void removeEverySeries() {
        m_all_series_hash.clear();
        color_map.clear(); 
        m_series_name_list.clear();
        notifyListeners(new DatasetChangeEvent(this, null));
    }
    
    // brute force simple way to get a fresh color, this is pretty rare, so its ok
    private Color getFreshColor() {
 
    	
    	Color[] used = new Color[color_map.size()];
    	int x = 0;
    	for(Enumeration e = color_map.elements(); e.hasMoreElements(); ){
    		used[x++] = (Color)e.nextElement();
    	}
    	
    	for(int i = 0; i < colors.length; i++){
    		boolean free = true;
    		for(int j = 0; j < used.length; j++){
    			if(colors[i].equals(used[j])) free = false;
    		}
    		if(free) return colors[i];
    	}
    	
        return Color.getHSBColor( (float)Math.random(), 1.0F, 1.0F );
    }
    

}
