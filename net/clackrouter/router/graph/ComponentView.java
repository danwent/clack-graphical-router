package net.clackrouter.router.graph;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import net.clackrouter.application.TCPSocket;
import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.Interface;
import net.clackrouter.component.tcp.TCB;
import net.clackrouter.ethereal.Ethereal;
import net.clackrouter.gui.ClackPaintable;
import net.clackrouter.gui.ClackView;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;


/**
 * Provides a view for the ComponentCell, representing a Clack Component.  
 */
public class ComponentView extends VertexView implements ClackPaintable
{
	public static int  sBORDER           = 2;
	public static int  smDefaultFontSize = 10; 
	public static Font smDefaultFont     = new Font(null, Font.BOLD , smDefaultFontSize);
	public static int  swPAD = 25; 
	public static int  shPAD = 20; 
	
	public static Color ERROR_FILL = Color.RED;
	
	protected Color  mBorderColor = null;
	protected Color  mFillColor   = null;
	
	protected Font   mFont     = null;
	protected int    mFontSize = smDefaultFontSize;
	
	protected String mName     = null;
	protected String mLabel    = null;
	
//	public static BasicStroke STROKE = new BasicStroke(sBORDER, 
//			BasicStroke.CAP_BUTT,
//			BasicStroke.JOIN_BEVEL, 10, null, 0);
	
	public static BasicStroke STROKE = new BasicStroke(1);
	public static BasicStroke ERROR_STROKE = new BasicStroke(2);
	
	public static Rectangle getDefaultBounds(String name, Point pt){
		TextLayout tl = new TextLayout(name, smDefaultFont,
				new FontRenderContext(null, false, false));
		
		Rectangle2D bounds = tl.getBounds();
		
		Rectangle rect =  new Rectangle(pt.x, pt.y, (int)(bounds.getWidth() + swPAD),
				(int)(bounds.getHeight() + shPAD)); 
		
		
		return rect;
	}
	
	
	// Constructor for Superclass
	public ComponentView(Object cell, JGraph graph, CellMapper cm) 
	{
		super(cell, graph, cm);
		
		
		mFont = smDefaultFont; 
		mName = ((ComponentCell)cell).toString();
		ClackComponent comp = ((ComponentCell)cell).getClackComponent();
		mLabel = comp.getTypeName();
		mFillColor = comp.getColor();
		mBorderColor = Color.GRAY;
		
		comp.setView(this);
		
	} // -- Constructor
	
    public ClackView getClackView() { return ((ComponentCell)getCell()).getClackView(); }
	
    public void clackPaint() {
		if(!getClackView().isVisibleView()) return;  // check to make sure we are the currently viewed level
		//  in the current type tab
    	Graphics2D g2 = (Graphics2D)graph.getGraphics();

		if(g2 == null){			return;		}
		double scale = graph.getScale();
		g2.scale(scale, scale);
    	graph.getUI().paintCell(g2, this, getBounds(), false);
    	//System.out.println("Trying to repaint " + ((ComponentCell)this.getCell()).getClackComponent().getName());
    }	
	
	// -----------------------------------------------------------------------
	//                             Rendering
	// -----------------------------------------------------------------------
	
	// Returns the Renderer for this View
	public CellViewRenderer getRenderer() {
		return renderer;
	}
	
	ComponentView.ComponentRenderer renderer = new ComponentRenderer();
	
	// Define the Renderer for an ComponentView
	class ComponentRenderer extends VertexRenderer 
	{
		public void paint(Graphics g) 
		{
			Graphics2D g2d = (Graphics2D)g;
			ComponentView vview = (ComponentView)view;
			ClackComponent clack_comp = ((ComponentCell)vview.getCell()).getClackComponent();
			
			
			Rectangle2D bounds = vview.getBounds();
			
			int strwidth = g.getFontMetrics().stringWidth(vview.mLabel);
			int width  = (int)vview.getBounds().getWidth();
			int height = (int)vview.getBounds().getHeight();
			


		
			g2d.setStroke(STROKE);
			if(clack_comp.hasError())
				g.setColor(ERROR_FILL);
			else
				g.setColor(vview.mFillColor);
			
			g2d.fillRect(sBORDER - 1, sBORDER - 1, 
					(int)view.getBounds().getWidth()  - sBORDER,
					(int)view.getBounds().getHeight() - sBORDER);
			g.setColor(Color.BLACK);
			
			g.setFont(vview.mFont);
			
			if(clack_comp instanceof Interface){
				strwidth = g.getFontMetrics().stringWidth(vview.mName);
				g.drawString(vview.mName, (int)(width/2) - (strwidth/2),(int) (height/4) + vview.mFontSize/2);  
				String ip_str = ((Interface)clack_comp).getIPAddress().getHostAddress();
				int ip_strwidth = g.getFontMetrics().stringWidth(ip_str);
				g.drawString(ip_str, (int)(width/2) - (ip_strwidth/2), (int) (3*height)/4 + vview.mFontSize/2);
			}else if(clack_comp instanceof TCPSocket){
				strwidth = g.getFontMetrics().stringWidth(vview.mLabel);
				g.drawString(vview.mLabel, (int)(width/2) - (strwidth/2),(int) (height/4) + vview.mFontSize/2); 
				TCB tcb = ((TCPSocket)clack_comp).getTCB();
				if(tcb.local_address != null){
					String ip_str = tcb.local_address.getHostAddress() + " : " + tcb.local_port;
					int ip_strwidth = g.getFontMetrics().stringWidth(ip_str);
					g.drawString(ip_str, (int)(width/2) - (ip_strwidth/2), (int) (3*height)/4 + vview.mFontSize/2);
				}
			} else if(clack_comp instanceof Ethereal){
				
				String eth_name = clack_comp.getName();
				int eth_width = g.getFontMetrics().stringWidth(eth_name);
				g.drawString(eth_name, (int)((width - eth_width)/2),(int) (height/2) + vview.mFontSize/2); 
				
			} else {
				g.drawString(vview.mLabel, (int)((width - strwidth)/2),(int) (height/2) + vview.mFontSize/2); 
			}
			
			// draw this last
			if(clack_comp.getPendingError()){
				g2d.setColor(ERROR_FILL);
				g2d.setStroke(ERROR_STROKE);
			} else {
				g2d.setStroke(STROKE);
				g2d.setColor(vview.mBorderColor);
			}
			

			g2d.drawRect(sBORDER - 1, sBORDER - 1, 
					(int)view.getBounds().getWidth()  - sBORDER,
					(int)view.getBounds().getHeight() - sBORDER);
			
		} // -- paint
		
	}  
} 
