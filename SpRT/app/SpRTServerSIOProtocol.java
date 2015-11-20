/* SpRTServerSIOProtocol.java
 * Austin Anderson
 * 4/13/2015
 */
package SpRT.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTResponse;


public class SpRTServerSIOProtocol implements TCPProtocol {
	
	/**
	 * accepts a connection to a client
	 * @param key the selection key to accept to
	 */
	@Override
	public void handleAccept(SelectionKey key) {
		try{
			SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
			clntChan.configureBlocking(false);//must be non blocking to register
			//register the selector with the new channel for read and attach byte buffer
			clntChan.register(key.selector(),SelectionKey.OP_READ, new SpRTKeyAttatchment());
		}
		catch(IOException bad){
			ErrorLog.logErr(null,"Error Accepting Connection",true);
		}
	}

	/**
	 * reads in the client's data and updates the running SpRTResponse
	 * @param key the selection key to read from
	 */
	@Override
	public void handleRead(SelectionKey key) {
		//client socket channel has pending data
		SocketChannel clntChan = (SocketChannel) key.channel();
		try{
			int bytesRead=((SpRTKeyAttatchment) key.attachment()).updateRecievedResponse(clntChan);
			if(bytesRead == -1){//did the other end close
				clntChan.close();
			}
			else{
				if(bytesRead!=0){//0 denotes nothing or an incomplete message
					//Indicate via key that writing is of interest now
					key.interestOps(SelectionKey.OP_WRITE);
					ErrorLog.logInf(((SpRTKeyAttatchment)key.attachment()).getReq());
				}
			}
		}
		catch(IOException bad){
			ErrorLog.logErr(null, "Unable to shut down: cannot close socket",true);
		}
		catch(SpRTException bad){
			attemptToInformUser(bad,key);
			ErrorLog.logErr(((SpRTKeyAttatchment)key.attachment()).getReq(), bad.getMessage(), false);
		}
	}
	/**
	 * writes out the current SpRTResponse to the client
	 * @param key the selection key to read from
	 */
	@Override
	public void handleWrite(SelectionKey key) {
		try{
			//channel is available for writing and key is valid 
			//(i.e. client channel not closed)
		
			SocketChannel clntChan = (SocketChannel) key.channel();
			boolean done=((SpRTKeyAttatchment) key.attachment()).encodeNextMessage(clntChan);
			if(done){//sent null as function?
				key.interestOps(0);//set no ops of interest
				clntChan.close();//kill the connection with finished client
			}
			else{
				key.interestOps(SelectionKey.OP_READ);
			}
			ErrorLog.logInf(((SpRTKeyAttatchment)key.attachment()).getResp());
		}
		catch(SpRTException bad){
				bad.printStackTrace();
			attemptToInformUser(bad,key);
			ErrorLog.logErr(((SpRTKeyAttatchment)key.attachment()).getResp(), bad.getMessage(), false);
		} catch (IOException e) {
			ErrorLog.dispErr("unale to close connection");
		}
		
	}
	/** utility function to notify clients of a server side error
	 * 
	 * @param bad the exception to tell the client about
	 * @param key the key for which client to send to
	 */
	private void attemptToInformUser(Exception bad,SelectionKey key){
		SocketChannel clntSock=(SocketChannel) key.channel();
		SpRTResponse toSend=null;
		try {//try one last ditch effort to send an error to the client
			toSend=new SpRTResponse("OK","NULL","dummy msg",new CookieList());
			toSend.setMessage("Error in transmission "+bad.getMessage());
			toSend.setStatus("ERROR");
			toSend.setFunction("NULL");
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			toSend.encode(out);
			byte[] data= out.toByteArray();
			ByteBuffer b=ByteBuffer.allocate(data.length);
			b.put(data);
			clntSock.write(b);
			clntSock.close();
		} 
		catch (SpRTException e) {
			ErrorLog.logErr(toSend, "Unable to send client error message",true);
		}
		catch(IOException e){
			ErrorLog.logErr(toSend, "Unable to close client socket",false);
		}
	}

}
