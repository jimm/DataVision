package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.source.Column;
import jimm.datavision.test.mock.source.MockDataSource;
import java.io.File;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Some tests for BSF {@link Scripting}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ScriptingTest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("test.xml"));
protected static final String VALUE_REPORT =
    AllTests.testDataFile("value.xml");

protected Report report;

public static Test suite() {
    return new TestSuite(ScriptingTest.class);
}

public ScriptingTest(String name) {
    super(name);
}

public void setUp() {
    report = new Report();
    report.setDataSource(new MockDataSource(report));
}

public void testReportObject() {
    report.setTitle("foo");
    Formula f = new Formula(new Long(0), report, "test report access");
    f.setEditableExpression("$report.getTitle()");
    assertEquals("foo", f.eval(null));

    f.setEditableExpression("$report.title");
    assertEquals("foo", f.eval(null));
}

public void testColumnAccess() throws Exception {
    report.read(EXAMPLE_REPORT);

    Formula f = report.findFormula("1");
    f.setEditableExpression("$report.findColumn('jobs.hourly rate')");

    Object result = f.eval(null);
    assertNotNull(result);
    assertEquals("jimm.datavision.source.Column", result.getClass().getName());
    assertTrue(result instanceof Column);
    assertEquals("jobs.hourly rate", ((Column)result).getId().toString());
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
