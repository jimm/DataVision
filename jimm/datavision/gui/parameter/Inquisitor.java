package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import jimm.util.I18N;
import javax.swing.*;

/**
 * An inquisitor knows how to display and control the widgets needed
 * to ask a user for parameter values. Inquisitors are used in
 * {@link ParamAskWin}s.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
abstract class Inquisitor {

protected static final int TEXT_FIELD_COLS = 24;

protected static int instanceCount;
protected Parameter parameter;
protected JPanel panel;
protected String panelName;

/**
 * This factory method returns the proper inquisitor for the specified
 * parameter.
 *
 * @param param a parameter
 */
public static Inquisitor create(Parameter param) {
    if (!param.isLegal(param.getType(), param.getArity())) {
	// Should not happen
	String str = I18N.get("Inquisitor.param_cap") + ' ' + param.getName()
	    + ' ' + I18N.get("Inquisitor.illegal");
	throw new IllegalArgumentException(str);
    }

    switch (param.getType()) {
    case Parameter.TYPE_BOOLEAN:
	return new BoolInq(param);
    case Parameter.TYPE_STRING:
	switch (param.getArity()) {
	case Parameter.ARITY_ONE: return new SingleStringInq(param);
	case Parameter.ARITY_RANGE: return new RangeStringInq(param);
	case Parameter.ARITY_LIST_SINGLE:
	    return new ListStringInq(param, false);
	case Parameter.ARITY_LIST_MULTIPLE:
	    return new ListStringInq(param, true);
	}
	break;
    case Parameter.TYPE_NUMERIC:
	switch (param.getArity()) {
	case Parameter.ARITY_ONE: return new SingleNumericInq(param);
	case Parameter.ARITY_RANGE: return new RangeNumericInq(param);
	case Parameter.ARITY_LIST_SINGLE:
	    return new ListNumericInq(param, false);
	case Parameter.ARITY_LIST_MULTIPLE:
	    return new ListNumericInq(param, true);
	}
	break;
    case Parameter.TYPE_DATE:
	switch (param.getArity()) {
	case Parameter.ARITY_ONE: return new SingleDateInq(param);
	case Parameter.ARITY_RANGE: return new RangeDateInq(param);
	}
	break;
    }
    return null;		// Not reached
}

/**
 * Constructor.
 *
 * @param param the parameter we're going to edit
 */
Inquisitor(Parameter param) {
    parameter = param;
    panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    panelName = "" + instanceCount++;
}

JPanel getPanel() { return panel; }

String getPanelName() { return panelName; }

/**
 * Copy all values from our GUI widgets into the parameter's value(s).
 */
abstract void copyGUIIntoParam();

/**
 * Copy all values from parameter's value(s) into our GUI widgets.
 */
abstract void copyParamIntoGUI();

}
