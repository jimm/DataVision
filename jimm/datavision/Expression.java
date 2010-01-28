package jimm.datavision;
import jimm.datavision.field.Field;
import jimm.util.XMLWriter;
import jimm.util.StringUtils;
import jimm.util.Replacer;
import jimm.datavision.source.Column;
import jimm.util.I18N;
import java.util.*;

/**
 * The abstract superclass of objects that are evaluated, such as formulas
 * and user columns. An expression contains text that is evaluated. The
 * text may contain database column values, formulas, special values,
 * or other types of objects.
 * <p>
 * Before being evaluated, the following substitutions are made withing
 * the evaluation string:
 * <ul>
 * <li>{<i>table_name.column_name</i>} is replaced by the current value
 * of the column <i>table_name.column_name</i>.</li>
 * <li>{&#64;<i>id_number</i>} is replaced by the results of evaluating the
 * formula whose id is <i>id_number</i>.</li>
 * <li> {%<i>special_value_name</i>} is replaced by a special value
 * (report title, report run date, page number, or record number).</li>
 * <li> {?<i>id_number</i>} is replaced by a parameter value (string,
 * number, or date).</li>
 * <li> {!<i>id_number</i>} is replaced by a user column's value (string,
 * number, or date).</li>
 * <ul>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class Expression
    extends Observable
    implements Identity, Nameable, Writeable, Draggable, Observer
{

protected Long id;
protected Report report;
protected String name;
protected String expr;
protected String exceptAfter;
protected ArrayList observedContents;

/**
 * Given a string, returns a string with all instances of formula,
 * parameter, and user column "formula strings" replaced by "display name"
 * strings. If there are no such strings, the original string is returned.
 *
 * @return a string with all formula strings replaced by display name
 * strings
 */
public static String expressionToDisplay(Report report, String str) {
    if (str == null || str.length() == 0 || str.indexOf("{") == -1)
	return str;

    StringBuffer buf = new StringBuffer();
    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	if (c == '{' && (i + 1) < len) {
	    int nameStart, nameEnd;
	    switch (str.charAt(i + 1)) {
	    case '@':		// Formula
		nameStart = i + 2;
		nameEnd = str.indexOf("}", nameStart);
		if (nameEnd != -1) {
		    String idAsString = str.substring(nameStart, nameEnd);
		    buf.append("{@");
		    buf.append(report.findFormula(idAsString).getName());
		    buf.append("}");
		    i = nameEnd;
		}
		break;
	    case '?':		// Parameter
		nameStart = i + 2;
		nameEnd = str.indexOf("}", nameStart);
		if (nameEnd != -1) {
		    String idAsString = str.substring(nameStart, nameEnd);
		    buf.append("{?");
		    buf.append(report.findParameter(idAsString).getName());
		    buf.append("}");
		    i = nameEnd;
		}
		break;
	    case '!':		// User column
		nameStart = i + 2;
		nameEnd = str.indexOf("}", nameStart);
		if (nameEnd != -1) {
		    String idAsString = str.substring(nameStart, nameEnd);
		    buf.append("{!");
		    buf.append(report.findUserColumn(idAsString).getName());
		    buf.append("}");
		    i = nameEnd;
		}
		break;
	    default:
		buf.append(c);
		break;
	    }
	}
	else {
	    buf.append(c);
	}
    }
    return buf.toString();
}

/**
 * Given a string, returns a string with all instances of formula,
 * parameter, and user column "display names" replaced by "formula
 * strings". If there are no such strings, the original string is returned.
 *
 * @param report a report
 * @param str a string with display names
 * @return a string with all display names replaced by formula strings
 */
public static String displayToExpression(Report report, String str) {
    if (str == null || str.length() == 0 || str.indexOf("{") == -1)
	return str;

    StringBuffer buf = new StringBuffer();
    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	if (c == '{' && (i + 1) < len) {
	    int nameStart, nameEnd;
	    switch (str.charAt(i + 1)) {
	    case '@':		// Formula
		nameStart = i + 2;
		nameEnd = str.indexOf("}", nameStart);
		if (nameEnd != -1) {
		    String formulaName = str.substring(nameStart, nameEnd);
		    buf.append("{@");
		    Formula formula = report.findFormulaByName(formulaName);
		    if (formula == null) {
			str = I18N.get("Utils.in")
			    + " \"" + str + "\": "
			    + I18N.get("Utils.no_such_formula")
			    + ' ' + formulaName;
			throw new IllegalArgumentException(str);
		    }
		    buf.append(formula.getId());
		    buf.append("}");
		    i = nameEnd;
		}
		break;
	    case '?':		// Parameter
		nameStart = i + 2;
		nameEnd = str.indexOf("}", nameStart);
		if (nameEnd != -1) {
		    String paramName = str.substring(nameStart, nameEnd);
		    buf.append("{?");
		    Parameter param = report.findParameterByName(paramName);
		    if (param == null) {
			str = I18N.get("Utils.in")
			    + " \"" + str + "\": "
			    + I18N.get("Utils.no_such_param")
			    + ' ' + paramName;
			throw new IllegalArgumentException(str);
		    }
		    buf.append(param.getId());
		    buf.append("}");
		    i = nameEnd;
		}
		break;
	    case '!':		// User column
		nameStart = i + 2;
		nameEnd = str.indexOf("}", nameStart);
		if (nameEnd != -1) {
		    String ucName = str.substring(nameStart, nameEnd);
		    buf.append("{!");
		    UserColumn uc = report.findUserColumnByName(ucName);
		    if (uc == null) {
			str = I18N.get("Utils.in")
			    + " \"" + str + "\": "
			    + I18N.get("Utils.no_such_usercol")
			    + ' ' + ucName;
			throw new IllegalArgumentException(str);
		    }
		    buf.append(uc.getId());
		    buf.append("}");
		    i = nameEnd;
		}
		break;
	    default:
		buf.append(c);
		break;
	    }
	}
	else {
	    buf.append(c);
	}
    }

    return buf.toString();
}

