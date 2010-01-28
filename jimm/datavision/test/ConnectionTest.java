package jimm.datavision.test;
import jimm.datavision.Report;
import jimm.datavision.layout.CharSepLE;
import jimm.datavision.source.Column;
import jimm.datavision.source.sql.Database;
import jimm.datavision.source.sql.SQLQuery;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.sql.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Tests the {@link Database} class and the ability to give a connection to a
 * report and the state of a connection's query after reconnecting.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ConnectionTest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("test.xml"));
protected static final String PARAMETER_XML_FILE_NAME =
    AllTests.testDataFile("test_parameters.xml");
protected static final File OUT_FILE =
    new File(System.getProperty("java.io.tmpdir"),
	     "datavision_connection_test_out.txt");

protected static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";
protected static final String CONNECTION_INFO =
    "jdbc:postgresql://127.0.0.1/dv_example";
protected static final String DB_NAME = "dv_example";
protected static final String DB_USER = "jimm";
protected static final String DB_PASSWORD = "";

public static Test suite() {
    return new TestSuite(ConnectionTest.class);
}

public ConnectionTest(String name) {
    super(name);
}

public void testConnection() {
    Connection conn = null;

    try {
	Driver d = (Driver)Class.forName(DRIVER_CLASS_NAME).newInstance();
	DriverManager.registerDriver(d);
	conn = DriverManager.getConnection(CONNECTION_INFO, DB_USER,
					   DB_PASSWORD);

	Report report = new Report();
	report.setDatabaseConnection(conn);

	OUT_FILE.deleteOnExit();
	PrintWriter out = new PrintWriter(new FileWriter(OUT_FILE));
	report.setLayoutEngine(new CharSepLE(out, '\t'));

	report.runReport();
    }
    catch (Exception e) {
	e.printStackTrace();
	fail("exception thrown: " + e);
    }
    finally {
	if (conn != null) {
	    try {
		conn.close();
	    }
	    catch (SQLException sqle) {
		fail("SQL exception thrown: " + sqle);
	    }
	}
	if (OUT_FILE.exists())
	    OUT_FILE.delete();
    }
}

public void testQueryAfterReset() {
    Report report = new Report();
    try {
	report.setDatabasePassword(DB_PASSWORD);
	report.read(EXAMPLE_REPORT);

	Database db = (Database)report.getDataSource();
	SQLQuery query = (SQLQuery)db.getQuery();

	assertEquals("{jobs.ID} < 100", query.getWhereClause());
	assertNotNull(db.findColumn("ALL_CAPS.COL1"));
	assertNotNull(db.findColumn("jobs.fk_office_id"));
	assertNotNull(db.findColumn("office.email"));
	assertNotNull(db.findColumn("aggregate_test.value"));

	// We should only have two tables in the query.
	query.findSelectablesUsed();
	assertEquals(2, query.getNumTables());

	db.reset(DRIVER_CLASS_NAME, CONNECTION_INFO, DB_NAME, DB_USER,
		 DB_PASSWORD);
	// The query doesn't have to be the same object, but it's where
	// clause (and all other information) should darned well be the same.
	assertEquals("{jobs.ID} < 100", query.getWhereClause());
	assertNotNull(db.findColumn("public.ALL_CAPS.COL1"));
	assertNotNull(db.findColumn("public.jobs.fk_office_id"));
	assertNotNull(db.findColumn("public.office.email"));
	assertNotNull(db.findColumn("public.aggregate_test.value"));

	// Make sure we still have two tables in the query.
	query.findSelectablesUsed();
	assertEquals(2, query.getNumTables());
    }
    catch (Exception e) {
	fail(e.toString());
    }
}

public void testDatabaseReset() throws Exception {
    Report report = new Report();
    report.setDatabasePassword(DB_PASSWORD);
    report.read(EXAMPLE_REPORT);

    Database db = (Database)report.getDataSource();
    SQLQuery origQuery = (SQLQuery)db.getQuery();

    db.reset(db.getDriverClassName(), db.getConnectionInfo(), db.getName(),
	     db.getUserName(), "");
    SQLQuery q = (SQLQuery)db.getQuery();

    // Unfortunately, we can't just compare query strings. That's because
    // the table and column lists aren't guaranteed to be sorted.
    //
    // At least these tests detect the bug we're fixing.
    assertEquals(origQuery.getNumTables(), q.getNumTables());
    assertEquals(origQuery.getNumSelectables(), q.getNumSelectables());
}

public void testSchemaNamesInColumns() throws Exception {
    Report report = new Report();
    report.setDatabasePassword(DB_PASSWORD);
    report.read(EXAMPLE_REPORT);

    // Found because we try blank schema
    Column col = report.findColumn("jobs.ID");
    assertNotNull(col);
    assertEquals("public.jobs.ID", col.getId());

    // Found due to exact match
    col = report.findColumn("public.jobs.ID");
    assertNotNull(col);
    assertEquals("public.jobs.ID", col.getId());

    // Not found because schema doesn't match table's schema
    col = report.findColumn("dv_example.jobs.ID");
    assertNull(col);
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
