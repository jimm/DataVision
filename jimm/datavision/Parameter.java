package jimm.datavision;
import jimm.util.XMLWriter;
import jimm.util.I18N;
import java.util.Observable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

/**
 * A parameter is a piece of data the value of which is determined by
 * asking the user each time a report runs. Default values are only used
 * when asking the user for values, not when generating values in
 * <code>getValue</code>.
 * <p>
 * I started out with subclasses for each type of parameter. The problem is,
 * the user gets to pick what kind of data the parameter holds and that
 * type can be changed any time after the parameter gets created. Therefore,
 * we hold objects and change our output based on the type of the data.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Parameter
    extends Observable
    implements Identity, Nameable, Writeable, Draggable, Cloneable
{

public static final int TYPE_BOOLEAN = 0;
public static final int TYPE_STRING = 1;
public static final int TYPE_NUMERIC = 2;
public static final int TYPE_DATE = 3;

public static final int ARITY_ONE = 0;
public static final int ARITY_RANGE = 1;
public static final int ARITY_LIST_SINGLE = 2;
public static final int ARITY_LIST_MULTIPLE = 3;

protected static SimpleDateFormat formatter =
    new SimpleDateFormat("yyyy-MM-dd");
protected static ParsePosition parsePosition = new ParsePosition(0);

protected Long id;
protected Report report;
protected String name;
protected String question;
protected int type;
protected int arity;
protected ArrayList defaultValues;
protected ArrayList values;

/**
 * Constructor. Creates a string parameter with no name or question
 * string.
 *
 * @param id the unique identifier for the new parameter; if
 * <code>null</code>, generate a new id
 * @param report the report in which this parameter resides
 */
public Parameter(Long id, Report report)
{
    this(id, report, "string", "", "", "single");
}

/**
 * Constructor.
 * <p>
 * If <i>id</i> is <code>null</code>, generates a new id number. This number
 * is one higher than any previously-seen id number. This does <em>not</em>
 * guarantee that no later parameter will be created manually with the same
 * id number.
 *
 * @param id the unique identifier for the new parameter; if
 * <code>null</code>, generate a new id
 * @param report the report in which this parameter resides
 * @param typeName one of "string", "numeric", or "date"; found in report XML
 * @param name the name of this parameter
 * @param question the question to ask when getting the parameter's value
 * from the user
 * @param arityString arity (single, range, list) as a string
 */
public Parameter(Long id, Report report, String typeName, String name,
		    String question, String arityString)
{
    this.report = report;

    // Convert type name to type number.
    if (typeName == null || typeName.length() == 0) {
	String str = I18N.get("Parameter.param_cap") + " " + id + ": "
	    + I18N.get("Parameter.missing_type");
	throw new IllegalArgumentException(str);
    }

    typeName = typeName.toLowerCase().trim();
    if ("boolean".equals(typeName)) type = TYPE_BOOLEAN;
    else if ("string".equals(typeName)) type = TYPE_STRING;
    else if ("numeric".equals(typeName)) type = TYPE_NUMERIC;
    else if ("date".equals(typeName)) type = TYPE_DATE;
    else {
	String str = I18N.get("Parameter.param_cap") + " " + id + ": "
	    + I18N.get("Parameter.illegal_type");
	throw new IllegalArgumentException(str);
    }

    this.name = name;
    this.question = question;

    // Convert arity string ("range", "list", or "single").
    if (arityString == null || arityString.length() == 0) {
	String str = I18N.get("Parameter.param_cap") + id + ": "
	    + I18N.get("Parameter.missing_arity");
	throw new IllegalArgumentException(str);
    }
    arityString = arityString.toLowerCase().trim();
    if ("single".equals(arityString)) arity = ARITY_ONE;
    else if ("range".equals(arityString)) arity = ARITY_RANGE;
    else if ("list-single".equals(arityString)) arity = ARITY_LIST_SINGLE;
    else if ("list-multiple".equals(arityString)) arity = ARITY_LIST_MULTIPLE;
    else {
	String str = I18N.get("Parameter.param_cap") + id + ": "
	    + I18N.get("Parameter.illegal_arity");
	throw new IllegalArgumentException(str);
    }

    initialize(id);
}

/**
 * Constructor.
 * <p>
 * If <i>id</i> is <code>null</code>, generates a new id number. This number
 * is one higher than any previously-seen id number. This does <em>not</em>
 * guarantee that no later parameter will be created manually with the same
 * id number.
 *
 * @param id the unique identifier for the new parameter; if
 * <code>null</code>, generate a new id
 * @param report the report in which this parameter resides
 * @param type one of
 * <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code>
 * @param name the name of this parameter
 * @param question the name of this parameter
 * @param arity one of <code>ARITY_ONE</code>, <code>ARITY_RANGE</code>,
 * <code>ARITY_LIST_SINGLE</code>, or <code>ARITY_LIST_MULTIPLE</code>
 */
