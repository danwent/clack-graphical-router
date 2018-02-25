/* File: Level3Demux.java
 * Date: Thu May 20 11:34:52 PDT 2004 
 */
package net.clackrouter.component.simplerouter;

import java.awt.Color;

import javax.swing.JPanel;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.propertyview.Level3DemuxPopup;
import net.clackrouter.router.core.Router;

/**
 * Demultiplexes between Level 3 packets, sending TCP, UDP and ICMP
 * packets to a different output port.  
 * 
 * @author mc
 */

public class Level3Demux extends ClackComponent
{

    public static int IP_IN     = 0; 
    public static int TCP_OUT   = 1; 
    public static int UDP_OUT   = 2;
    public static int ICMP_OUT  = 3;
    public static int OTHER_OUT = 4;
    public static int NUM_PORTS = 5;

    private int m_tcp_packet_count;   // -- Number of incoming tcp  packets
    private int m_udp_packet_count;   // -- Number of incoming udp  packets
    private int m_icmp_packet_count;  // -- Number of incoming icmp packets
    private int m_other_packet_count; // -- Number of "other" packets 
    
    public Level3Demux(Router router, String name)
    {
        super(router, name);
        m_tcp_packet_count   = 0;
        m_udp_packet_count   = 0;
        m_icmp_packet_count  = 0;
        m_other_packet_count = 0;
        setupPorts(NUM_PORTS);
    }
   
    public int getTCPCount() { return m_tcp_packet_count; }
    public int getUDPCount() { return m_udp_packet_count; }
    public int getICMPCount()  { return m_icmp_packet_count; }
    public int getOtherCount() { return m_other_packet_count; }

    protected void setupPorts(int numports)
    {
    	super.setupPorts(numports);
        String ip_in_descr     = "IP packet p be demultiplexed";
        String tcp_out_descr   = "TCP packets";
        String udp_out_descr   = "UDP packets";
        String icmp_out_descr  = "ICMP packets";
        String other_out_descr = "All packets not TCP/UDP/ICMP";

        m_ports[IP_IN]     = new ClackPort(this, IP_IN, ip_in_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
        m_ports[TCP_OUT]   = new ClackPort(this, TCP_OUT, tcp_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[UDP_OUT]   = new ClackPort(this, UDP_OUT, udp_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[ICMP_OUT]  = new ClackPort(this, ICMP_OUT, icmp_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
        m_ports[OTHER_OUT] = new ClackPort(this, OTHER_OUT, other_out_descr, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
    } // -- setupPorts

    public void acceptPacket(VNSPacket packet, int port_number) 
    {
        IPPacket ip_packet = (IPPacket) packet;
        
        if (port_number != IP_IN){
        	error(" acceptPacket not FROM_IFACE");
        	return;
        }
        byte protocol = ip_packet.getHeader().getProtocol();
        
        if(protocol == IPPacket.PROTO_TCP)
        {       	
            m_tcp_packet_count++;
            m_ports[TCP_OUT].pushOut(ip_packet);
        }else if(protocol == IPPacket.PROTO_UDP)
        {
            m_udp_packet_count++;
            m_ports[UDP_OUT].pushOut(ip_packet);
        }
        else if(protocol == IPPacket.PROTO_ICMP)
        {
            m_icmp_packet_count++;
            m_ports[ICMP_OUT].pushOut(ip_packet);
        }
        else
        {
        	m_other_packet_count++;
            m_ports[OTHER_OUT].pushOut(ip_packet);         
        }

    } // -- acceptPacket


    
    public JPanel getPropertiesView() {	return new Level3DemuxPopup(this);  }

    public Color getColor() { return new Color(167, 213, 255); }  // demux group color

} // --  public class Level3Demux extends VNSComponent
