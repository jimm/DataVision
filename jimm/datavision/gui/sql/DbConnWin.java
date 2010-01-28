package jimm.datavision.gui.sql;
import jimm.datavision.*;
import jimm.datavision.gui.*;
import jimm.datavision.source.sql.Database;
import jimm.datavision.gui.cmd.DbConnCommand;
import jimm.util.StringUtils;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

/**
 * A database connection editing dialog box. The user can either enter
 * separate values or copy values from an existing report XML file.
 *
 * @see DbConnReader
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DbConnWin extends EditWin implements ActionListener {

protected static final int TEXT_FIELD_COLS = 32;

protected Report report;
protected JTextField driverClassNameField;
protected JTextField connInfoField;
protected JTextField dbNameField;
protected JTextField userNameField;
protected JPasswordField passwordField;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the report
 * @param modal if <code>true</code>, this window is modal
 */
public DbConnWin(Designer designer, Report report, boolean modal) {
    super(designer, I18N.get("DbConnWin.title"), "DbConnCommand.name", modal);

    this.report = report;

    buildWindow();
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 */
protected void buildWindow() {
    JPanel editorPanel = buildEditor();

    // OK, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add values and buttons to window
    getContentPane().add(editorPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    new FocusSetter(driverClassNameField);
}

protected JPanel buildEditor() {
    Database db = (Database)report.getDataSource();
    EditFieldLayout efl = new EditFieldLayout();
    efl.setBorder(20);

    driverClassNameField =
	efl.addTextField(I18N.get("DbConnWin.driver_class_name"), db == null
			 ? "" : db.getDriverClassName(), TEXT_FIELD_COLS);
    connInfoField = efl.addTextField(I18N.get("DbConnWin.connection_info"),
				     db == null ? "" : db.getConnectionInfo(),
				     TEXT_FIELD_COLS);
    dbNameField = efl.addTextField(I18N.get("DbConnWin.database_name"),
				   db == null ? "" : db.getName(),
				   TEXT_FIELD_COLS);
    userNameField = efl.addTextField(I18N.get("DbConnWin.user_name"),
				     db == null ? "" : db.getUserName(),
				     TEXT_FIELD_COLS);
    String password = (db == null ? "" : db.getPassword());
    if (password == null) password = "";
    passwordField = efl.addPasswordField(I18N.get("DbConnWin.password"),
					 password, TEXT_FIELD_COLS);

    // Click to copy info from another report
    JButton copyButton = new JButton(I18N.get("DbConnWin.copy_settings"));
    copyButton.addActionListener(this);
    JPanel copyPanel = new JPanel();
    copyPanel.add(copyButton);
    efl.add(null, copyPanel);

    return efl.getPanel();
}

protected void fillEditFields() {
    Database db = (Database)report.getDataSource();
    if (db == null) {
	driverClassNameField.setText("");
	connInfoField.setText("");
	dbNameField.setText("");
	userNameField.setText("");
    }
    else {
	driverClassNameField.setText(db.getDriverClassName());
	connInfoField.setText(db.getConnectionInfo());
	dbNameField.setText(db.getName());
	userNameField.setText(db.getUserName());
    }
    passwordField.setText("");
}

/**
 * Handles the "Copy Settings..." button.
 *
 * @param e action event
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("DbConnWin.copy_settings").equals(cmd)) {
	JFileChooser chooser = Designer.getChooser();
  Designer.setPrefsDir(chooser,null);
	int returnVal = chooser.showOpenDialog(this);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    DbConnReader reader = new DbConnReader();
	    try {
		reader.read(chooser.getSelectedFile());
		driverClassNameField.setText(reader.getDriverClassName());
		connInfoField.setText(reader.getConnectionInfo());
		dbNameField.setText(reader.getDbName());
		userNameField.setText(reader.getUserName());
	    }
	    catch (Exception ex) {
		ErrorHandler.error(I18N.get("DbConnWin.copy_error"), ex);
	    }
	}
    }
    else
	super.actionPerformed(e);
}

protected void doSave() {
    DbConnCommand cmd =
	new DbConnCommand(report,
			  StringUtils.nullOrTrimmed(driverClassNameField.getText()),
			  StringUtils.nullOrTrimmed(connInfoField.getText()),
			  StringUtils.nullOrTrimmed(dbNameField.getText()),
			  StringUtils.nullOrTrimmed(userNameField.getText()),
			  StringUtils.nullOrTrimmed(new String(passwordField.getPassword())));
    cmd.perform();
    commands.add(cmd);
}

protected void doRevert() {
    fillEditFields();
}

}
