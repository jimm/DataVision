package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.EditWin;
import jimm.datavision.gui.cmd.ParamEditCommand;
import jimm.util.I18N;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import javax.swing.*;
import com.toedter.calendar.JCalendar;

/**
 * A parameter editing dialog box.
 * <p>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ParamEditWin extends EditWin {

protected static final int HORIZ_GAP = 20;
protected static final int VERT_GAP = 20;
protected static final int TEXT_FIELD_COLS = 24;
protected static final String CARD_NAME_SINGLE = "single";
protected static final String CARD_NAME_SINGLE_BOOL = "single-bool";
protected static final String CARD_NAME_RANGE = "range";
protected static final String CARD_NAME_LIST = "list";
protected static final String CARD_NAME_DATE = "date";

protected Parameter param;
protected JTextField nameField;
protected JTextField questionField;
protected JRadioButton boolRButton;
protected JRadioButton stringRButton;
protected JRadioButton numericRButton;
protected JRadioButton dateRButton;
protected JRadioButton singleRButton;
protected JRadioButton rangeRButton;
protected JRadioButton listSingleRButton;
protected JRadioButton listMultipleRButton;
protected JPanel cardPanel;
protected JTextField singleField;
protected JCalendar singleDate;
protected JTextField rangeFromField;
protected JTextField rangeToField;
protected JCalendar rangeFromDate;
protected JCalendar rangeToDate;
protected JRadioButton boolYesRButton;
protected JRadioButton boolNoRButton;
protected JList list;
protected JTextField listAddField;

/**
 * Constructor.
 *
 * @param designer the design window to which this dialog belongs
 * @param p a parameter
 */
public ParamEditWin(Designer designer, Parameter p) {
    super(designer, I18N.get("ParamEditWin.title"), "ParamEditCommand.name");

    param = p;

    buildWindow();
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 */
protected void buildWindow() {
    // Add edit panes and buttons to window
    getContentPane().add(buildPromptPanel(), BorderLayout.NORTH);
    getContentPane().add(buildCenterPanel(), BorderLayout.CENTER);

    // Add OK, Apply, Revert, and Cancel Buttons
    getContentPane().add(closeButtonPanel(), BorderLayout.SOUTH);

    fillEditWidgets();
}

protected JPanel buildPromptPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    // Labels
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new GridLayout(0, 1));
    JLabel label;
    labelPanel.add(label = new JLabel(I18N.get("ParamEditWin.name")));
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    labelPanel.add(label = new JLabel(I18N.get("ParamEditWin.question")));
    label.setHorizontalAlignment(SwingConstants.RIGHT);

    // Edit values
    JPanel fieldPanel = new JPanel();
    fieldPanel.setLayout(new GridLayout(0, 1));
    fieldPanel.add(nameField = new JTextField(TEXT_FIELD_COLS));
    fieldPanel.add(questionField = new JTextField(TEXT_FIELD_COLS));

    panel.add(labelPanel, BorderLayout.WEST);
    panel.add(fieldPanel, BorderLayout.CENTER);

    JPanel nonStretchyPanel = new JPanel();
    nonStretchyPanel.add(panel);
    return nonStretchyPanel;
}

protected JPanel buildCenterPanel() {
    JPanel panel = new JPanel();
    Box box = Box.createHorizontalBox();

    box.add(buildRadioButtonsPanel());
    box.add(buildCardPanel());

    panel.add(box);
    return panel;
}

protected JPanel buildRadioButtonsPanel() {
    JPanel panel = new JPanel();

    // Type radio buttons
    Box box = Box.createVerticalBox();
    ButtonGroup bg = new ButtonGroup();
    box.add(new JLabel("Type"));
    boolRButton = addRadioButton(I18N.get("ParamEditWin.bool"), box, bg);
    stringRButton = addRadioButton(I18N.get("ParamEditWin.text"), box, bg);
    numericRButton = addRadioButton(I18N.get("ParamEditWin.number"), box, bg);
    dateRButton = addRadioButton(I18N.get("ParamEditWin.date"), box, bg);
    panel.add(box);

    // Arity radio buttons
    box = Box.createVerticalBox();
    box.add(new JLabel(I18N.get("ParamEditWin.arity")));
    bg = new ButtonGroup();
    singleRButton = addRadioButton(I18N.get("ParamEditWin.single"), box, bg);
    rangeRButton = addRadioButton(I18N.get("ParamEditWin.range"), box, bg);
    listSingleRButton = addRadioButton(I18N.get("ParamEditWin.list_single"),
				       box, bg);
    listMultipleRButton = addRadioButton(I18N.get("ParamEditWin.list_mult"),
					 box, bg);
    panel.add(box);

    return panel;
}

