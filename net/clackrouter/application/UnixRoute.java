package net.clackrouter.application;

import java.net.InetAddress;

import net.clackrouter.component.base.Interface;
import net.clackrouter.netutils.NetUtils;
import net.clackrouter.routing.LocalLinkInfo;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;

public class UnixRoute extends ClackApplication {
	
	 public void application_main(String[] commands){
		 RoutingTable table = this.getRouter().getRoutingTable();
		 	if(commands.length == 1){
		 		print("type 'route -h' for help\n");
		 		
		 		if(table != null){
		 			
					print("Destination \t Mask \t\t  Next Hop \t Interface \n");
	 				for(int i = 0; i < table.numEntries(); i++){
	 					RoutingEntry entry = (RoutingEntry) table.getEntry(i);
	 					String hostAddr = entry.network.getHostAddress();
	 					String maskAddr = entry.mask.getHostAddress();
	 					
	 					print(entry.network.getHostAddress() + "\t");
	 					if( hostAddr.length() < 10){ 
	 						print("\t");
	 					}
	 					print(maskAddr + "\t");
	 					if( maskAddr.length() < 10) 
	 						print("\t");
	 					String nh = "*               ";
	 					if(entry.nextHop != null) nh = entry.nextHop.getHostAddress();
	 					print(nh + "\t" + entry.interface_name + "\n");
	 				}				
	 			}else {
		 			print("Error: could not find a routing table\n");
		 		}
				return;
		 	} else if (commands.length == 8 && commands[1].equals("add") && commands[2].equals("-net") &&
		 			commands[4].equals("netmask")){
		 		try {
		 			InetAddress net = InetAddress.getByName(commands[3]);
		 			InetAddress mask = InetAddress.getByName(commands[5]);
		 			
		 			
		 			if(commands[6].equals("gw")){
		 	 			InetAddress next_hop = InetAddress.getByName(commands[7]);
		 	 					
		 	 			RoutingEntry new_entry = new RoutingEntry(net,next_hop, mask, null);
		 	 			table.addRemoteEntry(new_entry);
						print("Successfully added route with next-hop " + commands[7] + "\n");
				
		 			}else if(commands[6].equals("dev")){
		 				String iface_name = commands[7];
		 				RoutingEntry new_entry = new RoutingEntry(net, null, mask, iface_name);
		 				table.addLocalEntry(new_entry);
 						print("Successfully added route for network adjacent to " + iface_name + "\n");
 			
		 			}else {
		 				print_usage();
		 			}
		 			
		 			return;
		 			
		 		}catch (Exception e){
		 			print(e.getMessage());
		 			print_usage();
		 		}
		 	} else if (commands.length == 5 && commands[1].equals("add") && commands[2].equals("default") &&
		 			commands[3].equals("gw")){
		 		try {
		 			InetAddress zeros = InetAddress.getByName("0.0.0.0");
		 			InetAddress next_hop = InetAddress.getByName(commands[4]);	
		 			RoutingEntry new_entry = new RoutingEntry(zeros, next_hop,zeros, null);
		 			table.addRemoteEntry(new_entry);
					print("Successfully added default route with next-hop " + commands[4] + "\n");
					
		 		}catch (Exception e){
	 				print(e.getMessage());
		 			print_usage();
		 		}
		 		
		 	} else if(commands.length == 6 && commands[1].equals("del") && commands[2].equals("-net") &&
		 				commands[4].equals("netmask")){
		 		try {
			 		
		 			InetAddress net = InetAddress.getByName(commands[3]);
		 			InetAddress mask = InetAddress.getByName(commands[5]);
		 			
		 			RoutingEntry entry = table.getRouteForPrefix(net,mask);
		 			if(entry == null){
		 				print("error: no matching routing entry found\n");
		 				return;
		 			}
		 			
		 			table.deleteEntry(entry);
		 			print("Entry deleted \n");
		 			getRouter().getDocument().redrawTopoView();
		 		}catch (Exception e){
		 			e.printStackTrace();
		 			print("error parsing 'del' command\n");
		 			print_usage();
		 		}	 		
		 	}else {
		 		if(!commands[1].equals("-h"))
		 			print("Error:  unknown command \n");
		 		print_usage();
		 	}
		 	
	 }
	 
	 private void print_usage() {
		 print("usage: \n");
		 print("route \t show routing table \n");
		 print("route add -net [network] netmask [subnet mask] dev [interface name] \t add a routing entry for a directly connected LAN\n");
		 print("route add -net [network] netmask [subnet mask] gw [next-hop address] \t add a routing entry with single next-hop\n");
		 print("route add default gw [next-hop address] \t add a default routing entry \n");
		 print("route del -net [network] netmask [subnet mask] \t delete first matching routing entry\n");
	 }
	 
	 public String getDescription() { return "simple version of unix 'route' utility"; }

}
