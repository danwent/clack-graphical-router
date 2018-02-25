package net.clackrouter.component.ethernet;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;
import net.clackrouter.component.base.*;
import net.clackrouter.packets.*;
import javax.swing.Timer; 

public class EtherSwitch extends ClackComponent {
		
		private class LearningTableEntry {
			public LearningTableEntry(int iface_num) { 
				update(iface_num); 
			}
			public void update(int iface_num) { 
				output_iface_num = iface_num; 
				last_seen_time = getTime(); 
			} 
			public int output_iface_num; 
			public long last_seen_time; 
		}
		
	    private Hashtable<String, LearningTableEntry> m_learning_table = null;
	    public static int DEFAULT_TIMEOUT_MSEC = 15000; // msecs
	    public static int NUM_INTERFACES = 4; 
	    private EthernetAddress ETH_BCAST_ADDR = EthernetAddress.getByAddress("ff:ff:ff:ff:ff:ff"); 
		private Timer timeoutTimer; 
		
		public EtherSwitch(Router router, String name){
			super(router, name);
			this.setupPorts(2 * NUM_INTERFACES); // NOTE: the number of ports will be 2 * NUM_INTERFACES, since each 
			  									 // interface will have an input and output port
			m_learning_table = new Hashtable<String, LearningTableEntry>(); 
			timeoutTimer = new Timer((DEFAULT_TIMEOUT_MSEC / 4) + 100, new TimerCallback());
			timeoutTimer.start();
		}
	
		// helpers map from interface number and interface type to port number. 
		private int getInputPortNum(int iface_num) { return iface_num; } 
		private int getOutputPortNum(int iface_num) { return NUM_INTERFACES + iface_num; } 
		// helper maps from port number to interface number 
		private int getIfaceNum(int port_num) { return port_num % NUM_INTERFACES; } 
		
	    protected void setupPorts(int numports)
	    {
	    	super.setupPorts(numports);
	    	for(int i = 0; i < NUM_INTERFACES; i++) { 
	    		int in_port = getInputPortNum(i); 
	    		int out_port = getOutputPortNum(i); 
	    		m_ports[in_port] = new ClackPort(this, in_port, "Input #" + i, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSEthernetPacket.class);
	    		m_ports[out_port] = new ClackPort(this, out_port, "Output #" + i, ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSEthernetPacket.class);
	    	}
	    } // -- setupPorts

	    /**
	     * Enqueues a packet at the tail of the queue, if possible, 
	     * or drops it if it is already at max occupancy.
	     */
	    public void acceptPacket(VNSPacket packet, int port_number) 
	    {
	    	int iface_num = getIfaceNum(port_number); 
        	VNSEthernetPacket eth_packet = (VNSEthernetPacket) packet;
        	ByteBuffer dst_addr = eth_packet.getDestinationAddressBuffer();
        	ByteBuffer src_addr = eth_packet.getSourceAddressBuffer();
        	EthernetAddress dst_eth_addr, src_eth_addr; 
        		
        	try { 		
        		src_eth_addr = EthernetAddress.getByAddress(src_addr.array());
        	} catch (UnknownHostException e) { 
        		error("Unable to parse source MAC address" + src_addr.toString()); 
        		return; 
        	}
        	try { 
        		dst_eth_addr = EthernetAddress.getByAddress(dst_addr.array());
        	} catch (UnknownHostException e) { 
        		error("Unable to parse destination MAC address" + dst_addr.toString()); 
        		return; 
        	} 
        	//System.out.println("iface # " + iface_num + " SRC: " + src_eth_addr.toString() + "  DST: " + dst_eth_addr.toString());   	
     	
        	// first, learn or update source mac address to port mapping
        	LearningTableEntry src_entry = m_learning_table.get(src_eth_addr.toString()); 
        	if(src_entry == null) { 
        		//System.out.println("Learning " + src_eth_addr.toString() + " on iface " + iface_num); 
        		src_entry = new LearningTableEntry(iface_num);
            	m_learning_table.put(src_eth_addr.toString(), src_entry); 
            }else {  
            	src_entry.update(iface_num); 
            }
 
        	// second, forward ethernet frame based on destination mac address
            if(dst_eth_addr.equals(ETH_BCAST_ADDR)) { 
           		//System.out.println("forwarding packet to broadcast address"); 
           		broadcastPacket(packet, iface_num); 
           		return;
           	}
       		LearningTableEntry dst_entry = m_learning_table.get(dst_eth_addr.toString());
       		if(dst_entry == null){ 
       			broadcastPacket(packet, iface_num); 
       			//System.out.println("no known port for " + dst_eth_addr.toString() + ", broadcasting"); 
       		} else { 
   	    		if(dst_entry.output_iface_num == iface_num) { 
   	    			error("Packet to MAC " + dst_eth_addr.toString() + " received and sent on interface #" + iface_num);
   	    			return; 
   	    		}
    	    	int out_port_num = getOutputPortNum(dst_entry.output_iface_num);
    	   		//System.out.println("sending packet to " + dst_eth_addr.toString() + " out iface " + dst_entry.output_iface_num); 
    	   		m_ports[out_port_num].pushOut(packet); 
        	}
     

	    } // -- acceptPacket

	    private void broadcastPacket(VNSPacket packet, int source_iface_num) { 
 
	    	for(int i = 0; i < NUM_INTERFACES; i++) { 
	    		if(i == source_iface_num)
	    			continue; 
	    		int out_port_num = getOutputPortNum(i); 
	    		ClackPort p = m_ports[out_port_num];
	    		if(p.getNumConnectedPorts() != 0) // only send if something is actually connected
	    			p.pushOut(packet); 
	    	}
	    	
	    }
	    
		private class TimerCallback implements ActionListener {
		      public void actionPerformed(ActionEvent evt) {
		    	  long cur_time_msec = getTime();
		    	  for(Enumeration<String> e = m_learning_table.keys(); e.hasMoreElements(); ) { 
		    		  String mac_str = e.nextElement();
		    		  LearningTableEntry entry = m_learning_table.get(mac_str); 
		    		  if(entry.last_seen_time + DEFAULT_TIMEOUT_MSEC < cur_time_msec) { 
		    			  //System.out.println("timing out learning table entry for: " + mac_str); 
		    			  m_learning_table.remove(mac_str); 
		    		  }		    		  
		    	  }
		      }
		}
}
