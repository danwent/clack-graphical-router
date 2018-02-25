
package net.clackrouter.packets;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Represents a Domain Name System (DNS) packet.
 * 
 * <p> Currently supports simple requests and responses with Query, 
 * Answer, Authority and Additional record types.  </p>
 */
public class DNSPacket extends VNSPacket {
	
	public static final int DNS_PORT = 53;
	public static final short HEADER_SIZE = 12;
	public static final int FLAGS_QUERY_RESPONSE_NO_ERROR = 0x8180;
	public static final int FLAGS_QUERY_STANDARD = 0x0100;
	public static final int TYPE_A = 1;
	public static final int TYPE_NS = 2;
	public static final int TYPE_CNAME = 5;
	public static final int TYPE_MX = 15;	
	public static final int CLASS_INET = 1;
	
	private short mTransactionID, mFlags;
	private ArrayList mQuestions, mAnswerRRs, mAuthorityRRs, mAdditionalRRs;
	private int current_question_offset = HEADER_SIZE;
	
	public DNSPacket(ByteBuffer buf) throws Exception {
		super(buf);
		mQuestions = new ArrayList();
		mAnswerRRs = new ArrayList();
		mAuthorityRRs = new ArrayList();
		mAdditionalRRs = new ArrayList();
		extractFromByteBuffer();
	}
	
	
	public DNSPacket(int transactionID, int flags)throws Exception {
		this(ByteBuffer.allocate(0));
		mTransactionID = (short)transactionID;
		mFlags = (short)flags;	
	}
	
	
	public void addQueryRecord(Query q) { mQuestions.add(q); pack(); }
	public void addAnswerRecord(Answer a) { mAnswerRRs.add(a); pack(); }
	public void addAuthorityRecord(Answer a) { mAuthorityRRs.add(a); pack(); }
	public void addAdditionalRecord(Answer a) { mAuthorityRRs.add(a); pack(); }
	
	public Query getQueryRecord(int i) { return (Query)mQuestions.get(i); }
	public Answer getAnswerRecord(int i) { return (Answer)mAnswerRRs.get(i); }
	public Answer getAuthorityRecord(int i) { return (Answer)mAuthorityRRs.get(i); }
	public Answer getAdditionalRecord(int i) { return (Answer)mAdditionalRRs.get(i); }
	
	public int getQueryRecordCount() { return mQuestions.size(); }
	public int getAnswerRecordCount() { return mAnswerRRs.size(); }
	public int getAuthorityRecordCount() { return mAuthorityRRs.size(); }
	public int getAdditionalRecordCount() { return mAdditionalRRs.size(); }
	
	public short getFlags() { return mFlags; }
	public void setFlags(short f) { mFlags = f; pack();}
	public short getID() { return mTransactionID; }
	public void setID(short id) { mTransactionID = id; pack();}
	
	public void pack() {
		
		ArrayList qbufs = new ArrayList();
		int total_q_size = 0;
		for(int i = 0; i < mQuestions.size(); i++){
			Query q = (Query)mQuestions.get(i);
			byte[] name_arr = getQNameArray(q.name);
			ByteBuffer buf = ByteBuffer.allocate(name_arr.length + 4);
			buf.put(name_arr);
			buf.putShort(q.dns_type);
			buf.putShort(q.dns_class);
			buf.rewind();
			qbufs.add(buf);
			total_q_size += buf.capacity();
		}
		
		ArrayList abufs = new ArrayList();
		int total_a_size = 0;
		for(int i = 0; i < mAnswerRRs.size(); i++){
			Answer a = (Answer)mAnswerRRs.get(i);
			byte[] name_arr = getQNameArray(a.name);
			ByteBuffer buf = ByteBuffer.allocate(name_arr.length + a.data.length + 12);
			buf.put(name_arr);
			buf.putShort(a.dns_type);
			buf.putShort(a.dns_class);
			buf.putInt(a.timeout);
			buf.putShort((short)a.data.length);
			buf.put(a.data);
			buf.rewind();
			abufs.add(buf);
			total_a_size += buf.capacity();
		}		
		m_packetByteBuffer = ByteBuffer.allocate(HEADER_SIZE + total_q_size + total_a_size);
		m_packetByteBuffer.putShort(mTransactionID);
		m_packetByteBuffer.putShort(mFlags);
		m_packetByteBuffer.putShort((short)mQuestions.size());
		m_packetByteBuffer.putShort((short)mAnswerRRs.size());
		m_packetByteBuffer.putShort((short)0);
		m_packetByteBuffer.putShort((short)0);
		
		for(int i = 0; i < mQuestions.size(); i++){
			m_packetByteBuffer.put((ByteBuffer)qbufs.get(i));
		}
		for(int i = 0; i < mAnswerRRs.size(); i++){
			m_packetByteBuffer.put((ByteBuffer)abufs.get(i));
		}
		m_packetByteBuffer.rewind();
	}

	
	
	public static byte[] getQNameArray(String name){
		byte[] arr = new byte[name.length() + 2];
		arr[name.length()] = 0; // null terminate
		byte count = 0;
		for(int i = name.length() - 1; i >= 0; i--){
			char c = name.charAt(i);
			if(c == '.'){
				arr[i + 1] = count;
				count = 0;
			}else {
				arr[i + 1] = (byte)c;
				count++;
			}
		}
		arr[0] = count;
		return arr;
	}
	
	private static String getStringFromQName(byte[] array){
		StringBuffer sb = new StringBuffer(array.length);
		byte count = (byte) (array[0]);
		for(int i = 1; i < array.length - 1; i++){
			if(count == 0){
				sb.append(".");
				count = array[i];
			}else {
				sb.append(Character.toString((char)array[i]));
				count--;
			}
		}
		return sb.toString();
	}
	
