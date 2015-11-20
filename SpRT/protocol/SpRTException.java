/* SpRTException
 * version: 1.0
 * last modified: 29/1/2015
 * Author: Austin Anderson
 */
package SpRT.protocol;

/** Exception for SpRT handling
 * 
 * @author Austin Anderson
 * @version 1.2
 */
public class SpRTException extends Exception {
	private boolean tooShort=false;

	private static final long serialVersionUID = 1L;
	/** Constructs SpRTexception
	 * 
	 * @param msg - exception message
	 */
	public SpRTException(String msg){
        super(msg);
	}
	/** Constructs SpRTexception
	 * 
	 * @param msg - exception message
	 * @param ts- if the error was caused by the message being too short 
	 */
	public SpRTException(String msg,boolean ts){
        super(msg);
        tooShort=ts;
    }
	/** constructs SpRTException
	 * 
	 * @param msg - exception message
	 * @param cause - exception cause
	 */
    public SpRTException(String msg, Throwable cause){
    	super(msg,cause);
    }
	/** constructs SpRTException
	 * 
	 * @param msg - exception message
	 * @param cause - exception cause
	 * @param ts- if the error was caused by the message being too short 
	 */
    public SpRTException(String msg, Throwable cause,boolean ts){
    	super(msg,cause);
    	tooShort=ts;
    }
    /**
     * 
     * @return if the message that through the error was too short
     */
	public boolean isTooShort() {
		return tooShort;
	}
}
