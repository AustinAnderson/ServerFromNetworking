/*testSPAResponse.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;


import static org.junit.Assert.*;

import org.junit.Test;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAResponse;

public class testSPAResponse {
	private static final String correctToString="(Msg 0) SPA_v2 Response with errCode 0:"
			+ " 'App One' used 54 times, 'App Two' used 2 times,"
			+ " last application was run Tue Jan 26 09:34:26 CST 1971"; 
	private static final int[] dataAsInt={
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
	private static byte[] data=null;
	private static byte[] dataWithNull=null;
	private static byte[] dataShort=null;
	{
		dataWithNull=new byte[dataAsInt.length+2];
		data=new byte[dataAsInt.length];
		for(int i=0;i<dataAsInt.length;i++){
			dataWithNull[i]=(byte)dataAsInt[i];
			data[i]=(byte)dataAsInt[i];
		}
		dataShort=new byte[dataAsInt.length-4];
		for(int i=0;i<dataAsInt.length-4;i++){
			dataShort[i]=(byte)dataAsInt[i];
		}
	}
	
	@Test(expected=SPAException.class)
	public void decodeFailshort() throws SPAException{
		SPAResponse test=new SPAResponse();
		test.finishDecoding(dataShort);
	}
	@Test(expected=SPAException.class)
	public void decodeFailExtraData() throws SPAException{
		SPAResponse test=new SPAResponse();
		test.finishDecoding(SPAMessage.concat(data, new byte[]{'s'}));
	}
	@Test(expected=SPAException.class)
	public void decodeFailExtraDataAfterNull() throws SPAException{
		SPAResponse test=new SPAResponse();
		test.finishDecoding(SPAMessage.concat(dataWithNull,new byte[]{'e'}));
	}
	@Test
	public void decodePassExtraNULL() throws SPAException{
		SPAResponse test=new SPAResponse();
		test.finishDecoding(dataWithNull);
		assertEquals(correctToString,test.toString());
	}
	@Test
	public void decode() throws SPAException{
		SPAResponse test=new SPAResponse();
		test.finishDecoding(data);
		assertEquals(correctToString,test.toString());
	}
	@Test
	public void encode() throws SPAException{
		SPAResponse test=new SPAResponse();
		test.finishDecoding(data);
		assertArrayEquals(data,test.encodeNoHeader());
	}

}
