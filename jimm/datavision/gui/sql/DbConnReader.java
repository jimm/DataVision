package jimm.datavision.gui.sql;


import jimm.datavision.Report;
import jimm.datavision.ReportReader;
import org.xml.sax.*;


/**
 * A database connection reader opens an existing report XML file and
 * reads the database connection information. It is opened when the
 * user clicks "Copy Settings..." from within a database connection
 * window.
 *
 * @see DbConnWin
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DbConnReader extends ReportReader {

protected String driverClassName;
protected String connInfo;
protected String dbName;
protected String username;

public DbConnReader() {
  super(new Report());
}

public String getDriverClassName() { return driverClassName; }
public String getConnectionInfo() { return connInfo; }
public String getDbName() { return dbName; }
public String getUserName() { return username; }

/**
 * Reads the database tag and grabs the attributes we want.
 */
public void startElement(final String namespaceURI, final String localName,
			 final String qName, final Attributes attributes)
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    if ("database".equals(tagName)) {
	driverClassName = attributes.getValue("driverClassName");
	connInfo = attributes.getValue("connInfo");
	dbName = attributes.getValue("name");
	username = attributes.getValue("username");
    }
}

public void endElement(final String namespaceURI, final String localName,
		       final String qName) {}
public void characters(char ch[], int start, int length) {}

}
