/*testSPAMencode.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;

import static org.junit.Assert.*;
import org.junit.Test;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAQuery;
import SPA.protocol.SPAResponse;

public class testSPAMencode {
	private static final String correctRToString="(Msg 23) SPA_v2 Response with errCode 3:"
			+ " 'App One' used 54 times, 'App Two' used 2 times,"
			+ " last application was run Tue Jan 26 09:34:26 CST 1971"; 
	private static final String correctQToString="(Msg 83) SPA_v2 Query with errCode 0:"
			+ " Business 'Dummy Bussiness'";
	private static final int[] ResponsedataAsInt={
		0b0010_1_011,//version_qr_errCode
		23,//MsgID
	 	2,3,4,2,//time
		2,//application count
		//entry one
		    0,//app use count[0]
			54,//app use count[1]
			7,//app name length
			'A','p','p',' ','O','n','e',//app name
		//entry two 
		    0,//app use count[0]
			2,//app use count[1]
			7,//app name length
			'A','p','p',' ','T','w','o'//app name
	};
	//this allows me to not have to cast each byte by hand when initializing the test array
	private static byte[] Rdata;
	{
		Rdata=new byte[ResponsedataAsInt.length];
		for(int i=0;i<ResponsedataAsInt.length;i++){
			Rdata[i]=(byte)ResponsedataAsInt[i];
		}
	}
	private static final int[] QuerydataAsInt={
		0b0010_0_000,//version_qr_errCode
		83,//MsgID
	 	15,//business name length
		'D','u','m','m','y',' ','B','u','s','s','i','n','e','s','s'//business name
	};
	//this allows me to not have to cast each byte by hand when initializing the test array
	private static byte[] Qdata;
	{
		Qdata=new byte[QuerydataAsInt.length];
		for(int i=0;i<QuerydataAsInt.length;i++){
			Qdata[i]=(byte)QuerydataAsInt[i];
		}
	}

	@Test
	public void decode_Response() throws SPAException {
		SPAMessage message=SPAMessage.decode(Rdata);
		assertEquals(correctRToString,message.toString());
	}
	@Test
	public void decode_Query() throws SPAException{
		SPAMessage message=SPAMessage.decode(Qdata);
		assertEquals(correctQToString,message.toString());
	}
	@Test//only valid if decode works
	public void encode_Response() throws SPAException{
		SPAMessage message=SPAMessage.decode(Rdata);
		assertArrayEquals(Rdata,message.encode());
	}
	@Test//only valid if decode works
	public void encode_Query() throws SPAException{
		SPAMessage message=SPAMessage.decode(Qdata);
		assertArrayEquals(Qdata,message.encode());
	}
	@Test
	public void castToResponse_pass() throws SPAException{
		SPAMessage message=SPAMessage.decode(Rdata);
		SPAResponse test=message.castToResponse();
		assertEquals(message.toString(),test.toString());
	}
	@Test(expected=SPAException.class)
	public void castToResponse_fail() throws SPAException{
		SPAMessage message=SPAMessage.decode(Qdata);
		@SuppressWarnings("unused")//just testing to make sure it fails
		SPAResponse test=message.castToResponse();
	}
	@Test
	public void castToQuery_pass() throws SPAException{
		SPAMessage message=SPAMessage.decode(Qdata);
		SPAQuery test=message.castToQuery();
		assertEquals(message.toString(),test.toString());
	}
	@Test(expected=SPAException.class)
	public void castToQuery_fail() throws SPAException{
		SPAMessage message=SPAMessage.decode(Rdata);
		@SuppressWarnings("unused")//just testing to make sure it fails
		SPAQuery test=message.castToQuery();
	}

}
