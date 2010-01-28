package jimm.datavision.gui.cmd;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.datavision.gui.PreMoveInfo;
import jimm.util.I18N;

/**
 * Moves a single field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldMoveCommand extends CommandAdapter {

protected FieldWidget fw;
protected Rectangle newBounds;
protected SectionWidget newSectionWidget;
protected SectionResizeCommand sectionResizeCommand;
protected PreMoveInfo preMoveInfo;

public FieldMoveCommand(FieldWidget fw, SectionWidget sw) {
    super(I18N.get("FieldMoveCommand.name"));

    this.fw = fw;
    preMoveInfo = fw.getPreMoveInfo();
    newBounds = new Rectangle(fw.getField().getBounds()); // Make a copy
    newSectionWidget = sw;
    sectionResizeCommand = new SectionResizeCommand(sw);
}

public void perform() {
    fw.getField().getBounds().setBounds(newBounds);
    fw.putDown(newSectionWidget);
    sectionResizeCommand.perform();
}

public void undo() {
    Field f = fw.getField();
    SectionWidget sw = fw.getSectionWidget();
    f.getBounds().setBounds(preMoveInfo.origBounds); // Move to original bounds
    fw.moveToSection(preMoveInfo.sectionWidget); // Move to original section
    sectionResizeCommand.undo();
    if (sw != preMoveInfo.sectionWidget)
	sw.repaint();
}

}
