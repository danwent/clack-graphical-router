/**
 * VRHWInfo.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.protocol.data;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Vector;

import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.protocol.VNSProtocolCharCoder;



/**
 * Encapsulates hardware information sent from the server.
 *
 */
public class VNSHWInfo extends VNSData
{
	
	public boolean isHWInfo() { return true; }
	
  /**
   * Constructs hardware info from the supplied byte buffer.
   *
   *
   * @param hwInfoByteBuffer Byte buffer containing hardware info
   */
  public VNSHWInfo(ByteBuffer hwInfoByteBuffer) throws Exception
  {
    super();

    hwInfoByteBuffer.rewind();

    int numberOfHWEntries = hwInfoByteBuffer.capacity() / 
      (KEY_LEN + VALUE_LEN);

    m_vHWEntries = new Vector(numberOfHWEntries);
    m_vInterfaceEntries = new Vector();
    m_vFixedIPEntries = new Vector();

    byte [] hwEntryBytes = new byte[KEY_LEN + VALUE_LEN];

    // Add the received hardware entries to the internal vector
    for(int hwEntryIndex = 0; hwEntryIndex < numberOfHWEntries;
	hwEntryIndex++) {
      hwInfoByteBuffer.get(hwEntryBytes, 0, KEY_LEN + VALUE_LEN);
      m_vHWEntries.addElement(new HWEntry(ByteBuffer.wrap(hwEntryBytes)));
    }

    // Now group all hw entries by inteface
    HWEntry hwEntry = null;
    InterfaceEntry interfaceEntry = null;
    for(int hwEntryIndex = 0; hwEntryIndex < numberOfHWEntries;
	hwEntryIndex++) {

      hwEntry = (HWEntry) m_vHWEntries.elementAt(hwEntryIndex);
      
      switch(hwEntry.getKey()) {
      case KEY_INTERFACE:
	{
	  // Add previously created and populated inteface entry
	  if( interfaceEntry != null )
	    m_vInterfaceEntries.addElement(interfaceEntry);
	  
	  String interfaceName = null;

	    interfaceName =   VNSProtocolCharCoder.decode(hwEntry.getValue(), true);

	  interfaceEntry = new InterfaceEntry(interfaceName);
	}
	break;

      case KEY_SPEED:
	{
	  interfaceEntry.setSpeed(hwEntry.getValue().getInt());
	}
	break;
	  
      case KEY_SUBNET:
	interfaceEntry.setSubnet(hwEntry.getValue());
	break;

      case KEY_FIXED_IP:
	m_vFixedIPEntries.addElement(hwEntry.getValue());
	break;

      case KEY_ETHERNET:
	interfaceEntry.setEthernetAddress(hwEntry.getValue());
	break;

      case KEY_IP:
	interfaceEntry.setIPAddress(hwEntry.getValue());
	break;
	
      case KEY_MASK:
	interfaceEntry.setMask(hwEntry.getValue());
	break;
      }
    }

    // Add previously created and populated inteface entry
    if( interfaceEntry != null )
      m_vInterfaceEntries.addElement(interfaceEntry);
      
  }

  /**
   * Represent harddware info in a readable format, by field.
   *
   * @return Hardware info
   */
  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();

    for(int ifaceEntryIndex = 0; ifaceEntryIndex <
	  m_vInterfaceEntries.size(); ifaceEntryIndex++) {
      stringBuffer.append(m_vInterfaceEntries.elementAt(ifaceEntryIndex).toString() + "\n");
    }

