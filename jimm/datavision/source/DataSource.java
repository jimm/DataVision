package jimm.datavision.source;
import jimm.datavision.*;
import jimm.util.XMLWriter;
import jimm.util.I18N;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.ArrayList;
import org.xml.sax.InputSource;

/**
 * An abstract data source.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see Column
 * @see Table
 * @see Query
 */
public abstract class DataSource implements Writeable {

protected Report report;
protected Query query;
protected String metadataURL;

public DataSource(Report r, Query q) {
    report = r;
    query = q;
}

/**
 * Used to enable/disable the &quot;Table Linker&quot; menu item.
 *
 * @return <code>true</code> if the &quot;Table Linker&quot; menu item
 * should be enabled.
 */
public abstract boolean canJoinTables();

/**
 * Used to enable/disable the &quot;SQL Query Text&quot; menu item.
 *
 * @return <code>true</code> if the &quot;SQL Query Text&quot; menu item
 * should be enabled.
 */
public abstract boolean isSQLGenerated();

/**
 * Used to enable/disable the &quot;Connection&quot; menu item.
 *
 * @return <code>true</code> if the &quot;Connection&quot; menu item
 * should be enabled.
 */
public abstract boolean isConnectionEditable();

/**
 * Used to enable/disable the &quot;Select Records&quot; menu item.
 *
 * @return <code>true</code> if the &quot;Select Records&quot; menu item
 * should be enabled.
 */
public abstract boolean areRecordsSelectable();

/**
 * Used to enable/disable the &quot;Sort By&quot; menu item.
 *
 * @return <code>true</code> if the &quot;Sort By&quot; menu item
 * should be enabled.
 */
public abstract boolean areRecordsSortable();

/**
 * Used to enable/disable the &quot;Group By&quot; menu item.
 *
 * @return <code>true</code> if the &quot;Group By&quot; menu item
 * should be enabled.
 */
public abstract boolean canGroupRecords();

/**
 * Used to enable/disable the &quot;Run&quot; and &quot;Export&quot; menu
 * items. Most data sources will enable these, of course.
 *
 * @return <code>true</code> if the &quot;Run&quot; and &quot;Export&quot;
 * menu items should be enabled.
 */
public boolean canRunReports() { return true; }

/**
 * Returns <code>true</code> if this data source uses a file to retrieve
 * data. The default implementation returns false.
 *
 * @return <code>true</code> if this data source uses a file to retrieve
 * data
 */
public boolean usesSourceFile() { return false; }

/**
 * Returns <code>true</code> if this data source uses a file to retrieve
 * data and does not yet have one. The default implementation returns false.
 *
 * @return <code>true</code> if this data source uses a file to retrieve
 * data and doesn't yet have one
 */
public boolean needsSourceFile() { return false; }

/**
 * Returns <code>true</code> if this data source uses a file to retrieve
 * data and and has already done so. The default implementation returns false.
 *
 * @return <code>true</code> if this data source uses a file to retrieve
 * data
 */
public boolean alreadyUsedSourceFile() { return false; }

/**
 * Accepts the path to a data source file. The default implementation does
 * nothing.
 *
 * @param filePath the full path to a file
 */
public void setSourceFile(String filePath) throws FileNotFoundException { }

/**
 * Gets the path to a data source file. The default implementation returns
 * null.
 *
 * @return The full path to a file
 */
public String getSourceFile() { return null; }

/**
 * Tells this data source to re-use (perhaps re-open) the current data
 * source file. The default implementation does nothing.
 */
public void reuseSourceFile() throws FileNotFoundException { }

public Report getReport() { return report; }

public Query getQuery() { return query; }

public abstract DataCursor execute() throws Exception;

/**
 * Called from <code>ReportReader.column</code> to add a column to a
 * data source.
 * <p>
 * The default implementation does nothing.
 *
 * @param col a column
 */
public void addColumn(Column col) {}

/**
 * Called from <code>Report.reloadColumns/code>, this method gives the
 * data source a chance to tell its ancillary objects (such as the query)
 * to reload column objects.
 * <p>
 * This is necessary, for example, after a SQL database data source has
 * reloaded all of its table and column information. The old column
 * objects no longer exist. New ones (with the same ids, we assume) have
 * taken their place.
 */
public void reloadColumns() {
    if (query != null)
	query.reloadColumns(this);
}

/**
 * Reads metadata from a URL. We save the URL string so we can write it
 * back out when we write to XML.
 *
 * @param urlString where to get the metadata
 * @see MetadataReader
 */
public void readMetadataFrom(String urlString) throws Exception {
    try {
	metadataURL = urlString;
	new MetadataReader(this).read(new InputSource(urlString));
    }
    catch (IOException ioe) {
	ErrorHandler.error(I18N.get("DataSource.metadata_err"), ioe,
			   I18N.get("DataSource.metadata_err_title"));
    }
}

/**
 * Given an id, returns the column that has that id. If no column with the
 * specified id exists, returns <code>null</code>.
 *
 * @return a column, or <code>null</code> if no column with the specified
 * id exists
 */
public abstract Column findColumn(Object id);

/**
 * Returns the index of the specified selectable.
 *
 * @param sel a selectable
 */
public int indexOfSelectable(Selectable sel) {
    return query.indexOfSelectable(sel);
}

/**
 * Returns an iterator over all tables, or <code>null</code> if the
 * data source does not have tables (for example, a character-separated
 * file data source).
 *
 * @return a possibly <code>null</code> iterator over all tables
 */
public abstract Iterator tables();

/**
 * Returns an iterator over all tables actually used in the report, or
 * <code>null</code> if the data source does not have tables (for example,
 * a character-separated file data source).
 *
 * @return a possibly <code>null</code> iterator over all tables used
 * in the report
 */
public abstract Iterator tablesUsedInReport();

/**
 * Returns an iterator over all columns.
 *
 * @return an iterator over all columns
 */
public abstract Iterator columns();

/**
 * Returns an iterator over all the columns in only the tables used by the
 * report, or over all columns if this data source does not have tables.
 *
 * @return an iterator over all columns in only tables used by the
 * report, or over all columns if this data source does not have tables
 */
public Iterator columnsInTablesUsedInReport() {
    Iterator iter = tablesUsedInReport();
    if (iter == null)
	return columns();

    // Create a new collection and return an iterator over it.
    ArrayList list = new ArrayList();
    while (iter.hasNext()) {
	Table t = (Table)iter.next();
	list.addAll(t.columns.values());
    }
    return list.iterator();
}

public void removeSort(Selectable s) {
    query.removeSort(s);
}

/**
 * Returns <code>true</code> if the specified parameter exists within this
 * data source's query.
 *
 * @param p a parameter
 * @return <code>true</code> if the specified parameter exists within
 * the query
 * @see Query#containsReferenceTo
 */
public boolean containsReferenceTo(Parameter p) {
    return query.containsReferenceTo(p);
}

/**
 * Writes this data source and its query as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("source");
    doWriteXML(out);
    query.writeXML(out);
    out.endElement();
}

protected abstract void doWriteXML(XMLWriter out);

}
