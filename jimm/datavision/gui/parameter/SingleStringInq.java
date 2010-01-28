package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import javax.swing.*;

/**
 * A single string inquisitor knows how to display and control the widgets
 * needed to ask a user for a string parameter value.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class SingleStringInq extends Inquisitor {

protected JTextField textField;

SingleStringInq(Parameter param) {
    super(param);
    // Build GUI
    panel.add(textField = new JTextField(TEXT_FIELD_COLS));
    // Fill in default value
    textField.setText(parameter.getDefaultValue(0).toString());

    // Copy default value into "real" value
    parameter.setValue(0, parameter.getDefaultValue(0));
}

void copyGUIIntoParam() {
    // setValue translates the string to the appropriate numeric type
    // (integer or float).
    parameter.setValue(0, textField.getText());
}

void copyParamIntoGUI() {
    textField.setText(parameter.getValue(0).toString());
}

}
