package ac.lyw.modules;

import java.awt.Button;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class MainControl extends GlobalVar implements Runnable {
	//Member Variables
	private final static int BTN_NUMBER = 4;
	private imgView videoF;
	private VideoCapture[] vcBuf = new VideoCapture[VIDEO_SIZE];
	private Frame mainF = new Frame("Tracking Video");
	private actionListener AL = new actionListener();
	private Thread playThread;
	private Button[] btns = new Button[BTN_NUMBER];
	private Panel btPanel = new Panel(new GridLayout(1, 4));
	private FileDialog FD = new FileDialog(mainF);


	private void initFrame() {
		mainF.setSize(400, 300);
		mainF.setLayout(bordLay);

		// Button allocate
		for (int i = 0; i < btns.length; i++) {
			btns[i] = new Button();
		}
		btns[0].setLabel("Load");
		btns[1].setLabel("NEXT IMAGE");
		btns[2].setLabel("Play&Stop");
		btns[3].setLabel("Change Image Type");

		// add buttons of btPanel
		for (int i = 0; i < btns.length; i++) {
			btPanel.add(btns[i]);
		}
		mainF.add(LOG, "Center");
		mainF.add(btPanel, "South");

		addListeners();


	}
	// Methods
	private void addListeners() {
		//MainFrame WindowListener
		mainF.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {
				mainF.setVisible(false);
				mainF.dispose();
				try {
					playThread.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		//addActionListener
		for (int i = 0; i < btns.length; i++) {
			btns[i].addActionListener(AL);
		}
	}
	private String getURL() {
		String r_url;
		FD.setVisible(true);
		r_url = FD.getDirectory().toString() + FD.getFile().toString();
		return r_url;
	}
	private void videoLoadAndSave() {
		if(CT_VIDEO_CNT < VIDEO_SIZE) {
			String url = getURL();
			vcBuf[CT_VIDEO_CNT] = new VideoCapture(url);
			LOG.append("Success Load Video\n");
			LOG.append(url+"\n");
			LOG.append("Current Frame Number is "+ CT_VIDEO_CNT +"\n");
			CT_VIDEO_CNT++;
			if(CT_VIDEO_CNT == VIDEO_SIZE) {
				videoF = new imgView(vcBuf);
				playThread = new Thread(videoF);
			}
		}

	}
	private void playByOneFrame() {
		if(playState == false) {
			if(currentFrameCnt < vcBuf[0].get(Videoio.CAP_PROP_FRAME_COUNT)) {
				LOG.append("Current Frame"+ currentFrameCnt + "\n");
				int tempCntF = 0;
				while(tempCntF < STEP_SIZE) {
					videoF.next();
					tempCntF++;
					currentFrameCnt++;
				}

			} else {
				LOG.append("This Video is a end..\n");
			}
		}
	}
	private void playAndStop() {
		if(isFirst) {
			playState = true;
			playThread.start();
			isFirst = false;
			return;
		}
		playState = !playState;
		LOG.append("isPlay? : "+playState+"\n");
		if(playState == true) {
			playThread.checkAccess();
			playThread.interrupt();
		}
	}
	private void changeImg() {
//		videoF.changeImg();
	}
	@Override
	public void run() {
		initFrame();
		mainF.setVisible(true);
	}


	// Mouse Action Listener
	private class actionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == btns[0]) {
				videoLoadAndSave();
			}
			if(e.getSource() == btns[1]) {
				playByOneFrame();
			}
			if (e.getSource() == btns[2]) {
				playAndStop();
			}
			if (e.getSource() == btns[3]) {
				changeImg();
			}
		}
	}


}
