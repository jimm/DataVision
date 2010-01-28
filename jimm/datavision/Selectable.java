package jimm.datavision;
import jimm.datavision.source.DataSource;
import jimm.datavision.source.Table;
import jimm.datavision.source.sql.SQLQuery;

/**
 * The <code>Selectable</code> interface represents things that can be
 * selected, grouped, and sorted. This includes data columns and user
 * columns.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see Group
 * @see jimm.datavision.source.Query
 * @see jimm.datavision.source.DataSource
 */
public interface Selectable {

/**
 * Returns the id of the selectable object.
 *
 * @return the id of the selectable object
 */
public Object getId();

/**
 * Returns the current value. May only be valid during a report run.
 *
 * @param r a report
 * @return the current value
 */
public Object getValue(Report r);

/**
 * Returns the string used by a SQL query to select this object.
 */
public String getSelectString(SQLQuery q);

/**
 * Returns the string used as the name/value of this selectable in a SQL
 * ORDER BY clause. This may be the same as the select string returned
 * by <code>getSelectString</code>.
 *
 * @return a string used when creating the ORDER BY clause
 * @see #getSelectString
 */
public String getSortString(SQLQuery q);

/**
 * Returns the table to which this selectable belongs; may be
 * <code>null</code>.
 *
 * @return the table to which this selectable belongs; may be
 * <code>null</code>
 */   
public Table getTable();

/**
 * Returns the string used to create a field of the appropriate type.
 *
 * @return a string useable by <code>Field.create</code>
 * @see jimm.datavision.field.Field#create
 */
public String fieldTypeString();

/**
 * Returns a (possibly new) instance of this selectable object. Used when
 * we are reestablishing or resetting a connection to a database. The
 * instance returned may or may not be the same object as this one.
 */
public Selectable reloadInstance(DataSource dataSource);

public String getDisplayName();

}
