/**
 * VRIPPacket.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;


import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;



/**
 * Encapsulates an Internet Protocol packet. Defined in RFC 791.
 */
public class IPPacket extends VNSPacket
{
    /**
     * Constucts an IP packet given valid bytes representing an IP packet,
     * consisting of an IP header and the payload (body).
     *
     * @param packetByteBuffer Byte buffer containing the IP packet
     */
    public IPPacket(ByteBuffer packetByteBuffer)
    {
        super(packetByteBuffer);

        byte [] header = new byte[HEADER_LEN];
        

        m_packetByteBuffer.rewind();

        // Extract header
        m_packetByteBuffer.get(header, 0, HEADER_LEN);
        m_header = new Header(ByteBuffer.wrap(header));
        
        // bug:  they were not getting the IP packet length from the 
        // header, thus there were trailing zeros (from Ethernet) for
        // small packets
        byte [] body = new byte[m_header.getPacketLength() - HEADER_LEN];

        // Extract body
        m_packetByteBuffer.get(body, 0, body.length);
        m_bodyBuffer = ByteBuffer.wrap(body);
    //    System.out.println("Extracting IP Packet with body length = " + body.length);
    //    System.out.println(VNSTCPPacket.getByteString(body));
    }

    /**
     * Constucts an IP packet given another packet for a payload, the
     * protocol of that other packet (e.g. ICMP) and the source and destination
     * addresses.
     */
    private IPPacket(VNSPacket packet, byte protocol,
            ByteBuffer srcAddr, ByteBuffer dstAddr)
    {
    	// get random for id
    	short id = (short) (rand.nextInt() & 0xffff);
    	
        // Construct the header
        m_header = new Header(VERSION_4, DEFAULT_IHL, 
                DEFAULT_TOS, 
                (short) (HEADER_LEN + packet.getByteBuffer().capacity()), // Total length
                id, DEFAULT_FLAGS,
                DEFAULT_FRAG_OFFSET, DEFAULT_TTL,
                protocol, srcAddr, dstAddr);

        // Assign the packet's body
        m_bodyBuffer = packet.getByteBuffer();

        // Allocate memory for the entire contents of the packet
        ByteBuffer packetBuffer = ByteBuffer.allocate(m_header.getLength() + 
                this.getBodyLength());

        // Put header and body in the packet buffer
        m_header.getByteBuffer().rewind();
        packetBuffer.put(m_header.getByteBuffer());
        m_bodyBuffer.rewind();
        packetBuffer.put(m_bodyBuffer);

        // Set the packet buffer in the super class
        setByteBuffer(packetBuffer);
    }
    
    public void setBodyBuffer(ByteBuffer new_bb){
    	m_bodyBuffer = new_bb;
    }

    public void pack()
    {
        // Allocate memory for the entire contents of the packet
        ByteBuffer packetBuffer = ByteBuffer.allocate(m_header.getLength() + 
                this.getBodyLength());

        // Put header and body in the packet buffer
        m_header.pack();
        m_header.getByteBuffer().rewind();
        packetBuffer.put(m_header.getByteBuffer());
        m_bodyBuffer.rewind();
        packetBuffer.put(m_bodyBuffer);

        // Set the packet buffer in the super class
        setByteBuffer(packetBuffer);
	//  	System.out.println("Packing IP Packet.  Body = " + m_bodyBuffer.capacity()
  	//			+ " bytes.  Total = " + m_packetByteBuffer.capacity());
    }
    

    /**
     * Returns the header of the IP packet.
     *
     * @return Header of the IP packet
     *
     * @see Header
     */
    public Header getHeader() { return m_header; }

    /**
     * Returns the byte buffer containing the body of an IP packet.
     *
     * @return Byte buffer containing the body of an IP packet
     */
    public ByteBuffer getBodyBuffer()
    { 
        m_bodyBuffer.rewind();
        return m_bodyBuffer;
    }

