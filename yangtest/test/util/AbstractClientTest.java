package util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.tailf.confm.Device;
import com.tailf.confm.DeviceUser;

public class AbstractClientTest {

    private Device dev[] = {null};
    private DeviceUser duser[] = {null};
    private Device emptydev[] = {};
    private DeviceUser emptyduser[] = {};

    @Test
    public void testInit() {
        try {
            util.Client.init(dev, null);
            fail("Expected nullpointer exception");
        } catch (AssertionError ae) {} 
          catch (NullPointerException npe) {}
        try {
            util.Client.init(null, duser);
            fail("Expected nullpointer exception");
        } catch (NullPointerException npe) {}
          catch (AssertionError ae) {}
        try {
            util.Client.init(null, null);
            fail("Expected nullpointer exception");
        } catch (NullPointerException npe) {}
          catch (AssertionError ae) {}
        try {
            util.Client.init(emptydev, duser);
            fail("Expected array index out of bounds exception");
        } catch (ArrayIndexOutOfBoundsException aioobe) {}
          catch (AssertionError ae) {}
        try {
            util.Client.init(dev, emptyduser);
            fail("Expected array index out of bounds exception");
        } catch (ArrayIndexOutOfBoundsException aioobe) {}
          catch (AssertionError ae) {}
        try {
            util.Client.init(emptydev, emptyduser);
            fail("Expected array index out of bounds exception");
        } catch (ArrayIndexOutOfBoundsException aioobe) {}
          catch (AssertionError ae) {}
        assertTrue(dev[0] == null);
        assertTrue(duser[0] == null);
        util.Client.init(dev, duser);
        assertFalse(dev[0] == null);
        assertFalse(duser[0] == null);
    }

}
