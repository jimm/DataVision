package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.gui.cmd.SuppressionProcEditCommand;
import jimm.util.I18N;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * An edit dialog for suppression procs.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see jimm.datavision.SuppressionProc
 * @see jimm.datavision.Formula
 * @see jimm.datavision.gui.cmd.SuppressionProcEditCommand
 */
public class SuppressionProcWin extends ScriptEditorWin {

protected static final int CHECK_BOX_INDENT_WIDTH = 32;

protected SectionWidget sectionWidget;
protected JCheckBox hideCheckBox;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param sectionWidget the section widget
 */
public SuppressionProcWin(Designer designer, SectionWidget sectionWidget) {
    super(designer, sectionWidget.getSection().getReport(),
	  sectionWidget.getSection().getSuppressionProc().getFormula()
	      .getEditableExpression(),
	  I18N.get("SuppressionProcWin.title"),
	  "SuppressionProcWin.error_unchanged",
	  "SuppressionProcWin.error_title");

    // Finish GUI setup
    hideCheckBox.setSelected(sectionWidget.getSection().getSuppressionProc()
			     .isHidden());
    enableEditBox();

    this.sectionWidget = sectionWidget;
    setLanguage(sectionWidget.getSection().getSuppressionProc().getFormula()
		.getLanguage());
}

protected JComponent northPanel(Report report) {
    hideCheckBox = new JCheckBox(I18N.get("SuppressionProcWin.always_hide"));
    hideCheckBox.addActionListener(this);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalStrut(CHECK_BOX_INDENT_WIDTH));
    box.add(hideCheckBox);

    JPanel northPanel = new JPanel();
    northPanel.add(box, BorderLayout.WEST);
    northPanel.add(super.northPanel(report), BorderLayout.EAST);

    return northPanel;
}

/**
 * Creates and executes a command that changes the formula's eval string.
 * If there is an error, the command is cancelled (never sent to the
 * design window).
 *
 * @param text the new eval string
 */
public void save(String text) {
    command = new SuppressionProcEditCommand(sectionWidget,
					     hideCheckBox.isSelected(), text,
					     getLanguage());
}

/**
 * Listens for &quot;Always Hide&quot; actions; passes all others on to
 * our superclass.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("SuppressionProcWin.always_hide").equals(cmd))
	enableEditBox();
    else
	super.actionPerformed(e);
}


/**
 * Enables or disables code editor box based on state of &quot;Always
 * Hide&quot; checkbox.
 */
protected void enableEditBox() {
    boolean hide = hideCheckBox.isSelected();
    codeField.setEditable(!hide);
    codeField.setBackground(hide ? Color.lightGray : Color.white);
}

}
