package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import java.util.Iterator;
import javax.swing.*;

/**
 * A single-choice string list inquisitor knows how to display and control
 * the widgets needed to ask a user for a single string parameter value from
 * a list.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class ListStringInq extends Inquisitor {

protected JList list;

ListStringInq(Parameter param, boolean allowMultipleSelection) {
    super(param);

    // Build GUI
    Box box = Box.createVerticalBox();

    DefaultListModel model = new DefaultListModel();
    list = new JList(model);
    list.setSelectionMode(allowMultipleSelection
			  ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
			  : ListSelectionModel.SINGLE_SELECTION);
    box.add(list);

    panel.add(box);

    // Copy default value into "real" value
    for (Iterator iter = parameter.defaultValues(); iter.hasNext(); )
	model.addElement(iter.next());

    if (!allowMultipleSelection)
	list.setSelectedIndex(0); // Select first item
}

void copyGUIIntoParam() {
    int[] is = list.getSelectedIndices();
    parameter.removeValues();
    for (int i = 0; i < is.length; ++i)
	parameter.setValue(i, parameter.getDefaultValue(is[i]));
}

void copyParamIntoGUI() {
    list.clearSelection();
    boolean first = true;
    for (Iterator iter = parameter.values(); iter.hasNext(); ) {
	list.setSelectedValue(iter.next(), first); // Scroll if first
	first = false;
    }
}

}
