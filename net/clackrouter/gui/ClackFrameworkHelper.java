/*
 * Created on Sep 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import net.clackrouter.error.ErrorReporter;
import net.clackrouter.jgraph.pad.resources.ImageLoader;
import net.clackrouter.jgraph.utils.Utilities;
import net.clackrouter.tutorial.ClackTutorial;


public class ClackFrameworkHelper {
	
	public static final String VERSION = "Clack (v1.5)";
	public static final String LOGO = "clack-small.png";
	
	// command link options
	public static final String LOAD_FILE = "loadfile";
	public static final String LOAD_URL = "loadurl";
	public static final String TUTORIAL = "tutorial";
	public static final String SHOW_CONSOLE = "showconsole";
	public static final String SIMPLE_GRAPH = "simplegraph";
	public static final String DEBUG = "debug";
	
	public static ClackFramework configureClackFramework(String[] args, ClackFramework framework) {

		String logo_name = LOGO;
		
		JWindow splashframe = null;
		Properties props = new Properties();
		try {	
			
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equalsIgnoreCase("-f") && args.length >= i + 1)
						props.setProperty(LOAD_FILE, args[i + 1]);
					else if (args[i].equalsIgnoreCase("-u") && args.length >= i + 1)
						props.setProperty(LOAD_URL, args[i + 1]);
					else if(args[i].equalsIgnoreCase("-t") && args.length >= i + 1)
						props.setProperty(TUTORIAL, args[i + 1]);
					else if(args[i].equalsIgnoreCase("-c"))
						props.setProperty(SHOW_CONSOLE, "true");
					else if(args[i].equalsIgnoreCase("-s")) // TODO: what does this do anymore?
						props.setProperty(SIMPLE_GRAPH, "true");
					else if(args[i].equalsIgnoreCase("-d")) // TODO: what does this do anymore?
						props.setProperty(DEBUG, "true");
					else if(args[i].equalsIgnoreCase("-m"))
						logo_name = "crazy_wires.jpg"; // easter egg for martin
				}
			}
			splashframe = showSplashScreen(logo_name);
			framework.initializeAndShow();
			
			if(props.getProperty(LOAD_URL) == null && props.getProperty(LOAD_FILE) == null){
				printUsage();
			//	JOptionPane.showMessageDialog(pad,"No configuration file or URL provided",
			//			"Error", JOptionPane.ERROR_MESSAGE);
			//	return null;
			}
			
			if(props.getProperty(SHOW_CONSOLE) != null)
				framework.getLogConsole().setVisible(true);
			
			framework.setIsSimpleGraph(props.getProperty(SIMPLE_GRAPH) != null);
			String tutorial_str = props.getProperty(TUTORIAL);
			if(tutorial_str != null){
				ClackTutorial ct = new ClackTutorial(tutorial_str);
			}
			
			InputStream is = null;
			String loadfile = props.getProperty(LOAD_FILE);
			String loadurl = props.getProperty(LOAD_URL);
			if(loadfile != null){
				is = new FileInputStream(loadfile);
			}else if(loadurl != null){
				is = (new URL(loadurl)).openStream();
			}
			
			framework.setDebug(props.getProperty(DEBUG) != null);
				
			
			// I have to do this here otherwise we can't see the popup windows
			splashframe.dispose(); 
			if(is != null)
				framework.getTopologyManager().configureTopoModelFromXML(is);	
	
			framework.update();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework,"Fatal Error While Loading Clack",
					"Error", JOptionPane.ERROR_MESSAGE);
			if(framework != null)
				ErrorReporter.reportError(framework, "Error Loading Clack.  Terminating...");
			else
				ErrorReporter.reportError("Error Loading Clack Application", e);
		} finally {
			if(splashframe != null)
				splashframe.dispose();
		}	
		return framework;
	}
	
	private static JWindow showSplashScreen(String logo_name){
		// Logo
		ImageIcon logoIcon = ImageLoader.getImageIcon(logo_name);
		
		JWindow frame = new JWindow();
		
		JLabel info = new JLabel();
		Font f = new Font("Arial", Font.BOLD, 12);
		info.setFont(f);
		info.setText("Initializing...");
		info.setHorizontalAlignment(JLabel.CENTER);
		info.setForeground(Color.black);
		JLabel lab = new JLabel(logoIcon) {
			public void paint(Graphics g) {
				super.paint(g);
				
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				
				g2.setFont(new Font("Arial", Font.BOLD, 27));
				g2.setColor(Color.WHITE.darker());
				Composite originalComposite = g2.getComposite();
				g2.setComposite(
						AlphaComposite.getInstance(
								AlphaComposite.SRC_OVER,
								0.5f));

				g2.setFont(new Font("Arial", Font.PLAIN, 10));
				g2.drawString(VERSION,18,getHeight() - 10);	
			}
		};
		
		frame.getContentPane().add(lab, BorderLayout.CENTER);
		frame.getContentPane().setBackground(Color.WHITE);
		lab.setLayout(new BorderLayout());
		lab.add(info, BorderLayout.NORTH);
		lab.setBorder(BorderFactory.createRaisedBevelBorder());
		info.setVerticalAlignment(SwingConstants.CENTER);
		info.setHorizontalAlignment(SwingConstants.CENTER);
		info.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		info.setPreferredSize(new Dimension(lab.getWidth(), 24));
		info.setForeground(Color.BLACK);
		frame.pack();
		Utilities.center(frame);
		frame.show();
		info.setText("Starting...");
		return frame;
	}
	
	private static void printUsage() {
		System.out.println("Clack Framework Usage");
		System.out.println("-f <local file> : load topology configuration file");
		System.out.println("-u <http URL>   : load topology configuration file over the web");
		System.out.println("-t <http URL>   : show tutorial frame displaying page specified by URL");
		System.out.println("-c 	: show debugging console automatically on load");
	}

}
