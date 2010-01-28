package jimm.datavision.gui;
import jimm.datavision.field.Field;
import jimm.datavision.field.ParameterField;
import jimm.datavision.gui.parameter.ParamEditWin;
import jimm.util.I18N;
import javax.swing.JDialog;

/**
 *
 * A parameter widget must be able to edit its parameter's settings and
 * perform a few other parameter-specific actions.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ParameterWidget extends EditWinWidget {

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param field a report field
 */
public ParameterWidget(SectionWidget sw, Field field) {
    super(sw, field);
}

protected JDialog createEditor() {
    return new ParamEditWin(sectionWidget.designer,
			    ((ParameterField)getField()).getParameter());
}

protected String getWidgetName() {
    return ((ParameterField)getField()).getParameter().getName();
}

protected String getEditorTitle() {
    return I18N.get("ParameterWidget.editor_title");
}

protected String getEditorLabel() {
    return I18N.get("ParameterWidget.editor_label");
}

public void setWidgetName(String newName) {
    if (newName.length() == 0)
	newName = I18N.get("FieldPickerWin.unnamed_parameter");
    ((ParameterField)getField()).getParameter().setName(newName);
}

}
