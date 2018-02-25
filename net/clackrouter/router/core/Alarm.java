package net.clackrouter.router.core;

public interface Alarm {

	/**
	 * Sets a timer
	 * @param millisecs  milliseconds later in "clack time" that the alarm will fire.
	 */
	public void setAlarm(long millisecs);
	
	/**
	 * call-back method implemented by the code setting the alarm.
	 * notifies that the alarm has fired. 
	 */
	public void notifyAlarm();
	
	/**
	 * Must return the router associated with the element setting the alarm
	 * needed for single-threaded components.
	 * @return
	 */
	public Router getRouter();
}
