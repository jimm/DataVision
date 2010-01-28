package jimm.datavision.test;
import jimm.datavision.source.charsep.DelimParser;
import jimm.util.StringUtils;
import java.io.*;
import java.util.List;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Compares CSV file input with "answers" file. The answers file is
 * tab-delimited and lines that end with a backslash are continued
 * on the next line.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DelimParserTest extends TestCase {

protected static final String TEST_INPUT =
    AllTests.testDataFile("delim_parser_in.txt");
protected static final String TEST_ANSWERS =
    AllTests.testDataFile("delim_parser_answers.txt");

public static Test suite() {
    return new TestSuite(DelimParserTest.class);
}

public DelimParserTest(String name) {
    super(name);
}

protected void sepTest(List answer, DelimParser parser) {
    List parsed = null;
    try {
	parsed = parser.parse();
    }
    catch (IOException e) {
	fail(e.toString());
    }
    assertEquals(answer, parsed);
}

public void testParser() {
    BufferedReader in = null, answers = null;
    try {
	in = new BufferedReader(new FileReader(TEST_INPUT));
	answers = new BufferedReader(new FileReader(TEST_ANSWERS));
	DelimParser parser = new DelimParser(in, ',');

	List answer;
	while ((answer = getNextAnswer(answers)) != null)
	    sepTest(answer, parser);
	sepTest(null, parser);
    }
    catch (Exception e) {
	fail(e.toString());
    }
    finally {
	try {
	    if (answers != null) answers.close();
	    if (in != null) in.close();
	}
	catch (Exception e2) {}
    }
}

protected List getNextAnswer(BufferedReader in) throws IOException {
    String line = in.readLine();
    if (line == null)
	return null;

    while (line.endsWith("\\")) {
	line = line.substring(0, line.length() - 1);
	line += "\n";
	line += in.readLine();
    }
    return StringUtils.split(line, "\t");
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
