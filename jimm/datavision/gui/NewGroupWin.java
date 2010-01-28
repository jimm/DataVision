package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.gui.cmd.NewGroupCommand;
import jimm.util.I18N;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;

/**
 * A dialog for creating a new group.
 * <p>
 * This dialog should only be created if the report has at least one field.
 * The method {@link Designer#enableMenuItems} makes sure this is true.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class NewGroupWin extends JDialog implements ActionListener {

protected Designer designer;
protected Report report;
protected JComboBox combo;
protected JRadioButton ascendingButton, descendingButton;

/**
 * Constructor; uses format of first selected field.
 *
 * @param designer the window to which this dialog belongs
 * @param report the report
 */
public NewGroupWin(Designer designer, Report report) {
    super(designer.getFrame(), I18N.get("NewGroupWin.title"));
    this.designer = designer;
    this.report = report;
    buildWindow();
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 */
protected void buildWindow() {
    JPanel gutsPanel = buildGuts();
    JPanel buttonPanel = buildButtonPanel();

    getContentPane().add(gutsPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
}

/**
 * Builds and returns a panel containing the stuff from which groups are
 * made.
 *
 * @return a panel
 */
protected JPanel buildGuts() {
    GridBagLayout bag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(6, 6, 6, 6);

    JPanel panel = new JPanel();
    panel.setLayout(bag);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // "Group Column" label
    JLabel label;
    label = new JLabel(I18N.get("NewGroupWin.group_column"));
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.EAST;
    bag.setConstraints(label, c);
    panel.add(label);

    // Group column dropdown
    JPanel comboPanel = buildColumnComboBox();
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.NORTHWEST;
    bag.setConstraints(comboPanel, c);
    panel.add(comboPanel);

    // "Sort Order" label
    label = new JLabel(I18N.get("NewGroupWin.sort_order"));
    c.gridx = 0;
    c.gridy = 1;
    c.anchor = GridBagConstraints.EAST;
    bag.setConstraints(label, c);
    panel.add(label);

    // Sort order radion buttons
    Box rbBox = buildSortOrderRadioButtons();
    c.gridx = 1;
    c.gridy = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridheight = 2;
    bag.setConstraints(rbBox, c);
    panel.add(rbBox);

    return panel;
}

protected JPanel buildColumnComboBox() {
    DefaultComboBoxModel model = new DefaultComboBoxModel();

    // Iterate through columns in tables used by report. We will remove
    // those associated with extant groups in a moment.
    Iterator iter;
    for (iter = report.userColumns(); iter.hasNext(); )
	model.addElement((Selectable)iter.next());
    for (iter = report.getDataSource().columnsInTablesUsedInReport();
	 iter.hasNext(); )
	model.addElement((Selectable)iter.next());

    // Remove all user columns and columns already in a group.
    for (iter = report.groups(); iter.hasNext(); ) {
	Group group = (Group)iter.next();
	model.removeElement(group.getSelectable());
    }

    combo = new JComboBox(model);
    combo.setSelectedIndex(0);

    JPanel panel = new JPanel();
    panel.add(combo);
    return panel;
}

protected Box buildSortOrderRadioButtons() {
    Box box = Box.createVerticalBox();

    ButtonGroup bg = new ButtonGroup();

    ascendingButton = new JRadioButton(I18N.get("GUI.ascending"));
    ascendingButton.addActionListener(this);
    box.add(ascendingButton);
    bg.add(ascendingButton);

    descendingButton = new JRadioButton(I18N.get("GUI.descending"));
    descendingButton.addActionListener(this);
    box.add(descendingButton);
    bg.add(descendingButton);

    ascendingButton.setSelected(true);
    return box;
}

/**
 * Builds and returns a panel containing the OK and Cancel
 *
 * @return a panel
 */
protected JPanel buildButtonPanel() {
    JPanel buttonPanel = new JPanel();
    JButton button;

    buttonPanel.add(button = new JButton(I18N.get("GUI.ok")));
    button.addActionListener(this);
    button.setDefaultCapable(true);

    buttonPanel.add(button = new JButton(I18N.get("GUI.cancel")));
    button.addActionListener(this);

    return buttonPanel;
}

/**
 * Handles the OK and Cancel buttons.
 *
 * @param e action event
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("GUI.ok").equals(cmd)) {
	int sortOrder = ascendingButton.isSelected()
	    ? Group.SORT_ASCENDING : Group.SORT_DESCENDING;
	NewGroupCommand ngc =
	    new NewGroupCommand(designer, report,
				(Selectable)combo.getSelectedItem(),
				sortOrder);
	designer.performCommand(ngc);
	dispose();
    }
    else if (I18N.get("GUI.cancel").equals(cmd)) {
	dispose();
    }
}

}
