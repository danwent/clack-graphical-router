
package net.clackrouter.gui.util;


import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;


/**
 * Dialog window that allows the user to select which ports to connect
 * when they create a wire between two Clack components.    
 */
public class PortConnectionDialog extends JDialog
	                   implements   PropertyChangeListener {

	    private JOptionPane optionPane;

	    private ClackComponent mSource, mTarget;
	    private static String CONNECT = "Connect";
	    private static String CANCEL = "Cancel";
	    private ButtonGroup mSourceGroup, mTargetGroup;
	    private int mSourcePort, mTargetPort;
	    private boolean mIsValid = false;

	    /** Creates the reusable dialog. */
	    public PortConnectionDialog(Frame aFrame, ClackComponent source, ClackComponent target) {
	        super(aFrame, true);
	       	mSource = source;
	       	mTarget = target;

	        setTitle("Connecting " + source.getName() + " and " + target.getName());

	        JPanel buttonPanel = createRadioButtons();

	        Object[] array = {buttonPanel};

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
	        
			pack();
	        setLocationRelativeTo(aFrame);
	        setVisible(true);
	    }
	    
	    protected JPanel createRadioButtons() {
	    	JPanel buttonPanel = new JPanel();
	        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
	    	buttonPanel.add(new JLabel("Source Port:"));
	        mSourceGroup = new ButtonGroup(  );
	    	for(int i = 0; i < mSource.getNumPorts(); i++){
	    		ClackPort p = mSource.getPort(i);
	    		if(p.getDirection() == ClackPort.DIR_OUT){
	    			JRadioButton rbutton = new JRadioButton(p.getTextDescription());
	    			rbutton.setActionCommand("" + i);
	    			mSourceGroup.add(rbutton);
	    			buttonPanel.add(rbutton);
	    			if(mSourceGroup.getButtonCount() == 1)
	    				mSourceGroup.setSelected(rbutton.getModel(), true);
	    		}	
	    	}
	    	
	    	buttonPanel.add(new JLabel("Target Port:"));
	    	mTargetGroup = new ButtonGroup();
	    	for(int i = 0; i < mTarget.getNumPorts(); i++){
	    		ClackPort p = mTarget.getPort(i);
	    		if(p.getDirection() == ClackPort.DIR_IN){
	    			JRadioButton rbutton = new JRadioButton(p.getTextDescription());
	    			rbutton.setActionCommand("" + i);
	    			mTargetGroup.add(rbutton);
	    			buttonPanel.add(rbutton);
	    			if(mTargetGroup.getButtonCount() == 1)
	    				mTargetGroup.setSelected(rbutton.getModel(), true);
	    		}	
	    	}

	        return buttonPanel;
	    }


	    /** This method reacts to state changes in the option pane. */
	    public void propertyChange(PropertyChangeEvent e) {
	        String prop = e.getPropertyName();

	        if ( (e.getSource() == optionPane)
	         && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
	             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
	            Object value = optionPane.getValue();

	            if (value == JOptionPane.UNINITIALIZED_VALUE) {
	                //ignore reset
	                return;
	            }

	            //Reset the JOptionPane's value.
	            //If you don't do this, then if the user
	            //presses the same button next time, no
	            //property change event will be fired.
	            optionPane.setValue(
	                    JOptionPane.UNINITIALIZED_VALUE);

	            if (value != null && value.equals(CONNECT)) {
	                    mSourcePort = Integer.parseInt(mSourceGroup.getSelection().getActionCommand());
	                    mTargetPort = Integer.parseInt(mTargetGroup.getSelection().getActionCommand());
	            
	                    mIsValid = true;
	             }
	             setVisible(false);

	        }
	    }
	    
	    public int getSourcePort() { return mSourcePort; }
	    public int getTargetPort() { return mTargetPort; }
	    public boolean isValid() { return mIsValid; }

}
