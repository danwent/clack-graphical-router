/*
 * Copyright (C) 2001-2004 Gaudenz Alder
 *
 * JGraphpad is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGraphpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGraphpad; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package net.clackrouter.actions;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.router.graph.RouterGraph;

import org.jgraph.graph.DefaultGraphModel;



public class FileOpenURL extends AbstractActionFile {
	
	/**
	 * Constructor for FileOpen.
	 * @param graphpad
	 */
	public FileOpenURL(ClackFramework graphpad) {
		super(graphpad);
	}
	
	/** Shows a file chooser with the
	 *  file filters from the file formats
	 *  to select a file.
	 *
	 *  Furthermore the method uses the selected
	 *  file format for the read process.
	 *
	 *  @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String s = (String)JOptionPane.showInputDialog(
                graphpad.getFrame(),
                "Enter the remote URL",
                "Customized Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null, 
                null);

		System.out.println(s);
		
		if ((s == null) || (s.length() <= 0)) {
			return; // Exit if they don't type anything
		}
		
		// check if already open
		URL name;
		try {
			name = new URL(s);
		} catch (MalformedURLException eurl) {
			JOptionPane.showMessageDialog(graphpad.getFrame(), "Sorry, \"" + s + "\" is not a valid URL (did you forget the http://)");
			return;
		}
		if (name == null)
			return;
		
		// test confirms file not already open so open
		addDocument(name);
	}
	
	protected void addDocument(URL url) {
		final URL name = url;
		
		Thread t = new Thread("Read File Thread") {
			
			public void run() {

				RouterGraph gpGraph = new RouterGraph(new DefaultGraphModel());
				try {

					InputStream is = null;
					if(name != null){
						is = name.openStream();
					}
					if(is != null)
						graphpad.getTopologyManager().configureTopoModelFromXML(is);	
					
					graphpad.update();
					//System.err.println("opening files are disabled for now");
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(graphpad, ex
							.getLocalizedMessage(), Translator
							.getString("Error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
					
			}
		};
		t.start();
		
	}
	
	/** Empty implementation.
	 *  This Action should be available
	 *  each time.
	 */
	public void update() {
	};
	
}
