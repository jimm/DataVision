package jimm.datavision.source.sql;
import jimm.datavision.ErrorHandler;
import jimm.datavision.source.DataCursor;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

/**
 * A concrete subclass of <code>DataCursor</code> that wraps a JDBC result set.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ResultSetRow extends DataCursor {

protected PreparedStatement stmt;
protected ResultSet rset;
protected int numSelectables;
protected boolean noMoreData;

ResultSetRow(Connection conn, SQLQuery query) throws SQLException {
    // Suggested by Konstantin. Though it works for his Oracle driver,
    // it doesn't work for my PostgreSQL driver. These args are also
    // legal for prepared statements.
    //  	stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
    //  				    ResultSet.CONCUR_READ_ONLY);

    String preparedStmtString = query.toPreparedStatementString();
    if (preparedStmtString != null && preparedStmtString.length() > 0) {
	stmt = conn.prepareStatement(preparedStmtString);
	query.setParameters(stmt);
	rset = stmt.executeQuery();
	numSelectables = query.getNumSelectables();
	noMoreData = false;
    }
    else {
	numSelectables = 0;
	noMoreData = true;
    }
}

public List readRowData() {
    // Avoid calling rset.next() if it has already returned false. Doing so
    // appears harmless in most cases but seems to be causing a problem
    // with at least one user's DB2 JDBC driver.
    if (noMoreData)
	return null;

    try {
	if (!rset.next()) {
	    noMoreData = true;
	    return null;
	}
    }
    catch (SQLException sqle) {
	ErrorHandler.error(sqle);
	return null;
    }

    ArrayList list = new ArrayList();
    try {
	for (int i = 1; i <= numSelectables; ++i)
	    list.add(rset.getObject(i));
    }
    catch (SQLException sqle) {
	ErrorHandler.error(sqle);
    }
    return list;
}

public void close() {
    try {
	if (rset != null) rset.close();
	if (stmt != null) stmt.close();
    }
    catch (SQLException sqle) {
	ErrorHandler.error(sqle);
    }
    finally {
	rset = null;
	stmt = null;
    }
}

}
