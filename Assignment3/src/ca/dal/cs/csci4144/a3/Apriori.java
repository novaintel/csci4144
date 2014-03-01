package ca.dal.cs.csci4144.a3;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Apriori {

	private double minSupport;
	private double minRate;
	private String inputDataFileName;
	private double currentItemSet;

	private ArrayList<String> startingData;

	class finalDataNode{
		String implyData;
		String resultData;

		double support;
		double confidence;


		public finalDataNode(String implyData, String resultData,
				double support, double confidence) {
			super();
			this.implyData = implyData;
			this.resultData = resultData;
			this.support = support;
			this.confidence = confidence;
		}

		public String getImplyData() {
			return implyData;
		}
		public String getResultData() {
			return resultData;
		}
		public double getSupport() {
			return support;
		}
		public double getConfidence() {
			return confidence;
		}
	}

	private ArrayList<finalDataNode> ruleList;

	private ArrayList<String> dataTypes;
	private ArrayList<String> data;

	public Apriori(){
		minSupport = 0.0;
		minRate = 0.0;
		currentItemSet = 0;
		inputDataFileName = "";

		ruleList = new ArrayList<finalDataNode>();

		startingData = new ArrayList<String>();
		dataTypes = new ArrayList<String>();
		data = new ArrayList<String>();
	}


	public void runApriori(){
		getConsoleInput();
		loadData();
		buildFirstItemSet();
		currentItemSet++;
		do{
			currentItemSet++;
			generateCandidates();
		}while(true);
	}

	protected void getConsoleInput(){

		Scanner keyboard = new Scanner(System.in);

		System.out.println("What is the anme of the gile containing your data?");
		inputDataFileName = keyboard.nextLine();

		System.out.println("Please select the minium support rate(0.00-1.00):");
		minRate = keyboard.nextDouble();

		System.out.println("Please select the minimum confidence rate(0.00-1.00):");
		minSupport = keyboard.nextDouble();

		keyboard.close();

	}

	public double getMinSupport() {
		return minSupport;
	}

	public double getMinRate() {
		return minRate;
	}

	public String getInputDataFileName() {
		return inputDataFileName;
	}

	public double getCurrentItemSet() {
		return currentItemSet;
	}

	private void loadData(){

		BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(inputDataFileName));

			while ((sCurrentLine = br.readLine()) != null) {
				if(dataTypes.isEmpty()){
					String[] words = sCurrentLine.split("\\s+");
					for(int i = 0; i < words.length;i++){
						dataTypes.add(words[i]);
					}
					continue;
				}
				String[] words = sCurrentLine.split("\\s+");
				for(int i = 0; i < words.length;i++){
					startingData.add(dataTypes.get(i) + "=" + words[i]);
					data.add(dataTypes.get(i) + "=" + words[i]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void buildFirstItemSet(){
		ArrayList<String> tempCandidates = new ArrayList<String>();
		String candidateOne, candidateTwo;
		StringTokenizer tokenOne, tokenTwo;

		for(int i=0; i<data.size(); i++)
		{
			tokenOne = new StringTokenizer(data.get(i));
			candidateOne = tokenOne.nextToken();
			for(int j=i+1; j<data.size(); j++)
			{
				tokenTwo = new StringTokenizer(data.get(j));
				candidateTwo = tokenTwo.nextToken();
				tempCandidates.add(candidateOne + " " + candidateTwo);
			}
		}
	}

	private void generateCandidates(){

		ArrayList<String> tempCandidates = new ArrayList<String>();
		String candidateOne, candidateTwo;
		StringTokenizer tokenOne, tokenTwo;

		for(int i=0; i<data.size(); i++)
		{
			for(int j=i+1; j<data.size(); j++)
			{
				candidateOne = new String();
				candidateTwo = new String();
				tokenOne = new StringTokenizer(data.get(i));
				tokenTwo = new StringTokenizer(data.get(j));

				for(int s=0; s<currentItemSet-2; s++)
				{
					candidateOne = candidateOne + " " + tokenOne.nextToken();
					candidateTwo = candidateTwo + " " + tokenTwo.nextToken();
				}

				if(candidateTwo.compareToIgnoreCase(candidateOne)==0)
					tempCandidates.add((candidateOne + " " + tokenOne.nextToken() + " " + tokenTwo.nextToken()).trim());
			}
		}

	}

	private void countItemSets(){

		boolean match;
		int countAB[] = new int[data.size()];
		int countA[] = new int[data.size()];

		for(int i=0; i<(startingData.size()/dataTypes.size()); i++)
		{
			// check each candidate
			for (int c = 0; c < data.size(); c++) {
				match = true; // reset match to false
				String[] words = data.get(c).split("\\s+");
			
				if (match) {
					countAB[c]++;
				}
			}
		}

		for (int i = 0; i < data.size(); i++) {
			if ((countAB[i] / (double) ((startingData.size()/dataTypes.size()))) >= minSupport) {

				double support = (countAB[i] / (double) ((startingData.size()/dataTypes.size())));

				String[] words = data.get(i).split("\\s+");
				String impliesData = "";

				for(int j = 0;j < words.length - 1;j++)
					impliesData = impliesData + " " + words[j];

				ruleList.add(new finalDataNode(impliesData,words[words.length - 1],support,0.0));

			}
		}

	}

}


