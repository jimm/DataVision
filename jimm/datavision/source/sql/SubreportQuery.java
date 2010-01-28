package jimm.datavision.source.sql;
import jimm.datavision.*;
import jimm.datavision.source.*;
import jimm.util.XMLWriter;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Queries build SQL query strings. They contain tables, joins, and
 * where clauses.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see ParserHelper
 */
public class SubreportQuery extends SQLQuery {

protected Subreport subreport;
protected ArrayList subreportJoins;

/**
 * Constructor.
 *
 * @param report the report for which this query will generate SQL
 */
public SubreportQuery(Subreport report) {
    super(report);
    subreport = report;
    subreportJoins = new ArrayList();
}

public void addSubreportJoin(Join join) {
    subreportJoins.add(join);
}

public void addSubreportJoins(Collection coll) {
    subreportJoins.addAll(coll);
}

/**
 * Returns an iterator over all of the columns that need to be included in
 * the parent report's query so that the values are available to this
 * subreport when it builds its query.
 *
 * @return an iterator over selectables
 */
public Iterator parentColumns() {
    ArrayList list = new ArrayList();
    for (Iterator iter = subreportJoins.iterator(); iter.hasNext(); )
	list.add(((Join)iter.next()).getFrom());
    return list.iterator();
}

protected void buildWhereClause(StringBuffer str, boolean forDisplay) {
    // We always have a where clauses, even if the user didn't specify one
    str.append(" where ");
    if (!joins.isEmpty()) {
	buildJoins(str);
	str.append(" and ");
    }
    buildUserWhereClause(str, forDisplay);
}

public String getWhereClauseForDisplay() {
    StringBuffer buf = new StringBuffer();
    if (whereClause != null && whereClause.length() > 0) {
	buf.append('(');
	buf.append(super.getWhereClauseForPreparedStatement());
	buf.append(") and (");
    }
    for (Iterator iter = subreportJoins.iterator(); iter.hasNext(); ) {
	Join j = (Join)iter.next();
	buf.append(((Column)j.getFrom()).fullName());
	buf.append(' ');
	buf.append(j.getRelation());
	buf.append(' ');
	buf.append(quoted(((Column)j.getTo()).fullName()));
	if (iter.hasNext())
	    buf.append(" and ");
    }
    if (whereClause != null && whereClause.length() > 0)
	buf.append(')');
    return buf.toString();
}

public String getWhereClauseForPreparedStatement() {
    StringBuffer buf = new StringBuffer();
    if (whereClause != null && whereClause.length() > 0) {
	buf.append('(');
	buf.append(super.getWhereClauseForPreparedStatement());
	buf.append(") and (");
    }
    for (Iterator iter = subreportJoins.iterator(); iter.hasNext(); ) {
	Join j = (Join)iter.next();
	buf.append("? ");
	buf.append(j.getRelation());
	buf.append(' ');
	buf.append(quoted(((Column)j.getTo()).fullName()));
	if (iter.hasNext())
	    buf.append(" and ");
    }
    if (whereClause != null && whereClause.length() > 0)
	buf.append(')');
    return buf.toString();
}

public void setParameters(PreparedStatement stmt) throws SQLException {
    super.setParameters(stmt);

    // Continue with parameters after those filled in by superclass.
    // Remember that param indices start at 1.
    int i = preparedStmtValues.size() + 1;
    for (Iterator iter = subreportJoins.iterator(); iter.hasNext(); ++i) {
	// In Oracle, Java Dates are turned into timestamps, or something
	// like that. This is an attempt to fix this problem.
	Column from = ((Join)iter.next()).getFrom();
	Object val = subreport.getParentReport().columnValue(from);
	if (val instanceof java.util.Date)
	    stmt.setDate(i,
			 new java.sql.Date(((java.util.Date)val).getTime()));
	else
	    stmt.setObject(i, val);
    }
}

protected void writeExtras(XMLWriter out) {
    out.startElement("subreport-joins");
    for (Iterator iter = subreportJoins.iterator(); iter.hasNext(); )
	((Join)iter.next()).writeXML(out);
    out.endElement();
}

}
