package net.clackrouter.test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;

import net.clackrouter.component.base.Interface;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.router.core.Router;
import net.clackrouter.topology.core.TopologyModel;

public class AddressAllocTest extends Thread {
	
	private InetAddress allotted_net; // network address of provided netblock 
	private InetAddress allotted_mask; // mask of provided netblock	
	
	private InetAddress isp_net;
	private InetAddress isp_mask;
	
	private Hashtable<String, Integer> net_size_map; // a hashtable holding the size of 
									// particular subnets required by the 
									// assignment
	private ArrayList links;  // links passed in
	private ArrayList nets;  // different prefixes, once we grab them
	
	private Hashtable<NetUtils.Net, TopologyModel.Link> net2link;
	
	private static Integer ROUTER_SUBNET_SIZE = 2;
	
	
	public AddressAllocTest(ArrayList l){
		links = l;
		nets = new ArrayList();
		try {
			
			// all of this info should be read in as paramters
			// if we want to make this code generic for any
			// IP allocation assignment
			
			allotted_net = InetAddress.getByName("177.66.55.0");
			allotted_mask = InetAddress.getByName("255.255.255.0");
			
			isp_net = InetAddress.getByName("177.66.20.8");
			isp_mask = InetAddress.getByName("255.255.255.254");
			net_size_map = new Hashtable<String, Integer>();
			net_size_map.put("eng1", 50);
			net_size_map.put("lab1", 100);
			net_size_map.put("acct1", 30);
			net_size_map.put("hr1", 10);
			
			net2link = new Hashtable<NetUtils.Net, TopologyModel.Link>();
			
		}catch (Exception e) { e.printStackTrace(); }
	}
	
