package jimm.datavision.gui;
import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Implements a sorted list model suitable for use with a <code>JList</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SortedListModel extends AbstractListModel {

ArrayList list;

public SortedListModel() {
    list = new ArrayList();
}

public void add(Comparable obj) {
    int size = list.size();
    if (size == 0) {
	list.add(obj);
	fireIntervalAdded(this, 0, 0);
	return;
    }

    // Add in list, sorted
    for (int i = 0; i < list.size(); ++i) {
	if (obj.compareTo(list.get(i)) < 0) {
	    list.add(i, obj);
	    fireIntervalAdded(this, i , i);
	    return;
	}
    }

    list.add(obj);
    fireIntervalAdded(this, size, size);
}

public void remove(int index) {
    list.remove(index);
    fireIntervalRemoved(this, index, index);
}

public void removeAllElements() {
    int size = list.size();
    if (size > 0) {
	list.clear();
	fireIntervalRemoved(this, 0, size - 1);
    }
}

public Iterator iterator() { return list.iterator(); }

public int getSize() { return list.size(); }

public Object getElementAt(int index) { return list.get(index); }

public Object[] toArray() { return list.toArray(); }

}
