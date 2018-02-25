package net.clackrouter.component.base;

/**
 * Class representing clack component events, used to pass information to listening entities
 * 
 * @see ClackComponent#registerListener(ClackComponentListener)
 * 
 */
public class ClackComponentEvent 
{
	/**
	 * A generic change to component internal state
	 */
	public static int EVENT_DATA_CHANGE = getUniqueInt();
	/**
	 * The component received a packet
	 */
	public static int EVENT_PACKET_IN   = getUniqueInt();
	/**
	 * The component sent a packet
	 */
	public static int EVENT_PACKET_OUT  = getUniqueInt();
	
    /**
     * A component has either had a connection added or removed
     */
    public static int EVENT_CONNECTION_CHANGE = getUniqueInt();

    private static int UNIQUE_INT = 99;
    public static int getUniqueInt() { return UNIQUE_INT++;}
	
    public int mType;
    public Object mValue;
    
    public ClackComponentEvent(int t, Object value){
        mType = t; mValue = value;
    }
    
    public ClackComponentEvent(int t){
    	this(t, "");
    }
}
