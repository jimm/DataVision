package jimm.datavision;
import jimm.datavision.field.*;
import jimm.datavision.source.*;
import jimm.datavision.source.sql.*;
import jimm.datavision.source.charsep.CharSepSource;
import jimm.datavision.source.ncsql.NCDatabase;
import jimm.util.I18N;
import java.io.*;
import java.util.*;
import java.awt.Color;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A report reader reads an XML file and creates the innards of a report.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ReportReader extends DefaultHandler {

// ================================================================
/**
 * This class is used when we are converting formulas from the old
 * pre-DTD_VERSION_FORMULA_IDS format to the new format.
 * <p>
 * We store a copy of the original eval string because if we were to
 * retrieve it from the formula by calling
 * <code>Formula.getEvalString</code>, the formula would attempt to start
 * observing other formulas within the eval string. This would not work
 * because those formulas are represented the old way (by name instead of
 * id) and thus the code that looks for those formulas in order to observe
 * them fails. We don't have to clone the eval string, just save a
 * reference to it.
 */
static class FormulaConversion {
Formula formula;
String expression;
FormulaConversion(Formula f, String expr) {
    formula = f;
    expression = expr;
}
}
// ================================================================

/**
 * If there is no report element dtd-version attribute, this is the default
 * value to use. That's because DataVision XML files before version 0.2
 * didn't include version numbers.
 */
protected static final double DEFAULT_DTD_VERSION = 0.2;

/**
 * This is the DTD version where formula ids were introduced. Versions
 * before this one require runtime conversion.
 */
protected static final double DTD_VERSION_FORMULA_IDS = 0.2;

protected Stack tagNameStack;
protected Report report;
protected Subreport subreport;
protected Parameter parameter;
protected Formula formula;
protected UserColumn usercol;
protected Section section;
protected Group group;
protected Field field;
protected String textData;
protected Border border;
protected Line line;
protected double dtdVersion;
protected HashMap formulasToConvert;
protected int nextSectionLocation;
protected boolean missingColumnSeen;
protected boolean inSubreportJoins;

/**
 * Constructor.
 *
 * @param report the report we are building
 */
public ReportReader(Report report) {
    this.report = report;
    tagNameStack = new Stack();
    dtdVersion = DEFAULT_DTD_VERSION;
}

/**
 * Uses the InputSource to find the XML, reads it, and builds the innards
 * of the report. To specify a URL, use <code>new
 * InputSource("http://...")</code>.
 *
 * @param in the input source
 */
public void read(InputSource in) throws Exception {
    removeReportSections();
    SAXParserFactory.newInstance().newSAXParser().parse(in, this);
    postParse();
}

/**
 * Reads an XML file and builds the innards of the report.
 *
 * @param f the XML file
 */
public void read(File f) throws Exception {
    removeReportSections();
    SAXParserFactory.newInstance().newSAXParser().parse(f, this);
    postParse();
}

/**
 * Removes the report sections that are created when a report is created.
 */
protected void removeReportSections() {
    getReport().headers().clear();
    getReport().pageHeaders().clear();
    getReport().pageFooters().clear();
    getReport().footers().clear();
    getReport().details().clear();
}

/**
 * Performed after a parse, we convert old-style formulas if necessary and
 * ensure that certain report sections are non-empty.
 */
protected void postParse() throws SAXException {
    convertFormulas();

    // Headers, footers, and details must have at least one section.
    ensureNotEmpty(report.headers());
    ensureNotEmpty(report.pageHeaders());
    ensureNotEmpty(report.details());
    ensureNotEmpty(report.footers());
    ensureNotEmpty(report.pageFooters());

    for (Iterator iter = report.subreports(); iter.hasNext(); )
      ensureNotEmpty(((Subreport)iter.next()).details());
}

/**
 * Ensures that the specified collection of sections is not empty. If we
 * do create a section, it is marked as suppressed.
 *
 * @param area collection of sections
 * sections
 */
protected void ensureNotEmpty(SectionArea area) {
    if (area.isEmpty()) {
	Section s = new Section(report);
	s.getSuppressionProc().setHidden(true);
	area.add(s);
    }
}

protected Report getReport() {
    return subreport != null ? subreport : report;
}

public void startElement(final String namespaceURI, final String localName,
			 final String qName, final Attributes attributes)
    throws SAXException
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    String parentTag = tagNameStack.empty() ? null
	: (String)tagNameStack.peek();
    tagNameStack.push(new String(tagName));

    // Get ready to start collecting text
    if (textData == null || textData.length() > 0)
	textData = new String();

    if ("report".equals(tagName)) report(attributes);
    else if ("bean-scripting-framework".equals(tagName))
	defaultLanguage(attributes);
    else if ("language".equals(tagName)) language(attributes);
    else if ("database".equals(tagName)) database(attributes);
    else if ("query".equals(tagName)) query(attributes);
    else if ("charsep".equals(tagName)) charSepSource(attributes);
    else if ("nc-database".equals(tagName)) ncDatabaseSource(attributes);
    else if ("column".equals(tagName)) column(attributes);
    else if ("subreport-joins".equals(tagName)) inSubreportJoins = true;
    else if ("join".equals(tagName)) join(attributes);
    else if ("sort".equals(tagName)) sort(attributes);
    else if ("subreport".equals(tagName)) subreport(attributes);
    else if ("parameter".equals(tagName)) parameter(attributes);
    else if ("formula".equals(tagName)) formula(parentTag, attributes);
    else if ("usercol".equals(tagName)) usercol(attributes);
    else if ("headers".equals(tagName)) header(parentTag);
    else if ("footers".equals(tagName)) footer(parentTag);
    else if ("group".equals(tagName)) group(attributes);
    else if ("details".equals(tagName))
	nextSectionLocation = SectionArea.DETAIL;
    else if ("section".equals(tagName)) section(attributes);
    else if ("field".equals(tagName)) field(attributes);
    else if ("bounds".equals(tagName)) bounds(attributes);
    else if ("edge".equals(tagName)) edge(attributes);
    else if ("format".equals(tagName)) format(attributes);
    else if ("border".equals(tagName)) border(attributes);
    else if ("line".equals(tagName)) line(attributes);
    else if ("point".equals(tagName)) point(attributes);
    else if ("paper".equals(tagName)) paper(attributes);
    else if ("suppression-proc".equals(tagName)) suppressionProc(attributes);
}