    public VNSPacket getPayload() 
    {
        if (m_header.getProtocol() == PROTO_ICMP){
            VNSICMPPacket icmppacket = new VNSICMPPacket(getBodyBuffer());
            icmppacket.setParentHeader(this);
            return icmppacket; 
        }else{
            return null;
        }
    } // -- getPayload(..)

    /**
     * Returns length, in bytes, of an IP packet's body.
     *
     * @return Length of an IP packet's body
     */
    public int getBodyLength() { return m_bodyBuffer.capacity(); }

    /**
     * Returns a byte array that contains the payload of an IP packet.
     *
     * @return Array of bytes containing the body of an IP packet
     */
    public byte [] getBodyBytes()
    {
        byte [] bodyBytes = new byte[m_bodyBuffer.capacity()];
        m_bodyBuffer.rewind();
        m_bodyBuffer.get(bodyBytes, 0, m_bodyBuffer.capacity());
        return bodyBytes;
    }
    
    public static IPPacket wrap(VNSPacket packet, byte protocol, 
    		InetAddress srcAddr, InetAddress destAddr) throws VNSInvalidPacketException {
    	ByteBuffer src = ByteBuffer.wrap(srcAddr.getAddress());
    	ByteBuffer dst = ByteBuffer.wrap(destAddr.getAddress());
    	return wrap(packet, protocol, src, dst);
    }

    /**
     * Creates an IP packet by using the supplied packet as a payload. If a
     * supplied packet is another IP packet, a VRInvalidPacketException is
     * thrown. Default values are used for header fields not listed in this
     * method's formal parameter list.
     *
     * @param packet Any packet, except IP packet
     * @param protocol Protocol
     * @param srcAddr Source IP address
     * @param dstAddr Destination IP address
     */
    public static IPPacket wrap(VNSPacket packet, byte protocol,
            ByteBuffer srcAddr, ByteBuffer dstAddr)
        throws VNSInvalidPacketException
        {
    		// dw: why have this??
    		//if( packet instanceof VNSIPPacket )
              //  throw new VNSInvalidPacketException("IP packet cannot wrap itself");

            return new IPPacket(packet, protocol, srcAddr, dstAddr);
        }

    /**
     * Represents an IP packet in a readable format, by field. It is
     * equivalent to calling toString() on the header of the packet.
     */
    public String toString()
    {
        return m_header.toString();
    }

    private ByteBuffer m_bodyBuffer;
    private Header m_header;
    
    private static Random rand;
    
    static {
    	rand = new Random();
    }

    // Header length
    private static final byte HEADER_LEN = 20;

    private static final byte VERSION_4            = 4;
    private static final byte DEFAULT_IHL          = 5; // Header length in 32-bit words
    private static final byte DEFAULT_TOS          = 0;
    private static final byte DEFAULT_TTL          = 64;
  //  private static final short DEFAULT_ID          = 0;
    private static final short DEFAULT_FLAGS       = 0;
    private static final short DEFAULT_FRAG_OFFSET = 0;

