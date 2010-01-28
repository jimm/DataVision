package jimm.datavision.field;
import jimm.datavision.Report;
import jimm.datavision.Section;
import jimm.datavision.source.Column;
import jimm.util.I18N;

/**
 * A column field represents a data source column. The value of a column field
 * holds the {@link Column} object.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ColumnField extends Field {

protected Column column;

/**
 * Constructs a column field with the specified id in the specified report
 * section whose database {@link Column}'s id is <var>value</var>.
 *
 * @param id the new field's id
 * @param report the report containing this element
 * @param section the report section in which the field resides
 * @param value the string id of a database column
 * @param visible show/hide flag
 */
public ColumnField(Long id, Report report, Section section, Object value,
		   boolean visible)
{
    super(id, report, section, value, visible);
    column = report.findColumn((String)value);
    if (column == null) {
	String errorMsg = I18N.get("UnknownColumn.the_column")
	    + ' ' + (String)value + ' '
	    + I18N.get("UnknownColumn.column_unknown");
	throw new IllegalArgumentException(errorMsg);
    }
}

public String dragString() {
    return typeString() + ":" + column.getId();
}

/**
 * Returns the database column.
 *
 * @return the database column
 */
public Column getColumn() { return column; }

/**
 * Sets the database column.
 *
 * @param newColumn the new database column
 */
public void setColumn(Column newColumn) {
    if (column != newColumn) {
	column = newColumn;
	setChanged();
	notifyObservers();
    }
}

public String typeString() { return "column"; }

public String formulaString() { return "{" + value + "}"; }

/**
 * Returns the value of this field. For column fields, this is the current
 * value of the database column.
 *
 * @return the value string
 */
public Object getValue() { return getReport().columnValue(column); }

/**
 * This override returns <code>true</code> if this column is in a detail
 * section and holds a numeric type.
 *
 * @return <code>true</code> if this field can be aggregated
 */
public boolean canBeAggregated() {
    // Section can be null during dragging.
    return section != null && section.isDetail() && getColumn().isNumeric();
}

}
