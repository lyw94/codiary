package ac.lyw.modules;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

public class VideoApi {
	private static final int MERGE = 1;
	private static final int SEPARATE = 2;
	private static final int NONE_ACT = 0;
	private static final int ERROR = -1;
	List<RoiStats> vectorRoi;
	List<RoiStats> currentRoi;
	List<RoiStats> prevRoi;
	List<RoiStats> deliverRoi;
	List<RoiStats> disappearRoi = new LinkedList<RoiStats>();
	public Mat getSegmentation(Mat c_img) {
		Mat temp = new Mat();
		Mat morph = new Mat();
		Mat t_1 = new Mat();
		Mat r_1 = new Mat();
		Mat r_2 = new Mat();
		new Mat();
		new Mat();
		new Mat();
		new Mat();
		Mat dst = new Mat();
		Mat meanShiftMat = new Mat();
		Mat hsvMat = new Mat();
		Mat rgbMat = new Mat();
		Mat compareHsvMat = new Mat();
		Mat compareRgbMat = new Mat();
		Mat compareMat = new Mat();
		Mat viewHsv = new Mat();
		Mat viewRgb = new Mat();

		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7), new Point(5, 5));
		Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9), new Point(5, 5));
		temp = c_img.clone();
		//		System.out.println(temp.channels());
		//		System.out.println(temp.type());
		Imgproc.erode(temp, morph, kernel);
		Imgproc.dilate(morph, morph, kernel1);

		t_1 = morph.clone();
		r_1 = doPyrMeanShiftFiltering(t_1, 20, 40);
		Imgproc.GaussianBlur(r_1, r_1, new Size(7, 7), 3);
		meanShiftMat = r_1.clone();

		//hsv split
		Mat hsvTempMat = new Mat();
		Imgproc.cvtColor(meanShiftMat, hsvTempMat, Imgproc.COLOR_BGR2HSV);
		//hsv meanshift
		r_2 = hsvTempMat.clone();//doPyrMeanShiftFiltering(hsvTempMat, 20, 40);
		hsvMat = hsvInrange(hsvTempMat);
		//Imgproc.GaussianBlur(hsvMat, hsvMat, new Size(5, 5), 3);
		//rgb split
		rgbMat = rgbInrange(meanShiftMat);
		//Imgproc.GaussianBlur(rgbMat, rgbMat, new Size(5, 5), 3);

		//cvtColor && threshold
		viewHsv = hsvMat.clone();
		viewRgb = rgbMat.clone();
		Mat viewSum = new Mat();
		Core.add(viewRgb, viewHsv, viewSum);
		cvtHSV2BGR2GRAY(hsvMat, rgbMat);

		Imgproc.cvtColor(meanShiftMat, meanShiftMat, Imgproc.COLOR_BGR2GRAY, 1);
		Imgproc.threshold(meanShiftMat, meanShiftMat, 65, 255, Imgproc.THRESH_BINARY);

		//compare hsv && rgb
		Core.compare(hsvMat, meanShiftMat, compareHsvMat, Core.CMP_GE);
		Core.compare(rgbMat, meanShiftMat, compareRgbMat, Core.CMP_GE);
		Mat addMat = new Mat();
		Core.add(hsvMat, rgbMat, addMat);
		Core.subtract(addMat, compareHsvMat, compareHsvMat);
		Core.add(compareRgbMat, compareHsvMat, compareMat);

		// show result
		//Imgproc.threshold(compareMat, compareMat, 130, 255, Imgproc.THRESH_BINARY);
		dst = compareMat.clone();
		//		dst = r_1.clone();

		Mat labels = new Mat();
		Mat stats = new Mat();
		Mat centroids = new Mat();
		int labelcnt = Imgproc.connectedComponentsWithStats(dst, labels, stats, centroids, 8, CvType.CV_32S);
		List<RoiStats> tempRoi = drawLabel(dst, stats, centroids, labelcnt);
		orderByLeft(tempRoi, true);
		for(int i = 0; i < tempRoi.size(); i++) {
			//				GlobalVar.blobLabel.set(i, getLabel(i));
			Imgproc.rectangle(r_1, tempRoi.get(i).startRectPt, tempRoi.get(i).endRectPt, new Scalar(255, 255, 255));
			Imgproc.putText(r_1, "<"+(i+1)+">", new Point(tempRoi.get(i).centroids.x, tempRoi.get(i).centroids.y+20), 5, 0.6, new Scalar(255, 255, 0));

		}

		GlobalVar.currentDrawLabelCnt = 0;
		GlobalVar.currentDrawLineCnt = 0;

		return dst.clone();
	}

	public Mat getMeanShiftImg(Mat c_img) {
		Mat temp = new Mat();
		Mat morph = new Mat();
		Mat t_1 = new Mat();
		Mat r_1 = new Mat();
		Mat meanShiftMat = new Mat();

		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7), new Point(5, 5));
		Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9), new Point(5, 5));
		temp = c_img.clone();
		//		System.out.println(temp.channels());
		//		System.out.println(temp.type());
		Imgproc.erode(temp, morph, kernel);
		Imgproc.dilate(morph, morph, kernel1);

		t_1 = morph.clone();		
		r_1 = doPyrMeanShiftFiltering(t_1, 20, 40);
		Imgproc.GaussianBlur(r_1, r_1, new Size(7, 7), 3);
		meanShiftMat = r_1.clone();

		return meanShiftMat.clone();
	}
	public Mat opticalFlowTracking(Mat input_1, Mat input_2, Mat drawImg) {
		Mat result = new Mat();
		Mat prevImg = input_1.clone();
		Mat nextImg = input_2.clone();
		Mat prevLabels = new Mat();
		Mat prevStats = new Mat();
		Mat prevCentroids = new Mat();
		int prevCnt = Imgproc.connectedComponentsWithStats(prevImg, prevLabels , prevStats, prevCentroids , 8, CvType.CV_32S);
		Mat nextLabels = new Mat();
		Mat nextStats = new Mat();
		Mat nextCentroids = new Mat();
		int nextCnt = Imgproc.connectedComponentsWithStats(nextImg, nextLabels, nextStats, nextCentroids, 8, CvType.CV_32S);
		double[][] prevCentroid = labeling(prevStats, prevCentroids, prevCnt);
		Point[] prevPt = makePoint(prevCentroid);

		double[][] nextCentroid = labeling(prevStats, prevCentroids, prevCnt);
		Point[] nextPt = makePoint(nextCentroid);

		MatOfPoint2f prevPts = new MatOfPoint2f(prevPt);
		MatOfPoint2f nextPts = new MatOfPoint2f(nextPt);
		MatOfByte status = new MatOfByte();
		MatOfFloat err = new MatOfFloat();

		result = drawImg.clone();

		Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts, nextPts, status, err, new Size(20, 20), 10, new TermCriteria(TermCriteria.EPS+TermCriteria.COUNT, 20, 0.5), Video.OPTFLOW_USE_INITIAL_FLOW, 0.1);
		if(GlobalVar.currentFrameCnt == (GlobalVar.SKIP_SIZE+GlobalVar.BUF_SIZE)) {
			System.out.println("pass createsym");
			GlobalVar.firstFrame = true;
			prevRoi = prevDrawLabel(result, prevStats, prevCentroids, prevCnt);
		} else {
			prevRoi = currentRoi;
			for(int i = 0; i < prevRoi.size(); i++) {
				//Imgproc.rectangle(result, prevRoi.get(i).startRectPt, prevRoi.get(i).endRectPt, new Scalar(0, 255, 255));
			}
		}
		currentRoi = drawLabel(result, nextStats, nextCentroids, nextCnt);
		vectorRoi = drawVector(prevPts, nextPts);

		orderByLeft(currentRoi, true);
		orderByLeft(vectorRoi, false);
		if(GlobalVar.firstFrame) {
			createSymbol();
		}
		// check symbol state
		switch(checkMergeAndSeparate()) {
		case NONE_ACT:
			// move currentSymbol to prevSymbol
			for(int i = 0; i < currentRoi.size(); i++) {
				//				GlobalVar.blobLabel.set(i, getLabel(i));
				Imgproc.rectangle(result, currentRoi.get(i).startRectPt, currentRoi.get(i).endRectPt, new Scalar(255, 255, 255));
				Imgproc.putText(result, currentRoi.get(i).getSymbol(), new Point(currentRoi.get(i).centroids.x, currentRoi.get(i).centroids.y+20), 5, 0.6, new Scalar(255, 255, 0));

			}
			for(int i = 0; i < vectorRoi.size(); i++) {
				Imgproc.arrowedLine(result, vectorRoi.get(i).vecPt1, vectorRoi.get(i).vecPt2, new Scalar(255, 0, 0), 3, Imgproc.LINE_4, 0, 3);
				//Imgproc.putText(result, getLabel(i), new Point(vectorRoi.get(i).vecPt1.x, vectorRoi.get(i).vecPt1.y+20), 1, 3, new Scalar(255, 0, 255));
			}
			break;
		case MERGE:
			GlobalVar.LOG.append("merged\n");
			for(int i = 0; i < currentRoi.size(); i++) {
				drawRectangle(result, currentRoi.get(i).startRectPt, currentRoi.get(i).endRectPt, new Scalar(255, 255, 255));
				Imgproc.putText(result, currentRoi.get(i).getSymbol(), new Point(currentRoi.get(i).centroids.x, currentRoi.get(i).centroids.y+20), 5, 0.6, new Scalar(255, 255, 0));
			}
			for(int i = 0; i < vectorRoi.size(); i++)
				Imgproc.arrowedLine(result, vectorRoi.get(i).vecPt1, vectorRoi.get(i).vecPt2, new Scalar(255, 0, 0), 3, Imgproc.LINE_4, 0, 3);
			break;
		case SEPARATE:
			//GlobalVar.LOG.append("separated\n");
			for(int i = 0; i < currentRoi.size(); i++) {
				drawRectangle(result, currentRoi.get(i).startRectPt, currentRoi.get(i).endRectPt, new Scalar(255, 255, 255));
				Imgproc.putText(result, currentRoi.get(i).getSymbol(), new Point(currentRoi.get(i).centroids.x, currentRoi.get(i).centroids.y+20), 5, 0.6, new Scalar(255, 255, 0));
			}
			break;
		case ERROR:
			break;
		}
		//		System.out.println("nextPts Size : "+nextPts.size());
		System.out.println("Line Cnt : "+GlobalVar.currentDrawLineCnt+", currentLabel Cnt : "+GlobalVar.currentDrawLabelCnt+", prevCurrentLabelCnt : "+GlobalVar.prevDrawLabelCnt);
		GlobalVar.prevDrawLabelCnt = GlobalVar.currentDrawLabelCnt;
		GlobalVar.LOG.append("Have Symbols ¡å\n");
		for(int i = 0; i < currentRoi.size(); i++) {
			GlobalVar.LOG.append(currentRoi.get(i).getSymbol()+",");
		}
		GlobalVar.LOG.append("\ndisappeared Symbol ¡å\n");
		for(int i = 0; i < disappearRoi.size(); i++) {
			GlobalVar.LOG.append(disappearRoi.get(i).getSymbol()+",");
		}

		GlobalVar.currentDrawLineCnt = 0;
		GlobalVar.currentDrawLabelCnt = 0;
		if(GlobalVar.firstFrame == true) {
			GlobalVar.firstFrame = false;
		}
		return result.clone(); 
	}
	private void createSymbol() {
		for(int i = 0; i < currentRoi.size(); i++) {
			currentRoi.get(i).setSymbol("<"+String.valueOf(GlobalVar.symbolCnt)+">");
			GlobalVar.symbolCnt++;
		}
	}
	private void orderByLeft(List<RoiStats> input, boolean isRect) {
		if(isRect) {
			int min = 0;
			for(int i = 0; i < input.size()-1; i++) {
				min = i;
				for(int j = i+1; j < input.size(); j++) {
					if(input.get(j).left < input.get(min).left) {
						min = j;
					}
				}
				RoiStats temp = input.get(i);
				input.set(i, input.get(min));
				input.set(min, temp);
			}
		} else {
			int min = 0;
			for(int i = 0; i < input.size()-1; i++) {
				min = i;
				for(int j = i+1; j < input.size(); j++) {
					if(input.get(j).vecPt1.x < input.get(min).vecPt1.x) {
						min = j;
					}
				}
				RoiStats temp = input.get(i);
				input.set(i, input.get(min));
				input.set(min, temp);
			}
		}
	}
	private void drawRectangle(Mat dst, Point a, Point b, Scalar s) {
		Imgproc.rectangle(dst, a, b, s);
	}
	private int checkMergeAndSeparate() {
		// Merge and disappear
		if(GlobalVar.currentDrawLabelCnt < GlobalVar.prevDrawLabelCnt) {
			System.out.println("merge and disappear");
			//check merged
			int i = 0, j = 0;
			while(i < currentRoi.size()) {
				while(j < prevRoi.size()) {
					if(prevRoi.get(j).centroids.inside(currentRoi.get(i).Roi)) {
						for(int k = 0; k < prevRoi.get(j).symbol.size(); k++) {
							currentRoi.get(i).setSymbol(prevRoi.get(j).symbol.get(k));
						}
					}  else {
						break;
					}
					//					} else {
					//						disappearRoi.add(prevRoi.get(j));
					//						GlobalVar.LOG.append("disappeared\n");
					//						break;
					//					}
					j++;
				}
				i++;
			}
			return MERGE;
		}
		// Separate and generate
		else if(GlobalVar.currentDrawLabelCnt > GlobalVar.prevDrawLabelCnt) {
			System.out.println("separate and generation");
			int[] separateCnt = new int[currentRoi.size()];
			//separate
			for(int i = 0; i < currentRoi.size(); i++) {
				for(int j = 0; j < prevRoi.size(); j++) {
					if(currentRoi.get(i).centroids.inside(prevRoi.get(j).Roi)) {
						separateCnt[i]++;
						if((i+1 < currentRoi.size()) && currentRoi.get(i+1).centroids.inside(prevRoi.get(j).Roi)) {
							separateCnt[i]++;
						}
//						if((i == currentRoi.size()-1) && currentRoi.get(i-1).centroids.inside(prevRoi.get(j).Roi)) {
//							separateCnt[i]++;
//						}
					}
				}
			}
			System.out.println(Arrays.toString(separateCnt));
			int colCnt = 0;
			int rowCnt = 0;
			for(int h = 0; h < currentRoi.size(); h++) {
				if(separateCnt[h] == 1) {
					if(rowCnt < prevRoi.size()) {
						currentRoi.get(colCnt).symbol = prevRoi.get(rowCnt).symbol;
						rowCnt++;
						colCnt++;
					}
				}
				else if(separateCnt[h] > 1){
					System.out.println(separateCnt[1]+"");
					for(int t = 0; t < separateCnt[h]; t++) {
						currentRoi.get(colCnt).setSymbol(prevRoi.get(rowCnt).getSymbol()+"_"+t);
						colCnt++;
					}
					rowCnt++;
				}else if(separateCnt[h] == 0) {
					GlobalVar.LOG.append("generated\n");
					System.out.println(colCnt);
					currentRoi.get(colCnt).setSymbol("<"+GlobalVar.symbolCnt+">");
					GlobalVar.symbolCnt++;
					colCnt++;
				}
			}
			return SEPARATE;
		}
		// none act
		else if(GlobalVar.currentDrawLabelCnt == GlobalVar.prevDrawLabelCnt) {
			if(!GlobalVar.firstFrame) {
				for(int i = 0; i < currentRoi.size(); i++) {
					currentRoi.get(i).symbol = prevRoi.get(i).symbol;
				}
			}
			return NONE_ACT;
		}
		else return ERROR;
	}

	private Point[] makePoint(double[][] db) {
		Point[] temp = new Point[db.length];
		for(int i = 0; i < db.length; i++) {
			temp[i] = new Point(db[i][0], db[i][1]);
		}
		return temp;
	}
	private double[][] labeling(Mat stats, Mat centroids, int labelCnt) {
		double[][] pts = new double[labelCnt][2];
		for(int i = 1; i < labelCnt; i++) {
			int area = (int)stats.get(i, Imgproc.CC_STAT_AREA)[0];
			int left = (int)stats.get(i, Imgproc.CC_STAT_LEFT)[0];
			int top = (int)stats.get(i, Imgproc.CC_STAT_TOP)[0];
			int width = (int)stats.get(i, Imgproc.CC_STAT_WIDTH)[0];
			int height = (int)stats.get(i, Imgproc.CC_STAT_HEIGHT)[0];
			double[] centroid = centroids.get(i, 0);
			//			System.out.println("centroid"+Arrays.toString(centroids.get(i, 0))+", "+Arrays.toString(centroids.get(i, 1)));
			//			System.out.printf("width : %d, height : %d, area : %d \n", width, height, area);
			if(area > 200) {
				for(int j = 0; j < 2; j++) {
					pts[i][j] = centroids.get(i, j)[0];

				}
			}
		}
		return pts;
	}

	private List<RoiStats> drawVector(MatOfPoint2f prevPts, MatOfPoint2f nextPts) {
		List<RoiStats> temp = new ArrayList<RoiStats>();
		for(int j=0; j<nextPts.size().height; j++){
			Point nullPt = new Point(0, 0);
			double nptx = nextPts.get(j, 0)[0], npty = nextPts.get(j, 0)[1];
			double pptx = prevPts.get(j, 0)[0], ppty = prevPts.get(j, 0)[1];
			Point prevPoint = new Point(pptx, ppty);
			Point nextPoint = new Point(nptx, npty);
			//			System.out.print(Arrays.toString(nextPts.get(j, 0)));
			if(!new Point(pptx, ppty).equals(new Point(0, 0))) {
				//				Imgproc.arrowedLine(result, prevPoint, nextPoint, new Scalar(255, 0, 0), 3, Imgproc.LINE_4, 0, 7);
				if(!new Point(nptx, npty).equals(new Point(0, 0))) {
					temp.add(new RoiStats(prevPoint, nextPoint));
					//					Imgproc.arrowedLine(result, prevPoint, nextPoint, new Scalar(255, 0, 0), 3, Imgproc.LINE_4, 0, 7);
					GlobalVar.currentDrawLineCnt++;
				}
			}
		}
		return temp;
	}
	private List<RoiStats> drawLabel(Mat dst, Mat CurStats, Mat CurCentroids, int CurLabelCnt) {
		List<RoiStats> temp = new ArrayList<RoiStats>();
		for(int i = 1; i < CurLabelCnt; i++) {
			int area, left, top, width, height;
			double cx, cy;
			area = (int)CurStats.get(i, Imgproc.CC_STAT_AREA)[0]; // area
			left = (int)CurStats.get(i, Imgproc.CC_STAT_LEFT)[0]; // left
			top = (int)CurStats.get(i, Imgproc.CC_STAT_TOP)[0]; // top
			width = (int)CurStats.get(i, Imgproc.CC_STAT_WIDTH)[0]; // width
			height= (int)CurStats.get(i, Imgproc.CC_STAT_HEIGHT)[0]; // height
			cx = CurCentroids.get(i, 0)[0];
			cy = CurCentroids.get(i, 1)[0];
			if(area > 200) {
				//Imgproc.circle(dst, new Point(cx, cy), 1, new Scalar(255, 255, 0));
				RoiStats roitemp = new RoiStats(area, left, top, width, height, cx, cy);
				temp.add(roitemp);
				//				Imgproc.rectangle(dst, new Point(left, top), new Point(left+width, top+height), new Scalar(255, 255, 255));
				GlobalVar.currentDrawLabelCnt++;
			}
		}

		return temp;
	}
	private List<RoiStats> prevDrawLabel(Mat dst, Mat CurStats, Mat CurCentroids, int CurLabelCnt) {
		List<RoiStats> temp = new ArrayList<RoiStats>();
		for(int i = 1; i < CurLabelCnt; i++) {
			int area, left, top, width, height;
			double cx, cy;
			area = (int)CurStats.get(i, Imgproc.CC_STAT_AREA)[0]; // area
			left = (int)CurStats.get(i, Imgproc.CC_STAT_LEFT)[0]; // left
			top = (int)CurStats.get(i, Imgproc.CC_STAT_TOP)[0]; // top
			width = (int)CurStats.get(i, Imgproc.CC_STAT_WIDTH)[0]; // width
			height= (int)CurStats.get(i, Imgproc.CC_STAT_HEIGHT)[0]; // height
			cx = CurCentroids.get(i, 0)[0];
			cy = CurCentroids.get(i, 1)[0];
			if(area > 200) {
				Imgproc.circle(dst, new Point(cx, cy), 1, new Scalar(255, 255, 0));
				RoiStats roitemp = new RoiStats(area, left, top, width, height, cx, cy);
				temp.add(roitemp);
				//Imgproc.rectangle(dst, new Point(left, top), new Point(left+width, top+height), new Scalar(0, 255, 255));
				GlobalVar.prevDrawLabelCnt++;
			}
		}
		return temp;
	}
	private Mat doPyrMeanShiftFiltering(Mat img_1, int i, int j) {
		Mat result = new Mat();
		Mat t_1 = new Mat();
		Imgproc.pyrMeanShiftFiltering(img_1, t_1, i, j);
		result = t_1.clone();
		return result;
	}

	private Mat hsvInrange(Mat input) {
		List<Mat> tempList = new ArrayList<Mat>();
		new Mat();
		new Mat();
		Mat output = new Mat();
		Core.split(input, tempList );
		Core.inRange(tempList.get(0), new Scalar(70), new Scalar(80), tempList.get(0)); // h
		Core.inRange(tempList.get(1), new Scalar(100), new Scalar(255), tempList.get(1)); // s
		Core.inRange(tempList.get(2), new Scalar(0), new Scalar(90), tempList.get(2)/*value_1*/); // v

		//Core.add(tempList.get(1), tempList.get(2), output);
		Core.merge(tempList, output);

		return output.clone();
	}

	private Mat rgbInrange(Mat input) {
		List<Mat> tempList = new ArrayList<Mat>();
		new ArrayList<Mat>();
		Mat output = new Mat();
		Core.split(input, tempList);
		Core.inRange(tempList.get(0), new Scalar(0), new Scalar(70), tempList.get(0)); // r
		Core.inRange(tempList.get(1), new Scalar(0), new Scalar(70), tempList.get(1)); // g
		Core.inRange(tempList.get(2), new Scalar(0), new Scalar(70), tempList.get(2)); // b

		new Mat();
		Core.merge(tempList, output);
		//		Core.merge(tempList, o_tp);

		/*		Core.split(input, tempList2);
		Core.inRange(tempList2.get(0), new Scalar(60), new Scalar(80), tempList2.get(0));
		Core.inRange(tempList2.get(1), new Scalar(45), new Scalar(70), tempList2.get(1));
		Core.inRange(tempList2.get(2), new Scalar(45), new Scalar(70), tempList2.get(2));
		Mat o_tp2 = new Mat();
		Core.merge(tempList2, o_tp2);

		Core.subtract(o_tp, o_tp2, output);*/
		return output.clone();
	}

	private void cvtHSV2BGR2GRAY(Mat hsv, Mat rgb) {
		//hsv
		Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_HSV2BGR);
		Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_BGR2GRAY, 1);
		Imgproc.threshold(hsv, hsv, 20, 255, Imgproc.THRESH_BINARY);
		//rgb
		Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2GRAY, 1);
		Imgproc.threshold(rgb, rgb, 20, 255, Imgproc.THRESH_BINARY);
	}


}
