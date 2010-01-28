package jimm.datavision.gui;
import jimm.datavision.field.Field;
import jimm.datavision.gui.cmd.TypingCommand;
import jimm.util.I18N;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

/**
 *
 * A text field widget is a field widget that is editable.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class TextFieldWidget extends FieldWidget {

protected int origHeight;
protected int lineHeight;
protected boolean changingEditState;
protected TypingCommand typingCommand;

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param field a report field
 */
public TextFieldWidget(SectionWidget sw, Field field) {
    super(sw, field);

    // Calculate line height.
    Font f = field.getFormat().getFont();
    FontMetrics fm = swingField.getComponent().getFontMetrics(f);
    lineHeight = fm.getHeight();
}

protected String getPopupNameText() {
    // This string is now found in the menu properties file.
    return I18N.get(I18N.MENU_FILE_PREFIX, "TextFieldWidget.popup_name");
}

protected void addCustomPopupItems() {
    MenuUtils.addToMenu(this, popup, "TextFieldWidget.popup_edit", POPUP_FONT);
    popup.addSeparator();
}

/**
 * Performs some action based on the action command string (the menu
 * item text).
 */
public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command == null) return;

    if ("edit".equals(command))
	startEditing();
    else
	super.actionPerformed(e);
}

/**
 * if this is a double-click, start editing; else handle the mouse event
 * like a normal field widget.
 *
 * @param e mouse event
 */
public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 2)
	startEditing();
    else
	super.mouseClicked(e);
}

/**
 * Makes our text editable and starts editing.
 */
public void startEditing() {
    // Bug fix: endEditing is called while startEditing is running
    synchronized(this) {
	if (changingEditState)
	    return;
	changingEditState = true;
    }

    if (!selected)
	select(true);

    JTextPane textPane = (JTextPane)getComponent();
    textPane.setEditable(true);
    textPane.getCaret().setVisible(true);
    textPane.requestFocus();
    origHeight = textPane.getBounds().height;

    sectionWidget.designer.setIgnoreKeys(true);

    textPane.removeMouseListener(this);
    textPane.removeMouseMotionListener(this);

    changingEditState = false;

    typingCommand = new TypingCommand(this, origHeight);
}

/**
 * Stores new value and bounds from self into field and makes text
 * non-editable.
 */
protected void endEditing() {
    // Bug fix: endEditing is called while startEditing is running
    synchronized(this) {
	if (changingEditState)
	    return;
	changingEditState = true;
    }

    if (typingCommand != null) {
	getSectionWidget().performCommand(typingCommand);
	typingCommand = null;
    }

    changingEditState = false;
}

/**
 * Perform selection; if becoming deselected, ends editing.
 *
 * @param makeSelected new selection state
 */
protected void doSelect(boolean makeSelected) {
    if (makeSelected == false)
	endEditing();
    super.doSelect(makeSelected);
}

/**
 * If this field is being edited, show text cursor. Else call superclass
 * method.
 *
 * @param e a mouse event
 */
protected void cursorForPosition(MouseEvent e) {
    JTextPane textPane = (JTextPane)getComponent();
    if (textPane.isEditable())
	textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    else
	super.cursorForPosition(e);
}

/**
 * Handles return key by expanding height of editor and backspace and
 * delete by shrinking height if newline is deleted.
 *
 * @param e key event
 */
public void keyTyped(KeyEvent e) {
    char c = e.getKeyChar();
    int pos;
    java.awt.Rectangle bounds;
    JTextPane textPane = (JTextPane)getComponent();

    switch (c) {
    case KeyEvent.VK_ENTER:
	// We've just grown. Make sure we still fit.
	bounds = getComponent().getBounds();
	bounds.height += lineHeight;
	getComponent().setBounds(bounds);

	// Make sure we still fit in section
	sectionWidget.growToFit();
	break;
    case KeyEvent.VK_BACK_SPACE:
	try {
	    pos = textPane.getCaretPosition();
	    if (pos > 0 && textPane.getText(pos - 1, 1).equals("\n")) {
		bounds = textPane.getBounds();
		bounds.height -= lineHeight;
		textPane.setBounds(bounds);
	    }
	}
	catch (BadLocationException ex) {}
	break;
    case KeyEvent.VK_DELETE:
	try {
	    pos = textPane.getCaretPosition();
	    if (pos < textPane.getText().length()
		&& textPane.getText(pos, 1).equals("\n"))
	    {
		bounds = textPane.getBounds();
		bounds.height -= lineHeight;
		textPane.setBounds(bounds);
	    }
	}
	catch (BadLocationException ex2) {}
	break;
    }
}

}
