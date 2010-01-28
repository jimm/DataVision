package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import jimm.util.I18N;
import javax.swing.*;

/**
 * A boolean inquisitor knows how to display and control the widgets needed
 * to ask a user for boolean parameter values.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class BoolInq extends Inquisitor {

protected JRadioButton boolYesRButton;
protected JRadioButton boolNoRButton;

BoolInq(Parameter param) {
    super(param);

    // Build GUI
    Box box = Box.createVerticalBox();
    ButtonGroup bg = new ButtonGroup();

    boolYesRButton = new JRadioButton(I18N.get("GUI.yes"));
    box.add(boolYesRButton);
    bg.add(boolYesRButton);

    boolNoRButton = new JRadioButton(I18N.get("GUI.no"));
    box.add(boolNoRButton);
    bg.add(boolNoRButton);

    panel.add(box);

    // Copy default value into "real" value
    parameter.setValue(0, parameter.getDefaultValue(0));
}

void copyGUIIntoParam() {
    parameter.setValue(0, Boolean.valueOf(boolYesRButton.isSelected()));
}

void copyParamIntoGUI() {
    if (((Boolean)parameter.getValue(0)).booleanValue())
	boolYesRButton.setSelected(true);
    else
	boolNoRButton.setSelected(true);
}

}
