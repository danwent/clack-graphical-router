/*
 * @(#)GPAboutDialog.java	2.0 1.2 11/11/02
 *
 * Copyright (C) 2001 Gaudenz Alder
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

package net.clackrouter.jgraph.pad;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import net.clackrouter.gui.ClackFrameworkHelper;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.jgraph.utils.HTMLPane;



public class GPAboutDialog extends JDialog {

	public GPAboutDialog(Frame owner, String title, ImageIcon logo) {
		super(owner, title, true);
		setSize(new Dimension(450, 320));

		JTabbedPane mainTabs = new JTabbedPane();
		JPanel aboutPanel = new JPanel();
		JPanel creditsPanel = new JPanel();
		mainTabs.addTab(Translator.getString("About"), aboutPanel);
		mainTabs.addTab(Translator.getString("Credits"), creditsPanel);
		getContentPane().add(mainTabs);
		setLocationRelativeTo(owner);
		setResizable(false);

		// Construct About Panel
		JLabel lab1 = new JLabel(logo);
		JLabel lab2 = new JLabel(ClackFrameworkHelper.VERSION);
		lab2.setFont(lab1.getFont().deriveFont(Font.PLAIN, 24));
		JLabel lab3 = new JLabel("Based on "+org.jgraph.JGraph.VERSION);
		lab3.setFont(lab3.getFont().deriveFont(Font.PLAIN, 12));

		JLabel lab5 =
			new JLabel("Java:"+System.getProperty("java.version")+" OS: "+System.getProperty("os.name"));
		lab5.setFont(lab5.getFont().deriveFont(Font.PLAIN, 12));
		lab1.setBounds(10, 9, 20, 24);
		lab2.setBounds(40, 5, 360, 30);
		lab3.setBounds(40, 33, 360, 25);
		HTMLPane text = new HTMLPane();
		text.setOpaque(false);
		text.setText(
			Translator.getString("AboutText"));
		text.setBounds(40, 65, 400, 140);
		text.setFont(lab3.getFont());
		text.setEditable(false);
		aboutPanel.setLayout(null);
		aboutPanel.add(lab1);
		aboutPanel.add(lab2);
		aboutPanel.add(lab3);
		aboutPanel.add(text);

		// Construct Credits Panel
		JTextArea credits = new JTextArea();
		creditsPanel.setLayout(new BorderLayout());
		creditsPanel.add(new JScrollPane(credits), BorderLayout.CENTER);
		credits.setOpaque(false);
		credits.setText("");
		credits.setCaretPosition(0);
		credits.setEditable(false);
	}

	// Close on escape
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setVisible(false);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}

}
