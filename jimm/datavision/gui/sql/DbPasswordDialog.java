package jimm.datavision.gui.sql;
import jimm.datavision.gui.EditFieldLayout;
import jimm.datavision.gui.FocusSetter;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * A modal dialog used to ask the user for a database password.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DbPasswordDialog extends JDialog implements ActionListener {

protected static final int FIELD_COLUMNS = 20;

protected String username;
protected String password;
protected JTextField usernameField;
protected JPasswordField passwordField;

/**
 * Constructor.
 *
 * @param parent frame with which this dialog should be associated
 * @param dbName database name
 * @param userName database user name
 */
public DbPasswordDialog(Frame parent, String dbName, String userName) {
    super(parent, I18N.get("DbPasswordDialog.title"), true); // Modal
    username = userName == null ? "" : userName;
    buildWindow(dbName);
    pack();
    setVisible(true);
}

/**
 * Returns username (or <code>null</code> if user hit Cancel).
 *
 * @return username (or <code>null</code> if user cancelled)
 */
public String getUserName() { return username; }

/**
 * Returns password (or <code>null</code> if user hit Cancel).
 *
 * @return password (or <code>null</code> if user cancelled)
 */
public String getPassword() { return password; }

protected void buildWindow(String dbName) {
    getContentPane().setLayout(new BorderLayout());

    EditFieldLayout efl = new EditFieldLayout();
    efl.addLabel(I18N.get("DbPasswordDialog.database"), dbName);
    efl.setBorder(20);
    usernameField = efl.addTextField(I18N.get("DbPasswordDialog.user_name"),
				     username, FIELD_COLUMNS);
    passwordField = efl.addPasswordField(I18N.get("DbPasswordDialog.password"),
					 FIELD_COLUMNS);

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

    new FocusSetter(passwordField);
}

/**
 * Handles the buttons.
 *
 * @param e action event
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("GUI.ok").equals(cmd)) {
	username = usernameField.getText();
	password = new String(passwordField.getPassword());
	dispose();
    }
    else if (I18N.get("GUI.cancel").equals(cmd)) {
	dispose();
    }
}

}
