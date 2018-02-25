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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import net.clackrouter.actions.AbstractActionList;
import net.clackrouter.router.graph.RouterGraph;

import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.CellView;


public class GraphListCellRenderer extends DefaultListCellRenderer {
	
	protected static RouterGraph dummyGraph = new RouterGraph();
	
	/** reference to the combobox for this renderer
	 */
	protected AbstractActionList action;
	
	/**
	 * Constructor for GraphListCellRenderer.
	 */
	public GraphListCellRenderer(AbstractActionList action) {
		this.action = action;
	}
	
	/**
	 */
	public Component getListCellRendererComponent(
			JList list,
			Object view,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
		
		if (view instanceof String){
			return new JLabel((String)view);
		}
		
		JComponent c =
			new RealGraphCellRenderer(
					dummyGraph ,
					new CellView[] {(CellView) view });
		Rectangle2D b = ((AbstractCellView) view).getBounds();
		if (b != null)
			c.setBounds(2, 2, (int)b.getWidth(), (int)b.getHeight());
		c.setPreferredSize(new Dimension((int)b.getWidth() , (int)b.getHeight() ));
		c.setOpaque(true);
		c.setBackground(dummyGraph .getBackground());
		return c;
	}
	
}
