/*
 * Created on Sep 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.application;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.simplerouter.ARPLookup;
import net.clackrouter.component.simplerouter.InterfaceIn;
import net.clackrouter.router.core.Router;
import net.clackrouter.routing.RoutingEntry;
import net.clackrouter.routing.RoutingTable;

/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Show extends ClackApplication {

	 public void application_main(String[] commands){
	 	if(commands.length == 3 && commands[1].equals("ip")){
	 		Router router = getRouter();
	 		if(router == null){
	 			print("Error: could not find router\n");
	 			return;
	 		}
	 		if(commands[2].equals("route")){
	 			RoutingTable table = router.getRoutingTable();
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
	 					print(entry.nextHop.getHostAddress() + "\t" + entry.interface_name + "\n");
	 				}				
	 			}else {
	 				print("Error: could not find a routing table\n");
	 			}
	 			return;
	 		}else if(commands[2].equals("arp")){
	 			ClackComponent[] all_comp = router.getAllComponents();
				print("Device \t IP Address \t Hardward Address\n");
	 			for(int i = 0; i < all_comp.length; i++){
	 				if(all_comp[i] instanceof ARPLookup){
	 					ARPLookup lookup = (ARPLookup)all_comp[i];
	 					Hashtable cache = lookup.getARPCache();
	 					for(Enumeration ifaces = cache.keys(); ifaces.hasMoreElements(); ){
	 						String iface = (String)ifaces.nextElement();
	 						Hashtable iface_cache = (Hashtable)cache.get(iface);
	 						for(Enumeration entries = iface_cache.keys(); entries.hasMoreElements(); ){
	 							InetAddress ip = (InetAddress)entries.nextElement();
	 							ARPLookup.CacheEntry entry = (ARPLookup.CacheEntry) iface_cache.get(ip);
	 							print(iface + "\t" + entry.mIPAddr.getHostAddress() + "\t" + entry.mEthAddr.toString() + "\n");
	 						}
	 					}
	 				}
	 			}
	 			print("\n");
	 			return;
	 		} else if(commands[2].equals("interface")){
	 			InterfaceIn[] ifaces = router.getInputInterfaces();
	 			print("Device \t IP address \t Hardware Address\n");
	 			for(int i = 0; i < ifaces.length; i++){
	 				print(ifaces[i].getDeviceName() + "\t" + ifaces[i].getIPAddress().getHostAddress() + 
	 						"\t" + ifaces[i].getMACAddressString() + "\n");
	 			}
	 			print("\n");
	 			return;
	 		}

	 	}
		print("show ip route \t : Show routing table\n");
		print("show ip arp \t : Show arp cache\n");
		print("show ip interface \t : Show interface information\n");
	 }
	 
	 public String getDescription() { return "view router configuration information"; }
	
}
