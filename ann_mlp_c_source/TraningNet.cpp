#include <opencv2\opencv.hpp>
#include <iostream>
#include <fstream>

#define TRAINING_SAMPLES 5000 // Ʈ���̴� �����ͼ�Ʈ�� ����
#define ATTRIBUTES 256 // ������ �ȼ��� 16x16
#define TEST_SAMPLES 3000 // �׽�Ʈ �����ͼ�Ʈ�� ����
#define HIDDEN_LAYER1 30
#define HIDDEN_LAYER2 60
#define HIDDEN_LAYER3 40
#define CLASSES 10 // ���ڶ��� ����

using namespace cv::ml;

void read_dataset(char *filename, cv::Mat &data, cv::Mat &classes, int total_samples) {


	int label;
	int pixelValue = 0;
	char str;
	const char *idxStr;

	std::string line;
	std::ifstream inputFile(filename, std::ios_base::in);

	for (int row = 0; row < total_samples; row++) {
		inputFile >> line; // ���� �Է��� string ������ �����Ŵ
		for (int col = 0; col <= 256; col++) {
			if (col < ATTRIBUTES) {
				str = line.at(col); // �� ���� ������ �о��
				idxStr = &str; // �о�� ������ int ���� ��ȯ�ϱ� ���� �����Ŵ
				pixelValue = atoi(idxStr); // ��ȯ��Ų int ������ �����Ŵ				data.at<float>(row, col) = pixelvalue; // ��Ŀ� �����Ŵ
				data.at<float>(row, col) = pixelValue;
			}
			else if (col == ATTRIBUTES) {
				str = line.at(col); // �� ���� ������ �о��
				idxStr = &str; // �о�� ������ int ���� ��ȯ�ϱ� ���� �����Ŵ
				label = atoi(idxStr); // ��ȯ��Ų int ������ �����Ŵ
				classes.at<float>(row, label) = 1.0;
			}
		}

		//std::cout << line << std::endl;
	}


}

