/*Austin Anderson
 * StateMathQuestion.java 
 */
package SpRT.app;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

public class StateMathQuestion implements SpRTState {
	public final static String StateFunctionName="MathQuestion";
	public final static String StateMessage="what is ";
	private static String getRandomNumber(){
		int value=(int)(Math.random()*100);
		return String.valueOf(value);
	}
	private static String getRandomOp(){
		String[] ops={"Plus","Times"};//must match with stateMathCheck
		int ndx=(int)(Math.random()*100)%ops.length;
		return ops[ndx];
	}
	/** override of setNextResponse to handle math question input
	 *  @see SpRTStateContext 
	 */
	@Override
	public void setNextResponse(SpRTStateContext context,
			SpRTRequest fromClient, SpRTResponse toChange) throws SpRTException {
		SpRTStateContext.clearStatus(toChange);
		if(context.validateFunction(fromClient,toChange, StateFunctionName)){
			//set question to last recorded question
			CookieList cookies=fromClient.getCookieList();
			String op=cookies.getValue(StateMathCheck.StateMathCheckOperator);
			String v1=cookies.getValue(StateMathCheck.StateMathCheckValue1);
			String v2=cookies.getValue(StateMathCheck.StateMathCheckValue1);
			//get value returns null if key doesn't exist, and last step of
			//stateMathCheck sets the question values to GetNew. this allows
			//interruption of client but spawns a new question on successful completion
			if(op==null||v1==null||v2==null
					||StateMathCheck.NewQCookie.equals(op)||StateMathCheck.NewQCookie.equals(v1)||StateMathCheck.NewQCookie.equals(v2)){
				op=getRandomOp();
				v1=getRandomNumber();
				v2=getRandomNumber();
			}
			
			toChange.setMessage(StateMessage+v1+" "+op+" "+v2+"?");
			cookies.add(StateMathCheck.StateMathCheckOperator, op);
			cookies.add(StateMathCheck.StateMathCheckValue1, v1);
			cookies.add(StateMathCheck.StateMathCheckValue2, v2);
			toChange.setCookies(fromClient.getCookieList());//note: only safe because SpRTServeClient
			//creates a new SpRTRequest from in on each iteration
			context.setState(new StateMathCheck());
			toChange.setFunction(StateMathCheck.StateFunctionName);
			toChange.setCookies(cookies);
		}
	}

}
