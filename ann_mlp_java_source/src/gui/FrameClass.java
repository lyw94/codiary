package gui;

import proc.ImgProcess;
import proc.mlProc;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Button;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class FrameClass implements Runnable{

	// Variable
	private String url;

	// GUI Container or Listener
	private Frame mFrame = new Frame("openCV in Clojure");
	Frame choiceType = new Frame("Choose ML Algorithm Type");
	actionEventListen ael = new actionEventListen();
	chooseMlListener cmlL = new chooseMlListener();

	// GUI Component
	private Panel topPanel = new Panel();
	private Panel eastPanel = new Panel();
	private Panel mlTypePn = new Panel();
	private Button[] mlTypes = new Button[3];
	private Button[] topBtn = new Button[4];
	private Button[] eastBtn = new Button[4];
	FileDialog FD = new FileDialog(mFrame);

	// Image preProcess
	private static Canvas2Image c2i;
	private static ImgProcess imgProc;

	// Machine Learning
	private mlProc mlp = new mlProc();

	// Thread instance
	private Thread imageLoadThread;

	// Frame Utility
	public void FrameDefaultSet() {
		mFrame.setSize(1280, 720);
		mFrame.setLayout(new BorderLayout(0, 0));
		mFrame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}
			@Override
			public void windowClosing(WindowEvent e) {
				mFrame.setVisible(false);
				mFrame.dispose();

			}
			@Override
			public void windowClosed(WindowEvent e) {
				mFrame.setVisible(false);
				mFrame.dispose();
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowActivated(WindowEvent e) {
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}

		});

		choiceType.setSize(320, 160);
		choiceType.setBackground(new Color(0, 0, 255));
		choiceType.setLocation(mFrame.getWidth()/2-100, mFrame.getHeight()/2-100);
		choiceType.addWindowListener(new WindowListener() {
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
				choiceType.setVisible(false);
				choiceType.dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				choiceType.setVisible(false);
				choiceType.dispose();
			}

			@Override
			public void windowActivated(WindowEvent e) {}
		});

		buttonInit();
		addFrame();
	}
	private void addFrame() {
		// TopPanel of Buttons ADD
		mFrame.add(topPanel, "North");
		mFrame.add(eastPanel,"East");
		choiceType.add(mlTypePn);

	}
	public void showFrame(boolean bool) {
		mFrame.setVisible(bool);
	}
	private void closeFrame(boolean bool) {
		mFrame.setVisible(bool);
		mFrame.dispose();
	}

	// Add Buttons on screen
	private void buttonInit() {
		topBtn[0] = new Button("MENU");
		topBtn[1] = new Button("LOAD IMAGE");
		topBtn[2] = new Button("IMAGE RELEASE");
		topBtn[3] = new Button("EXIT");

		eastBtn[0] = new Button("PreProcess");
		eastBtn[1] = new Button("Select ML Algorithm");
		eastBtn[2] = new Button("Train (only Ones)");
		eastBtn[3] = new Button("predict");

		mlTypes[0] = new Button("ANN_MLP");
		mlTypes[1] = new Button("K-NN");
		mlTypes[2] = new Button("SVM");


		// topBtn add Listener
		for(int i = 0; i < topBtn.length; i++) {
			topBtn[i].addActionListener(ael);
		}
		// eastBtn add Listener
		for(int i = 0; i < eastBtn.length; i++) {
			eastBtn[i].addActionListener(ael);
		}
		//mlTypes add Listener
		for(int i = 0; i < mlTypes.length; i++) {
			mlTypes[i].addActionListener(cmlL);
		}

		// top Panel add
		topPanel.setLayout(new GridLayout(1, 4));
		for(int i = 0; i < topBtn.length; i ++) {
			topPanel.add(topBtn[i]);
		}
		// east Panel add
		eastPanel.setLayout(new GridLayout(4, 1));
		for(int i = 0; i < eastBtn.length; i++) {
			eastPanel.add(eastBtn[i]);
		}
		//mlTypes Panel add
		mlTypePn.setLayout(new GridLayout(3, 1));
		for(int i = 0; i < mlTypes.length; i++) {
			mlTypePn.add(mlTypes[i]);
		}

	}


	// Class for EventListener
	class actionEventListen implements ActionListener {  // for main Frame

		boolean flag = false;
		Dialog dg;

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource().equals(topBtn[0])) { // Menu Button

			}
			if(e.getSource() == topBtn[1]) { // load Image Button
				try {
					if(!flag) {
						if(!imageLoadThread.isAlive()) { 
							url = showFileDialog();
							c2i = new Canvas2Image();
							imgProc = new ImgProcess(url); // fix issue
							mFrame.add(c2i, "Center");
							mFrame.validate();
							flag = true;
						} else {
							System.out.println("이미지 사전 로딩이 진행중");
						}
					} else {
						url = showFileDialog();
						mFrame.remove(c2i);
						imgProc.imageChange(url); // 수정중인 부분
						c2i.repaint();
						mFrame.add(c2i, "Center");
					}
				}
				catch(Exception btn2) {}
			}
			if(e.getSource() == topBtn[2]) { // Release Image Button
				try {
					mFrame.remove(c2i);
				} catch(Exception btn2) {}
			}
			if(e.getSource() == topBtn[3]) { // Exit Button
				closeFrame(true);
			}
			if(e.getSource() == eastBtn[0]) { // PreProcess Button 수정해야 할곳
				imgProc.preProcessing();
				mFrame.remove(c2i);
				c2i.repaint();
				mFrame.add(c2i, "Center");
			}
			if(e.getSource() == eastBtn[1]) {
				choiceType.setVisible(true);
			}
			if(e.getSource() == eastBtn[2]) {
				if(mlp.mlType == mlp.ANN_MLP_FLAG) {
					Long t1 = System.currentTimeMillis();
					mlp.ann_run();
					Long t2 = System.currentTimeMillis();
					System.out.println("Time : "+ (t2 - t1) +" ms");
				}
			}
			if(e.getSource() == eastBtn[3]) {
				// TODO
				if(mlp.getMlType() == mlProc.ANN_MLP_FLAG) {
					int result = 0;
					System.out.println(imgProc.getMatRoi().rows()+", "+imgProc.getMatRoi().cols());
					result = mlp.ann_predict(imgProc.getMatRoi());
					dg = new Dialog(mFrame);
					dg.add(new Label(String.valueOf(result)));
					dg.setSize(50, 50);
					dg.setLocation(500, 400);
					dg.addWindowListener(new WindowListener() {
						
						@Override
						public void windowOpened(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowIconified(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowDeiconified(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowDeactivated(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowClosing(WindowEvent e) {
							dg.setVisible(false);
							dg.dispose();
						}
						
						@Override
						public void windowClosed(WindowEvent e) {
							dg.setVisible(false);
							dg.dispose();
						}
						
						@Override
						public void windowActivated(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
					dg.setVisible(true);
					
				}
				if(mlp.getMlType() == mlProc.KNN_FLAG) {
					mlp.knn_predict(imgProc.getMatRoi());
				}
			}
		}
	}
	class chooseMlListener implements ActionListener { // for choose ml Types !!

		@Override
		public void actionPerformed(ActionEvent e) {

			if(e.getSource() == mlTypes[0]) {
				mlp.trainMachanism(mlProc.ANN_MLP_FLAG);
				choiceType.setVisible(false);
			}
			if(e.getSource() == mlTypes[1]) {
				mlp.trainMachanism(mlProc.KNN_FLAG);
				choiceType.setVisible(false);
			}
			if(e.getSource() == mlTypes[2]) {
				mlp.trainMachanism(mlProc.SVM_FLAG);
				choiceType.setVisible(false);
			}

		}

	}

	//Class for Image Load
	class Canvas2Image extends Canvas {
		private static final long serialVersionUID = -5020981869696281268L;
		String url;
		@Override
		public void paint(Graphics g) {
			g.drawImage(imgProc.getCurrentBufImage(), 0, 0, getPreferredSize().width, getPreferredSize().height, null);
		}
		@Override
		public void update(Graphics g) {
			g.drawImage(imgProc.getCurrentBufImage(), 0, 0, getPreferredSize().width, getPreferredSize().height, null);
			repaint();
		}
	}
	public String showFileDialog() {
		FD.setVisible(true);
		String r_url = FD.getDirectory().toString().replace("\\", "\\\\") + FD.getFile().toString(); // return URL

		return r_url;
	}

	@Override
	public void run() {
		boolean imgLoadFlag = false;

		FrameDefaultSet();
		showFrame(true);
		if(!imgLoadFlag) {
			imageLoadThread = new Thread(mlp);
			imageLoadThread.start();
			imgLoadFlag = true;
		}
	}
}