/**
 * Constructor. If <i>id</i> is <code>null</code>, throws an
 * <code>IllegalArgumentException</code>. This is because subclasses are
 * responsible for generating their id number. For example, formulas call
 * <code>Report.generateNewFormulaId</code>.
 *
 * @param id the unique identifier for the new expression; may not be
 * <code>null</code>
 * @param report the report containing this expression
 * @param name the expression name
 * @param expression the string to evaulate at runtime; may be
 * <code>null</code>
 * @param exceptAfter when looking for things inside "{}" braces, ignore
 * braces immediately after this string
 */
protected Expression(Long id, Report report, String name, String expression,
		     String exceptAfter)
{
    if (id == null)		// Need not use I18N; this is a programmer err
	throw new IllegalArgumentException("Subclasses of Expression must"
					   + " not pass in a null id");

    this.report = report;
    this.id = id;
    this.name = name;
    expr = expression;
    this.exceptAfter = exceptAfter;

    // I'd like to start observing the contents of the eval string here,
    // but the other expressions may not yet be defined (for example, when
    // reading in expressions from an XML file). That's why we start observing
    // contents when someone asks for the eval string.
    observedContents = null;
}

protected void finalize() throws Throwable {
    stopObservingContents();
    super.finalize();
}

public void update(Observable o, Object arg) {
    setChanged();
    notifyObservers(arg);
}

public Object getId() { return id; }

/**
 * Returns the name for this expression.
 *
 * @return the name
 */
public String getName() { return name; }

/**
 * Sets the name.
 *
 * @param newName the new name
 */