protected void header(String parentTag) {
    if ("report".equals(parentTag))
	nextSectionLocation = SectionArea.REPORT_HEADER;
    else if ("page".equals(parentTag))
	nextSectionLocation = SectionArea.PAGE_HEADER;
    else if ("group".equals(parentTag))
	nextSectionLocation = SectionArea.GROUP_HEADER;
}

protected void footer(String parentTag) {
    if ("report".equals(parentTag))
	nextSectionLocation = SectionArea.REPORT_FOOTER;
    else if ("page".equals(parentTag))
	nextSectionLocation = SectionArea.PAGE_FOOTER;
    else if ("group".equals(parentTag))
	nextSectionLocation = SectionArea.GROUP_FOOTER;
}

/**
 * Handle elements expecting text data.
 */
public void endElement(final String namespaceURI, final String localName,
		       final String qName)
    throws SAXException
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    // If we were paranoid, we could compare tagName to the top of the
    // stack. Let's not.
    tagNameStack.pop();

    if ("description".equals(tagName))
	getReport().setDescription(textData);
    else if ("subreport".equals(tagName))
	subreport = null;
    else if ("subreport-joins".equals(tagName))
	inSubreportJoins = false;
    else if ("default".equals(tagName) && parameter != null)
	parameter.addDefaultValue(textData);
    else if ("formula".equals(tagName)) {
	formula.setExpression(textData);
	// If we need to convert this formula later, save a copy of
	// this eval string text.
	if (formulasToConvert != null) {
	    FormulaConversion fc =
		(FormulaConversion)formulasToConvert.get(formula.getName());
	    if (fc != null)
		fc.expression = new String(textData);
	}
	formula = null;
    }
    else if ("usercol".equals(tagName)) {
	usercol.setExpression(textData);
	usercol = null;
    }
    else if ("text".equals(tagName) && field != null)
	field.setValue(textData);
    else if ("where".equals(tagName))
	getReport().getDataSource().getQuery().setWhereClause(textData);
    else if ("metadata-url".equals(tagName)) {
    	try {
    	    getReport().getDataSource().readMetadataFrom(textData);
	}
    	catch (Exception e) {
	    throw new SAXException(e);  
    	}
    }	
}

