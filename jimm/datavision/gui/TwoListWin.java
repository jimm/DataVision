package jimm.datavision.gui;
import jimm.datavision.Report;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/* ================================================================ */

/**
 * An abstract superclass for edit windows that manipulate a list of
 * sortable, orderable items.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class TwoListWin extends EditWin
    implements ActionListener, ListSelectionListener
{

protected static final String PROTOTYPE_CELL_VALUE = "table_name.column_name";

protected Report report;
protected SortedListModel leftModel;
protected DefaultListModel rightModel;
protected JList leftList;
protected JList rightList;
protected JButton addOne, addAll, removeOne, removeAll;
protected JButton moveUp, moveDown;
protected JRadioButton ascendingRButton, descendingRButton;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param title the window title
 * @param commandNameKey the {@link I18N} command name lookup key
 * @param rightBoxTitleKey the I18N lookup key for the right box title
 * @param report the...um...I forgot
 */
public TwoListWin(Designer designer, String title, String commandNameKey,
		  String rightBoxTitleKey, Report report)
{
    super(designer, title, commandNameKey);
    this.report = report;

    leftModel = new SortedListModel();
    rightModel = new DefaultListModel();
    fillListModels();

    buildWindow(rightBoxTitleKey);
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 *
 * @param rightBoxTitleKey the I18N lookup key for the right box title
 */
protected void buildWindow(String rightBoxTitleKey) {
    // Movement buttons
    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(32));

    addOne = new JButton(">");
    addOne.addActionListener(this);
    box.add(addOne);

    addAll = new JButton(">>>");
    addAll.addActionListener(this);
    box.add(addAll);

    removeOne = new JButton("<");
    removeOne.addActionListener(this);
    box.add(removeOne);

    removeAll = new JButton("<<<");
    removeAll.addActionListener(this);
    box.add(removeAll);

    JPanel moveButtonPanel = new JPanel();
    moveButtonPanel.add(box);

    // "Move Up", "Move Down", "Ascending" and "Descending" radio buttons
    box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(32));

    moveUp = new JButton(I18N.get("TwoListWin.move_up"));
    moveUp.addActionListener(this);
    box.add(moveUp);

    moveDown = new JButton(I18N.get("TwoListWin.move_down"));
    moveDown.addActionListener(this);
    box.add(moveDown);

    box.add(Box.createVerticalStrut(32));

    ButtonGroup bg = new ButtonGroup();

    ascendingRButton = new JRadioButton(I18N.get("GUI.ascending"));
    ascendingRButton.addActionListener(this);
    box.add(ascendingRButton);
    bg.add(ascendingRButton);

    descendingRButton = new JRadioButton(I18N.get("GUI.descending"));
    descendingRButton.addActionListener(this);
    box.add(descendingRButton);
    bg.add(descendingRButton);

    JPanel orderButtonPanel = new JPanel();
    orderButtonPanel.add(box);

    // Left list and label in panel
    leftList = new JList(leftModel);
    leftList.addListSelectionListener(this);
    leftList.setPrototypeCellValue(PROTOTYPE_CELL_VALUE);
    leftList.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	    if (e.getClickCount() == 2)
		moveToRight(leftList.locationToIndex(e.getPoint()));
	}
	});
    Box leftBox = Box.createVerticalBox();
    leftBox.add(new JLabel(I18N.get("TwoListWin.columns")));
    leftBox.add(new JScrollPane(leftList));

    // Right list and label in panel
    rightList = new JList(rightModel);
    rightList.addListSelectionListener(this);
    rightList.setPrototypeCellValue(PROTOTYPE_CELL_VALUE);
    rightList.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	    if (e.getClickCount() == 2)
		moveToLeft(rightList.locationToIndex(e.getPoint()));
	}
	});
    Box rightBox = Box.createVerticalBox();
    rightBox.add(new JLabel(I18N.get(rightBoxTitleKey)));
    rightBox.add(new JScrollPane(rightList));

    // Panel containing left, arrows, right, and radio buttons
    Box center = Box.createHorizontalBox();
    center.add(Box.createHorizontalStrut(12));
    center.add(leftBox);
    center.add(moveButtonPanel);
    center.add(rightBox);
    center.add(orderButtonPanel);
    center.add(Box.createHorizontalStrut(12));

    // OK, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add tables and buttons to window
    getContentPane().add(center, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    // Update button states
    adjustButtons();
}

protected abstract void fillListModels();

/**
 * Moves the specified item number from the left to the right list
 * and calls {@link #adjustButtons}.
 *
 * @param index the item number to move
 */
protected void moveToRight(int index) {
    moveToRight(index, true);
}

