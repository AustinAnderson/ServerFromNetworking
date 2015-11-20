/*AllTests.java
* Author: Austin Anderson
* Date last modified: 3/18/15
*/
package SPA.protocol.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SPAMessageTests.class, SPAQueryTests.class,SPAResponseTests.class })
public class AllTests {

}
