package jimm.datavision.gui.cmd;
import jimm.datavision.UserColumn;
import jimm.util.I18N;

/**
 * A command for changing a {@link UserColumn}'s code text.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class UserColumnEditCommand extends CommandAdapter {

protected UserColumn userColumn;
protected String newExpression;
protected String oldExpression;

public UserColumnEditCommand(UserColumn userColumn, String expression) {
    super(I18N.get("UserColumnEditCommand.name"));
    this.userColumn = userColumn;
    newExpression = expression;
    oldExpression = userColumn.getExpression();
}

public void perform() {
    userColumn.setEditableExpression(newExpression);
}

public void undo() {
    userColumn.setEditableExpression(oldExpression);
}

}
