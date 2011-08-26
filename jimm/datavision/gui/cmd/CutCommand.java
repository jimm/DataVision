package jimm.datavision.gui.cmd;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.Clipboard;
import jimm.util.I18N;
import java.util.ArrayList;

/**
 * Cuts (copies to the clipboard then deletes) a list of field widgets.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public class CutCommand extends DeleteCommand {

public CutCommand(Designer designer, ArrayList<FieldWidget> selectedFields) {
    super(designer, selectedFields, I18N.get("CutCommand.name"));
}

public void perform() {
    ArrayList<Pasteable> pasteables = new ArrayList<Pasteable>();
    for (FieldWidget fw : fieldWidgets)
	pasteables.add(new FieldClipping(fw));
    Clipboard.instance().setContents(pasteables);

    super.perform();
}

/**
 * Calls <code>super.perform</code> because we don't want to copy
 * anything to the clipboard a second time.
 */
public void redo() {
    super.perform();
}

}
