package jimm.datavision.test.mock.source;
import jimm.datavision.Report;
import jimm.datavision.Selectable;
import jimm.datavision.source.*;
import jimm.util.XMLWriter;
import java.util.*;
import java.sql.Types;

public class MockDataSource extends DataSource {

protected static final String DATABASE_NAME = "dv_example";

protected Map tables;
protected List tablesUsedInReport;
protected List columns;
protected String name;

public MockDataSource(Report r) {
  super(r, new Query(r));
  tables = new HashMap();
  tablesUsedInReport = new ArrayList();
  name = DATABASE_NAME;

  createOfficeTable();
  createJobsTable();
  createAggregateTestTable();
  createAllCapsTable();
}

protected void createOfficeTable() {
  Table t = new Table(this, "office");
  tables.put(t.getName(), t);
  tablesUsedInReport.add(t);

  addColumn(t, "id", Types.INTEGER);
  addColumn(t, "name", Types.VARCHAR);
  addColumn(t, "abbrev", Types.VARCHAR);
  addColumn(t, "fax", Types.VARCHAR);
  addColumn(t, "email", Types.VARCHAR);
  addColumn(t, "visible", Types.BOOLEAN);
}

protected void createJobsTable() {
  Table t = new Table(this, "jobs");
  tables.put(t.getName(), t);
  tablesUsedInReport.add(t);
  addColumn(t, "ID", Types.INTEGER);
  addColumn(t, "title", Types.VARCHAR);
  addColumn(t, "fk_office_id", Types.INTEGER);
  addColumn(t, "company", Types.VARCHAR);
  addColumn(t, "location", Types.VARCHAR);
  addColumn(t, "description", Types.VARCHAR);
  addColumn(t, "visible", Types.BOOLEAN);
  addColumn(t, "post_date", Types.DATE);
  addColumn(t, "hourly rate", Types.INTEGER);
}

protected void createAggregateTestTable() {
  Table t = new Table(this, "aggregate_test");
  tables.put(t.getName(), t);
  addColumn(t, "col1", Types.VARCHAR);
  addColumn(t, "col2", Types.VARCHAR);
  addColumn(t, "col3", Types.VARCHAR);
  addColumn(t, "value", Types.INTEGER);
}

protected void createAllCapsTable() {
  Table t = new Table(this, "ALL_CAPS");
  tables.put(t.getName(), t);
  addColumn(t, "COL1", Types.INTEGER);
  addColumn(t, "COL2", Types.VARCHAR);
}

protected void addColumn(Table table, String name, int type) {
  Column col = new Column(table.getName() + '.' + name, name, type);
  table.addColumn(col);
}

public boolean canJoinTables() { return true; }

public boolean isSQLGenerated() { return true; }

public boolean isConnectionEditable() { return true; }

public boolean areRecordsSelectable() { return true; }

public boolean areRecordsSortable() { return true; }

public boolean canGroupRecords() { return true; }

public DataCursor execute() throws Exception {
  return new MockDataCursor(getQuery());
}

public int indexOfSelectable(Selectable sel) {
  return MockDataCursor.indexOfSelectable(sel);
}

/** Copied from {@link jimm.datavision.source.sql.Database}. */
public Column findColumn(Object id) {
    String str = id.toString();
    int pos = str.lastIndexOf('.');
    if (pos == -1) return null;
    String tableName = str.substring(0, pos);
    Table t = findTable(tableName);
    return t == null ? null : t.findColumn(id);
}

/**
 * Copied from {@link jimm.datavision.source.sql.Database} and tweaked a
 * bit.
 */
protected Table findTable(String tableName) {
    // First try a simple exact match using tables.
    Table t = (Table)tables.get(tableName);
    if (t != null)
	return t;

    String schemaName = null;
    int pos = tableName.indexOf('.');
    if (pos >= 0) {
	schemaName = tableName.substring(0, pos);
	tableName = tableName.substring(pos + 1);
    }

    if (!getReport().caseSensitiveDatabaseNames()) {
	if (schemaName != null) schemaName = schemaName.toLowerCase();
	tableName = tableName.toLowerCase();
    }

    // First try with table's schema name, if any.
    if (schemaName != null) {
	if ((t = findTableWithId(schemaName + '.' + tableName)) != null)
	    return t;
    }

    // Now try with database's schema name if it's different from the
    // table's schema name.
    if (name != null && !name.equals(schemaName)) {
	if ((t = findTableWithId(name + '.' + tableName)) != null)
	    return t;
    }

    // Finally, try with no schema name.
    if ((t = findTableWithId(tableName)) != null)
	return t;

    return null;
}

/**
 * Copied from {@link jimm.datavision.source.sql.Database} and tweaked a
 * bit.
 */
protected Table findTableWithId(String id) {
    boolean caseSensitive = getReport().caseSensitiveDatabaseNames();
    if (!caseSensitive)
	id = id.toLowerCase();

    for (Iterator iter = tables.keySet().iterator(); iter.hasNext(); ) {
	String key = (String)iter.next();
	if (caseSensitive)
	    key = key.toLowerCase();
	if (key.equals(id))
	    return (Table)tables.get(key);
    }

    return null;
}

public Iterator tables() { return tables.values().iterator(); }

public Iterator tablesUsedInReport() { return tablesUsedInReport.iterator(); }

public Iterator columns() {
  return new ColumnIterator(tables.values().iterator());
}

protected void doWriteXML(XMLWriter out) { }

}