    return stringBuffer.toString();
  }

  /**
   * Returns the number of raw hardware entries for this router.
   *
   * @return Number of raw hardware entries for this router
   *
   * @see #getNumberOfInterfaces
   */
  public int getNumberOfHWEntries() { return m_vHWEntries.size(); }

  /**
   * Returns the raw hardware entry at the specified index.
   *
   * @param index Index of the raw hardware entry
   * @return Hardware entry at the specified index
   */
  public HWEntry getHWEntryAt(int index)
  {
    return (HWEntry) m_vHWEntries.elementAt(index);
  }

  /**
   * Returns number of interfaces on the router.
   *
   * @return Number of interfaces on this router
   *
   * @see #getInterfaceEntryAt
   */
  public int getNumberOfInterfaces() { return m_vInterfaceEntries.size(); }


  /**
   * Returns an interface entry at the supplied index.
   *
   * @param index Index of the interface
   * @return Interface entry
   *
   * @see #getNumberOfInterfaces
   */
  public InterfaceEntry getInterfaceEntryAt(int index) {
    return (InterfaceEntry) m_vInterfaceEntries.elementAt(index);
  }

  /**
   * Length of the key field in the (key, value) tuple that arrives from
   * the server inside the hardware info command.
   */
  public static final int KEY_LEN   = 4;

   /**
   * Length of the value field in the (key, value) tuple that arrives from
   * the server inside the hardware info command.
   */
  public static final int VALUE_LEN = 32;

  // Semantic meaning of a key in hardware entry

  /**
   * Key indicating that a value following it is an interface name.
   */
  public static final int KEY_INTERFACE = 1;   // Interface name

  /**
   * Key indicating that a value following it is an interface speed.
   */
  public static final int KEY_SPEED     = 2;   // Speed of the interface (Kbits/sec)

  /**
   * Key indicating that a value following it is a subnet IP address.
   */
  public static final int KEY_SUBNET    = 4;   // Subnet IP address

  /**
   * This key is deprecated and should not be used.
   */
  public static final int KEY_IN_USE    = 8;   // ??

  /**
   * Key indicating that a value following it is a host IP address and
   * it's fixed in the topology.
   */
  public static final int KEY_FIXED_IP  = 16;  // Fixed IP

  /**
   * Key indicating that a value following it is a interface hardware address.
   */
  public static final int KEY_ETHERNET  = 32;  // MAC address

  /**
   * Key indicating that a value following it is an interface IP address.
   */
  public static final int KEY_IP        = 64;  // IP address of the interface


  /**
   * Key indicating that a value following it is an interface subnet mask.
   */
  public static final int KEY_MASK      = 128; // Subnet mask

  private static final String VALUE_UNKNOWN = "unknown";

  private Vector m_vHWEntries;
  private Vector m_vInterfaceEntries;
  private Vector m_vFixedIPEntries;

  /**
   * Class representing an individual hardware entry.
   */
  public class HWEntry {

    public HWEntry(ByteBuffer hwEntryByteBuffer)
    {
      hwEntryByteBuffer.rewind();

      // Get the key
      m_key = hwEntryByteBuffer.getInt();

      // Get value
      byte [] valueBytes = new byte[VALUE_LEN];
      hwEntryByteBuffer.get(valueBytes, 0, VALUE_LEN);
      m_value = ByteBuffer.wrap(valueBytes);
    }

    public String toString()
    {
      return "HW: " + m_key + " -> "; // just to compile + getStringBuffer(m_value);
    }

    public int getKey() { return m_key; }
    public ByteBuffer getValue()
    { 
      m_value.rewind();
      return m_value; 
    }

    private int m_key;
    private ByteBuffer m_value;
  }

  /**
   * Encapsulates interface information.
   */
  public class InterfaceEntry
  {
    /**
     * Constructs an interface entry.
     *
     * @param name Name of an interface, e.g. eth0.
     */
    public InterfaceEntry(String name)
    {
      m_name = name;
    }

    /**
     * Returns the name of the interface.
     *
     * @return Name of an interface
     */
    public String getName() { return m_name; }

    /**
     * Returns the speed of an interface, in bits per second.
     *
     * @return Speed of an interface
     */
    public int getSpeed() { return m_speed; }

    /**
     * Returns the subnet mask of an interface.
     *
     * @return Byte buffer containing the subnet mask of an interface
     */
    public ByteBuffer getSubnet()
    { 
      m_subnet.rewind();
      return m_subnet;
    }

    /**
     * Returns the hardware address of an interface.
     *
     * @return  Byte buffer containing the hardware address of an interface
     */
    public ByteBuffer getEthernetAddress()
    {
      m_ethernetAddress.rewind();
      return m_ethernetAddress;
    }

    /**
     * Returns the IP address of an interface.
     *
     * @return Byte buffer containing the IP address of an interface
     */
    public ByteBuffer getIPAddress()
    {
      m_ipAddress.rewind();
      return m_ipAddress;
    }

    /**
     * Returns the subnet mask of an interface.
     *
     * @return  Byte buffer containing the subnet mask of an interface.
     */
    public ByteBuffer getMask()
    {
      m_mask.rewind();
      return m_mask;
    }

    /**
     * Sets the name of an interface.
     *
     * @param name Name of an interface
     */
    public void setName(String name) { m_name = name; }

    /**
     * Sets the speed of an interface, in bits per second.
     *
     * @param speed Speed of an interface
     */
    public void setSpeed(int speed) { m_speed = speed; }

    /**
     * Sets the subnet IP address of an interface.
     *
     * @param subnet Subnet IP address of an interface
     */
    public void setSubnet(ByteBuffer subnet)
    { 
      subnet.rewind();
      byte [] subnetBytes = new byte[IPPacket.Header.ADDR_LEN];
      subnet.get(subnetBytes, 0, IPPacket.Header.ADDR_LEN);
      m_subnet = ByteBuffer.wrap(subnetBytes);
    }

    /**
     * Sets the hardware address of an interface.
     *
     * @param ethernetAddress Byte buffer containing hardware address of
     *                        an interface
     */
    public void setEthernetAddress(ByteBuffer ethernetAddress)
    {
      ethernetAddress.rewind();
      byte [] ethernetAddressBytes = new byte[VNSEthernetPacket.ADDR_LEN];
      ethernetAddress.get(ethernetAddressBytes, 0, VNSEthernetPacket.ADDR_LEN);
      m_ethernetAddress = ByteBuffer.wrap(ethernetAddressBytes);
    }

    /**
     * Sets the IP address of an interface.
     *
     * @param ipAddress  Byte buffer containing IP address of an interface
     */
    public void setIPAddress(ByteBuffer ipAddress)
    {
      ipAddress.rewind();
      byte [] ipAddressBytes = new byte[IPPacket.Header.ADDR_LEN];
      ipAddress.get(ipAddressBytes, 0, IPPacket.Header.ADDR_LEN);
      m_ipAddress = ByteBuffer.wrap(ipAddressBytes);
    }

    /**
     * Sets the subnet mask of an interface.
     *
     * @param mask Byte buffer containing subnet mask of an interface
     */
    public void setMask(ByteBuffer mask)
    {
      mask.rewind();
      byte [] maskBytes = new byte[IPPacket.Header.ADDR_LEN];
      mask.get(maskBytes, 0, IPPacket.Header.ADDR_LEN);
      m_mask = ByteBuffer.wrap(maskBytes);
    }

    /**
     * Represents the interface information in a readable format, by field.
     *
     * @return Interface information in a readable format, by field
     */
    public String toString()
    {
      StringBuffer stringBuffer = new StringBuffer();

      stringBuffer.append("Inteface Name: [" + m_name + "]\n");
      stringBuffer.append("Speed: [" + m_speed + "]\n");
      stringBuffer.append("Subnet: " + getStringBuffer(m_subnet) + "\n");
      stringBuffer.append("Ethernet Address: " + getStringBuffer(m_ethernetAddress) + "\n");
      stringBuffer.append("IP Address: " + getStringBuffer(m_ipAddress) + "\n");
      stringBuffer.append("Mask: " + getStringBuffer(m_mask));
      
      return stringBuffer.toString();
    }
    
	protected StringBuffer getStringBuffer(ByteBuffer byteBuffer)
	{
	  byteBuffer.rewind();

	  StringBuffer stringBuffer = new StringBuffer();
	  stringBuffer.append("[ ");
	  for(int i=0; i<byteBuffer.capacity(); i++)
		stringBuffer.append( byteBuffer.get() + " ");
	  stringBuffer.append("]");

	  return stringBuffer;
	}    

    private String m_name;                // Name of the inteface
    private int m_speed;                  // Speed of the interface, in bits/sec
    private ByteBuffer m_subnet;          // Subnet on which interface exists
    private ByteBuffer m_ethernetAddress; // Interface MAC address
    private ByteBuffer m_ipAddress;       // Interface IP address
    private ByteBuffer m_mask;            // Subnet mask
  }
}