/**
 * Reads text data. Text data inside a single tag can be broken up into
 * multiple calls to this method.
 */
public void characters(char ch[], int start, int length) {
    textData += new String(ch, start, length);
}

/**
 * Reads the report tag.
 */
protected void report(Attributes attributes) {
    String dtdVersionString = attributes.getValue("dtd-version");
    dtdVersion = (dtdVersionString == null)
	? DEFAULT_DTD_VERSION : Double.parseDouble(dtdVersionString);

    getReport().setName(attributes.getValue("name"));
    getReport().setTitle(attributes.getValue("title"));
    getReport().setAuthor(attributes.getValue("author"));
}

protected void defaultLanguage(Attributes attributes) {
    String lang = rubyLanguageNameHack(attributes.getValue("default-language"));
    getReport().getScripting().setDefaultLanguage(lang);
}

protected void language(Attributes attributes) {
    String lang = rubyLanguageNameHack(attributes.getValue("name"));
    getReport().getScripting().addLanguage(lang, attributes.getValue("class"));
}

protected String rubyLanguageNameHack(String lang) {
    // Hack: Scripting.java had the default value of "ruby" originally,
    // and I've changed it to "Ruby".
    return "ruby".equals(lang) ? "Ruby" : lang;
}

/**
 * Reads the database tag and creates the database object. If the report
 * already has a data source (for example, someone has called
 * <code>Report.setDataSource</code> or
 * <code>Report.setDatabaseConnection</code>), then we don't do anything.
 *
 * @see Report#hasDataSource
 * @see Report#setDatabaseConnection
 */
protected void database(Attributes attributes) {
    if (getReport().hasDataSource())
	return;

    try {
	Database db = new Database(attributes.getValue("driverClassName"),
				   attributes.getValue("connInfo"),
				   getReport(),
				   attributes.getValue("name"),
				   attributes.getValue("username"));
	getReport().setDataSource(db);
    }
    catch (UserCancellationException iae) {
	// Thrown by dataSource when user cancelled password dialog.
	// Let the report catch this.
	throw iae;
    }
    catch (Exception e) {
	ErrorHandler.error(I18N.get("ReportReader.db_err"), e,
			   I18N.get("ReportReader.db_err_title"));
    }
}

/**
 * Reads the query. Nothing to do, since the data source already has an
 * empty query.
 */
protected void query(Attributes attributes) {
}

/**
 * Reads and creates a CharSepSource.
 */
protected void charSepSource(Attributes attributes) {
    if (getReport().hasDataSource())
	return;

    CharSepSource charSepSource = new CharSepSource(getReport());
    String charString = attributes.getValue("sep-char");
    if (charString != null)
	charSepSource.setSepChar(charString.charAt(0));

    getReport().setDataSource(charSepSource);
}

/**
 * Reads and creates an NCDatabase data source.
 */
protected void ncDatabaseSource(Attributes attributes) {
    if (!getReport().hasDataSource())
	getReport().setDataSource(new NCDatabase(getReport()));
}

protected void column(Attributes attributes) {
    String name = attributes.getValue("name");
    int type = Column.typeFromString(attributes.getValue("type"));
    Column col = new Column(name, name, type);
    col.setDateParseFormat(attributes.getValue("date-format"));

    getReport().getDataSource().addColumn(col);
}

protected void join(Attributes attributes) {
    Column from = findColumn(attributes.getValue("from"));
    Column to = findColumn(attributes.getValue("to"));
    if (from != null && to != null) {
	Join join = new Join(from, attributes.getValue("relation"), to);
	if (inSubreportJoins)
	    ((Subreport)getReport()).addJoin(join);
	else
	    getReport().getDataSource().getQuery().addJoin(join);
    }
}

protected void sort(Attributes attributes) {
    String oldColumnIdStr = attributes.getValue("column");
    Selectable selectable = null;
    if (oldColumnIdStr != null) { // Old-style column id attribute
	selectable = findColumn(oldColumnIdStr.trim());
	if (selectable == null) {
	    group = null;
	    return;
	}
    }
    else {
	selectable =
	    findSelectable(attributes.getValue("groupable-id").trim(),
			  attributes.getValue("groupable-type").trim());
    }

    if (selectable != null) {
	String str = attributes.getValue("order");
	int val = (str != null && str.length() > 0 && str.charAt(0) == 'd')
	    ? Query.SORT_DESCENDING : Query.SORT_ASCENDING;
	getReport().getDataSource().getQuery().addSort(selectable, val);
    }
}

