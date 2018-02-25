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
package net.clackrouter.jgraph.pad;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import net.clackrouter.gui.ClackDocument;


/**
 * 
 * Undo listener for one document. When a UndoableEditEvent is fired on a
 * listened object undoableEditHappened will be called. The UndoableEditEvent
 * will contain information about the latest edit and store this in the
 * undo command history
 */
public class UndoHandler implements UndoableEditListener {
	/**
	 * <code>document</code> is used by the UndoHandler to determine the
	 * document that undos/rados are actioned upon
	 */
	ClackDocument document;
	
	public UndoHandler(ClackDocument document) {
		this.document = document;
	}
	
	/**
	 * Messaged when the Document has created an edit, the edit is
	 * added to <code>graphUndoManager</code>, an instance of UndoManager.
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		document.getGraphUndoManager().addEdit(e.getEdit());
		// Update state of undo and redo buttons to reflect whether or not
		// there are actions in the history to undo/redo
		document.getFramework().getEditUndoAction().update();
		document.getFramework().getEditRedoAction().update();
	}
	
}
