package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.util.I18N;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * The main GUI {@link Report} design window.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DesignWin extends Designer {

/**
 * Constructor. Reads the named report file or, if it's <code>null</code>,
 * creates a new, empty report.
 *
 * @param f an XML report file; may be <code>null</code>
 */
public DesignWin(File f) {
    this(f, null);
}

/**
 * Constructor. Reads the named report file or, if it's <code>null</code>,
 * creates a new, empty report.
 *
 * @param f an XML report file; may be <code>null</code>
 * @param databasePassword string to give to report; OK if it's
 * <code>null</code>
 */
public DesignWin(File f, String databasePassword) {
    super(f, databasePassword, null,
	  new JFrame(I18N.get("DesignWin.title")));

    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    // Make sure we close the window when the user asks to close the window.
    // Update menu items when this window is activated.
    frame.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) { maybeClose(); }
	public void windowActivated(WindowEvent e) { enableMenuItems(); }
    });

    frame.pack();
    frame.show();
}

/**
 * Builds the window components.
 */
protected void buildWindow() {
    super.buildWindow();
}

}
