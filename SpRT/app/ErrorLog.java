/*Austin Anderson
 * ErrorLog.java 
 */
package SpRT.app;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;

import SPA.protocol.SPAMessage;
import SpRT.protocol.SpRTMessage;
import SpRT.protocol.SpRTResponse;//needed for reflection

public class ErrorLog {
	private static String addressPort;
	private static Logger logger;
	
	public static void setLogger(Logger logr){
		logger=logr;
	}
	public static void setAddressPort(String adprt){
		addressPort=adprt;
	}
	private static void setUpLogger(){
    	try{//append to a file named connections.log
    		logger =Logger.getLogger("practical");
    		logger.setUseParentHandlers(false);
    		Handler handle=new FileHandler("connections.log",true);
    		handle.setFormatter(new SimpleFormatter());
    		logger.addHandler(handle);
    		ErrorLog.setLogger(logger);
    	}
    	catch(Exception bad){
    		System.err.println("Unable to initialize logger");
    		System.exit(1);//if there is no logger, there is nowhere to log, so just exit
    	}
	}
	/** internal general purpose log function
	 *  
	 *  logs SpRTMessages in the format
	 *  <clientIp>:<client port>-<thread id> [Sent: <SpRTResponse>|Received: <SpRTRequest>]
	 *  first makes a new logger if one is not present, then uses reflection to tell
	 *  if the given message is a request or response, and logs it accordingly.
	 *  
	 *  if terminate is set, ***client terminated will be added on the end
	 *  
	 *  if message is null, it will be ignored, otherwise it will be logged
	 *  to the log file as a separate entry with the same log level imediately following
	 *  the standard format
	 * 
	 * @param rcvdSent the SpRTRequest or SpRTResponse to log
	 * @param level the level to have the logger use
	 * @param message the optional message to use 
	 * @param terminate whether or not the client had to be terminated
	 */
    private static void log(SpRTMessage rcvdSent,Level level,String message,boolean terminate) {
    	if(logger==null){
    		setUpLogger();
    	}
    	StringBuilder builder=new StringBuilder();
    	builder.append(addressPort);
    	builder.append("-");
    	builder.append(Thread.currentThread().getId());
    	builder.append("[");
    	if(SpRTResponse.class.isInstance(rcvdSent)){
    		builder.append("Sent");
    	}
    	else{
    		builder.append("Received");
    	}
    	builder.append(": ");
    	builder.append(rcvdSent);
    	builder.append("] ");
    	if(terminate){
    		builder.append(" ***client terminated");
    	}
    	logger.log(level,builder.toString()+"\n");//logger is thread-safe so it is OK to use statically
    	if(message!=null){
    		logger.log(level,message+"\n");
    	}
    }
    public static void dispErr(String message) {
    	System.err.println(message);
    }
    /** logs error using the internal log method described above
     * 
     * @param sprt SpRTResponse or SpRTRequest to use
     * @param message optional message to use
     * @param terminate whether or not the client had to be terminated
     */
    public static void logErr(SpRTMessage sprt,String message,boolean terminate) {
    	log(sprt,Level.WARNING,message,terminate);
    }
    /** logs the SpRTMessage using the internal log method described above
     * 
     * @param sprt SpRTResponse or SpRTRequest to use
     */
    public static void logInf(SpRTMessage sprt) {
    	log(sprt,Level.INFO,null,false);
    }
    
    private static void log(SPAMessage rcvdSent,Level level,String message,boolean terminate) {
    	if(logger==null){
    		setUpLogger();
    	}
    	StringBuilder builder=new StringBuilder();
    	builder.append("In SPAServer: ");
    	builder.append(addressPort);
    	builder.append("-");
    	builder.append("[");
    	if(rcvdSent!=null&&!rcvdSent.isQuery()){
    		builder.append("Sent");
    	}
    	else{
    		builder.append("Received");
    	}
    	builder.append(": ");
    	builder.append(rcvdSent);
    	builder.append("] ");
    	if(terminate){
    		builder.append(" ***terminated");
    	}
    	logger.log(level,builder.toString()+"\n");//logger is thread-safe so it is OK to use statically
    	if(message!=null){
    		logger.log(level,message+"\n");
    	}
    }
    /** logs error using the internal log method described above
     * 
     * @param spa SPAResponse or SPAQuery to use
     * @param message optional message to use
     * @param terminate whether or not the client had to be terminated
     */
    public static void logErr(String spaAddressPort,SPAMessage spa,String message,boolean terminate) {
    	String old=addressPort;
    	setAddressPort(spaAddressPort);
    	log(spa,Level.WARNING,message,terminate);
    	setAddressPort(old);
    }
    /** logs the SPAMessage using the internal log method described above
     * 
     * @param spa SPAResponse or SPAQuery to use
     */
    public static void logInf(String spaAddressPort,SPAMessage spa) {
    	String old=addressPort;
    	setAddressPort(spaAddressPort);
    	log(spa,Level.INFO,null,false);
    	setAddressPort(old);
    }
    

}
