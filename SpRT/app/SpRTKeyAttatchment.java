/* SpRTKeyAttatchment.java
 * Austin Anderson
 * 4/13/2015
 */
package SpRT.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

/** this class packages all the necessary information that needs to persist for a connection
 *  so it can by attached to a connection's key.
 *  
 * @author Austin Anderson
 */
public class SpRTKeyAttatchment{
	public static final int BUFFSIZE=65535;//buffer size in bytes (max tcp packet size)
	private static final byte[] END={'\r','\n','\r','\n'};
	private SpRTRequest fromClient;
	private SpRTResponse toSend;
	private SpRTStateContext advance;
	private ByteArrayInputStream iStream;
	private ByteBuffer iBuff;
	/**
	 * constructor sets default values
	 */
	SpRTKeyAttatchment() {
		try{
			//set the attachment's input stream and buffer to reference the same array
			iBuff=ByteBuffer.allocate(BUFFSIZE);
			iStream=new ByteArrayInputStream(iBuff.array());
			
			fromClient=null;//going to be assigned to a new one each update anyway.
			toSend=new SpRTResponse("OK","NULL","dummy msg",new CookieList());
			advance=null;//initial request is not yet known
		}
		catch(SpRTException bad){//shouldn't be able to happen
			System.err.println("dummy message unable to be created");
		}
	}
	/** utility function to test if the current position on an array is the last where
	 *  END can be found
	 *  
	 * @param array the array to check
	 * @param ndx the position to check the preceding elements with
	 * @return if the index is the last of the end.
	 */
	private boolean isAtEnd(byte[] array,int ndx){
		boolean flag=false;
		if(ndx>=3){
			if(array[ndx]==END[3]&&
					array[ndx-1]==END[2]&&
					array[ndx-2]==END[1]&&
					array[ndx-3]==END[0]){
				flag=true;
			}
		}
		return flag;
	}
	/**
	 * utility function to discard the old buffer and allocate a new one of the same size
	 */
	private void deepBufferClear(){
		//set the attachment's input stream and buffer to reference the same array
		iBuff=ByteBuffer.allocate(BUFFSIZE);
		iStream=new ByteArrayInputStream(iBuff.array());
	}
	/**
	 * reads data from the client socket channel and makes a new SprtRequest out of it.
	 * @param clntChan the socketchanel to read from
	 * @return the number of bytes read
	 * @throws SpRTException if an error occurs
	 */
	public int updateRecievedResponse(SocketChannel clntChan) throws SpRTException{
		int bytesRead=-1;
		try{
			bytesRead = clntChan.read(iBuff);
			if(bytesRead>0){
				try{
					byte[] contents=iBuff.array();
					int i=0;
					boolean messageFull=false;
					while(i<contents.length&&!messageFull){
						messageFull=isAtEnd(contents,i);
						i++;
					}
					if(messageFull){
						iStream.reset();//move the stream's read position to the front of the buffer
						fromClient=new SpRTRequest(iStream);
						deepBufferClear();//calling clear just resets the read position, so it would reread the old message
						while(contents[i]!=0){//I found no way to get the number of extra bytes, so I search for the next null
											 //this wont work if the client sends nulls.
							                //but the size of the extra read didn't always correspond to the number of extra bytes.
							iBuff.put(contents[i]);//put the bytes that were read from the next message in the front of the buffer
							//this increments the buffer's position, so the next read will add after these bytes
							i++;
						}
					}
					else{
						bytesRead=0;
					}
				}catch(SpRTException bad){
					if(bad.isTooShort()){
						bytesRead=0;
					}
					else{
						throw new SpRTException("Communication Problem: "+bad.getMessage());
					}
				}
				if(advance==null&&bytesRead>0){
					advance=new SpRTStateContext(fromClient,toSend);
				}
			}
		}
		catch(IOException bad){
			throw new SpRTException("Unable to Read Data",bad);
		}
		return bytesRead;
	}
	/** uses the serverStateContext to modify a SpRTResponse to have the appropriate message,
	 * then sends it through the socketchannel
	 * 
	 * @param clntChan the SocketChannel to write to
	 * @return true if the message sent was the last message, false otherwise
	 * @throws SpRTException
	 */
	public boolean encodeNextMessage(SocketChannel clntChan) throws SpRTException{
		boolean toReturn=false;
		try{
			advance.setNextResponse(fromClient, toSend);
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			toSend.encode(out);
			byte[] data=out.toByteArray();
			ByteBuffer buf=ByteBuffer.allocate(data.length);
			buf.put(data);//put the data in,
			buf.flip();//prepare buffer for writing
			clntChan.write(buf);
			toReturn="NULL".equals(toSend.getFunction());
		}
		catch(IOException bad){
			throw new SpRTException("Unable to Write Data",bad);
		}
		return toReturn;
	}
	/**
	 * @return the response associated with this keyAttachment
	 */
	public SpRTResponse getResp(){
		return toSend;
	}
	/**
	 * @return the request associated with this keyAttachment
	 */
	public SpRTRequest getReq(){
		return fromClient;
	}
}
