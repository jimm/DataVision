package jimm.datavision.source.object;
import jimm.datavision.*;
import jimm.datavision.source.*;
import jimm.util.XMLWriter;
import java.io.*;
import java.util.*;

/**
 * Provides the classes and interfaces that make up the a data source that
 * uses an ArrayList of ArrayLists representing a table of data.
 * See the ObjectSourceTest.java in the examples directory for usage example.
 *
 * @author Frank W. Zammetti, <a href="mailto:fzammetti@omnytex.com">fzammetti@omnytex.com</a>
 */
public class ObjectSource extends DataSource {

protected ArrayList columns;
private ArrayList data;

/**
 * Constructor.
 *
 * @param report The Report object that uses this data source.
 * @param inData An ArrayList of ArrayLists that is the data this data source
 *               is to use.
 */
public ObjectSource(Report report, ArrayList inData) {
    super(report, new ObjectQuery(report));
    columns = new ArrayList();
    data = inData;
}

public boolean canJoinTables() { return false; }
public boolean isSQLGenerated() { return false; }
public boolean isConnectionEditable() { return false; }
public boolean areRecordsSelectable() { return false; }
public boolean areRecordsSortable() { return false; }
public boolean canGroupRecords() { return false; }
public boolean usesSourceFile() { return false; }

/**
 * This override not only remembers the column but also hands it to the
 * query for cacheing.
 *
 * @param col a column
 */
public void addColumn(Column col) {
    columns.add(col);
    ((ObjectQuery)query).addColumn(col);
}

/**
 * Return the ArrayList of data this data source contains.
 *
 * @return The ArrayList of data passed to the data source during construction.
 */
public ArrayList getData() {
  return data;
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
    for (Iterator iter = columns.iterator(); iter.hasNext(); ) {
	Column col = (Column)iter.next();
	if (col.getId().equals(id))
	    return col;
    }
    return null;
}

public int indexOfSelectable(Selectable sel) { return columns.indexOf(sel); }

public Iterator tables() { return null; }

public Iterator tablesUsedInReport() { return null; }

public Iterator columns() { return columns.iterator(); }

public DataCursor execute() {
    return new ObjectRow(this, query);
}

/**
 * Writes this database and all its tables as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
protected void doWriteXML(XMLWriter out) {
    out.startElement("object");
    if (metadataURL != null)
	out.textElement("metadata-url", metadataURL);
    else
	for (Iterator iter = columns.iterator(); iter.hasNext(); )
	    ((Column)iter.next()).writeXML(out);
    out.endElement();
}

}
