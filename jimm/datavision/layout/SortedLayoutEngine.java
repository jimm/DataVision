package jimm.datavision.layout;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.field.ImageField;
import java.io.PrintWriter;
import java.util.*;

/**
 * A sorted layout engine outputs the fields within each section in order
 * of their y then x coordinates. Another way of putting it: the fields are
 * sorted top to bottom, then left to right. This ensures, for example,
 * that character-based layout engines such as <code>CharSepLE</code> and
 * <code>HTMLLE</code> will display fields in the correct left-to-right
 * order.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public abstract class SortedLayoutEngine extends LayoutEngine {

protected HashMap<Section, Object[]> sectionFields;
protected Comparator<Field> comp;

/**
 * Constructor.
 */
public SortedLayoutEngine() {
    this(null);
}

/**
 * Constructor.
 *
 * @param out output print writer
 */
public SortedLayoutEngine(PrintWriter out) {
    super(out);
    sectionFields = new HashMap<Section, Object[]>();

    // Sorts fields by their y coordinates, then their x coordinates.
    comp = new Comparator<Field>() {
	public int compare(Field f1, Field f2) {
	    double y1 = f1.getBounds().y;
	    double y2 = f2.getBounds().y;
	    if (y1 == y2) {
		double x1 = f1.getBounds().x;
		double x2 = f2.getBounds().x;
		return (x1 < x2) ? -1 : ((x1 > x2) ? 1 : 0);
	    }
	    else
		return (y1 < y2) ? -1 : ((y1 > y2) ? 1 : 0);
	}
	};
}

/**
 * This override iterates over a list of fields that have been sorted
 * by their y and x coordinates. Put another way, the fields are output
 * top to bottom, left to right.
 *
 * @param sect a section
 */
protected void doOutputSection(Section sect) {
    Object[] fields = (Object[])sectionFields.get(sect);
    if (fields == null)
	fields = buildSectionFields(sect);

    if (fields != null) {
	// Output the fields in the section
	for (int i = 0; i < fields.length; ++i) {
	    Field f = (Field)fields[i];
	    if (f.isVisible()) {
		if (f instanceof ImageField)
		    outputImage((ImageField)f);
		else
		    outputField(f);
	    }
	}
    }
    // Output the lines
    for (Line l : sect.lines())
	if (l.isVisible()) outputLine(l);
}

/**
 * Creates, saves, and returns an array of fields sorted by their y and x
 * coordinates (y first, then x). Another way of putting it: the fields are
 * sorted top to bottom, then left to right.
 *
 * @param sect a report section
 * @return a sorted array of fields
 */
protected Object[] buildSectionFields(Section sect) {
    int numFields = sect.numFields();
    if (numFields == 0)
	return null;

    Object[] fields = sect.fieldsSortedBy(comp);
    sectionFields.put(sect, fields);

    return fields;
}

}
