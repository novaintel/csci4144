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
	//Holds the values of each entry based on the picked target attribute for each node. Entries are added in getValue()
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
			//We haven't added 
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
			output = domains.get(attribute).get(point.dataValues.get(attribute) );
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
			if (point.dataValues.get(attribute) == value) 
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
				if (point.dataValues.get(numAttributes-1) == i) 
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


	//Just a small check to make sure that this node was not already decomposed saves 
	//time then redoing it
	public boolean isDecompose(Id3Node node, int attribute) {
		//Hit a leaf node
		if (node.children != null) {
			//If the attribute that was used to decompose the node is the same 
			//as the provided attribute then we have decomposed this node
			if (node.decompositionAttribute == attribute )
				return true;
		}
		
		//Just to make sure then when we are going back up the parent is not null.
		if (node.parent == null) 
			return false;

		return isDecompose(node.parent, attribute);
	}


	//The main algo for the ID3 impl
	public void decompose(Id3Node node) {
		
		//We have pick this node to be a rule
		boolean picked=false;
		//the entropy of a node
		double entropy=0;
		//the current attribute that we are looking at
		int attribute=0;
		

		//The current node of the tree that we are working with, we calculate its
		//entropy
		node.entropy = calEntropy(node.data);

		//If its 0 then we do not have to go any further
		if (node.entropy == 0) 
			return;

		
		for (int i=0; i< numAttributes-1; i++) {
			//Allready checked this node
			if (isDecompose(node, i)) 
				continue;
			double aventropy = 0;
			for (int j=0; j< domains.get(i).size(); j++) {
				ArrayList<Id3Entry> subset = getSubset(node.data, i, j);
				//There is nothing bellow this node so we work our way to the next
				if (subset.size() == 0) 
					continue;
				//get the entropy for the subset
				double subsetEntropy = calEntropy(subset);
				//check for the average for each node in the subset
				aventropy += subsetEntropy * subset.size();  
			}

			//Now check what the average entropy for each of the data entries in the current node
			aventropy = aventropy / node.data.size();
			//Set that we have pick this node to create rule
			if (!picked) {
				picked = true;
				entropy = aventropy;
				attribute = i;
			} else {
				//We found a better attribute to create a rule for this node
				if (aventropy < entropy) {
					picked = true;
					entropy = aventropy;
					attribute = i;
				}
			}

		}

		//If we got to this point then its most likely that we check this node or its a leaf node
		if (!picked) 
			return;

		//We found an attribute for the node so lets create the node and populate it.
		node.decompositionAttribute = attribute;
		//Set it up so that we can add children
		node.children = new Id3Node [domains.get(attribute).size()];
		//Populate the node
		for (int j=0; j< domains.get(attribute).size(); j++) {
			node.children[j] = new Id3Node();
			node.children[j].parent = node;
			node.children[j].data = getSubset(node.data, attribute, j);
			node.children[j].decompositionValue = j;
		}

		//Now work on the children that we have just created 
		for (int j=0; j< domains.get(attribute).size(); j++) {
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

		//We will get ready to add the unsorted data
		domains = new ArrayList<ArrayList<String>>(numAttributes);
		for (int i=0; i < numAttributes; i++) 
			domains.add(i, new ArrayList<String>());
		
		//Get the first line from the file as these are the attributes for all entries
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

			//We create a new entery for the new line
			Id3Entry entry = new Id3Entry(numAttributes);
			for (int i=0; i < numAttributes; i++) {
				//add all the values from the current line
				entry.dataValues.add(i, getValue(i, tokenizer.nextToken() ));
			}
			//As the root node covers all cases we add the entry to the root node.
			root.data.add(entry);

			input = bin.readLine();

		}

		bin.close();

		return true;

	}	

	public void printTree(Id3Node node, String tab, boolean newLine) {
		//Hit a leaf node as such we can print the "then" attribute
		if (node.children == null) {
			//Want to get the values of the node
			ArrayList<Integer> values = getValues(node.data, numAttributes-1 );
			//Case in which there are only one outcome for the if clause
			if (values.size() == 1) {
				System.out.println( attributes.get(numAttributes-1) + " is " + domains.get(numAttributes-1).get(values.get(0)));
				newLine = false;
				return;
			}
			//There are no values so go back
			if(values.size() == 0){
				newLine = false;
				return;
			}
			//Go through and list all the "then" values
			System.out.print(tab + "\t" + attributes.get(numAttributes-1));
			for (int i=0; i < values.size(); i++) {
				System.out.print(domains.get(numAttributes-1).get(values.get(i)));
				if ( i != values.size()-1 ){
					System.out.print( " , " );
				}
			}
			//next entry
			System.out.println();
			newLine = false;
			return;
		}

		//This section covers the If part of the rule
		int numvalues = node.children.length;
		for (int i=0; i < numvalues; i++) {
			//checks to see if there are subsets of the rule
			if(hasChilderen(i, node)){
				if(newLine)
					System.out.println();
				//Print out the If clause
				System.out.print(tab + "if " + attributes.get(node.decompositionAttribute) + " is " +
						domains.get(node.decompositionAttribute).get(i) + ", then " );
				newLine = true;
			}
			//Work on the children
			printTree(node.children[i], tab + "\t", newLine);
		}


	}
	
	//Just a hack method for checking if a new line should be done
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
