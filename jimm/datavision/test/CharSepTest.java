package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.layout.CharSepLE;
import jimm.datavision.source.Column;
import jimm.datavision.source.charsep.CharSepSource;
import java.io.*;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Reads a report from an XML file, runs it, and verifies the output. Uses
 * a {@link CharSepSource} data source and the {@link CharSepLE} layout engine
 * to produce a tab-delimited output file.
 * <p>
 * These tests are tightly coupled with the contents of the files
 * <code>charsep.xml</code> and <code>charsep_data.csv</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class CharSepTest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("charsep.xml"));
protected static final String DATA_FILE =
    AllTests.testDataFile("charsep_data.csv");
protected static final String EMPTY_DATA_FILE =
    AllTests.testDataFile("empty.csv");
protected static final String DATA_FILE_WITH_SHORT_LINES =
    AllTests.testDataFile("short_lines.csv");
protected static final File OUT_FILE =
    new File(System.getProperty("java.io.tmpdir"),
	     "datavision_charsep_test_out.txt");
// This must match the format string for the report.date field in
// EXAMPLE_REPORT.
protected static final String REPORT_DATE_FORMAT = "yyyy-MM-dd";
// This must be an alphabetically sorted list of office names from the
// offices table, EXCEPT for Chicago, which is excluded by the where
// clause.
protected static final String OFFICES[] = {
    "New Jersey", "New York"
};
// The value of the report.title special field, which appears
// in the report header.
protected static final String REPORT_TITLE = "Example Report";

protected Report report;
protected CharSepSource dataSource;
protected DecimalFormat dollarFormatter;
protected DecimalFormat lastColFormatter;
protected SimpleDateFormat titleDateFormatter;
protected int reportRowNumber;
protected int officeRowNumber;
protected int postDateRowNumber;

public static Test suite() {
    return new TestSuite(CharSepTest.class);
}

public CharSepTest(String name) {
    super(name);
}

public void setUp() throws Exception {
    dollarFormatter = new DecimalFormat("$#,###.00");
    lastColFormatter = new DecimalFormat("#,###.##");
    titleDateFormatter = new SimpleDateFormat(REPORT_DATE_FORMAT);

    report = new Report();
    reportRowNumber = officeRowNumber = postDateRowNumber = 1;

    OUT_FILE.deleteOnExit();
    PrintWriter out = new PrintWriter(new FileWriter(OUT_FILE));
    report.setLayoutEngine(new CharSepLE(out, '\t'));

    report.read(EXAMPLE_REPORT); // Must come after setting password

    dataSource = (CharSepSource)report.getDataSource();
    dataSource.setSepChar(',');
    dataSource.setInput(DATA_FILE);
}

public void tearDown() {
    if (OUT_FILE.exists())
	OUT_FILE.delete();
}

public void testColumnInfo() {
    String[] colNames = { "office.name", "jobs.ID", "jobs.title",
			  "jobs.hourly rate", "jobs.post_date" };
    for (int i = 0; i < colNames.length; ++i) {
	Column col;
	assertNotNull(col = dataSource.findColumn(colNames[i]));
	assertEquals(i, dataSource.indexOfSelectable(col));
    }
}

public void testReportRun() throws IOException, FileNotFoundException {
    assertEquals("{office.name} != 'Chicago'",
		 report.getDataSource().getQuery().getWhereClause());

    // Run report in this thread, not a separate one. Running the
    // report closes the output stream.
    report.runReport();

    // Open the output and look for various things.
    BufferedReader in = new BufferedReader(new FileReader(OUT_FILE));

    expectHeaders(in);

    // Each of the office groups. checkOfficeGroup() returns the total
    // dollar amount (really the total id number amount, but that is proved
    // to be the same thing within checkDetailLine()).
    int total = 0;
    for (int i = 0; i < OFFICES.length; ++i)
	total += checkOfficeGroup(in, OFFICES[i]);

    // The grand total.
    String line = in.readLine();
    assertNotNull(line);
    if (line.startsWith("Page "))
	assertNotNull(line = in.readLine());

    assertEquals("Grand Total:\t" + dollarFormatter.format(total)
		 + "\t" + (reportRowNumber - 1)
		 + "\t" + (reportRowNumber - 1),
		 line);

    // The final page number.
    assertNotNull(line = in.readLine());
    assertEquals("Page ", line.substring(0, 5));

    // Make sure we are at the end of the file.
    assertNull(in.readLine());

    in.close();
}

protected void expectHeaders(BufferedReader in) throws IOException {
    String line;

    // Line 1: report title and formatted date.
    // since that will most definitely be different.
    assertNotNull(line = in.readLine());
    assertEquals(REPORT_TITLE + '\t'
		 + "file:examples/Home16.gif"
		 + '\t' + titleDateFormatter.format(new Date()),
		 line);

    // Line 2: the page header.
    assertNotNull(line = in.readLine());
    assertEquals(0, line.indexOf("Job #\tTitle\tHourly Rate"));
}

/**
 * Checks a group (header plus detail lines) and returns the total
 * of the dollar amounts in the group.
 *
 * @param in the input reader
 * @param officeName the group name
 * @return the total dollar amount in the group
 */
