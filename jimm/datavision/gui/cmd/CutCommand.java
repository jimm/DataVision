package jimm.datavision.gui.cmd;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.Clipboard;
import jimm.util.I18N;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Cuts (copies to the clipboard then deletes) a list of field widgets.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class CutCommand extends DeleteCommand {

public CutCommand(Designer designer, ArrayList selectedFields) {
    super(designer, selectedFields, I18N.get("CutCommand.name"));
}

public void perform() {
    ArrayList pasteables = new ArrayList();
    for (Iterator iter = fieldWidgets.iterator(); iter.hasNext(); )
	pasteables.add(new FieldClipping((FieldWidget)iter.next()));
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
