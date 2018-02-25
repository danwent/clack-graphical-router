
package net.clackrouter.test;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import net.clackrouter.netutils.EthernetAddress;
import net.clackrouter.packets.VNSARPPacket;
import net.clackrouter.packets.VNSEthernetPacket;
import net.clackrouter.router.core.Router;


/**
 * This is an example of how one could perform an external "black box" test on a router, by applying
 * a packet to the router and then test what the router replies with.  
 * 
 * <p> Note: This should be pulled out into an interface, so anyone can implement it to perform a test. </p>
 */
public class ClackRouterTest {
	
	protected Router mRouter;

	
	public void startTest(Router router) {
		try {
			mRouter = router;
			InetAddress fake_ip = InetAddress.getByName("10.1.1.1");
			byte[] fake_eth = EthernetAddress.getByAddress("ff:ff:ff:ff:ff:ff").getAddress();
		
			InetAddress their_ip = mRouter.getInterfaceByName("eth0").getIPAddress();
			byte[] their_eth = mRouter.getInterfaceByName("eth0").getMACAddress().getAddress();
		
			//TODO: wow, making an ARP packet is ugly!  need to clean this up
			ByteBuffer arp_type = ByteBuffer.allocate(VNSEthernetPacket.TYPE_LEN);
			arp_type.putShort(VNSEthernetPacket.TYPE_ARP);
			VNSARPPacket request_packet = new VNSARPPacket(VNSARPPacket.HW_TYPE_ETHERNET,
                VNSARPPacket.PROTOCOL_TYPE_IP,
                (byte)VNSARPPacket.HW_ADDR_LEN,
                (byte)VNSARPPacket.PROTOCOL_ADDR_LEN,
                VNSARPPacket.OPERATION_REQUEST,
                ByteBuffer.wrap(fake_eth),
                ByteBuffer.wrap(fake_ip.getAddress()),
                ByteBuffer.wrap(EthernetAddress.getByAddress("0:0:0:0:0:0").getAddress()),
                ByteBuffer.wrap(their_ip.getAddress()));


            VNSEthernetPacket e_packet = 
                VNSEthernetPacket.wrap(request_packet, 
                        ByteBuffer.wrap(EthernetAddress.getByAddress("ff:ff:ff:ff:ff:ff").getAddress()), 
                        ByteBuffer.wrap(fake_eth), 
                        arp_type);
            e_packet.setInputInterfaceName("eth0");
            System.out.println("Sending ARP Request!");
            mRouter.handlePacket(e_packet);
        } catch (Exception e) {
            e.printStackTrace();
																				
        }
	}
	
	public void processPacket(VNSEthernetPacket packet){
		System.out.println("Test got packet on " + packet.getOutputInterfaceName());
		if(packet.getType() == VNSEthernetPacket.TYPE_ARP)
			System.out.println("arp packet");
		else
			System.out.println("other type: " + packet.getType());
		mRouter.endTest();
		System.out.println("Done with Test");
	}
}
