package jimm.datavision.field;
import jimm.datavision.*;
import java.util.*;

interface AggregateFunction {
public double aggregate(double[] values, int numValues);
}

/**
 * An aggregate field represents a field's aggregated values, either {@link
 * ColumnField} or {@link FormulaField}. It also may be associated with a
 * group (assigned in {@link ReportReader#field} or when editing a report),
 * meaning that the aggregate value is reset whenever the group's value
 * changes. The value of an aggregate field holds the id of some other field
 * whose value we are aggregating.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class AggregateField extends Field {

protected static final int START_VALUES_LENGTH = 100;

/** Maps function names to {@link AggregateFunction} objects. */
protected static HashMap functions;
/** A sorted array of the function names. */
protected static Object[] functionNames;

// Initialize map from function names to functions.
static {
    functions = new HashMap();
    functions.put("sum", new AggregateFunction() {
	public double aggregate(double[] values, int numValues) {
	    double total = 0;
	    for (int i = 0; i < numValues; ++i) total += values[i];
	    return total;
	}
	});
    functions.put("subtotal", functions.get("sum")); // Old name for "sum"
    functions.put("min", new AggregateFunction() {
	public double aggregate(double[] values, int numValues) {
	    double min = Double.MAX_VALUE;
	    for (int i = 0; i < numValues; ++i)
		if (values[i] < min) min = values[i];
	    return min;
	}
	});
    functions.put("max", new AggregateFunction() {
	public double aggregate(double[] values, int numValues) {
	    double max = Double.MIN_VALUE;
	    for (int i = 0; i < numValues; ++i)
		if (values[i] > max) max = values[i];
	    return max;
	}
	});
    functions.put("count", new AggregateFunction() {
	public double aggregate(double[] values, int numValues) {
	    return numValues;
	}
	});
    functions.put("average", new AggregateFunction() {
	public double aggregate(double[] values, int numValues) {
	    if (numValues == 0)
		return 0;
	    double total = 0;
	    for (int i = 0; i < numValues; ++i) total += values[i];
	    return total / numValues;
	}
	});
    functions.put("stddev", new AggregateFunction() {
	public double aggregate(double[] values, int numValues) {
	    if (numValues < 2)
		return 0;
	    double average = ((AggregateFunction)functions.get("average"))
		.aggregate(values, numValues);
	    double sumOfSquares = 0;
	    for (int i = 0; i < numValues; ++i)
		sumOfSquares += (values[i] - average) * (values[i] - average);
	    return Math.sqrt(sumOfSquares / (numValues - 1));
	}
	});

    // Create a sorted list of function names. Don't include "select", which
    // is the old name for "sum".
    TreeSet withoutSelect = new TreeSet(functions.keySet());
    withoutSelect.remove("select");
    functionNames = withoutSelect.toArray();
}

protected Group group;		// Set by report creation; possibly null
protected String functionName;
protected AggregateFunction function;
protected double[] values;	// Read-only
protected int valuesIndex;
protected Field fieldToAggregate;

/**
 * Returns <code>true</code> if <var>functionName</var> is a legal aggregate
 * function name.
 * 
 * @param functionName an aggregate function name (hopefully)
 * @return <code>true</code> if it's a function name
 */
public static boolean isAggregateFunctionName(String functionName) {
    return functions.keySet().contains(functionName);
}

/**
 * Returns the list of function names as an array of objects.
 *
 * @return all possible function names
 */
public static Object[] functionNameArray() {
    return functionNames;
}

/**
 * Constructs a field with the specified id in the specified report
 * section that aggregates the field with id <i>value</i>.
 *
 * @param id the new field's id
 * @param report the report containing this element
 * @param section the report section in which the field resides
 * @param value the magic string
 * @param visible show/hide flag
 * @param functionName "xxx", where xxx is the aggregate function
 */
public AggregateField(Long id, Report report, Section section, Object value,
		     boolean visible, String functionName)
{
    super(id, report, section, value, visible);
    values = null;

    setFunction(functionName);

    // The reason I don't grab fieldToAggregate right now is that this
    // aggregate field may be constructed before the field to which it
    // refers.
}