int main() {
	cv::Mat training_set(TRAINING_SAMPLES, ATTRIBUTES, CV_32F); // �н� �����͸� �����ϰ� �ִ� ���
	cv::Mat training_set_classifications(TRAINING_SAMPLES, CLASSES, CV_32F); // �н������͵��� ���� �����ϴ� ���
	cv::Mat test_set(TEST_SAMPLES, ATTRIBUTES, CV_32F); // �׽�Ʈ �����͸� �����ϰ� �ִ� ���
	cv::Mat test_set_classifications(TEST_SAMPLES, CLASSES, CV_32F); // �׽�Ʈ �������� ���� �����ϰ� �մ� ���

	cv::Mat classificationResult(1, CLASSES, CV_32F); // �н��� predict �޼ҵ��� ������� ������ ���

	// �н� ������ ��Ʈ�� �о�� ��Ŀ� �����Ŵ
	read_dataset("G:/desktop/trainingset.txt", training_set, training_set_classifications, TRAINING_SAMPLES);
	// �׽�Ʈ ������ ��Ʈ�� �о�� ��Ŀ� �����Ŵ
	read_dataset("G:/desktop/testset.txt", test_set, test_set_classifications, TEST_SAMPLES);

	// ANN �𵨿��� ����� ���̾� ����
	cv::Mat layers(5, 1, CV_32F);

	// �Է� ��� ���̾�
	layers.row(0) = cv::Scalar(ATTRIBUTES);
	// ���� ��� ���̾� 3���� ������ ������
	layers.row(1) = cv::Scalar(HIDDEN_LAYER1);
	layers.row(2) = cv::Scalar(HIDDEN_LAYER2);
	layers.row(3) = cv::Scalar(HIDDEN_LAYER3);
	// ��� ��� ���̾�
	layers.row(4) = cv::Scalar(CLASSES);

	// �ΰ� �Ű�� ��Ʈ��ũ �� ����
	cv::Ptr<ANN_MLP> nNetwork = cv::ml::ANN_MLP::create();

	// TermCriteria ����
	cv::TermCriteria termCriteria;
	// TermCriteria ���� ����
	termCriteria.type = CV_TERMCRIT_ITER + CV_TERMCRIT_EPS;
	termCriteria.maxCount = 1000;
	termCriteria.epsilon = 0.000001;

	// �ΰ��Ű�� ���� ����	
	nNetwork->setLayerSizes(layers);
	nNetwork->setActivationFunction(ANN_MLP::ActivationFunctions::SIGMOID_SYM, 0.6, 1);
	nNetwork->setTermCriteria(termCriteria);
	nNetwork->setTrainMethod(ANN_MLP::TrainingMethods::BACKPROP, 0.1, 0.1);
	// �н������͸� ����� �ѱ涧 ����� Ŭ���� ����
	cv::Ptr<TrainData> trainData = TrainData::create(training_set, SampleTypes::ROW_SAMPLE, training_set_classifications, cv::Mat(), cv::Mat());

	// �Ű�� �н�
	printf("\n using training dataset \n");
	int iterations = nNetwork->train(trainData, ANN_MLP::TrainFlags::UPDATE_WEIGHTS);
	printf("Training iterations: %i\n\n", iterations);

	// �н������� ����
	cv::FileStorage storage("G:/desktop/train.xml", cv::FileStorage::Mode::WRITE);
	nNetwork->write(storage);
	storage.release();

	cv::Mat test_sample; // ������ �н����� �׽�Ʈ ���� ����
	int correct_class = 0; // ��Ȯ�ϰ� �з��� ���� ī��Ʈ �ϴ� ����
	int wrong_class = 0; // �߸� �з��� �͵��� ī��Ʈ �ϴ� ����

	int classification_matrix[CLASSES][CLASSES] = { {} }; // � ������ ��� �з��Ǿ����� �����ϴ� �迭

	for (int tsample = 0; tsample < TEST_SAMPLES; tsample++) {

		// ������ ������
		test_sample = test_set.row(tsample);

		// �ش� Ŭ������ �����Ϳ� ���� ������ �õ��� try to predict its class
		nNetwork->predict(test_sample, classificationResult, cv::ml::StatModel::Flags::UPDATE_MODEL);
		// �з� ��� ����� �� Ŭ�������� ����ġ�� �����ϰ� �ִ�. 
		// �츮�� ����� ���� Ŭ������ �� ���� ����ġ���� ������ �ִ� ���� ������ ���̴�.


		// �ִ�ġ�� ����ġ�� ã�� �ڵ� find the class with maximum weightage
		int maxIndex = 0;
		float value = 0.0f;
		float maxValue = classificationResult.at<float>(0, 0);
		for (int index = 0; index < CLASSES; index++) {
			value = classificationResult.at<float>(0, index);
			if (value > maxValue) {
				maxValue = value;
				maxIndex = index;
			}
		}


		//���� ������ Ŭ������ ���� Ŭ������ ���� ���̴�. ���� ������ Ŭ������ ��Ȯ�ϴٸ�
		//test_set_classification[tsample][maxIndex] �� 1�� �ɰ� �̴�.
		// ���� Ʋ�ȴٸ� ������ ���� �Ѵ�.
		printf("Testing Sample %i -> class result (digit %d)\n", tsample, maxIndex);

		if (test_set_classifications.at<float>(tsample, maxIndex) < 1.0f) {
			// ���� �Ҽ��� ���ϰ��� �ٸ��� �ȴٸ� �߸��ȴٸ� �߸��� �з��� �Ѱ��̴�.
			wrong_class++;

			// 'class_index'�� ���� ���� ã�´�.
			for (int class_index = 0; class_index < CLASSES; class_index++) {
				if (test_set_classifications.at<float>(tsample, class_index) == 1.0f) {
					//class_index �� ������ maxIndex�� �߸� �з� �Ǿ�����
					classification_matrix[class_index][maxIndex]++;
					break;
				}
			}
		}
		else {
			// ������ ��Ȯ���� ���
			correct_class++;
			classification_matrix[maxIndex][maxIndex]++;
		}

		printf("\n Results on the testing dataset \n\tCorrect Classification : %d (%g%%)\n\tWrong Classification : %d (%g%%)\n"
			, correct_class, (double)correct_class * 100 / TEST_SAMPLES,
			wrong_class, (double)wrong_class * 100 / TEST_SAMPLES);

		for (int i = 0; i < CLASSES; i++) {
			std::cout << i << "\t";
		}
		std::cout << "\n";
		for (int row = 0; row < CLASSES; row++) {
			for (int col = 0; col < CLASSES; col++) {
				std::cout << classification_matrix[row][col] << "\t";
			}
			std::cout << "\n";
		}


	}
	/*cv::Mat forPredict = cv::imread("C:\\img\\test2.jpg", -1);
	cv::Mat classOut(1, 10, CV_64F);

	nNetwork->predict(forPredict, classOut);*/
	printf("BACKPROP_weightScale : %f", nNetwork->getBackpropWeightScale());
	printf("BACKPROP_MomentumScale : %f", nNetwork->getBackpropMomentumScale());
	return 0;
}