protected JPanel buildCardPanel() {
    cardPanel = new JPanel();
    cardPanel.setLayout(new CardLayout(HORIZ_GAP, VERT_GAP));

    cardPanel.add(boolCard(), CARD_NAME_SINGLE_BOOL);
    cardPanel.add(singleCard(), CARD_NAME_SINGLE);
    cardPanel.add(rangeCard(), CARD_NAME_RANGE);
    cardPanel.add(listCard(), CARD_NAME_LIST);
    cardPanel.add(dateCard(), CARD_NAME_DATE);

    return cardPanel;
}

protected JPanel dateCard() {
    JPanel panel = new JPanel();
    panel.add(new JLabel(I18N.get("ParamEditWin.date_default")));
    return panel;
}

protected JPanel boolCard() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    Box box = Box.createVerticalBox();
    ButtonGroup bg = new ButtonGroup();

    boolYesRButton = new JRadioButton(I18N.get("GUI.yes"));
    box.add(boolYesRButton);
    bg.add(boolYesRButton);

    boolNoRButton = new JRadioButton(I18N.get("GUI.no"));
    box.add(boolNoRButton);
    bg.add(boolNoRButton);

    panel.add(box);
    return panel;
}

protected JPanel singleCard() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    panel.add(new JLabel(I18N.get("ParamEditWin.default_value")));
    panel.add(singleField = new JTextField(TEXT_FIELD_COLS));

    return panel;
}

protected JPanel rangeCard() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    // Labels
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new GridLayout(0, 1));
    JLabel label;
    labelPanel.add(label = new JLabel(I18N.get("GUI.from")));
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    labelPanel.add(label = new JLabel(I18N.get("GUI.to")));
    label.setHorizontalAlignment(SwingConstants.RIGHT);

    // Edit values
    JPanel fieldPanel = new JPanel();
    fieldPanel.setLayout(new GridLayout(0, 1));

    // From and to fields
    fieldPanel.add(rangeFromField = new JTextField(TEXT_FIELD_COLS));
    fieldPanel.add(rangeToField = new JTextField(TEXT_FIELD_COLS));

    panel.add(labelPanel, BorderLayout.CENTER);
    panel.add(fieldPanel, BorderLayout.EAST);

    return panel;
}

protected JPanel listCard() {
    JPanel panel = new JPanel();
    Box box = Box.createVerticalBox();
    panel.add(box);

    list = new JList(new DefaultListModel());
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    box.add(new JScrollPane(list));

    JPanel addPanel = new JPanel();
    JButton button = new JButton(I18N.get("ParamEditWin.add_to_list"));
    button.addActionListener(this);
    addPanel.add(button);
    addPanel.add(listAddField = new JTextField(TEXT_FIELD_COLS));
    box.add(addPanel);

    button = new JButton(I18N.get("ParamEditWin.remove_from_list"));
    button.addActionListener(this);
    box.add(button);

    return panel;
}

/**
 * Creates and adds a radio buton
 *
 * @param name the button name (label)
 * @param parent a container
 * @param group the button group
 * @return the new radio button
 */
protected JRadioButton addRadioButton(String name, Container parent,
				      ButtonGroup group)
{
    JRadioButton button = new JRadioButton(name);
    button.addActionListener(this);
    parent.add(button);
    group.add(button);
    return button;
}

/**
 * Fills all widgets except name and question.
 */
