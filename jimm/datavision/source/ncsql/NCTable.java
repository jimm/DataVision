package jimm.datavision.source.ncsql;
import jimm.datavision.source.*;

/**
 * A table for no-connection database sources.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see NCDatabase
 */
public class NCTable extends Table {

/**
 * Constructor.
 *
 * @param dataSource the data source in which this table resides
 * @param name the table's name
 */
public NCTable(DataSource dataSource, String name) {
    super(dataSource, name);
}

}
