/*SPAQueryTests.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;

import static org.junit.Assert.*;

import org.junit.Test;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAQuery;

public class SPAQueryTests{
	public static final byte[] name={(byte)'t',(byte)'e',(byte)'s',(byte)'t',(byte)' ',(byte)'n',(byte)'a',(byte)'m',(byte)'e'};
	public static final byte[] nameSize={(byte)9};
	public static final byte[] data=(SPAMessage.concat(nameSize,name));
	public static byte[] dataWithNulls;
	{
		dataWithNulls=new byte[data.length+2];
		for(int i=0;i<data.length;i++){
			dataWithNulls[i]=data[i];
		}
	}

	@Test
	public void decode() throws SPAException {
		SPAQuery test=new SPAQuery();
		test.updateDecode(data);
		assertArrayEquals(new Object[]{nameSize[0],name},new Object[]{test.getNameLength(),test.getName()});
	}
	@Test
	public void decode_passExtraNull() throws SPAException {
		SPAQuery test=new SPAQuery();
		test.updateDecode(dataWithNulls);
		assertArrayEquals(new Object[]{nameSize[0],name},new Object[]{test.getNameLength(),test.getName()});
	}
	@Test(expected=SPAException.class)
	public void decode_failExtraData() throws SPAException {
		SPAQuery test=new SPAQuery();
		test.updateDecode(SPAMessage.concat(SPAMessage.concat(nameSize,name),new byte[]{'s'}));
	}
	@Test(expected=SPAException.class)
	public void decode_failExtraDataAfterNull() throws SPAException {
		SPAQuery test=new SPAQuery();
		test.updateDecode(SPAMessage.concat(dataWithNulls,new byte[]{'2'}));
	}
	@Test(expected=SPAException.class)
	public void decode_WrongSizeSmall() throws SPAException {
		SPAQuery test=new SPAQuery();
		test.updateDecode(SPAMessage.concat(new byte[]{2},name));
	}
	@Test(expected=SPAException.class)
	public void decode_WrongSizeBig() throws SPAException {
		SPAQuery test=new SPAQuery();
		test.updateDecode(SPAMessage.concat(new byte[]{100},name));
	}
	@Test
	public void encode() throws SPAException{
		SPAQuery test=new SPAQuery();
		test.updateDecode(data);
		assertArrayEquals(data,test.encodeNoHeader());
	}
	
}
