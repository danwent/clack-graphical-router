package net.clackrouter.actions;

import java.awt.event.ActionEvent;

import net.clackrouter.gui.ClackFramework;

public class ClackToggleRouteTableView extends AbstractActionDefault {

	/**
	 * Constructor for ViewScaleZoomIn.
	 * @param graphpad
	 */
	public ClackToggleRouteTableView(ClackFramework graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */

		public void actionPerformed(ActionEvent e) {
			this.graphpad.getCurrentDocument().toogleTopoRouteTableViewMode();
		}


}