    /**
     * Protocols
     */
    public static final byte PROTO_IP       = 0;	 // IP
    public static final byte PROTO_HOPOPTS  = 0;	 // Hop by hop header for IPv6
    public static final byte PROTO_ICMP     = 1;	 // Control Message Protocol
    public static final byte PROTO_IGMP     = 2;	 // Group Control Protocol
    public static final byte PROTO_GGP      = 3;	 // Gateway^2 (deprecated)
    public static final byte PPROTO_ENCAP   = 4;	 // IP in IP encapsulation
    public static final byte PROTO_TCP      = 6;	 // TCP
    public static final byte PROTO_EGP      = 8;	 // Exterior Gateway Protocol
    public static final byte PROTO_PUP      = 12;	 // PUP
    public static final byte PROTO_UDP      = 17;	 // User Datagram Protocol
    public static final byte PROTO_IDP      = 22;	 // XNS IDP
    public static final byte PROTO_IPV6     = 41;  // IPv6 encapsulated in IP
    public static final byte PROTO_ROUTING  = 43;	 // Routing header for IPv6
    public static final byte PROTO_FRAGMENT = 44;	 // Fragment header for IPv6
    public static final byte PROTO_RSVP     = 46;	 // RSVP
    public static final byte PROTO_ESP      = 50;	 // IPsec Encap. Sec. Payload
    public static final byte PROTO_AH       = 51;	 // IPsec Authentication Hdr.
    public static final byte PROTO_ICMPV6   = 58;	 // ICMP for IPv6
    public static final byte PROTO_NONE     = 59;	 // No next header for IPv6
    public static final byte PROTO_DSTOPTS  = 60;	 // Destination options
    public static final byte PPROTO_HELLO   = 63;	 // "hello" routing protocol
    public static final byte PROTO_ND       = 77;	 // UNOFFICIAL net disk proto
    public static final byte PROTO_EON      = 80;	 // ISO clnp
    public static final byte PROTO_PIM      = 103; // PIM routing protocol
    
    public static final byte PROTO_CLACK_RIP  = 119;  // Clack's toy distance vector protocol
    public static final byte PROTO_CLACK_OSPF = 120; // Clack's toy link state protocol
    
    
    // new convience methods for accessing IP header fields 
    // TODO: We eventually want to change everything to be accessed like this
    
