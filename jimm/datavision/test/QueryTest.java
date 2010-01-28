package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.source.sql.SQLQuery;
import jimm.datavision.layout.CharSepLE;
import java.io.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Reads a report from an XML file, tests its structure, and tests various
 * pieces like parameter and formula substitution.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class QueryTest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("test.xml"));
protected static final File PARAMETER_XML_FILE_NAME =
    new File(AllTests.testDataFile("test_parameters.xml"));
protected static final File OUT_FILE =
    new File(System.getProperty("java.io.tmpdir"),
	     "datavision_query_test_out.txt");

protected SQLQuery query;
protected Report report;

public static Test suite() {
    return new TestSuite(QueryTest.class);
}

public QueryTest(String name) {
    super(name);
}

public void setUp() throws Exception {
    report = new Report();
    report.setDatabasePassword("");
    report.read(EXAMPLE_REPORT); // Must come after setting password
    report.setParameterXMLInput(PARAMETER_XML_FILE_NAME);
    query = (SQLQuery)report.getDataSource().getQuery();
}

protected void preparedQueryTest(String whereClause, String answer) {
    query.setEditableWhereClause(whereClause);
    String sql = query.toPreparedStatementString();
    assertTrue("expected \"" + answer + "\" in where clause; sql = " + sql,
	       sql.indexOf(answer) >= 0);
}

public void testQueryRangeSubstitution() {
    String answer = " jobs.\"ID\"  between ? and ?";
    String notAnswer = " jobs.\"ID\"  not between ? and ?";

    preparedQueryTest("{jobs.ID} in {?Number Range}", answer);
    preparedQueryTest("{jobs.ID} in{?Number Range}", answer);
    preparedQueryTest("{jobs.ID} between {?Number Range}", answer);
    preparedQueryTest("{jobs.ID} = {?Number Range}", answer);
    preparedQueryTest("{jobs.ID}={?Number Range}", answer);
    preparedQueryTest("{jobs.ID} ={?Number Range}", answer);
    preparedQueryTest("{jobs.ID}= {?Number Range}", answer);

    preparedQueryTest("{jobs.ID} not in {?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID} not in{?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID} not between {?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID} != {?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID}!={?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID} !={?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID}!= {?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID} <> {?Number Range}", notAnswer);
    preparedQueryTest("{jobs.ID}<>{?Number Range}", notAnswer);

    answer = "jobs.ID between ? and ?";
    notAnswer = "jobs.ID not between ? and ?";

    preparedQueryTest("jobs.ID in {?Number Range}", answer);
    preparedQueryTest("jobs.ID in{?Number Range}", answer);
    preparedQueryTest("jobs.ID between {?Number Range}", answer);
    preparedQueryTest("jobs.ID = {?Number Range}", answer);
    preparedQueryTest("jobs.ID={?Number Range}", answer);
    preparedQueryTest("jobs.ID ={?Number Range}", answer);
    preparedQueryTest("jobs.ID= {?Number Range}", answer);

    preparedQueryTest("jobs.ID not in {?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID not in{?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID not between {?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID != {?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID!={?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID !={?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID!= {?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID <> {?Number Range}", notAnswer);
    preparedQueryTest("jobs.ID<>{?Number Range}", notAnswer);
}

public void testQueryListSubstitution() {
    String answer = " jobs.\"ID\"  in (?,?,?)";
    String notAnswer = " jobs.\"ID\"  not in (?,?,?)";

    preparedQueryTest("{jobs.ID} in {?Pick One}", answer);
    preparedQueryTest("{jobs.ID} = {?Pick One}", answer);
    preparedQueryTest("{jobs.ID}={?Pick One}", answer);
    preparedQueryTest("{jobs.ID} not in {?Pick One}", notAnswer);
    preparedQueryTest("{jobs.ID} != {?Pick One}", notAnswer);
    preparedQueryTest("{jobs.ID} <> {?Pick One}", notAnswer);

    answer = "jobs.ID in (?,?,?)";
    notAnswer = "jobs.ID not in (?,?,?)";

    preparedQueryTest("jobs.ID in {?Pick One}", answer);
    preparedQueryTest("jobs.ID = {?Pick One}", answer);
    preparedQueryTest("jobs.ID={?Pick One}", answer);
    preparedQueryTest("jobs.ID not in {?Pick One}", notAnswer);
    preparedQueryTest("jobs.ID != {?Pick One}", notAnswer);
    preparedQueryTest("jobs.ID <> {?Pick One}", notAnswer);
}

