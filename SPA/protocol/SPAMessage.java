/* SPAMessage.java
 * Author: Austin Anderson
 * Date last modified: 3/25/15
 */
package SPA.protocol;

import java.util.Arrays;
/**
 * contains decoding and encoding support for the common header of the
 * SPAQuery and SPAResponse
 * @author Austin Anderson
 *
 */
public abstract class SPAMessage {
	protected versionQrErrcode vqeByte;
	protected byte msgId;
	/** a utility method to concatenate two byte arrays into a bigger one
	 * 
	 * @param a the first byte array
	 * @param b the second byte array
	 * @return a byte array with the data from both
	 */
	public static byte[] concat(final byte[] a, final byte[]b){
		byte[] toReturn=null;
		if(a==null){
			toReturn=b;
		}
		else if(b==null){
			toReturn=a;
		}
		else{
			toReturn=Arrays.copyOf(a, a.length+b.length);
			for(int i=0;i<b.length;i++){
				toReturn[a.length+i]=b[i];
			}
		}
		return toReturn;
	}
	/** a utility method to concatenate two bytes into a byte array
	 * 
	 * @param a the first byte
	 * @param b the second byte
	 * @return a byte array with {a,b};
	 */
	public static byte[] concat(final byte a, final byte b){
		return new byte[]{a,b};
	}
	/**
	 *  return the string representation of this SPAMessage
	 */
	public String toString(){
		return "(Msg "+Byte.toUnsignedInt(msgId)+") "+vqeByte.toString();
	}
	/** decodes the SPAMessage and gives a new SPAMessage object
	 * 
	 * @param pkt the byte array to decode from
	 * @return a SPAMessage with the data decoded
	 * @throws SPAException if there is an error decoding
	 */
	public static SPAMessage decode(byte[] pkt) throws SPAException{
		if(pkt.length<2){
			throw new SPAException("message length to short",SPAException.BAD_LENGTH_SHORT);
		}
		versionQrErrcode initVqeByte=new versionQrErrcode(pkt[0]);//decode the version qr code and error code byt through the versionQrErrcode class
		SPAMessage toReturn=null;
		if(initVqeByte.isQuery()){
			toReturn=new SPAQuery();
		}
		else{
			toReturn=new SPAResponse();
		}
		toReturn.setVqe(initVqeByte);
		toReturn.setMsgId(pkt[1]);
		toReturn.finishDecoding(Arrays.copyOfRange(pkt, 2, pkt.length));//send the remaining unparsed bytes to the created SPAResponse or SPAQuery
		return toReturn;
	}
	/** returns this SPAMessage as a SPAResponse in order to allow the user to treat the message as a SPAResponse
	 * without the danger of a cast error
	 * 
	 * @return this as a SPAResponse
	 * @throws SPAException if the message is actually a SPAQuery
	 */
	public SPAResponse castToResponse() throws SPAException{
		if(vqeByte.isQuery()){
			throw new SPAException("Can't cast query to response",SPAException.SYS_ERR);
		}
		return (SPAResponse)this;
	}
	/** returns this SPAMessage as a SPAQuery in order to allow the user to treat the message as a SPAQuery
	 * without the danger of a cast error
	 * 
	 * @return this as a SPAQuery
	 * @throws SPAException if the message is actually a SPAResponse
	 */
	public SPAQuery castToQuery() throws SPAException{
		if(!vqeByte.isQuery()){
			throw new SPAException("Can't cast response to query",SPAException.SYS_ERR);
		}
		return (SPAQuery)this;
	}
	/** gives the byte array representation of the SPAMessage object
	 * 
	 * @return the byte array representation of the SPAMessage object
	 * @throws SPAException if an error occurs creating the byte array
	 */
	public byte[] encode() throws SPAException{
		return concat(vqeByte.encode(),msgId);
	}
	/** a method to be overridden by children that know whether they are
	 * a SPAQuery or SPAResponse
	 * @param pkt the byte array to decode
	 * @throws SPAException if called
	 */
	protected void finishDecoding(byte[] pkt) throws SPAException{
		//to be overridden
		throw new SPAException("finishDecoding is only to be called by decendents of SPAMessage",SPAException.SYS_ERR);
	}
	/**
	 * 
	 * @return message id
	 */
	public byte getID(){
		return msgId;
	}
	/**
	 * 
	 * @return the message's error code
	 */
	public byte getError(){
		return vqeByte.getErrCode();
	}
	/**
	 * 
	 * @return true if the message is a query, false otherwise
	 */
	public boolean isQuery(){
		return vqeByte.isQuery();
	}
	/**private setter for the vqe byte
	 * 
	 * @param vqe the new version/query response/errorcode byte
	 */
	private final void setVqe(versionQrErrcode vqe){
		this.vqeByte=vqe;
	}
	/**private setter for the message id
	 * 
	 * @param mid the new value for the message id
	 */
	private final void setMsgId(byte mid){
		this.msgId=mid;
	}

}
