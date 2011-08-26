package jimm.datavision.gui.cmd;
import jimm.datavision.Selectable;
import jimm.datavision.source.Query;
import jimm.datavision.gui.SortWinListItem;
import jimm.util.I18N;
import java.util.Collection;
import java.util.ArrayList;

/**
 * A command for changing the sort orders in a {@link Query}.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public class SortEditCommand extends CommandAdapter {

protected static final int NO_CHANGE = 0;
protected static final int ONLY_SORTING_CHANGE = 1;
protected static final int DRASTIC_CHANGE = 2;

protected Query query;
protected Collection<SortWinListItem> newSortItems;
protected Collection<SortWinListItem> oldSortItems;

public SortEditCommand(Query query, Collection<SortWinListItem> sortItems) {
    super(I18N.get("SortEditCommand.name"));
    this.query = query;
    this.newSortItems = sortItems;

    // Create list of current sorts
    oldSortItems = new ArrayList<SortWinListItem>();
    for (Selectable s : query.sortedSelectables())
	oldSortItems.add(new SortWinListItem(s, query.sortOrderOf(s)));
}

public void perform() {
    setSorts(newSortItems);
}

public void undo() {
    setSorts(oldSortItems);
}

protected void setSorts(Collection<SortWinListItem> itemList) {
    query.clearSorts();
    for (SortWinListItem item : itemList)
	query.addSort(item.getSelectable(), item.sortsAscending()
		      ? Query.SORT_ASCENDING : Query.SORT_DESCENDING);
}

}
