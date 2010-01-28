package jimm.datavision.gui.cmd;
import jimm.datavision.field.Rectangle;
import jimm.datavision.gui.SectionWidget;
import jimm.datavision.gui.TextFieldWidget;
import jimm.util.I18N;
import javax.swing.JTextPane;

/**
 * Moves a single field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class TypingCommand extends CommandAdapter {

protected TextFieldWidget fw;
protected SectionWidget sw;
protected String oldText;
protected String newText;
protected Rectangle oldBounds;
protected Rectangle newBounds;
protected int origHeight;
protected SectionResizeCommand sectionResizeCommand;

public TypingCommand(TextFieldWidget fw, int origHeight) {
    super(I18N.get("TypingCommand.name"));

    this.fw = fw;
    this.origHeight = origHeight;
    sw = fw.getSectionWidget();

    oldText = (String)fw.getField().getValue();
    oldBounds = new Rectangle(fw.getField().getBounds()); // Make a copy
    sectionResizeCommand = new SectionResizeCommand(sw);
}

/**
 * Saves new text. The type has already been performed by the user.
 */
public void perform() {
    JTextPane textPane = (JTextPane)fw.getComponent();
    if (textPane.isEditable()) {
	// If we wait until later to grab the text, then we loose text with
	// newlines in it. I don't know why. Go figure. It may be the
	// updates caused by the bounds change that cause it; I don't know.
	newText = textPane.getText();

	textPane.setEditable(false);
	textPane.getCaret().setVisible(false);

	newBounds = new Rectangle(textPane.getBounds());
	if (newBounds.height != origHeight)
	    fw.getField().getBounds().setBounds(newBounds);

	// Set text after settings bounds because setting the text triggers
	// an update which in turn re-sets the field's bounds, erasing any
	// changes we made in keyTyped().
	//
	// See also the note above: we must retrieve the text from the edit
	// widget before getting here.
	fw.getField().setValue(newText);

	fw.getSectionWidget().setIgnoreKeys(false);

	textPane.addMouseListener(fw);
	textPane.addMouseMotionListener(fw);
    }

    sectionResizeCommand.perform();
}

public void undo() {
    fw.getField().setValue(oldText);
    fw.getField().getBounds().setBounds(oldBounds);

    sectionResizeCommand.undo();
}

public void redo() {
    fw.getField().setValue(newText);
    fw.getField().getBounds().setBounds(newBounds);
    sectionResizeCommand.redo();
}

}
