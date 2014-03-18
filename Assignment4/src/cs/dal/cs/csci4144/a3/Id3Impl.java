package cs.dal.cs.csci4144.a3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Id3Impl {

	//Keep a count of the num of Attributes for convenience 
	int numAttributes;
	
	//The type of data that we are dealing with 
	ArrayList<String> attributes;	
	ArrayList<ArrayList<String>> domains;

	//The root of the ID3 tree that we will be creating
	Id3Node root = new Id3Node();

	//Find the location of string input given the attribute
	public int getValue(int attribute, String input) {
		//Get the attribute and Use Array list to look for the input String
		int index = domains.get(attribute).indexOf(input);
		//This is if hit a case where the string was not found. Most likely our 
		//mistake and we will add it to the tree
		if (index < 0) {
			//Add input
			domains.get(attribute).add(input);
			//Since we just added it we know its at the end of the tree
			return domains.get(attribute).size() -1;
		}
		return index;
	}

	//Only used to print out the data to the cmd. Gets all values from each node based on the attribute
	public ArrayList<Integer> getValues(ArrayList<Id3Entry> data, int attribute) {
		//Just the string to hold output from the queries from the ArrayList
		String output = "";
		
		//Just a temp ArrayList to hold the value as we find them
		ArrayList<String> values = new ArrayList<String>();
		
		//Going to loop through data and get the values for the target attribute
		for (Id3Entry point: data) {
			//Get the value from the current point
			output = domains.get(attribute).get(point.attributes.get(attribute) );
			//Make sure that we do not already have the value
			if (values.indexOf(output) < 0) {
				//Because we do not have the output value already add it to the list
				values.add(output);
			}
		}
		//Now that we have a rough list of the values that we want we can officially add
		//them to the needed places
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i=0; i< values.size(); i++) {
			output = values.get(i);
			//Get the location of the String in the domain, easy for accessing later.
			result.add(i,domains.get(attribute).indexOf(output));
		}
		return result;
	}

	//Just get the subset of the provided node given a value
	public ArrayList<Id3Entry> getSubset(ArrayList<Id3Entry> data, int attribute, int value) {
		//Holds the subset of the nodes
		ArrayList<Id3Entry> subset = new ArrayList<Id3Entry>();
		
		//Loop through all the data points of the node
		for (Id3Entry point: data) {
			//Found a point that has a value that we want
			if (point.attributes.get(attribute) == value) 
				subset.add(point);
		}
		return subset;

	}

	//Calculate the Entropy of a nodes data
	public double calEntropy(ArrayList<Id3Entry> data) {
		
		//Well if there is no data then the Entropy must be zero
		if (data.size() == 0) 
			return 0;
		
		//The sum will hold the entropy of the data of the node
		double sum = 0;
		//We are going to go through all the attributes for each node
		for (int i=0; i< domains.get(numAttributes-1).size(); i++) {
			//count to see if a node has a value for said attribute
			int count=0;
			//Each point of of the node
			for (Id3Entry point: data) {
				//Point has the attribute
				if (point.attributes.get(numAttributes-1) == i) 
					count++;
			}
			
			//If the count is greater then 0 then the prob may be higher then 0
			if (count > 0){
				//find the prob
				double probability = 1.*count/data.size();
				//The sum as defined by the ID3 algorithm
				sum += -probability*Math.log(probability);
			}
		}
		return sum;

	}


	public boolean isDecompose(Id3Node node, int attribute) {
		if (node.children != null) {
			if (node.decompositionAttribute == attribute )
				return true;
		}
		if (node.parent == null) 
			return false;

		return isDecompose(node.parent, attribute);
	}


	public void decompose(Id3Node node) {
		
		boolean picked=false;
		double entropy=0;
		int attribute=0;
		

		node.entropy = calEntropy(node.data);

		if (node.entropy == 0) 
			return;

		for (int i=0; i< numAttributes-1; i++) {
			int numvalues = domains.get(i).size();
			if (isDecompose(node, i)) 
				continue;
			double aventropy = 0;
			for (int j=0; j< numvalues; j++) {
				ArrayList<Id3Entry> subset = getSubset(node.data, i, j);
				if (subset.size() == 0) 
					continue;
				double subsetEntropy = calEntropy(subset);
				aventropy += subsetEntropy * subset.size();  
			}

			aventropy = aventropy / node.data.size();
			if (!picked) {
				picked = true;
				entropy = aventropy;
				attribute = i;
			} else {
				if (aventropy < entropy) {
					picked = true;
					entropy = aventropy;
					attribute = i;
				}
			}

		}

		if (!picked) 
			return;

		int numvalues = domains.get(attribute).size();
		node.decompositionAttribute = attribute;
		node.children = new Id3Node [numvalues];
		for (int j=0; j< numvalues; j++) {
			node.children[j] = new Id3Node();
			node.children[j].parent = node;
			node.children[j].data = getSubset(node.data, attribute, j);
			node.children[j].decompositionValue = j;
		}


		for (int j=0; j< numvalues; j++) {
			decompose(node.children[j]);
		}

	}


	public boolean readData(String filename)  throws Exception {

		FileInputStream in = null;

		try {
			File inputFile = new File(filename);
			in = new FileInputStream(inputFile);
		} catch ( Exception e) {
			return false;
		}

		BufferedReader bin = new BufferedReader(new InputStreamReader(in));

		String input= bin.readLine();

		StringTokenizer tokenizer = new StringTokenizer(input);
		numAttributes = tokenizer.countTokens();
		if (numAttributes <= 1) {
			bin.close();
			return false;
		}

		domains = new ArrayList<ArrayList<String>>(numAttributes);
		for (int i=0; i < numAttributes; i++) 
			domains.add(i, new ArrayList<String>());
		
		
		attributes = new ArrayList<String>(numAttributes);
		for (int i=0; i < numAttributes; i++) {
			attributes.add(i,tokenizer.nextToken());
		}

		
		input = bin.readLine();

		while(input != null) {


			tokenizer = new StringTokenizer(input);
			int numtokens = tokenizer.countTokens();
			if (numtokens != numAttributes) {
				bin.close();
				return false;
			}

			Id3Entry point = new Id3Entry(numAttributes);
			for (int i=0; i < numAttributes; i++) {
				point.attributes.add(i, getValue(i, tokenizer.nextToken() ));
			}
			root.data.add(point);

			input = bin.readLine();

		}

		bin.close();

		return true;

	}	

	public void printTree(Id3Node node, String tab, boolean newLine) {
		if (node.children == null) {
			ArrayList<Integer> values = getValues(node.data, numAttributes-1 );
			if (values.size() == 1) {
				System.out.println( attributes.get(numAttributes-1) + " is " + domains.get(numAttributes-1).get(values.get(0)));
				newLine = false;
				return;
			}
			if(values.size() == 0){
				newLine = false;
				return;
			}
			System.out.print(tab + "\t" + attributes.get(numAttributes-1));
			for (int i=0; i < values.size(); i++) {
				System.out.print(domains.get(numAttributes-1).get(values.get(i)));
				if ( i != values.size()-1 ){
					System.out.print( " , " );
				}
			}
			System.out.println();
			newLine = false;
			return;
		}

		int numvalues = node.children.length;
		for (int i=0; i < numvalues; i++) {
			if(hasChilderen(i, node)){
				if(newLine)
					System.out.println();
				System.out.print(tab + "if " + attributes.get(node.decompositionAttribute) + " is " +
						domains.get(node.decompositionAttribute).get(i) + ", then " );
				newLine = true;
			}
			printTree(node.children[i], tab + "\t", newLine);
		}


	}

	public boolean hasChilderen(int place, Id3Node node){
		int outputattr = numAttributes-1;

		if(node.children[place].data != null){
			ArrayList<Integer> childrenValues = getValues(node.children[place].data, outputattr);
			if(childrenValues.size() == 0)
				return false;
		}
		return true;
	}
	
	public Id3Node getRoot(){
		return root;
	}

}
