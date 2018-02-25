
package net.clackrouter.component.simplerouter;

import java.util.Date;

import net.clackrouter.component.base.Queue;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.router.core.Router;


/**
 * A drop-tail queue measuring its occupancy in bytes.
 * 
 */
public class ByteQueue extends Queue {

	private int cur_occupancy_bytes;
	public static int DEFAULT_MAX_SIZE = 10000;
	
	public ByteQueue(Router router, String name){
		this(router, name, DEFAULT_MAX_SIZE); 	
	}
	
	/** Create a queue specifying its max size in bytes */
	public ByteQueue(Router r, String name, int max_size){
		super(r, name, max_size);
		cur_occupancy_bytes = 0;
	}
	
	/** Add packet to the queue if the additional bytes can be stored without
	 * exceeding max size, otherwise drop. */
    public void acceptPacket(VNSPacket packet, int port_number) 
    {
    	
        if (port_number == PORT_TAIL)
        { 
        	if(cur_occupancy_bytes + packet.getLength() <= m_max_size){
        		m_queue.add(packet); 
        		cur_occupancy_bytes += packet.getLength();
        	}else {
        		m_total_drops++;
        		log("Packet Drop: " + new Date());
        		m_recent_drop = true;
        	}

        	queueOccupancyChanged(); // update either way
	
        }
        else
        { error( "Received packet on invalid port: " + port_number); }	

    } // -- acceptPacket
    
    /** Pull a packet from the head of the queue 
     * @return a single packet, or null if queue is empty 
     */
    public VNSPacket handlePullRequest(int port_number) { 

            if (port_number == PORT_HEAD)
            { 	

                if(!m_queue.isEmpty()){
                    VNSPacket packet = (VNSPacket) m_queue.remove(0);
                    cur_occupancy_bytes -= packet.getLength();
                    queueOccupancyChanged();                 
                    return packet;
                }

            }
            else  { error("Pull request on invalid port: " + port_number); }	
            
            return null;
        }
    
    public int getOccupancy(){
        return cur_occupancy_bytes;
    }
	
}
