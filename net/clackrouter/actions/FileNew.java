/*
 * @(#)FileNew.java	1.2 30.01.2003
 *
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

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.GPSelectProvider;


public class FileNew extends AbstractActionDefault {

    
	/**
	 * Constructor for FileNew.
	 * @param graphpad
	 */
	public FileNew(ClackFramework graphpad) {
		super(graphpad);
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		GPSelectProvider selectProvider = new GPSelectProvider(graphpad.getFrame() );
		selectProvider.show() ;
		if (selectProvider.getAnswer() == GPSelectProvider.OPTION_CANCEL )
			return;

	}
	/** Empty implementation.
	 *  This Action should be available
	 *  each time.
	 */
	public void update(){};
	
}
