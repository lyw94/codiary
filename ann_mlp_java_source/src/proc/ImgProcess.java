package proc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.core.CvType;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Mat;

public class ImgProcess {
	// using Variable
	private final static int MIN_CONTOUR_AREA = 100;
	private int extIdx;
	private String ext;
	// core image Variables
	private BufferedImage originBufferImg;
	private Mat originMatImg = new Mat();
	private BufferedImage currentBufImage;
	private Mat currentMatImage = new Mat();

	//Mat necessary image processing
	private Mat cvtMat = new Mat();
	private Mat gaussianMat = new Mat();
	private Mat threshMat = new Mat();
	private Mat RoI;

	//Mat For contours
	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	Mat hierarchy = new Mat();
	public ImgProcess(String url) {
		try {
			setOriginBufferImg(bufferedimageLoad(url));
			setCurrentMatImage(getOriginMatImg());
			setCurrentBufImage(getOriginBufferImg());
			System.out.println(getCurrentMatImage().rows()+", "+getCurrentMatImage().cols());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void imageChange(String url) { //fix issue
		try {
			setOriginBufferImg(bufferedimageLoad(url));
			setCurrentMatImage(getOriginMatImg());
			setCurrentBufImage(getOriginBufferImg());
			System.out.println(getCurrentMatImage().rows()+", "+getCurrentMatImage().cols());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void preProcessing() {
		Imgproc.cvtColor(getCurrentMatImage(), cvtMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(cvtMat, gaussianMat, new Size(5, 5), 0);
		Imgproc.adaptiveThreshold(gaussianMat, threshMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 3);
		Imgproc.findContours(threshMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		Rect boundingRect = new Rect();
		for(int i = 0; i < contours.size(); i++) {
			if (Imgproc.contourArea(contours.get(i)) > MIN_CONTOUR_AREA) {
				boundingRect = Imgproc.boundingRect(contours.get(i));
				//Imgproc.drawContours(getCurrentMatImage(), contours, i, new Scalar(0, 0, 255), 1);
				Imgproc.rectangle(getCurrentMatImage(), new Point(boundingRect.x, boundingRect.y), new Point(boundingRect.x + boundingRect.width, boundingRect.y+boundingRect.height), new Scalar(255, 0, 255));
			}
		}
		Imgproc.rectangle(getCurrentMatImage(), new Point(boundingRect.x, boundingRect.y), new Point(boundingRect.x + boundingRect.width, boundingRect.y+boundingRect.height), new Scalar(0, 0, 255));
		Imgproc.drawContours(getCurrentMatImage(), contours, -1, new Scalar(0, 255, 255));
		RoI = threshMat.submat(boundingRect);
		Imgproc.resize(RoI, RoI, new Size(16, 16));
		RoI.convertTo(RoI, CvType.CV_32F);
		RoI = RoI.reshape(1, 1);
		System.out.println(RoI.rows()+", "+RoI.cols());
		try {
			setCurrentBufImage(cvtMat2BufferedImage(getCurrentMatImage())); // Image Update
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Mat getMatRoi() {
		return RoI;
	}
	public BufferedImage bufferedimageLoad(String url) throws IOException {
		BufferedImage r_bufImg;
		Mat temp;
		try {
			temp = Imgcodecs.imread(url, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			extIdx = url.indexOf(".");
			ext = url.substring(extIdx);
			setOriginMatImg(temp);
			r_bufImg = cvtMat2BufferedImage(temp);
			return r_bufImg;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException();
		}
	}
	public Mat cvtBufferedImage2Mat(BufferedImage bufImg) {
		Mat r_img = new Mat(bufImg.getWidth(), bufImg.getHeight(), org.opencv.core.CvType.CV_8U);

		byte[] byteArray;

		byteArray = ((DataBufferByte)bufImg.getRaster().getDataBuffer()).getData();

		r_img.put(0, 0, byteArray);
		return r_img;
	}
	public BufferedImage cvtMat2BufferedImage(Mat matImg) throws IOException {
		BufferedImage r_img;

		MatOfByte matByte = new MatOfByte();
		if(ext.equals(".jpg"))
			Imgcodecs.imencode(".jpg", matImg, matByte);
		else if(ext.equals(".png"))
			Imgcodecs.imencode(".png", matImg, matByte);
		byte[] byteArray = matByte.toArray();
		InputStream in = new ByteArrayInputStream(byteArray);
		try {
			r_img = ImageIO.read(in);
			return r_img;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException();
		}
	}
	public BufferedImage getOriginBufferImg() {
		return originBufferImg;
	}
	public void setOriginBufferImg(BufferedImage originBufferImg) {
		this.originBufferImg = originBufferImg;
	}
	public Mat getOriginMatImg() {
		return originMatImg;
	}
	public void setOriginMatImg(Mat originMatImg) {
		this.originMatImg = originMatImg;
	}
	public BufferedImage getCurrentBufImage() {
		return currentBufImage;
	}
	public void setCurrentBufImage(BufferedImage currentBufImage) {
		this.currentBufImage = currentBufImage;
	}
	public Mat getCurrentMatImage() {
		return currentMatImage;
	}
	public void setCurrentMatImage(Mat currentMatImage) {
		this.currentMatImage = currentMatImage;
	}
}