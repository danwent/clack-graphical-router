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

import net.clackrouter.jgraph.pad.resources.ImageLoader;
import net.clackrouter.topology.core.TopoParser;
import net.clackrouter.topology.core.TopologyModel;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;


/**
 * View for a physical host in a Topology JGraph.    
 */
public class TopoPhysicalHostView extends VertexView 
{

    public static int  sBORDER           = 2;
    public static int  smDefaultFontSize = 10; 
    public static Font smDefaultFont     = new Font(null, Font.BOLD , smDefaultFontSize);
    public static int  swPAD = 8; 
    public static int  shPAD = 2; 

    public static String sActiveServerIconFile = "server_active.png";
    public static String sInactiveServerIconFile = "server_inactive.png";
    public static String sFirewallIconFile = "firewall.gif";
    public static String sInternetIconFile = "cloud.gif";
    public static int    sIconWidth  = 48;
    public static int    sIconHeight = 48;

    public static Image mServerIcon = null;
    public static Image mFirewallIcon = null;
    public static Image mInternetIcon = null;

    protected Font   mFont     = null;
    protected int    mFontSize = smDefaultFontSize;

    protected String mName     = null;

    protected BasicStroke mStroke = null;
/*
    public static Rectangle getDefaultBounds(String name, Point pt){
        TextLayout tl = new TextLayout(name, smDefaultFont,
                new FontRenderContext(null, false, false));

        Rectangle2D bounds = tl.getBounds();

        int widthmin = (int)bounds.getWidth() + shPAD;
        if (widthmin < sIconWidth + swPAD){
            widthmin = sIconWidth + swPAD;
        }

        Rectangle rect =  new Rectangle(pt.x, pt.y,  widthmin, 
                                       sIconHeight + (int)(smDefaultFontSize + shPAD)); 
        return rect;
    }
*/
    // Constructor for Superclass
    public TopoPhysicalHostView(Object cell, JGraph graph, CellMapper cm) 
    {
        super(cell, graph, cm);

        mFont = smDefaultFont; 
        mName = ((TopoHostCell)cell).mHost.name;

        mStroke = new BasicStroke(sBORDER);
        if(mServerIcon == null) {
        	mServerIcon = ImageLoader.getImage(sInactiveServerIconFile);
        }
        
        if (mFirewallIcon == null) {
        	mFirewallIcon = ImageLoader.getImage(sFirewallIconFile);	
        }
        
        if (mInternetIcon == null) {
        	mInternetIcon = ImageLoader.getImage(sInternetIconFile);	
        }


    } // -- Constructor


    // -----------------------------------------------------------------------
    //                             Rendering
    // -----------------------------------------------------------------------

    // Returns the Renderer for this View
    public CellViewRenderer getRenderer() {
        return renderer;
    }

    PhyHostRenderer renderer = new PhyHostRenderer();

    // Define the Renderer for an PhyHostView
    class PhyHostRenderer extends VertexRenderer 
    {
    	public int OFFSET = 10;
        public void paint(Graphics g) 
        {
            Graphics2D g2d = (Graphics2D)g;
            TopoPhysicalHostView vview = (TopoPhysicalHostView)view;
            TopoHostCell cell = (TopoHostCell)vview.getCell();
            
            int width  = (int)vview.getBounds().getWidth();
            int height = (int)vview.getBounds().getHeight();

            int imagestart = (width/2) - (sIconWidth/2);
            if (imagestart <= 0){
                imagestart = 0;
            }
            if (vview.mName.equals(TopoParser.FIREWALL_NAME)) {
            	g2d.drawImage(TopoPhysicalHostView.mFirewallIcon, imagestart,OFFSET, null);	
            }else if(vview.mName.equals("internet")){
            	imagestart -= 25;
            	g2d.drawImage(TopoPhysicalHostView.mInternetIcon, imagestart,OFFSET, null);
            } else {
            	g2d.drawImage(TopoPhysicalHostView.mServerIcon, imagestart,OFFSET, null);	
            }
            

            g.setFont(vview.mFont);
            int strwidth = g.getFontMetrics().stringWidth(vview.mName);

            g.setColor(Color.BLACK);
            if(!vview.mName.equals(TopoParser.INTERNET_NAME)) { 
            	g.drawString(vview.mName, (width/2) - (strwidth/2), sIconHeight + 4*OFFSET + shPAD);
            }
            if(cell.mClackDocument != null) {
            	for(int i = 0; i < cell.mHost.interfaces.size(); i++){
            		InetAddress addr = ((TopologyModel.Interface) cell.mHost.interfaces.get(i)).address;
            		if(addr == null) continue;
            		String interfaceString = ((TopologyModel.Interface) cell.mHost.interfaces.get(i)).name+": "+ addr.getHostAddress();
            		strwidth = g.getFontMetrics().stringWidth(interfaceString);
            		g.drawString(interfaceString, (width/2) - (strwidth/2), sIconHeight + shPAD + 3*OFFSET + 25*(i+1));
            	}
            }

            g.setColor(Color.GRAY);
            g.drawRect(0, 0, width-1, height-1);
            
        } // -- paint

    } // -- PhyHostRenderer
} // -- PhyHostView
