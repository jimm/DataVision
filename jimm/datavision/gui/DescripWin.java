package jimm.datavision.gui;
import jimm.datavision.Report;
import jimm.datavision.gui.cmd.SummaryCommand;
import jimm.util.I18N;
import java.awt.BorderLayout;
import javax.swing.*;

/**
 * A report description (name, title, author, etc.) editing dialog box.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DescripWin extends EditWin {

protected static final int TEXT_FIELD_COLS = 32;
protected static final int TEXT_AREA_ROWS = 6;
protected static final int TEXT_AREA_COLS = 32;

protected Report report;
protected JTextField nameField;
protected JTextField titleField;
protected JTextField authorField;
protected JTextArea descriptionField;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report report (I love useful, thoughtful comments)
 */
public DescripWin(Designer designer, Report report) {
    super(designer, I18N.get("DescripWin.title"), "SummaryCommand.name");

    this.report = report;

    buildWindow();
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 */
protected void buildWindow() {
    // All edit fields
    JPanel editorPanel = buildEditor();

    // OK, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add values and buttons to window
    getContentPane().add(editorPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
}

protected JPanel buildEditor() {
    EditFieldLayout efl = new EditFieldLayout();

    nameField = efl.addTextField(I18N.get("DescripWin.report_name"),
				 report.getName(), TEXT_FIELD_COLS);
    titleField = efl.addTextField(I18N.get("DescripWin.report_title"),
				  report.getTitle(), TEXT_FIELD_COLS);
    authorField = efl.addTextField(I18N.get("DescripWin.author_name"),
				   report.getAuthor(), TEXT_FIELD_COLS);
    descriptionField = efl.addTextArea(I18N.get("DescripWin.description"),
				       report.getDescription(),
				       TEXT_AREA_ROWS, TEXT_AREA_COLS);

    return efl.getPanel();
}

protected void fillEditFields() {
    nameField.setText(report.getName());
    titleField.setText(report.getTitle());
    authorField.setText(report.getAuthor());
    descriptionField.setText(report.getDescription());
}

protected void doSave() {
    SummaryCommand cmd =
	new SummaryCommand(report, nameField.getText(), titleField.getText(),
			   authorField.getText(), descriptionField.getText());
    cmd.perform();
    commands.add(cmd);
}

protected void doRevert() {
    fillEditFields();
}

}
