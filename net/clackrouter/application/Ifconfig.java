package net.clackrouter.application;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.clackrouter.component.base.Interface;
import net.clackrouter.router.core.Router;
import net.clackrouter.routing.LocalLinkInfo;
import net.clackrouter.topology.core.TopologyManager;
import net.clackrouter.topology.core.TopologyModel;

public class Ifconfig extends ClackApplication {

		 public void application_main(String[] commands){
		 	Router router = getRouter();
		 		
		 	if(router == null){
		 		print("Error: could not find router\n");
		 		return;
		 	}
		 	
		 	if(commands.length == 2 && commands[1].equals("all")){
		 		print("name \t IP Address \t MAC Address \t status \t metric\t use routing?\n");
		 		Hashtable local_links = getRouter().getLocalLinkInfo();
		 		Interface[] all_ifaces = getRouter().getInputInterfaces();
		 		
		 		for(int i = 0; i < all_ifaces.length; i++){
		 			
		 			String iface_str = all_ifaces[i].getDeviceName();
		 			LocalLinkInfo info = (LocalLinkInfo) local_links.get(iface_str);
		 			String ipaddr = all_ifaces[i].getIPAddress().getHostAddress();
		 			if(ipaddr.length() < 12) ipaddr += "     "; // stupid formatting
		 			String status = (info.is_up) ? "up" : "down";
		 			String is_routing = (info.is_routing_iface) ? "yes" : "no";
		 			print(info.local_iface + "\t" + ipaddr + 
		 					"\t" + all_ifaces[i].getMACAddressString() + "\t " + status + "\t" 
		 					+ info.metric + "\t" + is_routing + "\n");
		 		}
		 	}else if(commands.length >= 3){

		 		String iface_name = commands[1];
		 		Interface iface = router.getInterfaceByName(iface_name);
		 		if(iface == null){
		 			print("Error: host " + router.getHost() + " has no interface '" + iface_name + "'\n");
		 			return;
		 		}
		 
		 		TopologyManager man = getRouter().getDocument().getFramework().getTopologyManager();
		 		TopologyModel model = null; 
		 		try {
		 			model = man.getTopologyModel(getRouter().getTopology(), null);
		 		} catch (Exception e) {
		 			e.printStackTrace();
		 			return;
		 		}
		 
		 		TopologyModel.Link l = (TopologyModel.Link)model.getLinksForHost(router.getHost()).get(iface_name);
		 		if(commands[2].equals("up")) {
		 			model.setLinkStatus(getRouter().getDocument(), l, true);
		 			print("Bringing up interface " + iface_name + "\n");
		 		}else if (commands[2].equals("down")){
		 			model.setLinkStatus(getRouter().getDocument(), l, false);
		 			print("Shutting down interface " + iface_name + "\n");
		 		} else if (commands.length == 4 && commands[2].equals("metric")){
		 			try {
		 				int new_metric = Integer.parseInt(commands[3]);
		 				router.updateLinkMetric(iface_name, new_metric); // keep these changes local to the router
		 			}catch (Exception e){
		 				e.printStackTrace();
		 				print("invalid value for metric: '" + commands[3] + "'");
		 			}
		 		}else if (commands.length == 5 && commands[3].equals("netmask")){
		 			try {
		 				InetAddress addr = InetAddress.getByName(commands[2]);
		 				InetAddress mask = InetAddress.getByName(commands[4]);
		 				router.configureIPInterface(iface_name, addr, mask);
		 				
		 			}catch (Exception e){
		 				e.printStackTrace();
		 				print("could not parse address or subnet value");
		 			}
		 		}else 
		 			print("Error, command '" + commands[2] + "' not understood (type 'ifconfig ?' for help)\n");
		 		
		 	}else {
		 		print("Usage: \n ifconfig all \n ifconfig [interface name] [up/down]\n ifconfig [interface name] metric [new metric value]\n");
		 		print(" ifconfig [interface name] [ip address] netmask [subnet mask]");
		 	}
		 }
		 
		 public String getDescription() { return "view/change information about a router's interfaces"; }
		
	}