	private void extractFromByteBuffer() throws Exception {
		m_packetByteBuffer.rewind();
		if(m_packetByteBuffer.capacity() == 0) return ; // we don't have packet data
	//	System.out.println("Extracting DNS packet of size; " + m_packetByteBuffer.capacity());
		
		mTransactionID = m_packetByteBuffer.getShort();
		mFlags = m_packetByteBuffer.getShort();
		short mQuestionSize = m_packetByteBuffer.getShort();
		short mAnswerRRSize = m_packetByteBuffer.getShort();
		short mAuthorityRRSize = m_packetByteBuffer.getShort();
		short mAdditionalRRSize = m_packetByteBuffer.getShort();
		byte[] dnspacket = m_packetByteBuffer.array();
		
		int offset = HEADER_SIZE;
		
		
		for(int i = 0; i < mQuestionSize; i++){
			offset = extractQuery(mQuestions, dnspacket,offset );
		}
		for(int i = 0; i < mAnswerRRSize; i++){
			offset = extractAnswer(mAnswerRRs, dnspacket,offset );
		}
		for(int i = 0; i < mAuthorityRRSize; i++){
			offset = extractAnswer(mAuthorityRRs, dnspacket,offset );
		}
		for(int i = 0; i < mAdditionalRRSize; i++){
			offset = extractAnswer(mAdditionalRRs, dnspacket,offset );
		}
	}
	
	private static int extractQuery(ArrayList list, byte[] dnspacket, int offset)throws Exception {
		ParsePair pair = getStringAtOffset(dnspacket, offset);
		offset = pair.offset;
		short dns_type = (short)((dnspacket[offset] << 8) + dnspacket[offset + 1]);
		offset += 2;
		short dns_class = (short)((dnspacket[offset] << 8) + dnspacket[offset + 1]);
		list.add(new Query(pair.name, dns_type, dns_class));
		return offset += 2;
	}
	
	private static int extractAnswer(ArrayList list, byte[] dnspacket, int offset) throws Exception {
		ParsePair pair = getStringAtOffset(dnspacket, offset);
		offset = pair.offset;
		short dns_type = (short)((dnspacket[offset] << 8) + dnspacket[offset + 1]);
		offset += 2;
		short dns_class = (short)((dnspacket[offset] << 8) + dnspacket[offset + 1]);
		offset += 2;
		int ttl = ((dnspacket[offset] & 0xff) << 24) + ((dnspacket[offset + 1]& 0xff) << 16) 
			+ ((dnspacket[offset + 2]& 0xff) << 8) + ((dnspacket[offset + 3]& 0xff));
		offset += 4;
		int datalen = ((dnspacket[offset] << 8) + dnspacket[offset + 1]);
		offset += 2;

		byte[] data;
		if(dns_type == DNSPacket.TYPE_A){
			data = new byte[datalen];
			System.arraycopy(dnspacket, offset, data, 0, datalen);
		}else {
			// assume all types except for TYPE A are text.  this is a simplification
			ParsePair p = getStringAtOffset(dnspacket, offset);
			data = p.name.getBytes();
		}
		offset += datalen;
		list.add(new Answer(pair.name, dns_type, dns_class, ttl, data));
		return offset;
	}
	
	/*
	 * recursive function to get a string that starts at a given offset in a
	 * dns packet.  
	 */
	private static ParsePair getStringAtOffset(byte[] dnspacket, int offset) throws InvalidQNameParseException {
		
		StringBuffer sb = new StringBuffer(25);
		int count =  (dnspacket[offset] & 0xff); // starting point
//		System.out.println("looking for string at offset = " + offset + " with count = " + count);
		if(count == 0x0){
			return new ParsePair("", (short)(offset+1)); // base case
		}
		if((count & 0xc0) != 0){
			if(count != 0xc0) throw new InvalidQNameParseException("not valid qname");
			// compression!
			int newOffset = (dnspacket[offset + 1] & 0xff);
	//		System.out.println("jumping to offset " + newOffset);
			ParsePair pair1 = getStringAtOffset(dnspacket, newOffset);
	//		System.out.println("got: " + pair1.name);
			sb.append(pair1.name);
			return new ParsePair(pair1.name, offset + 2);
		}
		// loop only reads one sequence until we append a '.',
		// then we add the rest of the string with a recursive call
		for(int i = offset + 1; ; i++){
			if(count == 0){
				
				ParsePair pair = getStringAtOffset(dnspacket, (short)i);
				if(pair.name.length() > 0){
					sb.append(".");
					sb.append(pair.name);
				}		
	//			System.out.println("app:" + pair.name);
				return new ParsePair(sb.toString(), pair.offset);
			}else {
				sb.append(Character.toString((char)dnspacket[i]));
				count--;
			}
		}

	}
	/**
	 * Contains DNS Query information
	 */
	public static class Query {
		public String name;
		public short dns_type, dns_class;
		public Query(String n, short t, short c) { name = n; dns_type = t; dns_class = c; }
	}
	
	/**
	 * Contains DNS Answer information
	 */
	public static class Answer {
		public String name;
		public short dns_type, dns_class;
		public int timeout;
		public byte[] data;
		public Answer(String n, short t, short c, int ttl, byte[] d) { 
			name = n; dns_type = t; dns_class = c; timeout = ttl; data = d;
		}
	}
	
	
	private static class ParsePair {
		int offset;
		String name;
		public ParsePair(String s, int i) { name = s; offset = i; }
	}
	
	private static class InvalidQNameParseException extends Exception {
		public InvalidQNameParseException(String s){ super(s); }
	}

}