    public InetAddress getSourceAddress() {
    	try {
    		InetAddress.getByAddress(getHeader().getSourceAddress().array());
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public InetAddress getDestinationAddress() {
    	try {
    		InetAddress.getByAddress(getHeader().getDestinationAddress().array());
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	return null;
    } 
    
    public byte getProtocol() {
    	return getHeader().getProtocol();
    }
    
    
    
    /**
     * Encapsulates IP packet header.
     */
    public class Header
    {
        /**
         * Constructs IP packet header.
         *
         * @param headerBuffer Byte buffer containing the header of an IP packet
         */
        public Header(ByteBuffer headerBuffer)
        {
            m_headerBuffer = headerBuffer;
            m_length = m_headerBuffer.capacity();

            // Extract individual fields of the header
            extractFromHeaderBuffer();
        }

        /**
         * Extracts individual fields of the header.
         */
        private void extractFromHeaderBuffer()
        {
            byte [] versionAndIHL = new byte[VERSION_AND_IHL_LEN];
            byte [] typeOfService = new byte[TOS_LEN];
            byte [] totalLength = new byte[TOTAL_LENGTH_LEN];
            byte [] identification = new byte[ID_LEN];
            byte [] flagsAndFragmentOffset = new byte[FLAGS_AND_FRAG_OFFSET_LEN];
            byte [] ttl = new byte[TTL_LEN];
            byte [] protocol = new byte[PROTOCOL_LEN];
            byte [] checksum = new byte[CHECKSUM_LEN];
            byte [] srcAddr = new byte[ADDR_LEN];
            byte [] dstAddr = new byte[ADDR_LEN];

            m_headerBuffer.rewind();

            // Extract version and IHL
            m_headerBuffer.get(versionAndIHL, 0, VERSION_AND_IHL_LEN);
            m_versionAndIHL = ByteBuffer.wrap(versionAndIHL);

            // Extract type of service
            m_headerBuffer.get(typeOfService, 0, TOS_LEN);
            m_typeOfService = ByteBuffer.wrap(typeOfService);

            // Extract total length
            m_headerBuffer.get(totalLength, 0, TOTAL_LENGTH_LEN);
            m_totalLength = ByteBuffer.wrap(totalLength);

            // Extract identification
            m_headerBuffer.get(identification, 0, ID_LEN);
            m_identification = ByteBuffer.wrap(identification);

            // Extract flags and fragmentation offset
            m_headerBuffer.get(flagsAndFragmentOffset, 0, FLAGS_AND_FRAG_OFFSET_LEN);
            m_flagsAndFragmentOffset =
                ByteBuffer.wrap(flagsAndFragmentOffset);

            // Extract TTL
            m_headerBuffer.get(ttl, 0, TTL_LEN);
            m_ttl = ByteBuffer.wrap(ttl);

            // Extract protocol
            m_headerBuffer.get(protocol, 0, PROTOCOL_LEN);
            m_protocol = ByteBuffer.wrap(protocol);

            // Extract header checksum
            m_headerBuffer.get(checksum, 0, CHECKSUM_LEN);
            m_checksum = ByteBuffer.wrap(checksum);

            // Extract source address
            m_headerBuffer.get(srcAddr, 0, ADDR_LEN);
            m_srcAddr = ByteBuffer.wrap(srcAddr);

            // Extract destination address
            m_headerBuffer.get(dstAddr, 0, ADDR_LEN);
            m_dstAddr = ByteBuffer.wrap(dstAddr);

            //System.err.println(this);
        }

        /**
         * Constructs an IP header from given parameters. Checksum is omitted, to
         * be inserted after all field in the header are filled.
         *
         * @param version IP version; only 4 least significant bits are used
         * @param ihl     Internet header length; only 4 least significant
         *                bits are used
         * @param tos Type of service
         * @param totalLength Total length of a packet, in bytes
         * @param identification Identification
         * @param flags   Control flags; only 3 least significant bits are
         *                used 
         * @param fragmentOffset Fragmentation offset; only 13 least
         *                       significant bits are used
         * @param ttl Time-To-Live
         * @param protocol Protocol
         * @param srcAddr Source IP address
         * @param dstAddr Destination IP address
         */
        public Header(byte version, byte ihl, byte tos, short totalLength,
                short identification, short flags, short fragmentOffset,
                byte ttl, byte protocol, ByteBuffer srcAddr,
                ByteBuffer dstAddr)
        {
            byte versionAndIHL = (byte) ((version << IHL_BIT_LEN) | 
                    (ihl & IHL_BIT_MASK));

            short flagsAndFragmentOffset = (short) ((flags << FRAG_OFFSET_BIT_LEN) |
                    (fragmentOffset &
                     FRAG_OFFSET_BIT_MASK));

            srcAddr.rewind();
            dstAddr.rewind();

            ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_LEN);

            headerBuffer.put(versionAndIHL);
            headerBuffer.put(tos);
            headerBuffer.putShort(totalLength);
            headerBuffer.putShort(identification);
            headerBuffer.putShort(flagsAndFragmentOffset);
            headerBuffer.put(ttl);
            headerBuffer.put(protocol);
            headerBuffer.putShort((short) 0); // Checksum will be put later
            headerBuffer.put(srcAddr);
            headerBuffer.put(dstAddr);

            m_headerBuffer = headerBuffer;
            m_length = m_headerBuffer.capacity();

            // Set header's checksum now that the header was constucted
            short checksum = calculateChecksum();
            // Byte offset = 2 * Word Offset
            m_headerBuffer.putShort(2*CHECKSUM_WORD_OFFSET, checksum);

            // Extract individual fields of the header
            extractFromHeaderBuffer();
        }

        /**
         * Write all header fields to the internal buffer.
         */
        public void pack()
        {
            m_versionAndIHL.rewind();
            m_typeOfService.rewind();
            m_totalLength.rewind();
            m_identification.rewind();
            m_flagsAndFragmentOffset.rewind();
            m_ttl.rewind();
            m_protocol.rewind();
            m_checksum.rewind();
            m_srcAddr.rewind();
            m_dstAddr.rewind();

            ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_LEN);

            headerBuffer.put(m_versionAndIHL);
            headerBuffer.put(m_typeOfService);
            headerBuffer.put(m_totalLength);
            headerBuffer.put(m_identification);
            headerBuffer.put(m_flagsAndFragmentOffset);
            headerBuffer.put(m_ttl);
            headerBuffer.put(m_protocol);
            headerBuffer.put(m_checksum);
            headerBuffer.put(m_srcAddr);
            headerBuffer.put(m_dstAddr);

            m_headerBuffer = headerBuffer;
            m_length = m_headerBuffer.capacity();
        }
        
    //    public ByteBuffer getPseudoHeaderForTCP() {
    //    	ByteBuffer pheader = ByteBuffer.allocate(12);
    //    	pheader.put(m_srcAddr);
     //   	pheader.put(m_dstAddr);
     //   	pheader.put((byte)0);
      //  	pheader.put(m_protocol);
      //  	pheader.put(m_totalLength);
     //   	return pheader;
      //  }

        /**
         * Sets the checksum to the supplied value.
         *
         * @param checksum Checksum
         */
        public void setChecksum(short checksum)
        {
            m_checksum.rewind();
            m_checksum.putShort(checksum);
        }

        /**
         * Sets the checksum to the value calculated by the method
         * calculateChecksum.
         *
         * @see #calculateChecksum
         */
        public void setChecksum()
        {
            setChecksum(calculateChecksum());
        }
        
        public int getPacketLength() {
        	m_totalLength.rewind();
        	return 0xFFFF & m_totalLength.getShort();
        }

        /**
         * Returns a byte buffer containing the header.
         *
         * @return Byte buffer containing the header
         */
        public ByteBuffer getByteBuffer() { return m_headerBuffer; }

        /**
         * Returns the length of the header, in bytes.
         *
         * @return Length of the IP header
         */
        public int getLength() { return m_length; }

        /**
         * Returns an array of bytes representing the header.
         *
         * @return An array of bytes representing the header
         */
        public byte [] getBytes()
        {
            return m_headerBuffer.array();
        }

        /**
         * Returns the source address in the IP header.
         *
         * @return Byte buffer containing the source IP address
         */
        public ByteBuffer getSourceAddress()
        {
            m_srcAddr.rewind();
            return m_srcAddr;
        }

        /**
         * Returns the destination address in the IP header.
         *
         * @return Byte buffer containing the destination IP address
         */
        public ByteBuffer getDestinationAddress()
        {
            m_dstAddr.rewind();
            return m_dstAddr;
        }

        /**
         * Calculates IP header checksum.
         *
         * The checksum field is the 16 bit one's complement of the one's
         * complement sum of all 16 bit words in the header.  For purposes of
         * computing the checksum, the value of the checksum field is zero.
         *
         * @return Checksum
         */
        public short calculateChecksum()
        {
            int sum = 0;

            m_headerBuffer.rewind();

            for(int wordCount = 0; wordCount < m_headerBuffer.capacity() / 2; wordCount++) {

                int word = (short) m_headerBuffer.getShort();
                word &= 0xffff; // Convert into an unsigned short

                // Ignore the checksum field
                if( wordCount == CHECKSUM_WORD_OFFSET )
                    continue;

                sum += word;

                // If overflow occured
                if( (sum & 0xffff0000) != 0 ) {
                    sum &= 0xffff;
                    sum++;
                }
            }

            return (short) ~(sum & 0xffff);
        }

        /**
         * Returns the packet's checksum field value.
         *
         * @return Checksum
         */
        public short getChecksum()
        {
            m_checksum.rewind();
            return m_checksum.getShort();
        }

        /**
         * Returns the packet's Time-To-Live field value.
         *
         * @return Time-To-Live
         */
        public int getTTL()
        {
            m_ttl.rewind();
            byte b = m_ttl.get(); 
            return (b & 0xff);
        }

        /**
         * Sets the packet's Time-To-Live field value.
         *
         * @param newTTL Time-To-Live
         */
        public void setTTL(int newTTL)
        {
            m_ttl.rewind();
            m_ttl.put((byte)(newTTL & 0xff));
        }

        public void setSrcAddr(ByteBuffer src)
        {
            m_srcAddr.rewind();
            m_srcAddr.put(src);
        }
        
        public void setDestAddr(ByteBuffer dst)
        {
            m_dstAddr.rewind();
            m_dstAddr.put(dst);
        }

        /**
         * Returns the packet's Protocol field value.
         *
         * @return Protocol
         */
        public byte getProtocol()
        {
            m_protocol.rewind();
            return m_protocol.get();
        }

        /**
         * Represents the header in readable format, by field.
         *
         * @return Header in readable format
         */
        public String toString()
        {
            StringBuffer stringBuffer = new StringBuffer();

            byte versionAndIHL;

            m_versionAndIHL.rewind();
            versionAndIHL = m_versionAndIHL.get();

            stringBuffer.append("IP header length: " + m_length + "\n");
            stringBuffer.append("Version and IHL: " +
                    getStringBuffer(m_versionAndIHL).toString() + 
                    " " + Integer.toBinaryString(versionAndIHL) + "\n");
            stringBuffer.append("TOS: " +
                    getStringBuffer(m_typeOfService).toString() + "\n");
            stringBuffer.append("Total length: " +
                    getStringBuffer(m_totalLength).toString() +
                    "\n");
            stringBuffer.append("Identification: " +
                    getStringBuffer(m_identification).toString() + "\n");
            stringBuffer.append("Flags and fragmentation: " +
                    getStringBuffer(m_flagsAndFragmentOffset).toString() + "\n");
            stringBuffer.append("TTL: " +
                    getStringBuffer(m_ttl).toString() + "\n");
            stringBuffer.append("Protocol: " +
                    getStringBuffer(m_protocol).toString() + "\n");
            stringBuffer.append("Checksum: " +
                    getStringBuffer(m_checksum).toString() + "\n");
            stringBuffer.append("Source address: " +
                    getStringBuffer(m_srcAddr).toString() + "\n");
            stringBuffer.append("Destination address: " +
                    getStringBuffer(m_dstAddr).toString());

            return stringBuffer.toString();
        }

        private static final int CHECKSUM_WORD_OFFSET = 5;
        private static final int IHL_BIT_MASK         = 15;
        private static final int FRAG_OFFSET_BIT_MASK = 8191;

        // Lengths of sub-byte fields, in bits
        public static final int VERSION_BIT_LEN      = 4;
        public static final int IHL_BIT_LEN          = 4;
        public static final int FLAGS_BIT_LEN        = 3;
        public static final int FRAG_OFFSET_BIT_LEN  = 13;

        // Lengths of fields, in bytes
        public static final int VERSION_AND_IHL_LEN       = 1;
        public static final int TOS_LEN                   = 1;
        public static final int TOTAL_LENGTH_LEN          = 2;
        public static final int ID_LEN                    = 2;
        public static final int FLAGS_AND_FRAG_OFFSET_LEN = 2;
        public static final int TTL_LEN                   = 1;
        public static final int PROTOCOL_LEN              = 1;
        public static final int CHECKSUM_LEN              = 2;
        public static final int ADDR_LEN                  = 4;

        // Entire contents of the header
        private ByteBuffer m_headerBuffer;
        private int m_length;

        // Header layout
        private ByteBuffer m_versionAndIHL;          // Version and internet header length
        private ByteBuffer m_typeOfService;          // Type of service (priority)
        private ByteBuffer m_totalLength;            // Total length of the datagram
        private ByteBuffer m_identification;         //
        private ByteBuffer m_flagsAndFragmentOffset; // Fragmentation offset
        private ByteBuffer m_ttl;                    // Time-To-Live
        private ByteBuffer m_protocol;               // Protocol
        private ByteBuffer m_checksum;               // Header checksum
        private ByteBuffer m_srcAddr;                // Source address
        private ByteBuffer m_dstAddr;                // Destination address

        // Accessors

        public byte getVIHL()
        { 
            byte versionAndIHL;
            m_versionAndIHL.rewind();
            versionAndIHL = m_versionAndIHL.get();
            m_versionAndIHL.rewind();
            return versionAndIHL; 
        }
    } // class Header
}
