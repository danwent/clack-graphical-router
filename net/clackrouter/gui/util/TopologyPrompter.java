/*
 * Created on Sep 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.gui.util;import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Simple dialog class to prompt the user for what topology they want to 
 * connect to.  
 */
public class TopologyPrompter extends JDialog implements PropertyChangeListener {

	private JOptionPane optionPane;

	private static String CONNECT = "Connect";

	private static String CANCEL = "Cancel";


	private JTextField mTopoField;
	private boolean mIsValid = false;

	/** Creates the reusable dialog. */
	public TopologyPrompter(Frame aFrame, boolean failed) {
		super(aFrame, true);

		setTitle("Topology Selection");

		JPanel main = new JPanel();
		//main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));

		if(failed)
			main.add(new JLabel("Failed to Connect to VNS Topology. Enter a new VNS Topology Number:"));
		else
			main.add(new JLabel("Enter Your VNS Topology Number:"));
		mTopoField = new JTextField("", 5);	
		main.add(mTopoField);
		
		Object[] array = {main};

		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = { CONNECT, CANCEL };

		//Create the JOptionPane.
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.YES_NO_OPTION, null, options, options[0]);

		//Make this dialog display it.
		setContentPane(optionPane);

		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window, we're going to change
				 * the JOptionPane's value property.
				 */
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});

		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);

		pack();
		setLocationRelativeTo(aFrame);
		setVisible(true);
	}

	protected JPanel createRadioButtons() {

		return new JPanel();
	}

	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if ((e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
						.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}

			//Reset the JOptionPane's value.
			//If you don't do this, then if the user
			//presses the same button next time, no
			//property change event will be fired.
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

			if (value != null && value.equals(CONNECT)) {
				try {
					Integer.parseInt(mTopoField.getText());
					mIsValid = true;
				} catch (Exception ex) { 
					ex.printStackTrace();
				}
			}
			setVisible(false);

		}
	}

	public int getTopology() { return Integer.parseInt(mTopoField.getText()); }

	public boolean isValid() {
		return mIsValid;
	}

}