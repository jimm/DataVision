package jimm.datavision.gui;
import jimm.datavision.Report;
import jimm.datavision.ErrorHandler;
import jimm.datavision.gui.cmd.Command;
import jimm.util.I18N;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * This is the abstract superclass of windows used for editing paragraphs of
 * code such as formulas and where clauses. The text field accepts dragged
 * report fields by using a {@link DropListenerTextArea}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class CodeEditorWin extends JDialog implements ActionListener {

protected static final Dimension EDIT_SIZE = new Dimension(400, 225);

protected Designer designer;
protected JTextArea codeField;
protected Command command;
protected String errorSuffix;
protected String errorTitle;

/**
 * Constructor.
 *
 * @param designer the design window to which this dialog belongs
 * @param report the report
 * @param initialText the initial text to edit
 * @param title the window title
 * @param errorSuffixKey I18N lookup key for error text suffix; may be
 * <code>null</code>
 * @param errorTitleKey I18N lookup key for error window title; may be
 * <code>null</code>
 */
public CodeEditorWin(Designer designer, Report report, String initialText,
		     String title, String errorSuffixKey, String errorTitleKey)
{
    super(designer.getFrame(), title);
    this.designer = designer;
    errorSuffix = errorSuffixKey == null ? null : I18N.get(errorSuffixKey);
    errorTitle = errorTitleKey == null ? null : I18N.get(errorTitleKey);
    buildWindow(report, initialText);
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 *
 * @param report the report
 * @param initialText initial value of string
 */
protected void buildWindow(Report report, String initialText) {
    codeField =
	new DropListenerTextArea(report,
				 initialText == null ? "" : initialText);
    JScrollPane scroller = new JScrollPane(codeField);
    scroller.setPreferredSize(EDIT_SIZE); // Set preferred size for editor

    // Add edit panel and Ok/Cancel buttons to window
    getContentPane().add(scroller, BorderLayout.CENTER);
    getContentPane().add(buildButtonPanel(), BorderLayout.SOUTH);

    new FocusSetter(codeField);
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
 * Handles the OK and Cancel buttons. If performing the command throws
 * an error, we catch it and display it here and do not close this window.
 *
 * @param e action event
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    try {
	if (I18N.get("GUI.ok").equals(cmd)) {
	    save(codeField.getText());
	    if (command != null)
		designer.performCommand(command);
	    dispose();
	}
	else if (I18N.get("GUI.cancel").equals(cmd)) {
	    dispose();
	}
    }
    catch (Exception ex) {
	String str = ex.toString();
	if (errorSuffix != null) str += "\n" + errorSuffix;
	ErrorHandler.error(str, errorTitle);

	command = null;
    }
}

/**
 * Implement this to do whatevery you gotta do with the text.
 * You'll probably want to set <var>command</var> so it gets sent
 * to the design window to be performed.
 *
 * @param text the text in the edit box
 */
public abstract void save(String text);

}
