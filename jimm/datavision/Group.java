package jimm.datavision;
import jimm.datavision.field.Field;
import jimm.datavision.source.DataSource;
import jimm.util.XMLWriter;

/**
 * A group uses a {@link Selectable} object to define record grouping. A group
 * may contain multiple header and footer sections.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Group implements Writeable {

/**
 * Sort records by the group's selectable in ascending order (the default).
 */
public static final int SORT_ASCENDING = 0;
/**
 * Sort records by the group's selectable in descending order.
 */
public static final int SORT_DESCENDING = 1;

protected Report report;
protected SectionArea headers;
protected SectionArea footers;
protected Selectable selectable;
protected int sortOrder;
protected Object value;		// Current row value
protected boolean newValue;
protected boolean firstValue;
protected int recordCount;

/**
 * Creates a new group and gives it a header section containing a selectable
 * field and an empty footer section.
 *
 * @param report a report
 * @param selectable a selectable object
 * @return a new group with a header section containing a column field and
 * an empty footer section
 */
public static Group create(Report report, Selectable selectable) {
    Group group = new Group(report, selectable);

    // Create new header section to add to group.
    Section header = new Section(report);

    // Create field for selected selectable. Make it bold.
    Field f = Field.create(null, report, header, selectable.fieldTypeString(),
			   selectable.getId(), true);
    f.getFormat().setBold(true);

    // Add field to header section and add section to group.
    header.addField(f);
    group.headers().add(header);

    // Add empty footer section.
    group.footers().add(new Section(report));

    return group;
}

public static String sortOrderIntToString(int order) {
    return order == SORT_ASCENDING ? "asc" : "desc";
}

public static int sortOrderStringToInt(String order) {
    if (order == null || order.length() == 0)
	return SORT_ASCENDING;
    return "desc".equals(order.toLowerCase())
	? SORT_DESCENDING : SORT_ASCENDING;
}

/**
 * Constructor.
 *
 * @param report a report
 * @param selectable a selectable thingie
 */
public Group(Report report, Selectable selectable) {
    this.report = report;
    this.selectable = selectable;
    sortOrder = SORT_ASCENDING;
    headers = new SectionArea(SectionArea.GROUP_HEADER);
    footers = new SectionArea(SectionArea.GROUP_FOOTER);
}

/**
 * Returns the selectable used by this group.
 *
 * @return a selectable
 */
public Selectable getSelectable() { return selectable; }

/**
 * Sets the selectable used by this group.
 *
 * @param newSelectable the new selectable
 */
public void setSelectable(Selectable newSelectable) { selectable = newSelectable; }

/**
 * Reloads reference to selectable.
 */
public void reloadSelectable(DataSource dataSource) {
    setSelectable(selectable.reloadInstance(dataSource));
}

public String getSelectableName() {
    return selectable.getDisplayName();
}

/**
 * Returns the sort order (either <code>SORT_ASCENDING</code> or
 * <code>SORT_DESCENDING</code>).
 *
 * @return either <code>SORT_ASCENDING</code> or <code>SORT_DESCENDING</code>
 */
public int getSortOrder() { return sortOrder; }

/**
 * Sets the sort order.
 *
 * @param newSortOrder either <code>SORT_ASCENDING</code> or
 * <code>SORT_DESCENDING</code>
 */
public void setSortOrder(int newSortOrder) { sortOrder = newSortOrder; }

/**
 * Returns the value of this group's selectable. Only valid while
 * the report is running.
 *
 * @return the value of the selectable; undefined when the report
 * is not running
 */
public Object getValue() { return value; }

/**
 * Sets the group value that is returned by <code>getValue</code>. This
 * method should only be called by the report while it is running.
 *
 * @param report the report from which we retrieve our selectable's value
 */
public void setValue(Report report) {
    Object val = selectable.getValue(report);
    if (value == null) {
	value = val;
	firstValue = true;
	newValue = true;
    }
    else if (value.equals(val)) {
	newValue = false;
	firstValue = false;
    }
    else {
	value = val;
	newValue = true;
	firstValue = false;
    }
}

public void updateCounter() {
    if (newValue)
	recordCount = 1;
    else
	++recordCount;
}

/**
 * Returns <code>true</code> when the value of the selectable has
 * changed.
 *
 * @return <code>true</code> when the value of the selectable has
 * changed 
 */
public boolean isNewValue() { return newValue; }

/**
 * Returns the number of records in the group so far.
 *
 * @return the number of records in the group so far
 */
public int getRecordCount() { return recordCount; }

/**
 * Layout engines need to call this method when a group's footer is being
 * output not because this group has a new value but because some previous
 * group's value changed and we want to output this group's footer.
 *
 * @see jimm.datavision.layout.LayoutEngine#groupFooters
 */
public void forceFooterOutput() { newValue = true; }

/**
 * Returns <code>true</code> when this is the first value ever seen
 * by the group during a report run.
 *
 * @return <code>true</code> if this is the first value ever seen
 */
public boolean isFirstValue() { return firstValue; }

/**
 * Returns the headers.
 *
 * @return the headers section area
 */
public SectionArea headers() { return headers; }

/**
 * Returns the footers.
 *
 * @return the footers section area
 */
public SectionArea footers() { return footers; }

/**
 * Returns <code>true</code> if the specified section is inside this group,
 * either as a header or a footer.
 *
 * @param s a section
 * @return <code>true</code> if the section is within this group
 */
public boolean contains(Section s) {
    return headers.contains(s) || footers.contains(s);
}

/**
 * Called by a report when it starts running, this method prepares the
 * group for use.
 */
public void reset() {
    value = null;
    newValue = firstValue = true;
    recordCount = 1;
}

/**
 * Writes this group as an XML tag. Asks each section to write itself
 * as well.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("group");
    out.attr("groupable-id", selectable.getId());
    out.attr("groupable-type", selectable.fieldTypeString());
    out.attr("sort-order", sortOrderIntToString(sortOrder));

    ListWriter.writeList(out, headers.sections(), "headers");
    ListWriter.writeList(out, footers.sections(), "footers");

    out.endElement();
}

}
