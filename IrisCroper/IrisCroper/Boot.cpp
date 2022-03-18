#include "Boot.h"

int main(int argc, char** argv) {

	//if (argc < 2) {
	//	cout << "IrisCroper <image_dirs[.txt]>" << endl;
	//	return 0;
	//}

	

	string window;
	vector<string>* data;
	vector<string>::iterator it;
	cv::Mat image = cv::Mat();
	cv::Mat padImage;
	cv::Mat viewImage;
	handle::Processor* unit = new handle::Processor();
	vector<void*>* extraData = new vector<void*>();
	cv::MouseCallback evt = unit->onMouseEvent;

	//tool::FileReader* reader = new tool::FileReader(argc[++argv]);
	tool::FileReader* reader = new tool::FileReader("list2.txt");
	data = reader->getTextList();

	for (it = data->begin(); it != data->end(); it++) {
		window = *it;
		image = cv::imread(*it, 1);

		window = window.substr(28, 40);
		unit->paddingImg(&image, &padImage); // image padding

		viewImage = padImage.clone(); // viewer

		unit->makeWindow(window, viewImage.cols, viewImage.rows);

		extraData->push_back((void*)&viewImage);
		extraData->push_back((void*)&window);
		extraData->push_back((void*)&viewImage.cols);
		extraData->push_back((void*)&viewImage.rows);

		unit->addMouseCallback(window, unit->onMouseEvent, extraData);
		unit->viewImage(viewImage, window);
		
	}
	
	return 0;
}