/**
 * Moves the specified item number from the left to the right list
 * and optionally calls {@link #adjustButtons}.
 *
 * @param index the item number to move
 * @param callAdjustButtons if <code>true</code>, make it so
 */
protected void moveToRight(int index, boolean callAdjustButtons) {
    TLWListItem item = (TLWListItem)leftModel.getElementAt(index);
    leftModel.remove(index);
    rightModel.addElement(item);
    if (callAdjustButtons)
	adjustButtons();
}

/**
 * Moves the specified item number from the right to the left list
 * and calls {@link #adjustButtons}.
 *
 * @param index the item number to move
 */
protected void moveToLeft(int index) {
    moveToLeft(index, true);
}

/**
 * Moves the specified item number from the right to the left list
 * and optionally calls {@link #adjustButtons}.
 *
 * @param index the item number to move
 * @param callAdjustButtons if <code>true</code>, make it so
 */
protected void moveToLeft(int index, boolean callAdjustButtons) {
    TLWListItem item = (TLWListItem)rightModel.elementAt(index);
    rightModel.removeElementAt(index);
    leftModel.add(item);
    if (callAdjustButtons)
	adjustButtons();
}

/**
 * Handles all buttons except ascending and descending sort order.
 */
public void actionPerformed(ActionEvent e) {
    SortedListModel leftModel = (SortedListModel)leftList.getModel();
    DefaultListModel rightModel = (DefaultListModel)rightList.getModel();

    String cmd = e.getActionCommand();
    if (cmd.equals(">")) {
	int[] indices = leftList.getSelectedIndices();
	for (int i = 0; i < indices.length; ++i)
	    moveToRight(indices[i], false);
	adjustButtons();
    }
    else if (cmd.equals(">>>")) {
	Object[] items = leftModel.toArray();
	for (int i = 0; i < items.length; ++i)
	    rightModel.addElement(items[i]);
	leftModel.removeAllElements();
	adjustButtons();
    }
    else if (cmd.equals("<")) {
	int[] indices = rightList.getSelectedIndices();
	for (int i = 0; i < indices.length; ++i)
	    moveToLeft(indices[i], false);
	adjustButtons();
    }
    else if (cmd.equals("<<<")) {
	Object[] items = rightModel.toArray();
	for (int i = 0; i < items.length; ++i)
	    leftModel.add((TLWListItem)items[i]);
	rightModel.removeAllElements();
	adjustButtons();
    }
    else if (cmd.equals(I18N.get("TwoListWin.move_up"))) {
	int index = rightList.getSelectedIndex();
	Object item = rightModel.remove(index);
	rightModel.add(index - 1, item);
	rightList.setSelectedIndex(index - 1);
    }
    else if (cmd.equals(I18N.get("TwoListWin.move_down"))) {
	int index = rightList.getSelectedIndex();
	Object item = rightModel.remove(index);
	rightModel.add(index + 1, item);
	rightList.setSelectedIndex(index + 1);
    }
    else
	super.actionPerformed(e);
}

public void valueChanged(ListSelectionEvent e) {
    adjustButtons();
}

protected void adjustButtons() {
    boolean selEmpty;

    addOne.setEnabled(!leftList.isSelectionEmpty());
    addAll.setEnabled(leftList.getModel().getSize() > 0);

    selEmpty = rightList.isSelectionEmpty();
    if (selEmpty) {
	removeOne.setEnabled(false);
	moveUp.setEnabled(false);
	moveDown.setEnabled(false);
	ascendingRButton.setEnabled(false);
	descendingRButton.setEnabled(false);
	ascendingRButton.setSelected(false);
	descendingRButton.setSelected(false);
    }
    else {
	removeOne.setEnabled(true);

	int[] selectionIndices = rightList.getSelectedIndices();
	if (selectionIndices.length == 1) {
	    int numGroups = rightModel.size();
	    if (numGroups > 1) {
		int index = selectionIndices[0];
		moveUp.setEnabled(index != 0);
		moveDown.setEnabled(index != numGroups - 1);
	    }
	    else {
		moveUp.setEnabled(false);
		moveDown.setEnabled(false);
	    }

	    ascendingRButton.setEnabled(true);
	    descendingRButton.setEnabled(true);

	    TLWListItem item = (TLWListItem)rightList.getSelectedValue();
	    if (item.sortsAscending())
		ascendingRButton.setSelected(true);
	    else
		descendingRButton.setSelected(true);
	}
	else {
	    moveUp.setEnabled(false);
	    moveDown.setEnabled(false);
	    ascendingRButton.setEnabled(false);
	    descendingRButton.setEnabled(false);
	    ascendingRButton.setSelected(false);
	    descendingRButton.setSelected(false);
	}
    }
    removeAll.setEnabled(rightList.getModel().getSize() > 0);
}

}
