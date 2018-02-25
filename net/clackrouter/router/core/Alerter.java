
package net.clackrouter.router.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

/**
 * Simple "alarm clock" like class that gives an alert on regular intervals to any
 * classes that registered to be notified.  
 */
public class Alerter {

	private ArrayList mActiveAlerts = new ArrayList();;
	private ArrayList mNextAlerts = new ArrayList();
    private Timer mTimer;
    
    public Alerter(int alert_time){
		mTimer = new Timer(alert_time, new TimerCallback());
		mTimer.start();
    }
    
	public void addAlerter(Alertable a){
		mNextAlerts.add(a);
	}

	
	public class TimerCallback implements ActionListener {
	      public void actionPerformed(ActionEvent evt) {
	      		while(mActiveAlerts.size() > 0){
	      			Alertable alertable = (Alertable)mActiveAlerts.remove(0);
	      			alertable.alert();
	      		}
	      		// swap
	      		ArrayList tmp = mActiveAlerts;
	      		mActiveAlerts = mNextAlerts;
	      		mNextAlerts = tmp;
	      }
	}
	
	/**
	 * Simple interface for callback to an component that is being "alerted".
	 */
	public interface Alertable {
		public void alert();
	}
}
