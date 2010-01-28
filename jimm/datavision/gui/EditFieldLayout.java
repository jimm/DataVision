package jimm.datavision.gui;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
 * Lays out a bunch of label/edit widget pairs. Optionally creates
 * the edit widget for you. This is not a layout manager <i>per se</i>.
 * Calling any of the <code>add</code>* methods creates label/edit
 * widget pairs. Calling <code>getPanel</code> returns a panel containing
 * the labels and edit widgets, arranged for your pleasure.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class EditFieldLayout {

/* ================================================================ */
/** Represents a label/component pair. */
static class Row {
protected JLabel label;
protected Component component;
Row(JLabel l, Component c) {
    label = l;
    component = c;
}
}
/* ================================================================ */

protected ArrayList rows;
protected Border border;
protected JPanel panel;

public EditFieldLayout() {
    rows = new ArrayList();
}

/**
 * Adds the label/component pair to the layout and returns the component.
 * If <var>label</var> does not end with a colon, one will be added.
 * <var>label</var> may be <code>null</code>, in which case no label is
 * displayed.
 * <p>
 * All the other <code>add*</code> methods call this one.
 *
 * @param label a possibly <code>null</code> label string
 * @param c a GUI component
 * @return the component
 */
public Component add(String label, Component c) {
    if (label == null || label.length() == 0)
	label = "";
    else if (!label.endsWith(":"))
	label += ":";

    rows.add(new Row(new JLabel(label), c));
    return c;
}

/**
 * Creates a text field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @return the new text field
 */
public JTextField addTextField(String label) {
    return (JTextField)add(label, new JTextField());
}

/**
 * Creates a text field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param columns the text field's size
 * @return the new text field
 */
public JTextField addTextField(String label, int columns) {
    return (JTextField)add(label, new JTextField(columns));
}

/**
 * Creates a text field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param text the text field's initial text
 * @return the new text field
 */
public JTextField addTextField(String label, String text) {
    return (JTextField)add(label, new JTextField(text == null ? "" : text));
}

/**
 * Creates a text field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param text the text field's initial text
 * @param columns the text field's size
 * @return the new text field
 */
public JTextField addTextField(String label, String text, int columns) {
    return (JTextField)add(label, new JTextField(text == null ? "" : text,
						 columns));
}

/**
 * Creates a text area and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @return the new text area
 */
public JTextArea addTextArea(String label) {
    JTextArea area = new JTextArea();
    area.setBorder(BorderFactory.createLoweredBevelBorder());
    add(label, area);
    return area;
}

/**
 * Creates a text area and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param rows the text field's height
 * @param cols the text field's width
 * @return the new text area
 */
public JTextArea addTextArea(String label, int rows, int cols) {
    JTextArea area = new JTextArea(rows, cols);
    area.setBorder(BorderFactory.createLoweredBevelBorder());
    add(label, area);
    return area;
}

/**
 * Creates a text area and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param text the text field's initial text
 * @return the new text area
 */
public JTextArea addTextArea(String label, String text) {
    JTextArea area = new JTextArea(text == null ? "" : text);
    area.setBorder(BorderFactory.createLoweredBevelBorder());
    add(label, area);
    return area;
}

/**
 * Creates a text area and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param text the text field's initial text
 * @param rows the text field's height
 * @param cols the text field's width
 * @return the new text area
 */
public JTextArea addTextArea(String label, String text, int rows, int cols) {
    JTextArea area = new JTextArea(text == null ? "" : text, rows, cols);
    JScrollPane scroller = new JScrollPane(area);
    add(label, scroller);
    return area;
}

/**
 * Creates a check box and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @return the new check box
 */
public JCheckBox addCheckBox(String label) {
    return addCheckBox(label, 0);
}

/**
 * Creates a check box and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param key the mnemonic key (a <code>KeyEvent</code> constant)
 * @return the new check box
 */
public JCheckBox addCheckBox(String label, int key) {
    JCheckBox checkBox = new JCheckBox(label);
    checkBox.setMnemonic(key);
    return (JCheckBox)add(null, checkBox);
}

/**
 * Creates a combo box and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param items an array of objects
 * @return the new combo box
 */
public JComboBox addComboBox(String label, Object[] items) {
    return addComboBox(label, items, false);
}

/**
 * Creates a combo box and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param items an array of objects
 * @param editable if <code>true</code>, the combo box will allow custom
 * value entry by the user
 * @return the new combo box
 */
public JComboBox addComboBox(String label, Object[] items, boolean editable) {
    JComboBox comboBox = new JComboBox(items);
    comboBox.setEditable(editable);
    return (JComboBox)add(label, comboBox);
}

/**
 * Creates two labels and adds them.
 *
 * @param label a possibly <code>null</code> label string
 * @param text text for the right-hand label
 * @return the new right-hand label
 */
public JLabel addLabel(String label, String text) {
    return (JLabel)add(label, new JLabel(text == null ? "" : text));
}

/**
 * Creates a password field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @return the new password field
 */
public JPasswordField addPasswordField(String label) {
    return (JPasswordField)add(label, new JPasswordField());
}

/**
 * Creates a password field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param columns the password field's size
 * @return the new password field
 */
public JPasswordField addPasswordField(String label, int columns) {
    return (JPasswordField)add(label, new JPasswordField(columns));
}

/**
 * Creates a password field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param password the initial password text
 * @return the new password field
 */
public JPasswordField addPasswordField(String label, String password) {
    return (JPasswordField)add(label, new JPasswordField(password == null
							 ? "" : password));
}

/**
 * Creates a password field and adds it and the label.
 *
 * @param label a possibly <code>null</code> label string
 * @param password the initial password text
 * @param columns the password field's size
 * @return the new password field
 */
public JPasswordField addPasswordField(String label, String password,
				       int columns)
{
    return (JPasswordField)add(label, new JPasswordField(password == null
							 ? "" : password,
							 columns));
}

/**
 * Creates a button and adds it to the right-hand side, under the fields.
 *
 * @param label a button label
 * @return the new button
 */
public JButton addButton(String label) {
    return (JButton)add(null, new JButton(label));
}

/**
 * Creates an empty row.
 */
public void skipRow() {
    rows.add(null);
}

/**
 * Creates an empty border the same size on all sides.
 *
 * @param allSides the width of the border
 */
public void setBorder(int allSides) {
    setBorder(allSides, allSides, allSides, allSides);
}

/**
 * Creates an empty border on all sides.
 *
 * @param top top border size
 * @param left left border size
 * @param bottom bottom border size
 * @param right right border size
 */
public void setBorder(int top, int left, int bottom, int right) {
    border = BorderFactory.createEmptyBorder(top, left, bottom, right);
}

/**
 * Returns the panel containing all the labels and edit widgets. Lazily
 * instantiates the panel.
 *
 * @return the panel containing all the labels and edit widgets
 */
public JPanel getPanel() {
    if (panel == null)
	buildPanel();
    return panel;
}

/**
 * Builds the panel.
 */
protected void buildPanel() {
    GridBagLayout bag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(6, 6, 6, 6);
    panel = new JPanel();
    panel.setLayout(bag);
    if (border != null)
	panel.setBorder(border);

    c.gridy = 0;
    for (Iterator iter = rows.iterator(); iter.hasNext(); ++c.gridy) {
	Row row = (Row)iter.next();
	if (row == null)
	    continue;

	if (row.label != null) {
	    c.gridx = 0;
	    c.anchor = GridBagConstraints.NORTHEAST;
	    bag.setConstraints(row.label, c);
	    panel.add(row.label);
	}

	if (row.component != null) {
	    c.gridx = 1;
	    c.anchor = GridBagConstraints.NORTHWEST;
	    bag.setConstraints(row.component, c);
	    panel.add(row.component);
	}
    }
}

}
