package jimm.datavision.gui.applet;
import jimm.datavision.Report;
import jimm.datavision.ErrorHandler;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.AskStringDialog;
import jimm.util.I18N;
import jimm.util.XMLWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.xml.sax.InputSource;

/**
 * A designer suitable for use with applets. This designer is created
 * by a {@link DVApplet} in its <code>init</code> method.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class DesignApplet extends Designer {

DesignApplet(DVApplet applet) {
    super(null, null, applet, null);

    reportFilePath = getAppletViaCheapTrick().getSaveURL();
}

/**
 * Reads the named report file or, if it's <code>null</code>, creates
 * a new, empty report. Returns <code>true</code> if we need to ask
 * the user for connection info because this is a new report.
 *
 * @param fileName
 * @param databasePassword string to give to report; OK if it's
 * <code>null</code>
 * @return <code>true</code> if we need to ask the user for connection info
 */
protected boolean readReport(String fileName, String databasePassword) {
    report = new Report();
    String url = getAppletViaCheapTrick().getReportURL();
    try {
	if (url != null && url.length() > 0)
	    report.read(new InputSource(url));
    }
    catch (Exception e) {
	ErrorHandler.error(e);
    }

    reportFilePath = null;
    return false;		// No password needed
}

/**
 * A cheap trick: we need the applet but this method is called from the
 * constructor indirectly so we can't assign the applet to an instance var
 * of the correct type. However, we passed the applet in as our root pane
 * container.
 *
 * @return the applet
 */
protected DVApplet getAppletViaCheapTrick() {
    return (DVApplet)rootPaneContainer;
}

/**
 * Saves the current report to a URL specified by the user.
 */
protected void saveReportAs() {
    String name = new AskStringDialog(getFrame(),
				      I18N.get("DesignApplet.new_url_title"),
				      I18N.get("DesignApplet.new_url_prompt"),
				      reportFilePath)
	.getString();

    if (name != null) {
	reportFilePath = name;
	writeReportFile(reportFilePath);
    }
}

/**
 * Writes the current report to the specified file. Also tells the
 * command history the report has been saved so it knows how to report
 * if any changes have been made from this point on.
 *
 * @param fileName a file name
 */
protected void writeReportFile(String fileName) {
    URLConnection conn = null;

    try {
	URL url = new URL(fileName);
	conn = url.openConnection();
	conn.setDoOutput(true);

	sendData(conn);
	// I don't know why, but we have to read the response from the
	// server, even if it's empty.
	receiveResponse(conn);
    }
    catch (Exception e) {
	ErrorHandler.error(e);
    }

    // Don't forget this.
    commandHistory.setBaseline();
}

protected void sendData(URLConnection conn) throws IOException {
    XMLWriter out = null;
    try {
	out = new XMLWriter(conn.getOutputStream());
	report.writeXML(out);
	out.flush();
    }
    catch (IOException e) {
	throw e;
    }
    finally {
	if (out != null) out.close();
    }
}

protected void receiveResponse(URLConnection conn) throws IOException {
    InputStreamReader in = null;
    try {
	in = new InputStreamReader(conn.getInputStream());
	char[] buf = new char[1024];
	while (in.read(buf) > 0) ;
    }
    catch (IOException e) {
	throw e;
    }
    finally {
	if (in != null) in.close();
    }
}

}
