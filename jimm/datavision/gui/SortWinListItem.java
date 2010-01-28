package jimm.datavision.gui;
import jimm.datavision.Selectable;
import jimm.datavision.source.Query;

/**
 * Sort win list items are used by the sort editing window {@link SortWin} and
 * the {@link jimm.datavision.gui.cmd.SortEditCommand} to remember a sort
 * column and its sort order.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SortWinListItem extends TLWListItem {

public SortWinListItem(Selectable selectable, int sortOrder) {
    super(selectable, sortOrder == Query.SORT_DESCENDING ? 'd' : 'a');
}

public boolean sortsAscending() { return sortOrder == 'a'; }

}
