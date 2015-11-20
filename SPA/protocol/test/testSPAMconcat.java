/*testSPAMconcat.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;

import static org.junit.Assert.*;
import org.junit.Test;
import SPA.protocol.SPAMessage;


public class testSPAMconcat {

	@Test
	public void concat_full_full() {
		byte[] a={1,2,3,4};
		byte[] b={4,3,2,1};
		byte[] aPlusB={1,2,3,4,4,3,2,1};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_empty_full() {
		byte[] a={};
		byte[] b={4,3,2,1};
		byte[] aPlusB={4,3,2,1};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_full_empty() {
		byte[] a={1,2,3,4};
		byte[] b={};
		byte[] aPlusB={1,2,3,4};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_empty_empty() {
		byte[] a={};
		byte[] b={};
		byte[] aPlusB={};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_full_null() {
		byte[] a={1,2,3,4};
		byte[] b=null;
		byte[] aPlusB={1,2,3,4};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_null_full() {
		byte[] a=null;
		byte[] b={4,3,2,1};
		byte[] aPlusB={4,3,2,1};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_empty_null() {
		byte[] a={};
		byte[] b=null;
		byte[] aPlusB={};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_null_empty() {
		byte[] a=null;
		byte[] b={};
		byte[] aPlusB={};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_null_null(){
		byte[] a=null;
		byte[] b=null;
		byte[] aPlusB=null;
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
	@Test
	public void concat_single_single(){
		byte a=3;
		byte b=3;
		byte[] aPlusB={3,3};
		assertArrayEquals(aPlusB,SPAMessage.concat(a, b));
	}
}
