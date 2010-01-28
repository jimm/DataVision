package jimm.datavision.test;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.test.mock.source.MockDataSource;
import java.util.List;
import java.io.File;
import java.text.DecimalFormat;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Reads a report from an XML file, tests its structure, and tests various
 * pieces like parameter and formula substitution.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ReportTest extends TestCase {

protected static final File EXAMPLE_REPORT =
        new File(AllTests.testDataFile("test.xml"));
protected static final File PARAM_INPUT_FILE =
        new File(AllTests.testDataFile("test_parameters.xml"));

protected Report report;
protected DecimalFormat dollarFormatter;
protected DecimalFormat lastColFormatter;

public static Test suite() {
    return new TestSuite(ReportTest.class);
}

public ReportTest(String name) {
    super(name);
}

public void setUp() throws Exception {
    report = new Report();
    report.setDataSource(new MockDataSource(report));
    report.read(EXAMPLE_REPORT);
    report.setParameterXMLInput(PARAM_INPUT_FILE);
}

public void tearDown() {
    report = null;
}

public void testAttributes() {
    assertEquals("example1", report.getName());
    assertEquals("Example Report", report.getTitle());
    assertEquals("Jim Menard", report.getAuthor());
    assertEquals(new Long(7), report.generateNewParameterId());
}

public void testPaperFormat() {
    PaperFormat pf = report.getPaperFormat();
    assertNotNull(pf);
    assertEquals(PaperFormat.PORTRAIT, pf.getOrientation());
    assertEquals("US-Letter", pf.getName());
}

public void testImages() {
    Section detail = report.getFirstSectionByArea(SectionArea.DETAIL);

    // Add an image, passing in a file path, and make sure the URL is
    // correct.
    ImageField image = new ImageField(new Long(99), report, detail,
				      "/tmp/foo.gif", true);
    // I expected to see "file:///tmp/foo.gif", but apparently the URL
    // class modifies that to be "file:/tmp/foo.gif".
    assertEquals("file:/tmp/foo.gif", image.getImageURL().toString());

    // Now set value to a URL string.
    String url = "http://localhost/foo.gif";
    image.setValue(url);
    assertEquals(url, image.getImageURL().toString());
}

public void testSections() {
    // Sections
    assertEquals(1, report.headers().size());
    assertEquals(1, report.footers().size());
    assertEquals(1, report.pageHeaders().size());
    assertEquals(1, report.pageFooters().size());
    assertEquals(1, report.details().size());
    assertEquals(2, report.countGroups());

    // Add a new detail section
    Section detail = report.getFirstSectionByArea(SectionArea.DETAIL);
    assertNotNull(detail);
    report.insertSectionBelow(detail);
    assertEquals(2, report.details().size());
}

public void testFormulas() {
    Formula formula = report.findFormula("1");
    Long id = new Long(1);
    assertNotNull(formula);
    assertEquals("hourly rate / 100", formula.getName());
    assertEquals("{jobs.hourly rate}.nil? ? nil : {jobs.hourly rate} / 100.0",
		 formula.getExpression());
    assertEquals("formula:1", formula.dragString());
    assertEquals("{@hourly rate / 100}", formula.designLabel());
    assertEquals("{@1}", formula.formulaString());
    // We test StringUtils.formulaToDisplay() and
    // StringUtils.displayToFormula() in FormulaTest.java.

    formula = report.findFormula(id);
    assertNotNull(formula);
    assertEquals(id, formula.getId());
}

public void testFormulaReferences() {
    refTest("1", true);
    refTest("2", true);
    refTest("3", false);	// Formula exists, but not in any report field

    // Now put formula 3 in the report's startup script and look for it.
    // (We already know that report.findFormula won't return null.)
    Formula f = new Formula(null, report, "startup script",
			    report.findFormula("3").formulaString());
    report.setStartFormula(f);
    refTest("3", true);
}

protected void refTest(String formulaId, boolean shouldFindReference) {
    Formula f = report.findFormula(formulaId);
    assertNotNull(f);
    assertTrue("reference to formula " + formulaId
	       + ": wanted to find = " + shouldFindReference
	       + " but opposite happened",
	       report.containsReferenceTo(f) == shouldFindReference);
}

public void testUserColumnReferences() {
    UserColumn uc = report.findUserColumn("1");
    assertNotNull(uc);

    // We should not find the user column inside any report field
    assertTrue("shouldn't find reference to user column 1",
	       !report.containsReferenceTo(uc));

    // Add the user column to a formula that has a report field, so that
    // containsRefereceTo should return true.
    Formula f = report.findFormula("1");
    f.setExpression(uc.formulaString());
    assertTrue("can't find reference to user column 1",
	       report.containsReferenceTo(uc));

    // Now remove the user column from that formula and instead place it
    // inside a suppression proc.
    f.setExpression("");
    Section detail = report.findField("10").getSection();
    f = detail.getSuppressionProc().getFormula();
    f.setExpression(uc.formulaString());
    assertTrue("can't find reference to user column 1 in suppression proc",
	       report.containsReferenceTo(uc));

    // Now remove the user column from the suppression proc and put it in
    // the report's startup script instead.
    f.setExpression("");
    f = new Formula(null, report, "startup script", uc.formulaString());
    report.setStartFormula(f);
    assertTrue("can't find reference to user column 1 in startup script",
	       report.containsReferenceTo(uc));
}

public void testParameterReferences() {
    Parameter p = report.findParameter("1");
    assertNotNull(p);

    // Unlike the other testXXXReferences methods, we *should* find the
    // parameter inside a report field because it's there in the group
    // header.
    assertTrue("should find reference to parameter 1",
	       report.containsReferenceTo(p));

    // Now remove the parameter from the header and instead place it
    // inside a suppression proc.
    report.removeField(report.findField("15"));
    Section detail = report.findField("10").getSection();
    Formula f = detail.getSuppressionProc().getFormula();
    f.setExpression(p.formulaString());
    assertTrue("can't find reference to parameter 1 in suppression proc",
	       report.containsReferenceTo(p));

    // Now remove the parameter from the suppression proc and put it in the
    // report's startup script instead.
    f.setExpression("");
    f = new Formula(null, report, "startup script", p.formulaString());
    report.setStartFormula(f);
    assertTrue("can't find reference to parameter 1 in startup script",
	       report.containsReferenceTo(p));
}

public void testFieldReferences() {
    Field f;
    for (int i = 0; (f = report.findField("" + i)) != null; ++i)
	assertTrue("reference to field " + i + " not found",
		   report.containsReferenceTo(f));
}

public void testParameters() {
    Parameter param = report.findParameter("5");
    Long id = new Long(5);
    assertNotNull(param);
    assertEquals("parameter:5", param.dragString());
    assertEquals("{?5}", param.formulaString());
    assertEquals("{?Yes/No}", param.designLabel());
    assertEquals(Parameter.TYPE_BOOLEAN, param.getType());
    assertEquals(Parameter.ARITY_ONE, param.getArity());
    assertEquals(id, param.getId());
    assertEquals("Yes/No", param.getName());
    assertEquals("Do you breathe regularly?", param.getQuestion());

    param = report.findParameter(id);
    assertNotNull(param);
    assertEquals(id, param.getId());

    // Get ready to read values from parameter values file when asked.
    // We have to add a parameter to the report somewhere. If we don't,
    // the report won't read the XML file.
    report.getDataSource().getQuery()
	.setEditableWhereClause("office.name = {?String Param}");

    id = new Long(2);
    param = report.findParameter(id);
    assertNotNull(param);

    Object obj = report.getParameterValue(id); // Reads values from XML
    assertNotNull("range param value should not be null", obj);

    assertTrue("range param value is not a list; class = "
	       + obj.getClass().getName(),
	       obj instanceof List);
    List list = (List)obj;

    assertTrue("range param value list does not have length 2; it has length "
	       + list.size(), list.size() == 2);

    assertEquals(new Integer(50), list.get(0));
    assertEquals(new Integer(75), list.get(1));
}

public void testNullFieldIds() {
    // Empty reports start with one detail section
    Section detail = report.getFirstSectionByArea(SectionArea.DETAIL);
    assertNotNull(detail);

    // Add fields.
    TextField f = new TextField(null, report, detail, "field 1", true);
    assertNotNull(f.getId());
    Object id = f.getId();
    assertTrue("field id is not a Long", id instanceof Long);
    long longVal = ((Long)id).longValue();
    assertTrue("field id is not > 1", longVal >= 1);
    detail.addField(f);

    f = new TextField(null, report, detail, "field 2", true);
    Object newId = f.getId();
    assertNotNull(newId);
    long newLongVal = ((Long)newId).longValue();
    assertEquals(longVal + 1, newLongVal);
    detail.addField(f);

    id = newId;
    longVal = newLongVal;

    f = new TextField(null, report, detail, "field 3", true);
    newId = f.getId();
    assertNotNull(newId);
    newLongVal = ((Long)newId).longValue();
    assertEquals(longVal + 1, newLongVal);
    detail.addField(f);
}

public void testCaseSensitivity() {
    assertNotNull(report.findColumn("jobs.ID"));

    // These should fail since by default we search case-sensitively
    assertNull(report.findColumn("jobs.id"));
    assertNull(report.findColumn("JOBS.ID"));
    assertNull(report.findColumn("jOBs.id"));

    // These should succeed, because the table and column names are
    // all upper-case in the database. We go straight to the data
    // source because the report does not contain this column but
    // the database does.
    assertNotNull(report.getDataSource().findColumn("ALL_CAPS.COL1"));

    // Now tell the report to ignore case with table and column names
    report.setCaseSensitiveDatabaseNames(false);
    assertNotNull(report.findColumn("jobs.id"));
    assertNotNull(report.findColumn("JOBS.ID"));
    assertNotNull(report.findColumn("jOBs.id"));

    // Formula name search is always case-insensitive
    assertNotNull(report.findFormulaByName("refs f1"));
    assertNotNull(report.findFormulaByName("REFS F1"));
    assertNotNull(report.findFormulaByName("rEFs f1"));

    // Parameter name search is always case-insensitive
    assertNotNull(report.findParameterByName("Yes/No"));
    assertNotNull(report.findParameterByName("yes/no"));
    assertNotNull(report.findParameterByName("yES/No"));
}

public void testFormat() {
    Format f = report.getDefaultField().getFormat();
    Format f2 = (Format)f.clone();
    assertEquals("Default and cloned formats should be equal", f, f2);

    assertNotNull(f.getColor());

    Field field = Field.createFromDragString(report, "column:jobs.ID");
    assertNotNull(field);

    Format fmt = field.getFormat();
    assertNotNull(fmt);

    Format defaultFormat = report.getDefaultField().getFormat();

    assertEquals(defaultFormat, field.getFormat());

    // Test "transparency"; the format's format string is really null, but
    // it will return the default format's format string;
    assertEquals(defaultFormat.getFormat(), fmt.getFormat());
}

public void testCloning() {
    // Make sure whatever we create here does not have a value that
    // depends upon running the report and having data.
    Section s = report.getFirstSectionByArea(SectionArea.REPORT_HEADER);
    SpecialField f =
	(SpecialField)Field.create(null, report, s, "special",
				   "report.date", true);

    f.setFormat((Format)report.getDefaultField().getFormat().clone());
    f.setBounds(new Rectangle(1.0, 2.0, 3.0, 4.0));
    f.setBorder(new Border(f));

    SpecialField f2 = (SpecialField)f.clone();
    assertNotNull(f2);

    // assertEquals does too much, and we don't need Field.equals
    // anywhere else, so let's do it piece by piece

    assertEquals(f.getBounds().x, f2.getBounds().x, 0.00001);
    assertEquals(f.getBounds().y, f2.getBounds().y, 0.00001);
    assertEquals(f.getBounds().width, f2.getBounds().width, 0.00001);
    assertEquals(f.getBounds().height, f2.getBounds().height, 0.00001);
    assertEquals(f.getFormat(), f2.getFormat());
    assertEquals(f.getBorder(), f2.getBorder());

    // The value is a date. Ignore the seconds in both values
    // just in case they are different.
    String fVal = f.getValue().toString();
    String f2Val = f2.getValue().toString();
    assertEquals(fVal.substring(0, 16), f2Val.substring(0, 16));
    assertEquals(fVal.substring(19), f2Val.substring(19));
}

public void testHasParameterFields() {
    assertEquals(true, report.hasParameterFields());
}

public void testIdGeneration() {
    long nextFormulaId = report.generateNewFormulaId().longValue();
    long nextParameterId = report.generateNewParameterId().longValue();
    long nextUserColumnId = report.generateNewUserColumnId().longValue();

    // The new objects' id numbers will be the same as nextXXXId (instead
    // of nextXXXid + 1) because the new id numbers only get bumped up when
    // a new object is added to the report, not wehn generateNewXXXId gets
    // called or the object gets created.

    for (int i = 0; i < 3; ++i) {
	Formula f = new Formula(null, report, "");
	Object id = f.getId();
	assertTrue("formula id is not a Long", id instanceof Long);
	long longVal = ((Long)id).longValue();
	assertEquals(nextFormulaId, longVal);
	report.addFormula(f);
	++nextFormulaId;
		 
	Parameter p = new Parameter(null, report);
	id = p.getId();
	assertTrue("parameter id is not a Long", id instanceof Long);
	longVal = ((Long)id).longValue();
	assertEquals(nextParameterId, longVal);
	report.addParameter(p);
	++nextParameterId;
		 
	UserColumn uc = new UserColumn(null, report, "", "");
	id = uc.getId();
	assertTrue("user column id is not a Long", id instanceof Long);
	longVal = ((Long)id).longValue();
	assertEquals(nextUserColumnId, longVal);
	report.addUserColumn(uc);
	++nextUserColumnId;
    }
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
