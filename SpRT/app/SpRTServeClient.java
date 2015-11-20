/*Austin Anderson
 * SpRTServeClient.java 
 */
package SpRT.app;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SpRTServeClient implements Runnable {
	private static final String TIMELIMIT="50000";// timeout in miliseconds
	private static final String TIMELIMITPROP="Timelimit";

	private static int timelimit;
	private Socket clntSock;
    SpRTServeClient(Socket clntSock){
    	this.clntSock=clntSock;
    	//get the system time limit
    	timelimit=Integer.parseInt(System.getProperty(TIMELIMITPROP, TIMELIMIT));
    }
    
    /**
     * run the thread to service a client 
     */
	@Override
	public void run() {
		SpRTStateContext advance=null;
		SpRTRequest fromClient=null;
		SpRTResponse toSend=null;
		OutputStream out=null;
		try {
			toSend=new SpRTResponse("OK","NULL","dummy msg",new CookieList());
			InputStream in=clntSock.getInputStream();
			out=clntSock.getOutputStream();
			clntSock.setSoTimeout(timelimit);
			long startTime=System.currentTimeMillis();
			fromClient=new SpRTRequest(in);
			int timeSpent=(int) (System.currentTimeMillis()-startTime);
			advance=new SpRTStateContext(fromClient,toSend);
			advance.setNextResponse(fromClient,toSend);
			toSend.encode(out);
			while(timeSpent<=timelimit
					&&!"NULL".equals(toSend.getFunction())){
				timeSpent =timelimit;
				clntSock.setSoTimeout(timelimit);//reset the timer
			    startTime=System.currentTimeMillis();//log the current time
				fromClient=new SpRTRequest(in);
				timeSpent=(int) (System.currentTimeMillis()-startTime);//get the time elapsed since the request was read
				advance.setNextResponse(fromClient,toSend);
				toSend.encode(out);
			}
			
		} 
		catch (IllegalArgumentException bad) {
			ErrorLog.logErr(fromClient,"Unexpected function: "+bad.getMessage(),true);
		}
		catch(SpRTException bad){
			try {//try one last ditch effort to send an error to the client
				toSend.setMessage("Error in transmission "+bad.getMessage());
				toSend.setStatus("ERROR");
				toSend.setFunction("NULL");
				toSend.encode(out);
				clntSock.close();
			} 
			catch (SpRTException e) {
				ErrorLog.logErr(toSend, "Unable to send client error message",true);
			}
			catch(IOException e){
				ErrorLog.logErr(toSend, "Unable to close client socket",false);
			}
			ErrorLog.logErr(fromClient, "Communication problem: "+bad.getMessage(),true);
		}
		catch (IOException e) {
			ErrorLog.dispErr("Unspecified error: "+e.getMessage());
		}
		finally{
			try {
				if(clntSock!=null){
					clntSock.close();
				}
			} 
			catch (IOException e) {
				ErrorLog.dispErr("Unable to close client socket");
			}
		}
	}
}
