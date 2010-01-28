package jimm.datavision.source;
import jimm.datavision.Identity;
import jimm.datavision.Nameable;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * Represents a table that contains columns. Not all data sources will
 * use tables. For those that don't, their columns' <code>getTable</code>
 * method will return <code>null</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see Column
 * @see DataSource
 */
public class Table implements Identity, Nameable {

protected DataSource dataSource;
protected String name;
protected TreeMap columns;

/**
 * Given a column id, returns the column that has that id. If no column
 * with the specified id exists, returns <code>null</code>.
 *
 * @return a column, or <code>null</code> if no column with the specified
 * id exists
 */
public Column findColumn(Object id) {
    if (dataSource.getReport().caseSensitiveDatabaseNames())
	return (Column)columns.get(id.toString());

    // Case-insensitive match
    String target = id.toString().toLowerCase();
    for (Iterator iter = columns.keySet().iterator(); iter.hasNext(); ) {
	String key = (String)iter.next();
	if (target.equals(key.toLowerCase()))
	    return (Column)columns.get(key);
    }
    return null;
}

/**
 * Constructor.
 *
 * @param dataSource the data source in which this table resides
 * @param name the table's name
 */
public Table(DataSource dataSource, String name) {
    this.dataSource = dataSource;
    this.name = name;
    columns = new TreeMap();
}

/**
 * Returns the table id. By default, it's the same as the table name.
 *
 * @return the table id
 */
public Object getId() { return name; }

/**
 * Returns the table name.
 *
 * @return the table name
 */
public String getName() { return name; }

/** A table's name is immutable. */
public void setName(String name) { }

/**
 * Adds a column to the collection, using the column's id as the key.
 *
 * @param col a column
 */
public void addColumn(Column col) {
    columns.put(col.getId().toString(), col);
}

/**
 * Returns an iterator over the columns in this table.
 *
 * @return an iterator over the columns in this table
 */
public Iterator columns() { return columns.values().iterator(); }

}
