package jimm.datavision.gui.cmd;
import jimm.datavision.Nameable;
import jimm.util.I18N;

/**
 * A command for changing a {@link Nameable} object's name.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NameableRenameCommand extends CommandAdapter {

protected Nameable nameable;
protected String oldName;
protected String newName;

public NameableRenameCommand(Nameable nameable, String oldName, String newName)
{
    super(I18N.get("NameableRenameCommand.name"));
    this.nameable = nameable;
    this.oldName = oldName;
    this.newName = newName;
}

public void perform() {
    nameable.setName(newName);
}

public void undo() {
    nameable.setName(oldName);
}

}
