package jimm.datavision;
import jimm.datavision.source.DataSource;
import jimm.datavision.source.Table;
import jimm.datavision.source.sql.SQLQuery;
import jimm.util.XMLWriter;

/**
 * A user column is an arbitrary expression inserted into the SQL query
 * and retrieved as a column value. It may contain database
 * column values, parameters, special values, strings, or
 * numbers. It can't contain formulas because their values may be
 * undefined when the query is run.
 * <p>
 * When used by a query, the following substitutions are made withing
 * the <var>userString</var> of a user column:
 * <ul>
 * <li>{<i>table_name.column_name</i>} is replaced by the column name
 * <i>table_name.column_name</i>.</li>
 * <li> {%<i>special_value_name</i>} is replaced by a special value
 * (report title, report run date, page number, or record number).</li>
 * <li> {?<i>id_number</i>} is replaced by a parameter value (string,
 * number, or date).</li>
 * <ul>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class UserColumn extends Expression implements Selectable {

public static final int MAX_DISPLAY_NAME_LENGTH = 16;

/**
 * Constructor.
 *
 * @param id the unique identifier for the new user column; if
 * <code>null</code>, generate a new id
 * @param report the report containing this user column
 * @param name the user column name
 */
public UserColumn(Long id, Report report, String name) {
    this(id, report, name, null);
}

/**
 * Constructor. If <i>id</i> is <code>null</code>, generates a new id number.
 * This number is one higher than any previously-seen id number. This does
 * <em>not</em> guarantee that no later user column will be created manually
 * with the same id number.
 *
 * @param id the unique identifier for the new user column; if
 * <code>null</code>, generate a new id
 * @param report the report containing this user column
 * @param name the user column name
 * @param evalString the string to evaulate at runtime.
 */
public UserColumn(Long id, Report report, String name, String evalString) {
    super(id == null ? report.generateNewUserColumnId() : id, report, name,
	  evalString, null);
}

public Object getValue(Report report) {
    return report.columnValue(this);
}

public String fieldTypeString() { return "usercol"; }

public String getSelectString(SQLQuery query) {
    String str = getExpression();
    if (str == null)
	return null;
    return query.prepare(str);
}

public String getSortString(SQLQuery query) {
    return getSelectString(query);
}

public Table getTable() { return null; }

public String getDisplayName() { return getName(); }

public String dragString() { return "usercol:" + getId(); }

public String designLabel() { return "{!" + getName() + "}"; }

public String formulaString() { return "{!" + getId() + "}"; }

public Selectable reloadInstance(DataSource dataSource) {
    return this;
}

/**
 * Returns this user column's SQL text as entered by the user.
 */
public String toString() {
    return getExpression();
}

public void writeXML(XMLWriter out) {
    writeXML(out, "usercol");
}

}
