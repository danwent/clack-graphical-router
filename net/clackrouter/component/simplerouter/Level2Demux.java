
package net.clackrouter.component.simplerouter;



import java.awt.Color;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.Level2DemuxPopup;
import net.clackrouter.router.core.Router;

/**
 * Demultiplexes Level 2 packets, sending ARP packets to one output port and 
 * IP packets to another.  
 */

public class Level2Demux extends ClackComponent
{

    public static int FROM_IFACE = 0; 
    public static int TO_ARP     = 1; 
    public static int TO_IP      = 2;
    public static int NUM_PORTS  = 3;

    private int m_arp_packet_count;   // -- Number of incoming arp packets
    private int m_ip_packet_count;    // -- Number of incoming IP packets
    private int m_other_packet_count; // -- Number of "other" packets 

    public Level2Demux(Router router, String name)
    {
    	super(router, name);
        m_ip_packet_count    = 0;
        m_arp_packet_count   = 0;
        m_other_packet_count = 0;
        setupPorts(NUM_PORTS);
    }
    
    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String from_iface_desc = "Level2 Packets with Ethernet headers stripped";
        String to_arp_desc     = "Sends packets to ARP component";
        String to_ip_desc      = "Sends packets to IP component";
        m_ports[FROM_IFACE] = new ClackPort(this, FROM_IFACE, from_iface_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[TO_ARP]     = new ClackPort(this, TO_ARP, to_arp_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSARPPacket.class);
        m_ports[TO_IP]      = new ClackPort(this, TO_IP, to_ip_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);  		
    } // -- setupPorts

    public void acceptPacket(VNSPacket packet, int port_number) 
    {

        if(packet instanceof VNSARPPacket) {
            m_arp_packet_count++;
            m_ports[TO_ARP].pushOut(packet);
        }else if(packet instanceof IPPacket) {
            m_ports[TO_IP].pushOut(packet);
            m_ip_packet_count++;
        }else {
        	signalError();
        	error("Ethertype is not IP or ARP, dropping packet..."); 
        	m_other_packet_count++;
        }
       
    } // -- acceptPacket	

    public JPanel getPropertiesView() { return new Level2DemuxPopup(this); } 
    public Color getColor() { return new Color(167, 213, 255); }  // demux group color
    
    public int getARPCount() { return m_arp_packet_count; }  
    public int getIPCount() { return m_ip_packet_count;  }
    public int getOtherCount() { return m_other_packet_count; } 

} // --  
