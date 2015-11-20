/* SPAQuery.java
 * Author: Austin Anderson
 * Date last modified: 3/18/15
 */
package SPA.protocol;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Austin_Anderson1
 *
 */
public class SPAQuery extends SPAMessage {
	private int businessNameLength;
	private byte[] businessName;
	
	private static final String ENCODING="US-ASCII";
	/** 
	 *  @return the byte array representation of the SPAQuery object
	 */
	public byte[] encode() throws SPAException{
		byte[] common=super.encode();
		return SPAMessage.concat(common, encodeNoHeader());
	}
	/** constructor to assign a random id
	 *  setBusinessName should be called prior to sending the packet, and is set to nothing by default
	 *  
	 *  note that the explicit SPAQuery constructors only exist to aid creating a packet to send. 
	 *  Servers receiving a packet should use the SPAMessage.decode factory method instead
	 *  
	 * @throws SPAException
	 */
	public SPAQuery() throws SPAException{
		vqeByte=new versionQrErrcode(versionQrErrcode.DEFUALUT_QUERY_VQE);
		msgId=(byte)(Math.random()*1000);
	}
	/** constructor to explicitly choose an id
	 *  setBusinessName should be called prior to sending the packet, and is set to nothing by default
	 *  
	 *  note that the explicit SPAQuery constructors only exist to aid creating a packet to send. 
	 *  Servers receiving a packet should use the SPAMessage.decode factory method instead
	 *  
	 * @param msgID the message id to use
	 * @throws SPAException if the default versionQrErrcode is bad
	 */
	public SPAQuery(byte msgID) throws SPAException{
		vqeByte=new versionQrErrcode(versionQrErrcode.DEFUALUT_QUERY_VQE);
		this.msgId=msgID;
	}
	/** constructor to explicitly choose an id and name
	 *  
	 *  note that the explicit SPAQuery constructors only exist to aid creating a packet to send. 
	 *  Servers receiving a packet should use the SPAMessage.decode factory method instead
	 *  
	 * @param msgID the message id to use
	 * @param businessName the name to query for
	 * @throws SPAException if the default versionQrErrcode or the businessName is invalid
	 */
	public SPAQuery(byte msgID,String businessName) throws SPAException{
		vqeByte=new versionQrErrcode(versionQrErrcode.DEFUALUT_QUERY_VQE);
		this.msgId=msgID;
		setBusinessName(businessName);
	}
	/** encodes just the query data and not the message header
	 * 
	 * @return a byte array representing the query's data only
	 * @throws SPAException if an error occurs formatting the data
	 */
	public byte[] encodeNoHeader() throws SPAException{
		return SPAMessage.concat(new byte[]{(byte)businessNameLength}, businessName);
	}
	/** a public method for accessing the finishDecoding method
	 * 
	 * @param pkt the byte array to decode
	 * @throws SPAException if  the packet is the wrong size
	 */
	public void updateDecode(byte[] pkt) throws SPAException{
		this.finishDecoding(pkt);
	}
	/**
	 * returns the string representation of the query
	 */
	@Override
	public String toString(){
		String toReturn=null;
		try {
			toReturn=super.toString()+"Business '"+new String(businessName,ENCODING)+"'";
		} catch (UnsupportedEncodingException e) {
			System.err.println("The SPA protocol is not supported by your machine");
		}
		return toReturn;
	}
	/**
	 * utility method to decode the query portion of the data without the SPAMessage header
	 * @param pkt the byte array to decode from
	 * @throws SPAException if  the packet is the wrong size
	 */
	@Override
	protected void finishDecoding(byte[] pkt) throws SPAException{
		int varDataStartNdx=1;//this is the index of where the first byte of variable length data is
		if(pkt.length<varDataStartNdx){
			throw new SPAException("Query Too Short",SPAException.BAD_LENGTH_SHORT);
		}
		businessNameLength=Byte.toUnsignedInt(pkt[varDataStartNdx-1]);
		if(businessNameLength>(pkt.length-varDataStartNdx)){//if the stated businessNameLength 
															//is bigger than the remaining unparsed space
			throw new SPAException("Query Too Short",SPAException.BAD_LENGTH_SHORT);
		}
		businessName=new byte[getNameLength()];
		for(int i=0;i<businessNameLength;i++){
			businessName[i]=pkt[i+varDataStartNdx];
		}
		for(int i=businessNameLength+varDataStartNdx;i<pkt.length;i++){
			if(pkt[i]!=0){//if there is any data after the supposed length
				throw new SPAException("Query Too Long",SPAException.BAD_LENGTH_LONG);
			}
		}
	}
	/**
	 * 
	 * @param name the new Business Name
	 * @throws SPAException if the name is too long to store it's size in the length byte
	 */
	public void setBusinessName(String name) throws SPAException{
		byte[] newName=name.getBytes();
		if(newName.length>Byte.MAX_VALUE){
			throw new SPAException("business name too long",SPAException.BAD_MESSAGE);
		}
		businessName=newName;
		businessNameLength=(byte)businessName.length;
	}
	/**
	 * 
	 * @return businessNameLength;
	 */
	public int getNameLength(){
		return businessNameLength;
	}
	/**
	 * 
	 * @return businessName;
	 */
	public byte[] getName(){
		return businessName;
	}

}
