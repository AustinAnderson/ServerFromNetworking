/*Austin Anderson
 * SpRTState.java
 */
package SpRT.app;

import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;
import SpRT.protocol.SpRTException;
/** provides an interface to quickly move states
 * 
 * @author Austin Anderson
 *
 */
public interface SpRTState {
	/** this function should be implemented such that the context contains a state that
	 *  implements this interface as its context unless the state is the last state
	 * @param context the state context containing the next state to use 
	 * @param fromClient the SpRTRequest received 
	 * @param toChange the SpRTResponse to change
	 * @throws SpRTException if a problem occurs modifying fromClient or toChange
	 */
	void setNextResponse(SpRTStateContext context,SpRTRequest fromClient,SpRTResponse toChange)throws SpRTException;
}
