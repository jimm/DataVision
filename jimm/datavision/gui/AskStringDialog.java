package jimm.datavision.gui;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * A modal dialog used to ask the user for a simple string like a
 * formula or parameter name.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class AskStringDialog extends JDialog implements ActionListener {

protected static final int TEXT_FIELD_COLUMNS = 24;

protected String string;
protected JTextField stringField;

/**
 * Constructor.
 *
 * @param frame parent frame
 * @param title window title
 * @param label string field label
 */
public AskStringDialog(java.awt.Frame frame, String title, String label) {
    this(frame, title, label, "");
}

/**
 * Constructor.
 *
 * @param frame parent frame
 * @param title window title
 * @param label string field label
 * @param initialString initial string value
 */
public AskStringDialog(java.awt.Frame frame, String title, String label,
		       String initialString) {
    super((java.awt.Frame)null, title, true); // Modal
    buildWindow(title, label, initialString);
    pack();
    setVisible(true);
}

/**
 * Returns string (or <code>null</code> if user hit Cancel).
 *
 * @return string (or <code>null</code> if user cancelled)
 */
public String getString() { return string; }

/**
 * Builds window GUI.
 *
 * @param title window title
 * @param labelString string field label
 * @param initialString initial value of string
 */
protected void buildWindow(String title, String labelString,
			   String initialString)
{
    getContentPane().setLayout(new BorderLayout());

    EditFieldLayout efl = new EditFieldLayout();
    stringField =
	efl.addTextField(labelString, initialString, TEXT_FIELD_COLUMNS);

    // OK and Cancel buttons
    JPanel buttonPanel = new JPanel();
    JButton button;

    buttonPanel.add(button = new JButton(I18N.get("GUI.ok")));
    button.addActionListener(this);
    button.setDefaultCapable(true);
    getRootPane().setDefaultButton(button);

    buttonPanel.add(button = new JButton(I18N.get("GUI.cancel")));
    button.addActionListener(this);

    getContentPane().add(efl.getPanel(), BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	    dispose();
	}
	});

    new FocusSetter(stringField);
}

/**
 * Handles the buttons.
 *
 * @param e action event
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("GUI.ok").equals(cmd)) {
	string = new String(stringField.getText());
	dispose();
    }
    else if (I18N.get("GUI.cancel").equals(cmd)) {
	dispose();
    }
}

}
