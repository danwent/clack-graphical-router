/**
 * VRICMPCode.java
 *
 * @author Araik Grigoryan
 * @version 1.00 06/07/2002
 * @since 1.00
 */

package net.clackrouter.packets;

/**
 * Encapsulates Internet Control Message Protocol type. Defined in RFC
 * 792.
 */
public class VNSICMPCode
{
  /**
   * Constructs ICMP code.
   *
   * @param name Human-readable name of the code
   * @param value Protocol-dictated value of the code
   *
   * @see VRICMPType
   */
  private VNSICMPCode(String name, byte value)
  {
    m_name = name;
    m_value = value;
  }

  /**
   * Returns the human-readable name of the code.
   *
   * @return Name of the code
   */
  public String toString() { return m_name; }

  /**
   * Returns protocol-defined value of the code.
   *
   * @return Protocol-defined value of the code
   */
  public byte getValue() { return m_value; }

  private byte m_value;
  private String m_name;

  /**
   * ICMP packet codes.
   */
  public static final byte NONE                            = 0;
  public static final byte UNREACH_NET                     = 0;
  public static final byte UNREACH_HOST                    = 1;
  public static final byte UNREACH_PROTOCOL                = 2;
  public static final byte UNREACH_PORT                    = 3;
  public static final byte UNREACH_NEED_FRAG               = 4;
  public static final byte UNREACH_SOURCE_FAILED           = 5;
  public static final byte UNREACH_NET_UNKNOWN             = 6;
  public static final byte UNREACH_HOST_UNKNOWN            = 7;
  public static final byte UNREACH_ISOLATED                = 8;
  public static final byte UNREACH_NET_PROHIBITED          = 9;
  public static final byte UNREACH_HOST_PROHIBITED         = 10;
  public static final byte UNREACH_TOS_NET                 = 11;
  public static final byte UNREACH_TOS_HOST                = 12;
  public static final byte UNREACH_FILTER_PROHIBITED       = 13;
  public static final byte UNREACH_HOST_PRECEDENCE         = 14;
  public static final byte UNREACH_PRECEDENCE_CUTOFF       = 15;
  public static final byte REDIRECT_NET                    = 0;
  public static final byte REDIRECT_HOST                   = 1;
  public static final byte REDIRECT_TOS_NET                = 2;
  public static final byte REDIRECT_TOS_HOST               = 3;
  public static final byte TIME_EXCEEDED_IN_TRANSIT        = 0;
  public static final byte TIME_EXCEEDED_REASSEMBLY        = 1;
  public static final byte PARAMETER_PROBLEM_OP_TABLE_SENT = 1;

  public static final VNSICMPCode None                        =
    new VNSICMPCode("None", NONE);

  public static final VNSICMPCode UnreachNet                  =
    new VNSICMPCode("Unreachable Network", UNREACH_NET);

  public static final VNSICMPCode UnreachHost                 =
    new VNSICMPCode("Unreachable Host", UNREACH_HOST);

  public static final VNSICMPCode UnreachProtocol             =
    new VNSICMPCode("Unreachable Protocol", UNREACH_PROTOCOL);

  public static final VNSICMPCode UnreachPort                 =
    new VNSICMPCode("Unreachable Port", UNREACH_PORT);

  public static final VNSICMPCode UnreachNeedFrag             =
    new VNSICMPCode("Unreachable, Fragmentation Needed", UNREACH_NEED_FRAG);

  public static final VNSICMPCode UnreachSourceFailed         =
    new VNSICMPCode("Unreachable, Source Failed", UNREACH_SOURCE_FAILED);

  public static final VNSICMPCode UnreachNetUnknown           =
    new VNSICMPCode("Unreachable, Unknown Network", UNREACH_NET_UNKNOWN);

  public static final VNSICMPCode UnreachHostUnknown          =
    new VNSICMPCode("Unreachable, Unknown Host", UNREACH_HOST_UNKNOWN);

  public static final VNSICMPCode UnreachIsolated             =
    new VNSICMPCode("Unreachable, Isolated", UNREACH_ISOLATED);

  public static final VNSICMPCode UnreachNetProhibited        =
    new VNSICMPCode("Unreachable, Prohibited Network", UNREACH_NET_PROHIBITED);

  public static final VNSICMPCode UnreachHostProhibited       =
    new VNSICMPCode("Unreachablem Host Prohibited", UNREACH_HOST_PROHIBITED);

  public static final VNSICMPCode UnreachTOSNet               =
    new VNSICMPCode("Unreachable, Network TOS", UNREACH_TOS_NET);

  public static final VNSICMPCode UnreachTOSHost              =
    new VNSICMPCode("Unreachable, Host TOS", UNREACH_TOS_HOST);

  public static final VNSICMPCode UnreachFilterProhibited     =
    new VNSICMPCode("Unreachable, Prohibited Filter", UNREACH_FILTER_PROHIBITED);

  public static final VNSICMPCode UnreachHostPrecedence       =
    new VNSICMPCode("Unreachable, Host Precedence", UNREACH_HOST_PRECEDENCE);

  public static final VNSICMPCode UnreachPrecedenceCutoff     =
    new VNSICMPCode("Unreachable, Precedence Cutoff", UNREACH_PRECEDENCE_CUTOFF);

  public static final VNSICMPCode RedirectNet                 =
    new VNSICMPCode("Redirect Network", REDIRECT_NET);

  public static final VNSICMPCode RedirectHost                =
    new VNSICMPCode("Redirect Host", REDIRECT_HOST);

  public static final VNSICMPCode RedirectTOSNet              =
    new VNSICMPCode("Redirect Network TOS", REDIRECT_TOS_NET);

  public static final VNSICMPCode RedirectTOSHost             =
    new VNSICMPCode("Redirect Host TOS", REDIRECT_TOS_HOST);

  public static final VNSICMPCode TimeExceededInTransit       =
    new VNSICMPCode("Time Exceeded In Transit", TIME_EXCEEDED_IN_TRANSIT);

  public static final VNSICMPCode TimeExceededReassembly      =
    new VNSICMPCode("Time Exceeded, Reassembly", TIME_EXCEEDED_REASSEMBLY);

  public static final VNSICMPCode ParameterProblemOpTableSent =
    new VNSICMPCode("Parameter Problem, Op Table Sent", PARAMETER_PROBLEM_OP_TABLE_SENT);
}
