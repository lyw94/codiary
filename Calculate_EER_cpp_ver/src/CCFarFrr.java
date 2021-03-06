import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CCFarFrr {
	// CASE Integer
	private int MAX_CASE = 0;
	private int AUT_CASE = MAX_CASE/10;
	private int IMP_CASE = MAX_CASE - AUT_CASE;
	private final static int MAX_HD = 1000;

	private double threshold = 0;
	private int authenticCnt = 0;
	private int imposterCnt = 0;

	// File Load Buffer Stream
	private FileReader fr = null;//new FileReader(new File("HD_db.txt"));
	private BufferedReader in = null;

	// Load File Database
	private String db[];


	private String splitDB[][]; // [MAX_CASE][enroll, target, HD]
	private String recognition[];

	private HashMap<Double, Integer> authenticHisto = new HashMap<Double, Integer>();
	private HashMap<Double, Integer> imposterHisto = new HashMap<Double, Integer>(); 

	private Set<Double> autKeySet;
	private Set<Double> impKeySet;

	private List<Double> autKeyList = new ArrayList<Double>();
	private List<Double> impKeyList = new ArrayList<Double>();

	private Iterator<Double> autIter;
	private Iterator<Double> impIter;


	public CCFarFrr(String db_name) {
		File f = null;
		try {
			f = new File(db_name);
			fr = new FileReader(f);
			in = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			System.out.println("DB is not found");
			e.printStackTrace();
		}
	}

	private String[] getLine() {
		LinkedList<String> l_str = new LinkedList<String>();
		String[] o_str = null; 
		try {
			while(in.ready()) {
				l_str.add(in.readLine());
				MAX_CASE++;
			}
			o_str = new String[MAX_CASE];
			l_str.toArray(o_str);
		} catch (IOException e) {
			System.out.println("Read All Line");
		}

		AUT_CASE = MAX_CASE/10;
		IMP_CASE = MAX_CASE - AUT_CASE;

/*		for(int i = 0; i < MAX_CASE; i++)
			System.out.println(o_str[i]);*/
		return o_str.clone();
	}
	
	public void separateAccount() {
		db = new String[MAX_CASE];
		db = getLine();
		
		splitDB = new String[MAX_CASE][];
		
		for(int i = 0; i < MAX_CASE; i++)
			System.out.println(db[i]);
		
		for (int i = 0; i < db.length; i++) {
			splitDB[i] = db[i].split("\t");
		}
	}
	
	public void countRecognition() {
		String target = null;
		String enroll = null;

		recognition = new String[MAX_CASE];
		for(int i = 0; i < MAX_CASE; i++) {
			target = splitDB[i][0];
			enroll = splitDB[i][1];

			if(target.regionMatches(0, enroll, 0, 9)) {
				recognition[i] = String.valueOf(1); // ???? ????
			}else {
				recognition[i] = String.valueOf(0); // ???? ????
			}
			//			System.out.printf("%16s %20s %s %s\n", splitDB[i][0], splitDB[i][1], splitDB[i][2], recognition[i]);
		}
	}

	public void separateHisto() {
		double dTemp = 0.0;

		for(int i = 0; i < MAX_CASE; i++) {
			dTemp = Double.parseDouble(splitDB[i][2]);

			if(recognition[i].equals("1")) {				// Authentics
				if(authenticHisto.containsKey(dTemp))
					authenticHisto.put(dTemp, authenticHisto.get(dTemp).intValue()+1);
				else
					authenticHisto.put(dTemp, 1);
				authenticCnt++;

			}
			else if(recognition[i].equals("0")) {		// Imposter
				if(imposterHisto.containsKey(dTemp))
					imposterHisto.put(dTemp, imposterHisto.get(dTemp).intValue()+1);
				else
					imposterHisto.put(dTemp, 1);
				imposterCnt++;
			}
		}

		System.out.println("Authentic : "+authenticCnt+", Imposter : "+imposterCnt);
		System.out.println("MAX Case : "+MAX_CASE);
		// HD ???? ????
		autKeySet = authenticHisto.keySet();
		impKeySet = imposterHisto.keySet();

		// ?????? ???? ???????? ????
		autKeyList.addAll(autKeySet);
		impKeyList.addAll(impKeySet);

		// ????
		Collections.sort(autKeyList);
		Collections.sort(impKeyList);

		/*		autIter = autKeyList.iterator();
		impIter = impKeyList.iterator();
		 */
		int sum = 0;

		/*		// ???????? ????.
		while(autIter.hasNext()) {
			Double k = autIter.next();
			int v = authenticHisto.get(k);

			System.out.println(k + " " + v);
			sum += v;
		}
		System.out.println("SUM : " + sum);
		System.out.println();
		sum = 0;
		while(impIter.hasNext()) {
			Double k = impIter.next();
			int v = imposterHisto.get(k);

			System.out.println(k + " " + v);
			sum += v;
		}

		System.out.println("SUM : " + sum);*/
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	private double[] getHDSum() {
		double[] returnSumHD = new double[2]; // [authentic, imposter]
		
		for(int i = 0; i < MAX_CASE; i++) {
			if(recognition[i].equals("1")) {
				returnSumHD[0] += Double.parseDouble(splitDB[i][2]);
			}else if(recognition[i].equals("0")) {
				returnSumHD[1] += Double.parseDouble(splitDB[i][2]);
			}
		}
		
		return returnSumHD;
	}
	private double[] getHDSumSquare() {
		double[] returnSumSquareHD = new double[2]; // [authentic, imposter]
		
		for(int i = 0; i < MAX_CASE; i++) {
			if(recognition[i].equals("1")) {
				returnSumSquareHD[0] += Math.pow(Double.parseDouble(splitDB[i][2]),2);
			}else if(recognition[i].equals("0")) {
				returnSumSquareHD[1] += Math.pow(Double.parseDouble(splitDB[i][2]),2);
			}
		}
		
		return returnSumSquareHD;
	}
	public double[] calculateEER() {
		double authenticSumHD = 0.0;
		double authenticAvgHD = 0.0;
		double authenticSumOfSquare = 0.0;
		double authenticStandardDeviation = 0;

		double imposterSumHD = 0.0;
		double imposterAvgHD = 0.0;
		double imposterSumOfSquare = 0.0;
		double imposterStandardDeviation = 0;

		double differenceOfMeans = 0.0;
		double meanOfStds = 0.0;

		double authenticMinHD = 100.0;
		double authenticMaxHD = 0.0;
		double imposterMinHD = 100.0;
		double imposterMaxHD = 0.0;
		double dPrime = 0.0;
		double EER = 0.0;

		double[] returnVal =  new double[4];

		int falseAcceptCnt = 0;
		double FAR = 100;
		int falseRejectCnt = 0;
		double FRR = 100;

		autIter = autKeyList.iterator();
		impIter = impKeyList.iterator();
		
		
		// Key -> HD value
		// autIter <= autKeyList, impIter <= impKeyList
		
		double[] aryHDSum = getHDSum();
		double[] aryHDSquareSum = getHDSumSquare();
		
		// Authentic
		while(autIter.hasNext()) {
			double HD_Key = autIter.next();
			
			if(HD_Key > threshold) {
				falseRejectCnt += authenticHisto.get(HD_Key).intValue();
			}
			if(HD_Key < authenticMinHD)
				authenticMinHD = HD_Key;
			if(HD_Key > authenticMaxHD)
				authenticMaxHD = HD_Key;
		}

		// Imposter
		while(impIter.hasNext()) {
			double HD_key = impIter.next();

			if(HD_key < threshold) {
				falseAcceptCnt += imposterHisto.get(HD_key).intValue();
			}
			if(HD_key < imposterMinHD)
				imposterMinHD = HD_key;
			if(HD_key > imposterMaxHD)
				imposterMaxHD = HD_key;

		}
		
		authenticAvgHD = aryHDSum[0]/authenticCnt;
		authenticSumOfSquare = aryHDSquareSum[0];
		authenticStandardDeviation = Math.sqrt((authenticSumOfSquare/authenticCnt)-(Math.pow(authenticAvgHD, 2)));
		
		imposterAvgHD = aryHDSum[1]/imposterCnt;
		imposterSumOfSquare = aryHDSquareSum[1];
		imposterStandardDeviation = Math.sqrt((imposterSumOfSquare/imposterCnt)-(Math.pow(imposterAvgHD, 2)));

		differenceOfMeans = authenticAvgHD - imposterAvgHD;
		meanOfStds = Math.sqrt((Math.pow(authenticStandardDeviation, 2)+Math.pow(imposterStandardDeviation, 2))*0.5);

		if(differenceOfMeans < 0) differenceOfMeans *= -1;

		FAR = (falseAcceptCnt/(double)imposterCnt)*100.;
		FRR = (falseRejectCnt/(double)authenticCnt)*100;
		EER = (FAR + FRR)/(double)2;
		dPrime = differenceOfMeans/meanOfStds;

		returnVal[0] = EER;
		returnVal[1] = dPrime;
		returnVal[2] = FAR;
		returnVal[3] = FRR;
		
		System.out.println("authenticAvgHD : "+authenticAvgHD);
		System.out.println("imposterAvgHD : "+imposterAvgHD);
		System.out.println("SumOfSquare1 : "+authenticSumOfSquare);
		System.out.println("SumOfSquare2 : "+imposterSumOfSquare);
		System.out.println("std1 : "+authenticStandardDeviation);
		System.out.println("std2 : "+imposterStandardDeviation);
		System.out.println("differenceOfMeans : "+differenceOfMeans);
		System.out.println("meanOfStds : "+meanOfStds);
		System.out.println("d' : "+dPrime);

		/*		System.out.println();
		System.out.printf("Threshold %4f\nFAR = %4f%%\nFRR = %4f%% \n", threshold, FAR, FRR);
		System.out.printf("EER = %f%% \nAuthentic_HD_MAX = %3f \nAuthentic_HD_MIN = %3f\n", EER, authenticMaxHD, authenticMinHD);
		System.out.println("===============  ============================");
		System.out.printf("Imposter_HD_MAX = %3f \nImposter_HD_MIN = %3f\n" ,imposterMaxHD,imposterMinHD);
		System.out.println("===========================================");
		System.out.printf("Authentic_Avg_HD = %4f\nAuthentic_Std_HD = %4f\n",authenticAvgHD,authenticStandardDeviation);
		System.out.println("===========================================");
		System.out.printf("Imposter_Avg_HD = %4f\nImposter_Std_HD = %4f\n",imposterAvgHD,imposterStandardDeviation);
		System.out.println("===========================================");
		System.out.printf("d' = %4f\n",dPrime);
		System.out.println("===========================================");*/

		return returnVal;
	}
}
