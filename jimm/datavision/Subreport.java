package jimm.datavision;
import jimm.datavision.field.Field;
import jimm.datavision.source.*;
import jimm.datavision.source.sql.SubreportQuery;
import jimm.util.StringUtils;
import jimm.util.XMLWriter;
import java.util.*;

/**
 * A subreport is a report whose query is run every time the field
 * containing it is output.
 * <p>
 * When first created, the subreport adds the joins given to it
 * to its SQL where clause, turning the columns from the current
 * report into parameters.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Subreport extends Report implements Identity {

protected Report parentReport;
protected Long id;
protected String cachedValue;

public Subreport(Report parent, Long id) {
    if (id == null)		// Generate new value
	id = parent.generateNewSubreportId();
    this.id = id;

    parentReport = parent;
    parentReport.addSubreport(this);
}

public Object getId() { return id; }

public Report getParentReport() { return parentReport; }

public void addJoin(Join join) {
    ((SubreportQuery)getDataSource().getQuery()).addSubreportJoin(join);
}

public void addAllJoins(Collection coll) {
    ((SubreportQuery)getDataSource().getQuery()).addSubreportJoins(coll);
}

/**
 * Returns an iterator over all of the columns that need to be included in
 * the parent report's query so that the values are available to this
 * subreport when it builds its query.
 *
 * @return an iterator over selectables
 */
public Iterator parentColumns() {
    return ((SubreportQuery)getDataSource().getQuery()).parentColumns();
}

public void clearCache() {
    cachedValue = null;
}

/**
 * Runs the query and returns a string containing a line of text for each
 * row returned by the subreport query.
 *
 * @return a string with newlines separating each row of data
 * @see #makeRowStrings
*/
public Object getValue() {
    if (cachedValue != null)
	return cachedValue;

    rset = null;
    cachedValue = "";		// In case something happens
    try {
	rset = getDataSource().execute();
	if (rset != null)
	    cachedValue = StringUtils.join(makeRowStrings(), "\n");
    }
    catch (Exception e) {
	ErrorHandler.error(e.toString());
    }
    finally {
	if (rset != null)
	    rset.close();
    }

    return cachedValue;
}

/**
 * Returns an array of strings, each containing the values returned by the
 * subreport query separated by spaces.
 */
protected Collection makeRowStrings() {
    ArrayList rowStrings = new ArrayList();
    Section detail = getFirstSectionByArea(SectionArea.DETAIL);
    while (rset.next()) {
	ArrayList values = new ArrayList();
	for (Iterator iter = detail.fields(); iter.hasNext(); ) {
	    String str = ((Field)iter.next()).toString();
	    values.add(str == null ? "" : str);
	}
	rowStrings.add(StringUtils.join(values, " "));
    }
    return rowStrings;
}

public void writeXML(XMLWriter out) {
    out.startElement("subreport");
    out.attr("id", id);
    getDataSource().getQuery().writeXML(out);
    ListWriter.writeList(out, formulas.values(), "formulas");
    ListWriter.writeList(out, usercols.values(), "usercols");
    ListWriter.writeList(out, details.sections(), "details");
    out.endElement();
}

}
