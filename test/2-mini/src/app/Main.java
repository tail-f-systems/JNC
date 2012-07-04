package app;

import com.tailf.inm.INMException;

import gen.Minimal;

public class Main {
	
	public int run() {
		try {
			Minimal.enable();
		} catch (INMException e) {
			System.err.println("Schema file not found.");
			return -1;
		}
		return 0;
	}

	/**
	 * @param args Ignored
	 */
	public static void main(String[] args) {
		Main main = new Main();
		System.out.println(main.run());
	}

}
