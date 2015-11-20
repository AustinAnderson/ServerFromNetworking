/* SPAResponse.java
 * Author: Austin Anderson
 * Date last modified: 3/25/15
 */
package SPA.protocol;

public class SPAResponse extends SPAMessage {
	private SPATimeStamp time;
	private byte appCount;
	private AppEntry[] apps;
	/** creates an empty SPAResponse with just the response header
	 * note that the SPAMessage.decode() factory method should be used for clients 
	 * 
	 * @throws SPAException if an error occurs creating the timestamp or vqe byte
	 */
	public SPAResponse() throws SPAException{
		vqeByte=new versionQrErrcode(versionQrErrcode.DEFUALUT_RESPONSE_VQE);
		time=new SPATimeStamp();
		msgId=0;
	}
	/** creates a SPAResponse with the given id
	 * 
	 * @param id message id to use
	 * @throws SPAException if the id is out of range
	 */
	public SPAResponse(int id) throws SPAException{
		vqeByte=new versionQrErrcode(versionQrErrcode.DEFUALUT_RESPONSE_VQE);
		time=new SPATimeStamp();
		if(id>Byte.MAX_VALUE){
			throw new SPAException("id invalid",SPAException.SYS_ERR);
		}
		msgId=(byte) id;
	}
	/**sets the message id
	 * 
	 * @param id id to use
	 */
	public void setID(byte id){
		msgId=id;
	}
	/** sets the errorcode for the message
	 * 
	 * @param err the error code to use
	 * @throws SPAException if the error code is out of bounds
	 */
	public void setError(int err) throws SPAException{
		vqeByte.setError((byte)err);
	}
	/** creates an full SPAResponse out of the given info 
	 * note that the SPAMessage.decode() factory method should be used for clients 
	 * 
	 * @param appNames an array of strings holding the names of the applications supported by the server
	 * @param appUsages an array of ints holding the number of times each applications supported by the server was run
	 * 
	 * @throws SPAException if an error occurs creating the timestamp or vqe byte
	 * or if the number of elements in appUsages doesn't correspond to the number of elements in appNames
	 */
	public SPAResponse(String[] appNames,int[] appUsages,byte msgId) throws SPAException{
		vqeByte=new versionQrErrcode(versionQrErrcode.DEFUALUT_RESPONSE_VQE);
		time=new SPATimeStamp();
		if(appNames.length!=appUsages.length){
			throw new SPAException("app entry data mismatched",SPAException.SYS_ERR);
		}
		apps=new AppEntry[appNames.length];
		for(int i=0;i<apps.length;i++){
			apps[i]=new AppEntry(appNames[i],appUsages[i]);
		}
		if(apps.length>Byte.MAX_VALUE){
			throw new SPAException("too many applications",SPAException.BAD_LENGTH_LONG);
		}
		appCount=(byte)apps.length;
		this.msgId=msgId;
	}
	/**
	 * @return this SPAResponse as a byte array
	 * @throws SPAException if an error occurs creating the byte array
	 */
	public byte[] encode() throws SPAException{
		byte[] common=super.encode();
		return SPAMessage.concat(common, encodeNoHeader());
	}
	/**
	 * 
	 * @return this SPAResponse as a byte array without the message header data
	 * @throws SPAException if an error occurs creating the byte array
	 */
	public byte[] encodeNoHeader() throws SPAException{
		byte[] specific=SPAMessage.concat(time.encode(),new byte[]{appCount});//fill with response data
		if(apps!=null){
			for(AppEntry i:apps){
				specific=SPAMessage.concat(specific, i.encode());
			}
		}
		return specific;
	}
	/** a public method for accessing the finishDecoding method
	 * 
	 * @param pkt the byte array to decode
	 * @throws SPAException if  the packet is the wrong size
	 */
	public void updateDecode(byte[] pkt)throws SPAException{
		this.finishDecoding(pkt);
	}
	/**
	 *  returns this string representation of this SPAResponse object
	 */
	@Override
	public String toString(){
		StringBuilder builder=new StringBuilder();
		builder.append(super.toString());
		if(apps!=null){
			for(int i=0;i<apps.length;i++){
				builder.append(apps[i].toString()+", ");
			}
		}
		builder.append("last application was run "+time);
		return builder.toString();
	}
	/**
	 * utility method to decode the Response portion of the data without the SPAMessage header
	 * @param pkt the byte array to decode from
	 * @throws SPAException if  the packet is the wrong size
	 */
	@Override
	public void finishDecoding(byte[] pkt) throws SPAException{
		int varDataStartNdx=5;
		if(pkt.length<varDataStartNdx){
			throw new SPAException("Response too short",SPAException.BAD_LENGTH_SHORT);
		}
		time.decode(pkt);
		appCount=pkt[varDataStartNdx-1];
		apps=new AppEntry[getCount()];
		boolean beenUsed=false;
		int start=varDataStartNdx;
		for(int i=0;i<getCount();i++){
			apps[i]=new AppEntry(pkt,start);
			start+=apps[i].getSize();
			if(apps[i].getCount()!=0){
				beenUsed=true;
			}
		}
		if(!beenUsed){
			time.setTimeStampZero();
		}
		for(int i=start;i<pkt.length;i++){
			if(pkt[i]!=0){
				throw new SPAException("Response too Long",SPAException.BAD_LENGTH_LONG);
			}
		}
	}
	/** sets the timestamp to be the current time
	 */
	public void setTimeStampNow() {
		time.setTimeStampNow();
	}
	/** returns the current time as a big endian four byte byte array
	 * 
	 * @return the byte array representation of the timestamp
	 * @throws SPAException if an error occurs creating the byte array
	 */
	public byte[] getTime() throws SPAException{
		return time.encode();
	}
	/**
	 * @return the number of applications stored
	 */
	public int getCount(){
		return Byte.toUnsignedInt(appCount);
	}
	/** returns the AppEntry array containing the apps
	 * 
	 * @return the applications
	 */
	public AppEntry[] getEntries(){
		return apps;
	}
	/** updates a SPAResponse to reflect the usage of an app
	 * 
	 * @param which the index of the appEntry to use
	 * @throws SPAException if the index to use is out of bounds
	 */
	public void use(int which) throws SPAException{
		if(which>apps.length||which<0){
			throw new SPAException("app entry index out of bounds",SPAException.SYS_ERR);
		}
		apps[which].use();
		setTimeStampNow();
	}
}