protected int checkOfficeGroup(BufferedReader in, String officeName)
    throws IOException
{
    String line;
    int aggregate = 0;

    officeRowNumber = 1;

    // The office name. Skip page delimiters ("Page" at foot and
    // page headers at top).
    assertNotNull(line = in.readLine());
    while (line.startsWith("Page ") || line.startsWith("Job #\t"))
	assertNotNull(line = in.readLine());
    assertEquals(officeName, line);

    Object postDateGroupEnd;
    while ((postDateGroupEnd = checkPostDateGroup(in)) instanceof Integer)
	aggregate += ((Integer)postDateGroupEnd).intValue();
    line = (String)postDateGroupEnd;

    assertEquals("Total:\t" + dollarFormatter.format(aggregate)
		 + "\t" + (reportRowNumber - 1)
		 + "\t" + (officeRowNumber - 1),
		 line);

    return aggregate;
}

/**
 * Checks a subgroup (header plus detail lines) and returns the total
 * of the dollar amounts in the group. If the next report section is
 * not a subgroup, it will be a group total line; we return the line
 *
 * @param in the input reader
 * @return either an Integer containing the total or the next line read
 * from the report
 */
protected Object checkPostDateGroup(BufferedReader in) throws IOException {
    int aggregate = 0;
    String line;

    postDateRowNumber = 1;

    // Read either post date or next (super)group name. Return false if we
    // see a non-date group name; that means we're done. Skip page
    // delimiters ("Page" at foot and page headers at top).
    while (true) {
	assertNotNull(line = in.readLine());
	if (line.startsWith("Page ") || line.startsWith("Job #\t"))
	    continue;
	if (line.startsWith("Total:"))
	    return line;

	// Primitive date check
	assertTrue(Character.isDigit(line.charAt(0)) && line.length() == 10
		   && line.charAt(4) == '-' && line.charAt(7) == '-');
	break;
    }

    // The detail lines. Collect the id number from the beginning of each
    // detail line. It is the same as the dollar amount value. Add that
    // amount to the aggregate.
    while (true) {
	assertNotNull(line = in.readLine());
	if (line.startsWith("Post Date Total:"))
	    break;
	if (line.startsWith("Page ") || line.startsWith("Job #\t"))
	    continue;

	aggregate += checkDetailLine(line);
    }

    assertEquals("Post Date Total:\t" + dollarFormatter.format(aggregate)
		 + "\t" + (reportRowNumber - 1)
		 + "\t" + (postDateRowNumber - 1),
		 line);

    return new Integer(aggregate);
}

/**
 * Checks the format of a detail line and returns the integer id found
 * at the beginning of line. This is the same as the dollar amount.
 *
 * @param line a detail line
 * @return the id number
 */
protected int checkDetailLine(String line) {
    // Read job id from the beginning of the line.
    int id = Integer.parseInt(line.substring(0, line.indexOf("\t")));

    // The description, at random, may be repeated ("job 3 job 3"). We
    // check for the first full description.
    String str = "" + id + "\tThis is the short description of job " + id;
    assertTrue("line does not start with \"" + str + "\"",
	       line.startsWith(str));

    // Check for the remaining columns at the end of the line. We are also
    // assuring that the dollar amount is the same as the id number.
    if (id == 0) {
	assertEquals("0\tThis is the short description of job 0\t\t\t\t"
		     + reportRowNumber + "\t" + postDateRowNumber,
		     line);
    }
    else {
	str = "\t" + (id * 100)
	    + "\t" + dollarFormatter.format(id)
	    + "\t" + lastColFormatter.format(id)
	    + "\t" + reportRowNumber
	    + "\t" + postDateRowNumber;
	assertTrue("line \"" + line + "\" does not end with \"" + str,
		   line.endsWith(str));
    }

    ++reportRowNumber;
    ++officeRowNumber;
    ++postDateRowNumber;

    return id;
}

// We used to throw an exception if there were no records returned
// by the query but there was a column field in the page header or
// report header.
public void testNoRecords() {
    try {
	// Add a column field to the report header
	Section pageHeader =
	    report.getFirstSectionByArea(SectionArea.PAGE_HEADER);
	assertNotNull(pageHeader);
	ColumnField f = new ColumnField(null, report, pageHeader,
					"office.name", true);
	pageHeader.addField(f);

	// Create a query that returns 0 records
	report.getDataSource().getQuery().setWhereClause("1 == 2");

	// Run the report. We shouldn't throw an exception.
	report.runReport();
    }
    catch (Exception e) {
	fail("should not throw an exception just 'cause there are no records");
    }
}

// public void testWhereClause() {
// }

public void testEmptyFile() {
    try {
	dataSource.setInput(EMPTY_DATA_FILE);
	report.runReport();
    }
    catch (Exception e) {
	fail("should not throw an exception just 'cause the file is empty");
    }
}

public void testShortInputLines() {
    try {
	dataSource.setInput(DATA_FILE_WITH_SHORT_LINES);
	report.runReport();
    }
    catch (Exception e) {
	fail("should not throw an exception just 'cause some lines are short");
    }
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
