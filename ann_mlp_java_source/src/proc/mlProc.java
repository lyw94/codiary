package proc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;
import org.opencv.ml.StatModel;

public class mlProc implements Runnable {
	// Variables
	private static boolean LOADED = false;
	private final static int MIN_CONTOUR_AREA = 100;
	private final static int SAMPLE_SIZE = 3000;
	private final static int ATTRIBUTES = 256;
	private final static int CLASSES = 10;
	public int mlType;
	private ANN_MLP annetwork;
	private boolean annIsTrain = false;
	private boolean knnIsTrain = false;
	private boolean svmIsTrain = false;

	private KNearest knn;

	public final static int ANN_MLP_FLAG = 1;
	public final static int KNN_FLAG = 2;
	public final static int SVM_FLAG = 3;

	// ANN_MLP LAYERS Constant Variable
	private final static int HIDDEN_LAYER_1 = 50;
	private final static int HIDDEN_LAYER_2 = 60;
	private final static int HIDDEN_LAYER_3 = 40;

	//Mat necessary image processing
	private Mat training_set = new Mat();//new Mat(SAMPLE_SIZE, ATTRIBUTES, CvType.CV_32F); //new Mat(SAMPLE_SIZE, ATTRIBUTE, CvType.CV_32F);
	private Mat training_label = new Mat(SAMPLE_SIZE, CLASSES, CvType.CV_32F);
	private Mat predict_result = new Mat(1, 10, CvType.CV_32F);
	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	Mat hierarchy = new Mat();

	private void loadTrainingImg() {
		Mat temp, gray = new Mat(), gaublr = new Mat(), thresh = new Mat();
		int tempCnt = 0;
		for(int i = 0; i < 10; i++) {
			for(int j = 1; j <= SAMPLE_SIZE/10; j++) {
				temp = Imgcodecs.imread("C:\\img\\"+imgNumber(i)+"\\"+imgNumber(i)+" ("+imgNumber(j)+").png");
				Imgproc.cvtColor(temp, gray, Imgproc.COLOR_BGR2GRAY);
				Imgproc.GaussianBlur(gray, gaublr, new Size(5, 5), 0);
				Imgproc.adaptiveThreshold(gaublr, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 3);
				Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
				Rect boundingRect = new Rect();
				for(int k = 0; k < contours.size(); k++) {
					if(Imgproc.contourArea(contours.get(k)) > MIN_CONTOUR_AREA) {
						boundingRect = Imgproc.boundingRect(contours.get(k));
					}
				}
				Mat tempRoi = thresh.submat(boundingRect);
				Imgproc.resize(tempRoi, tempRoi, new Size(16, 16));
				tempRoi.convertTo(tempRoi, CvType.CV_32F);
				tempRoi = tempRoi.reshape(1, 1);
				System.out.println("tempRoi row & cols : "+tempRoi.rows()+", "+tempRoi.cols());
				training_set.push_back(tempRoi);
				System.out.println("set row & cols : "+training_set.rows()+", "+training_set.cols());
				training_label.put(tempCnt, i, 1.0);
				tempCnt++;
				System.out.println(tempCnt);
			}
		}
	}
	public void trainMachanism(int type) {
		if(type == ANN_MLP_FLAG) {
			mlType = ANN_MLP_FLAG;
		} else if(type == KNN_FLAG) {
			mlType = KNN_FLAG;

		} else if(type == SVM_FLAG) {
			mlType = SVM_FLAG;
		}
	}
	public int getMlType() {
		return mlType;
	}
	private String imgNumber(Integer i) {
		return i.toString();
	}
	public void ann_run() {
		System.out.println("ann_run start!");
		Mat layers = new Mat(5, 1, CvType.CV_32F);
		
		//레이어설정
		layers.row(0).setTo(new Scalar(ATTRIBUTES));
		layers.row(1).setTo(new Scalar(HIDDEN_LAYER_1));
		layers.row(2).setTo(new Scalar(HIDDEN_LAYER_2));
		layers.row(3).setTo(new Scalar(HIDDEN_LAYER_3));
		layers.row(4).setTo(new Scalar(CLASSES));
		
		//신경망 객체 생성 및 인자 설정
		annetwork = ANN_MLP.create();
		System.out.println("create annetwork");
		TermCriteria termCriteria = new TermCriteria();
		termCriteria.type = TermCriteria.MAX_ITER + TermCriteria.EPS;
		termCriteria.maxCount = 1000;
		termCriteria.epsilon = 0.00001;

		annetwork.setLayerSizes(layers); // 신경망에 레이어 설정
		annetwork.setTermCriteria(termCriteria); // 임계 조건 설정
		annetwork.setActivationFunction(ANN_MLP.SIGMOID_SYM, 0.6, 1); // 활성화 함수 설정
		annetwork.setTrainMethod(ANN_MLP.BACKPROP, 0.1, 0.1); // 학습 알고리즘 설정
		annetwork.train(training_set, Ml.ROW_SAMPLE, training_label); // 학습 시작
		System.out.println("train Complete");
	}
	private void knn_run() { // 수정해야함
		if(!knnIsTrain) {
			knn = KNearest.create();
			System.out.println("KNN Create");
			knn.setAlgorithmType(KNearest.BRUTE_FORCE);
			knn.train(training_set, Ml.ROW_SAMPLE, training_label);
			System.out.println("KNN train Complete");
			knnIsTrain = true;
		} else {
			System.out.println("Already Trained!");
		}
	}
	private void svm_run() { // 추가해야함

	}

	public int ann_predict(Mat Roi) {
		Roi.convertTo(Roi, CvType.CV_32F);
		annetwork.predict(Roi, predict_result, StatModel.UPDATE_MODEL);

		int maxIndex = 0;
		double value = 0.0f;
		double maxValue = predict_result.get(0, 0)[0];
		for (int index = 0; index < 10; index++) {
			value = predict_result.get(0, index)[0];
			System.out.printf("%f \n", predict_result.get(0, index)[0]);
			if (value > maxValue) {
				maxValue = value;
				maxIndex = index;
			}
		}
		System.out.println(maxIndex);

		return maxIndex;
	}
	public void knn_predict(Mat Roi) {
		Roi.convertTo(Roi, CvType.CV_32F);

		knn.findNearest(Roi, 1, predict_result);
		int maxIndex = 0;
		double value = 0.0f;
		double maxValue = predict_result.get(0, 0)[0];
		for (int index = 0; index < 10; index++) {
			value = predict_result.get(0, index)[0];
			System.out.printf("%f \n", predict_result.get(0, index)[0]);
			if (value > maxValue) {
				maxValue = value;
				maxIndex = index;
			}
		}
		System.out.println(maxIndex);
	}
	@Override
	public void run() {
		if(!LOADED) {
			loadTrainingImg(); // 이미지 로딩!
		} else {
			System.out.println("이미지 로딩은 되어 있습니다!");
		}
	}

}
