
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Id3Test {
	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("What is the name of the file containing your data?");

		String filename = br.readLine();

		Id3Impl newId3Instance = new Id3Impl();	

		boolean status = newId3Instance.readData(filename);
		if (!status){
			System.out.println("There was a problem reading the file!");
			System.exit(-1);
		}
		
		newId3Instance.decompose(newId3Instance.getRoot());
		newId3Instance.printTree(newId3Instance.getRoot(), "", false);

	}
}