	public void run() {
		
		// loop through each link
		for(int i = 0; i < links.size(); i++){
		  try {
			  TopologyModel.Link l = (TopologyModel.Link)links.get(i);
			  System.out.println("Link: " + l.host1.name + " <-> " + l.host2.name);
			
			  String iface1_name = l.iface1.name;
			  Router router1 = l.host1.document.getRouter(l.host1.name);
			  Interface iface1 = router1.getInterfaceByName(iface1_name);
			  NetUtils.Net n1 = new NetUtils.Net(
					NetUtils.applyNetMask(iface1.getIPAddress(), iface1.getIPSubnet()), 
					iface1.getIPSubnet());
			  
			  net2link.put(n1, l);
		
			  String iface2_name = l.iface2.name;
			  Router router2 = l.host2.document.getRouter(l.host2.name);
			  Interface iface2 = router2.getInterfaceByName(iface2_name);
			  NetUtils.Net n2 = new NetUtils.Net(
					NetUtils.applyNetMask(iface2.getIPAddress(), iface2.getIPSubnet()), 
					iface2.getIPSubnet());
			  
			  net2link.put(n2, l);
		
			  // 0: make sure subnets match for both ends of the link
			  if(!n1.equals(n2)){
				System.err.println("Subnets don't match: " + 
						NetUtils.NetworkAndMaskToString(n1.network, n1.mask) + " != " +
						NetUtils.NetworkAndMaskToString(n2.network, n2.mask) +
						" (" + getStringForNet(n1) + ")");
			  }
			
			  NetUtils.Net big_net, small_net;
			  if(NetUtils.getNumMaskBits(n1.mask) < NetUtils.getNumMaskBits(n2.mask)){
				  big_net = n1;
				  small_net = n2;
			  }else {
				  big_net = n2;
				  small_net = n1;
			  }
			  
			  
			  // 1: make sure subnet is in allotted range
			  // mask IP with real subnet mask, make sure they match
			  if(!NetUtils.isNetworkMatch(big_net.network, allotted_net, allotted_mask) 
					&& !NetUtils.isNetworkMatch(big_net.network, isp_net, isp_mask)){
				System.err.println("Newtork isn't within larger netblock: " + 
						NetUtils.NetworkAndMaskToString(big_net.network, big_net.mask) + " !E " +
						NetUtils.NetworkAndMaskToString(allotted_net, allotted_mask) +
						" (" + getStringForNet(big_net) + ")");	
			  }
			
			  int num_mask_bits = NetUtils.getNumMaskBits(big_net.mask);
			  int num_net_bits = 32 - num_mask_bits;
			  if(num_mask_bits < NetUtils.getNumMaskBits(allotted_mask)) {
				System.err.println("Mask for subnet is larger than the entire allotted" +
						"mask: subnet = " + big_net.mask + " total = " + allotted_mask +
						" (" + getStringForNet(big_net) + ")");
			  }
			
			  // 2: make sure subnets are big enough, according to problem description
			  int required_size = getRequiredSize(l).intValue();
			  double net_size = Math.pow(2, num_net_bits);
			  if(net_size <  required_size){
				System.err.println("Mask for subnet gives only " + net_size + " hosts, but requires " 
						+ required_size + " hosts " + " subnet mask = " + small_net.mask + 
						" (" + getStringForNet(small_net) + ")");			
			  }
			
			  // make sure that the addresses alignment makes sense.
			  // that is, a subnet of size X must have a last octet
			  // of a multiple of X (for /24 or less...)
		
				
				String net_address = big_net.network.getHostAddress(); 
				int last_octet = Integer.parseInt(net_address.split("\\.")[3]);
				if(last_octet % net_size != 0){
					System.err.println("Misaligned subnet, network " + net_address
							+ " has size " + net_size + " (" + getStringForNet(big_net) + ")");

			   	}
				if(!big_net.equals(small_net)){
					net_address = small_net.network.getHostAddress(); 
					last_octet = Integer.parseInt(net_address.split("\\.")[3]);
					if(last_octet % net_size != 0){
						System.err.println("Misaligned subnet, network " + net_address
							+ " has size " + net_size + " (" + getStringForNet(small_net) + ")");

					}
				}
				
				
				// add to list we will use to check for overlap
				nets.add(big_net);
				
			} catch (Exception e) { e.printStackTrace(); }
			
		}  // end for
		
		//make sure none of them overlap 
		while(nets.size() > 0){
			NetUtils.Net to_test = (NetUtils.Net) nets.remove(0);
			
			InetAddress max1 = NetUtils.getMaxAddressInSubnet(to_test.network, to_test.mask);
			
			System.out.println("***** outer net: " + to_test.network + " - " + max1 + " *****");		
			long bottom1 = NetUtils.getLongFromAddr(to_test.network);
			long top1 = NetUtils.getLongFromAddr(max1);
			
		
			for(int i = 0; i < nets.size(); i++){
				NetUtils.Net n = (NetUtils.Net)nets.get(i);
				InetAddress max2 = NetUtils.getMaxAddressInSubnet(n.network, n.mask);
				long bottom2 = NetUtils.getLongFromAddr(n.network);
				long top2 = NetUtils.getLongFromAddr(max2);
				
				
				System.out.println("inner net: " + n.network + " - " + max2);	
			//	System.out.println("inner: bottom1 = " + bottom1 + " top1 " + top1);	
			//	System.out.println("inner: bottom2 = " + bottom2 + " top2 " + top2);
				if(bottom2 >= bottom1 && bottom2 <= top1){
					
					System.err.println(NetUtils.NetworkAndMaskToString(to_test.network, to_test.mask)
							+ " (" + getStringForNet(to_test) + ") <conflicts>  " + 
							NetUtils.NetworkAndMaskToString(n.network, n.mask)
							+ " ( " + getStringForNet(n) + ")");
				}else if(top2 >= bottom1 && top2 <= top1){
					System.err.println(NetUtils.NetworkAndMaskToString(to_test.network, to_test.mask)
							+ " (" + getStringForNet(to_test) + ") <conflicts>  " + 
							NetUtils.NetworkAndMaskToString(n.network, n.mask)
							+ " ( " + getStringForNet(n) + ")");
				}
				
			}
		
			
		}
		
	}
	
	private String getStringForNet(NetUtils.Net net){
		TopologyModel.Link l = net2link.get(net);
		return l.host1.name + " <-> " + l.host2.name; 
	}
	
	private Integer getRequiredSize(TopologyModel.Link l){
		Integer size1 = net_size_map.get(l.host1.name);
		if(size1 != null) return size1;
		Integer size2 = net_size_map.get(l.host2.name);
		if(size2 != null) return size2;
		return ROUTER_SUBNET_SIZE;
	}

}