public Parameter(Long id, Report report, int type, String name,
		    String question, int arity)
{
    this.report = report;
    this.type = type;
    this.name = name;
    this.question = question;
    this.arity = arity;

    initialize(id);
}

private void initialize(Long id) {
    if (id == null)
	id = report.generateNewParameterId();
    this.id = id;

    // Check for legal combinations of type and arity
    switch (type) {
    case TYPE_BOOLEAN:
	if (arity != ARITY_ONE) {
	    String str = I18N.get("Parameter.param_cap") + id + ": "
		+ I18N.get("Parameter.yesno_single");
	    throw new IllegalArgumentException(str);
	}
	break;
    case TYPE_DATE:
	if (arity != ARITY_ONE && arity != ARITY_RANGE) {
	    String str = I18N.get("Parameter.param_cap") + id + ": "
		+ I18N.get("Parameter.date_arity_err");
	    throw new IllegalArgumentException(str);
	}
	break;
    }

    defaultValues = new ArrayList();
    values = new ArrayList();
}

public Object clone() {
    Parameter p = new Parameter(null, report, type, name, question, arity);
    for (Iterator iter = defaultValues.iterator(); iter.hasNext(); ) {
	Object obj = iter.next();
	if (obj instanceof Boolean)
	    p.defaultValues.add(obj);
	else if (obj instanceof String)
	    p.defaultValues.add(new String((String)obj));
	else if (obj instanceof Number)
	    p.defaultValues.add(obj);
	else if (obj instanceof Date)
	    p.defaultValues.add(((Date)obj).clone());
    }
    return p;
}

public Object getId() { return id; }

/**
 * Returns the name for this parameter.
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
 * Returns the question for this parameter.
 *
 * @return the question
 */
public String getQuestion() { return question; }

/**
 * Sets the question.
 *
 * @param newQuestion the new question
 */
