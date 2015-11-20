/*Austin Anderson
 * StateNameStep.java 
 */
package SpRT.app;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

public class StateNameStep implements SpRTState {
	public final static String StateFunctionName="NameStep";
	public final static String StateMessage="Name (First Last)>";
	
	/** override of setNextResponse to handle name step
	 *  @see SpRTStateContext 
	 */
	@Override
	public void setNextResponse(SpRTStateContext context,
			SpRTRequest fromClient, SpRTResponse toChange) throws SpRTException {
		SpRTStateContext.clearStatus(toChange);
		if(context.validateFunction(fromClient,toChange, StateFunctionName)){
			String[] fcParams=fromClient.getParams();
			if(fcParams.length!=2){
				toChange.setFunction(StateFunctionName);
				toChange.setStatus("ERROR");
				toChange.setMessage("Poorly formed name. "+StateMessage);
			}
			else{
				String FName=fcParams[0];
				String LName=fcParams[1];
				toChange.setFunction(StateFoodStep.StateFunctionName);
				toChange.setMessage(FName+"'s "+StateFoodStep.StateMessage);
				CookieList cookies=fromClient.getCookieList();
				cookies.add("FName",FName);
				cookies.add("LName",LName);
				toChange.setCookies(cookies);//note: only safe because SpRTServeClient
			    //creates a new SpRTRequest from in on each iteration
				context.setState(new StateFoodStep());
			}
		}

	}

}