protected void fillEditWidgets() {
    nameField.setText(param.getName());
    questionField.setText(param.getQuestion());

    // Select type radio button.
    int type = param.getType();
    switch (type) {
    case Parameter.TYPE_BOOLEAN:
	boolRButton.setSelected(true);
	break;
    case Parameter.TYPE_STRING:
	stringRButton.setSelected(true);
	break;
    case Parameter.TYPE_NUMERIC:
	numericRButton.setSelected(true);
	break;
    case Parameter.TYPE_DATE:
	dateRButton.setSelected(true);
	break;
    }

    // Based on arity, select the appropriate radio button.
    switch (param.getArity()) {
    case Parameter.ARITY_ONE:
	singleRButton.setSelected(true);
	break;
    case Parameter.ARITY_RANGE:
	rangeRButton.setSelected(true);
	break;
    case Parameter.ARITY_LIST_SINGLE:
	listSingleRButton.setSelected(true);
	break;
    case Parameter.ARITY_LIST_MULTIPLE:
	listMultipleRButton.setSelected(true);
	break;
    }

    selectAndFillCard();	// Select card and fill card contents
}

/**
 * Returns one of the <code>Parameter</code> constants
 * <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code> based on the state
 * of the GUI.
 *
 * @return one of the <code>Parameter</code> constants
 * <code>TYPE_BOOLEAN</code>, <code>TYPE_STRING</code>,
 * <code>TYPE_NUMERIC</code>, or <code>TYPE_DATE</code> based on the state
 * of the GUI
 */
protected int typeFromWidgets() {
    if (boolRButton.isSelected()) return Parameter.TYPE_BOOLEAN;
    if (stringRButton.isSelected()) return Parameter.TYPE_STRING;
    if (numericRButton.isSelected()) return Parameter.TYPE_NUMERIC;
    else return Parameter.TYPE_DATE;
}

/**
 * Returns one of the {@link Parameter} constants <code>ARITY_ONE</code>,
 * <code>ARITY_RANGE</code>, <code>ARITY_LIST_SINGLE</code>, or
 * <code>ARITY_LIST_MULTIPLE</code> based on the state of the GUI.
 *
 * @return one of the <code>Parameter</code> constants <code>ARITY_ONE</code>,
 * <code>ARITY_RANGE</code>, <code>ARITY_LIST_SINGLE</code>, or
 * <code>ARITY_LIST_MULTIPLE</code> based on the state of the GUI
 */
protected int arityFromWidgets() {
    if (singleRButton.isSelected()) return Parameter.ARITY_ONE;
    if (rangeRButton.isSelected()) return Parameter.ARITY_RANGE;
    if (listSingleRButton.isSelected()) return Parameter.ARITY_LIST_SINGLE;
    else return Parameter.ARITY_LIST_MULTIPLE;
}


/**
 * Enables only legal arity radio buttons.
 */
protected void enableLegalArityButtons() {
    int type = typeFromWidgets();
    singleRButton.setEnabled(param.isLegal(type, Parameter.ARITY_ONE));
    rangeRButton.setEnabled(param.isLegal(type, Parameter.ARITY_RANGE));
    listSingleRButton
	.setEnabled(param.isLegal(type, Parameter.ARITY_LIST_SINGLE));
    listMultipleRButton
	.setEnabled(param.isLegal(type, Parameter.ARITY_LIST_MULTIPLE));
}


/**
 * Based on type and arity, selects proper card and fills card contents.
 * Enables/disables the arity radion buttons based on legal choices.
 * <p>
 * Since we could be switching from one parameter type to another, we
 * have to do some checking along the way to see if the parameter's default
 * value is of the correct type.
 */
