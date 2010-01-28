package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.layout.CharSepLE;
import jimm.datavision.source.charsep.CharSepSource;
import java.io.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Tests formula evals when formulas are hidden or appear multiple
 * times.
 * <p>
 * These tests are tightly coupled with the contents of the files
 * <code>eval.xml</code> and <code>eval.csv</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FormulaEvalTest extends TestCase {

protected static final File EVAL_REPORT =
    new File(AllTests.testDataFile("eval.xml"));
protected static final String EVAL_DATA_FILE =
    AllTests.testDataFile("eval.csv");
protected static final File OUT_FILE =
    new File(System.getProperty("java.io.tmpdir"),
	     "datavision_charsep_eval_out.txt");
protected static final File PARAM_INPUT_FILE =
    new File(AllTests.testDataFile("test_parameters.xml"));

protected static final String[] EVAL_RESULTS = {
    "0",
    "1\t23.0\t1",
    "1\t44.3\t2",
    "1\t50.0\t3",
    "39.1",
    "0",
    "2\t33.0\t1",
    "2\t46.0\t2",
    "39.5"
};

protected Report report;
protected CharSepSource dataSource;

public static Test suite() {
    return new TestSuite(FormulaEvalTest.class);
}

public FormulaEvalTest(String name) {
    super(name);
}

public void setUp() throws Exception {
    report = new Report();

    OUT_FILE.deleteOnExit();
    PrintWriter out = new PrintWriter(new FileWriter(OUT_FILE));
    report.setLayoutEngine(new CharSepLE(out, '\t'));

    report.read(EVAL_REPORT); // Must come after setting password

    dataSource = (CharSepSource)report.getDataSource();
    dataSource.setSepChar(',');
    dataSource.setInput(EVAL_DATA_FILE);
}

public void tearDown() {
    if (OUT_FILE.exists())
	OUT_FILE.delete();
}

public void testEvalAllVisible() throws FileNotFoundException, IOException {
    runEvalTest(null);
}

public void testEvalDetalInvisible()
    throws FileNotFoundException, IOException
{
    // Make detail formula field invisible.
    report.findField("2").setVisible(false);

    runEvalTest(new ExpectedLineModifier() {
	String expected(String str) {
	    int pos = str.lastIndexOf("\t");
	    return (pos != -1) ? str = str.substring(0, pos) : str;
	}
	});
}

public void testEvalHeaderInvisible()
    throws IOException, FileNotFoundException
{
    // Make detail formula field invisible.
    report.findField("1").setVisible(false);

    runEvalTest(new ExpectedLineModifier() {
	String expected(String str) {
	    return ("0".equals(str)) ? "" : str;
	}
	});
}

public void testGroupHeaderInvisible()
    throws FileNotFoundException, IOException
{
    // Make detail formula field invisible.
    Section s = report.findField("1").getSection();
    s.getSuppressionProc().setHidden(true);

    runEvalTest(new ExpectedLineModifier() {
	String expected(String str) {
	    return ("0".equals(str)) ? null : str;
	}
	});
}

public void testParamInSuppressionProc()
    throws FileNotFoundException, IOException
{
    // We only need one parameter. This array of parameters is only
    // necessary because the parameter file contains data for parameters
    // with ids 1 - 6.
    Parameter[] params = new Parameter[6];
    for (int i = 0; i < 6; ++i) {
	params[i] = new Parameter(new Long(i + 1), report, "string",
				  "str param", "what do YOU want?",
				  "single");
	report.addParameter(params[i]);
    }

    report.setParameterXMLInput(PARAM_INPUT_FILE);

    // Find detail section
    Section detail = report.findField("100").getSection();
    Formula f = detail.getSuppressionProc().getFormula();
    f.setExpression("\"{?1}\" == 'never'");

    runEvalTest(null);

    // Make sure parameter's value has been read in. Thus we prove that the
    // report recognized the parameter was used in the detail section's
    // suppression proc.
    Parameter p = report.findParameter(new Long(1));
    assertNotNull(p);
    assertEquals("Chicago", p.getValue());
}

void runEvalTest(ExpectedLineModifier elm)
    throws FileNotFoundException, IOException
{
    // Run report in this thread, not a separate one. Running the
    // report closes the output stream.
    report.runReport();

    // Open the output and look for various things.
    BufferedReader out = new BufferedReader(new FileReader(OUT_FILE));

    // Check output file contents
    String outLine;
    for (int lineNum = 0; lineNum < EVAL_RESULTS.length
	     && (outLine = out.readLine()) != null; ++lineNum)
    {
	String expected = EVAL_RESULTS[lineNum];
	if (elm == null)
	    expected = EVAL_RESULTS[lineNum];
	else
	    expected = elm.expected(EVAL_RESULTS[lineNum]);
	while (expected == null) {
	    ++lineNum;
	    expected = EVAL_RESULTS[lineNum];
	    expected = elm.expected(EVAL_RESULTS[lineNum]);
	}
	assertEquals(expected, outLine);
    }

    // Make sure we are at the end of the file.
    assertNull(out.readLine());

    out.close();
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

// ================================================================
/**
 * Lets us modify the &quot;standard&quot; expected results to match
 * a particular test's output.
 */
abstract class ExpectedLineModifier {
/**
 * Given the &quot;standard&quot; expected value, returns the expected
 * text for a particular test. If we return <code>null</code> that means
 * the input line should be skipped.
 */
abstract String expected(String str);
}

}
