package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.layout.CharSepLE;
import jimm.datavision.source.Join;
import java.io.*;
import java.util.Iterator;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Reads a report from an XML file, runs it, and verifies the output. Uses
 * the {@link CharSepLE} layout engine to produce a tab-delimited output file.
 * <p>
 * These tests are tightly coupled with the contents of the file
 * <code>test_sub.xml</code> and the contents of the test database generated
 * by the files in <code>jimm/datavision/testdata</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SubreportRunTest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("test_sub.xml"));
protected static final File OUT_FILE =
    new File(System.getProperty("java.io.tmpdir"),
	     "datavision_subreport_run_test_out.txt");
// This must match the format string for the report.date field in
// EXAMPLE_REPORT.
protected static final String REPORT_DATE_FORMAT = "yyyy-MM-dd";
// This must be an alphabetically sorted list of office names from the
// offices table.
protected static final String OFFICES[] = {
    "Chicago", "New Jersey", "New York"
};
// The value of parameter one, which is at the beginning of the
// report header.
protected static final String STRING_PARAM_VALUE = "Chicago";
// The value of the report.title special field, which appears
// in the report header.
protected static final String REPORT_TITLE = "Example Report";

protected Report report;

public static Test suite() {
    return new TestSuite(SubreportRunTest.class);
}

public SubreportRunTest(String name) {
    super(name);
}

public void setUp() throws Exception {
    report = new Report();
    report.setDatabasePassword("");
    report.read(EXAMPLE_REPORT); // Must come after setting password

    if (OUT_FILE.exists())
	OUT_FILE.delete();	// Delete previous output
    OUT_FILE.deleteOnExit();
    PrintWriter out = new PrintWriter(new FileWriter(OUT_FILE));
    report.setLayoutEngine(new CharSepLE(out, '\t'));
}

public void tearDown() {
    if (OUT_FILE.exists())
	OUT_FILE.delete();
}

// Make sure a simple subreport runs
public void testReportRun() throws IOException, FileNotFoundException {
    // Run report in this thread, not a separate one. Running the
    // report closes the output stream.
    report.runReport();

    BufferedReader in = new BufferedReader(new FileReader(OUT_FILE));
    String line;
    int cityIndex = -1;
    String expectedOffice;
    while ((line = in.readLine()) != null) {
	int tabPos = line.indexOf("\t");

	if (tabPos == -1)	// New group
	    ++cityIndex;
	expectedOffice = ReportRunTest.OFFICES[cityIndex];

	if (tabPos == -1)
	    assertEquals(expectedOffice, line);
	else {
	    // Line is "NN<tab>name<tab>name"
	    line = line.substring(tabPos + 1);
	    tabPos = line.indexOf("\t");
	    assertEquals(expectedOffice, line.substring(tabPos + 1));
	}
    }
    in.close();
}

public void testMultipleJoins() throws IOException, FileNotFoundException {
    for (Iterator iter = report.subreports(); iter.hasNext(); ) {
	Subreport s = (Subreport)iter.next();
	s.addJoin(new Join(report.findColumn("office.name"), "=",
			   report.findColumn("office.name")));
    }

    // Run report in this thread, not a separate one. Running the
    // report closes the output stream.
    report.runReport();

    BufferedReader in = new BufferedReader(new FileReader(OUT_FILE));
    String line;
    int cityIndex = -1;
    String expectedOffice;
    while ((line = in.readLine()) != null) {
	int tabPos = line.indexOf("\t");

	if (tabPos == -1)	// New group
	    ++cityIndex;
	expectedOffice = ReportRunTest.OFFICES[cityIndex];

	if (tabPos == -1)
	    assertEquals(expectedOffice, line);
	else {
	    // Line is "NN<tab>name<tab>name"
	    line = line.substring(tabPos + 1);
	    tabPos = line.indexOf("\t");
	    assertEquals(expectedOffice, line.substring(tabPos + 1));
	}
    }
    in.close();
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