protected void subreport(Attributes attributes) {
    subreport = new Subreport(report, new Long(attributes.getValue("id")));
    // The subreport adds itself to the parent report.

    removeReportSections();	// Acts on subreport

    try {
	Database db = (Database)report.getDataSource();
	subreport.setDataSource(new SubreportDatabase(db.getConnection(),
						      subreport));
    }
    catch (Exception e) {
	ErrorHandler.error(I18N.get("ReportReader.db_err"), e,
			   I18N.get("ReportReader.db_err_title"));
    }
}

protected void parameter(Attributes attributes) throws SAXException {
    parameter = new Parameter(new Long(attributes.getValue("id")),
			      getReport(),
			      attributes.getValue("type"),
			      attributes.getValue("name"),
			      attributes.getValue("question"),
			      attributes.getValue("arity"));
    getReport().addParameter(parameter);
}

/**
 * Reads a formula. If the XML format is really old, we need to give
 * each formula an id number and translate its formula text so references
 * to other formulas use the other formula's id number instead of its name.
 */
protected void formula(String parentTag, Attributes attributes)
    throws SAXException
{
    String idString = attributes.getValue("id");
    String name = attributes.getValue("name");
    Long id = null;
    if (idString == null) {
	if (dtdVersion >= DTD_VERSION_FORMULA_IDS) {
	    String str = I18N.get("ReportReader.the_formula")
		+ ' ' + name + ' '
		+ I18N.get("ReportReader.formula_missing_id_err");
	    throw new SAXException(str);
	}
	// else, we are OK with a null id
    }
    else
	id = new Long(idString);

    if ("formulas".equals(parentTag)) {
	formula = new Formula(id, getReport(), name, null);
	getReport().addFormula(formula);
    }
    else if ("suppression-proc".equals(parentTag)) {
	// We don't use a new formula; we use the one the section has.
	formula = section.getSuppressionProc().getFormula();
    }
    else {
	formula = new Formula(id, getReport(), name, null);
	getReport().setStartFormula(formula);
    }

    // If the id is null, that means we need to convert this formula
    // to the current format.
    if (id == null) {
	if (formulasToConvert == null)
	    formulasToConvert = new HashMap();
	FormulaConversion fc = new FormulaConversion(formula, null);
	formulasToConvert.put(formula.getName(), fc);
    }

    String language = attributes.getValue("language");
    if (language != null)
	formula.setLanguage(language);
}

/**
 * Reads a user column. Value of user column will be read later.
 */
protected void usercol(Attributes attributes) throws SAXException {
    usercol = new UserColumn(new Long(attributes.getValue("id")), getReport(),
			     attributes.getValue("name"), null);
    getReport().addUserColumn(usercol);
}

/**
 * Revisits each formula and let it convert formula names to
 * formula id numbers within its eval string.
 */
protected void convertFormulas() throws SAXException {
    if (formulasToConvert != null) {
	for (Iterator iter = formulasToConvert.values().iterator();
	     iter.hasNext(); )
	{
	    FormulaConversion fc = (FormulaConversion)iter.next();
	    Formula f = fc.formula;
	    try {
		f.setEditableExpression(fc.expression);
	    }
	    catch (IllegalArgumentException iae) {
		String msg = I18N.get("ReportReader.the_formula")
		    + ' ' + f.getName() + ' '
		    + I18N.get("ReportReader.formula_unknown_name");
		throw new SAXException(msg);
	    }
	}
    }
}

/** Creates a group and adds it to the report. */
protected void group(Attributes attributes) {
    String oldColumnIdStr = attributes.getValue("column");
    Selectable selectable = null;
    if (oldColumnIdStr != null) { // Old-style column id attribute
	selectable = findColumn(oldColumnIdStr.trim());
	if (selectable == null) {
	    group = null;
	    return;
	}
    }
    else {
	selectable =
	    findSelectable(attributes.getValue("groupable-id").trim(),
			   attributes.getValue("groupable-type").trim());
    }

    group = new Group(getReport(), selectable);

    String sortOrderString = attributes.getValue("sort-order");	// OK if null
    if (sortOrderString != null)
	group.setSortOrder(Group.sortOrderStringToInt(sortOrderString.trim()));

    getReport().groups.add(group);
}

