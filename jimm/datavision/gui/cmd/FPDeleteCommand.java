package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldPickerTree;
import jimm.datavision.gui.FPLeafInfo;
import jimm.util.I18N;
import javax.swing.tree.*;

/**
 * Deletes something from the field picker tree.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FPDeleteCommand extends CommandAdapter {

protected Report report;
protected Designer designer;
protected FieldPickerTree tree;
protected DefaultMutableTreeNode leafNode;
protected MutableTreeNode parentNode;
protected FPLeafInfo info;

/**
 * Constructor.
 */
public FPDeleteCommand(Report report, Designer designer,
		       FieldPickerTree tree, DefaultMutableTreeNode node)
{
    this(report, designer, tree, node, I18N.get("FPDeleteCommand.name"));
}

/**
 * The delegated constructor.
 */
protected FPDeleteCommand(Report report, Designer designer,
			  FieldPickerTree tree, DefaultMutableTreeNode node,
			  String name)
{
    super(name);
    this.report = report;
    this.designer = designer;
    this.tree = tree;
    leafNode = node;
    parentNode = (MutableTreeNode)node.getParent();
    Object obj = node.getUserObject();
    if (obj instanceof FPLeafInfo)
	info = (FPLeafInfo)obj;
}

public void perform() {
    if (info != null) {
	report.remove(info.getLeaf());
	tree.removeCurrentNode();
    }
}

public void undo() {
    if (info != null) {
	report.add(info.getLeaf());

	((DefaultTreeModel)tree.getModel())
	    .insertNodeInto(leafNode, parentNode, parentNode.getChildCount());

	// Make leaf visible and make it the current selection
	TreePath path = new TreePath(leafNode.getPath());
	tree.scrollPathToVisible(path);
	tree.setSelectionPath(path);
    }
}

}
