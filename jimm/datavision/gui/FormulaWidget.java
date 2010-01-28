package jimm.datavision.gui;
import jimm.datavision.field.Field;
import jimm.datavision.field.FormulaField;
import jimm.util.I18N;
import javax.swing.JDialog;

/**
 *
 * A formula widget must be able to edit its formula's code and perform
 * a few other formula-specific actions.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FormulaWidget extends EditWinWidget {

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param field a report field
 */
public FormulaWidget(SectionWidget sw, Field field) {
    super(sw, field);
}

protected JDialog createEditor() {
    return new FormulaWin(sectionWidget.designer, getField().getReport(),
			  ((FormulaField)getField()).getFormula());
}

protected void updateEditor() {
    ((FormulaWin)editor).update(null, null); // Re-read code
}

protected String getWidgetName() {
    return ((FormulaField)getField()).getFormula().getName();
}

protected String getEditorTitle() {
    return I18N.get("FormulaWidget.editor_title");
}

protected String getEditorLabel() {
    return I18N.get("FormulaWidget.editor_label");
}

public void setWidgetName(String newName) {
    if (newName.length() == 0)
	newName = I18N.get("FieldPickerWin.unnamed_formula");
    ((FormulaField)getField()).getFormula().setName(newName);
}

}
