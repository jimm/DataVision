package jimm.datavision.gui.cmd;
import jimm.datavision.gui.EditWinWidget;
import jimm.util.I18N;

/**
 * A command for changing a field widget's field's name.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class WidgetRenameCommand extends CommandAdapter {

protected EditWinWidget eww;	// Yuck!
protected String oldName;
protected String newName;

public WidgetRenameCommand(EditWinWidget eww, String oldName, String newName) {
    super(I18N.get("WidgetRenameCommand.name"));
    this.eww = eww;
    this.oldName = oldName;
    this.newName = newName;
}

public void perform() {
    eww.setWidgetName(newName);
}

public void undo() {
    eww.setWidgetName(oldName);
}

}
