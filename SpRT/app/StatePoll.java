/*Austin Anderson
 * StatePoll.java 
 */
package SpRT.app;

import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

public class StatePoll implements SpRTState {
	public final static String StateFunctionName="Poll";
	/** override of setNextResponse to handle poll input
	 *  @see SpRTStateContext 
	 */
	@Override
	public void setNextResponse(SpRTStateContext context,
			SpRTRequest fromClient, SpRTResponse toChange)throws SpRTException {
		SpRTStateContext.clearStatus(toChange);
		String FName=fromClient.getCookieList().getValue("FName");
		String LName=fromClient.getCookieList().getValue("LName");
		toChange.setCookies(fromClient.getCookieList());//note: only safe because SpRTServeClient
		//creates a new SpRTRequest from in on each iteration
		if(FName!=null&&LName!=null){//if the cookies for name are here, skip to foodstep
			context.setState(new StateFoodStep());
		
			toChange.setFunction(StateFoodStep.StateFunctionName);
			toChange.setMessage(FName+"'s "+StateFoodStep.StateMessage);
		}
		else{
			context.setState(new StateNameStep());
			toChange.setMessage(StateNameStep.StateMessage);
			toChange.setFunction(StateNameStep.StateFunctionName);
		}
	}

}