/** Creates an empty section and adds it to the report. */
protected void section(Attributes attributes) {
    section = new Section(getReport());
    section.setMinHeight(Double.parseDouble(attributes.getValue("height")));

    // Handle old XML files that store a "suppressed" attribute.
    String boolValString = attributes.getValue("suppressed");
    if ("true".equals(boolValString))
	section.getSuppressionProc().setHidden(true);

    boolValString = attributes.getValue("pagebreak");
    section.setPageBreak("true".equals(boolValString));

    addSectionToReport();
}

/**
 * Adds the last seen section to the report. The value of
 * <code>nextSectionLocation</code> determines where the section belongs.
 */
protected void addSectionToReport() {
    switch (nextSectionLocation) {
    case SectionArea.GROUP_HEADER:
	if (group != null)
	    group.headers().add(section);
	break;
    case SectionArea.GROUP_FOOTER:
	if (group != null)
	    group.footers().add(section);
	break;
    default:
	getReport().getSectionArea(nextSectionLocation).add(section);
	break;
    }
}

/**
 * Reads and creates a field. If the XML format is really old, we need to
 * convert formula fields by changing their values from the formula name
 * to the formula id.
 */
protected void field(Attributes attributes) {
    if (section == null) {	// We're reading the report's default field
	field = report.getDefaultField();
	return;
    }

    String id = attributes.getValue("id");
    String type = attributes.getValue("type");
    Object value = attributes.getValue("value");
    String visibleString = attributes.getValue("visible");

    boolean visible = true;
    if (visibleString != null) {
	visibleString = visibleString.trim().toLowerCase();
	if (visibleString.length() > 0)
	    visible = "true".equals(visibleString);
    }

    // If this is a dataSource column, make sure the column exists.
    if ("column".equals(type) && findColumn(value.toString()) == null) {
	field = null;
	return;
    }

    // If we are converting formulas without id numbers, change our value
    // (a pointer to the formula itself) from the formula's name to the
    // formula's newly-created id.
    if ("formula".equals(type) && formulasToConvert != null && value != null) {
	Formula f = ((FormulaConversion)formulasToConvert.get(value)).formula;
	if (f != null)
	    value = f.getId();
    }

    field =
	Field.create(new Long(id), getReport(), section, type, value, visible);
    if (field instanceof AggregateField && group != null)
	((AggregateField)field).setGroup(group);

    section.addField(field);
}

/** Reads and sets the current field's bounds rectangle. */
protected void bounds(Attributes attributes) {
    if (field != null)
	field.getBounds()
	    .setBounds(Double.parseDouble(attributes.getValue("x")),
		       Double.parseDouble(attributes.getValue("y")),
		       Double.parseDouble(attributes.getValue("width")),
		       Double.parseDouble(attributes.getValue("height")));
}

/** * Reads and creates the current field's format. */
protected void format(Attributes attributes) {
    if (field == null)
	return;

    Format format = field.getFormat();
    String val;

    if ((val = attributes.getValue("font")) != null)
	format.setFontFamilyName(val);
    if ((val = attributes.getValue("size")) != null)
	format.setSize(Double.parseDouble(val));
    if ((val = attributes.getValue("bold")) != null)
	format.setBold("true".equals(val));
    if ((val = attributes.getValue("italic")) != null)
	format.setItalic("true".equals(val));
    if ((val = attributes.getValue("underline")) != null)
	format.setUnderline("true".equals(val));
    if ((val = attributes.getValue("wrap")) != null)
	format.setWrap("true".equals(val));
    if ((val = attributes.getValue("align")) != null)
	format.setAlign(Format.alignFromString(val));
    if ((val = attributes.getValue("color")) != null)
	format.setColor(parseColor(val));
    if ((val = attributes.getValue("format")) != null)
	format.setFormat(val);
}

