/**
 * VNSProtocolManager.java
 *
 * @author Araik Grigoryan (modified by Dan Wendlandt and Martin Casado)
 *
 *
 */

package net.clackrouter.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.Vector;

import net.clackrouter.error.ErrorReporter;
import net.clackrouter.packets.VNSPacket;
import net.clackrouter.protocol.commands.VNSAuthReplyProtocolCommand;
import net.clackrouter.protocol.commands.VNSAuthRequestProtocolCommand;
import net.clackrouter.protocol.commands.VNSAuthStatusProtocolCommand;
import net.clackrouter.protocol.commands.VNSBannerProtocolCommand;
import net.clackrouter.protocol.commands.VNSCloseProtocolCommand;
import net.clackrouter.protocol.commands.VNSHWInfoProtocolCommand;
import net.clackrouter.protocol.commands.VNSOpenProtocolCommand;
import net.clackrouter.protocol.commands.VNSPacketProtocolCommand;
import net.clackrouter.protocol.commands.VNSProtocolCommand;
import net.clackrouter.protocol.data.VNSClose;
import net.clackrouter.protocol.data.VNSData;
import net.clackrouter.protocol.data.VNSHWInfo;
import net.clackrouter.router.core.Router;



/**
 * Establishes a connection with VNS server, receives packets and
 * propagates them to the Clack Client. Also receives packets from the Clack
 * Client and sends them to the VNS server.
 */
public class VNSProtocolManager extends Thread
{

    // Server parameters
    private int mSleepTime;
    // use vectors because they are thread-safe
    private Vector incoming_commands;
    private Vector outgoing_commands;

    private int mTopology = -1;
    private String mHostName;
    private String mServer;
    private int mPort = -1;
    private String mUserName;
    private boolean mIsDone = false;
    SocketChannel    mSocket;
    private Router mRouter;
    
    public VNSProtocolManager(String server, int port) throws Exception
    {
        connect(server, port);
        mSleepTime = 20;
    } // -- VNSProtocolManager(String server, int port)

    /**
     * Establish a connection to the VNS server for this protocol manager
     * @param server server to connect to 
     * @param port port to connect to
     * @throws Exception
     */
    public void connect(String server, int port) throws Exception
    {
        if ( isConnected() )
        { return; }

        mServer = server;
        mPort   = port;

        mSocket = SocketChannel.open(
                new InetSocketAddress(server, port ));
        mSocket.configureBlocking(false);

        while (!mSocket.finishConnect())
        { ; }

        incoming_commands = new Vector();
        outgoing_commands = new Vector();
    } // -- connect
    
    /**
     * Main processing loop for the ProtocolManager thread
     * 
     * This is simply a loop in which the ProtocolManager checks if it has been told
     * to stop, then sleeps, then both tries to read a single command from the server
     * and send one command from its outgoing queue.  Note that both incoming and 
     * outgoing queues are accessed asynchronously by the {@link Router}.  
     */
    public void run() 
    {

    	try{
    		
    		while(true){

    			if(mIsDone){break; }
    			
    			try {
    				Thread.sleep(mSleepTime);
    			}catch (Exception e){ break; /*ignore */}
    			
        		if (isConnected() ) {
                    readCommand();
                    sendCommand();
        		}
    		}
            
            if ( isConnected() ) 
            	mSocket.socket().close(); 
  
            
    	} catch (Exception e){
    		e.printStackTrace();
    		System.out.println("Error in protocol manager for router: " + mRouter);
    		if(mRouter != null)
    			ErrorReporter.reportError(mRouter.getDocument().getFramework(), "ProtocolManager Thread Error:");
    		else 
    			ErrorReporter.reportError("ProtocolManager Thread Error: ", e);
    	}
    	System.out.println("Protocol Manager Exiting");   
       
    } // -- run
   
    /**
     * Tests that the protocol manager is still connected to the VNS server
     */
    public boolean isConnected()
    { 
        if (mSocket != null)
        { return mSocket.isConnected(); }
        return false;
    } // -- isConnected()

    /**
     * Signals the protocol manager to exit
     */
    public void disconnectFromVNS(){ mIsDone = true;}

    private void waitForIncomingCommand() throws Exception { 
        while(true) 
        {
        	if(!isConnected()) { 
        		throw new Exception("No longer connected to VNS server"); 
        	}
            Thread.sleep(mSleepTime);
            
            readCommand();
            if(incoming_commands.isEmpty()) {
            	sleep(10);
                continue;
            }
            break; 
        } // end while
    }
    
    /**
     * Loops until it receives the hardware information from the VNS server
     * about this ProtocolManager's host.  
     */
    public VNSHWInfo getHardwareInfo() throws Exception 
    {

    	waitForIncomingCommand(); 

        VNSProtocolCommand command = (VNSProtocolCommand) incoming_commands.remove(0);
        if( command.getType() == VNSPacketProtocolCommand.TYPE_HWINFO) { 		
            return (VNSHWInfo)((VNSHWInfoProtocolCommand) command).getData();
        } else {
        	mSocket.close();
        	throw new Exception("Expected HWInfo message, but got message of type " + command.getType());
        }

    }
    
