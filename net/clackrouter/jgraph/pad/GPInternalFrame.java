

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

/*
package org.jgraph.pad;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JInternalFrame;

import org.jgraph.GPGraphpad;
import org.jgraph.utils.gui.LocaleChangeAdapter;
import org.jgraph.utils.gui.PositionManager;

*/


/**
 * The graphpad internal frame is a container
 * is a container for one GPDocument.
 * There is a split between a document and
 * the joint internal frame, sothat you can use
 * the document without using internal frames.
 */

//public class GPInternalFrame extends org.jgraph.utils.gui.GPInternalFrame {

	/** A link to the Graphpad Document of this frame
	 */
//	GPDocument document;

	/**
	 * Constructor for GPInternalFrame.
	 */
/*
	public GPInternalFrame(GPDocument document) {
		super(document.getFrameTitle(), true, true, true, true);
		this.setFrameIcon(GPGraphpad.getApplicationIcon());
		this.document = document;
		this.document.setInternalFrame(this);
		this.getContentPane().add(document);
		this.addVetoableChangeListener(new GPVetoableListner(document));
		//this.setPreferredSize(new Dimension(600, 400));
		//this.pack();
	}
*/
	/*
	public GPInternalFrame(HierachComponent comp) {
		super(comp.getFrameTitle(), true, true, true, true);
		this.setFrameIcon(GPGraphpad.getApplicationIcon());
		this.document.setInternalFrame(this);
		this.getContentPane().add(comp);
		this.addVetoableChangeListener(new GPVetoableListner(document));
		//this.setPreferredSize(new Dimension(600, 400));
		//this.pack();
	}
	*/

	/**
	 * Returns the document.
	 * @return GPDocument
	 */
/*
	public GPDocument getDocument() {
		return document;
	}
*/

	/**
	 * Sets the document.
	 * @param document The document to set
	 */
/*
	public void setDocument(GPDocument document) {
		this.remove(this.document);
		this.document = document;
		this.add(this.document);
		//this.pack();
	}
*/

	/**
	 * 
	 * Cleans up references that cause memory leaks
	 * Maintainance fix, rather than proper fix
	 */
/*
	public void cleanUp(){
		this.document = null;
		LocaleChangeAdapter.removeContainer(this);
		PositionManager.removeComponent(this);
	}

}
*/

/*class GPVetoableListner implements VetoableChangeListener {

	GPDocument document;

	GPVetoableListner(GPDocument doc) {
		this.document = doc;
	}
	//
	 // @see javax.swing.event.InternalFrameListener#internalFrameClosing(InternalFrameEvent)
	 //
	public void vetoableChange(PropertyChangeEvent evt)
		throws PropertyVetoException {
		if (evt.getPropertyName() != JInternalFrame.IS_CLOSED_PROPERTY)
			return;

		if (((Boolean)evt.getNewValue()).booleanValue() && document.close(true)){
				document.getGraphpad().removeDocument(document);
		} else {
			throw new PropertyVetoException("Can't close the Internal Frame", evt) ;
		}
	}

}
*/

