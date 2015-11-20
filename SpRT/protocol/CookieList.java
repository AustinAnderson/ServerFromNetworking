/* CookieList.java
 * Austin Anderson
 * CSI 4321
 * 2/9/2015
 * 
 */
package SpRT.protocol;

import java.io.*;//IOException,InputStream,OutputStream,PrintStream;
import java.util.*;//Map,Scanner,Set,TreeMap;

/**
 * List of cookies (name/value pairs)
 * @author Austin Anderson
 * @version 2.0
 */
public class CookieList {
	protected final String CRLF="\r\n";
	protected final String ENCODING="US-ASCII";
	/** Creates a new Cookies with an empty list of name/value pairs
	 */
    public CookieList(){
    	
    }
    /** Creates a new CookieList by decoding the input stream
     * 
     * @param in - input stream from which to deserialize the name/value list
     * @throws SpRTException - if decoding fails due to I/O problems or validation failure such as illegal name and/or value, etc.
     */
    public CookieList(InputStream in)throws SpRTException{
    	if(in==null){
    		throw new NullPointerException("CookiList constructor cannot have an inputstream of null");
    	}
    	setCookieList(readRawCookies(in));
     
    }
    public CookieList(byte[] bArray)throws SpRTException{
    	if(bArray==null){
    		throw new NullPointerException("CookiList constructor cannot have an inputstream of null");
    	}
    	try{
    		setCookieList(new String(bArray,ENCODING));
    	}
    	catch(IOException bad){
    		throw new SpRTException("bad encoding: "+ENCODING,bad);
    	}
    }
    private void setCookieList(String parseWith) throws SpRTException{
    	if(!parseWith.equals("")&&parseWith!=null){//account for empty input stream
	        if(!(parseWith.charAt(parseWith.length()-1)=='\n'&&parseWith.charAt(parseWith.length()-2)=='\r')){
	        	throw new SpRTException("expected \\r\\n to terminate stream",true);
	        }
	        String[] cookieStrings=parseWith.split("\r\n");  
	        int endIndex=cookieStrings.length;
	        for (int i=0;i<endIndex;i++){
	        	if(cookieStrings[i].equals("")){
	        		break;
	        	}
	        	if(!cookieStrings[i].contains("=")){
	        		throw new SpRTException("expected a = in cookie pair");
	        	}
	            String[] nameValue=cookieStrings[i].split("=");
	            if(nameValue.length!=2){
	            	throw new SpRTException("name or value cannot be the emptystring");
	            }
	            if(!isValidToken(nameValue[0])){
	            	throw new SpRTException("name \""+nameValue[0]+"\" is not a valid token");
	            }
	            if(!isValidToken(nameValue[1])){
	            	throw new SpRTException("value \""+nameValue[1]+"\" is not a valid token");
	            }
	            add(nameValue[0],nameValue[1]);
	        }
        }
    }
    public CookieList(Scanner in, PrintStream out){
    }
    /** adds data from the inputstream to a string until the data matches
	 *  the CRLF character sequence twice in a row
	 * @param in inputstream to use
	 * @return String made from cookie part of raw message data
	 * @throws SpRTException if invalid encoding or a problem is encountered with reading
	 */
	protected String readRawCookies(final InputStream in) throws SpRTException{
		try{
			String toReturn="";//returned at the end init to "" for empty stream
			boolean done=false;
			int currentNdx=-1;//initialized to -1 instead of 0 to account for pre-increment
			int bufSize=255;
			byte[] buf=new byte[bufSize];
			byte[] currentByte=new byte[1];
			byte[] nd=CRLF.getBytes(ENCODING);
			byte[] nd1=CRLF.getBytes(ENCODING);
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
	        	if(currentNdx>=3&&buf[currentNdx]==nd[1]&&buf[currentNdx-1]==nd[0]
	        			&&buf[currentNdx-2]==nd1[1]&&buf[currentNdx-3]==nd1[0]){
	        		done=true;//stop looping if the last 4 bytes of buf match
	        		          //the last two of nd and nd1, each of which contains "\r\n"
	        	}
	        }
	        if(currentNdx!=-1){//-1 indicates empty stream
		        buf=Arrays.copyOf(buf, currentNdx+1);//trim the array
		        toReturn=new String(buf,ENCODING);
	        }
	        return toReturn;
		}
		catch(IOException bad){
			throw new SpRTException(bad.getMessage(),bad);
		}
	}
    /** checks if string contains a valid token
     *  a valid token has one or more characters, all of which are
     *  alphanumeric
     * 
     * @param str - the string to check
     * @return - true if string is a valid token, false otherwise
     */
    public static boolean isValidToken(String str){
    	if(str==null){
    		throw new NullPointerException("token cannont be null");
    	}
		boolean flag=(str.length()!=0);
		for(int i=0;flag&&i<str.length();i++){
			if(!Character.isLetterOrDigit(str.charAt(i))){
				flag=false;
			}
		}
		return flag;
	}
    /** Encode the name-value list
     * 
     * @param out - serialization output sink
     * @throws SpRTException - if there's a problem with IO
     * @throws java.lang.NullPointerException - if out is null
     */
    public void encode(OutputStream out) throws SpRTException{
    	if(out==null){
    		throw new NullPointerException("encode's output stream cannot be null");
    	}
        try{
            StringBuilder makeCookieString=new StringBuilder();
            Set<String> keys=getNames();
            String[] keysArray=keys.toArray(new String[0]);
            for(int i=0;i<keysArray.length;i++){
                makeCookieString.append(keysArray[i]);
                makeCookieString.append('=');
                makeCookieString.append(cookies.get(keysArray[i]));
                makeCookieString.append(CRLF);
            }
            makeCookieString.append(CRLF);
            out.write(makeCookieString.toString().getBytes(ENCODING));
        }
        catch(IOException bad){
            throw new SpRTException(bad.getMessage(),bad);
        }
    }
    /** Adds the name/value pair. If the name already exists, the new value replaces the old value
     * 
     * @param name - name to be added
     * @param value - value to be associated with the name
     * @throws SpRTException - if validation failure for name or value (either is null)
     * @throws NullPointerException - if name or value is null
     */
    public void add(String name, String value) throws SpRTException{
        if(!isValidToken(value)||!isValidToken(name)){
            throw new SpRTException("Illegal cookie name/value "+name+"="+value);
        }
        cookies.put(name,value);
    }
    /**utility function to concatenate CookieLists
     * 
     * @param other other CookieList to append to this one
     */
    public void add(CookieList other){
    	cookies.putAll(other.cookies);
    }
    /** Returns string representation of cookie list
     * @override - toString in class java.lang.Object
     * @return - string representation of cookie list
     */
    public String toString(){
        StringBuilder makeCookieString=new StringBuilder();
        Set<String> keys=getNames();
        String[] keysArray=keys.toArray(new String[0]);
        makeCookieString.append("Cookies=[");
        if(keysArray.length>0){
	        for(int i=0;i<keysArray.length-1;i++){
	            makeCookieString.append(keysArray[i]);
	            makeCookieString.append('=');
	            makeCookieString.append(cookies.get(keysArray[i]));
	            makeCookieString.append(' ');
	        }
	        makeCookieString.append(keysArray[keysArray.length-1]);
	        makeCookieString.append('=');
	        makeCookieString.append(cookies.get(keysArray[keysArray.length-1]));
        }
        makeCookieString.append(']');
        return makeCookieString.toString();
    }
    /** Gets the list of names
     * 
     * @return List (potentially empty) of names (strings) for this list
     */
    public Set<String> getNames(){
        return cookies.keySet();
    }
    /** Gets the value associated with the given name
     * 
     * @param name - String to serve as key
     * @return Value associated with the input name or null if no such name
     */
    public String getValue(String name){
        return cookies.get(name);
    }
    /** Gets hashCode for internal map
     * @overrides hashCode in class java.lang.Object
     */
    @Override
    public int hashCode(){
        return cookies.hashCode();
    }
    /** checks for equality between two CookieLists
     * @overrides equals in class java.lang.Object
     */
    //note: assumes obj will be another CookieList
    @Override
    public boolean equals(Object obj){
    	CookieList cmp2=(CookieList) obj;
        return cookies.equals(cmp2.cookies);
    }
    private Map<String,String> cookies=new TreeMap<String,String>();
}