/* SPAClient.java
 * Author: Austin Anderson
 * Date last modified: 3/25/15
 */
package SPA.app;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import SPA.protocol.SPAMessage;
import SPA.protocol.SPAQuery;


public class SPAClient{
	private static final int TIMEOUT=3000;
	private static final int MAXTRIES=5;//(2^16-1)-(ip header size)-(udp header size)
	private static final int MAXUDPSIZE=(((1<<16)-1)-20)-8;
	public static void main(String[] args){
		DatagramSocket socket=null;
		try{
			if(args.length!=3){//if the wrong number of arguments is given, throw a message.
				throw new IllegalArgumentException("Usage: <Server> <Port> <Business Name>");//note that this is inside the try block:
			}																  //the message is shown to the user before a "graceful" termination
			InetAddress serverAddress=InetAddress.getByName(args[0]);
			SPAQuery toSend=new SPAQuery();
			byte expectedID=toSend.getID();
			toSend.setBusinessName(args[2]);
			int servPort=Integer.parseInt(args[1]);
			socket=new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
			DatagramPacket sendPacket=new DatagramPacket(toSend.encode(),toSend.encode().length,serverAddress,servPort);//make the packet to send out of the given data
			DatagramPacket receivePacket=new DatagramPacket(new byte[MAXUDPSIZE],MAXUDPSIZE);//allocate a packet to hold the response
			int tries=0;
			boolean done=false;
			while(!done){
				boolean receivedResponse=false;
				do{
					socket.send(sendPacket);
					try{
						socket.receive(receivePacket);
						if(!receivePacket.getAddress().equals(serverAddress)){
							throw new IOException("Received packet from unknown source");
						}
						receivedResponse=true;
					}catch(InterruptedIOException e){
						tries++;
						System.out.println("Timed out, tries remaining: "+(MAXTRIES-tries)+"...");
					}
				}while((!receivedResponse)&&(tries<MAXTRIES));
				done=true;
				if(receivedResponse){
					byte[] b=receivePacket.getData();
					b=Arrays.copyOfRange(b, 0, receivePacket.getLength());//trim the data to match what was read
					SPAMessage got=SPAMessage.decode(b);
					if(got.getID()!=expectedID&&got.getError()==0){
						System.err.println("received response to a different Query");
						done=false;
					}
					else{
						if(got.getError()==0){
							System.out.println(got);
						}
						else{
							System.err.println(got);
						}
					}
				}
				else{
					System.out.println("No Response -- giving up.");
				}
			}
		}
		catch(UnknownHostException bad){
			System.err.println("Unknown Host: \""+bad.getMessage()+"\"");
		}
		catch(NumberFormatException bad){
			System.err.println("Value not an Integer: "+bad.getMessage());
		}
		catch(Exception bad){
			System.err.println(bad.getMessage());
		}
		finally{
			if(socket!=null){
				socket.close();
			}
		}
	}
}