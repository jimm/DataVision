package jimm.datavision;
import jimm.util.I18N;
import jimm.util.XMLWriter;
import java.util.*;

/*
 * A section area holds an ordered list of {@see Section}s and knows its
 * name and type.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SectionArea implements Writeable {

public static final int REPORT_HEADER = 0;
public static final int REPORT_FOOTER = 1;
public static final int PAGE_HEADER = 2;
public static final int PAGE_FOOTER = 3;
public static final int DETAIL = 4;
public static final int GROUP_HEADER = 5;
public static final int GROUP_FOOTER = 6;

/**
 * These are I18N lookup keys for <code>REPORT_*</code> constants.
 * The order of these keys must match the values of those constants.
 */
protected static final String[] AREA_NAME_KEYS = {
    "Report.report_header",
    "Report.report_footer",
    "Report.page_header",
    "Report.page_footer",
    "Report.detail",
    "Report.group_header",
    "Report.group_footer"
};

List sections;
/** One of the <code>REPORT_*</code> constants. */
int area;

/**
 * Given the <code>REPORT_*</code> constant <var>area</var>,
 * returns the section area name.
 *
 * @param area a <code>REPORT_*</code> constant
 */
public static String nameFromArea(int area) {
    return I18N.get(AREA_NAME_KEYS[area]);
}

/**
 * Constructor.
 *
 * @param area a <code>REPORT_*</code> constant
 */
public SectionArea(int area) {
    this.area = area;
    sections = new ArrayList();
}

public int getArea() {
    return area;
}

public int indexOf(Section s) {
    return sections.indexOf(s);
}

public Section get(int index) {
    return (Section)sections.get(index);
}

public Section first() {
    return (Section)sections.get(0);
}

/**
 * Adds a section to our list and sets its name and other area-related
 * information.
 *
 * @param s a section
 */
public void add(Section s) {
    sections.add(s);
    imprint(s);
}

/**
 * Adds a section to our list and sets its name and other area-related
 * information.
 *
 * @param s a section
 */
public void add(int index, Section s) {
    sections.add(index, s);
    imprint(s);
}

/**
 * Adds a (possibly created) section after <var>afterThis</var> and
 * returns the section. If <var>section</var> is <code>null</code>,
 * a new section will be created.
 *
 * @param section the section to insert; if <code>null</code>, a new
 * section will be created
 * @param afterThis the new section will be inserted after this one
 */
public Section insertAfter(Section section, Section afterThis) {
    if (section == null) {
	if (afterThis != null)
	    section = new Section(afterThis.getReport());
	else
	    throw new IllegalArgumentException("SectionArea.insertAfter:" +
					       " both section and afterThis" +
					       " can't be null");
    }

    sections.add(sections.indexOf(afterThis) + 1, section);
    imprint(section);
    return section;
}

/**
 * Modifies <var>section</var> so it knows that it's part of this area.
 *
 * @param section a section
 */
protected void imprint(Section section) {
    section.setArea(this);
}

/**
 * Returns <code>true</code> if this is a report detail section.
 *
 * @return <code>true</code> if this is a report detail section
 */
public boolean isDetail() {
    return area == DETAIL;
}

/**
 * Removes a section.
 *
 * @param s a report section
 */
public void remove(Section s) {
    sections.remove(s);
    s.setArea(null);
}

/**
 * Returns <code>true</code> if <var>s</var> is one of our sections.
 *
 * @param s a section
 * @return <code>true</code> if <var>s</var> is one of our sections
 */
public boolean contains(Section s) {
    return sections.contains(s);
}

/**
 * Returns the name of this area.
 *
 * @return the name of this area
 */
public String getName() {
    return nameFromArea(area);
}

/**
 * Returns an unmodifiable version of our list of sections.
 *
 * @return an unmodifiable version of our list of sections.
 */
public List sections() {
    return Collections.unmodifiableList(sections);
}

public Iterator iterator() {
    return sections.iterator();
}

public int size() {
    return sections.size();
}

public boolean isEmpty() {
    return sections.isEmpty();
}

public void clear() {
    for (Iterator iter = sections.iterator(); iter.hasNext(); )
	((Section)iter.next()).setArea(null);
    sections.clear();
}

public void withSectionsDo(SectionWalker s) {
    for (Iterator iter = sections.iterator(); iter.hasNext(); )
	s.step((Section)iter.next());
}

public void writeXML(XMLWriter out) {
    ListWriter.writeList(out, sections);
}

}
