package ac.lyw.modules;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;

class GlobalVar {

	// Default Variables -------------------------------------------------------------------
	protected static int CT_VIDEO_CNT = 0;
	protected final static int VIDEO_SIZE = 1;
	protected final static int STEP_SIZE = 1;
	// merge and separate frame count 550
	// disappear frame count 420;
	// generate frame count 480;
	protected final static int SKIP_SIZE = 470;
	protected static final int BUF_SIZE = 10;
	protected final static int ROWNCOL = 1;
	protected static int currentFrameCnt = 0;
	protected static boolean playState = false;
	protected boolean isFirst = true;
	public static boolean firstFrame = false;
	public static boolean isBoxing = true;
	public static int currentDrawLineCnt = 0;
	public static int currentDrawLabelCnt = 0;
	public static int prevDrawLabelCnt = 0;
	public static int symbolCnt = 1;
	// Frame Variables -------------------------------------------------------------------
	protected BorderLayout bordLay = new BorderLayout();
	public static TextArea LOG = new TextArea(30, 1);
	VideoApi v_api = new VideoApi();

}

class RoiStats {
	int area, left, top, width, height;
	int roiCnt;
	Point centroids;
	Point startRectPt;
	Point endRectPt;
	Rect Roi;
	Point vecPt1;
	Point vecPt2;
	boolean merged = false;
	int haveVector;
	List<Integer> vectorAddr = new ArrayList<Integer>();
	List<String> symbol = new ArrayList<String>(30);
	RoiStats(int area, int left, int top, int width, int height, double cx, double cy) {
		this.area = area;
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.centroids = new Point(cx, cy);
		this.startRectPt = new Point(left, top);
		this.endRectPt = new Point(left+width, top+height);
		
		Roi = new Rect(startRectPt, endRectPt);
	}
	RoiStats(Point vecPt1, Point vecPt2) {
		this.vecPt1 = vecPt1;
		this.vecPt2 = vecPt2;
	}
	void setSymbol(String i) {
		symbol.add(i);
	}
	String getSymbol() {
		String temp = "";
		for(int i = 0; i < symbol.size(); i++) {
			temp = temp +symbol.get(i);
		}
		return temp;
	}
	void addVectorCnt() {
		haveVector++;
	}
}
