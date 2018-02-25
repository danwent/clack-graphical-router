package net.clackrouter.topology.create;


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
import net.clackrouter.topology.core.TopoParser;
import net.clackrouter.topology.core.TopologyModel;
import net.clackrouter.topology.graph.TopoHostCell;
import net.clackrouter.topology.graph.TopoPhysicalHostView;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;


/**
 * View for a virtual host in a Topology JGraph.    
 */
public class CreateTopoHostView extends VertexView 
{
    public static int  sBORDER           = 2;
    public static int  smDefaultFontSize = 10; 
    public static Font smDefaultFont     = new Font(null, Font.BOLD , smDefaultFontSize);
    public static int  swPAD = 18; 
    public static int  shPAD = 16; 
    public static int  sIconWidth  = 100;
    public static int  sIconHeight = 52;

    
    public static Image mActiveRouterIcon = null;
    public static Image mActiveServerIcon = null;
    public static Image mInactiveServerIcon = null;
    public static Image mFirewallIcon = null;
    public static Image mInternetIcon = null;
    
    protected Color  mBorderColor = null;
    protected Color  mReservedFillColor   = null;
    protected Color  mNonReservedFillColor   = null;

    protected Font   mFont     = null;
    protected int    mFontSize = smDefaultFontSize;


   
    static {
        	mActiveRouterIcon = ImageLoader.getImage("router-active.gif");
        	mActiveServerIcon = ImageLoader.getImage("server_active.png");
        	mInactiveServerIcon = ImageLoader.getImage("server_inactive.png");
        	mFirewallIcon = ImageLoader.getImage("firewall.gif");
        	mInternetIcon = ImageLoader.getImage("cloud.gif");
    }


    // Constructor for Superclass
    public CreateTopoHostView(TopoHostCell cell, JGraph graph, CellMapper cm) 
    {
        super(cell, graph, cm);

        mFont = smDefaultFont; ;
        cell.mView = this;
       
    } // -- Constructor


    // -----------------------------------------------------------------------
    //                             Rendering
    // -----------------------------------------------------------------------

    // Returns the Renderer for this View
    public CellViewRenderer getRenderer() {
        return renderer;
    }

    CreateHostRenderer renderer = new CreateHostRenderer();

    // made no longer static, as we were having sharing issues 
    // because they were being drawn simultaneously 
    // Define the Renderer for an VhostView
     class CreateHostRenderer extends VertexRenderer 
    {
    	public int OFFSET = 10;
        public void paint(Graphics g) 
        {
        	
            TopoHostCell cell = ((TopoHostCell)view.getCell());
            TopologyModel.Host host = cell.mHost;
            
            Graphics2D g2d = (Graphics2D)g;
            CreateTopoHostView vview = (CreateTopoHostView)view;
            int width  = (int)vview.getBounds().getWidth();
            int height = (int)vview.getBounds().getHeight();
            
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0,0,width, height);
            
            int image_width_start = (width/2) - (sIconWidth/2);
            if (image_width_start <= 0){
                image_width_start = 0;
            }
                
         //   System.out.println(host.name + " has iface count = " + host.interfaces.size());
            if (host.name.equals(TopoParser.FIREWALL_NAME)) {
            	image_width_start += 20;
            	g2d.drawImage(CreateTopoHostView.mFirewallIcon, image_width_start,OFFSET, null);	
            }else if(host.name.equals("internet")){
            	g2d.drawImage(CreateTopoHostView.mInternetIcon, image_width_start,OFFSET, null);
            }else if(host.interfaces.size() > 1 || host.name.indexOf("router") != -1) {
                // if the cell has more than one interface, or has router in the name, draw it as
                // a router
            	g2d.drawImage(CreateTopoHostView.mActiveRouterIcon, image_width_start,OFFSET, null);	
            
            }else {
            	// draw as host
            	if (host.reservable)
            		g2d.drawImage(CreateTopoHostView.mActiveServerIcon, image_width_start * 2,OFFSET / 4, null);	
            	else
            		g2d.drawImage(CreateTopoHostView.mInactiveServerIcon, image_width_start * 2 ,OFFSET / 4 , null);
            }
            ///
            
            g.setColor(Color.BLACK);
            
            g.setFont(vview.mFont);
            int strwidth = g.getFontMetrics().stringWidth(host.name);
            
            if(!host.name.equals("internet")){
            	g.drawString(host.name, (width/2) - (strwidth/2), 
            			sIconHeight + sBORDER + 2* OFFSET);
            }
          
        	g.setColor(Color.GRAY); // default for outline

            g.drawRect(0, 0, width-1, height-1);

        } // -- paint
        

    } // -- VhostRenderer
} // -- VhostView
