package net.clackrouter.component.simplerouter;



import java.awt.Color;
import java.io.Serializable;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.packets.IPPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;

/**
 * Performs some basic sanity checks on the IP header as part of the forwarding path.
 * 
 * @author  Martin Casado 
 */

public class IPHeaderCheck extends ClackComponent implements Serializable
{
    public static int PORT_INPUT   = 0;
    public static int PORT_GOODEGG = 1;
    public static int PORT_BADEGG  = 2;
	public static int NUM_PORTS    = 3;

    // -- Some Counters For Stats

    private int mBadVer = 0;
    private int mBadIHL = 0;
    private int mBadLen = 0;
    private int mBadChk = 0;
    private int mBadCnt = 0;

    private int m_good_eggs = 0;

    public IPHeaderCheck(Router router, String name) {
    	super(router, name);
    	setupPorts(NUM_PORTS);
    }
    
    /**
     * Validates the IP checksum, version, and header length of the IP packet, send good packets out one
     * port for forwarding and sending those that fail the tests to a different port.  
     */
	protected void setupPorts(int numports)
    {
		super.setupPorts(numports);
		String ipcheck_input   = "incoming IP packets";
		String ipcheck_goodegg = "packets with valid headers";
		String ipcheck_badegg  = "packets with invalid headers";

		m_ports[PORT_INPUT]   = new ClackPort(this, PORT_INPUT,  ipcheck_input,   ClackPort.METHOD_PUSH, ClackPort.DIR_IN, IPPacket.class);
		m_ports[PORT_GOODEGG] = new ClackPort(this, PORT_GOODEGG,     ipcheck_goodegg, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);
		m_ports[PORT_BADEGG]  = new ClackPort(this, PORT_BADEGG, ipcheck_badegg,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, IPPacket.class);  		
	} // -- setupPorts

    public void acceptPacket(VNSPacket packet, int port_number) 
    {

        // Double check we get the packet from a valid port 
        if (port_number != PORT_INPUT)
        {  
        	error( "Received packet on invalid port: " + port_number);
        	return;
        }

        IPPacket egg = (IPPacket)packet;

        if ( ! checkIpHeader(egg)) {    
        	mBadCnt++; 
        	signalError();
        	error("Bad IP header, not forwarding packet...");
        	m_ports[PORT_BADEGG].pushOut(egg);
        } else { 
            // -- Forward Packet
        	m_good_eggs++;
        	 m_ports[PORT_GOODEGG].pushOut(egg); 
        }
    } // -- acceptPacket

    private boolean checkIpHeader(IPPacket egg)
    {
        IPPacket.Header iphdr = egg.getHeader();
        byte ver = (byte)(iphdr.getVIHL() >> 4);
        byte ihl = (byte)(iphdr.getVIHL() & 0x0f);

        // -- some very basic sanity checking
        if (egg.getLength() < 20)
        { return  false; }
        if ( egg.getLength() < (ihl*4) ) 
        { mBadIHL++; return false;}
        if ( ver != 4 ) 
        { mBadVer++; return false;}
        if ( ihl < 5 ) 
        { mBadIHL++; return false;}

        // -- Checksum verification 
        if( iphdr.calculateChecksum() !=
                iphdr.getChecksum() ) 
        { mBadChk++; return false;  } 

        
        return true;
    } // -- checkIpHeader


    public int getBadVer() { return mBadVer; }
    public int getBadIHL() { return mBadIHL; }
    public int getBadLen(){ return mBadLen; }
    public int getBadChk() { return mBadChk; }
    public int getBadCnt(){ return mBadCnt; }
    public Color getColor() { return new Color(130, 255, 154); }
    
} // -- class CheckIPHeader
