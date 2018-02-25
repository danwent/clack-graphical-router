/**
 * VRPacket.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;


import java.nio.ByteBuffer;

import net.clackrouter.protocol.data.VNSData;



/**
 * Encapsulates common functionality of a packet.
 */
public class VNSPacket extends VNSData
{


    // --
    // Annotations for each packet
    // --

    // Input interface name, set at packet arrival time
    protected String m_inputInterfaceName;

    // Output interface name, set at address lookup time
    protected String m_outputInterfaceName;

    // Next hop address of the packet, set at address lookup time
    protected ByteBuffer m_nextHopIPAddr;
    protected ByteBuffer m_nextHopMacAddr;
    protected ByteBuffer m_level2Type;
    protected boolean    m_local_packet;


    protected ByteBuffer m_packetByteBuffer; // The entire contents of the packet
    protected int m_length;                  // Length of the bytes

    
    // -- Used to create a chain of packet headers up to the
    //    uppermost header
    protected VNSPacket mParentHeader = null;

    private StringBuffer m_path;


    public boolean isPacket() { return true; }

    /**
     * Constructs a packet from the supplied byte buffer. The buffer is
     * presumed to contain the full contents of the packet.
     *
     * @param packetByteBuffer Byte buffer containing bytes representing the packet
     */
    public VNSPacket(ByteBuffer packetByteBuffer)
    {
        super();

        setByteBuffer(packetByteBuffer);

        m_inputInterfaceName  = "unknown";
        m_outputInterfaceName = "unknown";
        m_nextHopIPAddr  = null;
        m_local_packet = false;

        m_path = new StringBuffer();
    }

    /**
     * Constructs a packet of zero length.
     */
    protected VNSPacket()
    {
        setByteBuffer(ByteBuffer.allocate(0));

        m_path = new StringBuffer();
    }

    protected StringBuffer getStringBuffer(ByteBuffer byteBuffer)
    {
        byteBuffer.rewind();

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[ ");
        for(int i=0; i<byteBuffer.capacity(); i++)
            stringBuffer.append( byteBuffer.get() + " ");
        stringBuffer.append("]");

        return stringBuffer;
    }

    /**
     * Sets the packet's byte buffer to the supplied byte buffer.
     *
     * @param packetByteBuffer Byte buffer containing bytes representing the packet
     */
    protected void setByteBuffer(ByteBuffer packetByteBuffer)
    {
        m_length = packetByteBuffer.capacity();
        m_packetByteBuffer = packetByteBuffer;
    }

    public void setParentHeader(VNSPacket parent){
        mParentHeader = parent;
    }

    public VNSPacket getParentHeader(){
        return mParentHeader;
    }

    /**
     * Returns the interface name on which the packet came in, e.g. "eth0".
     *
     * @return Interface name on which the packet came in
     */
    public String getInputInterfaceName() { return m_inputInterfaceName; }

    /**
     * Returns the interfaces name on which the packet will leave, e.g "eth1".
     *
     * @return Interfaces name on which the packet will leave
     */
    public String getOutputInterfaceName() { return m_outputInterfaceName; }

    /**
     * Returns the packet's next-hop IP address. This parameter is set
     * at the time of address lookup.
     *
     * @return Byte buffer containing packet's next-hop IP address
     */
    public ByteBuffer getNextHopIPAddress()
    {
        m_nextHopIPAddr.rewind();
        return m_nextHopIPAddr;
    }

    public void setNeedsSourceAddress(boolean local)
    { m_local_packet = local; }

    public boolean needsSourceAddress()
    { return m_local_packet; }

    /**
     * Sets the input interface on which the packet came in.
     *
     * @param inputInterfaceName Interface on which the packet came in
     */
    public void setInputInterfaceName(String inputInterfaceName)
    {
        m_inputInterfaceName = inputInterfaceName;
    }

    /**
     * Sets the output interface on which the packet will go out.
     *
     * @param outputInterfaceName Interface on which the packet will go out
     */

    public void setOutputInterfaceName(String outputInterfaceName)
    { m_outputInterfaceName = outputInterfaceName; }

    /**
     * Sets packet's next-hop IP address. This operaion is performed at the
     * time of address lookup but can be done at any time by any mischievous box.
     *
     * @param nextHopAddr Next-hop IP address.
     */
    public void setNextHopIPAddress(ByteBuffer nextHopAddr)  { m_nextHopIPAddr = nextHopAddr; } 
    public void setNextHopMacAddress(ByteBuffer nextHopMacAddr)  { m_nextHopMacAddr = nextHopMacAddr; }  
    public ByteBuffer getNextHopMacAddress()  { m_nextHopMacAddr.rewind();     return m_nextHopMacAddr;  }   
    public void setLevel2Type(ByteBuffer type) { m_level2Type = type; }
    public ByteBuffer getLevel2Type() { m_level2Type.rewind(); return m_level2Type; }
    /**
     * Returns the byte buffer containing the packet's bytes.
     *
     * @return Byte buffer containing the packet's bytes
     */
    public ByteBuffer getByteBuffer() { return m_packetByteBuffer; }

    /**
     * Returns the length of the packet, in bytes.
     *
     * @return Number of bytes in the packet
     */
    public int getLength() { return m_length; }

    /**
     * Represents the packet in a readable format, by field.
     *
     * @return Packet in readable format
     */
    public String toString()
    {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("Input interface name: " + m_inputInterfaceName + "\n");
        stringBuffer.append("Output interface name: " + m_outputInterfaceName + "\n");
        stringBuffer.append("Path: " + m_path);

        return stringBuffer.toString();
    }

    public void addToPath(String name)
    {
        m_path.append(" [" + name + "]");
    }
    
    
	  // helper to handle yucky java "ununsignness" for 32-bit ints
	  public static long get32bit(byte[] arr){
      int firstByte = (0x000000FF & ((int)arr[0]));
      int secondByte = (0x000000FF & ((int)arr[1]));
      int thirdByte = (0x000000FF & ((int)arr[2]));
      int fourthByte = (0x000000FF & ((int)arr[3]));
      
      return ((long) (firstByte << 24
	                | secondByte << 16
                      | thirdByte << 8
                      | fourthByte))
                     & 0xFFFFFFFFL;  	
	  }
	  
//		 helper to handle yucky java "ununsignness" for 16-bit ints
	  public static int get16bit(byte[] arr){
        int firstByte = (0x000000FF & ((int)arr[0]));
        int secondByte = (0x000000FF & ((int)arr[1]));
	    return (int) (firstByte << 8 | secondByte);	  	
	  }

}
