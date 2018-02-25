/*
 * Created on Dec 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.gui.util;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.router.core.RouterConfig;



/**
 * Currently unused, but accepts basic input from user to open a single router,
 * by specifying all of the connection parameters.  
 */
public class OpenRouterDialog  extends JDialog
    implements   PropertyChangeListener {

	private JOptionPane optionPane;

	private static String CONNECT = "Connect";
	private static String CANCEL = "Cancel";

	private boolean mIsValid = false;
	private JTextField mServerField, mPortField, mTopoField, mHostField;
	
	/** Creates the reusable dialog. */
	public OpenRouterDialog(Frame aFrame) {
		super(aFrame, true);
		
		setTitle("Open VNS Connection");
		
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));
		main.add(new JLabel("VNS Server:"));
		mServerField = new JTextField(Translator.getString("VNS_SERVER_ADDRESS"));
		main.add(mServerField);
		main.add(new JLabel("Server Port:"));
		mPortField = new JTextField(Translator.getString("VNS_PORT"));
		main.add(mPortField);
		main.add(new JLabel("Topology:"));
		mTopoField = new JTextField("");
		main.add(mTopoField);
		main.add(new JLabel("Virtual Host:"));
		mHostField = new JTextField("vrhost");
		main.add(mHostField);		
		
		Object[] array = {main};
		
		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = {CONNECT,CANCEL};
		
		//Create the JOptionPane.
		optionPane = new JOptionPane(array,
		                     JOptionPane.PLAIN_MESSAGE,
		                     JOptionPane.YES_NO_OPTION,
		                     null,
		                     options,
		                     options[0]);
		
		//Make this dialog display it.
		setContentPane(optionPane);
		
		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		 public void windowClosing(WindowEvent we) {
		 /*
		  * Instead of directly closing the window,
		  * we're going to change the JOptionPane's
		  * value property.
		  */
		     optionPane.setValue(new Integer(
		                         JOptionPane.CLOSED_OPTION));
		}
		});
		
		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
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
					Integer.parseInt(mPortField.getText());
					mIsValid = true;
				}catch (Exception exc){
					exc.printStackTrace(); /* do not report */
				}
				
			}
			setVisible(false);

		}
	}
	
	public RouterConfig.ConnectionInfo getConnectionInfo() {
		RouterConfig.ConnectionInfo cInfo = new RouterConfig.ConnectionInfo();
		cInfo.server = getServer();
		cInfo.host = getHost();
		cInfo.port = getPort();
		cInfo.topo_max = getTopology();
		cInfo.topo_min = getTopology();
		return cInfo;
	}

	public int getPort() {
		return Integer.parseInt(mPortField.getText());
	}

	public int getTopology() {
		return Integer.parseInt(mTopoField.getText());
	}
	
	public String getServer() { return mServerField.getText(); }
	public String getHost() { return mHostField.getText(); }

	public boolean isValid() {
		return mIsValid;
	}

}

