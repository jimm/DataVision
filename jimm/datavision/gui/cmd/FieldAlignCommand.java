package jimm.datavision.gui.cmd;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.gui.FieldWidget;
import jimm.util.I18N;

/**
 * Aligns a single field with another.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldAlignCommand extends CommandAdapter {

protected FieldWidget fw;
protected int which;
protected Field prototype;
protected Rectangle origBounds;

public FieldAlignCommand(FieldWidget fw, int which, Field prototype) {
    super(I18N.get("FieldAlignCommand.name"));

    this.fw = fw;
    this.which = which;
    this.prototype = prototype;
    origBounds = new Rectangle(fw.getField().getBounds()); // Make a copy
}

public void perform() {
    fw.align(which, prototype);
}

public void undo() {
    fw.getField().getBounds().setBounds(origBounds);
}

}
