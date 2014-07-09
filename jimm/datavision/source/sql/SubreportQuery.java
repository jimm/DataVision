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
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 * @see ParserHelper
 */
public class SubreportQuery extends SQLQuery {

protected Subreport subreport;
protected ArrayList<Join> subreportJoins;

/**
 * Constructor.
 *
 * @param report the report for which this query will generate SQL
 */
public SubreportQuery(Subreport report) {
    super(report);
    subreport = report;
    subreportJoins = new ArrayList<Join>();
}

public void addSubreportJoin(Join join) {
    subreportJoins.add(join);
}

public void addSubreportJoins(Collection<Join> coll) {
    subreportJoins.addAll(coll);
}

/**
 * Returns an iterator over all of the columns that need to be included in
 * the parent report's query so that the values are available to this
 * subreport when it builds its query.
 *
 * @return an iterator over selectables
 */
public Iterable<Column> parentColumns() {
    ArrayList<Column> list = new ArrayList<Column>();
    for (Join j : subreportJoins)
	list.add(j.getFrom());
    return list;
}

protected void buildWhereClause(StringBuilder str, boolean forDisplay) {
    // We always have a where clauses, even if the user didn't specify one
    str.append(" where ");
    if (!joins.isEmpty()) {
	buildJoins(str);
	str.append(" and ");
    }
    buildUserWhereClause(str, forDisplay);
}

public String getWhereClauseForDisplay() {
    StringBuilder buf = new StringBuilder();
    if (whereClause != null && whereClause.length() > 0) {
	buf.append('(');
	buf.append(super.getWhereClauseForPreparedStatement());
	buf.append(") and (");
    }
    boolean hasJoin = false;
    for (Join j : subreportJoins) {
	if (hasJoin)
	    buf.append(" and ");
	else
	    hasJoin = true;
	buf.append(((Column)j.getFrom()).fullName());
	buf.append(' ');
	buf.append(j.getRelation());
	buf.append(' ');
	buf.append(quoted(((Column)j.getTo()).fullName()));
    }
    if (whereClause != null && whereClause.length() > 0)
	buf.append(')');
    return buf.toString();
}

public String getWhereClauseForPreparedStatement() {
    StringBuilder buf = new StringBuilder();
    if (whereClause != null && whereClause.length() > 0) {
	buf.append('(');
	buf.append(super.getWhereClauseForPreparedStatement());
	buf.append(") and (");
    }
    boolean hasJoin = false;
    for (Join j : subreportJoins) {
	if (hasJoin)
	    buf.append(" and ");
	else
	    hasJoin = true;
	buf.append("? ");
	buf.append(j.getRelation());
	buf.append(' ');
	buf.append(quoted(((Column)j.getTo()).fullName()));
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
    for (Join j : subreportJoins) {
	// In Oracle, Java Dates are turned into timestamps, or something
	// like that. This is an attempt to fix this problem.
	Column from = j.getFrom();
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
    for (Join j : subreportJoins)
	j.writeXML(out);
    out.endElement();
}

}
