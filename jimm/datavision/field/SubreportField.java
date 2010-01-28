package jimm.datavision.field;
import jimm.datavision.Report;
import jimm.datavision.Section;
import jimm.datavision.Subreport;

/**
 * A subreport field represents an entire report within a field. The value
 * of a subreport field holds a report object.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SubreportField extends Field {

protected Subreport subreport;

/**
 * Constructs a text field with the specified id in the specified report
 * section whose text value is <i>value</i>.
 *
 * @param id the new field's id
 * @param report the report containing this element
 * @param section the report section in which the field resides
 * @param value the id of a subreport
 * @param visible show/hide flag
 */
public SubreportField(Long id, Report report, Section section, Object value,
		      boolean visible)
{
    super(id, report, section, value, visible);
    subreport = report.findSubreport(value);
}

public Subreport getSubreport() { return subreport; }

/**
 * This override adds or removes the subreport from its parent report. When
 * <var>s</var> is <code>null</code>, the subreport is removed from the
 * report. Doing this prevents the subreport from being output as part of
 * the report when this field has been removed from the report.
 *
 * @param s the new section; may be <code>null</code>
 */
public void setSection(Section s) {
    if (s == null)
	report.removeSubreport(subreport);
    else {
	// The first time we are added to the report, the subreport has
	// already been added to the report. Avoid adding it again.
	if (report.findSubreport(subreport.getId()) == null)
	    report.addSubreport(subreport);
    }
    super.setSection(s);
}

/**
 * Returns a string containing a line of text for each row returned by
 * the subreport query.
 *
 * @return a string with newlines separating each row of data
 */
public Object getValue() {
    return subreport.getValue();
}

public String dragString() {
    return typeString() + ":" + getId();
}

public String typeString() { return "subreport"; }

public String formulaString() { return "{|subreport " + getId() + "}"; }

}
