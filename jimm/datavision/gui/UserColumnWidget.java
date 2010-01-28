package jimm.datavision.gui;
import jimm.datavision.field.Field;
import jimm.datavision.field.UserColumnField;
import jimm.util.I18N;
import javax.swing.JDialog;

/**
 *
 * A user column widget must be able to edit its user column's code and
 * perform a few other user column-specific actions.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class UserColumnWidget extends EditWinWidget {

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param field a report field
 */
public UserColumnWidget(SectionWidget sw, Field field) {
    super(sw, field);
}

protected JDialog createEditor() {
    return new UserColumnWin(sectionWidget.designer, getField().getReport(),
			     ((UserColumnField)getField()).getUserColumn());
}

protected void updateEditor() {
    ((UserColumnWin)editor).update(null, null); // Re-read code
}

protected String getWidgetName() {
    return ((UserColumnField)getField()).getUserColumn().getName();
}

protected String getEditorTitle() {
    return I18N.get("UserColumnWidget.editor_title");
}

protected String getEditorLabel() {
    return I18N.get("UserColumnWidget.editor_label");
}

public void setWidgetName(String newName) {
    if (newName.length() == 0)
	newName = I18N.get("FieldPickerWin.unnamed_usercol");
    ((UserColumnField)getField()).getUserColumn().setName(newName);
}

}
