package jimm.datavision.source.ncsql;
import jimm.datavision.Report;
import jimm.datavision.source.sql.SQLQuery;
import jimm.util.StringUtils;
import java.util.List;

/**
 * Queries build NC query strings. They contain tables, joins, and
 * where clauses.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NCQuery extends SQLQuery {

/**
 * Constructor.
 *
 * @param report the report for which this query will generate NC
 */
public NCQuery(Report report) {
    super(report);
}

/**
 * Quotes those parts of a table or column name that need to be quoted.
 * <p>
 * Different databases and JDBC drivers treat case sensitively differently.
 * We assume the database is case-sensitive.
 *
 * @param name a table or column name
 * @return a quoted version of the name
 */
public String quoted(String name) {
    List components = StringUtils.split(name, ".");
    int len = components.size();
    for (int i = 0; i < len; ++i) {
	String component = (String)components.get(i);
	// Put quotes around the component if there is a space in the
	// component or we have non-lower-case letters.
	if (component.indexOf(" ") >= 0
	    || !component.equals(component.toLowerCase()))
	    components.set(i, "\"" + component + "\"");
    }
    return StringUtils.join(components, ".");
}

}
