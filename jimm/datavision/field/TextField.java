package jimm.datavision.field;
import jimm.datavision.Report;
import jimm.datavision.Section;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.TextFieldWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.util.XMLWriter;

/**
 * A text field represents static text. The value of a text field holds the
 * text.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class TextField extends Field {

/**
 * Constructs a text field with the specified id in the specified report
 * section whose text value is <i>value</i>.
 *
 * @param id the new field's id
 * @param report the report containing this element
 * @param section the report section in which the field resides
 * @param value the text string
 * @param visible show/hide flag
 */
public TextField(Long id, Report report, Section section, Object value,
		 boolean visible)
{
    super(id, report, section, value, visible);
}

public void setValue(Object newValue) {
    String newString = (newValue == null) ? null : newValue.toString();
    if (value != newString && (value == null || !value.equals(newString))) {
	value = newString;
	setChanged();
	notifyObservers();
    }
}

public FieldWidget makeWidget(SectionWidget sw) {
    return new TextFieldWidget(sw, this);
}

public String dragString() {
    return typeString() + ":" + value;
}

public String typeString() { return "text"; }

public String designLabel() { return value.toString(); }

/**
 * Returns a string representing the field as it appears in a formula.
 * We need to escape quotes in the string.
 *
 * @return a string useful in a formula
 */
public String formulaString() {
    String str = value.toString();

    int pos = str.indexOf('"');
    int len = str.length();
    if (pos == -1 || len == 0)
	return "\"" + str + "\"";

    StringBuffer buf = new StringBuffer("\"");
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	if (c == '"')
	    buf.append("\\\"");
	else
	    buf.append(c);
    }
    buf.append("\"");
    return buf.toString();
}

public void writeXML(XMLWriter out) {
    out.startElement("field");
    out.attr("id", id);
    out.attr("type", typeString());
    out.cdataElement("text", value.toString());
    writeFieldGuts(out);
    out.endElement();
}

}
