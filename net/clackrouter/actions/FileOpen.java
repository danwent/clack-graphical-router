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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;



public class FileOpen extends AbstractActionFile {
	
	/**
	 * Constructor for FileOpen.
	 * @param graphpad
	 */
	public FileOpen(ClackFramework graphpad) {
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
		// show the file chooser
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(Translator.getString("openLabel"));
		
		// return if cancel
		int result = chooser.showOpenDialog(graphpad);
		if (result == JFileChooser.CANCEL_OPTION)
			return;
		
		// check if already open
		URL name;
		try {
			name = chooser.getSelectedFile().toURL();
		} catch (MalformedURLException eurl) {
			return;
		}
		if (name == null)
			return;
		
		// test confirms file not already open so open
		addDocument(chooser);
	}
	
	protected void addDocument(JFileChooser chooser) {
		// get the file format, provider
		final File file = chooser.getSelectedFile();
		
		Thread t = new Thread("Read File Thread") {
			
			public void run() {

				try {

					InputStream is = null;
					if(file != null){
						is = new FileInputStream(file);
					}
					if(is != null)
						graphpad.getTopologyManager().configureTopoModelFromXML(is);	
					
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
