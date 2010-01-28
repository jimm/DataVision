package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.gui.cmd.UserColumnEditCommand;
import jimm.util.I18N;
import java.util.Observable;
import java.util.Observer;

/**
 * This dialog is for editing {@link UserColumn} code.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see UserColumnWidget
 * @see UserColumnEditCommand
 */
public class UserColumnWin extends CodeEditorWin implements Observer {

protected UserColumn userColumn;

/**
 * Constructor.
 *
 * @param designer the design window to which this dialog belongs
 * @param report the report
 * @param userColumn the userColumn whose text needs editing
 */
public UserColumnWin(Designer designer, Report report, UserColumn userColumn)
{
    super(designer, report, userColumn.getEditableExpression(),
	  I18N.get("UserColumnWin.title_prefix") + ' ' + userColumn.getName(),
	  "UserColumnWin.error_unchanged", "UserColumnWin.error_title");
    this.userColumn = userColumn;
    userColumn.addObserver(this);
}

protected void finalize() throws Throwable {
    userColumn.deleteObserver(this);
    super.finalize();
}

public void update(Observable o, Object arg) {
    setTitle(I18N.get("UserColumnWin.title_prefix") + ' '
	     + userColumn.getName());
    codeField.setText(userColumn.getEditableExpression());
}

public void save(String text) {
    userColumn.deleteObserver(this);
    command = new UserColumnEditCommand(userColumn, text);
}

}
