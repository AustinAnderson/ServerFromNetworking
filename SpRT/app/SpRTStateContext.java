/* SpRTStateContext.java
 * Author: Austin Anderson
 * Date last modified: 3/25/15
 */
package SpRT.app;

import SPA.app.SPAServer;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

public class SpRTStateContext {
	private SpRTState current;
	
	public SpRTStateContext(SpRTRequest initial,SpRTResponse toChange) throws SpRTException{
		if(StatePoll.StateFunctionName.equals(initial.getFunction())){
			SPAServer.useApp(SpRTServer.POLL);//static method, spa server is singleton
			setState(new StatePoll());
		}
		else if(StateMathQuestion.StateFunctionName.equals(initial.getFunction())){
			SPAServer.useApp(SpRTServer.MATHQUESTION);//static method spa server is singleton
			setState(new StateMathQuestion());
		}
		else{
			setBadFunction(initial, toChange);
			current=null;
		}
	}
	void setState(final SpRTState next){
		current=next;
	}
	/** set toChange to send back to the client the fact that
	 *  a bad function was recieved
	 * @param fromClient the SpRTRequest read from the client
	 * @param toChange the SpRTResponse to change
	 * @throws SpRTException if an error occurs modifying toChange or fromClient
	 */
	void setBadFunction(SpRTRequest fromClient,SpRTResponse toChange) throws SpRTException {
		toChange.setStatus("ERROR");
		toChange.setFunction("NULL");
		toChange.setMessage("Bad Function: "+fromClient.getFunction());
		ErrorLog.logErr(fromClient, "Unexpected function: "+fromClient.getFunction(),true);
	}
	static void clearStatus(SpRTResponse toChange) throws SpRTException{
		toChange.setStatus("OK");
	}
	/** utility function to validate functions for each state
	 * 
	 * 
	 * @param actual function name received
	 * @param expected the valid function name for the current state
	 * @param toChange the response object to change to reflect a bad message 
	 */
	boolean validateFunction(SpRTRequest actual,SpRTResponse toChange, String expected)throws SpRTException {
		boolean toReturn=true;
		String checkWith=actual.getFunction();
		if(!checkWith.equals(expected)){
			this.setBadFunction(actual,toChange);
			toReturn=false;
		}
		else{
			ErrorLog.logInf(actual);
		}
		return toReturn;
	}
	/** the public method for advancing states
	 *  repeated calls to this should advance through
	 *  states due to the different state classes being held in current;
	 * @param fromClient the SpRTRequest read from the client
	 * @param toChange the SpRTResponse to change
	 * @throws SpRTException if an error occurs modifying toChange or fromClient
	 */
	public void setNextResponse(SpRTRequest fromClient,SpRTResponse toChange) throws SpRTException{
		if(current!=null){
			current.setNextResponse(this, fromClient, toChange);
		}
	}
}
