/*Austin Anderson
 * StateMathCheck.java
 */
package SpRT.app;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

public class StateMathCheck implements SpRTState {
	public final static String NewQCookie="GetNew";
	public final static String StateFunctionName="MathCheckAnswer";
	public final static String StateMessageCorrect="You are correct!";
	public final static String StateMessageWrong="Incorrect! go back to elementry school.";
	public final static String StateMessageError="Only one answer is allowed. (hint it's a whole number)";
	public final static String StateMathCheckOperator="Operator";
	public final static String StateMathCheckValue1="Value1";
	public final static String StateMathCheckValue2="Value2";
	/**validate the received answer to the math question
	 * 
	 * @param fromClient the SpRTRequest received containing the user's answer
	 * @param toChange the SpRTResponse to change to respond to the client
	 * @throws SpRTException if an error occurs modifying toChange or fromClient
	 */
	private static void checkCorrect(SpRTRequest fromClient, SpRTResponse toChange) throws SpRTException{
		CookieList cookielist=fromClient.getCookieList();
		if(!cookielist.equals(toChange.getCookieList())){//client should send back the same question
			toChange.setMessage("Cookies corrupted in transmition");
			throw new SpRTException("mathCheck cookielist mismatch");
		}
		String v1=cookielist.getValue(StateMathCheckValue1);
		String v2=cookielist.getValue(StateMathCheckValue2);
		String op=cookielist.getValue(StateMathCheckOperator);
		String message=null;
		try{
			if(fromClient.getParams().length!=1){
				throw new NumberFormatException("cant have multiple numbers");
			}
			message=StateMessageCorrect;
			int act=Integer.parseInt(fromClient.getParams()[0]);
			int expected=0;
			int val1=Integer.parseInt(v1);
			int val2=Integer.parseInt(v2);
			if("Plus".equals(op)){
				expected=val1+val2;
			}
			else if("Times".equals(op)){
				expected=val1*val2;
			}
			if(act!=expected){
				message=StateMessageWrong;
			}
		}
		catch(NumberFormatException bad){
			message=StateMessageError+" "+bad.getMessage();
			toChange.setStatus("ERROR");
		}
		toChange.setMessage(message);
		toChange.setFunction("NULL");
		setNewQuestionCookies(toChange);//store the fact that a new question will be asked next time to the client
	}
	/** utility function to set the cookies to notify the server that a new question should be asked
	 *  next time the function is run 
	 * @param toChange the SpRTResponse to change the cookies of
	 * @throws SpRTException if a failure occurs modifying toChange
	 */
	private static void setNewQuestionCookies(SpRTResponse toChange) throws SpRTException{
		CookieList cookielist=toChange.getCookieList();
		cookielist.add(StateMathCheckOperator,NewQCookie);
		cookielist.add(StateMathCheckValue1,NewQCookie);
		cookielist.add(StateMathCheckValue2,NewQCookie);
	}
	/** override of setNextResponse to handle math answer input
	 *  @see SpRTStateContext 
	 */
	@Override
	public void setNextResponse(SpRTStateContext context,
			SpRTRequest fromClient, SpRTResponse toChange) throws SpRTException {
		SpRTStateContext.clearStatus(toChange);
		if(context.validateFunction(fromClient,toChange, StateFunctionName)){
			checkCorrect(fromClient,toChange);
		}

	}

}
