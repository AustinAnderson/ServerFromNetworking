/* SpRTRequest.java
 * Austin Anderson
 * CSI 4321
 * 2/9/2015
 * 
 */
package SpRT.protocol;

import java.io.*;//ByteArrayInputStream,IOException,InputStream,PrintStream
import java.util.Arrays;
import java.util.Scanner;

/**Represents a SpRT request and provides serialization/deserialization
 * 
 * @author Austin Anderson
 * @version 1.0
 */
public class SpRTRequest extends SpRTMessage{
	public static final String COMMANDRUN="RUN";
	private String[] Params;
	/** Constructs SpRT request using given values
	 * 
	 * @param command   request command
	 * @param function   request function
	 * @param params   request parameters
	 * @param cookies   request cookie list
	 * @throws SpRTException   if error with given values
	 * @throws NullPointerException   if null parameter
	 */
	public SpRTRequest(String command,String function,String[] params, CookieList cookies)throws SpRTException{
		super(function,cookies);
		setCommand(command);//kept in to validate command parameter
		setParams(params);
	}
	/** Constructs SpRT request using deserialization
	 * 
	 * @param in   deserialization input source
	 * @throws SpRTException   if io,parse, or validation failure
	 * @throws NullPointerException if inputStream is null
	 */
	public SpRTRequest(InputStream in)throws SpRTException{
		super(null);
		if(in==null){
			throw new NullPointerException("inputstream can't be null");
		}
        String parseWith=readRawMessage(in);//helper function defined in SpRTMessage
        String[] pieces=parseAndValidateGeneral(parseWith);//helper function defined in SpRTMessage
        if(pieces.length<2){
        	throw new SpRTException("expected multiple space delimited tokens after SpRT/1.0");
        }
        String[] params={};
        if(pieces.length>2){
        	params=Arrays.copyOfRange(pieces, 2, pieces.length);
        }
        setParams(params);
        setCommand(pieces[0]);
        setFunction(pieces[1]);
	}
	public SpRTRequest(Scanner in,PrintStream out)throws SpRTException{
		super(null);
		//do not need to implement yet
	}	
	/** encodes the SpRTRequest and overrides the encode of SpRTMessage
	 * 
	 * @overrides encode in SpRTMessage
	 * @param out   the output stream to write to
	 * @throws SpRTException  if an io problem occurs
	 */
	@Override
	public void encode(OutputStream out)throws SpRTException{
		StringBuilder output=new StringBuilder();
		output.append(MAGIC);
		output.append(getCommand());
		output.append(' ');
		output.append(getFunction());
		for(String i:getParams()){
			output.append(' ');
			output.append(i);
		}
		output.append("\r\n");
		try{
			try{
				out.write(output.toString().getBytes(ENCODING));
			}
			catch(UnsupportedEncodingException bad){//doesn't catch other io errors so they can be handled
				throw new SpRTException("bad encoding: "+ENCODING);//by outter catch
			}
			cookies.encode(out);
		}
		catch(IOException bad){
			throw new SpRTException(bad.getMessage(),bad);
		}
	
	}
	/** Complete string representation of SpRTRequest
	 *  
	 *  @overrides toString in class SpRTMessage
	 *  @returns string representation
	 */
	public String toString(){
		StringBuilder output=new StringBuilder();
		output.append(MAGIC);
		output.append(getCommand());
		output.append(' ');
		output.append(getFunction());
		for(String i:getParams()){
			output.append(' ');
			output.append(i);
		}
		output.append(' ');
		output.append(cookies.toString());
		return output.toString();
	}
	/** return command
	 * 
	 * @return command
	 */
	public String getCommand(){
		return COMMANDRUN;
	}
	/** set the command
	 * 
	 * @param command   new command
	 * @throws SpRTException   if invalid command
	 * @throws NullPointerException   if null command
	 */
	public void setCommand(String command)throws SpRTException{
		if(command==null){
			throw new NullPointerException("command cannot be null");
		}
		if(!command.equals(COMMANDRUN)){
			throw new SpRTException("command invalid");
		}
	}
	/** return parameter list
	 * 
	 * @return parameter list
	 */
	public String[] getParams(){
		return Params;
	}
	/**
	 * set parameters
	 * @param params new parameters
	 * @throws SpRTException  if invalid params
	 * @throws NullPointerException  if null array or array element
	 */
	public void setParams(String[] params)throws SpRTException{
		if(params==null){
			throw new NullPointerException("params array cannot be null");
		}
		for(String i:params){
			if(!CookieList.isValidToken(i)){
				throw new SpRTException("param(s) invalid");
			}
		}
		Params=params;
	}
}
