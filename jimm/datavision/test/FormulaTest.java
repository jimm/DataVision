package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.source.Column;
import jimm.datavision.source.Query;
import jimm.datavision.test.mock.source.MockDataSource;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class FormulaTest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("test.xml"));

protected Report report;

public static Test suite() {
    return new TestSuite(FormulaTest.class);
}

public FormulaTest(String name) {
    super(name);
}

public void setUp() {
    report = new Report();
    report.setDataSource(new MockDataSource(report));
}

public void testSingleTokens() throws IOException {
    HashMap testVals = new HashMap();
    Long theAnswer = new Long(42);
    testVals.put("42", theAnswer);
    testVals.put(" 42", theAnswer);
    testVals.put("42 ", theAnswer);
    testVals.put(" 42 ", theAnswer);
    testVals.put(" \t 42 \t", theAnswer);
    testVals.put("66.6", new Double(66.6));
    testVals.put("\"foo\"", "foo");
    testVals.put("\"don't forget apostrophes\"",
		 "don't forget apostrophes");
    testVals.put("\"or \\\"quoted quotes\\\" either\"",
		 "or \"quoted quotes\" either");

    runTests(testVals);
}

public void testExpressions() throws IOException {
    HashMap testVals = new HashMap();
    Long theAnswer = new Long(42);
    Double theAnswerAsDouble = new Double(42);
    String theAnswerAsString = "42";
    Long aBoringAnswer = new Long(36);

    testVals.put("39 + 3", theAnswer);
    testVals.put(" 39 + 3 ", theAnswer);
    testVals.put(" 39\t+ 3", theAnswer);
    testVals.put(" 39    +\t3", theAnswer);
    testVals.put("39 + 3", theAnswer);
    testVals.put("45 - 3", theAnswer);
    testVals.put("21 * 2", theAnswer);
    testVals.put("84.00 / 2.0", theAnswerAsDouble);
    testVals.put(" \"foo\" + \"bar\"", "foobar");
    testVals.put("\"foo\" * 3", "foofoofoo");
    testVals.put("\"foo\" * 3.5", "foofoofoo");
    testVals.put("\"foo\" * 3.5.to_i", "foofoofoo");
    testVals.put("42.3343.floor", theAnswer);
    testVals.put("42.3343.round", theAnswer);
    testVals.put("41.3343.ceil", theAnswer);
    testVals.put("6 ** 2", aBoringAnswer);
    testVals.put("[42, 36].max", theAnswer);
    testVals.put("[36, 42].max", theAnswer);
    testVals.put("[42, 36].min", aBoringAnswer);
    testVals.put("[36, 42].min", aBoringAnswer);
    testVals.put("42.abs", theAnswer);
    testVals.put("-42.abs", theAnswer);
    testVals.put("str='45';str.gsub!(/5/,'2');str.to_i", theAnswer);
    testVals.put("Math.sqrt(1764)", theAnswerAsDouble);
    testVals.put("Math.sqrt(Math.sqrt(1764) ** 2)", theAnswerAsDouble);

    testVals.put("'42abc'[0, 2]", theAnswerAsString);

    runTests(testVals);
}

public void testDisplay() throws Exception {
    report.read(EXAMPLE_REPORT);

    Formula f = report.findFormula(new Long(1));
    assertNotNull(f);

    assertEquals("{jobs.hourly rate}.nil? ? nil : {jobs.hourly rate} / 100.0",
		 f.getEditableExpression());

    f.setExpression("{?1}");
    assertEquals("{?1}", f.getExpression());
    assertEquals("{?String Param}", f.getEditableExpression());

    f.setEditableExpression("{?String Param}");
    assertEquals("{?1}", f.getExpression());
    assertEquals("{?String Param}", f.getEditableExpression());

    f.setEditableExpression("{!Short Title}");
    assertEquals("{!1}", f.getExpression());
    assertEquals("{!Short Title}", f.getEditableExpression());
}

public void runTests(HashMap testVals) throws IOException {
    Formula f = new Formula(null, report, "Unnamed Formula");
    for (Iterator iter = testVals.keySet().iterator(); iter.hasNext(); ) {
	String evalStr = (String)iter.next();
	Object answer = testVals.get(evalStr);

	f.setExpression(evalStr);
	Object calculated = f.eval();
	assertEquals(answer, calculated);
	assertEquals(answer.getClass(), calculated.getClass());
    }
}

public void testContainsColumns() throws Exception {
    report.read(EXAMPLE_REPORT);

    UserColumn uc = report.findUserColumn(new Long(1));
    assertNotNull(uc);

    // Pick any formula that has a formula field in the report. Change
    // the formula's expression to be the user column.
    Formula f = report.findFormula(new Long(2));
    assertNotNull(f);
    f.setExpression(uc.formulaString());

    // If a formula contains a user column and the user column isn't used
    // in any report field (but the formula is), prove that the query will
    // contain the user column.
    Query q = (Query)report.getDataSource().getQuery();
    q.findSelectablesUsed();
    assertTrue("user col contained in formula isn't in query",
	       q.indexOfSelectable(uc) >= 0);

    // Now make the formula's expression a Ruby expression that "hides" a
    // column that isn't otherwise in the report. By "hide" I mean that a
    // simple search for "{" won't work; the formula will have to use its
    // "exceptAfter" ivar to ignore the "#{" Ruby expession start.
    f.setExpression("#{{aggregate_test.col1}}");
    Column col = report.findColumn("aggregate_test.col1");
    assertNotNull(col);
    q.findSelectablesUsed();
    assertTrue("col contained in Ruby string expression isn't in query",
	       q.indexOfSelectable(col) >= 0);
}

public void testIgnoreNonColumns() {
    Formula f = new Formula(new Long(0), report, "ignore non-columns");
    try {
	f.setEditableExpression("x = 42");
	f.eval(null);

	f.setEditableExpression("x = {}; x[:z] = 42");
	f.eval(null);

	f.setEditableExpression("x = {  }; x[:z] = 42");
	f.eval(null);
    }
    catch (Exception e) {
	fail(e.toString());
    }
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
