package jimm.datavision.source.ncsql;
import jimm.datavision.*;
import jimm.datavision.source.*;
import jimm.util.XMLWriter;
import java.util.*;

/**
 * An <code>NCDatabase</code> a data source that acts like a SQL
 * database data source but can't run reports. It gets its column
 * descriptions from metadata described in the report XML file.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NCDatabase extends DataSource {

protected static final String ORPHANS_TABLE = "no_table";

protected TreeMap tables;

public NCDatabase(Report report) {
    super(report, new NCQuery(report));
    tables = new TreeMap();
}

public boolean canRunReports() { return false; }
public boolean canJoinTables() { return true; }
public boolean isSQLGenerated() { return true; }
public boolean isConnectionEditable() { return false; }
public boolean areRecordsSelectable() { return true; }
public boolean areRecordsSortable() { return true; }
public boolean canGroupRecords() { return true; }

/**
 * This override not only remembers the column but also hands it to the
 * query for cacheing.
 *
 * @param col a column
 */
public void addColumn(Column col) {
    // We need to turn the column into a NCColumn and perhaps add an
    // NCTable, too.
    
    // Parse the column's full name, looking for the table name and column
    // name.
    String fullName = col.fullName();
    int lastDotPos = fullName.lastIndexOf('.');
    String tableName = null;
    String colName = null;
    if (lastDotPos == -1) {
	tableName = ORPHANS_TABLE;
	colName = fullName;
    }
    else {
	tableName = fullName.substring(0, lastDotPos);
	colName = fullName.substring(lastDotPos + 1);
    }

    // Find the table. Create one if we don't already have one.
    NCTable table = (NCTable)tables.get(tableName);
    if (table == null) {
	table = new NCTable(this, tableName);
	tables.put(tableName, table);
    }

    table.addColumn(new NCColumn(table, colName, col.getType()));
}

/**
 * Given an id (a column name), returns the column that has that id. If no
 * column with the specified id exists, returns <code>null</code>. Uses
 * <code>Table.findColumn</code>.
 *
 * @param id a column id
 * @return a column, or <code>null</code> if no column with the specified
 * id exists
 * @see Table#findColumn
 */
public Column findColumn(Object id) {
    if (tables == null)
	return null;

    for (Iterator iter = tables.values().iterator(); iter.hasNext(); ) {
	Column col = ((Table)iter.next()).findColumn(id);
	if (col != null)
	    return col;
    }
    return null;
}

public Iterator tables() {
    return tables.values().iterator();
}

public Iterator tablesUsedInReport() {
    return ((NCQuery)query).getTablesUsed().iterator();
}

public Iterator columns() {
    return new ColumnIterator(tables.values().iterator());
}

public DataCursor execute() { return null; }

/**
 * Writes this database and all its tables as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
protected void doWriteXML(XMLWriter out) {
    out.startElement("nc-database");
    if (metadataURL != null)
	out.textElement("metadata-url", metadataURL);
    else
	for (Iterator iter = columns(); iter.hasNext(); )
	    ((Column)iter.next()).writeXML(out);
    out.endElement();
}

}
