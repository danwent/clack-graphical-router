/*
 * Created on May 29, 2005
 *
 */
package net.clackrouter.actions;

import java.awt.event.ActionEvent;

import net.clackrouter.gui.ClackFramework;



/**
 * An action class to show the logging console to see Clack debugging information
 */
public class ClackShowConsole extends AbstractActionDefault {

	/**
	 * Constructor for ViewScaleZoomIn.
	 * @param graphpad
	 */
	public ClackShowConsole(ClackFramework graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */

		public void actionPerformed(ActionEvent e) {
			this.graphpad.getLogConsole().setVisible(true);
		}

}
