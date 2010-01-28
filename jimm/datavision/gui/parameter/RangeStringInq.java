package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import jimm.util.I18N;
import javax.swing.*;

/**
 * A range string inquisitor knows how to display and control the widgets
 * needed to ask a user for two string parameter values.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class RangeStringInq extends Inquisitor {

protected JTextField rangeFromField;
protected JTextField rangeToField;

RangeStringInq(Parameter param) {
    super(param);

    // Build GUI
//      panel.setLayout(new BorderLayout());

    // Labels
    Box labelBox = Box.createVerticalBox();

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel(I18N.get("GUI.from")));
    labelBox.add(box);

    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel(I18N.get("GUI.to")));
    labelBox.add(box);

    // Edit values
    Box fieldBox = Box.createVerticalBox();

    // From and to
    fieldBox.add(rangeFromField = new JTextField(TEXT_FIELD_COLS));
    fieldBox.add(rangeToField = new JTextField(TEXT_FIELD_COLS));

    // Horizontal box
    Box innerBox = Box.createHorizontalBox();
    panel.add(innerBox);
    innerBox.add(labelBox);
    innerBox.add(Box.createHorizontalStrut(8));
    innerBox.add(fieldBox);

    // Copy default values into "real" values
    parameter.setValue(0, parameter.getDefaultValue(0));
    parameter.setValue(1, parameter.getDefaultValue(1));
}

void copyGUIIntoParam() {
    // setValue translates the string to the appropriate numeric type
    // (integer or float).
    parameter.setValue(0, rangeFromField.getText());
    parameter.setValue(1, rangeToField.getText());
}

void copyParamIntoGUI() {
    rangeFromField.setText(parameter.getValue(0).toString());
    rangeToField.setText(parameter.getValue(1).toString());
}

}
