package net.clackrouter.gui.util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.topology.core.TopologyModel;

public class HostnamePrompter extends JDialog
implements   PropertyChangeListener {
	
    private JOptionPane optionPane;
    private String mPrompt = "Please select a host from the topology";
    private static String SELECT = "Select";
    private static String CANCEL = "Cancel";
    private ButtonGroup mGroup;
    private String mSelectedHost;
    private boolean mIsValid = false;

    /** Creates the reusable dialog. */
    public HostnamePrompter(ClackFramework framework, String p) {
        super(framework.getFrame(), true);
        
        if(p != null) mPrompt = p;
        
        JPanel buttonPanel = createRadioButtons(framework);

        if(!mIsValid){ // don't bother if this is a trivial selection
        	Object[] array = {buttonPanel};
        	Object[] options = {SELECT,CANCEL};

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
                    optionPane.setValue(new Integer(
                                        JOptionPane.CLOSED_OPTION));
        		}
        	});

        	//Register an event handler that reacts to option pane state changes.
        	optionPane.addPropertyChangeListener(this);
        
        	pack();
        	setLocationRelativeTo(framework.getFrame());
        	setVisible(true);
        }
    }
    
    protected JPanel createRadioButtons(ClackFramework graphpad) {
    	
		try {
		   	JPanel buttonPanel = new JPanel();
	        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
			int topo_num = graphpad.getCurrentDocument().getTopology();
			TopologyModel model = graphpad.getTopologyManager().getTopologyModel(topo_num, null);
			ArrayList all_hosts = model.getHosts();
			
			mGroup = new ButtonGroup(  );
			
	    	buttonPanel.add(new JLabel(mPrompt));
			
			for(int i = 0; i < all_hosts.size(); i++){
				TopologyModel.Host h = (TopologyModel.Host) all_hosts.get(i);
				if(!h.reservable) continue;
				
    			JRadioButton rbutton = new JRadioButton(h.name);
    			rbutton.setActionCommand(h.name);
    			mGroup.add(rbutton);
    			buttonPanel.add(rbutton);
    			if(mGroup.getButtonCount() == 1)
    				mGroup.setSelected(rbutton.getModel(), true);
			}
			
			if(mGroup.getButtonCount() == 1){
                mSelectedHost = mGroup.getSelection().getActionCommand();
                mIsValid = true;			
			}
			
	        return buttonPanel;
	        
		} catch (Exception ex){
				ex.printStackTrace();
		}
    	
		return null;
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

            if (value != null && value.equals(SELECT)) {
                    mSelectedHost = mGroup.getSelection().getActionCommand();
                    mIsValid = true;
             }
             setVisible(false);

        }
    }
    
    public String getSelectedHost() { return mSelectedHost; }
    public boolean isValidResponse() { return mIsValid; }


}
