
package net.clackrouter.application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.simplerouter.ICMPEcho;
import net.clackrouter.netutils.ICMPListener;

/**
 * A simplified version of the "ping" command to send ICMP echo requests to a remote host.
 */

public class Ping extends ClackApplication implements ICMPListener {

	long[] send_times;
	int identifier;
	int pingCount = 10;
	int last_received = -1;
	public static int SLEEP_TIME_MSEC = 500;

	public void application_main(String[] args) throws InterruptedException {
		if(args.length < 2 || args[1].equals("?")){
			print("usage: ping <dns/ip address>");
			return;
		}
		try {
		
			InetAddress addr = InetAddress.getByName(args[1]);
			
			send_times = new long[pingCount];
		
			ICMPEcho echo = null;
			ClackComponent[] all_comps = getRouter().getAllComponents();
			for(int i = 0; i < all_comps.length; i++){
				if(all_comps[i] instanceof ICMPEcho) {
					echo = (ICMPEcho)all_comps[i];
					echo.addListener(this);
					break;
				}
			}
			
			if(echo == null){
				print("Error, could not find ICMPEcho element in your router\n");
				return;
			}
			
			for(int j = 0; j < pingCount; j++){
				Random rand = new Random();
				identifier = rand.nextInt(1000);
				send_times[j] = getTime();
			//	print("sending ping " + identifier + " at time = " + send_times[j] + "\n");
				echo.sendEchoRequest(addr, identifier, j);
				pause(SLEEP_TIME_MSEC);
			}
			
			if(last_received < pingCount - 1)
				pause(SLEEP_TIME_MSEC * 6);
			
			while(last_received < pingCount - 1){
				print("no response for packet #" + (last_received + 2) + "...\n");
				last_received++;
			}
			
			echo.removeListener(this);
		//	print("Done pinging, removing listen \n");
		}catch (UnknownHostException e){
			e.printStackTrace();
		}

	}
	
	public void receivedEchoReply(int id, int seq){
		long current = getTime();
		//print("Received reply with id = " + id + " and seq = " + seq + " at time " + current + "\n");
		if(id != identifier) return;
		
		while(last_received < seq - 1){
			print("no response for packet #" + (last_received + 2) + "...\n");
			last_received++;
		}
		last_received = seq;
		if(current >= send_times[seq])
			print("Echo Received: (" + (current - send_times[seq]) + " ms)\n");
		else
			print("Echo Received: (local) \n");
	}
	
	public void receivedEchoRequest(int id, int seq){
		// ping can just ignore this, our ICMP stack handles replies
	}
	
	public String getDescription(){
		return "standard ICMP echo-based ping utility";
	}

}
