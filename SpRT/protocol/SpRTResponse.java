/* SpRTResponse.java
 * Austin Anderson
 * CSI 4321
 * 2/9/2015
 * 
 */
package SpRT.protocol;

import java.io.*;//ByteArrayInputStream;IOException;InputStream;OutputStream;PrintStream;
import java.util.Scanner;

/** Represents a SpRT response and provides serialization/deserialization
 * 
 * @author Austin
 * @version 1.0
 */
public class SpRTResponse extends SpRTMessage {
	private String message;
	private String status;
	/** Constructs SpRT response using given values
	 * 
	 * @param status   response status
	 * @param function   response function
	 * @param message   response message
	 * @param cookies   response cookie list
	 * @throws SpRTException  if error with given values
	 * @throws NullPointerException   if null parameter
	 */
	public SpRTResponse(String status, String function, String message, CookieList cookies)throws SpRTException{
		super(function,cookies);
		setStatus(status);
		setMessage(message);
	}
	/**Constructs SpRT response using deserialization.
	 * 
	 * @param in  source
	 * @throws SpRTException   if decoding fails do to io problems or validation
	 * failure such as illegal name and/or value, etc.
	 * @throws UnsupportedEncodingException 
	 * @throws  NullPointerException   if input stream is null
	 */
	public SpRTResponse(InputStream in) throws SpRTException{
		super(null);
		if(in==null){
			throw new SpRTException("inputstream in constructor can't be null");
		}
        String parseWith=readRawMessage(in);//helper function defined in SpRTMessage
        String[] pieces=parseAndValidateGeneral(parseWith);//helper function defined in SpRTMessage
        if(pieces.length<2){//two because message could be "", which wouldn't be put in array
        	throw new SpRTException("expected <status> <function> <message> after "+MAGIC);
        }
        //find the index following the second occurrence of ' ' after skipping SpRT/1.0
        int messageAt=parseWith.indexOf(' ',parseWith.indexOf(' ',MAGIC.length())+1)+1;
        //get the portion of the raw message from messageAt, where the message starts
        //to the first \n, where cookies start. parseWith is guaranteed to have a \n in it here
        //due to parseAndValidateGeneral throwing if parseWith doesn't when its called above
    	setMessage(parseWith.substring(messageAt,parseWith.indexOf('\n')).trim());
    	setStatus(pieces[0]);
    	setFunction(pieces[1]);
	}
	public SpRTResponse(Scanner in, PrintStream out)throws SpRTException{
		super(null);//don't need to implement yet
	}
	/** encodes the SpRTResponse and overrides the encode of SpRTMessage
	 * 
	 * @overrides encode in SpRTMessage
	 * @param out   the output stream to write to
	 * @throws SpRTException  if an io problem occurs
	 */
	@Override
	public void encode(OutputStream out)throws SpRTException{
		StringBuilder output=new StringBuilder();
		output.append(MAGIC);
		output.append(getStatus());
		output.append(' ');
		output.append(getFunction());
		output.append(' ');
		output.append(getMessage());
		output.append("\r\n");
		try{
			try{
				out.write(output.toString().getBytes(ENCODING));
			}
			catch(UnsupportedEncodingException bad){
				throw new SpRTException("bad encoding: "+ENCODING);
			}
			cookies.encode(out);
		}
		catch(IOException bad){
			throw new SpRTException(bad.getMessage(),bad);
		}
	
	}
	/** returns message
	 * 
	 * @return message
	 */
	public String getMessage(){
		return message;
	}
	/** returns status
	 * 
	 * @return status
	 */
	public String getStatus(){
		return status;
	}
	/** complete string representation of SpRTResponse
	 *  
	 *  @overrides toString in class SpRTMessage
	 *  @returns string representation
	 */
	public String toString(){
		StringBuilder output=new StringBuilder();
		output.append(MAGIC);
		output.append(getStatus());
		output.append(' ');
		output.append(getFunction());
		output.append(' ');
		output.append(getMessage());
		output.append(' ');
		output.append(cookies.toString());
		return output.toString();
	}
	/** returns if character is an ascii printable
	 * 
	 * @param c   char to check
	 * @return if c is in the range of ascii printables
	 */
	public static boolean isAsciiPrintable(char c){
		return (int)c>=0x20&&(int)c<0x7F;
	}
	/** returns if the string is printable
	 * 
	 * @param str   string to check
	 * @return if str is printable with only ascii
	 */
	public static boolean isPrintable(String str){
		boolean flag=true;
		for(int i=0;flag&&i<str.length();i++){
			if(!isAsciiPrintable(str.charAt(i))){
				flag=false;
			}
		}
		return flag;
	}
	/** set the message
	 * 
	 * @param message   new message
	 * @throws SpRTException   if invalid message
	 * @throws NullPointerException   if null message
	 */
	public void setMessage(final String message)throws SpRTException{
		if(message==null){
			throw new NullPointerException("message cant be null");
		}
		if(!isPrintable(message)){
			throw new SpRTException("message invalid: >"+message+"<");
		}
		this.message=message;
	}
	/** set the status
	 * 
	 * @param status   new status
	 * @throws SpRTException   if invalid status
	 * @throws NullPointerException   if null message
	 */
	public void setStatus(final String status)throws SpRTException {
		if(status==null){
			throw new NullPointerException("status cant be null");
		}
		if(!status.equals("OK")&&!status.equals("ERROR")){
			throw new SpRTException("status invalid");
		}
		this.status=status;
	}

}
