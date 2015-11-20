/* SpRTServerSIO.java
 * written by Jeff Donahoo (sockets book)
 * modified by Austin Anderson
 * 4/11/2015
 */
package SpRT.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import SPA.app.SPAServer;
import SPA.protocol.SPAException;
import SPA.protocol.SPAResponse;

public class SpRTServerSIO {
	private static final int TIMEOUT=5000;//wait timeout in milliseconds
	public static final String[] supportedApplications={"Poll","MathQuestion"};
	public static final int POLL=0;
	public static final int MATHQUESTION=1;
	
	public static void main(String[] args) {
		Thread SPA_Server=null;
		ServerSocketChannel listnChannel=null;
		try{//handled gracefully!
			if(args.length!=1){
				throw new IllegalArgumentException("Parameter(s): <Port>");
			}
			//create a selector to multiplex listening sockets and connections
			Selector selector=Selector.open();
			
			//start up the SPAServer on a new thread
			int servPort = Integer.parseInt(args[0]);
			SPAResponse response=new SPAResponse(supportedApplications,new int[]{0,0}, (byte)0);
			SPA_Server=new Thread(SPAServer.setupSPAServer(response,servPort));
			SPA_Server.start();
			
			//create listening socket channel for each port and register selector
			listnChannel=ServerSocketChannel.open();
			listnChannel.socket().bind(new InetSocketAddress(Integer.parseInt(args[0])));
			listnChannel.configureBlocking(false);//must be non blocking to register
			//register selector with channel. the returned key is ignored
			listnChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			//create a handler that will implement the protocol
			TCPProtocol protocol = new SpRTServerSIOProtocol();
			
			while(true){//run forever, processing available I/O operations
				//wait for some channel to be ready or timeout
				if(selector.select(TIMEOUT)==0){
					System.out.print(".");
					continue;
				}
				//get iterator on set of keys with I/O to processes
				Iterator<SelectionKey> keyIter=selector.selectedKeys().iterator();
				while(keyIter.hasNext()){
					SelectionKey key = keyIter.next();// key is bit mask
					//server socket channel has pending connection requests?
					if (key.isAcceptable()){
						protocol.handleAccept(key);
					}
					//client socket channel has pending data?
					if(key.isReadable()&&key.isValid()){
						protocol.handleRead(key);
					}
					//client socket channel is available for writing and
					//key is valid (i.e. channel not closed)?
					if(key.isValid()&&key.isWritable()){
						protocol.handleWrite(key);
					}
					keyIter.remove();//remove from set of selected keys
				}
			}
			
		}
		catch(IllegalArgumentException bad){
			ErrorLog.dispErr("Unable to Start: "+bad.getMessage());
		} 
		catch (IOException e) {
			ErrorLog.dispErr("Unspecified error: "+e.getMessage());
		}
		catch (SPAException e) {
			ErrorLog.dispErr("Unable to Start SPAServer: "+e.getMessage());
		}
		finally{
			try {
				if(listnChannel!=null){
					listnChannel.close();
				}
			}
			catch (IOException e) {
				ErrorLog.dispErr("Unable to shutdown servSocket");
			}
			SPAServer.kill();
			try {
				if(SPA_Server!=null){
					SPA_Server.join();
				}
			} 
			catch (InterruptedException e) {
				ErrorLog.dispErr("Unable to shutdown SPAServer");
			}
		}
	}
}
