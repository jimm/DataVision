package jimm.datavision.testdata;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class AggregateTestData {

protected String col1;
protected String col2;
protected int value;

public static Iterator aggregateTestData(String filePath) {
    ArrayList data = new ArrayList();
    try {
	BufferedReader in =
	    new BufferedReader(new FileReader(filePath));
	String line;
	while ((line = in.readLine()) != null) {
	    int pos1 = line.indexOf(',');
	    int pos2 = line.indexOf(',', pos1+1);
	    String col1 = line.substring(0, pos1);
	    String col2 = line.substring(pos1 + 1, pos2);
	    int value = Integer.parseInt(line.substring(pos2 + 1));
	    data.add(new AggregateTestData(col1, col2, value));
	}
    }
    catch (IOException ioe) {
	System.err.println(ioe);
    }
    return data.iterator();
}

public AggregateTestData(String c1, String c2, int v) {
    col1 = c1;
    col2 = c2;
    value = v;
}

public String col1() { return col1; }
public String col2() { return col2; }
public int value() { return value; }

public static void main(String[] args) {
    Iterator iter = AggregateTestData.aggregateTestData("aggregate_test.dat");
    while (iter.hasNext()) {
	AggregateTestData data = (AggregateTestData)iter.next();
	System.out.println(data.col1 + ", " + data.col2 + ", " + data.value);
    }
}

}