protected void finalize() throws Throwable {
    if (fieldToAggregate != null)
	fieldToAggregate.deleteObserver(this);
    super.finalize();
}

public String getFunction() { return functionName; }
public void setFunction(String newFunctionName) {
    newFunctionName = newFunctionName.toLowerCase();
    if (functionName != newFunctionName &&
	(functionName == null || !functionName.equals(newFunctionName)))
    {
	functionName = newFunctionName;
	function = (AggregateFunction)functions.get(functionName);
	setChanged();
	notifyObservers();
    }
}

/**
 * Resets this aggregate. Called by the report once at the beginning of
 * each run.
 */
public void initialize() {
    values = null;
}

public String dragString() {
    return typeString() + ":" + getField().getId();
}

/**
 * Returns the group over which this field is aggregating. May return
 * <code>null</code> since not all aggregate fields are associated with
 * a group.
 *
 * @return a group; <code>null</code> if this aggregate field is not
 * associated with any group
 */
public Group getGroup() { return group; }

/**
 * Sets the group this field is aggregate. The group may be
 * <code>null</code>.
 *
 * @param newGroup a group
 */
public void setGroup(Group newGroup) { 
    if (group != newGroup) {
	group = newGroup;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the field over which we are aggregating. We lazily instantiate
 * it because when we are constructed that field may not yet exist. We also
 * start observing the field so we can notify our observers in turn.
 *
 * @return the field we are aggregating
 */
public Field getField() {
    if (fieldToAggregate == null) {
	fieldToAggregate = getReport().findField(value);
	fieldToAggregate.addObserver(this);
    }
    return fieldToAggregate;
}

/**
 * Returns the id of the field over which we are aggregating. The field
 * itself may not yet exist.
 *
 * @return the id of the field we are aggregating
 */
public Long getFieldId() {
    return (value instanceof Long) ? (Long)value : new Long(value.toString());
}

/**
 * Returns the current aggregate value.
 *
 * @return a doubleing point total
 */
public double getAggregateValue() {
    if (function == null)
	return 0;
    return function.aggregate(values, valuesIndex);
}

public String typeString() { return functionName; }

public String designLabel() {
    return functionName + "(" + getField().designLabel() + ")";
}

public String formulaString() { return designLabel(); }

public boolean refersTo(Field f) {
    return getField() == f;
}

public boolean refersTo(Formula f) {
    return (getField() instanceof FormulaField)
	&& ((FormulaField)getField()).getFormula() == f;
}

public boolean refersTo(UserColumn uc) {
    return (getField() instanceof UserColumnField)
	&& ((UserColumnField)getField()).getUserColumn() == uc;
}

public boolean refersTo(Parameter p) {
    if ((getField() instanceof ParameterField)
	&& ((ParameterField)getField()).getParameter() == p)
	return true;

    if ((getField() instanceof FormulaField)
	&& ((FormulaField)getField()).refersTo(p))
	return true;

    return false;
}

public boolean canBeAggregated() {
    return true;
}

/**
 * Updates the aggregate value. Called by the report when a new
 * line of data is retrieved.
 */
public void updateAggregate() {
    /*
     * Our value field holds the id of some other field. Get that field's
     * value, then convert it to a double.
     */
    Object obj = getField().getValue();
    double value = 0;
    if (obj != null) {
	if (obj instanceof Number)
	    value = ((Number)obj).doubleValue();
	else
	    value = Double.parseDouble(obj.toString());
    }

    // If we are aggregating within a group and this is a new value,
    // reset the aggregate value. If we have not yet collected any
    // values, allocate space.
    if (values == null || (group != null && group.isNewValue())) {
	values = new double[START_VALUES_LENGTH];
	valuesIndex = 0;
    }
    else if (valuesIndex == values.length) { // Expand the values array
	double[] newValues = new double[values.length * 2];
	System.arraycopy(values, 0, newValues, 0, values.length);
	values = newValues;
    }
    values[valuesIndex++] = value;
}

/**
 * Returns the value of this field: the aggregate as a Double.
 *
 * @return a Double
 */
public Object getValue() { return new Double(getAggregateValue()); }

}
