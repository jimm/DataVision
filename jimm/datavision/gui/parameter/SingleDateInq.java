package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import java.util.Date;
import java.util.Calendar;
import javax.swing.*;
import com.toedter.calendar.JCalendar;

/**
 * A single date inquisitor knows how to display and control the widgets
 * needed to ask a user for a date parameter value.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class SingleDateInq extends Inquisitor {

protected JCalendar cal;

SingleDateInq(Parameter param) {
    super(param);

    // Build GUI. Use current date as default value.
    JPanel innerPanel = new JPanel();
    innerPanel.add(cal = new JCalendar());
    panel.add(innerPanel);

    // Copy default value into "real" value. Default value is the
    // current date and time.
    parameter.setValue(0, parameter.getDefaultValue(0));
}

void copyGUIIntoParam() {
    parameter.setValue(0, cal.getCalendar().getTime());
}

void copyParamIntoGUI() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime((Date)parameter.getValue(0));
    cal.setCalendar(calendar);
}

}
