
package net.clackrouter.netutils;

import java.io.Serializable;
import java.net.UnknownHostException;

/**
 * Class representing an Ethernet address and associated utility functions. 
 */
public class EthernetAddress implements Serializable
{
    static int LEN = 6; 

    static
    public EthernetAddress getByAddress(byte [] addr_in) throws UnknownHostException
    {
        if ( addr_in.length != LEN )  
        { throw new UnknownHostException(); }

        EthernetAddress returnme = new EthernetAddress();
        System.arraycopy(addr_in, 0, returnme.addr, 0, LEN);

        returnme.generateReadableString();

        return returnme; 
    }
    
    static
    public EthernetAddress getByAddress(String addr_in){
        EthernetAddress returnme = new EthernetAddress();
    	String[] split = addr_in.split(":");
    	
    	byte[] tmp = new byte[LEN];
    	for(int i = 0; i < LEN; i++){
    		Integer integer = Integer.decode("0x" + split[i]);
    		tmp[i] = (byte) integer.byteValue();
    	}

        System.arraycopy(tmp, 0, returnme.addr, 0, LEN);
        returnme.generateReadableString(); 
    	// System.out.println("After decode: " + returnme + " " + addr_in);	
        
        return returnme;
    }


    byte [] addr    = null;
    String  straddr = "";

    public EthernetAddress()
    { 
        addr    = new byte[LEN]; 
    }

    public String toString()
    { return straddr; }
    
    public byte[] getBytes() { return addr; }
    public byte[] getAddress() { return addr; }

    private void generateReadableString()
    {
        straddr = ""; 
        for(int i = 0 ; i < LEN - 1; ++i)
        { straddr += Integer.toHexString((int)addr[i] & 0x000000ff)+":"; }
        //{ straddr += (new Integer((int)addr[i])).toHexString()+":"; }
        straddr += Integer.toHexString((int)addr[LEN-1] & 0x000000ff);
    }
    
    public boolean equals(Object obj) {

    	if(obj == null) return false;
    	EthernetAddress other = (EthernetAddress)obj;
    	byte[] other_bytes = other.getBytes();
    	for(int i = 0; i < LEN; i++){
    		if(other_bytes[i] != addr[i]) return false;
    	}
    	return true;
    }

} // -- EthernetAddress
