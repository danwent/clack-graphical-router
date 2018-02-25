package net.clackrouter.topology.graph;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.net.InetAddress;

import net.clackrouter.component.simplerouter.InterfaceIn;
import net.clackrouter.jgraph.pad.resources.ImageLoader;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.router.core.Router;
import net.clackrouter.routing.OSPFRoutingEntry;
import net.clackrouter.routing.RIPRoutingEntry;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;
import net.clackrouter.topology.core.TopologyModel;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;


/**
 * View for a virtual host in a Topology JGraph.    
 */
public class TopoVirtualHostView extends VertexView 
{
    public static int  sBORDER           = 2;
    public static int  smDefaultFontSize = 10; 
    public static Font smDefaultFont     = new Font(null, Font.BOLD , smDefaultFontSize);
    public static int  swPAD = 18; 
    public static int  shPAD = 16; 
    public static int  sIconWidth  = 100;
    public static int  sIconHeight = 52;

    public static String sActiveRouterIconFile = "router-active.gif";
    public static String sInactiveRouterIconFile = "router-inactive.gif";
    
    public static Image mActiveRouterIcon = null;
    public static Image mInactiveRouterIcon = null;
    public static Image mActiveServerIcon = null;
    public static Image mInactiveServerIcon = null;
    
    protected Color  mBorderColor = null;
    protected Color  mReservedFillColor   = null;
    protected Color  mNonReservedFillColor   = null;

    protected Font   mFont     = null;
    protected int    mFontSize = smDefaultFontSize;

    protected String mName     = null;
    protected boolean mIsReserved = false;
    protected BasicStroke mStroke = null;
    
    public static int ROUTE_ENTRY_HIGHLIGHT_MSEC = 500;

    public Rectangle getDefaultBounds(String name, Point pt){
        TextLayout tl = new TextLayout(name, smDefaultFont,
                new FontRenderContext(null, false, false));

        Rectangle2D bounds = tl.getBounds();
        
        int numIfaces = ((TopoHostCell)getCell()).mHost.interfaces.size();

        Rectangle rect =  new Rectangle(pt.x, pt.y, (int)(bounds.getWidth() + swPAD),
                                       (int)(bounds.getHeight() + shPAD)); 
        return rect;
    }

    // Constructor for Superclass
    public TopoVirtualHostView(TopoHostCell cell, JGraph graph, CellMapper cm) 
    {
        super(cell, graph, cm);

        mFont = smDefaultFont; 
        mName = cell.mHost.name;
        mIsReserved = cell.mIsReservable;
        cell.mView = this;
        
    //    mBorderColor = new Color(92, 133, 38); // -- kinda pea green
    //    mReservedFillColor   = new Color(255,255,153); // -- kinda yellowish
  //      mNonReservedFillColor = Color.LIGHT_GRAY;
        float[] dashes = new float[]{5.0f, 2.0f};
        mStroke = new BasicStroke(sBORDER, 
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL, 10, dashes, 0);
        
        if(mActiveRouterIcon == null) {
        	mActiveRouterIcon = ImageLoader.getImage(sActiveRouterIconFile);
        }
        
        if (mInactiveRouterIcon == null) {
        	mInactiveRouterIcon = ImageLoader.getImage(sInactiveRouterIconFile);	
        }
        
        if (mActiveServerIcon == null){
        	mActiveServerIcon = ImageLoader.getImage("server_active.png");
        }

        if (mInactiveServerIcon == null){
        	mInactiveServerIcon = ImageLoader.getImage("server_inactive.png");
        }
    } // -- Constructor


    // -----------------------------------------------------------------------
    //                             Rendering
    // -----------------------------------------------------------------------

    // Returns the Renderer for this View
    public CellViewRenderer getRenderer() {
        return renderer;
    }

    VhostRenderer renderer = new VhostRenderer();

    // made no longer static, as we were having sharing issues 
    // because they were being drawn simultaneously 
    // Define the Renderer for an VhostView
     class VhostRenderer extends VertexRenderer 
    {
    	public int OFFSET = 10;
        public void paint(Graphics g) 
        {
        	
            TopoHostCell cell = ((TopoHostCell)view.getCell());
        	if(cell.mClackDocument != null && cell.mClackDocument.inTopoRouteTableViewMode()){
        		paintWithRoutingTable(g);
        		return;
        	}
            Graphics2D g2d = (Graphics2D)g;
            TopoVirtualHostView vview = (TopoVirtualHostView)view;
            int width  = (int)vview.getBounds().getWidth();
            int height = (int)vview.getBounds().getHeight();
            
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0,0,width, height);
            
            int image_width_start = (width/2) - (sIconWidth/2);
            if (image_width_start <= 0){
                image_width_start = 0;
            }
            
            // if the cell has more than one interface, or has router in the name, draw it as
            // a router
            if(cell.mHost.interfaces.size() > 1 || cell.mHost.name.indexOf("router") != -1) {
            	// draw as a router
            	if (vview.mIsReserved)
            		g2d.drawImage(TopoVirtualHostView.mActiveRouterIcon, image_width_start,OFFSET, null);	
            	else
            		g2d.drawImage(TopoVirtualHostView.mInactiveRouterIcon, image_width_start,OFFSET, null);
            }else {
            	// draw as host
            	if (vview.mIsReserved)
            		g2d.drawImage(TopoVirtualHostView.mActiveServerIcon, image_width_start * 2,OFFSET / 4, null);	
            	else
            		g2d.drawImage(TopoVirtualHostView.mInactiveServerIcon, image_width_start * 2 ,OFFSET / 4 , null);
            }
            ///
            
