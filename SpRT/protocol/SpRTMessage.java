/* SpRTMessage.java
 * Austin Anderson
 * CSI 4321
 * 2/9/2015
 * 
 */
package SpRT.protocol;
import java.io.*;//IOException,InputStream,OutputStream,PrintStream
import java.util.Arrays;
import java.util.Scanner;

/**represents a generic portion of a SpRT message and provides serialization/deserialization
 * 
 * @author Austin Anderson
 * @version 1.0
 */
public abstract class SpRTMessage {
	protected CookieList cookies;
	protected String function;
	protected final String END="\r\n\r\n";
	protected final String ENCODING="US-ASCII";
	protected final String MAGIC="SpRT/1.0 ";
	/** constructs SpRTMessage using given values
	 * 
	 * @param function  function
	 * @param cookies   cookie list
	 * @throws SpRTException   if error with given values 
	 * @throws NullPointerException  if null parameter
	 */
	public SpRTMessage(final String function,final CookieList cookies) throws SpRTException{
		setFunction(function);
		setCookies(cookies);
	}
	/** Constructs SpRTMessage using deserialization
	 * 
	 * @param in   input stream to read from
	 * @throws SpRTException if decoding fails
	 */
	public SpRTMessage(final InputStream in) throws SpRTException{
		//function and CookieList come after status/command which requires
		//knowledge of whether the sub class is a request or response,
		//so it made more sense to me to have those classes parse the entire SpRT-Message
		cookies=null;
		function=null;
	}
	/** adds data from the inputstream to a string until the data matches
	 *  the END character sequence
	 * @param in inputstream to use
	 * @return String made from raw message data
	 * @throws SpRTException if invalid encoding or a problem is encountered with reading
	 */
	protected String readRawMessage(final InputStream in) throws SpRTException{
		try{
			boolean done=false;
			int currentNdx=-1;//initialized to -1 instead of 0 to account for pre-increment
			int bufSize=255;
			byte[] buf=new byte[bufSize];
			byte[] currentByte=new byte[1];
			byte[] nd=END.getBytes(ENCODING);
	        while(!done){
				if(in.read(currentByte,0,1)==-1){
					done=true;
				}
	        	else{//read in a byte, and if successful, add it to buf
	        		currentNdx++;//if buf is to small, change it to fit
	        		if(currentNdx>=bufSize){
	        			bufSize=currentNdx+1;
	        			buf=Arrays.copyOf(buf, bufSize);
	        		}
	        		buf[currentNdx]=currentByte[0];
	        	}	        	
				if(currentNdx>=3&&buf[currentNdx]==nd[3]&&buf[currentNdx-1]==nd[2]
	        			&&buf[currentNdx-2]==nd[1]&&buf[currentNdx-3]==nd[0]){
	        		done=true;//stop looping if the last 4 bytes of buf match
	        		          //the last four of END, which contains "\r\n\r\n"
	        	}
	        }
	        if(currentNdx==-1){
	        	throw new SpRTException("InputStream empty");
	        }
	        buf=Arrays.copyOf(buf, currentNdx+1);//trim the array
	        return new String(buf,ENCODING);
		}
		catch(IOException bad){
			throw new SpRTException(bad.getMessage(),bad);
		}
	}
	/** reads and validates the common part of a message, sets the cookielist, then
	 *  returns an array of strings for the uncommon parts, delimited by a space.
	 * 
	 * @param parseWith the string form of the raw message data
	 * @return a String array containing the strings between the magic string and the cookieList
	 * @throws SpRTException if ENCODING is invalid
	 */
	protected String[] parseAndValidateGeneral(String parseWith) throws SpRTException{
        int cookiesStartAt=parseWith.indexOf('\n');
        if(cookiesStartAt==-1){//true to indicate cause was message is too short
        	throw new SpRTException("expected \\n and then CookieList",true);
        }
        if(!parseWith.contains(MAGIC)){
        	throw new SpRTException("expected \""+MAGIC+"\" to begin message");
        }
        String rqstOrRpnse=null;
        rqstOrRpnse=parseWith.substring(MAGIC.length(),cookiesStartAt).trim();//get the part of the message from the magic sting to the cookies
        String mahCookies=(parseWith.substring(cookiesStartAt+1));
        try{
        	setCookies(new CookieList(new ByteArrayInputStream(mahCookies.getBytes(ENCODING))));
        }
        catch(UnsupportedEncodingException bad){
        	throw new SpRTException("Ecoding Invalid: "+ENCODING,bad);
        }
    	return rqstOrRpnse.split(" ");
	}
	public SpRTMessage(Scanner in, PrintStream out)throws SpRTException{
		//do not need to implement yet
	}
	public static SpRTMessage decode(Scanner in, PrintStream out)throws SpRTException{
		return null;
		//do not need to implement yet
	}
	/**Encode the SpRT message
	 * 
	 * @param out   serialization output sink
	 * @throws SpRTException   if io problem
	 * @throws NullPointerException   if out is null
	 */
	public void encode(OutputStream out)throws SpRTException{
		//overridden by children, but definition for superclass needs to be here
		//for the children to override
	}
	/** return function
	 * 
	 * @return function
	 */
	public String getFunction(){
		return function;
	}
	/** set function
	 * 
	 * @param function   new function
	 * @throws SpRTException   if invalid command
	 * @throws NullPointerException   if null command
	 */
	public void setFunction(final String function)throws SpRTException{
		if(!CookieList.isValidToken(function)){
			throw new SpRTException("function invalid");
		}
		this.function=function;
	}
	/** return message cookie list
	 * 
	 * @return cookie list
	 */
	public CookieList getCookieList(){
		return cookies;
	}
	/** set cookie list
	 * 
	 * @param cookies   cookie list
	 * @throws NullPointerException   if null command
	 */
	public void setCookies(final CookieList cookies){
		if(cookies==null){
			throw new NullPointerException("cookies cant be null");
		}
		this.cookies=cookies;
	}

}
