#include "Features.h"

Features::Features(std::string dir) {
	streamDir = dir;
	canStreamOpen = true;
}

void Features::loadDataOfFeatures() {
	if (canStreamOpen) {
		inputFile = std::ifstream(streamDir.data());
		iFeature = new std::vector<std::string>();
		rawDB = new std::vector<std::vector<std::string>>();
		while (std::getline(inputFile, StrBuf))
		{
			iFeature->clear();
			originalStr = (char*)StrBuf.c_str();
			token = new char[4000];
			token = strtok_s(originalStr, " ", &context);
			while (token) {
				iFeature->push_back(token);
				originalStr = context;
				token = strtok_s(originalStr, " ", &context);
			}
			/*iFeature->at(0) = iFeature->at(0).substr(5, 19);*/
			std::cout << iFeature->at(0) << std::endl;
			//std::cout << segmentDB->size() << std::endl;
			rawDB->push_back(*iFeature);
		}
		tuple_size = iFeature->size();
		db_size = rawDB->size();
		iFeature->clear();
		//std::cout << tuple_size << " " << db_size << std::endl;
	}
	else {
		std::cout << "Can not open stream of file" << std::endl;
	}
}
std::vector<std::string> &Features::getDataByImageFeature(int index) {
	return rawDB->at(index);
}

