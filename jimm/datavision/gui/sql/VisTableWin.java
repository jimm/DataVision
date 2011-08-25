package jimm.datavision.gui.sql;
import jimm.datavision.*;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.EditWin;
import jimm.datavision.source.*;
import jimm.datavision.gui.cmd.TableJoinCommand;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;

/**
 * The dialog used for defining joins between database tables. Eventually,
 * this will be nice and graphical.
 * <p>
 * This dialog should only be created if the report uses more than one table.
 * The method {@link Designer#enableMenuItems} makes sure this is true.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
@SuppressWarnings("serial")
public class VisTableWin extends EditWin implements ActionListener {

/* ================================================================ */
static class RevertInfo {
ArrayList<Join> joins;
RevertInfo(Query q) {
    joins = new ArrayList<Join>();
    for (Join j : q.joins())
	joins.add((Join)j.clone());
}
}

/* ================================================================ */
static class JoinFields {

Join join;			// If null, it's a newly created join
JCheckBox del;
JComboBox from;
JComboBox relation;
JComboBox to;

JoinFields(Join j, DataSource db) {
    join = j;
    del = new JCheckBox();
    from = buildColDropdown((j == null) ? null : j.getFrom(), db);
    relation = buildRelDropdown((j == null) ? null : j.getRelation());
    to = buildColDropdown((j == null) ? null : j.getTo(), db);
}

protected JComboBox buildColDropdown(Column col, DataSource db) {
    JComboBox cb = new JComboBox();

    // Get iterator over tables actually used by the report. Will be
    // null if the data source does not have tables.
    for (Column c : db.columnsInTablesUsedInReport()) {
	cb.addItem(c.fullName());
	if (col != null && col.equals(c))
	    cb.setSelectedItem(c.fullName());
    }

    return cb;
}

protected JComboBox buildRelDropdown(String rel) {
    JComboBox cb = new JComboBox(Join.RELATIONS);
    if (rel != null)
	cb.setSelectedItem(rel);
    return cb;
}
}

/* ================================================================ */

protected Report report;
protected Query query;
protected ArrayList<JoinFields> joinFieldsList;
protected JPanel joinsPanel;
protected Box delCheckBoxPanel;
protected Box fromPanel;
protected Box relationPanel;
protected Box toPanel;
protected JButton deleteButton;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the...um...I forgot
 */
public VisTableWin(Designer designer, Report report) {
    this(designer, report, report.getDataSource().getQuery());
}

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the...um...I forgot
 * @param query a query
 */
public VisTableWin(Designer designer, Report report, Query query) {
    super(designer, I18N.get("VisTableWin.title"), "TableJoinCommand.name");
    this.report = report;
    this.query = query;

    buildWindow();
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 */
protected void buildWindow() {
    // Joins
    buildJoinsPanel();

    // Add/Delete buttons
    JPanel addDelButtons = new JPanel();
    JButton button = new JButton(I18N.get("VisTableWin.add"));
    button.addActionListener(this);
    addDelButtons.add(button);

    deleteButton = new JButton(I18N.get("VisTableWin.delete_selected"));
    deleteButton.addActionListener(this);
    addDelButtons.add(deleteButton);

    // Panel containing joins and add/delete buttons
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BorderLayout());
    centerPanel.add(joinsPanel, BorderLayout.CENTER);
    centerPanel.add(addDelButtons, BorderLayout.SOUTH);

    // OK, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add tables and buttons to window
    getContentPane().add(centerPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
}

protected void buildJoinsPanel() {
    joinsPanel = new JPanel();
    joinsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    Box panel = Box.createHorizontalBox();
    joinsPanel.add(panel);

    delCheckBoxPanel = Box.createVerticalBox();
    fromPanel = Box.createVerticalBox();
    relationPanel = Box.createVerticalBox();
    toPanel = Box.createVerticalBox();

    fillJoinsPanel();

    panel.add(delCheckBoxPanel);
    panel.add(fromPanel);
    panel.add(relationPanel);
    panel.add(toPanel);
}

protected void fillJoinsPanel() {
    joinFieldsList = new ArrayList<JoinFields>();
    for (Join j : query.joins())
	joinFieldsList.add(new JoinFields(j, report.getDataSource()));

    for (JoinFields jf : joinFieldsList) {
	delCheckBoxPanel.add(jf.del);
	fromPanel.add(jf.from);
	relationPanel.add(jf.relation);
	toPanel.add(jf.to);
    }
}

protected void emptyJoinsPanel() {
    delCheckBoxPanel.removeAll();
    fromPanel.removeAll();
    relationPanel.removeAll();
    toPanel.removeAll();
}

/**
 * Handles add and delete buttons.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals(I18N.get("VisTableWin.add")))
	addNewJoin();
    else if (cmd.equals(I18N.get("VisTableWin.delete_selected")))
	deleteSelectedJoins();
    else
	super.actionPerformed(e);
}

protected void addNewJoin() {
    JoinFields jf = new JoinFields(null, report.getDataSource());
    joinFieldsList.add(jf);

    delCheckBoxPanel.add(jf.del);
    fromPanel.add(jf.from);
    relationPanel.add(jf.relation);
    toPanel.add(jf.to);

    joinsPanel.invalidate();
    pack();
}

@SuppressWarnings("unchecked")
protected void deleteSelectedJoins() {
    ArrayList<JoinFields> copy = (ArrayList<JoinFields>)joinFieldsList.clone();
    for (JoinFields jf : copy) {
	if (jf.del.isSelected()) {
	    joinFieldsList.remove(jf);

	    delCheckBoxPanel.remove(jf.del);
	    fromPanel.remove(jf.from);
	    relationPanel.remove(jf.relation);
	    toPanel.remove(jf.to);
	}
    }
    joinsPanel.invalidate();
    pack();
}

protected void doSave() {
    ArrayList<Join> newJoins = new ArrayList<Join>();
    for (JoinFields jf : joinFieldsList) {
	Column from = columnFromDropdown(jf.from);
	String relation = (String)jf.relation.getSelectedItem();
	Column to = columnFromDropdown(jf.to);

	newJoins.add(new Join(from, relation, to));
    }

    TableJoinCommand cmd = new TableJoinCommand(query, newJoins);
    cmd.perform();
    commands.add(cmd);
}

protected Column columnFromDropdown(JComboBox cb) {
    String sel = (String)cb.getSelectedItem();
    return report.findColumn(sel);
}

protected void doRevert() {
    // Rebuild widgets
    emptyJoinsPanel();
    fillJoinsPanel();
    joinsPanel.invalidate();
    pack();
}

}

