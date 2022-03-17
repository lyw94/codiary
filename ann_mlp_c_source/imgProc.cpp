#include <opencv2\opencv.hpp>
#include <iostream>
#include <fstream>

void cropImage(cv::Mat &originalImage, cv::Mat &croppedImage) {
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

std::string convertInt(int number) {
	std::stringstream ss;
	ss << number;
	return ss.str();
}

void readFile(std::string dataSetPath, int samplesPerClass, std::string outFile) {
	std::ofstream file(outFile, std::ios::out);
	for (int sample = 1; sample <= samplesPerClass; sample++) {
		for (int digit = 0; digit < 10; digit++) {
			// ������ ��θ� ������ ���ڿ� ����
			std::string imagePath = dataSetPath + convertInt(digit) + "\\" + convertInt(digit) + " (" + convertInt(sample) + ").png";
			// �̹��� �о��
			cv::Mat img;
			img = cv::imread(imagePath, -1);
			cv::Mat out;
			// ����þ� �� �����Ͽ� ������ ����
			cv::GaussianBlur(img, out, cv::Size(5, 5), 0);
			// �̹����� ����ȭ
			cv::threshold(out, out, 50, 255, CV_THRESH_BINARY);


			// ������ �ٿ��� �̹��� ������ Mat ����
			cv::Mat scaledDownImage(16, 16, CV_32F, cv::Scalar(0));

			int pixelValueArray[256];

			// �߶� �̹���
			cropImage(out, out);
			//cv::namedWindow("thresh", CV_WINDOW_AUTOSIZE);
			//cv::imshow("thresh", out);
			//cv::waitKey(0);
			// �̹��� ũ�⸦ �������� �̹���
			cv::resize(out, scaledDownImage, cv::Size(16, 16));
			//scaleDownImage(out, scaledDownImage);
			//cv::imshow("resize", scaledDownImage);
			//cv::waitKey(0);
			// �ȼ������͸� ����
			convert2PixelValueArray(scaledDownImage, pixelValueArray);
			// ���Ͽ� �����͸� �����
			for (int d = 0; d < 256; d++) {
				file << pixelValueArray[d];
			}

			// ���� ���Ͽ� �����
			file << digit << "\n";

		}
	}
	file.close();
}

int main() {
	std::cout << "Reading the training set ............" << std::endl;
	readFile("C:/img/", 600, "G:/desktop/trainingset.txt");
	std::cout << "Reading the test set ..........." << std::endl;
	readFile("C:/img/", 500, "G:/desktop/testset.txt");
	std::cout << "operation completed" << std::endl;
}