protected void selectAndFillCard() {
    enableLegalArityButtons();

    CardLayout cardLayout = (CardLayout)cardPanel.getLayout();
    Object objVal;
    int type = typeFromWidgets();

    switch (arityFromWidgets()) {
    case Parameter.ARITY_ONE:
	switch (type) {
	case Parameter.TYPE_BOOLEAN:
	    cardLayout.show(cardPanel, CARD_NAME_SINGLE_BOOL);
	    objVal = param.getDefaultValue(0);
	    Boolean val = null;
	    val = (objVal instanceof Boolean) ? (Boolean)objVal
		: (Boolean)param.getDefaultForType(type);
	    if (val != null && val.booleanValue())
		boolYesRButton.setSelected(true);
	    else
		boolNoRButton.setSelected(true);
	    break;
	case Parameter.TYPE_DATE:
	    cardLayout.show(cardPanel, CARD_NAME_DATE);
	    break;
	default:
	    cardLayout.show(cardPanel, CARD_NAME_SINGLE);
	    setField(singleField, 0);
	    break;
	}
	break;
    case Parameter.ARITY_RANGE:
	if (type == Parameter.TYPE_DATE)
	    cardLayout.show(cardPanel, CARD_NAME_DATE);
	else
	    cardLayout.show(cardPanel, CARD_NAME_RANGE);
	setField(rangeFromField, 0);
	setField(rangeToField, 1);
	break;
    case Parameter.ARITY_LIST_SINGLE:
    case Parameter.ARITY_LIST_MULTIPLE:
	cardLayout.show(cardPanel, CARD_NAME_LIST);

	// Erase list and re-fill it
	DefaultListModel model = (DefaultListModel)list.getModel();
	model.clear();
	for (Iterator iter = param.defaultValues(); iter.hasNext(); )
	    model.addElement(iter.next());
	break;
    }
}

/**
 * Fills the specified text field with the nth parameter default value.
 * If the value is <code>null</code>, "fill" the text field with the
 * empty string.
 *
 * @param f the text field
 * @param which n
 */
protected void setField(JTextField f, int which) {
    Object obj = param.getDefaultValue(which); // Default value
    f.setText(obj == null ? "" : obj.toString());
}

/**
 * Handles radio buttons.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    int type = typeFromWidgets();
    int arity = arityFromWidgets();
    boolean refill = false;

    // Type changes. When the type changes, make sure the new type and
    // selected arity are a legal combination. If not, change the arity.
    if (cmd.equals(I18N.get("ParamEditWin.bool"))
	|| cmd.equals(I18N.get("ParamEditWin.text"))
	|| cmd.equals(I18N.get("ParamEditWin.number"))
	|| cmd.equals(I18N.get("ParamEditWin.date")))
    {
	refill = true;
	if (!param.isLegal(type, arity))
	    singleRButton.setSelected(true);
    }

    // Arity changes. The user can't pick an illegal combination because
    // we make sure of that in selectAndFillCard().
    else if (cmd.equals(I18N.get("ParamEditWin.single"))
	     || cmd.equals(I18N.get("ParamEditWin.range"))
	     || cmd.equals(I18N.get("ParamEditWin.list_single"))
	     || cmd.equals(I18N.get("ParamEditWin.list_mult")))
	refill = true;

    else if (cmd.equals(I18N.get("ParamEditWin.add_to_list"))) {
	((DefaultListModel)list.getModel()).addElement(listAddField.getText());
	listAddField.setText("");
    }

    else if (cmd.equals(I18N.get("ParamEditWin.remove_from_list"))) {
	((DefaultListModel)list.getModel())
	    .removeElement(list.getSelectedValue());
    }
    else {
	super.actionPerformed(e);
    }

    if (refill)
	selectAndFillCard();
}

protected void doSave() {
    ArrayList defaultValues = new ArrayList();
    int type = typeFromWidgets();
    int arity = arityFromWidgets();

    // Create list of new default values.
    switch (arity) {
    case Parameter.ARITY_ONE:
	switch (type) {
	case Parameter.TYPE_BOOLEAN:
	    defaultValues.add(Boolean.valueOf(boolYesRButton.isSelected()));
	case Parameter.TYPE_DATE:
	    break;
	default:
	    defaultValues.add(singleField.getText());
	    break;
	}
	break;
    case Parameter.ARITY_RANGE:
	defaultValues.add(rangeFromField.getText());
	defaultValues.add(rangeToField.getText());
	break;
    case Parameter.ARITY_LIST_SINGLE:
    case Parameter.ARITY_LIST_MULTIPLE:
	DefaultListModel model = (DefaultListModel)list.getModel();
	for (Enumeration e = model.elements(); e.hasMoreElements(); )
	    defaultValues.add(e.nextElement());
	break;
    }

    ParamEditCommand cmd =
	new ParamEditCommand(param, nameField.getText(),
			     questionField.getText(), type, arity,
			     defaultValues);
    cmd.perform();
    commands.add(cmd);
}

protected void doRevert() {
    fillEditWidgets();
}

}
