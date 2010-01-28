package jimm.datavision;
import jimm.datavision.field.Field;
import jimm.datavision.field.FormulaField;
import jimm.util.XMLWriter;
import java.util.*;

/**
 * A section of a report contains a group of fields and suppression
 * information. Sections include page headers and footers, report headers
 * and footers, group headers and footers, and details. Sections contain
 * elements (fields and lines).
 *
 * @see Report
 * @see Field
 * @see Line
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Section
    extends Observable
    implements Writeable, Observer
{

protected static final double DEFAULT_HEIGHT = 20;

protected Report report;
protected SectionArea area;
protected double minHeight;
protected ArrayList fields;
protected ArrayList lines;
protected SuppressionProc suppressionProc;
protected boolean pageBreak;

/**
 * Constructor.
 *
 * @param r the report containing this section
 */
public Section(Report r) {
    report = r;
    minHeight = DEFAULT_HEIGHT;
    fields = new ArrayList();
    lines = new ArrayList();
    suppressionProc = new SuppressionProc(report);
}

public void update(Observable o, Object arg) {
    setChanged();
    notifyObservers(arg);
}

/**
 * Returns the report containing this section.
 */
public Report getReport() { return report; }

/**
 * Returns the min height of this section.
 *
 * @return the min height
 */
public double getMinHeight() { return minHeight; }

/**
 * Sets the min height.
 *
 * @param newMinHeight the new min height
 */
