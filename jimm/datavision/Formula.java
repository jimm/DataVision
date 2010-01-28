package jimm.datavision;
import jimm.datavision.field.Field;
import jimm.datavision.field.SpecialField;
import jimm.datavision.source.Column;
import jimm.util.StringUtils;
import jimm.util.XMLWriter;
import jimm.util.Replacer;
import jimm.util.I18N;

/**
 * A formula is a Bean Scripting Framework (BSF) script evaluated at runtime.
 * It may contain database column values, other formulas, special values, and
 * aggregates.
 * <p>
 * Before being evaluated, the following substitutions are made withing
 * the evaluation string of a formula:
 * <ul>
 * <li>{<i>table_name.column_name</i>} is replaced by the current value
 * of the column <i>table_name.column_name</i>, but only if the column exists.
 * If it doesn't, then the entire "{...}" text is kept as-is.</li>
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
public class Formula extends Expression {

protected String language;
protected Object cachedEvalResult;
protected boolean useCache;
protected boolean shouldEvaluate;
protected boolean showException;

/**
 * Constructor.
 *
 * @param id the unique identifier for the new formula; if <code>null</code>,
 * generate a new id
 * @param report the report containing this formula
 * @param name the formula name
 */
public Formula(Long id, Report report, String name) {
    this(id, report, name, null);
    useCache = false;
    shouldEvaluate = true;
    showException = true;
}

/**
 * Constructor. If <i>id</i> is <code>null</code>, generates a new id number.
 * This number is one higher than any previously-seen id number. This does
 * <em>not</em> guarantee that no later formula will be created manually with
 * the same id number.
 *
 * @param id the unique identifier for the new formula; if <code>null</code>,
 * generate a new id
 * @param report the report containing this formula
 * @param name the formula name
 * @param evalString the string to evaulate at runtime.
 */
public Formula(Long id, Report report, String name, String evalString) {
    super(id == null ? report.generateNewFormulaId() : id, report, name,
	  evalString, "#");
    language = report.getScripting().getDefaultLanguage();
}


public String dragString() {
    return "formula:" + getId();
}

public String designLabel() {
    return "{@" + getName() + "}";
}

public String formulaString() {
    return "{@" + getId() + "}";
}

/**
 * Tells this formula to evaluate only when <var>shouldEvaluate</var> is
 * <code>true</code> and to return the cached value otherwise.
 */
public void useCache() { useCache = true; }

/**
 * Tells this formula to evaluate the BSF script the next time
 * <code>eval</code> is called.
 */
public void shouldEvaluate() { shouldEvaluate = true; }

public void setExpression(String newExpression) {
    super.setExpression(newExpression);
    showException = true;
}

/**
 * Evaluate this formula and return the result as either a
 * <code>String</code> or a <code>Double</code>. Before evaluation, special
 * fields, formulas, and column references are subtituted with their
 * values.
 * <p>
 * After substitution, if the formula contains a <code>null</code> anywhere
 * then <code>null</code> is returned.
 *
 * @return the result of evaluating the formula as either a
 * <code>String</code> or a <code>Double</code>; possibly <code>null</code>
 */
public Object eval() {
    return eval(null);
}

/**
 * Evaluate this formula and return the result. Before evaluation, special
 * fields, formulas, and column references are subtituted with their
 * values. <code>null</code> values are replaced by &quot;nil&quot;.
 * <p>
 * After substitution, if the formula contains a <code>null</code> anywhere
 * then <code>null</code> is returned. If the BSF script throws an exception,
 * then <code>null</code> is returned.
 * <p>
 * During substitution, we take care not to replace "#{...}" Ruby string
 * substition operators.
 * <p>
 * The <var>formulaField</var> is used when the formula contains a
 * &quot;group.count&quot; special field.
 *
 * @param formulaField the field containing this formula; may be
 * <code>null</code>
 * @return the result of evaluating the formula as a BSF script; possibly
 * <code>null</code>
 * 
 */
public Object eval(Field formulaField) {
    if (!useCache || shouldEvaluate) {
	cachedEvalResult = evaluate(formulaField);
	shouldEvaluate = false;
    }
    return cachedEvalResult;
}

/**
 * Modifies the formula text so it is ready to evaluate, then gives it to the
 * report to evaluate and returns the result. {@link #eval} calls this method
 * and stores the return value into <var>cachedEvalResult</var>.
 *
 * @param formulaField the field that is using this formula, used to
 * evaluate any special fields in the formula; may be <code>null</code>
 * @return the result of evaluating the formula as a BSF script; possibly
 * <code>null</code>
 * @see SpecialField#value
 */
