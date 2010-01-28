package jimm.datavision.test;
import jimm.datavision.Report;
import jimm.datavision.layout.pdf.PDFLE;
import jimm.datavision.source.charsep.CharSepSource;
import java.io.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Tests for the {@link PDFLE} PDF layout engine.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class PDFLETest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("charsep.xml"));
protected static final String DATA_FILE =
    AllTests.testDataFile("charsep_data.csv");
protected static final File OUT_FILE =
    new File(System.getProperty("java.io.tmpdir"),
	     "datavision_pdfle_test_out.txt");

protected Report report;
protected CharSepSource dataSource;

public static Test suite() {
    return new TestSuite(PDFLETest.class);
}

public PDFLETest(String name) {
    super(name);
}

public void setUp() throws Exception {
    report = new Report();

    OUT_FILE.deleteOnExit();
    report.setLayoutEngine(new PDFLE(new FileOutputStream(OUT_FILE)));

    report.read(EXAMPLE_REPORT);

    dataSource = (CharSepSource)report.getDataSource();
    dataSource.setSepChar(',');
    dataSource.setInput(DATA_FILE);
}

public void tearDown() {
    if (OUT_FILE.exists())
	OUT_FILE.delete();
}

public void testNullReportSummary() {
    report.setName(null);
    report.setTitle(null);
    report.setAuthor(null);
    report.setDescription(null);
    report.runReport();
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
