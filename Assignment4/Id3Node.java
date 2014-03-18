
import java.util.ArrayList;

public class Id3Node {

	public double entropy;			
	public ArrayList<Id3Entry> data;			
	public int decompositionAttribute;	
	public int decompositionValue;		
	public Id3Node []children;		
	public Id3Node parent;			
	
	public Id3Node() {
		data = new ArrayList<Id3Entry>();
	}
}
