package jimm.datavision.source;
import jimm.datavision.*;
import jimm.datavision.source.sql.SQLQuery;
import jimm.util.XMLWriter;
import java.sql.Types;

/**
 * Represents a data column. Not all data sources' columns  will be
 * contained within tables. For those that don't, their columns'
 * <code>getTable</code> method will return <code>null</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see Table
 * @see DataSource
 */
public class Column
    implements Identity, Nameable, Selectable, Draggable, Writeable
{

public static final String DEFAULT_DATE_PARSE_FORMAT = "yyyy-MM-dd";

protected Object id;
protected String name;
protected int type;
protected String dateParseFormat;

public static int typeFromString(String str) {
    if (str == null || str.length() == 0)
	return Types.VARCHAR;

    str = str.toLowerCase();
    if (str.equals("number"))
	return Types.NUMERIC;
    if (str.equals("date"))
	return Types.DATE;
    return Types.VARCHAR;
}

public static String typeToString(int type) {
    switch (type) {
    case Types.BIGINT:
    case Types.BIT:
    case Types.DECIMAL:
    case Types.DOUBLE:
    case Types.INTEGER:
    case Types.NUMERIC:
    case Types.REAL:
    case Types.SMALLINT:
    case Types.TINYINT:
	return "number";
    case Types.DATE:
    case Types.TIME:
    case Types.TIMESTAMP:
	return "date";
    default:
	return "string";
    }
}

public Column(Object id, String name, int type) {
    this.id = id;
    this.name = name;
    this.type = type;
}

/**
 * Returns <code>true</code> if the other object is a column with the
 * same id.
 *
 * @param obj any <code>Object</code>
 * @return <code>true</code> if the other object is a column with the
 * same id.
 */
public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Column)) return false;
    if (this == obj) return true;
    return id.equals(((Column)obj).getId());
}

public int hashCode() {
    return id.hashCode();
}

/**
 * Returns the table id. This is a string of the form
 * "table_name.column_name".
 *
 * @return the id string
 */
public Object getId() { return id; }

/**
 * Returns the column's name. The name may not be unique. To retrieve
 * a unique name (for example, "table.column"), use <code>fullName</code>.
 *
 * @return the column's name
 * @see #fullName
 */
public String getName() { return name; }

/** A column's name is immutable. */
public void setName(String name) { }

/**
 * Returns the date parse format, useful for data sources that read text
 * strings and convert them into date objects. If no format has been
 * defined, returns <code>DEFAULT_DATE_PARSE_FORMAT</code>
 *
 * @return the date parse format string (if not defined, returns
 * <code>DEFAULT_DATE_PARSE_FORMAT</code>)
 */
public String getDateParseFormat() {
    return dateParseFormat == null
	? DEFAULT_DATE_PARSE_FORMAT : dateParseFormat;
}

/**
 * Sets the date parse format, useful for data sources that read text
 * strings and convert them into date objects. Called from
 * <code>ReportReader.column</code>, for example.
 *
 * @param format the date parse format string
 */
public void setDateParseFormat(String format) { dateParseFormat = format; }

/**
 * Returns the table to which this column belongs, if any.
 *
 * @return the table, or <code>null</code> if there is none
 */
public Table getTable() { return null; }

/**
 * Returns the full named of this column: the id as a string. The column's
 * name may not be unique, but the full name should be unique.
 * <p>
 * For SQL columns, this is a string of the form "table_name.column_name".
 * To retrieve just the column name, use <code>getName</code>.
 *
 * @return the full name; this is the same as the id as a string
 * @see #getName
 */
public String fullName() {
    return id.toString();
}

/**
 * Returns the type constant (a <code>java.sql.Types</code> value).
 *
 * @return a <code>java.sql.Types</code> value
 * @see java.sql.Types
 */
public int getType() { return type; }

public Object getValue(Report report) {
    return report.columnValue(this);
}

public String fieldTypeString() { return "column"; }

public String getSelectString(SQLQuery query) {
    return query.quoted(fullName());
}

public String getSortString(SQLQuery query) {
    return getSelectString(query);
}

public String dragString() {
    return "column:" + fullName();
}

public String getDisplayName() { return fullName(); }

public Selectable reloadInstance(DataSource dataSource) {
    return dataSource.findColumn(getId());
}

/**
 * Returns <code>true</code> if this column is some numeric type
 * (double, int, etc.)
 *
 * @return <code>true</code> if this column is some numeric type
 * (double, int, etc.)
 */
public boolean isNumeric() {
    return type == Types.BIGINT
	|| type == Types.BIT
	|| type == Types.DECIMAL
	|| type == Types.DOUBLE
	|| type == Types.FLOAT
	|| type == Types.INTEGER
	|| type == Types.NUMERIC
	|| type == Types.REAL
	|| type == Types.SMALLINT
	|| type == Types.TINYINT;
}

/**
 * Returns <code>true</code> if this column is some date type.
 *
 * @return <code>true</code> if this column is some date type
 */
public boolean isDate() {
    return type == Types.DATE
	|| type == Types.TIME
	|| type == Types.TIMESTAMP;
}

/**
 * Returns <code>true</code> if this column is some character type.
 *
 * @return <code>true</code> if this column is some character type
 */
public boolean isString() {
    return !isNumeric() && !isDate();
}

/**
 * Returns a string representation of this column. Calls
 * <code>fullName</code>.
 *
 * @see #fullName
 */
public String toString() {
    return fullName();
}

public void writeXML(XMLWriter out) {
    out.startElement("column");
    out.attr("name", getName());
    out.attr("type", typeToString(type));
    if (dateParseFormat != null)
	out.attr("date-format", dateParseFormat);
    out.endElement();
}

}
