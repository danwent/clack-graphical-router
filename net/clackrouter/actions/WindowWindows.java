/*
 * @(#)WindowWindows.java	1.2 09.02.2003
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

import javax.swing.JMenu;

import net.clackrouter.gui.ClackFramework;


public class WindowWindows
	extends AbstractActionList {

	/** a list of the menus
	 */
//	Vector menus = new Vector();

	/**
	 * Constructor for WindowWindows.
	 * @param graphpad
	 */
	public WindowWindows(ClackFramework graphpad) {
		super(graphpad);
	}

	/**Returns an empty item list by default.
	 *
	 * @see net.clackrouter.actions.AbstractActionList#getItems()
	 */
	protected Object[] getItems() {
		return new Object[] {
		};
	}

	/**
	 * Gets the GPInternalFrame from the ActionEvent and sets the
	 * frame toFront and selected.
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
//		GPInternalFrame gpframe = (GPInternalFrame) this.getSelectedItem(e);
//		gpframe.toFront();
//		try {
//			gpframe.setSelected(true);
//		} catch (PropertyVetoException pve) {
//		}
	}

	/**Returns a JMenu and stores the JMenu at the menus Vector
	 *
	 * @see net.clackrouter.actions.AbstractActionList#getMenuBarComponent()
	 */
	protected JMenu getMenuBarComponent() {
//		JMenu menu = new JMenu(this);
//		menu.setName(getName());
//		menus.add(menu);
		return null; //menu;
	}

	/** returns the actionCommand (The presentation file name from the document)
	 */
	public String getPresentationText(String actionCommand) {
		return actionCommand;
	}

	/** returns null
	 */
	public String getItemPresentationText(Object itemValue) {
		return null;
	}

	/** updates the window list at the menu entries
	 *
	 */
	public void update(){
//		super.update() ;

//		JInternalFrame[] iframes = graphpad.getAllFrames();

//		for (int j = 0; j < menus.size(); j++) {
//			JMenu menu = (JMenu) menus.get(j);
//			menu.removeAll();

//			for (int i = 0; i < iframes.length; i++) {
//				GPInternalFrame internalFrame = (GPInternalFrame) iframes[i];
//				menu.add(
//					getMenuComponent(
//						internalFrame.getDocument().getFrameTitle(),
//						internalFrame));
//			}
//		}
	}

}
