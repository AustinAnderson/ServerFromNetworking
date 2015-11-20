/*testSPATimeStamp.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import SPA.protocol.SPAException;
import SPA.protocol.SPATimeStamp;

public class testSPATimeStamp {

	@Test
	public void timeStampNow() throws SPAException {
		SPATimeStamp test=new SPATimeStamp();
		test.setTimeStampNow();
		Date expected=new Date();
		assertEquals(expected,test.getNow());
	}
	@Test
	public void isZeroTime_true(){
		SPATimeStamp test=new SPATimeStamp();
		assertEquals(true,test.isZeroTime());
	}
	@Test//only counts if timeStampNow does
	public void isZeroTime_false() throws SPAException{
		SPATimeStamp test=new SPATimeStamp();
		test.setTimeStampNow();
		assertEquals(false,test.isZeroTime());
		
	}
	@Test
	public void encode() throws SPAException{
		SPATimeStamp test=new SPATimeStamp();
		assertArrayEquals(new byte[]{(byte)0,(byte)0,(byte)0,(byte)0},test.encode());
	}
	@Test//only counts if timeStampNow does
	public void decode() throws SPAException{
		byte[] testWith={(byte)23,(byte)25,(byte)120,(byte)100};
		int testWithInt=387545188;
		String array="";
		for(byte b:testWith){
			array+=(Integer.toHexString(b));
		}
		if(!array.equals(Integer.toHexString(testWithInt))){
			fail("testWith value is not the same as its byte form, test invalid");
		}//must pass this for test to be valid
		//this is not the test
		SPATimeStamp test=new SPATimeStamp();
		test.decode(testWith);
		assertEquals((long)testWithInt,test.getNow().getTime()/1000);
	}
	@Test//only counts if timeStampNow does
	public void decode_byteMoreThan127() throws SPAException{
		byte[] testWith={(byte)23,(byte)25,(byte)120,(byte)200};
		int testWithInt=387545288;
		String array="";
		for(byte b:testWith){
			array+=(Integer.toHexString(Byte.toUnsignedInt(b)));
		}
		if(!array.equals(Integer.toHexString(testWithInt))){
			fail("testWith value is not the same as its byte form, test invalid");
		}//must pass this for test to be valid
		//this is not the test
		SPATimeStamp test=new SPATimeStamp();
		test.decode(testWith);
		assertEquals((long)testWithInt,test.getNow().getTime()/1000);
	}
	
}
