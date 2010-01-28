package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.gui.cmd.BoundsCommand;
import jimm.util.I18N;
import java.awt.BorderLayout;
import javax.swing.*;

/**
 * A field bounds (position and size) editing dialog box.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class BoundsWin extends EditWin implements FieldWalker {

protected static final int TEXT_FIELD_COLS = 8;

protected Field field;
protected jimm.datavision.field.Rectangle origBounds;
protected jimm.datavision.field.Rectangle fieldBounds;
protected JTextField x_text;
protected JTextField y_text;
protected JTextField w_text;
protected JTextField h_text;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param f a field from which we will take the bounds
 */
public BoundsWin(Designer designer, Field f) {
    super(designer, I18N.get("BoundsWin.title"), "BoundsCommand.name");

    field = f;
    origBounds = field.getBounds();
    fieldBounds = new jimm.datavision.field.Rectangle(origBounds);

    buildWindow();
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 */
protected void buildWindow() {
    JPanel editorPanel = buildBoundsEditor();

    // OK, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add values and buttons to window
    getContentPane().add(editorPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
}

protected JPanel buildBoundsEditor() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    x_text = addCoord(panel, I18N.get("BoundsWin.x"), fieldBounds.x,
		      BorderLayout.WEST);
    y_text = addCoord(panel, I18N.get("BoundsWin.y"), fieldBounds.y,
		      BorderLayout.NORTH);
    w_text = addCoord(panel, I18N.get("BoundsWin.width"), fieldBounds.width,
		      BorderLayout.EAST);
    h_text = addCoord(panel, I18N.get("BoundsWin.height"), fieldBounds.height,
		      BorderLayout.SOUTH);

    return panel;
}

protected JTextField addCoord(JPanel parent, String label, double value,
			      String compassPoint)
{
    JPanel coordPanel = new JPanel();
    coordPanel.add(new JLabel(label));
    JTextField text = new JTextField("" + value, TEXT_FIELD_COLS);
    coordPanel.add(text);
    parent.add(coordPanel, compassPoint);
    return text;
}

protected void fillCoords(jimm.datavision.field.Rectangle bounds) {
    x_text.setText("" + bounds.x);
    y_text.setText("" + bounds.y);
    w_text.setText("" + bounds.width);
    h_text.setText("" + bounds.height);
}

protected void doSave() {
    fieldBounds.setBounds(Double.parseDouble(x_text.getText()),
			  Double.parseDouble(y_text.getText()),
			  Double.parseDouble(w_text.getText()),
			  Double.parseDouble(h_text.getText()));

    if (designer.countSelectedFields() == 0
	|| field == field.getReport().getDefaultField()) // "==", not "equals"
	step(field);
    else			// Call step() for all selected fields
	designer.withSelectedFieldsDo(this);
}

/**
 * Creates and performs a command that gives the bounds to the specified
 * field.
 *
 * @param f the field
 */
public void step(Field f) {
    BoundsCommand command = new BoundsCommand(f, fieldBounds);
    command.perform();
    commands.add(command);
}

protected void doRevert() {
    fillCoords(origBounds);
}

}
