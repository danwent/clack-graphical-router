/**
 * VRICMPType.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;

/**
 * Encapsulates Internet Control Message Protocol type. Defined in RFC 792.
 */
public class VNSICMPType
{
  /**
   * Constructs ICMP type.
   *
   * @param name Human-readable name of the type
   * @param value Protocol-dictated value of the type
   *
   * @see VRICMPCode
   */
  private VNSICMPType(String name, byte value)
  {
    m_name = name;
    m_value = value;
  }

  /**
   * Returns the human-readable name of the type.
   *
   * @return Name of the code
   */
  public String toString() { return m_name; }

  /**
   * Returns protocol-defined value of the type.
   *
   * @return Protocol-defined value of the type
   */
  public byte getValue() { return m_value; }

  /**
   * Convert a byte version of the type to its object version.
   *
   * @param type Type of the ICMP packet
   * @return Type of the ICMP packet
   */
  public static VNSICMPType convert(byte type)
  {
    VNSICMPType typeObject = null;

    switch(type) {
    case VNSICMPType.ECHO_REPLY:
      typeObject = VNSICMPType.EchoReply;
      break;
    case VNSICMPType.UNREACH:
      typeObject = VNSICMPType.Unreach;
      break;
    case VNSICMPType.SOURCE_QUENCH:
      typeObject = VNSICMPType.SourceQuench;
      break;
    case VNSICMPType.REDIRECT:
      typeObject = VNSICMPType.Redirect;
      break;
    case VNSICMPType.ECHO_REQUEST:
      typeObject = VNSICMPType.EchoRequest;
      break;
    case VNSICMPType.ROUTER_ADVERT:
      typeObject = VNSICMPType.RouterAdvert;
      break;
    case VNSICMPType.ROUTER_SOLICIT:
      typeObject = VNSICMPType.RouterSolicit;
      break;
    case VNSICMPType.TIME_EXCEEDED:
      typeObject = VNSICMPType.TimeExceeded;
      break;
    case VNSICMPType.PARAMETER_PROBLEM:
      typeObject = VNSICMPType.ParameterProblem;
      break;
    case VNSICMPType.TIME_STAMP:
      typeObject = VNSICMPType.TimeStamp;
      break;
    case VNSICMPType.TIME_STAMP_REPLY:
      typeObject = VNSICMPType.TimeStampReply;
      break;
    case VNSICMPType.INFO_REQUEST:
      typeObject = VNSICMPType.InfoRequest;
      break;
    case VNSICMPType.INFO_REQUEST_REPLY:
      typeObject = VNSICMPType.InfoRequestReply;
      break;
    case VNSICMPType.MASK_REQUEST:
      typeObject = VNSICMPType.MaskRequest;
      break;
    case VNSICMPType.MASK_REQUEST_REPLY:
      typeObject = VNSICMPType.MaskRequestReply;
      break;
    }

    return typeObject;
  }

  private byte m_value;
  private String m_name;

  /**
   * ICMP message types.
   */
  public static final byte ECHO_REPLY         = 0;
  public static final byte UNREACH            = 3;
  public static final byte SOURCE_QUENCH      = 4;
  public static final byte REDIRECT           = 5;
  public static final byte ECHO_REQUEST       = 8;
  public static final byte ROUTER_ADVERT      = 9;
  public static final byte ROUTER_SOLICIT     = 10;
  public static final byte TIME_EXCEEDED      = 11;
  public static final byte PARAMETER_PROBLEM  = 12;
  public static final byte TIME_STAMP         = 13;
  public static final byte TIME_STAMP_REPLY   = 14;
  public static final byte INFO_REQUEST       = 15;
  public static final byte INFO_REQUEST_REPLY = 16;
  public static final byte MASK_REQUEST       = 17;
  public static final byte MASK_REQUEST_REPLY = 18;

  public static final VNSICMPType EchoReply        = 
    new VNSICMPType("Echo Reply", ECHO_REPLY);

  public static final VNSICMPType Unreach          =
    new VNSICMPType("Unreachable", UNREACH);

  public static final VNSICMPType SourceQuench     =
    new VNSICMPType("Source Quench", SOURCE_QUENCH);

  public static final VNSICMPType Redirect         =
    new VNSICMPType("Redirect", REDIRECT);

  public static final VNSICMPType EchoRequest     =
    new VNSICMPType("Echo Request", ECHO_REQUEST);

  public static final VNSICMPType RouterAdvert     =
    new VNSICMPType("Router Advertisement", ROUTER_ADVERT);

  public static final VNSICMPType RouterSolicit    =
    new VNSICMPType("Router Solicitation", ROUTER_SOLICIT);

  public static final VNSICMPType TimeExceeded     =
    new VNSICMPType("Time Exceeded", TIME_EXCEEDED);

  public static final VNSICMPType ParameterProblem =
    new VNSICMPType("Parameter Problem", PARAMETER_PROBLEM);

  public static final VNSICMPType TimeStamp        =
    new VNSICMPType("Time Stamp", TIME_STAMP);

  public static final VNSICMPType TimeStampReply   =
    new VNSICMPType("Time Stamp Reply", TIME_STAMP_REPLY);

  public static final VNSICMPType InfoRequest      =
    new VNSICMPType("Information Request", INFO_REQUEST);

  public static final VNSICMPType InfoRequestReply =
    new VNSICMPType("Information Request Reply", INFO_REQUEST_REPLY);

  public static final VNSICMPType MaskRequest      =
    new VNSICMPType("Mask Request", MASK_REQUEST);

  public static final VNSICMPType MaskRequestReply =
    new VNSICMPType("MaskRequestReply", MASK_REQUEST_REPLY);
}
