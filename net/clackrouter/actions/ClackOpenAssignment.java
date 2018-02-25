package net.clackrouter.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.tutorial.ClackAssignment;

public class ClackOpenAssignment extends AbstractActionFile {
	
	public ClackOpenAssignment(ClackFramework graphpad) {
		super(graphpad);
	}
	
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(Translator.getString("openLabel"));
	
		// return if cancel
		int result = chooser.showOpenDialog(graphpad);
		if (result == JFileChooser.CANCEL_OPTION)
		return;
	
		try {
			File f = chooser.getSelectedFile();
			ClackAssignment assign = new ClackAssignment(f);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
