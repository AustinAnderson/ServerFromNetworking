/*Austin Anderson
 * StateFoodStep.java 
 */
package SpRT.app;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

public class StateFoodStep implements SpRTState {
	public final static String StateFunctionName="FoodStep";
	public final static String StateMessage="food mood>";
	/** override of setNextResponse to handle food step
	 *  @see SpRTStateContext 
	 */
	@Override
	public void setNextResponse(SpRTStateContext context, 
			SpRTRequest fromClient, SpRTResponse toChange) throws SpRTException {
		SpRTStateContext.clearStatus(toChange);
		if(context.validateFunction(fromClient,toChange, StateFunctionName)){
			CookieList cookies=fromClient.getCookieList();
			String repeatLevel=cookies.getValue("Repeat");
			if(repeatLevel==null){
				repeatLevel="0";
			}
			try{
				repeatLevel=String.valueOf(Integer.parseInt(repeatLevel)+1);
			}
			catch(NumberFormatException bad){
				toChange.setMessage("Cookies corrupted in transmition");
				throw new SpRTException("corrupted cookies: ",bad);
			}
			cookies.add("Repeat",repeatLevel);
			repeatLevel="+ "+repeatLevel+"% ";
			String where="off at ";
			String discount=null;
			if("Italian".equals(fromClient.getParams()[0])){
				where+="Pastastic";
				discount="20% "+repeatLevel;
			}
			else if("Mexican".equals(fromClient.getParams()[0])){
				where+="Tacopia";
				discount="25% "+repeatLevel;
			}
			else if("Korean".equals(fromClient.getParams()[0])){
				where+="Phocal Point";
				discount="50% "+repeatLevel;
			}
			else{
				discount="";
				where="You have poor taste. 10% off at McDonalds.";
			}
			toChange.setMessage(discount+where);
			toChange.setFunction("NULL");
			toChange.setCookies(cookies);
		}
	}

}
