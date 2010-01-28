package jimm.datavision;
import jimm.util.XMLWriter;
import java.awt.Color;

/**
 * A line is a visual report element. Lines are used in field {@link
 * jimm.datavision.field.Border}s and independently.
 * <p>
 * Note that currently, line thickness is ignored in the Java GUI (but not
 * in layout engines such as the LaTeXLE).
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Line extends Element {

protected static final Color DEFAULT_COLOR = Color.black;

protected double thickness;
protected Point[] points;
protected Color color;

/**
 * Constructor.
 *
 * @param report the report containing this line
 * @param section the section containing this line
 * @param thickness the line thickness
 * @param color may be <code>null</code>
 * @param visible show/hide flag
 */
public Line(Report report, Section section, double thickness, Color color,
	    boolean visible)
{
    this(report, section, thickness, color, visible, null, null);
}

/**
 * Constructor.
 *
 * @param report the report containing this line
 * @param section the section containing this line
 * @param thickness the line thickness
 * @param color may be <code>null</code>
 * @param visible show/hide flag
 * @param p0 one end point of the line
 * @param p1 the other end point of the line
 */
public Line(Report report, Section section, double thickness, Color color,
	    boolean visible, Point p0, Point p1)
{
    super(report, section, visible);
    this.thickness = thickness;
    points = new Point[2];
    points[0] = p0;
    points[1] = p1;
    this.color = color == null ? DEFAULT_COLOR : color;
}

/**
 * Adds an end point to the line. Used when constructing a line from XML,
 * where we don't see the point until after creating this line.
 *
 * @param x the x coordinate
 * @param y the y coordinate
 */
public void addEndPoint(double x, double y) {
    Point p = new Point(x, y);
    if (points[0] == null)
	points[0] = p;
    else
	points[1] = p;
    p.addObserver(this);
}

/**
 * Returns the line thickness.
 *
 * @return the line thickness
 */
public double getThickness() { return thickness; }

/**
 * Sets the line thickness.
 *
 * @param newThickness the new line thickness
 */
public void setThickness(double newThickness) {
    if (thickness != newThickness) {
	thickness = newThickness;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns one of the two end points of the line.
 *
 * @param index either 0 or 1
 * @return a point
 */
public Point getPoint(int index) { return points[index]; }

/**
 * Sets one of the two end points.
 *
 * @param newPoint a point
 * @param index either 0 or 1
 */
public void setPoint(Point newPoint, int index) {
    if (points[index] != newPoint) {
	if (points[index] != null) points[index].deleteObserver(this);
	points[index] = newPoint;
	if (points[index] != null) points[index].addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the length of the line.
 *
 * @return the distance between the two end points
 */
public double length() { return points[0].distanceTo(points[1]); }

/**
 * Returns a string representation of this line.
 *
 * @return a string representing this line
 */
public String toString() { return "(" + points[0] + ", " + points[1] + ")"; }

/**
 * Returns the line's color. The return value will never be
 * <code>null</code>.
 *
 * @return the line's color
 */
public Color getColor() { return color == null ? DEFAULT_COLOR : color; }

/**
 * Sets the line's color. If <var>c</var> is <code>null</code>, then the
 * color is set to <code>DEFAULT_COLOR</code>.
 *
 * @param c new line color; if <code>null</code>, color is set to
 * <code>DEFAULT_COLOR</code>
 */   
public void setColor(Color c) { color = c == null ? DEFAULT_COLOR : c; }

/**
 * Writes this line as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("line");
    out.attr("thickness", thickness);
    if (color != null && !color.equals(DEFAULT_COLOR))
	out.attr("color", color);
    if (!visible)
	out.attr("visible", visible);
    points[0].writeXML(out);
    points[1].writeXML(out);
    out.endElement();
}

}