    public void doAuth(String username, String auth_key) throws Exception {
    	
    	waitForIncomingCommand(); 
    	VNSProtocolCommand command = (VNSProtocolCommand) incoming_commands.remove(0);
        if( command.getType() != VNSPacketProtocolCommand.TYPE_AUTHREQUEST) { 
        	mSocket.close();
        	throw new Exception("Got unexpected command type '" + command.getType() + 
        			" while trying to read an AuthRequest!");
        } 

        ByteBuffer salt = ((VNSAuthRequestProtocolCommand)command).getSalt();
        VNSAuthReplyProtocolCommand reply = new VNSAuthReplyProtocolCommand(username, salt, auth_key);
		outgoing_commands.add(reply);
		sendCommand();
        
    	waitForIncomingCommand(); 
    	command = (VNSProtocolCommand) incoming_commands.remove(0);
        if( command.getType() != VNSPacketProtocolCommand.TYPE_AUTHSTATUS) { 
        	mSocket.close();
        	throw new Exception("Got unexpected command type '" + command.getType() + 
        			" while trying to read an AuthStatus!");
        } 

    }

    /**
     * Sends an OPEN command to the VNS server to initiate a session
     */ 
    public void sendOpenCommand(short topologyID, String virtualRouterID, String username) throws Exception 
    {
		VNSProtocolCommand openCommand =new VNSOpenProtocolCommand(topologyID, virtualRouterID, username);
		outgoing_commands.add(openCommand);
		sendCommand();

        mTopology = topologyID;
        mHostName = virtualRouterID;
        mUserName = username;
    }

    /**
     * Supplies the next data chunk from the VNS server
     */
    public VNSData getData() throws Exception  {
    	
        if(!incoming_commands.isEmpty()) {
        	VNSProtocolCommand  com = (VNSProtocolCommand)incoming_commands.remove(0);
        	return com.getData();
        }

        return null;
    }
    
    /**
     * Performs a test to see if the VNS server is sending us a CLOSE
     * command.  
     */
    public VNSClose checkForVNSCloseCommand() throws Exception {
    	
    	// loop so that we give the server a chance to return a CLOSE command
    	for(int i = 0; i < 10; i++){
    		try {
    			Thread.sleep(50);
    			
    		}catch (Exception e) {
    			e.printStackTrace(); /* do not report */
    		}
    		readCommand();
    	}
    	
    	// look through all received commands, looking for a close
    	for(int i = 0; i < incoming_commands.size(); i++){
        	VNSProtocolCommand  com = (VNSProtocolCommand)incoming_commands.get(i);
        	if(com.getType() == VNSProtocolCommand.TYPE_CLOSE){
        		mSocket.close();
        		return (VNSClose) com.getData();
        	}
    	}
    	return null;
    }

    /**
     * Send data to the VNS server (currently supports only Packet data)
     */
    public void sendData(VNSData data) throws Exception {
        if(data == null ) return;
        if(data instanceof VNSPacket) {
            VNSPacketProtocolCommand send_packet_com = new VNSPacketProtocolCommand( (VNSPacket) data);
            outgoing_commands.add(send_packet_com);
        }
    }
    

    /**
     * Write a single chunk of data to the VNS server, if one exists
     * @throws IOException
     */
    private void sendCommand() throws IOException
    {
    	if(outgoing_commands.isEmpty())  { return; }
    	
    	VNSProtocolCommand command = (VNSProtocolCommand) outgoing_commands.remove(0);
        ByteBuffer commandBuffer = command.getByteBuffer();

        commandBuffer.rewind();
        int numberOfBytesWritten = mSocket.write(commandBuffer);
    }

