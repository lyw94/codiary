#include <opencv2\opencv.hpp>
#include <iostream>
#include <fstream>

#define TRAINING_SAMPLES 5000 // 트레이닝 데이터세트의 개수
#define ATTRIBUTES 256 // 샘플의 픽셀수 16x16
#define TEST_SAMPLES 3000 // 테스트 데이터세트의 개수
#define HIDDEN_LAYER1 30
#define HIDDEN_LAYER2 60
#define HIDDEN_LAYER3 40
#define CLASSES 10 // 숫자라벨의 갯수

using namespace cv::ml;

void read_dataset(char *filename, cv::Mat &data, cv::Mat &classes, int total_samples) {


	int label;
	int pixelValue = 0;
	char str;
	const char *idxStr;

	std::string line;
	std::ifstream inputFile(filename, std::ios_base::in);

	for (int row = 0; row < total_samples; row++) {
		inputFile >> line; // 파일 입력후 string 변수에 저장시킴
		for (int col = 0; col <= 256; col++) {
			if (col < ATTRIBUTES) {
				str = line.at(col); // 한 행의 열값을 읽어옴
				idxStr = &str; // 읽어온 열값을 int 형을 변환하기 위해 저장시킴
				pixelValue = atoi(idxStr); // 변환시킨 int 형값을 저장시킴				data.at<float>(row, col) = pixelvalue; // 행렬에 저장시킴
				data.at<float>(row, col) = pixelValue;
			}
			else if (col == ATTRIBUTES) {
				str = line.at(col); // 한 행의 열값을 읽어옴
				idxStr = &str; // 읽어온 열값을 int 형을 변환하기 위해 저장시킴
				label = atoi(idxStr); // 변환시킨 int 형값을 저장시킴
				classes.at<float>(row, label) = 1.0;
			}
		}

		//std::cout << line << std::endl;
	}


}

int main() {
	cv::Mat training_set(TRAINING_SAMPLES, ATTRIBUTES, CV_32F); // 학습 데이터를 저장하고 있는 행렬
	cv::Mat training_set_classifications(TRAINING_SAMPLES, CLASSES, CV_32F); // 학습데이터들의 라벨을 저장하는 행렬
	cv::Mat test_set(TEST_SAMPLES, ATTRIBUTES, CV_32F); // 테스트 데이터를 저장하고 있는 행렬
	cv::Mat test_set_classifications(TEST_SAMPLES, CLASSES, CV_32F); // 테스트 데이터의 라벨을 저장하고 잇는 행렬

	cv::Mat classificationResult(1, CLASSES, CV_32F); // 학습후 predict 메소드의 결과값을 저장할 행렬

	// 학습 데이터 세트를 읽어와 행렬에 저장시킴
	read_dataset("G:/desktop/trainingset.txt", training_set, training_set_classifications, TRAINING_SAMPLES);
	// 테스트 데이터 세트를 읽어와 행렬에 저장시킴
	read_dataset("G:/desktop/testset.txt", test_set, test_set_classifications, TEST_SAMPLES);

	// ANN 모델에서 사용할 레이어 선언
	cv::Mat layers(5, 1, CV_32F);

	// 입력 노드 레이어
	layers.row(0) = cv::Scalar(ATTRIBUTES);
	// 히든 노드 레이어 3개의 층으로 구분함
	layers.row(1) = cv::Scalar(HIDDEN_LAYER1);
	layers.row(2) = cv::Scalar(HIDDEN_LAYER2);
	layers.row(3) = cv::Scalar(HIDDEN_LAYER3);
	// 출력 노드 레이어
	layers.row(4) = cv::Scalar(CLASSES);

	// 인공 신경망 네트워크 모델 선언
	cv::Ptr<ANN_MLP> nNetwork = cv::ml::ANN_MLP::create();

	// TermCriteria 선언
	cv::TermCriteria termCriteria;
	// TermCriteria 설정 셋팅
	termCriteria.type = CV_TERMCRIT_ITER + CV_TERMCRIT_EPS;
	termCriteria.maxCount = 1000;
	termCriteria.epsilon = 0.000001;

	// 인공신경망 인자 셋팅	
	nNetwork->setLayerSizes(layers);
	nNetwork->setActivationFunction(ANN_MLP::ActivationFunctions::SIGMOID_SYM, 0.6, 1);
	nNetwork->setTermCriteria(termCriteria);
	nNetwork->setTrainMethod(ANN_MLP::TrainingMethods::BACKPROP, 0.1, 0.1);
	// 학습데이터를 만들어 넘길때 사용할 클래스 생성
	cv::Ptr<TrainData> trainData = TrainData::create(training_set, SampleTypes::ROW_SAMPLE, training_set_classifications, cv::Mat(), cv::Mat());

	// 신경망 학습
	printf("\n using training dataset \n");
	int iterations = nNetwork->train(trainData, ANN_MLP::TrainFlags::UPDATE_WEIGHTS);
	printf("Training iterations: %i\n\n", iterations);

	// 학습데이터 저장
	cv::FileStorage storage("G:/desktop/train.xml", cv::FileStorage::Mode::WRITE);
	nNetwork->write(storage);
	storage.release();

	cv::Mat test_sample; // 생성된 학습모델의 테스트 샘플 선언
	int correct_class = 0; // 정확하게 분류된 것을 카운트 하는 변수
	int wrong_class = 0; // 잘못 분류된 것들을 카운트 하는 변수

	int classification_matrix[CLASSES][CLASSES] = { {} }; // 어떤 샘플이 어떻게 분류되었는지 저장하는 배열

	for (int tsample = 0; tsample < TEST_SAMPLES; tsample++) {

		// 샘플을 추출함
		test_sample = test_set.row(tsample);

		// 해당 클래스의 데이터에 대해 예측을 시도함 try to predict its class
		nNetwork->predict(test_sample, classificationResult, cv::ml::StatModel::Flags::UPDATE_MODEL);
		// 분류 결과 행렬은 각 클래스간의 가중치를 보유하고 있다. 
		// 우리는 결과로 나온 클래스들 중 높은 가중치값을 가지고 있는 것을 가져올 것이다.


		// 최대치의 가중치를 찾는 코드 find the class with maximum weightage
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


		//이제 예측한 클래스와 실제 클래스를 비교할 것이다. 만약 예상한 클래스가 정확하다면
		//test_set_classification[tsample][maxIndex] 이 1로 될것 이다.
		// 만약 틀렸다면 다음과 같이 한다.
		printf("Testing Sample %i -> class result (digit %d)\n", tsample, maxIndex);

		if (test_set_classifications.at<float>(tsample, maxIndex) < 1.0f) {
			// 만약 소수점 이하값이 다르게 된다면 잘못된다면 잘못된 분류를 한것이다.
			wrong_class++;

			// 'class_index'의 실제 라벨을 찾는다.
			for (int class_index = 0; class_index < CLASSES; class_index++) {
				if (test_set_classifications.at<float>(tsample, class_index) == 1.0f) {
					//class_index 의 샘플이 maxIndex로 잘못 분류 되었을때
					classification_matrix[class_index][maxIndex]++;
					break;
				}
			}
		}
		else {
			// 나머지 정확했을 경우
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