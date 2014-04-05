

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

public class navieBayesDemo {

    public static void main(String[] args) {
    	
    	Scanner input = new Scanner(System.in);
        final Classifier<String, String> bayes =
                new BayesClassifier<String, String>();
    	
    	System.out.print("Please enter a training file: ");
    	String trainingFile = input.nextLine();
    	System.out.print("Please enter a testing file: ");
    	String testingFile = input.nextLine();
    	boolean firstline = true;
    	String attribute;
    	int intAttribute = 0;
    	try(BufferedReader br = new BufferedReader(new FileReader(trainingFile))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	    	if(firstline){
    	    		String[] r = line.split("\\s");
    	    		System.out.println("Please choose an attribute (by number):");
    	    		System.out.println("Attribute:");
    	    		for(int i = 0; i < r.length;i++){
    	    			System.out.println((i+1) + ". " + r[i]);
    	    		}
    	    		System.out.println("Attribute: ");
    	    		attribute = input.nextLine();
    	    		intAttribute = Integer.parseInt(attribute) - 1;
    	    		firstline = false;
    	    		continue;
    	    	}
    	    	
    	    	 String[] r = line.split("\\s");
    	    	 if(!line.equals(""))
    	    		 bayes.learn(r[intAttribute], Arrays.asList(r));
    	    	 else
    	    		 continue;
    	    }
    	    // line is not visible here.
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	try(BufferedReader br = new BufferedReader(new FileReader(testingFile))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	    	 String temp = 
    	                 bayes.classify(Arrays.asList(line)).getCategory();
    	    	 System.out.println(temp);
    	    }
    	    // line is not visible here.
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Done");
    }

}
