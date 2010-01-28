package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.Clipboard;
import jimm.datavision.gui.FieldPickerTree;
import jimm.util.I18N;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Cuts (copies to the clipboard then deletes) something from a field
 * picker tree.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FPCutCommand extends FPDeleteCommand {

public FPCutCommand(Report report, Designer designer, FieldPickerTree tree,
		    DefaultMutableTreeNode node)
{
    super(report, designer, tree, node, I18N.get("FPCutCommand.name"));
}

public void perform() {
    Clipboard.instance().setContents(info);
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
