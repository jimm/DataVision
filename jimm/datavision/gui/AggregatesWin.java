package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.field.AggregateField;
import jimm.datavision.gui.cmd.NewAggregateCommand;
import jimm.datavision.gui.cmd.EditAggregateFuncCommand;
import jimm.datavision.gui.cmd.DeleteAggregateCommand;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * An aggregates editor for a single field that lets the user add and delete
 * aggregates at each level (group footers, report footer). Never called
 * when multiple fields are selected.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
@SuppressWarnings("serial")
public class AggregatesWin extends EditWin {

static class Slot {

FieldWidget aggregate;
boolean existedAlready;
JCheckBox checkBox;
JComboBox functionMenu;

Slot(FieldWidget fw, JCheckBox cb, JComboBox menu) {
    existedAlready = (fw != null);
    aggregate = fw;
    checkBox = cb;
    functionMenu = menu;
}
}

protected static final int TEXT_FIELD_COLS = 8;

protected Report report;
protected FieldWidget fieldWidget;
protected HashMap<Group, Slot> slots;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param fw a field widget
 */
public AggregatesWin(Designer designer, FieldWidget fw) {
    super(designer, I18N.get("AggregatesWin.title_prefix") + ' ' + fw,
	  "AggregatesWin.command_name");

    fieldWidget = fw;
    report = designer.report;

    // If we have been handed a aggregate field's widget, find the widget of
    // the original field.
    if (fieldWidget.getField() instanceof AggregateField) {
	final Field fieldToAggregate = getAggregateField().getField();
	final FieldWidget[] list = new FieldWidget[1];

	designer.withWidgetsDo(new FieldWidgetWalker() {
	    public void step(FieldWidget fw) {
		Field f = fw.getField();
		if (f == fieldToAggregate)
		    list[0] = fw;
	    }
	    });
	fieldWidget = list[0];
	// Reset window title
	setTitle(I18N.get("AggregatesWin.title_prefix") + ' ' + fieldWidget);
    }

    // Create a hash that maps either a group or a report footer section to
    // the associated existing aggregate widget.
    final HashMap<Object, FieldWidget> aggregates = new HashMap<Object, FieldWidget>();
    final AbstractList<AggregateField> subs =
	designer.report.getAggregateFieldsFor(fieldWidget.getField());
    designer.withWidgetsDo(new FieldWidgetWalker() {
	public void step(FieldWidget fw) {
	    Field f = fw.getField();
	    if (subs.contains(f)) {
		Object key = ((AggregateField)f).getGroup();
		if (key == null)
		    key = fw.getField().getSection();
		aggregates.put(key, fw);
	    }
	}
	});

    buildWindow(aggregates);
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 *
 * @param aggregates a hash that maps either a group or a report footer
 * section to the associated existing aggregate widget
 */
protected void buildWindow(HashMap<Object, FieldWidget> aggregates) {
    JPanel editorPanel = buildAggregatesEditor(aggregates);

    // OK, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add title, values, and buttons to window
    getContentPane().add(new JLabel(I18N.get("AggregatesWin.title_prefix")
				    + ' ' + fieldWidget),
			 BorderLayout.NORTH);
    getContentPane().add(editorPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
}

/**
 * Builds the editor widget panel.
 *
 * @param aggregates a hash that maps either a group or a report footer
 * section to the associated existing aggregate widget
 */
protected JPanel buildAggregatesEditor(HashMap<Object, FieldWidget> aggregates) {
    Object[] functionNames = AggregateField.functionNameArray();
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(0, 1));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    JCheckBox cb = null;
    JComboBox menu = null;
    slots = new HashMap<Group, Slot>();

    // For each footer in the report, create a checkbox and a slot.

    // Group footer sections
    int i = report.countGroups();
    for (Group g : report.groupsReversed()) {
	FieldWidget fw = aggregates.get(g);

	cb = new JCheckBox(I18N.get("AggregatesWin.group") + " #" + i + " ("
			   + g.getSelectableName() + ")");
	panel.add(cb);
	menu = new JComboBox(functionNames);
	panel.add(menu);
	if (fw != null) {
	    cb.setSelected(true);
	    menu.setSelectedItem(getAggregateField(fw).getFunction());
	}
	slots.put(g, new Slot(fw, cb, menu));
	--i;
    }

    // Report footer sections
    cb = new JCheckBox(I18N.get("AggregatesWin.grand_total"));
    panel.add(cb);
    menu = new JComboBox(functionNames);
    panel.add(menu);
    boolean addedSlot = false;
    for (Section s : report.footers()) {
	FieldWidget fw = aggregates.get(s);
	if (fw != null) {
	    cb.setSelected(true);
	    menu.setSelectedItem(getAggregateField(fw).getFunction());
	    slots.put(null, new Slot(fw, cb, menu));
	    addedSlot = true;
	}
    }
    if (!addedSlot)
	slots.put(null, new Slot(null, cb, menu));

    // Add an "all" button
    JButton all = new JButton(I18N.get("GUI.all"));
    all.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    for (Slot s : slots.values())
		s.checkBox.setSelected(true);
	}
	});
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(all);
    panel.add(buttonPanel);

    return panel;
}

/**
 * Return the <code>AggregateField</code> associated with the ivar
 * <var>fieldWidget</var>. Convenience method.
 */
protected AggregateField getAggregateField() {
    return getAggregateField(fieldWidget);
}
/**
 * Return the <code>AggregateField</code> associated with <var>fw</var>.
 *  Convenience method.
 */
protected AggregateField getAggregateField(FieldWidget fw) {
    return (AggregateField)fw.getField();
}

protected void doSave() {
    // Hide existing aggregates that are unchecked and show existing
    // aggregates that are checked. For checked aggregates, modify function
    // (no harm done if function is not changed).
    for (Object key : slots.keySet()) {
	Slot slot = (Slot)slots.get(key);
	if (slot.checkBox.isSelected()) {
	  String functionName = slot.functionMenu.getSelectedItem().toString();
	    if (slot.aggregate == null) {
		NewAggregateCommand cmd =
		    new NewAggregateCommand(report, fieldWidget, (Group)key,
					   functionName);
		cmd.perform();
		commands.add(cmd);

		slot.aggregate = cmd.getAggregateWidget();
	    }
	    else {		// Already have one; change to selected func
		EditAggregateFuncCommand cmd =
		    new EditAggregateFuncCommand(getAggregateField(slot.aggregate),
						 functionName);
		cmd.perform();
		commands.add(cmd);
		getAggregateField(slot.aggregate).setFunction(functionName);
	    }
	}
	else {			// We don't want an aggregate for this slot
	    if (slot.aggregate != null) {
	        String functionName =
		    getAggregateField(slot.aggregate).getFunction();
		DeleteAggregateCommand cmd =
		    new DeleteAggregateCommand(report, fieldWidget,
					       slot.aggregate, functionName,
					       (Group)key);
		cmd.perform();
		commands.add(cmd);
		slot.aggregate = null;
	    }
	}
    }
}

protected void doRevert() {
    for (Group g : slots.keySet()) {
	Slot slot = slots.get(g);
	if (slot.existedAlready) {
	    slot.aggregate.getComponent().setVisible(true);
	    slot.checkBox.setSelected(true);
	}
	else {
	    if (slot.aggregate != null) {
		slot.aggregate.doDelete();
		slot.aggregate = null;
	    }
	    slot.checkBox.setSelected(false);
	}
	// For some reason we need to force a repaint of the section. Can't
	//just call invalidate.
	if (slot.aggregate != null)
	  slot.aggregate.getComponent().getParent().repaint();
    }
}

}
