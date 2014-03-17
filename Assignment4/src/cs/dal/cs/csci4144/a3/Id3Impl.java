package cs.dal.cs.csci4144.a3;

import java.io.*;
import java.util.*;

public class Id3Impl {

	int numAttributes;
	ArrayList<String> attributeNames;	
	
	ArrayList<ArrayList<String>> domains;

	
	class DataPoint {

		public ArrayList<Integer> attributes;

		public DataPoint(int numattributes) {
			attributes = new ArrayList<Integer>(numattributes);
		}
	};

	class TreeNode {
		public double entropy;			
		public ArrayList data;			
		public int decompositionAttribute;	
		public int decompositionValue;		
		public TreeNode []children;		
		public TreeNode parent;			
		
		public TreeNode() {
			data = new ArrayList();
		}

	};

	/*  The root of the decomposition tree  */
	TreeNode root = new TreeNode();


	/*  This function returns an integer corresponding to the symbolic value of the attribute.
		If the symbol does not exist in the domain, the symbol is added to the domain of the attribute
	*/
	public int getSymbolValue(int attribute, String symbol) {
		int index = domains.get(attribute).indexOf(symbol);
		if (index < 0) {
			domains.get(attribute).add(symbol);
			return domains.get(attribute).size() -1;
		}
		return index;
	}

	/*  Returns all the values of the specified attribute in the data set  */
	public int []getAllValues(ArrayList data, int attribute) {
		ArrayList values = new ArrayList();
		int num = data.size();
		for (int i=0; i< num; i++) {
			DataPoint point = (DataPoint)data.get(i);
			String symbol = (String)domains.get(attribute).get(point.attributes.get(attribute) );
			int index = values.indexOf(symbol);
			if (index < 0) {
				values.add(symbol);
			}
		}

		int []array = new int[values.size()];
		for (int i=0; i< array.length; i++) {
			String symbol = (String)values.get(i);
			array[i] = domains.get(attribute).indexOf(symbol);
		}
		values = null;
		return array;
	}


	/*  Returns a subset of data, in which the value of the specfied attribute of all data points is the specified value  */
	public ArrayList getSubset(ArrayList data, int attribute, int value) {
		ArrayList subset = new ArrayList();

		int num = data.size();
		for (int i=0; i< num; i++) {
			DataPoint point = (DataPoint)data.get(i);
			if (point.attributes.get(attribute) == value) subset.add(point);
		}
		return subset;

	}


	/*  Calculates the entropy of the set of data points.
		The entropy is calculated using the values of the output attribute which is the last element in the array attribtues
	*/
	public double calculateEntropy(ArrayList data) {

		int numdata = data.size();
		if (numdata == 0) return 0;

		int attribute = numAttributes-1;
		int numvalues = domains.get(attribute).size();
		double sum = 0;
		for (int i=0; i< numvalues; i++) {
			int count=0;
			for (int j=0; j< numdata; j++) {
				DataPoint point = (DataPoint)data.get(j);
				if (point.attributes.get(attribute) == i) count++;
			}
			double probability = 1.*count/numdata;
			if (count > 0) sum += -probability*Math.log(probability);
		}
		return sum;

	}

	/*  This function checks if the specified attribute is used to decompose the data set
		in any of the parents of the specfied node in the decomposition tree.
		Recursively checks the specified node as well as all parents
	*/
	public boolean alreadyUsedToDecompose(TreeNode node, int attribute) {
		if (node.children != null) {
			if (node.decompositionAttribute == attribute )
				return true;
		}
		if (node.parent == null) return false;
		return alreadyUsedToDecompose(node.parent, attribute);
	}

