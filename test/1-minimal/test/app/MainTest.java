package app;

import static org.junit.Assert.*;

import org.junit.Test;

import app.Main;

public class MainTest {

	@Test
	public void testRun() {
		Main tester = new Main();
		assertEquals("Result", 0, tester.run());
	}

}
