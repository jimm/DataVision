package jimm.datavision.gui.applet;
import javax.swing.JApplet;

/**
 * A report design applet. When initialized, it creates a
 * {@link DesignApplet} designer.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DVApplet extends JApplet {

protected static final String COLUMN_PARAM_DELIM = ";";

protected DesignApplet designer;

public void init() {
    designer = new DesignApplet(this);
}

/**
 * Returns the &quot;report-url&quot; applet parameter value.
 *
 * @return the &quot;report-url&quot; applet parameter value
 */
String getReportURL() {
    return getParameter("report-url");
}

/**
 * Returns the &quot;save-url&quot; applet parameter value.
 *
 * @return the &quot;save-url&quot; applet parameter value
 */
String getSaveURL() {
    return getParameter("save-url");
}

}
