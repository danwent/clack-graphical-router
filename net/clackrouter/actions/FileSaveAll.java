/*
 * @(#)FileSaveAs.java	1.2 30.01.2003
 *
 * Copyright (C) 2001-2004 Gaudenz Alder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package net.clackrouter.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.clackrouter.actions.FileSave.ClackFileFilter;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.topology.core.TopoSerializer;



public class FileSaveAll extends FileSave {

	/**
	 * Constructor for FileSave.
	 * @param graphpad
	 */
	public FileSaveAll(ClackFramework graphpad) {
		super(graphpad);
	}

	/** Shows a file chooser if the filename from the
	 *  document is null. Otherwise the method
	 *  uses the file name from the document.
	 *
	 *  Furthermore the method uses the registered
	 *  file formats for the save process.
	 */
	public void actionPerformed(ActionEvent e) {
		int topology = getCurrentDocument().getTopology();

		File file;

			// show the file chooser
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(Translator.getString("FileSaveAsLabel"));

			int result = chooser.showSaveDialog(graphpad);
			if (result == JFileChooser.CANCEL_OPTION)
				return;

//			chooser.setFileFilter(new ClackFileFilter(true, false));

			try {
				file = chooser.getSelectedFile();
				if(file == null) { 
					System.out.println("file null");
					return;
				}

			} catch (Exception eurl) {
				eurl.printStackTrace();
				JOptionPane.showMessageDialog(
					graphpad,
					"Error selecting file from file-chooser: " + eurl.getLocalizedMessage(),
					Translator.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
				return;
			}

		// try to write the file
		try {
			System.out.println("saving topology " + topology + " to file " + file.getName());
			TopoSerializer.serializeTopology(graphpad, topology, file, true);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(
				graphpad,
				"Error writing file to disk: " + ex.getLocalizedMessage(),
				Translator.getString("Error"),
				JOptionPane.ERROR_MESSAGE);
		} finally {
			graphpad.update();
			graphpad.invalidate();
		}

	}
	
	public static class ClackFileFilter extends FileFilter {
		private boolean acceptTopo, acceptRouter;
		public ClackFileFilter(boolean topo, boolean router){
			acceptTopo = topo;
			acceptRouter = router;
		}
		/**
		 * @see javax.swing.filechooser.FileFilter#accept(File)
		 */
		public boolean accept(File f) {
			if (f == null)
				return false;
			if (f.getName() == null)
				return false;
			if (acceptTopo && f.getName().endsWith(".topo"))
				return true;
			if (acceptRouter && f.getName().endsWith(".router"))
				return true;
			if (f.isDirectory())
				return true;

			return false;
		}

		/**
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		public String getDescription() {
			return "Clack File Filter"; 
		}
	};

}
