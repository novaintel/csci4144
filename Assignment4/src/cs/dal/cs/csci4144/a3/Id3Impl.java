package cs.dal.cs.csci4144.a3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Id3Impl {

	int numAttributes;
	ArrayList<String> attributeNames;	

	ArrayList<ArrayList<String>> domains;

	Id3Node root = new Id3Node();

	public int getSymbolValue(int attribute, String symbol) {
		int index = domains.get(attribute).indexOf(symbol);
		if (index < 0) {
			domains.get(attribute).add(symbol);
			return domains.get(attribute).size() -1;
		}
		return index;
	}

	public ArrayList<Integer> getAllValues(ArrayList<Id3Entry> data, int attribute) {
		ArrayList<String> values = new ArrayList<String>();
		int num = data.size();
		for (int i=0; i< num; i++) {
			Id3Entry point = data.get(i);
			String symbol = domains.get(attribute).get(point.attributes.get(attribute) );
			int index = values.indexOf(symbol);
			if (index < 0) {
				values.add(symbol);
			}
		}

		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i=0; i< values.size(); i++) {
			String symbol = values.get(i);
			result.add(i,domains.get(attribute).indexOf(symbol));
		}
		return result;
	}


	public ArrayList<Id3Entry> getSubset(ArrayList<Id3Entry> data, int attribute, int value) {
		
		ArrayList<Id3Entry> subset = new ArrayList<Id3Entry>();

		int num = data.size();
		for (int i=0; i< num; i++) {
			Id3Entry point = data.get(i);
			if (point.attributes.get(attribute) == value) 
				subset.add(point);
		}
		return subset;

	}


	public double calculateEntropy(ArrayList<Id3Entry> data) {
		
		if (data.size() == 0) 
			return 0;

		int attribute = numAttributes-1;
		int numvalues = domains.get(attribute).size();
		double sum = 0;
		for (int i=0; i< numvalues; i++) {
			int count=0;
			for (int j=0; j< data.size(); j++) {
				Id3Entry point = data.get(j);
				if (point.attributes.get(attribute) == i) count++;
			}
			double probability = 1.*count/data.size();
			if (count > 0) sum += -probability*Math.log(probability);
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


	public void decomposeNode(Id3Node node) {

		double bestEntropy=0;
		boolean selected=false;
		int selectedAttribute=0;

		node.entropy = calculateEntropy(node.data);

		if (node.entropy == 0) 
			return;

		for (int i=0; i< numAttributes-1; i++) {
			int numvalues = domains.get(i).size();
			if ( isDecompose(node, i) ) continue;
			double averageentropy = 0;
			for (int j=0; j< numvalues; j++) {
				ArrayList<Id3Entry> subset = getSubset(node.data, i, j);
				if (subset.size() == 0) 
					continue;
				double subentropy = calculateEntropy(subset);
				averageentropy += subentropy * subset.size();  
			}

			averageentropy = averageentropy / node.data.size();
			if (selected == false) {
				selected = true;
				bestEntropy = averageentropy;
				selectedAttribute = i;
			} else {
				if (averageentropy < bestEntropy) {
					selected = true;
					bestEntropy = averageentropy;
					selectedAttribute = i;
				}
			}

		}

		if (selected == false) return;

		int numvalues = domains.get(selectedAttribute).size();
		node.decompositionAttribute = selectedAttribute;
		node.children = new Id3Node [numvalues];
		for (int j=0; j< numvalues; j++) {
			node.children[j] = new Id3Node();
			node.children[j].parent = node;
			node.children[j].data = getSubset(node.data, selectedAttribute, j);
			node.children[j].decompositionValue = j;
		}


		for (int j=0; j< numvalues; j++) {
			decomposeNode(node.children[j]);
		}


		node.data = null;

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
		
		
		attributeNames = new ArrayList<String>(numAttributes);
		for (int i=0; i < numAttributes; i++) {
			attributeNames.add(i,tokenizer.nextToken());
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
				point.attributes.add(i, getSymbolValue(i, tokenizer.nextToken() ));
			}
			root.data.add(point);

			input = bin.readLine();

		}

		bin.close();

		return true;

	}	

	public void printTree(Id3Node node, String tab, boolean newLine) {
		if (node.children == null) {
			ArrayList<Integer> values = getAllValues(node.data, numAttributes-1 );
			if (values.size() == 1) {
				System.out.println( attributeNames.get(numAttributes-1) + " is " + domains.get(numAttributes-1).get(values.get(0)));
				newLine = false;
				return;
			}
			if(values.size() == 0){
				newLine = false;
				return;
			}
			System.out.print(tab + "\t" + attributeNames.get(numAttributes-1));
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
				System.out.print(tab + "if " + attributeNames.get(node.decompositionAttribute) + " is " +
						domains.get(node.decompositionAttribute).get(i) + ", then " );
				newLine = true;
			}
			printTree(node.children[i], tab + "\t", newLine);
		}


	}

	public boolean hasChilderen(int place, Id3Node node){
		int outputattr = numAttributes-1;

		if(node.children[place].data != null){
			ArrayList<Integer> childrenValues = getAllValues(node.children[place].data, outputattr);
			if(childrenValues.size() == 0)
				return false;
		}
		return true;
	}
	
	public Id3Node getRoot(){
		return root;
	}

}
