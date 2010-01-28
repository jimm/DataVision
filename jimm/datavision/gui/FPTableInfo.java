package jimm.datavision.gui;
import jimm.datavision.source.Table;
import jimm.datavision.source.Column;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Iterator;

/**
 * An internal table node for the {@link FieldPickerTree}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */

public class FPTableInfo {

protected Table table;
protected Designer designer;
protected DefaultMutableTreeNode tableNode;

FPTableInfo(Table table, Designer designer) {
    this.table = table;
    this.designer = designer;
}

void setTableNode(DefaultMutableTreeNode tableNode) {
    this.tableNode = tableNode;
}

public String toString() { return table.getName(); }

public void loadColumns() {
    if (tableNode == null)
	return;

    // Remove table node's existing dummy child node.
    tableNode.remove(0);

    // Add columns.
    for (Iterator iter = table.columns(); iter.hasNext(); ) {
	Column column = (Column)iter.next();
	ColumnInfo info = new ColumnInfo(column, designer);
	tableNode.add(new DefaultMutableTreeNode(info, false));
    }

    // A signal that we have loaded the columns.
    tableNode = null;
}

}