	/*  This function decomposes the specified node according to the ID3 algorithm.
		Recursively divides all children nodes until it is not possible to divide any further
                I have changed this code from my earlier version. I believe that the code
                in my earlier version prevents useless decomposition and results in a better decision tree!
                This is a more faithful implementation of the standard ID3 algorithm
	*/
	public void decomposeNode(TreeNode node) {

		double bestEntropy=0;
		boolean selected=false;
		int selectedAttribute=0;

		int numdata = node.data.size();
		int numinputattributes = numAttributes-1;
                node.entropy = calculateEntropy(node.data);
		if (node.entropy == 0) return;

		/*  In the following two loops, the best attribute is located which
			causes maximum decrease in entropy
		*/
		for (int i=0; i< numinputattributes; i++) {
			int numvalues = domains.get(i).size();
                        if ( alreadyUsedToDecompose(node, i) ) continue;
                        // Use the following variable to store the entropy for the test node created with the attribute i
                        double averageentropy = 0;
			for (int j=0; j< numvalues; j++) {
				ArrayList subset = getSubset(node.data, i, j);
				if (subset.size() == 0) continue;
				double subentropy = calculateEntropy(subset);
                                averageentropy += subentropy * subset.size();  // Weighted sum
			}

                        averageentropy = averageentropy / numdata;   // Taking the weighted average
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

		// Now divide the dataset using the selected attribute
                int numvalues = domains.get(selectedAttribute).size();
		node.decompositionAttribute = selectedAttribute;
		node.children = new TreeNode [numvalues];
                for (int j=0; j< numvalues; j++) {
                  node.children[j] = new TreeNode();
                  node.children[j].parent = node;
                  node.children[j].data = getSubset(node.data, selectedAttribute, j);
                  node.children[j].decompositionValue = j;
                }

		// Recursively divides children nodes
                for (int j=0; j< numvalues; j++) {
                  decomposeNode(node.children[j]);
                }

		// There is no more any need to keep the original vector.  Release this memory
		node.data = null;		// Let the garbage collector recover this memory

	}


   	/** Function to read the data file.
		The first line of the data file should contain the names of all attributes.
		The number of attributes is inferred from the number of words in this line.
		The last word is taken as the name of the output attribute.
		Each subsequent line contains the values of attributes for a data point.
		If any line starts with // it is taken as a comment and ignored.
		Blank lines are also ignored.
   	*/
   	public int readData(String filename)  throws Exception {

      		FileInputStream in = null;

      		try {
         		File inputFile = new File(filename);
	 		in = new FileInputStream(inputFile);
      		} catch ( Exception e) {
			System.err.println( "Unable to open data file: " + filename + "\n" + e);
			return 0;
      		}

      		BufferedReader bin = new BufferedReader(new InputStreamReader(in) );

		String input;
      		while(true) {
        		input = bin.readLine();
			if (input == null) {
				System.err.println( "No data found in the data file: " + filename + "\n");
				return 0;
			}
			if (input.startsWith("//")) continue;
			if (input.equals("")) continue;
			break;
		}


     		StringTokenizer tokenizer = new StringTokenizer(input);
		numAttributes = tokenizer.countTokens();
		if (numAttributes <= 1) {
			System.err.println( "Read line: " + input);
			System.err.println( "Could not obtain the names of attributes in the line");
			System.err.println( "Expecting at least one input attribute and one output attribute");
			return 0;
		}

		domains = new ArrayList<ArrayList<String>>(numAttributes);
		for (int i=0; i < numAttributes; i++) domains.add(i, new ArrayList<String>());
		attributeNames = new ArrayList<String>(numAttributes);

     		for (int i=0; i < numAttributes; i++) {
         		attributeNames.add(i,tokenizer.nextToken());
     		}


      		while(true) {
        		input = bin.readLine();
			if (input == null) break;
			if (input.startsWith("//")) continue;
			if (input.equals("")) continue;

			tokenizer = new StringTokenizer(input);
			int numtokens = tokenizer.countTokens();
			if (numtokens != numAttributes) {
				System.err.println( "Read " + root.data.size() + " data");
				System.err.println( "Last line read: " + input);
				System.err.println( "Expecting " + numAttributes  + " attributes");
				return 0;
			}

			DataPoint point = new DataPoint(numAttributes);
     			for (int i=0; i < numAttributes; i++) {
         			point.attributes.add(i, getSymbolValue(i, tokenizer.nextToken() ));
     			}
			root.data.add(point);

		}

		bin.close();

      		return 1;

   	}	// End of function readData
   	//-----------------------------------------------------------------------

	/*  This function prints the decision tree in the form of rules.
		The action part of the rule is of the form
			outputAttribute = "symbolicValue"
		or
			outputAttribute = { "Value1", "Value2", ..  }
		The second form is printed if the node cannot be decomposed any further into an homogenous set
	*/
	public void printTree(TreeNode node, String tab) {

		int outputattr = numAttributes-1;

		if (node.children == null) {
			int []values = getAllValues(node.data, outputattr );
			if (values.length == 1) {
				System.out.println(tab + "\t" + attributeNames.get(outputattr) + " = \"" + domains.get(outputattr).get(values[0]) + "\";");
				return;
			}
			System.out.print(tab + "\t" + attributeNames.get(outputattr) + " = {");
			for (int i=0; i < values.length; i++) {
				System.out.print("\"" + domains.get(outputattr).get(values[i]) + "\" ");
				if ( i != values.length-1 ) System.out.print( " , " );
			}
			System.out.println( " };");
			return;
		}

		int numvalues = node.children.length;
                for (int i=0; i < numvalues; i++) {
                  System.out.println(tab + "if( " + attributeNames.get(node.decompositionAttribute) + " == \"" +
                          domains.get(node.decompositionAttribute).get(i) + "\") {" );
                  printTree(node.children[i], tab + "\t");
                  if (i != numvalues-1) System.out.print(tab +  "} else ");
                  else System.out.println(tab +  "}");
                }


	}

	/*  This function creates the decision tree and prints it in the form of rules on the console
	*/
	public void createDecisionTree() {
		decomposeNode(root);
		printTree(root, "");
	}


  	/* Here is the definition of the main function */
   	public static void main(String[] args) throws Exception {

      		int num = args.length;
		if (num != 1) {
      			System.out.println("You need to specify the name of the datafile at the command line " );
			return;
		}


		Id3Impl me = new Id3Impl();

		long startTime = System.currentTimeMillis();	//  To print the time taken to process the data

		int status = me.readData(args[0]);
		if (status <= 0) return;

		me.createDecisionTree();


		long endTime = System.currentTimeMillis();
		long totalTime = (endTime-startTime)/1000;

		System.out.println( totalTime + " Seconds");


   	}
   	/*  End of the main function  */	
}
