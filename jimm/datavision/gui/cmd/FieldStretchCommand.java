package jimm.datavision.gui.cmd;
import jimm.datavision.field.Rectangle;
import jimm.datavision.gui.FieldWidget;
import jimm.util.I18N;

/**
 * After stretching a field using the mouse, this command lets us undo and
 * redo the size change.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldStretchCommand extends CommandAdapter {

protected FieldWidget fw;
protected Rectangle origBounds;
protected Rectangle newBounds;

public FieldStretchCommand(FieldWidget fw, Rectangle origBounds) {
    super(I18N.get("FieldStretchCommand.name"));

    this.fw = fw;
    this.origBounds = origBounds;
}

public void perform() {
    // Remember the field's new bounds.
    newBounds = new Rectangle(fw.getField().getBounds()); // Make a copy
}

public void undo() {
    fw.getField().getBounds().setBounds(origBounds);
}

public void redo() {
    fw.getField().getBounds().setBounds(newBounds);
}

}
