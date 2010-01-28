package jimm.datavision.source.ncsql;
import jimm.datavision.source.Table;
import jimm.datavision.source.Column;

/**
 * A database column. It knows the table to which it belongs, its name,
 * and its type. The id of a column is a string of the form
 * "table_name.column_name".
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NCColumn extends Column {

protected NCTable table;

/**
 * Constructor.
 *
 * @param table the table to which this column belongs
 * @param colName the column's name
 * @param colType the column's type id
 */
public NCColumn(NCTable table, String colName, int colType) {
    super(table.getName() + '.' + colName, colName, colType);
    this.table = table;
}

/**
 * Returns the table to which this column belongs.
 *
 * @return the table
 */
public Table getTable() { return table; }

}