            g.setColor(Color.BLACK);
            
            g.setFont(vview.mFont);
            int strwidth = g.getFontMetrics().stringWidth(vview.mName);
            
            g.drawString(vview.mName, (width/2) - (strwidth/2), sIconHeight + sBORDER + 2* OFFSET);
          
        	g.setColor(Color.GRAY); // default for outline
            
            // if we're a live router, 
            // we actually want to synch to the current IP addresses of the router
            if(cell.mClackDocument != null){
            	Router r = cell.mClackDocument.getFramework().getTopologyManager().getRouterByName(cell.mHost.name);            
            	if(r == null) return;
            	InterfaceIn[] all_ifaces = r.getInputInterfaces();
            	for(int i = 0; i < all_ifaces.length; i++){
            		String interfaceString = all_ifaces[i].getDeviceName() +": "+ all_ifaces[i].getIPAddress().getHostAddress();
            		strwidth = g.getFontMetrics().stringWidth(interfaceString);
            		g.drawString(interfaceString, (width/2) - (strwidth/2), sIconHeight + 3 * OFFSET + 10*(i+1));
            	}
                if(r != null && r.getPendingError())
                	g.setColor(Color.RED);
            }

            g.drawRect(0, 0, width-1, height-1);

        } // -- paint
        
        public void paintWithRoutingTable(Graphics g) 
        {
        	try{

            Graphics2D g2d = (Graphics2D)g;
            TopoVirtualHostView vview = (TopoVirtualHostView)view;

            int width  = (int)vview.getBounds().getWidth();
            int height = (int)vview.getBounds().getHeight();
            
      
            g2d.setColor(Color.WHITE);
            g2d.fillRect(30,10,width - 30, height - 20);
            
            g.setColor(Color.BLACK);

            TopoHostCell cell = (TopoHostCell)vview.getCell();
            
            g.setFont(vview.mFont);           
            Router r = cell.mClackDocument.getFramework().getTopologyManager().getRouterByName(cell.mHost.name);            
            if(r == null) {
            	return;
            }   
            RoutingTable routing_table = r.getRoutingTable();
            if(routing_table == null) {
            	return; // sometimes happens if you click on the show routing tables too early
            }
            
            String routing_type = "static";
            if(routing_table.numEntries() > 0 && routing_table.getEntry(0) instanceof RIPRoutingEntry)
            	routing_type = "RIP";
            if(routing_table.numEntries() > 0 && routing_table.getEntry(0) instanceof OSPFRoutingEntry)
            	routing_type = "OSPF";
            
            String header = vview.mName + " (" + routing_type + " table) (" + routing_table.numEntries() + " entries)";
            int strwidth = g.getFontMetrics().stringWidth(header);
            
            g.drawString(header , (width/2) - (strwidth/2), sBORDER + 2* OFFSET);
            
            int DST_WIDTH = 30;
            int IFACE_WIDTH = 130;
            int COST_WIDTH = 190;
            int TTL_WIDTH = 220; 
            
        	int h = 3 * OFFSET + 15;
        	g.drawString("destination" , DST_WIDTH,  h);
        	g.drawString("iface", IFACE_WIDTH, h);   
        	if(routing_type.equals("RIP") ){
        		g.drawString("Cost", COST_WIDTH, h);
        		g.drawString("TTL" , TTL_WIDTH, h);     		
        	}else if(routing_type.equals("OSPF")) {
        		g.drawString("Cost", COST_WIDTH, h);   		
        	}
            
        	long cur_time = r.getTimeManager().getTimeMillis();
            for(int i = 0; i < routing_table.numEntries(); i++){
            	RoutingEntry entry = (RoutingEntry) routing_table.getEntry(i);
            	if(entry == null) continue;
            	
            	if((cur_time - entry.last_access) < ROUTE_ENTRY_HIGHLIGHT_MSEC ){
            		g.setColor(Color.CYAN);
            	}else {
            		g.setColor(Color.BLACK);
            	}
            	String prefix = NetUtils.NetworkAndMaskToString(entry.network, entry.mask);
            	String iface = entry.interface_name;
            	h = 3 * OFFSET + 15*(i+2);
            	            	
            	g.drawString(prefix , DST_WIDTH,  h);
            	if(entry.nextHop == null) {
            		if(iface == null)
            			g.drawString("local/null", IFACE_WIDTH, h);
            		else 
            			g.drawString(iface, IFACE_WIDTH, h);
            	} else {
                	String nh = entry.nextHop.getHostAddress();
                	int last_dot = nh.lastIndexOf(".");
                	nh = nh.substring(last_dot);
                	g.drawString(iface + " (" + nh + ")", IFACE_WIDTH, h);
            	}
            		
            	
            	if(routing_type.equals("RIP")){
            		g.drawString("" + ((RIPRoutingEntry)entry).cost, COST_WIDTH + 10, h);
            		g.drawString("" + ((RIPRoutingEntry)entry).ttl, TTL_WIDTH, h);
            	}
            	if(routing_type.equals("OSPF")){
            		g.drawString("" + ((OSPFRoutingEntry)entry).cost, COST_WIDTH + 10, h);
            	}
            }
              
            if(r.getPendingError())
            	g.setColor(Color.RED);
            else
            	g.setColor(Color.GRAY);
            g.drawRect(0, 0, width-1, height-1);
             
            } catch (Exception e) {
            	e.printStackTrace();
            }
        } // -- paint

    } // -- VhostRenderer
} // -- VhostView
