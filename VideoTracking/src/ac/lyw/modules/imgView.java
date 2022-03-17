package ac.lyw.modules;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class imgView extends GlobalVar implements Runnable {
	private static final int WIDTH = 360, HEIGHT = 288, marginWIDTH = 16, marginHEIGHT = 39;
	private Frame originF = new Frame("Origin Video");
	private Frame processF = new Frame("Process");
	private Panel videoPanel = new Panel(new GridLayout(ROWNCOL, ROWNCOL));
	private Panel procPanel = new Panel(new GridLayout(ROWNCOL, ROWNCOL));
	private Panel opticalPanel = new Panel(new GridLayout(ROWNCOL, ROWNCOL));
	private videoCanvas[] currentCanvas = new videoCanvas[VIDEO_SIZE];
	private videoCanvas[] processCanvas = new videoCanvas[VIDEO_SIZE];
	private videoCanvas[] opticalFlowCanvas = new videoCanvas[VIDEO_SIZE];

	private VideoCapture[] vcTempBuf = new VideoCapture[VIDEO_SIZE];
	private ArrayList<List<Mat>> currentMat = new ArrayList<List<Mat>>(VIDEO_SIZE);
	private ArrayList<List<Mat>> processMat = new ArrayList<List<Mat>>(VIDEO_SIZE);
	
	/*	private Mat[][] currentMat = new Mat[VIDEO_SIZE][BUF_SIZE];
	private Mat[][] procMat = new Mat[VIDEO_SIZE][BUF_SIZE];
	private Mat[] resultMat = new Mat[VIDEO_SIZE];*/
	/*	private Mat[] currentMat = new Mat[VIDEO_NUMBER];
	private Mat[] preProcMat = new Mat[VIDEO_NUMBER];
	private Mat[] procMat = new Mat[VIDEO_NUMBER];*/


	public imgView(VideoCapture[] vcBuf) {
		this.vcTempBuf = vcBuf.clone();
		//		// Mat initialize
		loadFirstMat();
		// Canvas initialize
		initVideoCanvas(currentMat, currentCanvas);
		initVideoCanvas(processMat, processCanvas);

		initFrame();
		addListeners(originF);
		addListeners(processF);
		//addListeners(preProcF);
	}

	//----------------------------------------------------------------------------------------
	private void loadFirstMat() {
		//       SIZE,         SRC,            DST
		initMat();
		
		for(int i = 0; i < VIDEO_SIZE; i++) {
			for(int j = 0; j < SKIP_SIZE; j++) {
				vcTempBuf[i].read(new Mat());
			}
		}
		currentFrameCnt+= SKIP_SIZE;
		for(int i = 0; i < VIDEO_SIZE; i++) {
			for(int j = 0; j < BUF_SIZE; j++) {
				vcTempBuf[i].read(getMat(i, j, currentMat)); // load First image
			}
		}
		for(int i = 0; i < VIDEO_SIZE; i++) {
			for(int j = 0; j < BUF_SIZE; j++) {
				processMat.get(i).set(j, v_api.getSegmentation(getMat(i, j, currentMat))); // process First image
			}
		}
		currentFrameCnt += BUF_SIZE;
	}
	//----------------------------------------------------------------------------------------
	private void initMat() {
		for(int i = 0; i < VIDEO_SIZE; i ++) {
			initMat(getListOfMat(), currentMat); 		// initialize null Mat into currentMat
			initMat(getListOfMat(), processMat);		// initialize null Mat into processMat
		}
	}
	//----------------------------------------------------------------------------------------
	private void initMat(List<Mat> src, List<List<Mat>> dst) {
		for(int i = 0; i < BUF_SIZE; i++) {
			src.add(new Mat());
		}
		dst.add(src);
	}
	//----------------------------------------------------------------------------------------
	private void initVideoCanvas(List<List<Mat>> src, videoCanvas[] dst) {
		for(int i = 0; i < VIDEO_SIZE; i++) {
			dst[i] = new videoCanvas(getMat(i, 1, src));
		}
	}
	//----------------------------------------------------------------------------------------
	private List<Mat> getListOfMat() {
		List<Mat> temp = new ArrayList<Mat>(BUF_SIZE); 
		return temp;
	}
	//----------------------------------------------------------------------------------------
	private Mat getMat(int i, int j, List<List<Mat>> m) {
		return m.get(i).get(j);
	}
	//----------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------
	private void initFrame() {
		initFrame(originF, currentCanvas, videoPanel, 1, 1);
		initFrame(processF, processCanvas, procPanel, 1, 1);
	}

	private void initFrame(Frame f, videoCanvas[] vc, Panel dst, int row, int col) {
		/*
		 * Video Size -> width : 360, height : 288
		 * window margin -> width : 16, height : 39
		 * */
		f.setLocation(200, 300);
		f.setSize((WIDTH*row)+marginWIDTH, (HEIGHT*col)+marginHEIGHT);
		f.setLayout(bordLay);
		addVC2Panel(vc, dst);
		f.add(dst);
		f.setVisible(true);
	}
	private void addVC2Panel(videoCanvas[] vc, Panel dst) {
		for(int i = 0; i < VIDEO_SIZE; i++) {
			dst.add(vc[i]);
		}
	}
	private void addListeners(Frame f) {
		f.addWindowListener(new WindowListener() {
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
				f.setVisible(false);
				f.dispose();
			}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
		});
	}
	//----------------------------------------------------------------------------------------
	// next() overloading
	public void next() {
		next(currentMat, currentCanvas, false);
		next(processMat, processCanvas, true);
		System.out.println("pass");
		/*		currentFrameCnt++;
		next(currentCanvas, currentMat);
		nextSubtractSeg();
		System.out.println(BUF_POINT);
		BUF_POINT++;*/
	}
	private void next(List<List<Mat>> src, videoCanvas[] vc, boolean flag) {
		if(!flag) {
			for(int i = 0; i < VIDEO_SIZE; i++) {
				src.get(i).remove(0);
				src.get(i).add(readImage(i, vcTempBuf));
				vc[i].next(src.get(i).get(2));
				vc[i].repaint();
				src.get(i).size();
			}
		} else if(flag){
			for(int i = 0; i < VIDEO_SIZE; i++) {
				Mat temp = new Mat();
				src.get(i).remove(0);
				temp = v_api.opticalFlowTracking(v_api.getSegmentation(getMat(i, 1, currentMat)), v_api.getSegmentation(getMat(i, 2, currentMat)), v_api.getMeanShiftImg(getMat(i, 2, currentMat)));
				LOG.append("\n-------------------------------\n");
//				temp = v_api.getSegmentation(getMat(i, 1, currentMat));
				vc[i].next(temp);
				src.get(i).add(getMat(i, 9, currentMat));
				vc[i].repaint();
				src.get(i).size();
			}
		}
	}