protected void displayQueryTest(String whereClause, String answer) {
    query.setEditableWhereClause(whereClause);
    String sql = query.toString();
    assertTrue("expected \"" + answer + "\" in where clause; sql = " + sql,
	       sql.indexOf(answer) >= 0);
}

public void testQueryWhereClauseDisplay() {
    displayQueryTest("{jobs.ID} < 100", " jobs.\"ID\"  < 100");
    displayQueryTest("{jobs.ID} = {?Number Range}",
		     " jobs.\"ID\"  between {?Number Range} and {?Number Range}");
    displayQueryTest("{office.name}={?String Param}",
		     " office.name  = {?String Param}");
    displayQueryTest("{office.name}!={?String Param}",
		     " office.name  != {?String Param}");
    displayQueryTest("{office.name}<>{?String Param}",
		     " office.name  <> {?String Param}");
    displayQueryTest("{office.name}is{?String Param}",
		     " office.name  is {?String Param}");
    displayQueryTest("{office.name} is not {?String Param}",
		     " office.name  is not {?String Param}");
    displayQueryTest("{office.name}is not{?String Param}",
		     " office.name is not {?String Param}");
}

public void testQueryDateParam() {
    preparedQueryTest("jobs.post_date >= {?Date}", "jobs.post_date >= ?");
    preparedQueryTest("{jobs.post_date} >= {?Date}", " jobs.post_date  >= ?");
}

public void testQueryUserColPrep() {
    String answer = "substr( jobs.title , 1, 8)";
    UserColumn uc = new UserColumn(null, report, "my user col",
				   "substr({jobs.title}, 1, 8)");
    assertEquals(answer, uc.getSelectString(query));
}

public void testWhereClauseContainsParam() {
    Parameter p = report.findParameter("1");
    assertNotNull(p);

    // Make sure we're not imagining things.
    query.setEditableWhereClause("");
    assertTrue(!query.containsReferenceTo(p));

    // It's easy to find this one.
    query.setEditableWhereClause("{office.name} = {?String Param}");
    assertTrue(query.containsReferenceTo(p));

    // Create a formula that refers to the parameter.
    Formula f = report.findFormula("3");
    assertNotNull(f);
    f.setEditableExpression("{?String Param}");
    assertTrue(f.refersTo(p));

    // Put the formula into the where clause, then look for it. Don't let
    // the formula name decieve you (it's "contains usercol").
    query.setEditableWhereClause("{office.name} = " + f.designLabel());
    assertTrue(query.containsReferenceTo(p));
}

public void testManualParameter() throws Exception {
    report = new Report();
    report.setDatabasePassword("");
    report.read(EXAMPLE_REPORT); // Must come after setting password
    // Do not call report.setParameterXMLFile()

    Parameter p = report.findParameter("1");
    assertNotNull(p);

    query = (SQLQuery)report.getDataSource().getQuery();
    query.setEditableWhereClause("office.name = {?String Param}");

    report.parametersSetManually(true);
    p.setValue(0, "Chicago");

    OUT_FILE.deleteOnExit();
    PrintWriter out = new PrintWriter(new FileWriter(OUT_FILE));
    report.setLayoutEngine(new CharSepLE(out, '\t'));

    try {
	report.runReport();
    }
    catch (Exception e) {
	e.printStackTrace();
	fail("Exception seen: " + e);
    }
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
