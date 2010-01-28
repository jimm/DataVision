package jimm.datavision.gui.cmd;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.gui.FieldWidget;
import jimm.util.I18N;

/**
 * Resizes a single field by comparing it with another field and copying
 * one of its dimensions.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldResizeCommand extends CommandAdapter {

protected FieldWidget fw;
protected int which;
protected Field prototype;
protected Rectangle origBounds;
protected SectionResizeCommand sectionResizeCommand;

public FieldResizeCommand(FieldWidget fw, int which, Field prototype) {
    super(I18N.get("FieldResizeCommand.name"));

    this.fw = fw;
    this.which = which;
    this.prototype = prototype;
    origBounds = new Rectangle(fw.getField().getBounds()); // Make a copy
    sectionResizeCommand = new SectionResizeCommand(fw.getSectionWidget());
}

public void perform() {
    fw.size(which, prototype);

    fw.getSectionWidget().growToFit();
    sectionResizeCommand.perform();
}

public void undo() {
    fw.getField().getBounds().setBounds(origBounds);
    sectionResizeCommand.undo();
}

}
