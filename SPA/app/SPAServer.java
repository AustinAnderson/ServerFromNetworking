/* SPAServer.java
 * Author: Austin Anderson
 * Date Last modified: 3/25/15
 */
package SPA.app;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAResponse;
import SpRT.app.ErrorLog;

public class SPAServer implements Runnable {
	public static final SPAServer instance=new SPAServer();
	                                   //(2^16-1)-(ip header size)-(udp header size)
	private static final int MAXUDPSIZE=(((1<<16)-1)-20)-8;
	
	private static SPAResponse response=null;//this is not to be modified after it is set by setupSPAServer
	private static int port;
	private volatile static boolean done=false;
	/**
	 * enforce spaserver being single threaded by making it a singleton.
	 * this way, spaserver's response won't enter a race condition
	 */
	private SPAServer(){
	}
	/**
	 * allow the sprtServer to signify this server to end its loop and terminate
	 */
	public static void kill(){
		done=true;
	}
	/** return the instance of spaServer
	 * and set it's values if this is the first time it is used
	 * 
	 * @param r the response to use
	 * @param port the port to send to 
	 */
    public static SPAServer setupSPAServer(SPAResponse r,int port){
    	if(response==null){
    		SPAServer.port=port;
    		response=r;
    	}
    	return instance;
    }
    /** a function to update an app supported by the server
     * synchronized so outside threads can update without messing each other up
     * 
     * @param which the index of the app which to update
     * @throws SPAException if there is an error updating
     */
    synchronized public static void useApp(int which) {
    	try{
    		if(response==null){
    			throw new SPAException("Cannot use Server, server not set up",SPAException.SYS_ERR);
    		}
    		response.use(which);
    	}
    	catch(SPAException bad){
    		ErrorLog.logErr(null,response,"Error updating use counts: "+bad.getMessage(),false);
    	}
    }
    /** sends a response udp packet
     * 
     * @param sendThrough the socket to send through
     * @param toSend the SPAResponse to encode
     * @param iaddr the ip address to send to
     * @param returnPort the port to send to
     * @throws SPAException if there is an error encoding
     * @throws IOException if there is an error sending the encoded packet
     */
	private static void sendResponse(DatagramSocket sendThrough, SPAResponse toSend,InetAddress iaddr,int returnPort) throws SPAException, IOException{
		byte[] bytesToSend=toSend.encode();
		DatagramPacket packet=new DatagramPacket(bytesToSend,bytesToSend.length);
		if(iaddr==null){
			throw new SPAException("can't send response: packet destination unknown",SPAException.SYS_ERR);
		}
		packet.setAddress(iaddr);
		packet.setPort(returnPort);
		sendThrough.send(packet);
	}
	/**
	 * run the SPAServer
	 */
	@Override
	public void run() {
		DatagramSocket socket=null;
		int remotePort=0;
		InetAddress iaddr=null;
		try {
			socket=new DatagramSocket(port);
			DatagramPacket packet=new DatagramPacket(new byte[MAXUDPSIZE],MAXUDPSIZE);
			while(!done){
				socket.receive(packet);
				remotePort=packet.getPort();
				iaddr=packet.getAddress();
				SPAMessage m=null;
				try{
					byte[] b=packet.getData();
					b=Arrays.copyOfRange(b,0,packet.getLength());
					m=SPAMessage.decode(b);
					if(!m.isQuery()){
						throw new SPAException("Server received spa response",SPAException.WRONG_MESSAGE_TYPE);
					}
					if(m.getError()==SPAException.SYS_ERR){
						throw new SPAException("Query has system error",SPAException.SYS_ERR);
					}
					ErrorLog.logInf(iaddr.toString()+":"+remotePort, m);
				}
				catch(SPAException bad){
					SPAResponse notifyOfError=new SPAResponse();
					notifyOfError.setError(bad.getErrCode());
					ErrorLog.logErr(iaddr.toString()+":"+remotePort, m, bad.getMessage(),false);
					ErrorLog.logInf(iaddr.toString()+":"+port, notifyOfError);
					sendResponse(socket,notifyOfError,iaddr,remotePort);
					continue;//skip to next iteration
				}
				response.setID(m.getID());
				sendResponse(socket,response,iaddr,remotePort);
				ErrorLog.logInf(iaddr.toString()+":"+port, response);
			}
		} catch (SocketException e) {//from creating socket
			ErrorLog.dispErr("Unable to start: error creating socket: "+e.getMessage());
		}catch(IOException bad){//from recieve(packet)
			ErrorLog.logErr(iaddr.toString()+":"+remotePort, null, "Error recieving packet: "+bad.getMessage(), true);
		}catch(SPAException bad){//from all spa stuff
			ErrorLog.logErr(iaddr.toString()+":"+remotePort, null, "Error creating response: "+bad.getMessage(), true);
		}
		finally{
			if(socket!=null){
				socket.close();
			}
		}
	}
}
