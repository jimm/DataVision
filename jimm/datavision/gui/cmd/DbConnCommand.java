package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.ErrorHandler;
import jimm.datavision.source.sql.Database;
import jimm.util.I18N;

/**
 * A command for changing a database's connection information.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DbConnCommand extends CommandAdapter {

protected Report report;
protected Database origDatabase;
protected String driverClassName;
protected String connInfo;
protected String dbName;
protected String username;
private String password;

/**
 * Constructor.
 *
 * @param report the report using this database
 * @param driverClassName database driver class name
 * @param connInfo database connection info string
 * @param dbName the database name
 * @param username the user name to use when logging in to the database
 * @param password the database password
 */
public DbConnCommand(Report report, String driverClassName,
		     String connInfo, String dbName, String username,
		     String password)
{
    super(I18N.get("DbConnCommand.name"));

    origDatabase = (Database)report.getDataSource();
    this.report = report;
    this.driverClassName = driverClassName;
    this.connInfo = connInfo;
    this.dbName = dbName;
    this.username = (username == null) ? "" : username;
    this.password = password;
}

public void perform() {
    Database db = origDatabase;
    try {
	if (db == null) {
	    db = new Database(driverClassName, connInfo, report, dbName,
			      username, password);
	    report.setDataSource(db);
	}
	else {
	    db.reset(driverClassName, connInfo, dbName, username, password);
	}
    }
    catch (Exception e) {
	ErrorHandler.error(e, I18N.get("DbConnWin.connect_error"));
    }
}

public void undo() {
    try {
	if (origDatabase == null)
	    report.setDataSource(null);
	else
	    origDatabase.reset(driverClassName, connInfo, dbName, username,
			       password);
    }
    catch (Exception e) {
	ErrorHandler.error(I18N.get("DbConnWin.revert_error") + "\n" + e,
			   I18N.get("DbConnWin.revert_error_title"));
    }
}

}
