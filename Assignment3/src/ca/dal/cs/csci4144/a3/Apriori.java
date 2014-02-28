package ca.dal.cs.csci4144.a3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Apriori {

	private double minSupport;
	private double minRate;
	private String inputDataFileName;
	
	private ArrayList<String> dataTypes;
	private ArrayList<String> data;
	
	public Apriori(){
		minSupport = 0.0;
		minRate = 0.0;
		inputDataFileName = "";
		dataTypes = new ArrayList<String>();
		data = new ArrayList<String>();
	}

	
	public void runApriori(){
		getConsoleInput();
		loadData();
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
	
	private void generateCandidates(int currentItemSet){
		
	}

}
