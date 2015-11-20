/*testVQE.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;

import static org.junit.Assert.*;

import org.junit.Test;

import SPA.protocol.versionQrErrcode;
import SPA.protocol.SPAException;

public class testVQE{
	final static byte goodQuery=(byte)0b0010_0_000;
	final static byte badmessageQuery=(byte)0b0010_0_011;
	final static byte goodResponse=(byte)0b0010_1_000;
	final static byte badmessageResponse=(byte)0b0010_1_011;
	final static byte badErr=(byte)0b0010_0_111;
	final static byte badVersion=(byte)0b0110_0_000;
	
	@Test
	public void testDecodeGoodQuery() throws SPAException {
		versionQrErrcode test=new versionQrErrcode(goodQuery);
		assertArrayEquals(new byte[]{test.getVersion(),test.getQR(),test.getErrCode()},
				new byte[]{(byte)2,(byte)0,(byte)0});
	}
	@Test
	public void testDecodeBadQuery() throws SPAException {
		versionQrErrcode test=new versionQrErrcode(badmessageQuery);
		assertArrayEquals(new byte[]{test.getVersion(),test.getQR(),test.getErrCode()},
				new byte[]{(byte)2,(byte)0,(byte)3});
	}
	@Test
	public void testDecodeGoodResponse() throws SPAException {
		versionQrErrcode test=new versionQrErrcode(goodResponse);
		assertArrayEquals(new byte[]{test.getVersion(),test.getQR(),test.getErrCode()},
				new byte[]{(byte)2,(byte)1,(byte)0});
	}
	@Test
	public void testDecodeBadResponse() throws SPAException {
		versionQrErrcode test=new versionQrErrcode(badmessageResponse);
		assertArrayEquals(new byte[]{test.getVersion(),test.getQR(),test.getErrCode()},
				new byte[]{(byte)2,(byte)1,(byte)3});
	}
	@Test(expected=SPAException.class)
	public void testDecodeInvalidErr() throws SPAException{
		@SuppressWarnings("unused")//just testing to make sure that function throws
		versionQrErrcode test=new versionQrErrcode(badErr);
	}
	@Test(expected=SPAException.class)
	public void testDecodeInvalidVersion() throws SPAException{
		@SuppressWarnings("unused")//just testing to make sure that function throws
		versionQrErrcode test=new versionQrErrcode(badVersion);
	}
	//requires decode to work
	@Test
	public void testEncode() throws SPAException{
		versionQrErrcode test=new versionQrErrcode(badmessageResponse);
		assertEquals(badmessageResponse,test.encode());
	}
	@Test
	public void testToStringResponse() throws SPAException{
		String expected="SPA_v"+2+" Response with errCode "+3+": ";
		versionQrErrcode test=new versionQrErrcode(badmessageResponse);
		assertEquals(expected,test.toString());
	}
	@Test
	public void testToStringQuery() throws SPAException{
		String expected="SPA_v"+2+" Query with errCode "+3+": ";
		versionQrErrcode test=new versionQrErrcode(badmessageQuery);
		assertEquals(expected,test.toString());
	}

}
