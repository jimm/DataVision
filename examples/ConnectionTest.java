package examples;
import jimm.util.Getopts;
import java.sql.*;

/**
 * This program lets you test your driver class name, connection info
 * string, and all the other stuff that goes into the DataVision database
 * connection dialog box.
 * <p>
 * To compile and run using Ant, specify the all of the command line args
 * using the single property "cmdline". Here is an example:
 * <pre>
 * % ant -Dcmdline="-d driver -c conninfo" conntest
 * </pre>
 * <p>
 * To compile and run using make, use "make conntest". To run, type the
 * following on a single line (broken up for readability):
 * <pre>
 * java -classpath classes:path-to-driver.jar examples.ConnectionTest
 * [-v] -d driver -c conninfo [-s schema] -u username [-p password]
 * </pre>
 * <p>
 * Both the <b>-s</b> and <b>-p</b> options are...um...optional.
 * <p>
 * As an example, here is how I run the test:
 * <pre>
 * java -classpath classes:/usr/lib/pgsql/pgjdbc2.jar
 * examples.ConnectionTest -d org.postgresql.Driver
 * -c jdbc:postgresql://localhost/dv_example -s dv_example -u jimm -v
 * </pre>
 * <p>
 * The <b>-v</b> option turns on verbose mode; messages describing the
 * progress of establishing the connection are printed as is a list of
 * all the table names in the schema.
 * <p>
 * The <b>-h</b> option prints a help message.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ConnectionTest {

public static void usage() {
    System.err.println(
"usage: java [-classpath path] examples.ConnectionTest\n" +
"        -d driver -c conn [-s schema] -u username [-p password]\n" +
"  -d driver    Driver class name\n" +
"  -c conn      Connection info string\n" +
"  -s schema    Database schema name (database name)\n" +
"  -u username  Database user name\n" +
"  -p password  Database password\n" +
"\n" +
"  -v           Verbose; print all table names found\n" +
"  -h           This help");
    System.exit(1);
}

public static void main(String[] args) {
    Getopts g = new Getopts("hvd:c:s:u:p:", args);
    if (g.error() || g.hasOption('h') || !g.hasOption('d') || !g.hasOption('c')
	|| !g.hasOption('u'))
	usage();

    boolean verbose = g.hasOption('v');
    try {
	// Load the database JDBC driver
	if (verbose) System.out.println("loading driver");
	Driver d = (Driver)Class.forName(g.option('d')).newInstance();
	if (verbose) System.out.println("registering driver");
	DriverManager.registerDriver(d);

	// Connect to the database
	if (verbose) System.out.println("creating database connection");
	Connection conn = DriverManager.getConnection(g.option('c'),
						      g.option('u'),
						      g.option('p'));

	// If verbose, read table names and print them
	if (verbose) {
	    DatabaseMetaData dbmd = conn.getMetaData();

	    System.out.println("stores lower case identifiers = "
			       + dbmd.storesLowerCaseIdentifiers());
	    System.out.println("stores upper case identifiers = "
			       + dbmd.storesUpperCaseIdentifiers());

	    System.out.println("tables:");
	    ResultSet rset = dbmd.getTables(null, g.option('s'), "%", null);
	    while (rset.next())
		System.out.println("  " + rset.getString("TABLE_NAME"));
	    rset.close();
	}

	if (verbose) System.out.println("closing the connection");
	conn.close();

	System.out.println("done");
    }
    catch (SQLException sqle) {
	SQLException ex = (SQLException)sqle;
	ex = ex.getNextException();
	while (ex != null) {
	    System.err.println(ex.toString());
	    ex = ex.getNextException();
	}
	sqle.printStackTrace();
	System.exit(1);
    }
    catch (Exception e) {
	System.err.println(e.toString());
	e.printStackTrace();
	System.exit(1);
    }
}

}