    /**
     * reads a single command from the VNS server, storing it in the command queue if one exists
     * @throws Exception
     */
    private void readCommand() throws Exception
    {
        VNSProtocolCommand readCommand;
        
        int commandLength = readCommandLength();
        if(commandLength <= 0){return;  }
        
        int commandType = readCommandType(commandLength);
        
        ByteBuffer commandBuffer = ByteBuffer.allocate(commandLength);

       // Put the length and type into the buffer
       commandBuffer.putInt(commandLength);
       commandBuffer.putInt(commandType);


       // Read the command 
       int numberOfBytesRead = 0;
       while( numberOfBytesRead < (commandLength - 
                VNSProtocolCommand.LENGTH_LEN - 
                 VNSProtocolCommand.TYPE_LEN) ) {
                    numberOfBytesRead += mSocket.read(commandBuffer);
                    Thread.sleep(mSleepTime);
       }


       // Subtype the command to a specific type
            switch(commandType) 
            {

                case VNSProtocolCommand.TYPE_CLOSE:
                    readCommand = new VNSCloseProtocolCommand(commandBuffer);
                	System.err.println("Received VNS Close command: " + ((VNSCloseProtocolCommand)readCommand).getData().toString());
                    break;

                case VNSProtocolCommand.TYPE_PACKET:
                    readCommand = new VNSPacketProtocolCommand(commandBuffer);
                    break;

                case VNSProtocolCommand.TYPE_BANNER:
                    readCommand = new VNSBannerProtocolCommand(commandBuffer);
                    break;

                case VNSProtocolCommand.TYPE_HWINFO:
                    readCommand = new VNSHWInfoProtocolCommand(commandBuffer);
                    break;
                    
                case VNSProtocolCommand.TYPE_AUTHREQUEST:
                    readCommand = new VNSAuthRequestProtocolCommand(commandBuffer);
                    break;
                    
                case VNSProtocolCommand.TYPE_AUTHSTATUS:
                    readCommand = new VNSAuthStatusProtocolCommand(commandBuffer);
                    break;

                default:
                	System.out.println("unknown command type: " + commandType); 
                    throw new Exception("Unknown command type '" + commandType);
            }
        

        if( readCommand == null )
        { System.err.println("Read command is null"); }
        else
        { incoming_commands.add(readCommand); }

    } // -- readcommand()
    
    private int readCommandLength() throws Exception {
        // Read in enough bytes to see how long the command is
        ByteBuffer commandLengthBuffer = ByteBuffer.allocate(VNSProtocolCommand.LENGTH_LEN);
            int read_len = 0;

            while ( read_len < VNSProtocolCommand.LENGTH_LEN )
            {
                read_len += mSocket.read(commandLengthBuffer);
                if(read_len <= 0)
                {  return read_len; }
            }

            commandLengthBuffer.rewind();

            // Convert command length bytes into an integer
            return commandLengthBuffer.getInt();
    }
    
    private int readCommandType(int commandLength) throws Exception {
        ByteBuffer commandTypeBuffer   = ByteBuffer.allocate(VNSProtocolCommand.TYPE_LEN);

        // Read command type bytes (non-blocking, so we can check if user
        // stopped the thread)
 
            int bytes_read = 0;
            while( bytes_read < VNSProtocolCommand.TYPE_LEN ) 
            {
                bytes_read += mSocket.read(commandTypeBuffer);

                Thread.sleep(mSleepTime);
            }

           
        	int commandType = VNSProtocolCommand.TYPE_INVALID;

            commandTypeBuffer.rewind();
            // Convert command tyoe bytes into an integer
            commandType = commandTypeBuffer.getInt();

            // Perform a sanity check on the command type
            switch(commandType) {	  
                case VNSProtocolCommand.TYPE_CLOSE:
                 //   System.out.println(" Received CLOSE command ");
                    if( commandLength > VNSCloseProtocolCommand.MAX_LEN + 
                            VNSProtocolCommand.LENGTH_LEN +
                            VNSProtocolCommand.TYPE_LEN ) {
                        System.err.println("Bad CLOSE command: length = " + commandLength +
                                " type = " + commandType);
                    }
                  //  mSocket.close();  dw: bug fix.  this makes it impossible to READ the close command
                    break;
                case VNSProtocolCommand.TYPE_PACKET:
               // 	System.out.println(" Received PACKET command ");
                    if( commandLength > VNSPacketProtocolCommand.MAX_LEN + 
                            VNSProtocolCommand.LENGTH_LEN +
                            VNSProtocolCommand.TYPE_LEN ) {
                        throw new Exception("Bad PACKET command: length = " + commandLength +
                                " type = " + commandType);
                    }
                    break;
                case VNSProtocolCommand.TYPE_BANNER:
               // 	System.out.println(" Received BANNER command ");
                    if( commandLength > VNSBannerProtocolCommand.MAX_LEN + 
                            VNSProtocolCommand.LENGTH_LEN +
                            VNSProtocolCommand.TYPE_LEN ) {
                        throw new Exception("Bad BANNER command: length = " + commandLength +
                                " type = " + commandType);
                    }
                    break;
                  
                case VNSProtocolCommand.TYPE_HWINFO:
               	//	System.out.println(" Received HWINFO command ");
                    break;
                case VNSProtocolCommand.TYPE_AUTHREQUEST:
                //	System.out.println(" Received AUTHREQUEST command ");
                    break;
                case VNSProtocolCommand.TYPE_AUTHSTATUS:
                //	System.out.println(" Received AUTHSTATUS command ");
                    break;
                default:                	
                    throw new Exception ("Bad UNKNOWN command: length = " + commandLength +
                            " type = " + commandType);
            }
            return commandType;
    }

    public void setRouter(Router r) { mRouter = r; }
    public int getTopology() { return mTopology; }
    public String getHostName(){ return mHostName; }
    public int getPort() { return mPort; }
    public String getServer() { return mServer; }
}
