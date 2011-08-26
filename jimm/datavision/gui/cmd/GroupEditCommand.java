package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Group;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.GroupWinListItem;
import jimm.util.I18N;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A command for changing a report's {@link Group}s.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public class GroupEditCommand extends CommandAdapter {

protected static final int NO_CHANGE = 0;
protected static final int ONLY_SORTING_CHANGE = 1;
protected static final int DRASTIC_CHANGE = 2;

protected Report report;
protected Designer designer;
protected Collection<GroupWinListItem> newGroupItems;
protected Collection<GroupWinListItem> oldGroupItems;

public GroupEditCommand(Report r, Designer designer, Collection<GroupWinListItem> groupItems) {
    super(I18N.get("GroupEditCommand.name"));
    report = r;
    this.designer = designer;
    this.newGroupItems = groupItems;

    // Create list of current groups
    oldGroupItems = new ArrayList<GroupWinListItem>();
    for (Group group : report.groups())
	oldGroupItems.add(new GroupWinListItem(group.getSelectable(), group));
}

public void perform() {
    setGroups(oldGroupItems, newGroupItems);
}

public void undo() {
    setGroups(newGroupItems, oldGroupItems);
}

protected void setGroups(Collection<GroupWinListItem> fromList, Collection<GroupWinListItem> toList) {
    switch (whatChanged(fromList, toList)) {
    case ONLY_SORTING_CHANGE:
	for (GroupWinListItem item : toList)
	    item.getGroup().setSortOrder(item.getSortOrder());
	break;
    case DRASTIC_CHANGE:
	report.removeAllGroups();
	for (GroupWinListItem item : toList) {
	    Group g = item.getGroup();
	    if (g == null)
		g = Group.create(report, item.getSelectable());
	    g.setSortOrder(item.getSortOrder());
	    report.addGroup(g);
	}
	designer.rebuildGroups();
	break;
    case NO_CHANGE:
	break;
    }
}

/**
 * Determines the severity of the difference between the report and what we
 * have now: <code>NO_CHANGE</code>, <code>ONLY_SORTING_CHANGE</code>, or
 * <code>DRASTIC_CHANGE</code>.
 *
 * @param fromList the list of groups before the pending change
 * @param toList the list of groups after the pending change
 * @return one of <code>NO_CHANGE</code>, <code>ONLY_SORTING_CHANGE</code>,
 * or <code>DRASTIC_CHANGE</code>
 */
protected int whatChanged(Collection<GroupWinListItem> fromList, Collection<GroupWinListItem> toList) {
    if (fromList.size() != toList.size())
	return DRASTIC_CHANGE;

    int change = NO_CHANGE;
    Iterator<GroupWinListItem> fromIter = fromList.iterator();
    Iterator<GroupWinListItem> toIter = toList.iterator();
    while (fromIter.hasNext()) {
	GroupWinListItem fromItem = (GroupWinListItem)fromIter.next();
	GroupWinListItem toItem = (GroupWinListItem)toIter.next();
	if (fromItem.getGroup() != toItem.getGroup())
	    return DRASTIC_CHANGE;
	if (fromItem.sortsAscending() != toItem.sortsAscending())
	    change = ONLY_SORTING_CHANGE;
    }
    return change;		// Either NO_CHANGE or ONLY_SORTING_CHANGE
}

}
