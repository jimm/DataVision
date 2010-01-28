package jimm.datavision.field;
import jimm.util.StringUtils;
import java.awt.FontMetrics;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import javax.swing.JLabel;

/**
 * Helps avoid multiple expensive formatting and font size calculations. Each
 * {@link Field} holds on to one of these and asks it for the formatted
 * (wrapped) version of its value or the height needed to output the formatted
 * value.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class FormattedValueCache implements Observer {

protected static final double LINE_SIZE_FUDGE_FACTOR = 1.2;
protected static final HashMap decimalFormatters = new HashMap();
protected static final HashMap dateFormatters = new HashMap();
protected static JLabel wrappingCalculationsLabel;

protected Field field;
protected Object value;
protected String formatted;
protected double height;

FormattedValueCache(Field f) {
    field = f;
    if (field != null) {
	field.addObserver(this);
	if (field.getFormat() != null)
	    field.getFormat().addObserver(this);
    }
}

protected void finalize() throws Throwable {
    if (field != null) {
	field.deleteObserver(this);
	if (field.getFormat() != null)
	    field.getFormat().deleteObserver(this);
    }
}

/** When format changes, erase value so we recalculate it */
public void update(Observable o, Object arg) {
    if (field != null && field.getFormat() != null)
	value = null;
}

String getFormattedString(Object val) {
    if (notSameAs(val)) {
	value = val;
	calcValues();
    }
    return formatted;
}

double getOutputHeight(Object val) {
    if (notSameAs(val)) {
	value = val;
	calcValues();
    }
    return height;
}

protected boolean notSameAs(Object otherValue) {
    if (value == null)
	return otherValue != null;
    else
	return !value.equals(otherValue);
}

/**
 * Cacluates formatted (wrapped) string and output height.
 */
void calcValues() {
    if (value == null) {
	formatted = null;
	height = 0;
	return;
    }

    Format format = field.getFormat();
    String fmtStr = format.getFormat();

    if (value instanceof Number) {
	if (fmtStr == null) formatted = value.toString();
	else formatted = getNumberFormatterFor(fmtStr).format((Number)value);
    }
    else if (value instanceof Date) {
	if (fmtStr == null) formatted = value.toString();
	else formatted = getDateFormatterFor(fmtStr).format((Date)value);
    }
    else {
	formatted = value.toString();
	if (format.isWrap()) {
	    FontMetrics fm =
	      getWrappingCalcsLabel().getFontMetrics(format.getFont());
	    formatted =
		StringUtils.join(StringUtils.wrap(formatted, fm,
						  (int)field.getBounds().width),
				 "\n");
	}
    }

    // Height calculation. Field's bounds height is the minimum height.
    height = field.bounds.height;
    if (formatted.length() != 0) {
	List lines = StringUtils.splitIntoLines(formatted);
	double h = lines.size() * format.getSize() * LINE_SIZE_FUDGE_FACTOR;
	if (h > height)
	    height = h;
    }
}

protected DecimalFormat getNumberFormatterFor(String formatString) {
    DecimalFormat formatter =
	(DecimalFormat)decimalFormatters.get(formatString);
    if (formatter == null) {
	formatter = new DecimalFormat(formatString);
	decimalFormatters.put(formatString, formatter);
    }
    return formatter;
}

protected SimpleDateFormat getDateFormatterFor(String formatString) {
    SimpleDateFormat formatter =
	(SimpleDateFormat)dateFormatters.get(formatString);
    if (formatter == null) {
	formatter = new SimpleDateFormat(formatString);
	dateFormatters.put(formatString, formatter);
    }
    return formatter;
}

protected static JLabel getWrappingCalcsLabel() {
    if (wrappingCalculationsLabel == null) // Lazy instantiation
	wrappingCalculationsLabel = new JLabel();
    return wrappingCalculationsLabel;
}
}
