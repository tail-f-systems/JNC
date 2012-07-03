package gen;

import static org.junit.Assert.*;

import org.junit.Test;

import app.Main;

public class MainTest {

	@Test
	public void testMultiply() {
		Main tester = new Main();
		assertEquals("Result", 50, tester.multiply(10, 5));
	}

}
