package jimm.datavision.source.object;
import jimm.datavision.Formula;
import jimm.datavision.ErrorHandler;
import jimm.datavision.source.DataCursor;
import jimm.datavision.source.Column;
import jimm.datavision.source.Query;
import java.util.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * A concrete subclass of <code>DataCursor</code> that wraps an object parser.
 *
 * @author Frank W. Zammetti, <a href="mailto:fzammetti@omnytex.com">fzammetti@omnytex.com</a>
 */
public class ObjectRow extends DataCursor {

protected ObjectSource source;
protected Query query;
protected Formula whereClauseFormula;
protected boolean noMoreData;
protected HashMap dateParsers;
protected boolean dateParseErrorReported;

private ArrayList data;
ObjectRow(ObjectSource source, Query query) {
    this.source = source;
    this.query = query;
    this.data = source.getData();
    this.query.findSelectablesUsed();	// Needed so we can find columns later

    String script = query.getWhereClause();
    if (script != null && script.length() > 0)
	whereClauseFormula = new Formula(null, source.getReport(), "", script);
}

/**
 * Returns the next row of data. If there is a where clause, use that to
 * determine which rows we accept or reject.
 */
public List readRowData() {
    if (noMoreData)
	return null;

    List data = null;

    boolean acceptRow;
    do {
	data = retrieveNextRow();
	if (whereClauseFormula != null && data != null) {
	    // Run the Ruby script and retrive its boolean value. If true,
	    // accept the line. Else, reject it and move on to the next
	    // line.

	    // First, save the existing current row and make the new data
	    // the current row. We need to do this because the formula we
	    // are about to evaluate may make use of data in the new
	    // current row.
	    List origCurrRowData = currRowData;
	    currRowData = data;	// Need data available to formula

	    // Evaulate the script and retrieve a boolean.
	    Object obj = whereClauseFormula.eval();
	    acceptRow = ((Boolean)obj).booleanValue();

	    // Replace the original current row data (acutally the previous
	    // row, but we don't care).
	    currRowData = origCurrRowData;
	}
	else
	    acceptRow = true;
    } while (!acceptRow);

    return data;
}

/**
 * Retrieve the next row of data and return it as a list of column values.
 *
 * @return a list of column values
 */
private int rowIndex = 0;
protected List retrieveNextRow() {

    if (noMoreData) { return null; }
    List row = null;
    try {
      row = (ArrayList)data.get(rowIndex);
      rowIndex++;
    } catch (IndexOutOfBoundsException ioobe) {
      noMoreData = true;
    }
    return row;

}

}
