package net.clackrouter.router.core;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JLabel;

public class TimeManager {
	
    /**
     * Default delay is max-speed (ie: zero delay)
     */
    private static int DEFAULT_TICK_DELAY = 0;
    private static int NO_ALARM = 0;
    
    public static int MAX_SLOWDOWN = 300;  // max value for speed slider
    public static int MIN_SLOWDOWN = 0;
	
	private boolean is_stepping, take_step, is_continue;
	private int normal_delay;
	private long internal_clock;
	private Vector<AlarmPair> alarm_waiters;
	private long next_alarm;
	private boolean mKill = false;
	private JLabel mClock;
	DecimalFormat format;
	
	private boolean saw_tick; // used to see if tick() is being called
	
	public TimeManager() {
		normal_delay = DEFAULT_TICK_DELAY;
		internal_clock = 0;
		alarm_waiters = new Vector<AlarmPair>();
		next_alarm = NO_ALARM;
		
		format = new DecimalFormat("#0.00 s");
		
		TimerThread t = new TimerThread();
		t.start();
	}
	
	// called to indicate the passage of time
	// this function will pause until the program should continue
	public void tick() {
		try {	
			saw_tick = true;
			
			if(is_stepping){
				while(true){
					// loop until we are told to step
					Thread.sleep(300);
					if(take_step || !is_stepping){
						take_step = false;
						break;
					}
				}
			}else if(normal_delay > 0){
				Thread.sleep(normal_delay);
			}
		}catch (Exception e) { /*ignore*/ }
		
		// no matter how much "real-world time" goes by, 
		// we've only spent 1 millisecond! ;-)
		++internal_clock;
		saw_tick = true;
		
		updateClock();
	}
	
	public void setClock(JLabel clock){
		mClock = clock;
	}
	
	// this is a hack so that time progresses when
	// we're not stepping and time-outs can actually fire
	// the effect of this function is that when no packets
	// are being handled by a clack router, time passes at
	// full speed.
	private class TimerThread extends Thread {
		public void run() {
			
			while(true){
				if(next_alarm != NO_ALARM && next_alarm < internal_clock)
					signalAlarm();
				
				if(mKill) // let users quit even when paused
					return;
				
				// this is kind of dumb, b/c if we're stepping
				// time goes slowly even if there are no packets
				// TODO: We want a "continue until next tick" buttom
				// Another hack:  make time progress according to the speed on
				// the route slider, hence the scalar
				if(!saw_tick){
					if(!is_stepping) {
						float scaler = (((float)(MAX_SLOWDOWN - normal_delay))/ ((float)MAX_SLOWDOWN));
						internal_clock += (int)(500.0 * scaler);
					} else if (is_continue) {
						float scaler = (((float)(MAX_SLOWDOWN - normal_delay))/ ((float)MAX_SLOWDOWN));
						internal_clock += (int)(100 * scaler); // small step, so we don't overshoot
					}
				}
				try {
					saw_tick = false;
					Thread.sleep(500);
				}catch(Exception e) {/*ignore*/}
				
				updateClock();
			}
		}
	}
	
	private void updateClock() {
		if(mClock != null){
			double clock_val = ((double)internal_clock)/ 1000;
			String s = format.format(clock_val);
			mClock.setText(s);
			mClock.repaint();
		}	
		
	}
	
	public void Continue() {
		is_continue = true;
	}
	
	
	public void Step(){
		is_stepping = true;
		take_step = true;
	}
	
	public void Pause() {
		is_stepping = true;
		take_step = false;
		is_continue = false;
	}
	
	public void Play() {
		is_stepping = false;
	}
	
	// sets the delay for every tick in milliseconds
	public void setDelay(int speed) {
		normal_delay = speed;
	}
	
	public int getDelay() { return normal_delay; }
	
	public long getTimeMillis() { return internal_clock; }
	
	private class AlarmPair {
		public AlarmPair(Alarm a, long t) { callback = a; alarm_time = t; }
		Alarm callback;
		long alarm_time;
	}
	
	public void setAlarm(Alarm t, long millis){
		
		long alarm_time = internal_clock + millis;
		
		alarm_waiters.add(new AlarmPair(t, alarm_time));
		if(next_alarm == NO_ALARM || (next_alarm > alarm_time))
			next_alarm = alarm_time;
	}
	
	/**
	 * We do this weird notification via the router in order
	 * to maintain the fact that clack components are single
	 * threaded.  Since signalAlarm is called from a separate
	 * thread as the router, we need to make sure that whatever
	 * is getting the alarm callback runs only in the router 
	 * thread
	 */
	private void signalAlarm() {
		next_alarm = NO_ALARM;
		int num_alarms = 0;
		for(int i = 0; i < alarm_waiters.size(); i++){
			AlarmPair pair = alarm_waiters.get(i);
			if(pair.alarm_time < internal_clock){
				pair.callback.getRouter().addNeedToNotify(pair.callback);
				alarm_waiters.remove(i);
				i--; // we've removed an item
				num_alarms++;
			}else if(pair.alarm_time < next_alarm || next_alarm == NO_ALARM){
				next_alarm = pair.alarm_time;
			}
		}
	//	System.out.println("at: " + internal_clock + 
	//			"notifying " + num_alarms + 
	//			" next_alarm = " + next_alarm + " with size = " + alarm_waiters.size());
	}
	
	public void killMe() { mKill = true; }

}
