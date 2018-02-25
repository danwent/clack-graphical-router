package net.clackrouter.component.extension;


import java.util.Hashtable;

import javax.swing.JPanel;

import net.clackrouter.chart.ChartUtils;
import net.clackrouter.chart.ClackOccData;
import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.netutils.ExtractEncapUtils;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.packets.VNSTCPPacket;
import net.clackrouter.propertyview.TCPSeqMonPView;
import net.clackrouter.router.core.Router;

public class TCPSeqMon extends ClackComponent {
	
	public static int PORT_IN = 0, PORT_OUT = 1;
	private long start_time;
	private Hashtable isn_map = new Hashtable();
	protected ClackOccData m_occ_flowdata = new ClackOccData(); 

	public TCPSeqMon(Router router, String name){
		super(router, name); 
		start_time = System.currentTimeMillis(); // getTime();
		setupPorts(2);
	}
	
	public JPanel getPropertiesView() {	return new TCPSeqMonPView(this);}
	
	public ClackOccData getOccData() { return m_occ_flowdata; }
	
    protected void setupPorts(int numPorts)
    {
    	super.setupPorts(numPorts);
        String in_desc = "TCPSeqMon input";
        String out_desc = "TCPSeqMon output";
        m_ports[PORT_IN] = new ClackPort(this, PORT_IN, in_desc, ClackPort.METHOD_PUSH, ClackPort.DIR_IN, VNSPacket.class);
        m_ports[PORT_OUT] =  new ClackPort(this, PORT_OUT,  out_desc,  ClackPort.METHOD_PUSH, ClackPort.DIR_OUT, VNSPacket.class);
    } // -- setupPorts
    
    public boolean isModifying() { return false; }
    
    public void acceptPacket(VNSPacket packet, int port_number)
    {
        
        if ( port_number == PORT_IN ){
        	VNSPacket l3 = ExtractEncapUtils.extractToLayer3(packet);
        	
        	
        	if(l3 != null && l3 instanceof VNSTCPPacket){
        		VNSTCPPacket tcp = (VNSTCPPacket)l3;
        		String key = ChartUtils.getFlowKeyForPacket(packet); // must call with original packet
        		Long isn = (Long)isn_map.get(key);
        		Long seq_diff = null;
        		if(isn == null){
        			isn_map.put(key, new Long(tcp.seq_num));
        			seq_diff = new Long(0);
        		}else {
        			seq_diff = new Long(tcp.getSeqNum() - isn.longValue());
        			System.out.println(key + " : " + tcp.getSeqNum() + " - " + isn.longValue() + " = " + seq_diff);
        			if(seq_diff.intValue() > 1){  // everyone gets at least one, based on SYN so don't show it
        				double time_diff_sec = ((double)(System.currentTimeMillis() /*getTime()*/ - start_time )) / (1000);
        				m_occ_flowdata.addXYValue(key, new Double(time_diff_sec), seq_diff);
        			}
        		}
        	}
        	m_ports[PORT_OUT].pushOut(packet); 
        } else
        { error( "Received packet on invalid port: " + port_number); }

    } // -- acceptPacket
	
}
