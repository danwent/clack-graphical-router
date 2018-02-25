package net.clackrouter.component.base;

import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Properties;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.protocol.data.VNSHWInfo;
import net.clackrouter.router.core.Router;
import net.clackrouter.topology.core.TopologyModel;

/**
 * 
 * Abstract parent class of both InterfaceIn and InterfaceOut, which provides the basic
 * functionality of a Clack Interface component.
 * 
 * Interfaces are unique in that they cannot be created or destroyed, but rather they are defined
 * by the topology, as are their attributes like hardware address, IP address and netmask.  
 */

public abstract class Interface extends ClackComponent 
{

    protected EthernetAddress m_eth_addr  = null;
    protected InetAddress     m_ip_addr   = null;
    protected InetAddress     m_ip_subnet = null;
    public EthernetAddress ETH_BCAST_ADDR = EthernetAddress.getByAddress("ff:ff:ff:ff:ff:ff"); 
	protected String device_name; // device name, like eth0, which is different from the
								// actual component name such as FromDevice(eth0)
	
	public static String IP_ADDRESS = "ip_address";
	public static String IP_NETMASK = "ip_netmask";
	
	
    /**
     * Creates a new interface component
     * @param name unique name for this component in the router
     * @param router parent router for this component
     * @param entry Clack specific data-structure specifying Interface attributes
     */
    public Interface(String name, Router router, VNSHWInfo.InterfaceEntry entry) {
		super(router, name);
		device_name = entry.getName();

		try{
			m_eth_addr = EthernetAddress.getByAddress(
                entry.getEthernetAddress().array());
			m_ip_addr = InetAddress.getByAddress(entry.getIPAddress().array());
			m_ip_subnet  = InetAddress.getByAddress(entry.getSubnet().array());

		}catch(UnknownHostException e) {
			e.printStackTrace();
		}   
    }
    
    
    public Interface(String name, Router router, 
    		TopologyModel.Interface entry){
    	super(router, name);
    	device_name = entry.name;
		m_eth_addr = entry.hw_addr;
		m_ip_addr = entry.address;
		m_ip_subnet  = entry.mask;
    }


    /**
     * Get hardware address associated with this interface
     */
    public EthernetAddress getMACAddress() { return m_eth_addr; }
    /**
     * Get IP address associated with this interface
     */
    public InetAddress getIPAddress() { return m_ip_addr; }
    
    public void setIPAddress(InetAddress a) { m_ip_addr = a; }
    
    /**
     * Get subnet mask for this interface
     */
    public InetAddress getIPSubnet() { return m_ip_subnet; }
    
    public void setIPSubnet(InetAddress s) { m_ip_subnet = s; }

	/**
	 * Gets simple device name of the component (e.g. "eth0")
	 */
	public String getDeviceName() { return device_name; }
	
	/** 
	 * Get hardware MAC address as a string
	 */
    public String getMACAddressString()
    { 
        if (m_eth_addr == null)
        { return null; }
        return m_eth_addr.toString(); 
    }
    
    
    public Properties getSerializableProperties(boolean isTransient){
    	Properties props = super.getSerializableProperties(isTransient);
    	if(!isTransient) return props;
    	
    	props.setProperty(IP_ADDRESS, m_ip_addr.getHostAddress());
    	props.setProperty(IP_NETMASK, m_ip_subnet.getHostAddress());

    	return props;
    }
    
	public void initializeProperties(Properties props) {
		try {
			String addr_str = props.getProperty(IP_ADDRESS);
			String mask_str = props.getProperty(IP_NETMASK);
			if(addr_str != null && mask_str != null){
				m_ip_addr = InetAddress.getByName(addr_str);
				m_ip_subnet = InetAddress.getByName(mask_str);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
    
    /**
     * For an interface, the type name to be displayed is the actual name of the component (e.g. FromDevice(eth0))
     */
    public String getTypeName() { return getName(); }
    
    public Color getColor() { return Color.LIGHT_GRAY; }

} // -- class Interface

