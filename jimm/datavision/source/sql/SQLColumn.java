package jimm.datavision.source.sql;
import jimm.datavision.source.Table;
import jimm.datavision.source.Column;
import java.sql.*;

/**
 * A database column. It knows the table to which it belongs, its name,
 * and other metadata. The id of a column is a string of the form
 * "table_name.column_name".
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SQLColumn extends Column {

protected SQLTable table;

/**
 * Constructor.
 *
 * @param table the table to which this column belongs
 * @param colName the column name
 * @param type the data types
 * @see java.sql.DatabaseMetaData#getColumns
 */
public SQLColumn(SQLTable table, String colName, int type)
    throws SQLException
{
    super(table.getName() + "." + colName, colName, type);
    this.table = table;
}

/**
 * Returns the table to which this column belongs.
 *
 * @return the table
 */
public Table getTable() { return table; }

}
