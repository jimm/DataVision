package jimm.datavision.gui.cmd;
import jimm.datavision.Selectable;
import jimm.datavision.source.Query;
import jimm.datavision.gui.SortWinListItem;
import jimm.util.I18N;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A command for changing the sort orders in a {@link Query}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SortEditCommand extends CommandAdapter {

protected static final int NO_CHANGE = 0;
protected static final int ONLY_SORTING_CHANGE = 1;
protected static final int DRASTIC_CHANGE = 2;

protected Query query;
protected Collection newSortItems;
protected Collection oldSortItems;

public SortEditCommand(Query query, Collection sortItems) {
    super(I18N.get("SortEditCommand.name"));
    this.query = query;
    this.newSortItems = sortItems;

    // Create list of current sorts
    oldSortItems = new ArrayList();
    for (Iterator iter = query.sortedSelectables(); iter.hasNext(); ) {
	Selectable g = (Selectable)iter.next();
	oldSortItems.add(new SortWinListItem(g, query.sortOrderOf(g)));
    }
}

public void perform() {
    setSorts(newSortItems);
}

public void undo() {
    setSorts(oldSortItems);
}

protected void setSorts(Collection itemList) {
    query.clearSorts();
    for (Iterator iter = itemList.iterator(); iter.hasNext(); ) {
	SortWinListItem item = (SortWinListItem)iter.next();
	query.addSort(item.getSelectable(), item.sortsAscending()
		      ? Query.SORT_ASCENDING : Query.SORT_DESCENDING);
    }
}

}
