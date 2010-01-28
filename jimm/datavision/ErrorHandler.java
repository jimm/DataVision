package jimm.datavision;
import jimm.util.StringUtils;
import jimm.util.I18N;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * This class provides static methods for displaying error messages.
 * It displays error messages to System.err. When the GUI is being used
 * it displays error messages in dialog boxes as well.
 * <p>
 * The only shortcoming is that you have to explicitly tell this class
 * whether to use the GUI or not.
 * <p>
 * This class is also used by other parts of the system to determine
 * if the report is being run with or without a GUI.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ErrorHandler {

protected static final int MAX_MESSAGE_WIDTH = 66;

protected static boolean useGUI = false;

/**
 * Tells the error handler routines whether to display error messages
 * in dialog boxes or not.
 *
 * @param b if <code>true</code>, all error messages will be displayed
 * using a dialog box in addition to being printed on System.err
 */
public static void useGUI(boolean b) { useGUI = b; }

/**
 * Returns <code>true</code> if we've been told to use the GUI.
 *
 * @return <code>true</code> if we've been told to use the GUI
 */
public static boolean usingGUI() { return useGUI; }

/**
 * Displays an error message.
 *
 * @param message the error message; may be <code>null</code>,
 * but that would be rather silly
 */
public static void error(String message) {
    error(message, null, null);
}

/**
 * Displays an error message and an exception (actually a
 * <code>Throwable</code>) Both arguments are optionally <code>null</code>.
 *
 * @param message the error message; may be <code>null</code>
 * @param t a throwable; may be <code>null</code>
 */
public static void error(String message, Throwable t) {
    error(message, t, null);
}

/**
 * Displays an error message with the given window title. Both
 * arguments are optionally <code>null</code>.
 *
 * @param message the error message; may be <code>null</code>
 * @param windowTitle a string to use as the dialog title; may be
 * <code>null</code>
 */
public static void error(String message, String windowTitle) {
    error(message, null, windowTitle);
}

/**
 * Displays an exception (actually a <code>Throwable</code>). It may
 * be <code>null</code>, but that would be rather silly.
 *
 * @param t a throwable; may be <code>null</code>
 */
public static void error(Throwable t) {
    error(null, t, null);
}

/**
 * Displays an error message and an exception (actually a
 * <code>Throwable</code>) with the given window title. All three
 * arguments are optionally <code>null</code>.
 *
 * @param t a throwable; may be <code>null</code>
 * @param windowTitle a string to use as the dialog title; may be
 * <code>null</code>
 */
public static void error(Throwable t, String windowTitle) {
    error(null, t, windowTitle);
}

/**
 * Displays an error message and an exception (actually a
 * <code>Throwable</code>) with the given window title. All three
 * arguments are optionally <code>null</code>.
 *
 * @param message the error message; may be <code>null</code>
 * @param t a throwable; may be <code>null</code>
 * @param windowTitle a string to use as the dialog title; may be
 * <code>null</code>
 */
public static void error(String message, Throwable t, String windowTitle) {
    StringBuffer buf = new StringBuffer();
    if (message != null) StringUtils.splitUp(buf, message, MAX_MESSAGE_WIDTH);
    if (t != null) {
	if (message != null) buf.append("\n");
	StringUtils.splitUp(buf, t.toString(), MAX_MESSAGE_WIDTH);
	if (t instanceof SQLException) {
	    SQLException ex = (SQLException)t;
	    ex = ex.getNextException();
	    while (ex != null) {
		buf.append("\n");
		StringUtils.splitUp(buf, ex.toString(), MAX_MESSAGE_WIDTH);
		ex = ex.getNextException();
	    }
	}
    }
    String errorMessage = buf.toString();

    System.err.println("DataVision v" + info.Version);
    if (windowTitle != null)
	System.err.print(windowTitle + ": ");
    System.err.println(errorMessage);

    if (t != null)
	t.printStackTrace();

    if (useGUI) {
	if (windowTitle == null)
	    windowTitle = I18N.get("ErrorHandler.default_win_title");
	JOptionPane.showMessageDialog(null, errorMessage, windowTitle,
				      JOptionPane.ERROR_MESSAGE);
    }
}

}
