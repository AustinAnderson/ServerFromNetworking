/* versionQrErrcode.java
 * Author: Austin Anderson
 * Date last modified: 3/24/15
 */
package SPA.protocol;

/** a utility class to handle the part of the message header 
 *  containing the version, query or response bit, and the error code
 * 
 * @author Austin Anderson
 *
 */
public class versionQrErrcode {
	public final static byte DEFUALUT_RESPONSE_VQE=((byte) 0b0010_1_000);
	public final static byte DEFUALUT_QUERY_VQE=((byte) 0b0010_0_000);
	private final static byte vMASK=(byte) 0b11110000;
	private final static byte qMASK=(byte) 0b00001000;
	private final static byte eMASK=(byte) 0b00000111;
	private static final byte MAX_ERR = SPAException.SYS_ERR;
	private static final byte CURRENT_VERSION = 2;
	
	private byte version;
	private byte qr;
	private byte errCode;
	
	/** creates a new versionQrErrcode out of the given byte
	 * 
	 * @param data the byte to decode
	 * @throws SPAException if a validation error occurs
	 */
	public versionQrErrcode(byte data) throws SPAException{
		decode(data);
	}
	/** decodes the bits of the version qr and error code byte
	 * 
	 * @param data the byte to decode
	 * @throws SPAException if there is a validation error
	 */
	public void decode(byte data) throws SPAException{
		version=(byte) ((data&vMASK)>>>4);
		qr=(byte) ((data&qMASK)>>>3);//clear all bits except the ones we want and shift it appropriately
		errCode=(byte) (data&eMASK);
		if(version!=CURRENT_VERSION){
			throw new SPAException("bad version: "+version,SPAException.BAD_VERSION);
		}
		else if(errCode>MAX_ERR||errCode<0){
			throw new SPAException("bad error code: "+errCode,SPAException.BAD_VERSION);
		}
	}
	/**
	 * @return whether or not this is the versionQrErrcode byte of a Query.
	 */
	public boolean isQuery(){
		boolean flag=true;
		if(qr==1){
			flag=false;
		}
		return flag;
	}
	/**
	 * 
	 * @return the version
	 */
	public byte getVersion(){
		return version;
	}
	/**
	 * 
	 * @return the qr bit as a byte
	 */
	public byte getQR(){
		return qr;
	}
	/**
	 * 
	 * @return the error code
	 */
	public byte getErrCode(){
		return errCode;
	}
	/**
	 * 
	 * @return the byte containing the version, qr bit and error code
	 */
	public byte encode() {
		return (byte) ((version<<4)|(qr<<3)|(errCode));
	}
	/**
	 * returns a string represtation of this versionQrErrcode object
	 */
	public String toString(){
		String toReturn=null;
		if(qr==1){
			toReturn="SPA_v"+version+" Response with errCode "+errCode+": ";
		
		}
		else {
			toReturn="SPA_v"+version+" Query with errCode "+errCode+": ";
		}
		return toReturn;
	}
	/** sets the error code for the message
	 * 
	 * @param err the new error code to use
	 * @throws SPAException if the error is out of bounds
	 */
	public void setError(byte err) throws SPAException{
		if(err==SPAException.BAD_LENGTH_LONG||err==SPAException.BAD_LENGTH_SHORT){
			err=SPAException.BAD_LENGTH;
		}
		if(err==SPAException.WRONG_MESSAGE_TYPE){
			err=SPAException.BAD_MESSAGE;
		}
		if(err>MAX_ERR||err<0){
			throw new SPAException("error out of range",SPAException.SYS_ERR);
		}
		errCode=err;
	}
}