void Features::seperateDataByLabel() {
	if (!isSeperate) {
		alignmentDB = new std::multimap<std::string, std::vector<std::string>>();
		std::vector<std::vector<std::string>> labelDB;
		std::vector<std::string> temp;

		for (int i = 0; i < db_size; i++) {
			temp = getDataByImageFeature(i);

			std::cout << temp.at(0) << std::endl;
			alignmentDB->insert(std::pair<std::string, std::vector<std::string>>(temp.at(1), temp));

			if (i == db_size - 1) {
				numberOfLabel = atoi(temp.at(1).c_str()) + 1;
			}
		}


		std::cout << "Separate DB by Label Done " << numberOfLabel << std::endl;
		isSeperate = true;
	}
	else {
		std::cout << "already Separated DB by Label" << std::endl;
	}
}
void Features::calcGeometricCenter(std::string dir, int f_cnt) {
	if (isSeperate) {

		std::vector<std::vector<std::string>> tdb; // feature of each label class

												   //iteration pair for get a same label class in different images.
		std::pair<std::multimap<std::string, std::vector<std::string>>::iterator, std::multimap<std::string, std::vector<std::string>>::iterator> ret;
		// iteration for match for real data
		std::multimap<std::string, std::vector<std::string>>::iterator iter;
		std::vector<double> *totalDiff;
		int numImages = 0;
		// iterate each label class
		openStream(dir, std::ofstream::app);
		for (int label = 0; label < numberOfLabel; label++) {
			ret = alignmentDB->equal_range(std::to_string(label)); // find a Feature data to each class in a each image

																   // iterate each image of has same label class
			for (iter = ret.first; iter != ret.second; iter++) {
				// get a current Feature..
				tdb.push_back(iter->second);
			} // end of for (i = ret.first; i != ret.second; i++)
			numImages = tdb.size();
			totalDiff = new std::vector<double>(numImages);
			for (int l1 = 0; l1 < numImages; l1++) {
				for (int l2 = 0; l2 < numImages; l2++) {
					double diff = 0.0;
					double data1 = 0.0;
					double data2 = 0.0;

					for (int feat = 2; feat < f_cnt; feat++) {
						//계산부.
						data1 = atof(tdb.at(l1).at(feat).data());
						data2 = atof(tdb.at(l2).at(feat).data());

						diff = std::abs(data1 - data2);

						totalDiff->at(l1) += diff;
					} // end of for (int feat = 2; feat < 2050; feat++)
				} // end of for (int l2 = l1 + 1; l2 < numImages; l2++) {
				std::cout << totalDiff->at(l1) << std::endl;
			} // end of for (int l1 = 0; l1 < numImages; l1++) {

			  //for (int o = 0; o < numImages; o++)
			  //	std::cout << totalDiff->at(o) << " ";
			double max, min;
			int index = 0;

			max = min = totalDiff->at(0);
			for (int i = 1; i < numImages; i++) {

				//if (totalDiff->at(i) > max)
				//	max = totalDiff->at(i);
				if (totalDiff->at(i) < min) {
					min = totalDiff->at(i);
					index = i;
				}
			}

			//std::cout << std::endl;
			std::string tempTemp;
			std::cout << tdb.at(index).at(0).c_str() << " " << tdb.at(index).at(1).c_str() << std::endl;
			tempTemp.append(tdb.at(index).at(0));
			tempTemp.append(" ");
			tempTemp.append(tdb.at(index).at(1));
			tempTemp.append("\n");
			save(tempTemp);
			//	_sleep(500000000000000);
			tempTemp.clear();
			totalDiff->clear();
			tdb.clear();
		} // end of for (int label = 0; label < numberOfLabel; label++)
		closeStream();
	} // end of if (isSeperate)
}
void Features::compareInnerClass(std::string geoDir, std::string saveName, int f_cnt) {
	if (isSeperate) {
		geometricDB = new std::vector<std::vector<std::string>>();
		std::vector<std::vector<std::string>> tdb; // feature of each label class
												   //iteration pair for get a same label class in different images.
		std::pair<std::multimap<std::string, std::vector<std::string>>::iterator, std::multimap<std::string, std::vector<std::string>>::iterator> ret;
		// iteration for match for real data
		std::multimap<std::string, std::vector<std::string>>::iterator iter;
		std::string currentGeometricLabel;
		std::string outputFileName = saveName;
		std::vector<double> *totalDistance;
		int currentGeometricIndex = 0;
		int numImages = 0;


		iFeature = new std::vector<std::string>();

		geometricFile.open(geoDir.c_str());

		while (std::getline(geometricFile, StrBuf))
		{
			iFeature->clear();
			originalStr = (char*)StrBuf.c_str();
			token = new char[4000];
			token = strtok_s(originalStr, " ", &context);
			while (token) {
				iFeature->push_back(token);
				originalStr = context;
				token = strtok_s(originalStr, " ", &context);
			}
			geometricDB->push_back(*iFeature);
			iFeature->clear();
		}

		// iterate each label class
		for (int label = 0; label < numberOfLabel; label++) {
			ret = alignmentDB->equal_range(std::to_string(label)); // find a Feature data to each class in a each image

																   // iterate each image of has same label class
			for (iter = ret.first; iter != ret.second; iter++) {
				// get a current Feature..
				tdb.push_back(iter->second);
			} // end of for (i = ret.first; i != ret.second; i++)
			numImages = tdb.size();

			totalDistance = new std::vector<double>(numImages);
			currentGeometricLabel = geometricDB->at(label).at(0);

			std::cout << currentGeometricLabel.c_str() << std::endl;

			for (int i = 0; i < numImages; i++) {
				if (currentGeometricLabel.compare(tdb.at(i).at(0)) == 0) {
					currentGeometricIndex = i;
				}
				//std::cout << i << " " << std::endl;
			}

			for (int l1 = 0; l1 < numImages; l1++) {
				if (l1 == currentGeometricIndex) {
					std::cout
						<< tdb.at(currentGeometricIndex).at(0).c_str()
						<< " " << tdb.at(l1).at(0).c_str() << " "
						<< "geometric image pass" << std::endl;
					continue;
				}
				else {
					double data1 = 0.0;
					double data2 = 0.0;

					for (int feat = 2; feat < f_cnt; feat++) {
						//계산부.
						data1 = atof(tdb.at(currentGeometricIndex).at(feat).data());
						data2 = atof(tdb.at(l1).at(feat).data());

						totalDistance->at(l1) += std::fabs(std::sqrt(std::pow<double>(data1, 2) + std::pow<double>(data2, 2) + (-2 * data1*data2)));

					}
					//std::cout
					//	<< tdb.at(currentGeometricIndex).at(0).c_str()
					//	<< " " << tdb.at(l1).at(0).c_str() << " "
					//	<< totalDistance->at(l1) 
					//	<< " " << std::endl;
				}
			}
			////saveLog("Geometric Center Image : ", outputFileName);
			//saveLog(tdb.at(currentGeometricIndex).at(0), outputFileName);
			//saveLog("\n", outputFileName);
			std::string saveLog = "";
			openStream(outputFileName, std::ofstream::app);
			for (int i = 0; i < numImages; i++) {
				saveLog.append(tdb.at(currentGeometricIndex).at(0));
				saveLog.append("\t");
				saveLog.append(tdb.at(i).at(0));
				saveLog.append("\t");
				saveLog.append(std::to_string(totalDistance->at(i)));
				saveLog.append("\n");
				save(saveLog);
				saveLog.clear();
			}
			closeStream();

			//saveLog("\n", outputFileName);

			totalDistance->clear();
			tdb.clear();
		} // end of for (int label = 0; label < numberOfLabel; label++)
	}

	geometricFile.close();
}

