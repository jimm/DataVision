package jimm.datavision.gui;
import jimm.datavision.field.Field;
import jimm.datavision.gui.cmd.WidgetRenameCommand;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

/**
 *
 * An abstract superclass for widgets that open separate windows used to
 * edit the widget.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class EditWinWidget extends FieldWidget {

protected JDialog editor;

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param field a report field
 */
EditWinWidget(SectionWidget sw, Field field) {
    super(sw, field);
}

protected void addCustomPopupItems() {
    MenuUtils.addToMenu(this, popup, "EditWinWidget.popup_edit", POPUP_FONT);
    MenuUtils.addToMenu(this, popup, "EditWinWidget.popup_rename", POPUP_FONT);
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
	openEditor();
    else if ("rename".equals(command))
	rename();
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
    if (sectionWidget.designer.isPlacingNewTextField())
	sectionWidget.createNewTextField(e);
    if (e.getClickCount() == 2)
	openEditor();
    else
	super.mouseClicked(e);
}

/**
 * Makes our text editable and starts editing.
 */
public void openEditor() {
    if (editor == null)
	editor = createEditor();
    else
	updateEditor();
    editor.setVisible(true);
    editor.toFront();
}

/**
 * Creates and returns a new frame suitable for editing this widget.
 *
 * @return a frame (window or dialog) used to edit the widget
 */
protected abstract JDialog createEditor();

/**
 * Updates the editor. This method supplies default do-nothing behavior.
 */
protected void updateEditor() {}

/**
 * Opens a name editor.
 */
protected void rename() {
    Designer designer = sectionWidget.designer;
    String name = new AskStringDialog(designer.getFrame(), getEditorTitle(),
				      getEditorLabel(), getWidgetName())
	.getString();
    if (name != null)
	designer.performCommand(new WidgetRenameCommand(this, getWidgetName(),
							name));
}

/**
 * Returns the name string.
 *
 * @return the name to be edited
 */
protected abstract String getWidgetName();

/**
 * Returns the name edit window's title.
 */
protected abstract String getEditorTitle();

/**
 * Returns the name edit window's label (the prompt that goes before the
 * text edit field).
 */
protected abstract String getEditorLabel();

/**
 * Set editable object's name. The new name is guaranteed not to be
 * <code>null</code>.
 *
 * @param newName the new name string
 */
public abstract void setWidgetName(String newName);

}
