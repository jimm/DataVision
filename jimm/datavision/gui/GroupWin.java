package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.gui.cmd.GroupEditCommand;
import jimm.util.I18N;
import java.awt.event.*;
import java.util.*;

/**
 * This dialog is used for editing report groups.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class GroupWin extends TwoListWin {

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the...um...I forgot
 */
public GroupWin(Designer designer, Report report) {
    super(designer, I18N.get("GroupWin.title"), "GroupChangeCommand.name",
	  "GroupWin.right_box_title", report);
}

protected void fillListModels() {
    Iterator iter;

    // First find and add groups in order.
    for (iter = report.groups(); iter.hasNext(); ) {
	Group group = (Group)iter.next();
	rightModel.addElement(new GroupWinListItem(group.getSelectable(),
						   group));
    }

    // Now iterate through all user cols and columns in tables used by the
    // report, adding to the left list those that are not already grouped
    // to the left list.
    for (iter = report.userColumns(); iter.hasNext(); )
	addToModel((Selectable)iter.next());
    for (iter = report.getDataSource().columnsInTablesUsedInReport();
	 iter.hasNext(); )
	addToModel((Selectable)iter.next());
}

protected void addToModel(Selectable s) {
    Group group = report.findGroup(s);
    if (group == null)
	leftModel.add(new GroupWinListItem(s, group));
}

/**
 * Handles ascending and descending sort order buttons.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals(I18N.get("GUI.ascending")))
	((GroupWinListItem)rightList.getSelectedValue()).sortOrder =
	    Group.SORT_ASCENDING;
    else if (cmd.equals(I18N.get("GUI.descending")))
	((GroupWinListItem)rightList.getSelectedValue()).sortOrder =
	    Group.SORT_DESCENDING;
    else
	super.actionPerformed(e);
}

protected void doSave() {
    // Turn the model's vector into a collection
    ArrayList items = new ArrayList();
    for (Enumeration e = rightModel.elements(); e.hasMoreElements(); )
	items.add(e.nextElement());

    GroupEditCommand cmd = new GroupEditCommand(report, designer, items);
    cmd.perform();
    commands.add(cmd);
}

protected void doRevert() {
    // Rebuild list models
    leftModel.removeAllElements();
    rightModel.removeAllElements();
    fillListModels();
    adjustButtons();
}

}
