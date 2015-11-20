/* SPATimeStamp.java
 * Author: Austin Anderson
 * Date last modified: 3/18/15
 */
package SPA.protocol;

import java.util.Date;
/**
 * utility class to handle the part of the response data asociated with the timestamp
 * @author Austin Anderson
 *
 */
public class SPATimeStamp {
	private final static int byte0=0b11111111_00000000_00000000_00000000;
	private final static int byte1=0b00000000_11111111_00000000_00000000;
	private final static int byte2=0b00000000_00000000_11111111_00000000;
	private Date now;
	
	/**
	 * create a new timestamp with the time as 0
	 */
	public SPATimeStamp(){
		setTimeStampZero();
	}
	/**
	 * 
	 * @return whether or not the time is 0
	 */
	public boolean isZeroTime(){
		return (now.getTime()==0L);
	}
	/**
	 *  sets the timestamp to the epoch 
	 */
	public void setTimeStampZero() {
		now=new Date(0L);
	}
	/**
	 *  sets the timestamp to the current time
	 */
	public void setTimeStampNow() {
		now=new Date();
	}
	/** returns the current time as a big endian four byte byte array
	 * 
	 * @return the byte array representation of the timestamp
	 * @throws SPAException if an error occurs creating the byte array
	 */
	public byte[] encode() throws SPAException{
		byte[] toReturn=null;
		long timeNumLong=(now.getTime()/1000);
		if(timeNumLong>Integer.MAX_VALUE){//if the time is too big to be stored
			throw new SPAException("protocol is too old to use",SPAException.SYS_ERR);
		}
		int timeNum=(int)timeNumLong;
		toReturn =new byte[]{(byte)((byte0&timeNum)>>>(3*8)),
			(byte)((byte1&timeNum)>>>(2*8)),
			(byte)((byte2&timeNum)>>>(8)),
			(byte)timeNum
		};
		return toReturn;
	}
	/** decodes the four bytes of the response's byte array to a timestamp
	 * 
	 * @param data the byte array to decode
	 * @throws SPAException if the byte array is not 4 bytes
	 */
	public void decode(byte[] data) throws SPAException{
		if(data.length<4){
			throw new SPAException("timeStamp too short",SPAException.BAD_LENGTH_SHORT);
		}
		//convert each byte to its unsigned long equivalent before shifting it to the correct position,
		//then or them all together to get the full long
		long time=((Byte.toUnsignedLong(data[0])<<(8*3))|(Byte.toUnsignedLong(data[1])<<(8*2))|
				(Byte.toUnsignedLong(data[2])<<(8))|(Byte.toUnsignedLong(data[3])))*1000;
		now.setTime(time);
	}
	/**
	 * return the string representation of the time
	 */
	public String toString(){
		String toReturn=null;
		if(isZeroTime()){
			toReturn="N/A";
		}
		else{
			toReturn=now.toString();
		}
		return toReturn;
	}
	/**
	 * 
	 * @return the time stamp as a java Date
	 */
	public Date getNow(){
		return now;
	}
}
