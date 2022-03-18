#pragma once

#include <iostream>
#include <fstream>
#include <opencv2/opencv.hpp>

class Features {
private:
	std::ifstream inputFile;
	std::ifstream geometricFile;
	std::string streamDir;
	//For DATABASE
	std::vector<std::vector<std::string>>* rawDB;
	std::vector<std::string>* iFeature;
	std::vector<std::vector<std::string>> *geometricDB;
	std::multimap<std::string, std::vector<std::string>> *alignmentDB;
	std::ofstream stream;
	// used to load file and separate string.
	std::string StrBuf;
	char* originalStr = NULL;
	char* token = NULL;
	char* context = NULL;
	// used to count the total db size and number of each feature vector.
	int db_size = 0;
	int tuple_size = 0;

	int numberOfLabel = 0; // //number of image classes.
	bool canStreamOpen = false;
	bool isSeperate = false; // used to check that db was separated.

private:
	void openStream(std::string fileName, int mode);
	void closeStream();
	void save(std::string str);

public:
	Features(std::string dir);
	std::vector<std::string> &getDataByImageFeature(int index);
	void loadDataOfFeatures();
	void seperateDataByLabel();
	void compareInnerClass(std::string geoDir, std::string saveName, int f_cnt);
	void compareOtherClass(std::string geoDir, std::string saveName, int f_cnt);
	void calcGeometricCenter(std::string dir, int f_cnt);
};
