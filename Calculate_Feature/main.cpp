#include "main.h"

int main(int argc, char** argv) {

	if (argc < 6) {
		std::cout << "<feature_file_name> <geometric_file_name> <Authentic_match_file_Name> <Imposter_match_file_Name> <number_of_features>" << std::endl;
		return 0;
	}

	std::string featureFDir = argv[1];
	std::string geometricDir = argv[2];
	std::string authenticDir = argv[3];
	std::string imposterDir = argv[4];
	std::string feature_num = argv[5];

	////////////////////////////////////////////////
	Features* fc = new Features(featureFDir);
	
	std::cout << feature_num << std::endl;
	int f_cnt = std::stoi(feature_num) + 2;

	fc->loadDataOfFeatures();

	fc->seperateDataByLabel();

	fc->calcGeometricCenter(geometricDir, f_cnt);

	fc->compareInnerClass(geometricDir, authenticDir, f_cnt);

	fc->compareOtherClass(geometricDir, imposterDir, f_cnt);

	return 0;
}