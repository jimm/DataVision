package jimm.datavision.source;
import java.util.List;

/**
 * Represents a row of data. Provides the interface needed by
 * <code>Report</code>, no more. When using JDBC, this is a wrapper around
 * a <code>ResultSet</code>.
 * <p>
 * The only method subclasses <em>must</em> implement is
 * <code>readRowData()</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class DataCursor {

protected int currRowNumber;	// Starts at 1
protected List prevRowData;
protected List currRowData;
protected List nextRowData;
protected List lastRowData;
protected int lastRowNumber;

public boolean isFirst() { return currRowNumber == 1; }
public boolean isLast() {
    if (nextRowData != null)	// We already have next row cached
	return false;
    nextRowData = readRowData(); // Read next row
    return nextRowData == null;	// If it's null, we are at the last row
}

public boolean next() {
    if (nextRowData == null)	// If we have no cached data, read the next row
	nextRowData = readRowData();

    if (nextRowData == null) {	// When no more data, curr row is the last row
	lastRowData = currRowData;
	lastRowNumber = currRowNumber;
    }

    prevRowData = currRowData;
    currRowData = nextRowData;
    nextRowData = null;

    ++currRowNumber;
    return currRowData != null;
}

public boolean previous() {
    if (currRowNumber <= 1)	// Not same as isFirst()
	return false;

    nextRowData = currRowData;
    currRowData = prevRowData;
    prevRowData = null;
    --currRowNumber;
    return true;
}

public boolean last() {
    while (lastRowData == null && next())
	;
    currRowData = lastRowData;
    currRowNumber = lastRowNumber;
    return true;
}

public int getRow() { return currRowNumber; }

public void close() {}

/**
 * Returns the object in the specified column. <var>index</var> starts
 * at 1.
 * <p>
 * Even when running a report, <var>currRowData</var> is not always
 * defined. For example, when a query returns zero rows but a column field
 * is in some header or footer, this method is called but
 * <var>currRowData</var> is <code>null</code>. If it is, we return
 * <code>null</code>.
 *
 * @return the object in the <var>index</var>'th column, or <code>null</code>
 * if no data has yet been read
 */
public Object getObject(int index) {
    return currRowData == null ? null : currRowData.get(index - 1);
}

protected abstract List readRowData();

}
