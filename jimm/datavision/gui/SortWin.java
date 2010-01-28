package jimm.datavision.gui;
import jimm.datavision.Report;
import jimm.datavision.UserColumn;
import jimm.datavision.Selectable;
import jimm.datavision.source.*;
import jimm.datavision.gui.cmd.SortEditCommand;
import jimm.util.I18N;
import java.awt.event.*;
import java.util.*;

/**
 * This dialog is used for editing the report query's list of sort
 * orders.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SortWin extends TwoListWin {

protected Query query;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the...um...I forgot
 */
public SortWin(Designer designer, Report report) {
    super(designer, I18N.get("SortWin.title"), "SortChangeCommand.name",
	  "SortWin.right_box_title", report);
}

/**
 * Fills the unsorted and sorted columns lists. Excludes colums that are
 * used in report groups.
 */
protected void fillListModels() {
    // Since this method is called from within our superclass ctor, before
    // the query gets initialized, we have to grab it here.
    DataSource ds = report.getDataSource();
    query = ds.getQuery();

    // Add user cols used in report that are not used by a group.
    Iterator iter;
    for (iter = report.userColumns(); iter.hasNext(); ) {
	UserColumn uc = (UserColumn)iter.next();
	if (!report.isUsedBySomeGroup(uc)) // Ignore group user groups
	    addToModel(uc);
    }

    // Add columns used in report that are not used by a group.
    for (iter = ds.columnsInTablesUsedInReport(); iter.hasNext(); ) {
	Column col = (Column)iter.next();
	if (!report.isUsedBySomeGroup(col)) // Ignore group columns
	    addToModel(col);
    }
}

protected void addToModel(Selectable g) {
    int order = query.sortOrderOf(g);
    SortWinListItem item = new SortWinListItem(g, order);
    if (order == Query.SORT_UNDEFINED)
	leftModel.add(item);
    else
	rightModel.addElement(item);
}

/**
 * Handles ascending and descending sort order buttons.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals(I18N.get("GUI.ascending")))
	((SortWinListItem)rightList.getSelectedValue()).sortOrder = 'a';
    else if (cmd.equals(I18N.get("GUI.descending")))
	((SortWinListItem)rightList.getSelectedValue()).sortOrder = 'd';
    else
	super.actionPerformed(e);
}

protected void doSave() {
    // Turn the model's vector into a collection
    ArrayList items = new ArrayList();
    for (Enumeration e = rightModel.elements(); e.hasMoreElements(); )
	items.add(e.nextElement());

    SortEditCommand cmd = new SortEditCommand(query, items);
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