public void setName(String newName) {
    if (name != newName && (name == null || !name.equals(newName))) {
	name = newName;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the expression string.
 *
 * @return the eval string
 */
public String getExpression() {
    // I'd like to start observing the contents of the eval string as soon
    // as we are constructed, but the other expressions may not yet be defined
    // (for example, when reading in expressions from an XML file). That's why
    // we start observing contents when someone asks for the eval string.
    if (observedContents == null)
	startObservingContents();

    return expr;
}

/**
 * Sets the eval string.
 *
 * @param newExpression the new eval string
 */
public void setExpression(String newExpression) {
    if (expr != newExpression
	&& (expr == null || !expr.equals(newExpression)))
    {
	stopObservingContents();
	expr = newExpression;

	// Don't start observing contents yet. Wait until someone calls
	// getExpression(). See the comment there.
//  	startObservingContents();

	setChanged();
	notifyObservers();
    }
}

/**
 * Starts observing all observables referenced by this expression: formulas,
 * parameters, and user columns.
 */
protected void startObservingContents() {
    observedContents = new ArrayList();	// Even if expr is null

    if (expr == null || expr.length() == 0)
	return;

    // Here, we are using replacers so we can start observing things.
    // Usually, they are used to replace strings.

    // Formulas
    StringUtils.replaceDelimited(exceptAfter, "{@", "}", new Replacer() {
	public Object replace(String str) {
	    Formula f = report.findFormula(str);
	    observedContents.add(f);
	    f.addObserver(Expression.this);
	    return "";		// Avoid early bail-out
	}},
			   expr);

    // Parameters
    StringUtils.replaceDelimited(exceptAfter, "{?", "}", new Replacer() {
	public Object replace(String str) {
	    Parameter p = report.findParameter(str);
	    observedContents.add(p);
	    p.addObserver(Expression.this);
	    return "";		// Avoid early bail-out
	}},
			   expr);

    // User columns
    StringUtils.replaceDelimited(exceptAfter, "{!", "}", new Replacer() {
	public Object replace(String str) {
	    UserColumn uc = report.findUserColumn(str);
	    observedContents.add(uc);
	    uc.addObserver(Expression.this);
	    return "";		// Avoid early bail-out
	}},
			   expr);
}

/**
 * Stops observing that which we were observing.
 */
protected void stopObservingContents() {
    if (observedContents != null) {
	for (Iterator iter = observedContents.iterator(); iter.hasNext(); )
	    ((Observable)iter.next()).deleteObserver(this);
	observedContents = null;
    }
}

/**
 * Returns the expression string fit for human consumption. This mainly means
 * that we substitute formula, parameter, and user column numbers with names.
 * Called from any expression editor. This code assumes that curly braces are
 * never nested.
 *
 * @return the eval string with formula, parameter, and user column id numbers
 * replaced with names
 */
public String getEditableExpression() {
    return expressionToDisplay(report, getExpression());
}

/**
 * Sets the eval string after replacing formula, parameter, and user column
 * names with their id numbers. Called from a editor.
 * <p>
 * This method will throw an <code>IllegalArgumentException</code> if any
 * formula, parameter, or user column name is not the name of some existing
 * object.
 *
 * @param newExpression the new eval string
 * @throws IllegalArgumentException
 */
public void setEditableExpression(String newExpression) {
    setExpression(displayToExpression(report, newExpression));
}

public abstract String dragString();
public abstract String designLabel();
public abstract String formulaString();

/**
 * Returns <code>true</code> if this expression contains a reference to the
 * specified field.
 *
 * @param f a field
 * @return <code>true</code> if this field contains a reference to the
 * specified field
 */
public boolean refersTo(Field f) {
    String str = getExpression();
    if (str != null && str.length() > 0)
	return str.indexOf(f.formulaString()) != -1;
    else
	return false;
}

/**
 * Returns <code>true</code> if this expression contains a reference to the
 * specified expression (formula or user column).
 *
 * @param expression an expression
 * @return <code>true</code> if this field is the same as or contains a
 * reference to the specified expression
 */
public boolean refersTo(Expression expression) {
    String str = getExpression();
    if (str != null && str.length() > 0)
	return str.indexOf(expression.formulaString()) != -1;
    else
	return false;
}

/**
 * Returns <code>true</code> if this expression contains a reference to the
 * specified parameter.
 *
 * @param p a parameter
 * @return <code>true</code> if this field contains a reference to the
 * specified parameter
 */
public boolean refersTo(Parameter p) {
    String str = getExpression();
    if (str != null && str.length() > 0)
	return str.indexOf(p.formulaString()) != -1;
    else
	return false;
}

/**
 * Returns a collection of the columns used in the expression. This is used
 * by the report's query when it is figuring out what columns and tables
 * are used by the report.
 *
 * @return a possibly empty collection of database columns
 * @see jimm.datavision.source.Query#findSelectablesUsed
 */
public Collection columnsUsed() {
    final ArrayList list = new ArrayList();

    // We are using a replacer passively, to look for curly-delimited
    // expressions. Nothing in the expression text gets modified.
    StringUtils.replaceDelimited(exceptAfter, "{", "}", new Replacer() {
	public Object replace(String str) {
	    switch (str.charAt(0)) {
	    case '!':		// User column
		UserColumn uc = report.findUserColumn(str.substring(1));
		if (uc != null)	// Should never be null
		    list.addAll(uc.columnsUsed());
		break;
	    case '%':		// Special field
	    case '@':		// Formula
	    case '?':		// Parameter
		break;		// ...all are ignored
	    default:
		Column col = report.findColumn(str);
		if (col != null)	// May be null if language uses braces
		    list.add(col);
	    }
	    return "";		// So we don't quit early
	}},
			   getExpression());

    return list;
}

/**
 * Returns a collection of the user columns used in the expression. This
 * is used by the report's query when it is figuring out what columns,
 * tables, and user columns are used by the report.
 *
 * @return a possibly empty collection of user columns
 * @see jimm.datavision.source.Query#findSelectablesUsed
 */
public Collection userColumnsUsed() {
    final ArrayList list = new ArrayList();

    // We are using a replacer passively, to look for curly-delimited
    // expressions. Nothing in the expression text gets modified.
    StringUtils.replaceDelimited(exceptAfter, "{!", "}", new Replacer() {
	public Object replace(String str) {
	    UserColumn uc = report.findUserColumn(str);
	    if (uc != null)	// Should never be null
		list.add(uc);
	    return "";		// So we don't bail out
	}},
			   getExpression());

    return list;
}

/**
 * Writes this expression as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public abstract void writeXML(XMLWriter out);

protected void writeXML(XMLWriter out, String elementName) {
    out.startElement(elementName);
    out.attr("id", id);
    out.attr("name", name);
    writeAdditionalAttributes(out);
    out.cdata(getExpression());
    out.endElement();
}

/**
 * Writes additional attributes. Default behavior is to do nothing.
 *
 * @param out a writer that knows how to write XML
 */
protected void writeAdditionalAttributes(XMLWriter out) {}

}
