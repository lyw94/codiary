package ac.lyw.trigger;

import ac.lyw.modules.MainControl;

public class Booter {

	public static void main(String[] args) {
		System.loadLibrary("opencv_java300");
		MainControl main_Ctr = new MainControl();
		Thread mainCTR = new Thread(main_Ctr);
		mainCTR.start();
	}
}
