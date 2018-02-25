package net.clackrouter.protocol.commands;


public class VNSInvalidProtocolCommandException extends Exception
{
  public VNSInvalidProtocolCommandException()
  {
    super();
  }
  
  public VNSInvalidProtocolCommandException(String message)
  {
    super(message);
  }
}