public void setMinHeight(double newMinHeight) {
    if (minHeight != newMinHeight) {
	minHeight = newMinHeight;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the height of this section: not the minimum height defined
 * when the report was designed but rather the height necessary to
 * output this section. The height returned is the maximum of
 * <var>minHeight</var> and the highest y coordinate of any field.
 *
 * @return the height this section will need to be output
 */
public double getOutputHeight() {
    double h = minHeight;
    for (Iterator iter = fields.iterator(); iter.hasNext(); ) {
	Field f = (Field)iter.next();
	double y = f.getBounds().y + f.getOutputHeight();
	if (y > h) h = y;
    }
    return h;
}

/**
 * Returns the width of this section.
 *
 * @return the width
 */
public double getWidth() { return report.getPaperFormat().getWidth(); }

/**
 * Returns the area this section is contained within.
 *
 * @return the area this section is contained within
 */
public SectionArea getArea() { return area; }

/**
 * Sets the area this section is contained within and notifies any observers
 * of the change.
 *
 * @param area a section area
 */
public void setArea(SectionArea area) {
    if (area != this.area) {
	this.area = area;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the name of this section.
 *
 * @return the section name
 */
public String getName() { return (area == null) ? null : area.getName(); }

/**
 * Given an id, returns the field within this section that has that id.
 * If no field with the specified id exists, returns <code>null</code>.
 *
 * @return a field, or <code>null</code> if no field with the specified
 * id exists in this section
 */
public Field findField(Object id) {
    Long lid;
    if (id instanceof String)
	lid = new Long((String)id);
    else
	lid = (Long)id;

    for (Iterator iter = fields.iterator(); iter.hasNext(); ) {
	Field f = (Field)iter.next();
	if (f.getId().equals(lid))
	    return f;
    }
    return null;
}

/**
 * Adds a field to this section.
 *
 * @param f a field
 */
public void addField(Field f) {
    fields.add(f);
    f.addObserver(this);
    f.setSection(this);
    setChanged();
    notifyObservers();
}

/**
 * Removes a field from this section.
 *
 * @param f field
 */
public void removeField(Field f) {
    if (fields.contains(f)) {
	fields.remove(f);
	f.deleteObserver(this);
	f.setSection(null);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns an iterator over all fields in this section.
 *
 * @return an iterator
 */
public Iterator fields() { return fields.iterator(); }

/**
 * Returns an array of this section's fields sorted by <var>comp</var>.
 *
 * @param comp the comparator to use for sorting
 * @return an array of fields sorted by <var>comp</var>
 */
public Object[] fieldsSortedBy(Comparator comp) {
    Object[] sorted = fields.toArray();
    Arrays.sort(sorted, comp);
    return sorted;
}

/**
 * Returns the number of fields in this section.
 *
 * @return the number of fields in this section
 */
public int numFields() { return fields.size(); }

/**
 * Adds a line to this section.
 *
 * @param l a line
 */
public void addLine(Line l) {
    lines.add(l);
    setChanged();
    notifyObservers();
}

/**
 * Removes a line from this section.
 *
 * @param f line
 */
public void removeLine(Line f) {
    lines.remove(f);
    setChanged();
    notifyObservers();
}

/**
 * Returns an iterator over all lines in this section.
 *
 * @return an iterator
 */
public Iterator lines() { return lines.iterator(); }

public boolean isHidden() {
    return suppressionProc.isHidden();
}

/**
 * Returns <code>true</code> if this is a report detail section.
 *
 * @return <code>true</code> if this is a report detail section
 */
public boolean isDetail() { return area.isDetail(); }

/**
 * Returns the boolean page break flag.
 *
 * @return <code>true</code> if we should start a new page before this
 * section.
 */
public boolean hasPageBreak() { return pageBreak; }

/**
 * Sets the page break flag.
 *
 * @param flag new value
 */
public void setPageBreak(boolean flag) { pageBreak = flag; }

/**
 * Returns the supression proc this section uses
 *
 * @return a suppression proc
 */
public SuppressionProc getSuppressionProc() { return suppressionProc; }

/**
 * Returns <code>true</code> if the specified field is inside this section.
 *
 * @param f a field
 * @return <code>true</code> if the field is within this section
 */
public boolean contains(Field f) {
    return fields.contains(f);
}

/**
 * Returns <code>true</code> if the specified field exists within this
 * section either directly (as a field) or indirectly (as a formula used
 * by an aggregate, user column, or formula or by the suppression proc).
 *
 * @param f a field
 * @return <code>true</code> if the specified field is referenced within
 * this section
 */
public boolean containsReferenceTo(Field f) {
    for (Iterator iter = fields(); iter.hasNext(); ) {
	Field field = (Field)iter.next();
	if (field == f || field.refersTo(f))
	    return true;
    }
    return suppressionProc.refersTo(f);
}

/**
 * Returns <code>true</code> if the specified formula exists within this
 * section either directly (as a formula field) or indirectly (as a formula
 * used by an aggregate, user column, or formula or by the suppression proc).
 *
 * @param f a formula
 * @return <code>true</code> if the specified formula is referenced within
 * this section
 */
public boolean containsReferenceTo(Formula f) {
    for (Iterator iter = fields(); iter.hasNext(); ) {
	Field field = (Field)iter.next();
	if (field.refersTo(f))
	    return true;
    }
    return suppressionProc.refersTo(f);
}

/**
 * Returns <code>true</code> if the specified user column exists within this
 * section either directly (as a user column field) or indirectly (as a user
 * column used by an aggregate, a formula, or by the suppression proc).
 *
 * @param uc a user column
 * @return <code>true</code> if the specified formula is referenced within
 * this section
 */
public boolean containsReferenceTo(UserColumn uc) {
    for (Iterator iter = fields(); iter.hasNext(); ) {
	Field field = (Field)iter.next();
	if (field.refersTo(uc))
	    return true;
    }
    return suppressionProc.refersTo(uc);
}

/**
 * Returns <code>true</code> if the specified parameter exists within this
 * section either directly (as a parameter field) or indirectly (as a parameter
 * used by an aggregate, a formula, or by the suppression proc).
 *
 * @param p a parameter
 * @return <code>true</code> if the specified formula is referenced within
 * this section
 */
public boolean containsReferenceTo(Parameter p) {
    for (Iterator iter = fields(); iter.hasNext(); ) {
	Field field = (Field)iter.next();
	if (field.refersTo(p))
	    return true;
    }
    return suppressionProc.refersTo(p);
}

/**
 * Forces all of the formulas used in this section to be evaluated.
 * See the comment for <code>LayoutEngine.groupHeaders</code> for why
 * this method is necessary.
 *
 * @see jimm.datavision.layout.LayoutEngine#groupHeaders
 */
public void evaluateFormulas() {
    for (Iterator iter = fields(); iter.hasNext(); ) {
	Field field = (Field)iter.next();
	if (field instanceof FormulaField)
	    field.getValue();
    }
}

/**
 * Return <code>true</code> if this section should be printed, given this
 * particular row of data and our supressed state and suppression proc.
 *
 * @return <code>true</code> if the data should be displayed
 */
public boolean isVisibleForCurrentRow() {
    return !suppressionProc.suppress();
}

/**
 * Writes this section and all it contains as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("section");
    out.attr("height", minHeight);
    if (pageBreak) out.attr("pagebreak", pageBreak);

    ListWriter.writeList(out, fields);
    ListWriter.writeList(out, lines);
    suppressionProc.writeXML(out);

    out.endElement();
}

}
