package jimm.datavision.gui;
import jimm.datavision.Selectable;

/**
 * Used in lists to hold and display database column and sort order.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 * @see TwoListWin
 * @see GroupWin
 * @see SortWin
 */
abstract class TLWListItem implements Comparable<TLWListItem> {

Selectable selectable;
int sortOrder;

/**
 * Constructor.
 *
 * @param selectable a selectable thingie
 * @param sortOrder a subclass-specific value for sort order
 */
TLWListItem(Selectable selectable, int sortOrder) {
    this.selectable = selectable;
    this.sortOrder = sortOrder;
}

public abstract boolean sortsAscending();

public Selectable getSelectable() { return selectable; }

public int getSortOrder() { return sortOrder; }

public int compareTo(TLWListItem o) {
    String otherName = o.selectable.getDisplayName();
    return selectable.getDisplayName().compareTo(otherName);
}

public String toString() { return selectable.getDisplayName(); }

}
