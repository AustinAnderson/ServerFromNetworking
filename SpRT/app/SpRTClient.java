/* SpRTClient.java
 * Austin Anderson
 * CSI 4321
 * 2/9/2015
 * 
 */
package SpRT.app;

import java.net.Socket;
import java.net.SocketAddress;
import java.io.EOFException;
import java.io.StreamCorruptedException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;
/**
 * 
 * @author Austin Anderson
 * @version 1.0
 */
public class SpRTClient {
	public static void main(String[] args){//init to nulls avoid scoping issues
    	if(args.length!=3){//Test for correct number of args
    		System.out.println("Parameters(s): <Server> <Port> <CookieFilePath>");
    		System.exit(1);
    	}
        Scanner keyboard=new Scanner(System.in);
        String server=args[0];//server name
        String ipAddress=null;
        int servPort=0;
        try{
        	servPort=Integer.parseInt(args[1]);//get port
        }
        catch(NumberFormatException bad){
        	System.err.println("Unable to Start: "+bad.getMessage());
        	System.exit(1);
        }
        String cookieFileName=args[2];
        Map<String,byte[]> cookieLists=null;
        CookieList runningCookies=null;
        CookieList initCookies=new CookieList();
        Socket socket=null;
        InputStream in=null;
        OutputStream out=null;
        FileInputStream file=null;
        try{//unable to start try catch

	    	ObjectInputStream readCookiesFrom=null;
	        try{
	        	file=new FileInputStream(cookieFileName);
	        }
	        catch(FileNotFoundException bad){
	        	
	        	FileOutputStream makeAfile=new FileOutputStream(cookieFileName);
	        	makeAfile.close();
	        	file=new FileInputStream(cookieFileName);
	        }
	        try{//check if cookie file is empty
	        	//if cookie file is empty, the objectInputStream constructor will throw EOFException
		        try{//skipping this try catch block, which handles reading, and doesn't catch EOFException
	        		readCookiesFrom= new ObjectInputStream(file);
		        	 cookieLists=suppressWarnCast(readCookiesFrom.readObject());//unchecked casting unavoidable here because
		        	 //file contents cannot be guaranteed
		        }
		        catch(StreamCorruptedException|ClassNotFoundException bad){
	        		FileOutputStream makeAfile=new FileOutputStream(cookieFileName);
		        	makeAfile.close();//clear the file so error doesn't happen on second execution
		        	throw new SpRTException("unable to read cookie file. Cookie file reset",bad);
		        }
	        }
	        catch(EOFException doNutnWitMeh){//doesn't catch SpRTException, which is handled by general unable to start try catch
	        	cookieLists=new HashMap<String,byte[]>();//if empty cookie file, make the new map since this is the first time storing cookies
	        }
	        //Create socket that is connected to server on specified port
	        socket= new Socket(server,servPort);
	        SocketAddress getIP=socket.getRemoteSocketAddress();
	        ipAddress=getIP.toString();
	        in= socket.getInputStream();
	        out=socket.getOutputStream();
	        int ipStartAt=ipAddress.lastIndexOf('/')+1;//trim out the ip. last index because of sites like www.example.com/heresAslash/127.0.0.1/
	        int ipEndAt=ipAddress.lastIndexOf(':');//remove this and the second parameter
	        ipAddress=ipAddress.substring(ipStartAt,ipEndAt);//here if the ip address needs to include the port for separating cookies
	        byte[] cookieCrisp=cookieLists.get(ipAddress);//get the serialized cookies (get it eh? eh?) from the map, could be null
	        if(cookieCrisp==null){
	        	runningCookies=new CookieList();
	        }
	        else{
	        	runningCookies=new CookieList(cookieCrisp);//recreate cookie list if it's not null
	        }
	        if(readCookiesFrom!=null)readCookiesFrom.close();
	    }
        catch(IOException|SpRTException bad){
        	System.err.println("Unable to Start: "+bad.getMessage());
        	closeAll(keyboard,socket,in,out);
        }
        SpRTResponse getResponse=null;
        SpRTRequest sendRequest=null;
        String userInput=null;
        String[] params={};
        boolean validStartFunction=false;
        System.out.print("Function> ");
        userInput=safeReadLine(keyboard,socket,in,out);
        initCookies.add(runningCookies);//make a copy of runningCookies to give to sendRequest to avoid duplication when
        								//updating running cookies
        while(!validStartFunction){//handle start up
	        try{
	        	sendRequest=new SpRTRequest("RUN",userInput,params,initCookies);
	            validStartFunction=true;
	        }
	        catch(SpRTException bad){
	        	System.err.print("Bad user input: "+bad.getMessage()+". Function> ");
	            userInput=safeReadLine(keyboard,socket,in,out);
	        }
        }
        try{
        	sendRequest.encode(out);
        	getResponse=new SpRTResponse(in);
            runningCookies.add(getResponse.getCookieList());//update cookielist
        }
        catch(SpRTException bad){
        	System.err.println("Communication Problem: "+bad.getMessage());
        	closeAll(keyboard,socket,in,out);
        }
        String currentMessage=getResponse.getMessage();
        while(!"NULL".equals(getResponse.getFunction())){
	        if("ERROR".equals(getResponse.getStatus())){
	        	System.err.print("Error: "+currentMessage);
	        }
	        else{
	        	System.out.print(currentMessage+" ");
	        }
	        userInput=safeReadLine(keyboard,socket,in,out);
        	params=userInput.split(" ");
        	try{
        		sendRequest.setParams(params);
        	}
        	catch(SpRTException bad){
        		System.err.print("Bad user input: "+bad.getMessage()+" "+getResponse.getMessage()+" ");
        		continue;//re do input portion of loop due to bad user input, skip sending message
        	}
	        try{
	        	sendRequest.setFunction(getResponse.getFunction());
	        	                                        //because of ONE erroneous assumption, THE LACK OF 
	        	sendRequest.setCookies(runningCookies);//THIS ONE LINE CAUSED ME TO FAIL THE GUESS TESTS!!!!!
	        	
	        	sendRequest.encode(out);
	        	getResponse=new SpRTResponse(in);//build new SpRTResponse from server's response, overwriting cookies
	        }
	        catch(SpRTException bad){
	        	System.err.println("Communication problem: "+bad.getMessage());
		        runningCookies.add(getResponse.getCookieList());//update old cookies with new prematurely before quit
    	        saveCookies(ipAddress,cookieFileName,cookieLists,runningCookies);
    	    	closeAll(keyboard,socket,in,out);
	        	
	        }
	        runningCookies.add(getResponse.getCookieList());//update old cookies with new
	        currentMessage=getResponse.getMessage();
        } 
        if("ERROR".equals(getResponse.getStatus())){
        	System.err.println("Error: "+currentMessage);
        }
        else{
        	System.out.println(currentMessage);
        }
        saveCookies(ipAddress,cookieFileName,cookieLists,runningCookies);
    	closeAll(keyboard,socket,in,out);
    }
	@SuppressWarnings("unchecked")
	private static Map<String, byte[]> suppressWarnCast(Object readObject) {
		return (Map<String,byte[]>)readObject;
	}
	/** handles reading from a scanner and makes sure there is input
	 *  if there is no input in the buffer then prints an error and quits
	 *  
	 *  this could happen if the user types ^D sending an EOF, or if the input is a
	 *  string piped in that is too short
	 * 
	 * @param keyboard the scanner to guard and read from and close if empty
	 * @param socket Socket to close
	 * @param in InputStream to close
	 * @param out OutputStream to close
	 * @return the scanner's nextLine if it has one
	 */
	private static String safeReadLine(Scanner keyboard,Socket skt,InputStream n, OutputStream out){
		String in=null;
        if(keyboard.hasNextLine()){
        	in=keyboard.nextLine();
        }
        else{
        	System.err.println("Bad user input: no input in buffer");
        	closeAll(keyboard,skt,n,out);
        }
        return in;
	}
	/** attempts to close the four streams opened during program execution
	 * @param keyboard Scanner to close
	 * @param socket Socket to close
	 * @param in InputStream to close
	 * @param out OutputStream to close
	 * @note all streams following the first failed closure will not be closed
	 */
	private static void closeAll(Scanner keyboard,Socket socket,InputStream in, OutputStream out){
		try{
	        if(keyboard!=null)keyboard.close();
	        if(socket!=null)socket.close();//Close the socket and its streams
	        if(out!=null)out.close();
	        if(in!=null)in.close();
		}
		catch(IOException bad){
			System.err.println("Shutdown Problem: "+bad.getMessage());
		}
		System.exit(1);
	}
	/** attempts to save the current cookieLists to the cookie file
	 * 
	 * @param serverIp the current server to save to
	 * @param cookieFile the file path of the file to save to
	 * @param cookieListMap the map holding the cookie lists
	 * @param updateWith the message whose cookie list is to be used to update the map
	 */
	private static void saveCookies(String serverIp,String cookieFile,Map<String,byte[]> cookieListMap, CookieList updateWith){
		try{
			ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(cookieFile));
			cookieListMap.put(serverIp, cookieListToByteArray(updateWith));
			out.writeObject(cookieListMap);
			out.close();
		}
		catch(IOException|SpRTException bad){
			System.err.println("Shutdown Problem: "+bad.getMessage());
		}
	}
	/** converts a cookieList to a byte Array to aid with serialization when
	 *  the cookieList cannot directly be encoded to an outputstream
	 * @param ck the cookie list to convert
	 * @return a byte array containing the cookieList's data
	 * @throws SpRTException if an error occurs writing
	 */
	private static byte[] cookieListToByteArray(CookieList ck) throws SpRTException{
		ByteArrayOutputStream writeTo=new ByteArrayOutputStream();//used because cookieList has serialization methods, but
		ck.encode(writeTo);                                      //is not serializable as a class.
		return writeTo.toByteArray();
	}
}
