package jimm.datavision.source.charsep;
import jimm.datavision.*;
import jimm.datavision.source.*;
import jimm.util.XMLWriter;
import java.io.*;
import java.util.*;

/**
 * A data source for files whose lines are rows and columns are separated
 * by a character.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class CharSepSource extends DataSource {

protected static final char DEFAULT_SEP_CHAR = ',';

protected ArrayList columns;
protected char sepChar;
protected BufferedReader reader;
protected String sourceFilePath;

public CharSepSource(Report report) {
    super(report, new CharSepQuery(report));
    columns = new ArrayList();
    sepChar = DEFAULT_SEP_CHAR;
}

public boolean canJoinTables() { return true; }
public boolean isSQLGenerated() { return false; }
public boolean isConnectionEditable() { return false; }
public boolean areRecordsSelectable() { return true; }
public boolean areRecordsSortable() { return false; }
public boolean canGroupRecords() { return false; }

public boolean usesSourceFile() { return true; }
public boolean needsSourceFile() { return reader == null; }
public boolean alreadyUsedSourceFile() {
    return sourceFilePath != null && reader == null;
}

public String getSourceFile() { return sourceFilePath; }

public void setSourceFile(String filePath) throws FileNotFoundException {
    sourceFilePath = filePath;
    reuseSourceFile();
}

public void reuseSourceFile() throws FileNotFoundException {
    setInput(new FileReader(sourceFilePath));
}

public void setInput(Reader reader) {
    if (reader instanceof BufferedReader)
	this.reader = (BufferedReader)reader;
    else
	this.reader = new BufferedReader(reader);
}

public void setInput(InputStreamReader inputStreamReader) {
    reader = new BufferedReader(inputStreamReader);
}

public void setInput(String fileName) throws FileNotFoundException {
    setSourceFile(fileName);
}

public char getSepChar() { return sepChar; }
public void setSepChar(char c) { sepChar = c; }

/**
 * This override not only remembers the column but also hands it to the
 * query for cacheing.
 *
 * @param col a column
 */
public void addColumn(Column col) {
    columns.add(col);
    ((CharSepQuery)query).addColumn(col);
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
    return new CharSepRow(this, query);
}

BufferedReader getReader() {
    return reader;
}

void closeReader() {
    try {
	if (reader != null)
	    reader.close();
    }
    catch (IOException ioe) {
	ErrorHandler.error(ioe);
    }
    finally {
	reader = null;
    }
}

/**
 * Writes this database and all its tables as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
protected void doWriteXML(XMLWriter out) {
    out.startElement("charsep");
    out.attr("sepchar", sepChar);
    if (metadataURL != null)
	out.textElement("metadata-url", metadataURL);
    else
	for (Iterator iter = columns.iterator(); iter.hasNext(); )
	    ((Column)iter.next()).writeXML(out);
    out.endElement();
}

}
