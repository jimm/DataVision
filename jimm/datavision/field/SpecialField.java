package jimm.datavision.field;
import jimm.datavision.Report;
import jimm.datavision.Section;
import jimm.datavision.Group;
import jimm.util.I18N;
import java.util.HashMap;

/**
 * A SpecialField represents a special value such as the report name or
 * current page number. The value of a SpecialField is a string that
 * identifies which value to display.
 * <ul>
 * <li>report.name</li>
 * <li>report.title</li>
 * <li>report.author</li>
 * <li>report.description</li>
 * <li>report.date</li>
 * <li>report.row</li>
 * <li>page.number</li>
 * <li>group.count</li>
 * </ul>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SpecialField extends Field {

public static final String TYPE_STRING = "special";

/**
 * Returns an array of all of the special field names. We read the map
 * each time because (in theory) the language could have changed and
 * all the strings could be different.
 *
 * @return an array strings containing all of the special field names
 */
public static HashMap specialFieldNames() {
    HashMap map = new HashMap();

    map.put("report.title", I18N.get("SpecialField.report.title"));
    map.put("report.name", I18N.get("SpecialField.report.name"));
    map.put("report.author", I18N.get("SpecialField.report.author"));
    map.put("report.description", I18N.get("SpecialField.report.description"));
    map.put("report.date", I18N.get("SpecialField.report.date"));
    map.put("report.row", I18N.get("SpecialField.report.row"));
    map.put("page.number", I18N.get("SpecialField.page.number"));
    map.put("group.count", I18N.get("SpecialField.group.count"));

    return map;
}

/**
 * Returns the value associated with the given special field name string.
 *
 * @param str the special field name string
 * @param report the report that is running
 * @return a string
 */
public static Object value(Field f, String str, Report report) {
    if ("report.title".equals(str))
	return report.getTitle();
    if ("report.name".equals(str))
	return report.getName();
    if ("report.author".equals(str))
	return report.getAuthor();
    if ("report.description".equals(str))
	return report.getDescription();
    else if ("report.date".equals(str))
	return new java.util.Date();
    else if ("report.row".equals(str))
	return new Integer(report.rowNumber());
    else if ("page.number".equals(str))
	return new Integer(report.pageNumber());
    else if ("group.count".equals(str))
	return groupCount(f);
    else
	return I18N.get("SpecialField.unknown");
}

/**
 * Returns the group count for the group in which this field resides. If we
 * are in the detail section, return the count of the innermost group. Else
 * If the group is <code>null</code> (for example, we are in the report
 * footer), returns the report.row value. If <var>f</var> is
 * <code>null</code>, return 0.
 *
 * @param f a field
 * @return the number of records in the group or the current record number
 * within the group.
 */
protected static Integer groupCount(Field f) {
    if (f == null)
	return new Integer(0);

    Report report = f.getReport();
    Group group = report.findGroup(f.getSection());

    if (group == null && f.getSection().isDetail())
	group = report.innermostGroup(); // May be null

    return new Integer(group == null ? report.rowNumber()
		       : group.getRecordCount());
}


/**
 * Constructs a special field with the specified id in the specified report
 * section whose special value is <i>value</i>.
 *
 * @param id the new field's id
 * @param report the report containing this element
 * @param section the report section in which the field resides
 * @param value the magic string
 * @param visible show/hide flag
 */
public SpecialField(Long id, Report report, Section section, Object value,
		    boolean visible)
{
    super(id, report, section, value, visible);
}

public String dragString() {
    return typeString() + ":" + value;
}

public String typeString() { return TYPE_STRING; }

public String designLabel() { return "{" + value + "}"; }

public String formulaString() { return "{%" + value + "}"; }

/**
 * Returns the value of this field.
 *
 * @return a string
 */
public Object getValue() {
    return SpecialField.value(this, (String)value, section.getReport());
}
}