protected Object evaluate(final Field formulaField) {
    String str = getExpression();
    if (str == null || str.trim().length() == 0)
	return null;

    // Special values
    str = StringUtils.replaceDelimited("#", "{%", "}", new Replacer() {
	public Object replace(String str) {
	    Object obj = SpecialField.value(formulaField, str, report);
	    return obj == null ? "nil" : obj;
	}},
			   str);
    if (str == null) return null;

    // Formula values
    str = StringUtils.replaceDelimited("#", "{@", "}", new Replacer() {
	public Object replace(String str) {
	    Formula f = report.findFormula(str);
	    return f == null ? "nil" : f.eval(formulaField);
	}},
			   str);
    if (str == null) return null;

    // Parameter values
    str = StringUtils.replaceDelimited("#", "{?", "}", new Replacer() {
	public Object replace(String str) {
	    Parameter p = report.findParameter(str);
	    return p == null ? "nil" : p.getValue();
	}},
			   str);
    if (str == null) return null;

    // User column values
    str = StringUtils.replaceDelimited("#", "{!", "}", new Replacer() {
	public Object replace(String str) {
	    UserColumn uc = report.findUserColumn(str);
	    return uc == null ? "nil" : report.columnValue(uc);
	}},
			   str);
    if (str == null) return null;

    // Column values
    str = StringUtils.replaceDelimited("#", "{", "}", new Replacer() {
	public Object replace(String str) {
	    Column col = report.findColumn(str);
	    if (col == null)
		return "{" + str + "}";

	    Object val = null;
	    switch (col.getType()) {
	    case java.sql.Types.CHAR:
	    case java.sql.Types.VARCHAR:
	    case java.sql.Types.DATE:
	    case java.sql.Types.TIME:
	    case java.sql.Types.TIMESTAMP:
		val = report.columnValue(col);
		val = val == null ? "nil" : quoted(val);
		break;
	    default:
		val = report.columnValue(col);
		if (val == null)
		    val = "nil";
		break;
	    }
	    return val;
	}},
			   str);
    if (str == null || str.trim().length() == 0) return null;

    try {
	return report.eval(getLanguage(), str, getName());
    }
    catch (Exception e) {
	if (showException) {
	    showException = false;
	    // I don't pass e to error() so we avoid a stack trace, which
	    // will be almost useless to the user or to me.
	    ErrorHandler.error(I18N.get("Formula.script_error")
			       + " \"" + str + '"' + ": " + e.toString(),
			       I18N.get("Formula.script_error_title"));
	}
	return null;
    }
}

/**
 * Returns the scripting language this formula uses.
 *
 * @return the language to use when evaluating this formula
 */
public String getLanguage() {
    return language == null ? report.getScripting().getDefaultLanguage()
	: language;
}

public void setLanguage(String newLang) {
    if (newLang == null) newLang = report.getScripting().getDefaultLanguage();

    if (!language.equals(newLang)) {
	language = newLang;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns a string representation of an object enclosed in double quotes.
 * Each double quote and backslash within the object's string
 * representation is escaped with a backslash character.
 *
 * @param obj any object
 * @return a double-quoted string representation of the object
 */
protected String quoted(Object obj) {
    String val = obj.toString();
    StringBuffer buf = new StringBuffer("\"");
    int len = val.length();
    for (int i = 0; i < len; ++i) {
	char c = val.charAt(i);
	switch (c) {
	case '"': case '\\':
	    buf.append('\\');
	    buf.append(c);
	    break;
	default:
	    buf.append(c);
	    break;
	}
    }
    buf.append('"');
    return buf.toString();
}

/**
 * If <var>obj</var> is a <code>String</code> and it is surrounded by
 * double quotes, return an unquoted copy. All doubled double quotes
 * are turned into single double quotes.
 *
 * @param obj any object
 * @return <var>obj</var> if it is not a <code>String</code>, an unquoted
 * copy if it is a <code>String</code>
 */
public Object unquoted(Object obj) {
    if (obj instanceof String) {
	String str = (String)obj;
	if (str.startsWith("\"") && str.endsWith("\"")) {
	    str = str.substring(1, str.length() - 1);
	    StringBuffer buf = new StringBuffer();
	    int oldPos = 0;
	    for (int pos = str.indexOf("\\"); pos != -1;
		 pos = str.indexOf("\\", oldPos))
	    {
		buf.append(str.substring(oldPos, pos));
		buf.append(str.charAt(pos + 1));
		oldPos = pos + 2;
	    }
	    buf.append(str.substring(oldPos));
	    return buf.toString();
	}
    }
    return obj;
}

public void writeXML(XMLWriter out) {
    writeXML(out, "formula");
}

protected void writeAdditionalAttributes(XMLWriter out) {
    if (language != null && language.length() != 0
	&& !language.equals(report.getScripting().getDefaultLanguage()))
	out.attr("language", language);
	
}

}
