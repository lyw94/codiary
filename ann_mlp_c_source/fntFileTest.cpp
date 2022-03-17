#include <opencv2\opencv.hpp>
#include <opencv2\ml\ml.hpp>
#include <iostream>

using namespace cv;
using namespace cv::ml;
void scaleDownImage(Mat &originalImg, Mat &scaledDownImage) {
	for (int x = 0; x < 16; x++) {
		for (int y = 0; y < 16; y++) {
			int yD = ceil((float)(y*originalImg.cols / 16));
			int xD = ceil((float)(y*originalImg.rows / 16));
			scaledDownImage.at<uchar>(x, y) = originalImg.at<uchar>(xD, yD);
		}
	}
}

void cropImage(Mat &originalImage, Mat &croppedImage) {
	int row = originalImage.rows;
	int col = originalImage.cols;
	int TLX, TLY, BRX, BRY; // T = TOP, B = BOTTOM, R = RIGHT, L = LEFT

	TLX = TLY = BRX = BRY = 0;

	float sumL = 0;
	float sumR = 0;

	int flag = 0;

	/****************** TOP EDGE **********************/
	for (int x = 1; x < row; x++) {
		for (int y = 0; y < col; y++) {
			if (originalImage.at<uchar>(x, y) == 0) {
				flag = 1;
				TLY = x;
				break;
			}
		}
		if (flag == 1) {
			flag = 0;
			break;
		}
	}

	/****************** BOTTOM EDGE **********************/
	for (int x = row - 1; x > 0; x--) {
		for (int y = 0; y < col; y++) {
			if (originalImage.at<uchar>(x, y) == 0) {
				flag = 1;
				BRY = x;
				break;
			}
		}
		if (flag == 1) {
			flag = 0;
			break;
		}
	}

	/****************** LEFT EDGE **********************/
	for (int y = 0; y < col; y++) {
		for (int x = 0; x < row; x++) {
			if (originalImage.at<uchar>(x, y) == 0) {
				flag = 1;
				TLX = y;
				break;
			}
		}
		if (flag == 1) {
			flag = 0;
			break;
		}
	}

	/****************** RIGHT EDGE **********************/
	for (int y = col - 1; y > 0; y--) {
		for (int x = 0; x < row; x++) {
			if (originalImage.at<uchar>(x, y) == 0) {
				flag = 1;
				BRX = y;
				break;
			}
		}
		if (flag == 1) {
			flag == 0;
			break;
		}
	}

	int width = BRX - TLX;
	int height = BRY - TLY;


	// crop(originalImage, cv::Rect(TLX, TLY, width, height));
	cv::Mat crop(originalImage, cv::Rect(TLX, TLY, width, height));


	croppedImage = crop.clone();
}

void convert2PixelValueArray(cv::Mat &img, int pixelArray[]) {
	int i = 0;
	for (int x = 0; x < 16; x++) {
		for (int y = 0; y < 16; y++) {
			pixelArray[i] = (img.at<uchar>(x, y) == 255) ? 1 : 0;
			i++;
		}
	}
}

void readPixeldata(Mat &data, Mat &classes, int total_sample, int pixelData[]) {
	int label;
	for (int row = 0; row < total_sample; row++) {
		for (int col = 0; col < 256; col++) {
			if (col < 256) {
				data.at<float>(row, col) = (float)pixelData[row, col]; // ��Ŀ� �����Ŵ
			}
			else if (col == 256) {
				label = pixelData[row, col];
				printf("%d", label);
				classes.at<float>(row, label) = 1.0;
			}
		}

		//std::cout << line << std::endl;
	}

}

void predictNumber(std::string &filePath) {
	// ����� �������� ����
	int pixelValue[256] = {};
	cv::Mat test_set(1, 256, CV_32F);
	cv::Mat classes(1, 10, CV_32F);
	cv::Mat classificationResult(1, 10, CV_32F);
	cv::Mat originImg = imread(filePath, -1);
	cv::Mat cropImg;
	cv::Mat scaleDown(16, 16, CV_8U, cv::Scalar(0));

	// �ΰ��Ű�� ��� ����
	Ptr<ANN_MLP> nNetwork = ANN_MLP::create();
	FileStorage storage("G:/desktop/train.xml", FileStorage::Mode::READ);
	nNetwork->read(storage.root());
	storage.release();

	//�̹��� ��ó��
	cropImage(originImg, originImg);
	threshold(originImg, cropImg, 120, 255, THRESH_BINARY);
	namedWindow("crop", WINDOW_AUTOSIZE);
	imshow("crop", cropImg);
	waitKey(0);
	scaleDownImage(cropImg, scaleDown);

	//���� ���� Ȯ�ο� ��
	

	// �о�� �̹����� �ȼ��� ��ȯ�� �׽�Ʈ ��Ŀ� ����
	convert2PixelValueArray(scaleDown, pixelValue);


	// ����� �ȼ� �迭�� ������ �󺧰� �����͸� �и��� �ΰ��Ű�� ���� ��������
	readPixeldata(test_set, classes, 1, pixelValue);
	nNetwork->predict(test_set, classificationResult, cv::ml::StatModel::Flags::UPDATE_MODEL);

	int wrong_class = 0;
	int correct_class = 0;

	int maxIndex = 0;
	float value = 0.0f;
	float maxValue = classificationResult.at<float>(0, 0);
	for (int index = 0; index < 10; index++) {
		value = classificationResult.at<float>(0, index);
		printf("%f \n", classificationResult.at<float>(0, index));
		if (value > maxValue) {
			maxValue = value;
			maxIndex = index;
		}
	}
	printf("Testing Sample -> class result (digit %d)\n", maxIndex);
}
int main() {
	std::string path[7] = { "G:/desktop/img/Fnt/6/6 (397).png",
		"G:/desktop/img/Fnt/4/4 (756).png",
		"G:/desktop/img/Fnt/7/7 (854).png",
		"G:/desktop/img/Fnt/2/2 (917).png",
		"G:/desktop/img/Fnt/3/3 (635).png",
		"G:/desktop/img/Fnt/8/8 (893).png",
		"G:/desktop/img/Fnt/9/9 (737).png"
	};
	predictNumber(path[0]);
	predictNumber(path[1]);
	predictNumber(path[2]);
	predictNumber(path[3]);
	predictNumber(path[4]);
	predictNumber(path[5]);
	predictNumber(path[6]);

	return 0;
}