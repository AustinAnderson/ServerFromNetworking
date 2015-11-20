/* AppEntry.java
 * Author: Austin Anderson
 * Date last modified: 3/18/15
 */
package SPA.protocol;

import java.io.UnsupportedEncodingException;

/**utility class to handle the portion of a respons's data that corresponds to an app entry
 * 
 * @author Austin Anderson
 *
 */
public class AppEntry {
	private static final String ENCODING="US-ASCII";
	private static final int VAR_DATA_START_NDX=3;
	private int appUseCount;//really two bytes
	private byte appNameLength;
	private byte[] appName;
	
	
	/**
	 * 
	 * @return the number of bytes in this AppEntry
	 */
	public int getSize(){
		return VAR_DATA_START_NDX+appName.length;
	}
	/** creates a new app entry out of primitives instead of a byte array
	 * 
	 * @param name the application name
	 * @param Count the number of times the application has been run
	 * @throws SPAException
	 */
	public AppEntry(String name,int count) throws SPAException{
		try{
			appName=name.getBytes(ENCODING);
		}
		catch(UnsupportedEncodingException bad){
			throw new SPAException("SPA protocol is not supported on your system",SPAException.SYS_ERR);
		}
		if(appName.length>Byte.MAX_VALUE){
			throw new SPAException("Application name too long",SPAException.BAD_LENGTH_LONG);
		}
		appNameLength=(byte) appName.length;
		appUseCount=count;
	}
	/** creates a new app entry out of the data byte array
	 * note that the data must contain only the app entry data for this version of the constructor
	 * 
	 * @param data the data to decode.
	 * @throws SPAException if an error occurs decoding
	 */
	public AppEntry(byte[] data) throws SPAException{
		decode(data);
	}
	/** creates a new app entry out of the data byte array
	 * 
	 * @param start the index to start decoding from
	 * @param data the data to decode.
	 * @throws SPAException if an error occurs decoding
	 */
	public AppEntry(byte[] data,int start) throws SPAException{
		decode(data,start);
	}
	/**
	 * 
	 * @return the number of times the application has been used
	 */
	public int getCount(){
		return appUseCount;
	}
	/**
	 * 
	 * @return the length of the app name
	 */
	public int getNameLength(){
		return Byte.toUnsignedInt(appNameLength);
	}
	/**
	 * 
	 * @return the byte array containing the apps name
	 */
	public byte[] getName(){
		return appName;
	}
	/** decodes the app entry and sets all attributes
	 * 
	 * @param data the byte array to decode from
	 * @throws SPAException if there is an error decoding
	 */
	public void decode(byte[] data) throws SPAException{
		decode(data,0);
	}
	/** decodes the app entry and sets all attributes
	 * 
	 * @param startNdx the index to start reading from
	 * @param data the byte array to decode from
	 * @throws SPAException if there is an error decoding
	 */
	public void decode(byte[] data,int startNdx) throws SPAException{
		if(data.length-startNdx<VAR_DATA_START_NDX){//if the length of the protocol constant app entry data is longer than the remaining space
			throw new SPAException("Application entry too short",SPAException.BAD_LENGTH_SHORT);
		}
		appUseCount=Byte.toUnsignedInt(data[0+startNdx])<<8|Byte.toUnsignedInt(data[1+startNdx]);
		appNameLength=data[2+startNdx];
		if((getNameLength()+VAR_DATA_START_NDX+startNdx)>data.length){//if the stated name length is greater than the remaining unparsed space
			throw new SPAException("Application entry too short",SPAException.BAD_LENGTH_SHORT);
		}
		appName=new byte[getNameLength()];
		for(int i=0;i<getNameLength();i++){
			appName[i]=data[i+VAR_DATA_START_NDX+startNdx];
			if(appName[i]==0){//assumes a valid name can't have '\0' in it
				throw new SPAException("Application entry too short",SPAException.BAD_LENGTH_SHORT);
			}
		}
		
		//for(int i=startNdx;i<data.length;i++){
			//System.out.print(""+data[i]+"-");
		//}
		//System.out.println();
	}
	/** gives the byte array representation of this AppEntry object
	 * 
	 * @return byte array representation of this AppEntry object
	 * @throws SPAException if there is an error creating the byte array
	 */
	public byte[] encode() throws SPAException{
		byte first=(byte) (appUseCount>>>8);
		byte second=(byte) appUseCount;
		return SPAMessage.concat(new byte[]{first,second,appNameLength}, appName);
	}
	/**
	 *  returns a string representation of this AppEntry object
	 */
	@Override
	public String toString(){
		String toReturn=null;
		try {
			toReturn="'"+(new String(appName,ENCODING))+"' used "+appUseCount+" times";
		} catch (UnsupportedEncodingException e) {
			System.err.println("The SPA protocol is not supported by your machine");
		}
		return toReturn;
	}
	
	/**
	 * allows a user to increment the number of times the application has been used
	 */
	public void use(){
		int maxValTwoBytes=((Byte.MAX_VALUE+1)*(Byte.MAX_VALUE+1))-1;
		if(appUseCount<maxValTwoBytes){
			appUseCount++;
		}
	}
	
}
