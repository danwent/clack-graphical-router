/**
 * VRProtocolCharCoder.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.protocol;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Transforms arbitrary strings into a buffer of bytes and vice versa
 * using US ASCII encoding and decoding.
 */
public class VNSProtocolCharCoder
{
  /** The US ASCII character set of the coder */
  public static Charset charset = Charset.forName("US-ASCII"); 

  /** Encoder of the US ASCII strings */
  public static CharsetEncoder encoder = charset.newEncoder();

  /** Decoder of the US ASCII byte buffers */
  public static CharsetDecoder decoder = charset.newDecoder();

  private static final byte [] nullByte = {0};

  /**
   * Encodes the supplied message into a byte buffer using US ASCII encoding.
   *
   * @param message Message to be encoded
   * @return Byte buffer containing the encoded message
   */
  public static ByteBuffer encode(String message) throws CharacterCodingException
  {
    CharBuffer messageCharBuffer = CharBuffer.wrap(message);
    return encoder.encode(messageCharBuffer);
  }

  /**
   * Decodes the supplied byte buffer into a string using US ASCII decoding.
   *
   * @param byteBuffer Byte buffer containing the encoded messasge
   * @return Decoded message
   */
  public static String decode(ByteBuffer byteBuffer) throws CharacterCodingException
  {
    return decode(byteBuffer, false); // Do not trim the string
  }

  
  /**
   * Decodes the supplied byte buffer into a string using US ASCII
   * decoding. The returned message is less than or equal to the specified
   * length. 
   *
   * @param byteBuffer Byte buffer containing the encoded messasge
   * @param length Length of the returned message
   * @return Decoded message
   */
  public static String decode(ByteBuffer byteBuffer, int length) throws CharacterCodingException
  {
    String message = decoder.decode(byteBuffer).toString();
    
    if( length < message.length() )
      return message.substring(0, length);
    else
      return message;
  }

  /**
   * Decodes the supplied byte buffer into a string using US ASCII
   * decoding. Remove all the trailing spaces, starting from the first
   * occurence of a space in the string.
   *
   * @param byteBuffer Byte buffer containing the encoded messasge
   * @param trim If true, the message will be trimmed from the right
   * @return Decoded message
   */
  public static String decode(ByteBuffer byteBuffer, boolean trim) throws CharacterCodingException
  {
    String message = decoder.decode(byteBuffer).toString();
    String nullString = decoder.decode(ByteBuffer.wrap(nullByte)).toString();

    if( trim )
      return message.substring(0, message.indexOf(nullString));
    else
      return message;
  }
}
