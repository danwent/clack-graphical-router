
package net.clackrouter.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.clackrouter.gui.ClackDocument;
import net.clackrouter.router.core.Router;
import net.clackrouter.router.core.TimeManager;



/**
 * Represents the slider bar on the side of clack router frame. 
 * 
 * <p> Router speed on the slider is the opposite of speed inside the router.
 * In the sider, higher speeds are faster, but these values are inverted by 
 * subtracting them from MAX_SPEED before setting the speed on the router, since
 * within the router "speed" is the delay between processing by each different
 * router component. </p>
 */
public class RouterSpeedSlider extends JPanel {
	
	ClackDocument mDocument;
	JSlider mSlider;

	public static int DEFAULT_SLIDER = TimeManager.MAX_SLOWDOWN;
	
	public static String PLAY = ">";
	public static String PAUSE = "||";
	public static String STEP = "Step";
	public static String CONT = "C";
	
	private JButton playButton, pauseButton, stepButton, contButton;
	private JLabel clock;
	
	public RouterSpeedSlider(ClackDocument doc){
		super();
		mDocument = doc;
		mSlider = new JSlider(JSlider.VERTICAL, TimeManager.MIN_SLOWDOWN, TimeManager.MAX_SLOWDOWN, DEFAULT_SLIDER);		
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		mSlider.addChangeListener(new SpeedChangeListener());
		clock = new JLabel("0");
		
		ButtonPressed bp = new ButtonPressed();
        playButton = new JButton(PLAY);
        playButton.setToolTipText("Play");
        playButton.setEnabled(false);
        playButton.addActionListener(bp);
        pauseButton = new JButton(PAUSE);
        pauseButton.setToolTipText("Pause");
        pauseButton.addActionListener(bp);
        stepButton = new JButton(STEP);
        stepButton.addActionListener(bp);
  /*      contButton = new JButton(CONT);
        contButton.addActionListener(bp);
	*/
        add(new JLabel("Time:"));
        add(clock);
        mDocument.getFramework().getTimeManager().setClock(clock);
		mSlider.setValue(TimeManager.MAX_SLOWDOWN - mDocument.getFramework().getTimeManager().getDelay());    
        add(playButton);
        add(pauseButton);
   //     add(stepButton);
    //    add(contButton);
		add(new JLabel("Max"));
		add(new JLabel("Speed"));
		add(mSlider);
		add(new JLabel("Min"));
		add(new JLabel("Speed"));		
		
	}
	
	
	private class SpeedChangeListener implements ChangeListener {
			public void stateChanged(ChangeEvent e) {
		
				
		    JSlider source = (JSlider)e.getSource();
		    if (!source.getValueIsAdjusting()) {
		    	int delay = TimeManager.MAX_SLOWDOWN - (int)source.getValue();
		    	System.out.println("setting speed to: " + delay);
		    	
		         mDocument.getFramework().getTimeManager().setDelay(delay);
		    }
		}
		
	}

    private class ButtonPressed implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
        	
            Object source = event.getSource();
            if(source == playButton){
            	System.out.println("play!");
            	playButton.setEnabled(false);
            	pauseButton.setEnabled(true);
            	mDocument.getFramework().getTimeManager().Play();
            }else if(source == pauseButton){
            	System.out.println("pause");
            	playButton.setEnabled(true);
            	pauseButton.setEnabled(false);
            	mDocument.getFramework().getTimeManager().Pause();
            }else if(source == stepButton){
            	System.out.println("Step");
            	mDocument.getFramework().getTimeManager().Step();
            } else if(source == contButton){
            	System.out.println("Continue!");
            	mDocument.getFramework().getTimeManager().Continue();
            }else {
            	System.err.println("RouterSpeedSlider: unkown event source");
            }
       
            
        }
    } 


}
