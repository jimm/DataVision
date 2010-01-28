package jimm.datavision.gui.cmd;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.datavision.gui.Clipboard;
import jimm.util.I18N;
import java.util.*;

public class PasteCommand extends CommandAdapter {

// ================================================================
static class PasteInfo {
HashSet fieldWidgets;
SectionResizeCommand sectionResizeCommand;

PasteInfo(SectionWidget sw) {
    fieldWidgets = new HashSet();
    sectionResizeCommand = new SectionResizeCommand(sw);
}
void add(FieldWidget fw) { fieldWidgets.add(fw); }
Iterator fieldWidgets() { return fieldWidgets.iterator(); }
}
// ================================================================

protected Designer designer;
/** Maps section widgets to sets of field widgets contained within them. */
protected HashMap sectionFields;

/**
 * Constructor.
 */
public PasteCommand(Designer designer) {
    super(I18N.get("PasteCommand.name"));

    this.designer = designer;
}

public void perform() {
    Iterator iter = ((List)Clipboard.instance().getContents()).iterator();
    while (iter.hasNext())
	((Pasteable)iter.next()).paste(designer);
}

public void undo() {
    Iterator iter = ((List)Clipboard.instance().getContents()).iterator();
    while (iter.hasNext())
	((Pasteable)iter.next()).undo(designer);
}

}
