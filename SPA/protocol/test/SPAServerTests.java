/* SPAServerTests.java
 * Author: Austin Anderson
 * Date Last modified: 3/25/15
 */
package SPA.protocol.test;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import SPA.protocol.SPAMessage;

public class SPAServerTests {
	private static final int[] good={
		0b0010_0_000,//version_qr_errCode
		83,//MsgID
	 	15,//business name length
		'D','u','m','m','y',' ','B','u','s','s','i','n','e','s','s'//business name
	};
	
	private static final int[] badMessage_version={
		0b0011_0_000,//version_qr_errCode
		83,//MsgID
	 	15,//business name length
		'D','u','m','m','y',' ','B','u','s','s','i','n','e','s','s'//business name
	};
	
	private static final int[] badMessage_badError={
		0b0010_0_111,//version_qr_errCode
		83,//MsgID
	 	15,//business name length
		'D','u','m','m','y',' ','B','u','s','s','i','n','e','s','s'//business name
	};
	
	private static final int[] badLengthShort={
		0b0010_0_000,//version_qr_errCode
		83,//MsgID
	 	19,//business name length
		'D','u','m','m','y',' ','B','u','s','s','i','n','e','s','s'//business name
	};
	
	private static final int[] badLengthLong={
		0b0010_0_000,//version_qr_errCode
		83,//MsgID
	 	10,//business name length
		'D','u','m','m','y',' ','B','u','s','s','i','n','e','s','s'//business name
	};
	private static final int[] sysErr={
		0b0010_0_100,//version_qr_errCode
		83,//MsgID
	 	15,//business name length
		'D','u','m','m','y',' ','B','u','s','s','i','n','e','s','s'//business name
	};
	
	private static final int[] sendResponse={
		0b0010_1_000,//version_qr_errCode
		83,//MsgID
	 	2,3,4,2,//time
		2,//application count
		//entry one
		    0,//app use count[0]
			54,//app use count[1]
			7,//app name length
			'A','p','p',' ','O','n','e',//app name
		//entry two 
		    0,//app use count[0]
			2,//app use count[1]
			7,//app name length
			'A','p','p',' ','T','w','o'//app name
	};
	
	private static byte[] getData(int[] dataAsInt){
		byte[] Qdata=new byte[dataAsInt.length];
		for(int i=0;i<dataAsInt.length;i++){
			Qdata[i]=(byte)dataAsInt[i];
		}
		return Qdata;
	}
	

	private static final int TIMEOUT=3000;
	private static final int MAXTRIES=5;//(2^16-1)-(ip header size)-(udp header size)
	private static final int MAXUDPSIZE=(((1<<16)-1)-20)-8;
	public static void main(String[] args){
		try{
			System.out.println("good should have errCode 0");
			runOnce(args,getData(good));
			Thread.sleep(10);
			System.out.println("badMessage_version should have errCode 1");
			runOnce(args,getData(badMessage_version));
			Thread.sleep(10);
			System.out.println("badMessage_badError should have errCode 1");
			runOnce(args,getData(badMessage_badError));
			Thread.sleep(10);
			System.out.println("badLengthShort should have errCode 2");
			runOnce(args,getData(badLengthShort));
			Thread.sleep(10);
			System.out.println("badLengthLong should have errCode 2");
			runOnce(args,getData(badLengthLong));
			Thread.sleep(10);
			System.out.println("sendResponse should have errCode 3");
			runOnce(args,getData(sendResponse));
			Thread.sleep(10);
			System.out.println("sysErr should have errCode 4");
			runOnce(args,getData(sysErr));
		}
		catch(InterruptedException bad){
			bad.printStackTrace();
		}
	}
	public static void runOnce(String[] args,byte[] data){
		DatagramSocket socket=null;
		try{
			if(args.length!=3){
				throw new IllegalArgumentException("Usage: <Server> <Port> <Business Name>");
			}
			InetAddress serverAddress=InetAddress.getByName(args[0]);
			int servPort=Integer.parseInt(args[1]);
			socket=new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
			int expectedID=83;
			
			DatagramPacket sendPacket=new DatagramPacket(data,data.length,serverAddress,servPort);
			DatagramPacket receivePacket=new DatagramPacket(new byte[MAXUDPSIZE],MAXUDPSIZE);
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
					b=Arrays.copyOfRange(b, 0, receivePacket.getLength());
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

