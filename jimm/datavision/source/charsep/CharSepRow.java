package jimm.datavision.source.charsep;
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
 * A concrete subclass of <code>DataCursor</code> that wraps a delimited file parser.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class CharSepRow extends DataCursor {

protected CharSepSource source;
protected Query query;
protected Formula whereClauseFormula;
protected boolean noMoreData;
protected DelimParser parser;
protected HashMap dateParsers;
protected boolean dateParseErrorReported;

CharSepRow(CharSepSource source, Query query) {
    this.source = source;
    this.query = query;
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
protected List retrieveNextRow() {
    if (parser == null)
	parser = new DelimParser(source.getReader(), source.getSepChar());

    List data = null;
    try {
	data = parser.parse();
    }
    catch (IOException ioe) {
	ErrorHandler.error(ioe);
	noMoreData = true;
	return null;
    }
    if (data == null) {
	noMoreData = true;
	return null;
    }

    int numColumnsInData = data.size();
    int i = 0;
    for (Iterator iter = source.columns(); iter.hasNext(); ++i) {
	Column col = (Column)iter.next();

	if (i >= numColumnsInData) {
	    data.add(null);
	    continue;
	}

	if (col.isNumeric()) {
	    String str = data.get(i).toString();
	    if (str == null || str.length() == 0)
		data.set(i, new Integer(0));
	    else if (str.indexOf('.') == -1)
		data.set(i, new Integer(str));
	    else
		data.set(i, new Double(str));
	}
	else if (col.isDate())
	    data.set(i, parseDate(col, data.get(i).toString()));
	// else, it's a string; there is nothing to modify
    }

    return data;
}

protected Date parseDate(Column col, String dateString) {
    String formatString = col.getDateParseFormat();

    // Find existing parser, if any
    if (dateParsers == null)
	dateParsers = new HashMap();
    SimpleDateFormat parser = (SimpleDateFormat)dateParsers.get(formatString);

    if (parser == null) {
	parser = new SimpleDateFormat(formatString);
	dateParsers.put(formatString, parser);
    }

    try {
	return parser.parse(dateString);
    }
    catch (ParseException ex) {
	if (!dateParseErrorReported) {
	    ErrorHandler.error("Parse format string = " + formatString, ex);
	    dateParseErrorReported = true;
	}
	return null;
    }
}

public void close() {
    source.closeReader();
}

}
