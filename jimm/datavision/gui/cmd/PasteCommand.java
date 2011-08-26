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
HashSet<FieldWidget> fieldWidgets;
SectionResizeCommand sectionResizeCommand;

PasteInfo(SectionWidget sw) {
    fieldWidgets = new HashSet<FieldWidget>();
    sectionResizeCommand = new SectionResizeCommand(sw);
}
void add(FieldWidget fw) { fieldWidgets.add(fw); }
Iterator<FieldWidget> fieldWidgets() { return fieldWidgets.iterator(); }
}
// ================================================================

protected Designer designer;

/**
 * Constructor.
 */
public PasteCommand(Designer designer) {
    super(I18N.get("PasteCommand.name"));

    this.designer = designer;
}

@SuppressWarnings("unchecked")
public void perform() {
    for (Pasteable p : (List<Pasteable>)Clipboard.instance().getContents())
	p.paste(designer);
}

@SuppressWarnings("unchecked")
public void undo() {
    for (Pasteable p : (List<Pasteable>)Clipboard.instance().getContents())
	p.undo(designer);
}

}
