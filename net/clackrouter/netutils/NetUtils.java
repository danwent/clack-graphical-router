

package net.clackrouter.netutils;

import java.net.InetAddress;
import java.nio.ByteBuffer;


/**
 * Simple utilities for operating on ByteBuffers and Internet Addresses.
 *
 */

public class NetUtils
{
    static public ByteBuffer Inet2ByteBuffer(String in)
    {
        try {
            return Inet2ByteBuffer(InetAddress.getByName(in));
        }catch(Exception e)
        { return null; }
    }
    static public ByteBuffer Inet2ByteBuffer(InetAddress in)
    {
        return ByteBuffer.wrap(in.getAddress());
    }

    static public InetAddress ByteBuffer2Inet(ByteBuffer in)
    {
        try{
            return InetAddress.getByAddress(in.array());
        }catch(Exception e)
        { return null; }
    }
    
    static public String NetworkAndMaskToString(InetAddress net, InetAddress mask){

    	int prefix = getNumMaskBits(mask);
    	int mask_addr = getIntFromAddr(mask);
    	int net_addr = getIntFromAddr(net);
    	net_addr = net_addr & mask_addr;
    	
    	try {
    		return intToInetAddress(net_addr).getHostAddress() + "/" + prefix;
    	} catch (Exception e){
    		e.printStackTrace();
    		return "unparsable prefix";
    	}
    }
    
    public static int getNumMaskBits(InetAddress mask){
     	int mask_addr = getIntFromAddr(mask);  	
    	int prefix = 0;
    	while(mask_addr != 0){
    		++prefix;
    		mask_addr = mask_addr << 1;
    	}
    	return prefix;
    }
    
    
    public static class Net {
    	public Net(InetAddress n, InetAddress m) { network = n; mask = m; }
    	public InetAddress network, mask;
    	public boolean equals(Object b)  { 
    		if(b instanceof Net) {
    			Net netb = (Net)b;
    			if(!netb.mask.equals(mask)) return false;
    			try {
    				InetAddress b_net = applyNetMask(netb.network, mask);
    				InetAddress net = applyNetMask(network, mask);
    				return net.equals(b_net);
    			} catch(Exception e){
    				e.printStackTrace();
    				return false;
    			}
    		}
    		return false;
    	}
    }
    
    public static Net NetworkAndMaskFromString(String s) throws Exception {
    	String[] split= s.split("/");
    	if(split.length != 2) return null;
    	InetAddress network = InetAddress.getByName(split[0]);
    	int mask = getIntFromAddr(InetAddress.getByName("255.255.255.255"));
    	int zero_bits = 32 - Integer.parseInt(split[1]);
    	for(int i = 0; i < zero_bits; i++) mask  <<= 1;
    	return new Net(network, intToInetAddress(mask));
    }
    

    
    public static int getIntFromAddr(InetAddress in)
    {
        return ByteBuffer.wrap(in.getAddress()).getInt();
    } // -- getAddrInt
    
    // dw: this is way-overkill, but java unsigned-ness is a pain i just 
    // don't want to deal with
    public static long getLongFromAddr(InetAddress in)
    {
        byte[] arr = ByteBuffer.wrap(in.getAddress()).array();
        long[] l_arr = new long[arr.length];
        l_arr[0] = arr[0] & 0xFF;
        l_arr[1] = arr[1] & 0xFF;
        l_arr[2] = arr[2] & 0xFF;
        l_arr[3] = arr[3] & 0xFF;
        
        long l = ((long)l_arr[0] << (long)24 | (long)l_arr[1] << (long)16 | (long)l_arr[2] << (long)8 | (long)l_arr[3]);
        return l;
    } // -- getAddrInt
 
    /*  duplicate function
    public static InetAddress getAddrFromInt(int addr) throws Exception {
    	ByteBuffer b = ByteBuffer.allocate(4);
    	b.putInt(addr);
    	b.rewind();
    	return InetAddress.getByAddress(b.array());
    }
    
    */
    
    public static boolean isNetworkMatch(InetAddress testIp, InetAddress network, InetAddress netmask)
    {
        int ip = NetUtils.getIntFromAddr(testIp);
        int net  = NetUtils.getIntFromAddr(network);
        int mask  = NetUtils.getIntFromAddr(netmask);

        return ( (ip & mask) == (net & mask) );
 
    }
    
    public static InetAddress intToInetAddress(long addr) {
    	try {
    		byte[] bytes = new byte[4];
    		for (int i = 0; i < 4; ++i) {
    			bytes[i] = (byte)((addr >> (8 * (3 - i))) & 0xff);
    		}
    		return InetAddress.getByAddress(bytes);
    	}catch (Exception e) { e.printStackTrace(); }
    	return null;
    }
    
    public static InetAddress applyNetMask(InetAddress addr, InetAddress mask) throws Exception {
			long net_int = NetUtils.getLongFromAddr(addr);
 			long mask_int = NetUtils.getLongFromAddr(mask);
 			int result = (int)(net_int & mask_int);
 			return NetUtils.intToInetAddress(result);
    }
    
    public static InetAddress getMaxAddressInSubnet(InetAddress network, InetAddress netmask){
     
        int net  = NetUtils.getIntFromAddr(network);
        int mask  = NetUtils.getIntFromAddr(netmask);
        int max_addr = net | ~(mask);
        return intToInetAddress(max_addr);
    }
    
}
