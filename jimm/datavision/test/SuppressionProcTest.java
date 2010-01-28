package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.test.mock.source.MockDataSource;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Tests {@link SuppressionProc}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SuppressionProcTest extends TestCase {

protected SuppressionProc proc;
protected Report report;

public static Test suite() {
    return new TestSuite(SuppressionProcTest.class);
}

public SuppressionProcTest(String name) {
    super(name);
}

public void setUp() {
    report = new Report();
    proc = new SuppressionProc(report);
}

public void testBasicStuff() {
    assertTrue(!proc.isHidden());
    assertNotNull(proc.getFormula());
    assertTrue(!proc.suppress());
}

public void testRefersTo() {
    assertTrue(!proc.refersTo((Field)null));

    report.setDataSource(new MockDataSource(report));
    ColumnField f = (ColumnField)Field.create(null, report, null, "column",
					      "jobs.title", true);
    assertTrue(!proc.refersTo(f));

    proc.getFormula().setExpression("{jobs.title}");
    assertTrue(proc.refersTo(f));
}

public void testSuppress() {
    assertTrue(!proc.isHidden());
    assertTrue(!proc.suppress());

    proc.setHidden(true);
    assertTrue(proc.suppress());

    proc.setHidden(false);
    assertTrue(!proc.suppress());

    Formula f = proc.getFormula(); // Forces creation of formula object
    assertTrue(!proc.suppress());

    f.setExpression("true");
    assertTrue(proc.suppress());

    f.setExpression("false");
    assertTrue(!proc.suppress());

    proc.setHidden(true);
    assertTrue(proc.suppress());
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
