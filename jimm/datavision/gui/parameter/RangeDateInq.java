package jimm.datavision.gui.parameter;
import jimm.datavision.Parameter;
import java.util.Date;
import java.util.Calendar;
import javax.swing.*;
import com.toedter.calendar.JCalendar;

/**
 * A range date inquisitor knows how to display and control the widgets
 * needed to ask a user for two date parameter values.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class RangeDateInq extends Inquisitor {

protected JCalendar fromCal;
protected JCalendar toCal;

RangeDateInq(Parameter param) {
    super(param);

    // Build GUI
    Box box = Box.createVerticalBox();
    panel.add(box);

    box.add(fromCal = new JCalendar());
    box.add(Box.createVerticalStrut(8));
    box.add(toCal = new JCalendar());

    // Copy default values into "real" values
    parameter.setValue(0, parameter.getDefaultValue(0));
    parameter.setValue(1, parameter.getDefaultValue(1));
}

void copyGUIIntoParam() {
    parameter.setValue(0, fromCal.getCalendar().getTime());
    parameter.setValue(1, toCal.getCalendar().getTime());
}

void copyParamIntoGUI() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime((Date)parameter.getValue(0));
    fromCal.setCalendar(calendar);

    calendar = Calendar.getInstance();
    calendar.setTime((Date)parameter.getValue(1));
    toCal.setCalendar(calendar);
}

}
