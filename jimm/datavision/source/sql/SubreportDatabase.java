package jimm.datavision.source.sql;
import jimm.datavision.Subreport;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The only difference between this and a <code>Database</code> is the
 * type of the query that it holds and the single constructor.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SubreportDatabase extends Database {

public SubreportDatabase(Connection conn, Subreport report)
  throws SQLException
{
    super(conn, report);
    query = new SubreportQuery(report);
}

}
