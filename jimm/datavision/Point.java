package jimm.datavision;
import jimm.util.XMLWriter;
import java.util.Observable;

/**
 * A point with double coordinates.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Point extends Observable implements Writeable {

/**
 * Warning: though public, treat as read-only. When writing, make sure to
 * use setter method so observers are notified.
 */
public double x;
/**
 * Warning: though public, treat as read-only. When writing, make sure to
 * use setter method so observers are notified.
 */
public double y;

/** Constructor. */
public Point() { this(0, 0); }

/** Constructor. */
public Point(java.awt.Point p) { this((double)p.x, (double)p.y); }

/** Constructor. */
public Point(Point p) { this(p.x, p.y); }

/**
 * Constructor.
 *
 * @param x a double
 * @param y a double
 */
public Point(double x, double y) {
    this.x = x;
    this.y = y;
}

/**
 * Returns the x coordinate.
 *
 * @return the doubleing-point x coordinate
 */
public double getX() { return x; }

/**
 * Sets the x coordinate.
 *
 * @param newX the new x coordinate
 */
public void setX(double newX) {
    if (x != newX) {
	x = newX;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the y coordinate.
 *
 * @return the doubleing-point y coordinate
 */
public double getY() { return y; }

/**
 * Sets the y coordinate.
 *
 * @param newY the new y coordinate
 */
public void setY(double newY) {
    if (y != newY) {
	y = newY;
	setChanged();
	notifyObservers();
    }
}

/**
 * Translates this point by the coordinates of another.
 */
public void translate(java.awt.Point p) {
    translate((double)p.x, (double)p.y);
}

/**
 * Translates this point by the coordinates of another.
 */
public void translate(Point p) {
    translate(p.x, p.y);
}

/**
 * Translates this point by the coordinates of another.
 */
public void translate(double dx, double dy) {
    if (dx != 0 && dy != 0) {
	x += dx;
	y += dy;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the distance from this point to another.
 *
 * @param p the other point
 * @return the distance between the two points
 */
public double distanceTo(Point p) {
    double dx = p.x - x;
    double dy = p.y - y;
    if (dx == 0) return Math.abs(dy);
    if (dy == 0) return Math.abs(dx);
    return Math.sqrt(dx * dx + dy * dy);
}

/**
 * Returns a string representation of this point.
 *
 * @return a string representing this point
 */
public String toString() {
    return "(" + x + ", " + y + ")";
}

/**
 * Writes this point as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("point");
    out.attr("x", x);
    out.attr("y", y);
    out.endElement();
}

}
