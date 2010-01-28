package jimm.datavision;
import jimm.util.XMLWriter;
import java.util.Observer;
import java.util.Observable;

/**
 * <code>Element</code> is the abstract superclass of <code>Field</code>
 * and <code>Line</code>. These are the visual elements of a report section.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */

public abstract class Element
    extends Observable
    implements Writeable, Observer
{

protected Report report;
protected Section section;
protected boolean visible;

/**
 * Constructor. (Though a section knows about it's report, too, often we
 * create elements with a <code>null</code> section and then add them to
 * some section later on).
 *
 * @param report the report containing this element
 * @param section the report section containing this element
 */
public Element(Report report, Section section, boolean visible) {
    this.report = report;
    this.section = section;
    this.visible = visible;
}

public void update(Observable o, Object arg) {
    setChanged();
    notifyObservers(arg);
}

/**
 * Returns the section that containts this field.
 *
 * @return the section
 */
public Section getSection() { return section; }

/**
 * Returns the report that containts this field.
 *
 * @return the report
 */
public Report getReport() { return report; }

/**
 * Modifies the section to which this field belongs. Only called by
 * section itself.
 *
 * @param s the section
 */
public void setSection(Section s) {
    if (s != section) {
	section = s;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the visible state of this element.
 *
 * @return the visible state
 */
public boolean isVisible() { return visible; }

/**
 * Sets the visibility of this element.
 *
 * @param newVisible the new visible state
 */
public void setVisible(boolean newVisible) {
    if (visible != newVisible) {
	visible = newVisible;
	setChanged();
	notifyObservers();
    }
}

/**
 * Writes this element as an XML tag. This abstract method is overridden
 * by the <code>Field</code> and <code>Line</code> subclasses.
 *
 * @param out a writer that knows how to write XML
 */
public abstract void writeXML(XMLWriter out);

}