public void setQuestion(String newQuestion) {
    if (question != newQuestion
	&& (question == null || !question.equals(newQuestion)))
    {
	question = newQuestion;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the type of this field. Will be one of
 * <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code>.
 *
 * @return the type number
 */
public int getType() { return type; }

/**
 * Sets the parameter type. Must be one of
 * <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code>. If the new type
 * is different than the old, we also make sure the arity is appropriate
 * (for example, no boolean lists) and clear the value and default value
 * lists.
 *
 * @param newType the new type; must be one of <code>TYPE_BOOLEAN</code>,
 * <code>TYPE_STRING</code>, <code>TYPE_NUMERIC</code>, or
 * <code>TYPE_DATE</code>
 */
public void setType(int newType) {
    if (type != newType) {
	type = newType;

	defaultValues.clear();
	values.clear();

	if (type == TYPE_BOOLEAN) {
	    if (arity != ARITY_ONE)
		arity = ARITY_ONE;
	}
	else if (type == TYPE_DATE) {
	    if (arity == ARITY_LIST_SINGLE || arity == ARITY_LIST_MULTIPLE)
		arity = ARITY_ONE;
	}

	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the arity of this field. Will be one of <code>ARITY_ONE</code>,
 * <code>ARITY_RANGE</code>, <code>ARITY_LIST_SINGLE</code>, or
 * <code>ARITY_LIST_MULTIPLE</code>.
 *
 * @return the arity number
 */
public int getArity() { return arity; }

/**
 * Returns <code>true</code> if the specified combination of type and arity
 * are legal.
 *
 * @param aType one of <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code>
 * @param anArity one of <code>ARITY_ONE</code>, <code>ARITY_RANGE</code>,
 * <code>ARITY_LIST_SINGLE</code>, or <code>ARITY_LIST_MULTIPLE</code>
 * @return <code>true</code> if the specified combination of type and arity
 * are legal
 */
public boolean isLegal(int aType, int anArity) {
    switch (aType) {
    case TYPE_BOOLEAN:
	return anArity == ARITY_ONE;
    case TYPE_DATE:
	return anArity != ARITY_LIST_SINGLE && anArity != ARITY_LIST_MULTIPLE;
    case TYPE_STRING:
    case TYPE_NUMERIC:
    default:
	return true;
    }
}

/**
 * Sets the parameter arity. Must be one of <code>ARITY_ONE</code>,
 * <code>ARITY_RANGE</code>, <code>ARITY_LIST_SINGLE</code>, or
 * <code>ARITY_LIST_MULTIPLE</code>. We disallow illegal arity values.
 * For example, if our type is boolean we disallow a list arity.
 *
 * @param newArity one of <code>ARITY_ONE</code>, <code>ARITY_RANGE</code>,
 * <code>ARITY_LIST_SINGLE</code>, or <code>ARITY_LIST_MULTIPLE</code>
 */
public void setArity(int newArity) {
    if (arity != newArity) {

	if (type == TYPE_BOOLEAN) {
	    if (newArity != ARITY_ONE) {
		String str = I18N.get("Parameter.param_cap") + id + ": "
		    + I18N.get("Parameter.yesno_single");
		throw new IllegalArgumentException(str);
	    }
	}
	else if (type == TYPE_DATE) {
	    if (newArity == ARITY_LIST_SINGLE
		|| newArity == ARITY_LIST_MULTIPLE)
	    {
		String str = I18N.get("Parameter.param_cap") + id + ": "
		    + I18N.get("Parameter.date_arity_err");
		throw new IllegalArgumentException(str);
	    }
	}

	arity = newArity;

	defaultValues.clear();
	values.clear();

	setChanged();
	notifyObservers();
    }
}

/**
 * Returns an iterator over the default values for this parameter.
 *
 * @return an interator
 */
public Iterator defaultValues() { return defaultValues.iterator(); }

/**
 * Returns the i'th defaultValue for this parameter. If none has been
 * assigned, create and return -- but do not store -- a reasonable default.
 * The default is obtained by calling {@link #getDefaultForType}.
 *
 * @param i the index
 * @return the defaultValue
 */
public Object getDefaultValue(int i) {
    Object val;
    if (i < 0 || i >= defaultValues.size()
	|| (val = defaultValues.get(i)) == null)
    {
	return getDefaultForType(type);
    }
    else
	return val;
}

/**
 * Returns the default value for a specific parameter type. This is not
 * the same as the i'th default value; it is called when you have a
 * parameter that has no value or default value, or when you have one
 * with a different type and you want to switch types.
 *
 * @param type one of <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code>
 * @return a new object appropriate for the type
 */
public Object getDefaultForType(int type) {
	switch (type) {
	case TYPE_BOOLEAN: return Boolean.valueOf(false);
	case TYPE_STRING: return "";
	case TYPE_NUMERIC: return new Integer(0);
	case TYPE_DATE: return new Date();
	default:
	    String str = I18N.get("Paramter.illegal_type_value");
	    throw new IllegalArgumentException(str + " " + type);
	}
}

/**
 * Erases all default values.
 */
public void removeDefaultValues() {
    if (defaultValues.size() > 0) {
	defaultValues.clear();
	setChanged();
	notifyObservers();
    }
}

/**
 * Adds a default value to the list.
 *
 * @param newDefaultValue a new default value
 */
public void addDefaultValue(Object newDefaultValue) {
    // Make sure newDefaultValue is of proper type for the values we hold
    newDefaultValue = convertType(newDefaultValue);

    defaultValues.add(newDefaultValue);
    setChanged();
    notifyObservers();
}

/**
 * Sets the i'th defaultValue. If <var>i</var> is out of range,
 * the list of default values grows to fit.
 *
 * @param i the index
 * @param newDefaultValue a value
 */
public void setDefaultValue(int i, Object newDefaultValue) {
    // Make sure newDefaultValue is of proper type for the values we hold
    newDefaultValue = convertType(newDefaultValue);

    Object defaultValue = null;
    if (i < defaultValues.size())
	defaultValue = getDefaultValue(i);
    if (defaultValue != newDefaultValue
	&& (defaultValue == null || !defaultValue.equals(newDefaultValue)))
    {
	defaultValues.add(i, newDefaultValue);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns an iterator over the values for this parameter.
 *
 * @return an interator
 */
public Iterator values() { return values.iterator(); }

/**
 * Returns the parameter value(s) the user has previously specified. If
 * the parameter has one value, return that value or possibly null. Else,
 * return a copy of our list of values.
 *
 * @return values (see description)
 */
public Object getValue() {
    switch (arity) {
    case ARITY_ONE:
    case ARITY_LIST_SINGLE:
	return getValue(0);
    case ARITY_RANGE:
	ArrayList list = new ArrayList();
	list.add(getValue(0));
	list.add(getValue(1));
	return list;
    case ARITY_LIST_MULTIPLE:
	return values.clone();
    }
    return null;		// Will never happen
}

/**
 * Returns the current value or, if that is <code>null</code>, the default
 * value. If the index is out of range, return <code>null</code>.
 *
 * @param i the index
 * @return the current or default value.
 */
public Object getValue(int i) {
    Object val = null;
    if (i < values.size())
	val = values.get(i);
    if (val == null) {
	if (i < defaultValues.size())
	    val = defaultValues.get(i);
    }
    return val;
}

/**
 * Adds a value to the list.
 *
 * @param newValue a new value
 */
public void addValue(Object newValue) {
    values.add(convertType(newValue));
    setChanged();
    notifyObservers();
}

/**
 * Erases all values.
 */
public void removeValues() {
    if (values.size() > 0) {
	values.clear();
	setChanged();
	notifyObservers();
    }
}

/**
 * Sets the <var>i</var>'th value. If <var>i</var> is out of range,
 * the list of values grows to fit.
 *
 * param i the index
 * @param newValue the new value
 */
public void setValue(int i, Object newValue) {
    // Make sure newValue is of proper type for the values we hold
    values.add(i, convertType(newValue));

    setChanged();
    notifyObservers();
}

/**
 * Converts the specified object to the proper type for this parameter.
 * Whenever we add or set a value or default value, we convert it to the
 * proper type (string, date, etc.)
 * <p>
 * If our type is boolean and the incoming object is:
 * <ul>
 * <li>A string, return
 * a <code>true Boolean</code> if the value matches "true", "t", "yes",
 * or "y" (ignoring case).
 * <li>A number, return a <code>true Boolean</code> if the value is non-zero.
 * <li>Anything else, return a <code>true Boolean</code> (any better
 * suggestions?)
 *
 * @param val any old object
 * @return some object of the proper type
 */
protected Object convertType(Object val) {
    if (val == null)
	return null;

    switch (type) {
    case TYPE_BOOLEAN:		// Return value as a boolean
	if (val instanceof Boolean)
	    return val;
	else if (val instanceof String) {
	    val = ((String)val).toLowerCase().trim();
	    if ("true".equals(val) || "t".equals(val)
		|| "yes".equals(val) || "y".equals(val))
		return Boolean.valueOf(true);
	    else
		return Boolean.valueOf(false);
	}
	else if (val instanceof Number) {
	    return Boolean.valueOf(((Number)val).doubleValue() == 0);
	}
	else {
	    return Boolean.valueOf(true); // What to do here?
	}
    case TYPE_STRING:		// Return value as a string
	return val.toString();
    case TYPE_NUMERIC:		// Return value as a number
	if (val instanceof Number)
	    return val;
	else {			// Convert val to string, then to number
	    String str = val.toString();
	    if (str.length() == 0)
		return new Integer(0);
	    else if (str.indexOf(".") == -1)
		return new Integer(str);
	    else
		return new Double(str);
	}
    case TYPE_DATE:		// Return value as a date
	if (val instanceof Date)
	    return val;
	else {			// Convert val to string, then to date
	    String str = val.toString();
	    if (str.length() == 0)
		return new Date();
	    else {
		parsePosition.setIndex(0);
		return formatter.parse(str, parsePosition);
	    }
	}
    default:			// Should never happen
	return null;
    }
}

/**
 * Returns the string used as the "type" attribute when writing this
 * parameter as XML.
 *
 * @return the "type" attribute string
 */
protected String typeString() {
    switch (type) {
    case TYPE_BOOLEAN: return "boolean";
    case TYPE_STRING: return "string";
    case TYPE_NUMERIC: return "numeric";
    case TYPE_DATE: return "date";
    default: return "unknown";	// Should never happen
    }
}

public String dragString() {
    return "parameter:" + getId();
}

public String designLabel() {
    return "{?" + getName() + "}";
}

public String formulaString() {
    return "{?" + getId() + "}";
}

/**
 * Writes this parameter as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    String arityString = null;
    switch (arity) {
    case ARITY_ONE: arityString = "single"; break;
    case ARITY_RANGE: arityString = "range"; break;
    case ARITY_LIST_SINGLE: arityString = "list-single"; break;
    case ARITY_LIST_MULTIPLE: arityString = "list-multiple"; break;
    }

    out.startElement("parameter");
    out.attr("id", id);
    out.attr("type", typeString());
    out.attr("name", name);
    out.attr("question", question);
    out.attr("arity", arityString);

    for (Iterator iter = defaultValues.iterator(); iter.hasNext(); )
	out.textElement("default", iter.next().toString());

    out.endElement();
}

public String toString() {
    StringWriter sw = new StringWriter();
    writeXML(new XMLWriter(sw));
    return sw.toString();
}

}
