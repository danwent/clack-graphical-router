/*
 * @(#)FilePrint.java	1.2 30.01.2003
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
import java.awt.print.PrinterJob;

import net.clackrouter.gui.ClackFramework;


/**
 * Prints the current graph.
 */
public class FilePrint extends AbstractActionDefault {

	/**
	 * Constructor for FilePrint.
	 * @param graphpad
	 */
	public FilePrint(ClackFramework graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
			PrinterJob printJob = PrinterJob.getPrinterJob();
			printJob.setPrintable(graphpad.getCurrentDocument() );
			if (printJob.printDialog()) {
				try {
					boolean oldvalue = false;
					//	graphpad.getCurrentDocument().setScale(1);
					oldvalue = getCurrentGraph().isPageVisible();
					printJob.print();
				//	graphpad.getCurrentDocument() .setScale(oldscale);
					getCurrentGraph().setPageVisible(oldvalue);
				} catch (Exception printException) {
					printException.printStackTrace();
				}
			}
	}

}
