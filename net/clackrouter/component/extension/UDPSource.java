
package net.clackrouter.component.extension;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.propertyview.DelayPView;
import net.clackrouter.propertyview.UDPSourcePView;
import net.clackrouter.router.core.Router;


/**
 * Generates uniform user-specified UDP traffic at a given rate.
 */
public class UDPSource extends ClackComponent {


	private InetAddress src_address, dst_address;
	private int src_port, dst_port;
	private ByteBuffer payload; 
	private long next_tx_msec = 0; 
	private int sending_rate = 0; 
	
	public static int PORT_OUT = 0;
	public static int NUM_PORTS = 1;
	
	public static int DEFAULT_SIZE = 1000 * 8; // in bits
	public static int DEFAULT_PORT = 9999;
	public static int DEFAULT_SENDING_RATE = 10; // in kbps
	
	public UDPSource(Router r, String name){
		super(r, name); 
		src_port = DEFAULT_PORT;
		dst_port = DEFAULT_PORT;
		payload = ByteBuffer.allocate(DEFAULT_SIZE / 8);
		sending_rate = DEFAULT_SENDING_RATE;
		
		r.registerForPoll(this);
		try {
			src_address = InetAddress.getByName("0.0.0.0");
			dst_address = InetAddress.getByName("0.0.0.0");
		} catch (Exception e){
			e.printStackTrace();
		}
		setupPorts(NUM_PORTS);
	}

	
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  "UDP source output",  
        		ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
    } // -- setupPorts
    
    /** override poll() to send UDP packets at a regular rate */
    public void poll() {
    	// technically these should use the "internal" clack clock, but that doesn't work well in practice
    	long cur_time_msec = System.currentTimeMillis();
    	int len = payload.capacity(); 
    	
    	//System.out.println("Poll:  cur = " + cur_time + " last = " + last_send_time); 
    	if(cur_time_msec < next_tx_msec || sending_rate == 0){
    		return; // not time to transmit yet
    	}
    		
    	next_tx_msec = cur_time_msec + (len * 8) / sending_rate; 
    	ByteBuffer src_addr_buf = ByteBuffer.wrap(src_address.getAddress());
        ByteBuffer dst_addr_buf = ByteBuffer.wrap(dst_address.getAddress());

    	try {  
    		payload.rewind();
    		VNSUDPPacket udp = new VNSUDPPacket(src_port, dst_port, payload);
    		IPPacket ip = IPPacket.wrap(udp, IPPacket.PROTO_UDP, src_addr_buf, dst_addr_buf);
    		m_ports[PORT_OUT].pushOut(ip);	
    	} catch(Exception e) { 
    	    e.printStackTrace(); /* shouldn't happen ever */
    	}
    	
    }
    
    public void setSourceAddress(InetAddress src) { src_address = src; }
    public void setDestinationAddress(InetAddress dst) { dst_address = dst; }

    
    /** Set source port for this UDP traffic */
    public void setSourcePort(int sPort) { src_port = sPort; }
    /** Set destination port for this UDP traffic */
    public void setDestinationPort(int dPort) { dst_port = dPort; }
    
    
    /** Sets rate in kbps  */
    public void setSendingRate(int r) { 
    	if(r < 0) r = 0;
    	sending_rate = r; 
    }
    public int getSendingRate() { return sending_rate; } 
    
	public JPanel getPropertiesView(){	return new UDPSourcePView(this); } 
	
    /** Serialize the delay value for reloading the router */
    public Properties getSerializableProperties(boolean isTransient){
    	Properties props = super.getSerializableProperties(isTransient);
    	props.setProperty("rate", "" + sending_rate);
    	return props;
    }
    
    /** Load the saved delay value */
	public void initializeProperties(Properties props) {
		for(Enumeration e = props.keys(); e.hasMoreElements(); ){
			String key = (String)e.nextElement();
			String value = props.getProperty(key);
			if(key.equals("rate")){
				try{ 
					sending_rate = Integer.parseInt(value);
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}
}