/** Parses color string and returns a <code>java.awt.Color</code>. */
protected Color parseColor(String val) {
    StringTokenizer tok = new StringTokenizer(val, ";");
    int r = Integer.parseInt(tok.nextToken().trim());
    int g = Integer.parseInt(tok.nextToken().trim());
    int b = Integer.parseInt(tok.nextToken().trim());
    int a = Integer.parseInt(tok.nextToken().trim());
    return new Color(r, g, b, a);
}

/** Reads and creates a new field border. */
protected void border(Attributes attributes) {
    if (field != null) {
	border = new Border(field);
	field.setBorder(border);

	String val = attributes.getValue("color");
	if (val != null)
	    border.setColor(parseColor(val));
    }
}

/** Reads and creates a new border edge. */
protected void edge(Attributes attributes) {
    if (field == null)
	return;

    String val = attributes.getValue("style");
    int style = BorderEdge.styleFromString(val);

    val = attributes.getValue("thickness");
    double thickness = (val == null)
	? BorderEdge.DEFAULT_THICKNESS : Double.parseDouble(val);

    val = attributes.getValue("number");
    int num = (val == null)
	? BorderEdge.DEFAULT_NUMBER : Integer.parseInt(val);

    BorderEdge edge = new BorderEdge(style, thickness, num);

    String loc = attributes.getValue("location");
    if ("top".equals(loc)) border.setTop(edge);
    else if ("bottom".equals(loc)) border.setBottom(edge);
    else if ("left".equals(loc)) border.setLeft(edge);
    else if ("right".equals(loc)) border.setRight(edge);
}

/** Reads and creates a new line. */
protected void line(Attributes attributes) {
    String val = attributes.getValue("thickness");
    double thickness = (val == null) ? 1.0 : Double.parseDouble(val);
    Color color = null;
    if ((val = attributes.getValue("color")) != null)
	color = parseColor(val);

    val = attributes.getValue("visible");
    boolean visible = true;
    if (val != null) {
	val = val.trim().toLowerCase();
	if (val.length() > 0)
	    visible = "true".equals(val);
    }

    section.lines.add(line = new Line(getReport(), section, thickness, color,
				      visible));
}

/** Reads a line's point and adds it to the current line. */
protected void point(Attributes attributes) {
    line.addEndPoint(Double.parseDouble(attributes.getValue("x")),
		     Double.parseDouble(attributes.getValue("y")));
}

/** Reads paper size name and orientation. */
protected void paper(Attributes attributes) {
    int orientation = PaperFormat.PORTRAIT; // Default value
    String orientationStr = attributes.getValue("orientation");
    if (orientationStr != null
	&& "landscape".equals(orientationStr.toLowerCase()))
	orientation = PaperFormat.LANDSCAPE;

    PaperFormat pf =
	PaperFormat.get(orientation, attributes.getValue("name"));
    if (pf != null)
	getReport().setPaperFormat(pf);
}

/** Reads suppression proc. */
protected void suppressionProc(Attributes attributes) {
    String state = attributes.getValue("hide");
    if (state != null && state.length() > 0) {
	SuppressionProc sp = section.getSuppressionProc();
	state = state.trim().toLowerCase();
	if ("true".equals(state)) sp.setHidden(true);
    }
}

/**
 * Returns the column identified by its name. The first time we can not find
 * a column, we report an error to the user.
 *
 * @param fullName a column name
 * @return a dataSource column
 */
protected Column findColumn(String fullName) {
    Column col = getReport().findColumn(fullName);
    if (col == null && !missingColumnSeen) {
	missingColumnSeen = true;
	ErrorHandler.error(I18N.get("ReportReader.the_column")
			   + ' ' + fullName + ' '
			   + I18N.get("ReportReader.column_unknown"));
    }
    return col;
}

/**
 * Returns the selectable identified by its id and type. The first time we can
 * not find one, we report an error to the user.
 *
 * @param idStr an id string
 * @param typeStr a type string ("column", "usercol")
 * @return a dataSource column
 */
protected Selectable findSelectable(String idStr, String typeStr) {
    Selectable g = getReport().findSelectable(idStr, typeStr);
    if (g == null && !missingColumnSeen) {
	missingColumnSeen = true;
	ErrorHandler.error(I18N.get("ReportReader.the_column")
			   + ' ' + idStr + " (" + typeStr + ") "
			   + I18N.get("ReportReader.column_unknown"));
    }
    return g;
}

}
