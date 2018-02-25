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

import javax.swing.JOptionPane;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.util.TopologyPrompter;
import net.clackrouter.jgraph.pad.resources.Translator;


public class FileConnect extends AbstractActionFile {
	
	/**
	 * Constructor for FileConnect.
	 * @param graphpad
	 */
	public FileConnect(ClackFramework graphpad) {
		super(graphpad);
	}
	
	public void actionPerformed(ActionEvent e) {
		TopologyPrompter prompter = new TopologyPrompter(graphpad.getFrame(), false);
		if(prompter.isValid()){
			openTopo(prompter.getTopology());
		}else {
			System.err.println("Error, invalid topology");
		}
	}
	
	protected void openTopo(int topo) {

		final int topo_num = topo;
		Thread t = new Thread("Read File Thread") {
			
			public void run() {

				//RouterGraph gpGraph = new RouterGraph(new DefaultGraphModel());
				try {


						graphpad.getTopologyManager().reloadTopologyFile(topo_num)	;

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
