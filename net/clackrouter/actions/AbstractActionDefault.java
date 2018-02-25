/*
 * @(#)AbstractActionDefault.java	1.2 29.01.2003
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

import java.awt.Component;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import net.clackrouter.gui.ClackBarFactory;
import net.clackrouter.gui.ClackDocument;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.TranslatorConstants;
import net.clackrouter.jgraph.utils.Utilities;
import net.clackrouter.router.graph.RouterGraph;

import org.jgraph.graph.GraphLayoutCache;


/** An abstract JGraphpad action.
 * 	The base class for
 *  all JGraphpad actions.
 */
public abstract class AbstractActionDefault
	extends AbstractAction
	implements TranslatorConstants {

	/** A reference back to the graphpad.
	 *  If an action was performed the
	 *  Actions applies the changes to the current
	 *  Document at the graphpad.
	 *
	 *
	 */
	protected ClackFramework graphpad;

	/**
	 * Constructor for AbstractActionDefault.
	 * The Abstract action uses the class name
	 * without package prefix as action name.
	 *
	 * @see Action#NAME
	 *
	 */
	public AbstractActionDefault() {
		this((ClackFramework)null);
	}

	/**
	 * Constructor for AbstractActionDefault.
	 * The Abstract action uses the class name
	 * without package prefix as action name.
	 *
	 *
	 * @param graphpad The reference to the graphpad for this action
	 * @see Action#NAME
	 *
	 */
	public AbstractActionDefault(ClackFramework graphpad) {
		this.graphpad = graphpad;

		// build the name for this action
		// without the package prefix
		putValue(Action.NAME, Utilities.getClassNameWithoutPackage(getClass()));
	}


	/**
	 * Constructor for AbstractActionDefault.
	 *
	 * @param name Key for the name of this action
	 */

	public AbstractActionDefault(String name) {
		super(name);
	}
	/**
	 * Constructor for AbstractActionDefault.
	 *
	 * @param graphpad The reference to the graphpad for this action
	 * @param name Key for the name of this action
	 */

	public AbstractActionDefault(ClackFramework graphpad, String name) {
		super(name);
		this.graphpad = graphpad;
	}

	/**
	 * Constructor for AbstractActionDefault.
	 *
	 * @param graphpad The reference to the graphpad for this action
	 * @param name Key for the name of the action
	 * @param icon The icon for this action
	 */

	public AbstractActionDefault(ClackFramework graphpad, String name, Icon icon) {
		super(name, icon);
		this.graphpad = graphpad;
	}

	/**
	 * Constructor for AbstractActionDefault.
	 *
	 * @param name Key for the name of this action
	 * @param icon The icon for this action
	 */

	public AbstractActionDefault(String name, Icon icon) {
		super(name, icon);
	}

	/** Returns the name of the action
	 *
	 */
	public String getName() {
		return (String) getValue(NAME);
	}

	public RouterGraph getCurrentGraph() {
		return graphpad.getCurrentRouterGraph();
	}

	public GraphLayoutCache getCurrentGraphLayoutCache() {
		return graphpad.getCurrentDocument().getCurrentGraph().getGraphLayoutCache();
	}

	public void setSelectionAttributes(Map map) {
		if (graphpad != null && graphpad.getCurrentDocument() != null)
			graphpad.getCurrentDocument().setSelectionAttributes(map);
	}


	public ClackDocument getCurrentDocument() {
		return graphpad.getCurrentDocument();
	}

	/** Creates by default an arry with one
	 *  entry. The entry contains a JMenuItem
	 *  which joins the instance of this Action.
	 */
	public Component[] getMenuComponents() {
		return new Component[] { getMenuComponent(null)};
	}

	/** Returns by default a list with one JButton.
	 *  The button joints this action.
	 *
	 *
	 */
	public Component[] getToolComponents() {
		return new Component[] { getToolComponent(null)};
	}

	/** Returns a JMenuItem with a link to this action.
	 */
	protected Component getMenuComponent(String actionCommand) {
		JMenuItem item = new JMenuItem(this);
		ClackBarFactory.fillMenuButton(item, getName(), actionCommand);
		String presentationText = getPresentationText(actionCommand);
		if (presentationText != null)
			item.setText(presentationText);

		return item;
	}

	/** Returns a clean JButton which has a link to this action.
	 *
	 */
	protected Component getToolComponent(String actionCommand) {
		AbstractButton b = new JButton(this) {
			public float getAlignmentY() {
				return 0.5f;
			}
		};
		return ClackBarFactory.fillToolbarButton(
			b,
			getName(),
			actionCommand);
	}

	/** empty implementation for this typ of action
	 *
	 */
	public void update() {
		if (graphpad.getCurrentDocument() == null)
			setEnabled(false);
		else
			setEnabled(true);
	}

	/** Should return presentation Text for the
	 *  action command or null
	 *  for the default
	 */
	public String getPresentationText(String actionCommand) {
		return null;
	}

	/**
	 * Sets the graphpad.
	 * @param graphpad The graphpad to set
	 */
	public void setGraphpad(ClackFramework graphpad) {
		this.graphpad = graphpad;
	}

	/**
	 * Returns the graphpad.
	 * @return GPGraphpad
	 */
	public ClackFramework getGraphpad() {
		return graphpad;
	}

}
