/*testAppEntry.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;

import static org.junit.Assert.*;

import org.junit.Test;

import SPA.protocol.AppEntry;
import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;

public class testAppEntry{
	public static final byte[] useCount={2,6};
	public static final int useCountInt=518;
	public static final byte[] name={(byte)'t',(byte)'e',(byte)'s',(byte)'t',(byte)' ',(byte)'n',(byte)'a',(byte)'m',(byte)'e'};
	public static final byte[] nameShort={(byte)'t',(byte)'e',(byte)'s',(byte)'t',(byte)' ',(byte)'n',(byte)'a'};
	public static final byte[] nameSize={(byte)9};
	public static final byte[] messageExactSize=SPAMessage.concat(SPAMessage.concat(useCount, nameSize),name);
	public static final byte[] messageShort=SPAMessage.concat(SPAMessage.concat(useCount, nameSize),nameShort);
	public static final byte[] messageNullBytes=SPAMessage.concat(messageExactSize,new byte[2]);
	public static final byte[] messageShortNullBytes=SPAMessage.concat(messageShort,new byte[2]);
	public static final byte[] mesDouble=SPAMessage.concat(messageExactSize,messageExactSize);
	@Test
	public void decode() throws SPAException {
		AppEntry test=new AppEntry(messageNullBytes);
		assertArrayEquals(new Object[]{useCountInt,(int)nameSize[0],name,messageExactSize.length},
				new Object[]{test.getCount(),test.getNameLength(),test.getName(),test.getSize()});
	}
	@Test
	public void decodeExact() throws SPAException {
		AppEntry test=new AppEntry(messageExactSize);
		assertArrayEquals(new Object[]{useCountInt,(int)nameSize[0],name,messageExactSize.length},
				new Object[]{test.getCount(),test.getNameLength(),test.getName(),test.getSize()});
	}
	@Test
	public void decode_second() throws SPAException {
		AppEntry test=new AppEntry(mesDouble,messageExactSize.length);
		assertArrayEquals(new Object[]{useCountInt,(int)nameSize[0],name,messageExactSize.length},
				new Object[]{test.getCount(),test.getNameLength(),test.getName(),test.getSize()});
	}
	@Test(expected=SPAException.class)
	public void decode_WrongSizeBig() throws SPAException {
		@SuppressWarnings("unused")//just checking to make sure the constructor throws
		AppEntry test=new AppEntry(SPAMessage.concat(SPAMessage.concat(useCount, new byte[]{100}),name));
	}
	@Test(expected=SPAException.class)
	public void decode_WrongSizeSmall() throws SPAException {
		@SuppressWarnings("unused")//just checking to make sure the constructor throws
		AppEntry test=new AppEntry(messageShort);
	}
	@Test(expected=SPAException.class)
	public void decode_WrongSizeSmallnull() throws SPAException {
		@SuppressWarnings("unused")//just checking to make sure the constructor throws
		AppEntry test=new AppEntry(messageShortNullBytes);
	}
	//only valid if decode passes
	@Test
	public void encode() throws SPAException {
		AppEntry test=new AppEntry(messageExactSize);
		byte[] actual=test.encode();
		assertArrayEquals(messageExactSize,actual);
	}
	@Test
	public void testToString() throws SPAException {
		String expected="'test name' used 518 times";
		AppEntry test=new AppEntry(messageExactSize);
		assertEquals(expected,test.toString());
	}

}
