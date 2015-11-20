/* SPAException.java
 * last modified: 3/25/2015
 * Author: Austin Anderson
 */
package SPA.protocol;

public class SPAException extends Exception{
	//in the last program, figuring out what kind of error a sprtException
	//was was a pain, so SPAException will include an indicator variable that
	//will match with the error code specification
	//this allows something like SPAMessage.setErr(SPAException.getErr());
	//to that end, since the error codes in the spec dont distinguish between bad length long
	//and bad length short, these must be put at the end of the list 
    private int errCode;
	public final static int BAD_VERSION=1;
	public final static int BAD_LENGTH=2;
	public final static int BAD_MESSAGE=3;
	public final static int SYS_ERR=4;
	//extended types. these are translated into their generics when passed to spaResponse.setError
	public final static int BAD_LENGTH_LONG=5;
	public final static int BAD_LENGTH_SHORT=6;
	public final static int WRONG_MESSAGE_TYPE=7;
	

	private static final long serialVersionUID = 1L;
	
	/** Constructs SPAException
	 * 
	 * @param msg - exception message
	 */
	public SPAException(String msg,int eCode){
        super(msg);
		errCode=eCode;
    }
	/** constructs SPAException
	 * 
	 * @param msg - exception message
	 * @param cause - exception cause
	 */
    public SPAException(String msg, Throwable cause,int eCode){
    	super(msg,cause);
		errCode=eCode;
    }
    /**
     * 
     * @return the error code associated with this SPAException
     */
    public int getErrCode(){
    	return errCode;
    }

}
