/*Austin Anderson
 * SpRTServer.java 
 * Date last modified: 3/25/15
 */
package SpRT.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import SPA.app.SPAServer;
import SPA.protocol.SPAException;
import SPA.protocol.SPAResponse;


/** Actual server. Runs a thread pool
 *  that handles clients adapted from sockets textbook
 *
 */
public class SpRTServer {
	
	public static final String[] supportedApplications={"Poll","MathQuestion"};
	public static final int POLL=0;
	public static final int MATHQUESTION=1;
	public static void main(String[] args){
		Thread SPA_Server=null;
		ServerSocket servSock=null;
		Socket clntSock = null;
		try{
			if(args.length!=2){
				throw new IllegalArgumentException();
			}
			int servPort = Integer.parseInt(args[0]);
			SPAResponse response=new SPAResponse(supportedApplications,new int[]{0,0}, (byte)0);
			SPA_Server=new Thread(SPAServer.setupSPAServer(response,servPort));
			SPA_Server.start();
			int numThreads= Integer.parseInt(args[1]);
			servSock=new ServerSocket(servPort);//throws ioerro
			servSock.setReuseAddress(true);
			String remoteAddr=null;
			Executor service= Executors.newFixedThreadPool(numThreads);
			while(true){
				clntSock = servSock.accept();
				remoteAddr=clntSock.getRemoteSocketAddress().toString();
				ErrorLog.setAddressPort(remoteAddr);
				service.execute(new SpRTServeClient(clntSock));
			}
			
		}
	    catch(IllegalArgumentException bad){
			ErrorLog.dispErr("Unable to Start: Parameters <port> <number of Threads> must be integers");
		}
		catch(IOException bad){
			ErrorLog.dispErr("Unable to Start: Unable to establish connection ");
		}
		catch(SPAException bad){
			ErrorLog.dispErr("Unable to Start SPAServer: "+bad.getMessage());
		}
		finally{
			try {
				if(servSock!=null){
					servSock.close();
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
			} catch (InterruptedException e) {
				ErrorLog.dispErr("Unable to shutdown SPAServer");
			}
		}
	}
}
