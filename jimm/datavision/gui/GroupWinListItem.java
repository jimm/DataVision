package jimm.datavision.gui;
import jimm.datavision.Group;
import jimm.datavision.Selectable;

/**
 * Group win list items are used by the group editing window {@link GroupWin}
 * and the {@link jimm.datavision.gui.cmd.GroupEditCommand} to remember a
 * group and its sort order.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class GroupWinListItem extends TLWListItem {

Group group;

public GroupWinListItem(Selectable selectable, Group group) {
    super(selectable,
	  group == null ? Group.SORT_ASCENDING : group.getSortOrder());
    this.group = group;		// May be null
}

public Group getGroup() { return group; }

public boolean sortsAscending() { return sortOrder == Group.SORT_ASCENDING; }

}
