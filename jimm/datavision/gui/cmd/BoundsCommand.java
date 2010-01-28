package jimm.datavision.gui.cmd;
import jimm.datavision.field.Rectangle;
import jimm.datavision.field.Field;
import jimm.util.I18N;

/**
 * A command for changing a field's bounds.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class BoundsCommand extends CommandAdapter {

protected Field field;
protected Rectangle origBounds;
protected Rectangle newBounds;

public BoundsCommand(Field f, Rectangle bounds) {
    super(I18N.get("BoundsCommand.name"));
    field = f;
    origBounds = new Rectangle(field.getBounds());
    newBounds = bounds;
}

public void perform() {
    field.getBounds().setBounds(newBounds);
}

public void undo() {
    field.getBounds().setBounds(origBounds);
}

}
