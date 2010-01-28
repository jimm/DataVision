<%--
This page runs the report found in postgresql.xml.

Be sure to edit DATABASE_PASSWORD and REPORT_FILE below.

To use with Tomcat 3.3:
    * Create the directory $TOMCAT_HOME/webapps/datavision
    * Under that directory, create the directories WEB-INF then WEB-INF/lib
    * Copy this file to $TOMCAT_HOME/webapps/datavision/index.jsp
    * Copy lib/*.jar to $TOMCAT_HOME/webapps/WEB-INF/lib
    * Copy your JDBC jar file to $TOMCAT_HOME/webapps/WEB-INF/lib
    * Start Tomcat
    * Visit http://localhost:8080/datavision/

You may need to use a port number other than 8080; check you Tomcat
installation. Also, the installation instructions may be slightly
different for different versions of Tomcat (specifically 4.X).
 --%>
<%@ page import="jimm.datavision.*" %>
<%@ page import="jimm.datavision.layout.HTMLLE" %>
<%@ page import="org.xml.sax.InputSource" %>
<%@ page import="java.io.*" %>
<%!
static final String DATABASE_PASSWORD = "";
static final String REPORT_FILE =
    "/Users/jimm/src/datavision/examples/postgresql.xml";

// NOTE: this WHERE_CLAUSE string is an example, and is most likely not what
// you want at all. It is used below, and is only here to give you an example
// of how to set the WHERE clause in your SQL. You should either set this
// string to null or get rid of it completely along with the if statement
// below that uses it.
static final String WHERE_CLAUSE = "{jobs.ID} < 100";
%>
<%
Report report = new Report();
report.setDatabasePassword(DATABASE_PASSWORD);
report.read(new File(REPORT_FILE));

File tmp = File.createTempFile("datavision", null);
tmp.deleteOnExit();

HTMLLE le = new HTMLLE(new PrintWriter(new FileWriter(tmp)));
report.setLayoutEngine(le);

// Let's limit the records that we select, Ok?
if (WHERE_CLAUSE != null)
    report.getDataSource().getQuery().setWhereClause(WHERE_CLAUSE);

report.runReport();

// Copy data from generated HTML file to this page's output.
BufferedReader in = new BufferedReader(new FileReader(tmp));
String line;
while ((line = in.readLine()) != null) {
    out.println(line);
}
%>
