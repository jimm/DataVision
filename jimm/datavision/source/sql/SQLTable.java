package jimm.datavision.source.sql;
import jimm.datavision.source.Table;
import jimm.datavision.source.Column;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.*;

/**
 * Represents a database table.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SQLTable extends Table {

protected DatabaseMetaData dbmd;
    protected HashMap colCacheMap;

/**
 * Constructor.
 *
 * @param database the database in which this table resides
 * @param name the table's name
 * @param dbmd database metadata information
 */
public SQLTable(Database database, String name, DatabaseMetaData dbmd) {
    super(database, name);
    this.dbmd = dbmd;
}

public Column findColumn(Object colIdObj) {
    if (dbmd != null) loadColumns();

    String colId = colIdObj.toString();

    // First try a simple exact match using colCacheMap. This will often
    // fail the first time, but after we have found a column using the quite
    // convoluted search below we store the column in colCacheMap.
    Column col = (Column)colCacheMap.get(colId);
    if (col != null)
	return col;

    boolean caseSensitive =
	dataSource.getReport().caseSensitiveDatabaseNames();

    String schemaName = null;
    int pos = colId.indexOf('.');
    if (pos >= 0) {
	schemaName = colId.substring(0, pos);
	colId = colId.substring(pos + 1);
    }

    if (!caseSensitive) {
	if (schemaName != null) schemaName = schemaName.toLowerCase();
	colId = colId.toLowerCase();
    }

    // First try with table's schema name, if any.
    if (schemaName != null) {
	String target = schemaName + '.' + colId;
	for (Iterator iter = columns.keySet().iterator(); iter.hasNext(); ) {
	    String key = (String)iter.next();
	    if (caseSensitive) {
		if (key.equals(target)) {
		    col = (Column)columns.get(key);
		    colCacheMap.put(colId, col); // Store in cache
		    return col;
		}
	    }
	    else {
		if (key.toLowerCase().equals(target.toLowerCase())) {
		    col = (Column)columns.get(key);
		    colCacheMap.put(colId, col); // Store in cache
		    return col;
		}
	    }
	}
    }

    // Now try with database's schema name if it's different from the
    // table's schema name.
    if (name != null && !name.equals(schemaName)) {
	String target = name + '.' + colId;
	for (Iterator iter = columns.keySet().iterator(); iter.hasNext(); ) {
	    String key = (String)iter.next();
	    if (caseSensitive) {
		if (key.equals(target)) {
		    col = (Column)columns.get(key);
		    colCacheMap.put(colId, col); // Store in cache
		    return col;
		}
	    }
	    else {
		if (key.toLowerCase().equals(target.toLowerCase())) {
		    col = (Column)columns.get(key);
		    colCacheMap.put(colId, col); // Store in cache
		    return col;
		}
	    }
	}
    }

    // Finally, try with no schema name.
    String target = colId;
    for (Iterator iter = columns.keySet().iterator(); iter.hasNext(); ) {
	String key = (String)iter.next();
	if (caseSensitive) {
	    if (key.equals(target))
		return (Column)columns.get(key);
	}
	else {
	    if (key.toLowerCase().equals(target.toLowerCase()))
		return (Column)columns.get(key);
	}
    }

    return null;
}

public Iterator columns() {
    if (dbmd != null) loadColumns();
    return super.columns();
}

protected void loadColumns() {
    colCacheMap = new HashMap();

    String schemaName = null;
    String tableName = name;
    int pos = name.indexOf('.');
    if (pos >= 0) {
	schemaName = name.substring(0, pos);
	tableName = name.substring(pos + 1);
    }

    loadColumnsUsing(((Database)dataSource).getName(), tableName);
    if (schemaName != null && columns.isEmpty())
	loadColumnsUsing(schemaName, tableName);
    if (columns.isEmpty())
	loadColumnsUsing(null, tableName);

    dbmd = null;
}

protected void loadColumnsUsing(String schemaName, String tableName) {
    ResultSet rset = null;
    String getColumnsAttempt = "%";
    try {
	// The last two args should be able to be null, but some users have
	// reported that their drivers barf unless I use the "%" wildcard.
	// (That's ancient history; I'm not sure if it is still true.
	// However, rough timing indicates that there's not much of a
	// performance difference in either case.)
	// FWZ (1/23/2008): A bug was discovered by Byron Hurder WRT SQLite.  It
	// turns out that "%" causes no columns to show up, null instead has to be
	// used.  In order to maintain the current functionality but fix SQLite,
	// I added the code here to try with "%" first, and then null if the
	// ResultSet is empty.  If it's still empty at that point, so be it, that
	// SHOULD mean there's really no columns.  Note any exceptions will be
	// eaten, but I added logging so at least it's reported.
	rset = dbmd.getColumns(null, schemaName, tableName, "%");
	if (!rset.next()) {
	    rset.close();
	    rset = null;
	    getColumnsAttempt = "null";
	    rset = dbmd.getColumns(null, schemaName, tableName, null);
	} else {
	    try {
		rset.beforeFirst();	// Undo side-effect of rset.next in if test
	    }
	    catch (SQLException sqle) {	// Regenerate the result set instead
		rset = dbmd.getColumns(null, schemaName, tableName, "%");
	    }
	}

	while (rset.next()) {
	    SQLColumn col = new SQLColumn(this,
					  rset.getString("COLUMN_NAME").trim(),
					  rset.getInt("DATA_TYPE"));
	    addColumn(col);
	}
    }
    catch (SQLException e) {
      System.out.println("Exception attempting to load columns for table " +
        tableName + " in schema " + schemaName + " using " + getColumnsAttempt +
        " as third parameter to getColumns() call.  Stack trace follows: ");
      e.printStackTrace();
    }
    finally {
	try { if (rset != null) rset.close(); }
	catch (SQLException e2) {}
    }
}

}
