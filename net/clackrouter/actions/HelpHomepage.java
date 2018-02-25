/*
 * @(#)HelpAbout.java	1.2 01.02.2003
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

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.jgraph.utils.BrowserLauncher;



public class HelpHomepage extends AbstractActionDefault {

	/** The about dialog for JGraphpad
	 */
	protected JDialog aboutDlg;

	/**
	 * Constructor
	 * @param graphpad
	 */
	public HelpHomepage(ClackFramework graphpad) {
		super(graphpad);
	}

	/** Opens the url <a href="http://www.jgraph.com/">http://www.jgraph.com/</a>.
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		try {
			BrowserLauncher.openURL("http://www.jgraph.com/");
		} catch (Exception ex){
			JOptionPane.showMessageDialog(graphpad, ex.toString(), Translator.getString("Error"), JOptionPane.ERROR_MESSAGE );
		}

	}
	/** Empty implementation.
	 *  This Action should be available
	 *  each time.
	 */
	public void update(){};

}
