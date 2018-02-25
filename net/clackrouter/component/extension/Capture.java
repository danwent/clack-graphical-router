
package net.clackrouter.component.extension;

import java.awt.Color;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.jpcap.RawPacket;
import net.clackrouter.jpcap.TcpdumpWriter;
import net.clackrouter.jpcap.Timeval;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSICMPPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.packets.VNSUDPPacket;
import net.clackrouter.propertyview.CapturePopup;
import net.clackrouter.router.core.Router;


/**
 * Component that allows the user to capture packet through it to a PCAP file readable by 
 * Ethereal or another packet-analyzer.
 * 
 * <p> Depending on the layer of the packets captured, this component will wrap the packet
 * in dummy layers because PCAP expects Ethernet frames.  </p>
 * 
 * <p> Ignore the code about JCaptor and JpcapDumper, as it was an early attempt to 
 * build in a packet analyzer that we have halted, at least for now </p>
 */
public class Capture extends ClackComponent {

	private ArrayList packets;
	private boolean isLiveCapture = false;
	public static int PORT_IN = 0;
	public static int PORT_OUT = 1;
	private long capture_start_time = 0;
	//private JDCaptor captor;
	
	
	public Capture(Router router, String name){
		super(router, name); 
		setupPorts(2);
		/*
		JpcapDumper.loadProperty();
		JDPacketAnalyzerLoader.loadDefaultAnalyzer();
		JDStatisticsTakerLoader.loadStatisticsTaker();

		captor = JpcapDumper.openNewWindow();
		captor.startCaptureThread();
		*/
	}
	
	/** Signals that this component should begin capturing packets, but does not write them to file */
	public void startCapture() { 
		packets = new ArrayList();
		isLiveCapture = true; 
		capture_start_time = getTime();
	}
	
	/** Ends capture process */
	public void stopCapture() { 
		isLiveCapture = false;
	}
	
	/** Writes all packets capture in the last capture sequence to the specified file */
	public void saveToFile(String filename) throws Exception {
		
			TcpdumpWriter.writeHeader(filename, TcpdumpWriter.LITTLE_ENDIAN, 2000);
			while(packets.size() > 0){
				RawPacket rp = (RawPacket) packets.remove(0);

				
				TcpdumpWriter.appendPacket(filename, rp, TcpdumpWriter.LITTLE_ENDIAN);
			}
	
	}
	
    protected void setupPorts(int numPorts)
    {
    	super.setupPorts(numPorts);
        String in_desc = "Capture input";
        String out_desc = "Capture output";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; }
    
    /**
     * <p> Looks at all packets sent through the component, and keeps a copy of the packet if it 
     * is supposed to be capturing the packet, then passes the packet on. </p>
     */
    public void acceptPacket(VNSPacket packet, int port_number){
        
        if ( port_number == PORT_IN ){
        	try {
        		if(isLiveCapture){
        			long delta_msec = getTime() - capture_start_time;
        			Timeval tv = new Timeval((int)delta_msec / 1000, (int)delta_msec % 1000);
        			VNSEthernetPacket ethPacket = encapPacket(packet);
        			
        			byte[] data = ethPacket.getByteBuffer().array();
        			RawPacket rp = new RawPacket(tv, data, 0);
            		System.out.println("Captured " + packets.size() + " packets ");
        			packets.add(rp);
        		}
        		m_ports[PORT_OUT].pushOut(packet); 
        	} catch (Exception e){
        		e.printStackTrace();
        	}
        } else{ 
        	error( "Received packet on invalid port: " + port_number);; 
        }

    } // -- acceptPacket
    
    /** 
     * <p>Encapusulates a VNSPacket with fake header
     * layers so that TCPDump can make sense of it. </p>
     * 
     * <p> Assumes that the provided packet is TCP, UDP, or Ethernet. </p>
     */ 
    public VNSEthernetPacket encapPacket(VNSPacket packet) throws Exception {
    	
    	if(packet instanceof VNSEthernetPacket) return (VNSEthernetPacket) packet;
    	if(packet instanceof VNSTCPPacket || packet instanceof VNSUDPPacket || packet instanceof VNSICMPPacket)
    		return encapInEthernet(encapInIP(packet));	
    	if(packet instanceof IPPacket || packet instanceof VNSARPPacket)
    		return encapInEthernet(packet);
    	else throw new Exception("unknown packet type");
    		
    }
    
    private VNSEthernetPacket encapInEthernet(VNSPacket packet) throws Exception {
    	ByteBuffer zeros = ByteBuffer.wrap(EthernetAddress.getByAddress("00:00:00:00:00:00").getBytes());
    	ByteBuffer ones = ByteBuffer.wrap(EthernetAddress.getByAddress("01:01:01:01:01:01").getBytes());
        ByteBuffer type = ByteBuffer.allocate(VNSEthernetPacket.TYPE_LEN);
        
    	if(packet instanceof VNSARPPacket)type.putShort(VNSEthernetPacket.TYPE_ARP);
    	else if (packet instanceof IPPacket) type.putShort(VNSEthernetPacket.TYPE_IP);
    	else throw new Exception("unknown packet type");
    	
       return VNSEthernetPacket.wrap(packet, zeros, ones, type); 
    }
    
    private IPPacket encapInIP(VNSPacket packet) throws Exception {
    	byte proto = -1;

    	if(packet instanceof VNSTCPPacket) proto = IPPacket.PROTO_TCP;
    	else if (packet instanceof VNSUDPPacket) proto = IPPacket.PROTO_UDP;
    	else throw new Exception("unknown packet type");
    	
    	ByteBuffer zeros = ByteBuffer.wrap(InetAddress.getByName("0.0.0.0").getAddress());
    	ByteBuffer ones = ByteBuffer.wrap(InetAddress.getByName("1.1.1.1").getAddress());
        return IPPacket.wrap(packet, proto, zeros, ones);
    }

    
    public JPanel getPropertiesView() {	return new CapturePopup(this); }
    
    public Color getColor() { return Color.PINK; }
}
