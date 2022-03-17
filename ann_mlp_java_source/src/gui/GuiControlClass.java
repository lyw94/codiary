package gui;

import proc.mlProc;

public class GuiControlClass {
	
	public static void main(String[] main) {
		System.loadLibrary("opencv_java300");
		Thread guiThread = new Thread(new FrameClass());
		guiThread.start();
	}
}
