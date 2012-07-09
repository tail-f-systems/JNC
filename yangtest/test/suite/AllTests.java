package suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    cont.ClientTest.class,
    util.AbstractClientTest.class,
})
public class AllTests {
    // Add tests to the SuiteClasses annotation to run them!
}