/*	public void changeImg() {
		for(int i = 0; i < VIDEO_SIZE; i++) {
//			processCanvas[i].next(v_api.getSegmentation(getMat(i, 1, processMat)));
			processCanvas[i].next(v_api.opticalFlowTracking(v_api.getSegmentation(getMat(i, 1, currentMat)), v_api.getSegmentation(getMat(i, 2, currentMat)), v_api.getMeanShiftImg(getMat(i, 1, currentMat))));
		}
	}*/
	//private void next(List<List<Mat>> src, Mat)
	private Mat readImage(int index, VideoCapture[] v) {
		Mat temp = new Mat();
		v[index].read(temp);
		return temp;
	}
	//----------------------------------------------------------------------------------------
	/*	public void nextSubtractSeg() {
		for(int i = 0; i < VIDEO_SIZE; i++) {
			resultMat[i] = v_api.getSubtract(procMat[i][BUF_POINT]);
		} 
		next(processCanvas, resultMat);
	}*/
	//----------------------------------------------------------------------------------------
	@Override
	public void run() {
		while(true) {
			while(playState) {
				for(int i = 0; i < 1; i++) {
					next();
				}
				currentFrameCnt++;
				LOG.append("Current Frame"+ currentFrameCnt + "\n");
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(99999999);
			} catch (InterruptedException e) {}
		}
	}
	private class videoCanvas extends Canvas {
		private static final long serialVersionUID = 173102047345361287L;
		private Mat videoFrame;
		private BufferedImage bufImg = null;
		public videoCanvas(Mat videoFrame) {
			this.videoFrame = videoFrame;
		}
		public void next(Mat videoFrame) {
			this.videoFrame = videoFrame;
			repaint();
		}
		@Override
		public void paint(Graphics g) {
			// TODO Auto-generated method stub
			bufImg = mat2Img(videoFrame);
			g.drawImage(bufImg, 0, 0, this);
		}
	}
	
	private BufferedImage mat2Img(Mat img) {
		BufferedImage out;
		byte[] data = new byte[360 * 288 * (int)img.elemSize()];
		int type;
		img.get(0, 0, data);
		if(img.channels() == 1)
			type = BufferedImage.TYPE_BYTE_GRAY;
		else
			type = BufferedImage.TYPE_3BYTE_BGR;
		out = new BufferedImage(360, 288, type);

		out.getRaster().setDataElements(0, 0, 360, 288, data);
		return out;
	}
}