void Features::compareOtherClass(std::string geoDir, std::string saveName, int f_cnt) {
	if (isSeperate) {
		geometricDB = new std::vector<std::vector<std::string>>();
		std::vector<std::vector<std::string>> tdb; // feature of each label class
		std::vector<std::vector<std::string>> t2db; // feature of each label class
													//iteration pair for get a same label class in different images.
		std::pair<std::multimap<std::string, std::vector<std::string>>::iterator, std::multimap<std::string, std::vector<std::string>>::iterator> geoMap;
		std::pair<std::multimap<std::string, std::vector<std::string>>::iterator, std::multimap<std::string, std::vector<std::string>>::iterator> impoMap;
		// iteration for match for real data
		std::multimap<std::string, std::vector<std::string>>::iterator geoIter;
		std::multimap<std::string, std::vector<std::string>>::iterator impoIter;
		std::string currentGeometricLabel;
		std::string outputFileName = saveName;
		std::vector<double> *totalDistance;
		int currentGeometricIndex = 0;
		int geoImgSize = 0;
		int impoImgSize = 0;

		iFeature = new std::vector<std::string>();

		geometricFile.open(geoDir.c_str());
		std::pair<int, int> a(1, 2);
		while (std::getline(geometricFile, StrBuf))
		{
			iFeature->clear();
			originalStr = (char*)StrBuf.c_str();
			std::pair<int, int> a = std::pair<int, int>(1, 2);

			std::vector<std::vector<std::pair<int, int>>> tempp(10);
			token = new char[4000];
			token = strtok_s(originalStr, " ", &context);
			while (token) {
				iFeature->push_back(token);
				originalStr = context;
				token = strtok_s(originalStr, " ", &context);
			}
			geometricDB->push_back(*iFeature);
			iFeature->clear();
		}

		// iterate each label class
		for (int label = 0; label < numberOfLabel; label++) {
			geoMap = alignmentDB->equal_range(std::to_string(label)); // find a Feature data to each class in a each image

																	  // iterate each image of has same label class
			for (geoIter = geoMap.first; geoIter != geoMap.second; geoIter++) {
				// get a current Feature..
				tdb.push_back(geoIter->second);
			} // end of for (i = ret.first; i != ret.second; i++)
			geoImgSize = tdb.size();

			currentGeometricLabel = geometricDB->at(label).at(0);

			std::cout << currentGeometricLabel.c_str() << std::endl;

			for (int i = 0; i < geoImgSize; i++) {
				if (currentGeometricLabel.compare(tdb.at(i).at(0)) == 0) {
					currentGeometricIndex = i;
				}
				/*std::cout << i << " " << std::endl;*/
			}

			for (int impo = 0; impo < numberOfLabel; impo++) {
				if (impo == label) {
					continue;
				}
				else {
					impoMap = alignmentDB->equal_range(std::to_string(impo));
					for (impoIter = impoMap.first; impoIter != impoMap.second; impoIter++) {
						t2db.push_back(impoIter->second);
						//impoImgSize++;
					}
				}
			}
			impoImgSize = t2db.size();

			totalDistance = new std::vector<double>(impoImgSize);

			for (int l1 = 0; l1 < impoImgSize; l1++) {
				double data1 = 0.0;
				double data2 = 0.0;

				for (int feat = 2; feat < f_cnt; feat++) {
					data1 = atof(tdb.at(currentGeometricIndex).at(feat).data());
					data2 = atof(t2db.at(l1).at(feat).data());

					totalDistance->at(l1) += std::fabs(std::sqrt(std::pow<double>(data1, 2) + std::pow<double>(data2, 2) + (-2 * data1*data2)));

				}
				//std::cout
				//	<< tdb.at(currentGeometricIndex).at(0).c_str()
				//	<< " " << t2db.at(l1).at(0).c_str() << " "
				//	<< totalDistance->at(l1) << " " << std::endl;
			}
			//saveLog("Geometric Center Image : ", outputFileName);
			//saveLog(tdb.at(currentGeometricIndex).at(0), outputFileName);
			//saveLog("\n", outputFileName);

			std::string saveLog = "";
			openStream(outputFileName, std::ofstream::app);
			for (int i = 0; i < impoImgSize; i++) {
				saveLog.append(tdb.at(currentGeometricIndex).at(0));
				saveLog.append("\t");
				saveLog.append(t2db.at(i).at(0));
				saveLog.append("\t");
				saveLog.append(std::to_string(totalDistance->at(i)));
				saveLog.append("\n");
				save(saveLog);
				saveLog.clear();
			}
			closeStream();

			//saveLog("\n", outputFileName);

			totalDistance->clear();
			tdb.clear();
			t2db.clear();
		} // for (int label = 0; label < numberOfLabel; label++) {
		geometricFile.close();
	} // if(isSeparate)

}
void Features::openStream(std::string fileName, int mode) {
	this->stream.open(fileName, mode);
}

void Features::closeStream() {
	stream.close();
}
void Features::save(std::string str) {
	stream << str;
}