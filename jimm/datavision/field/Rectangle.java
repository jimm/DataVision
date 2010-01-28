package jimm.datavision.field;
import jimm.datavision.Writeable;
import jimm.util.XMLWriter;
import java.util.Observable;

/**
 * A rectangle with double coordinates.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Rectangle extends Observable implements Writeable {

/**
 * Warning: only read the x coordinate. When writing, make sure to use
 * setter method so observers are notified.
 */
public double x;
/**
 * Warning: only read the y coordinate. When writing, make sure to use
 * setter method so observers are notified.
 */
public double y;
/**
 * Warning: only read the width. When writing, make sure to use
 * setter method so observers are notified.
 */
public double width;
/**
 * Warning: only read the height. When writing, make sure to use
 * setter method so observers are notified.
 */
public double height;

/**
 * Constructor.
 */
public Rectangle() { this(0, 0, 0, 0); }

/**
 * Constructor.
 *
 * @param r the <em>other</em> kind of rectangle
 */
public Rectangle(java.awt.Rectangle r) { this(r.x, r.y, r.width, r.height); }

/**
 * Constructor.
 *
 * @param r another rectangle
 */
public Rectangle(Rectangle r) { this(r.x, r.y, r.width, r.height); }

/**
 * Constructor.
 *
 * @param x a double
 * @param y a double
 * @param width a double
 * @param height a double
 */
public Rectangle(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
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
 * Returns the width.
 *
 * @return the doubleing-point width
 */
public double getWidth() { return width; }

/**
 * Sets the width.
 *
 * @param newWidth the new width
 */
public void setWidth(double newWidth) {
    if (width != newWidth) {
	width = newWidth;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the height.
 *
 * @return the doubleing-point height
 */
public double getHeight() { return height; }

/**
 * Sets the height.
 *
 * @param newHeight the new height
 */
public void setHeight(double newHeight) {
    if (height != newHeight) {
	height = newHeight;
	setChanged();
	notifyObservers();
    }
}

/**
 * Sets everything at once.
 *
 * @param newX the new x coordinate
 * @param newY the new y coordinate
 * @param newWidth the new width
 * @param newHeight the new height
 */
public void setBounds(double newX, double newY, double newWidth,
		      double newHeight)
{
    // Instead of calling setX(), sety(), etc. individually, copy the
    // code so we can call notifyObservers() once.
    boolean needsToNotify = false;
    if (x != newX) {
	x = newX;
	needsToNotify = true;
    }
    if (y != newY) {
	y = newY;
	needsToNotify = true;
    }
    if (width != newWidth) {
	width = newWidth;
	needsToNotify = true;
    }
    if (height != newHeight) {
	height = newHeight;
	needsToNotify = true;
    }

    if (needsToNotify) {
	setChanged();
	notifyObservers();
    }
}

/**
 * Sets everything at once.
 *
 * @param r a jimm.datavision.Rectangle
 */
public void setBounds(jimm.datavision.field.Rectangle r) {
    setBounds(r.x, r.y, r.width, r.height);
}

/**
 * Sets everything at once.
 *
 * @param r a java.awt.Rectangle
 */
public void setBounds(java.awt.Rectangle r) {
    setBounds(r.x, r.y, r.width, r.height);
}

/**
 * Returns a string representation of this rectangle.
 *
 * @return a string representing this rectangle
 */
public String toString() {
    return "[x=" + x + ", y=" + y + ", w=" + width + ", h=" + height + "]";
}

/**
 * Writes this rectangle as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("bounds");
    out.attr("x", x);
    out.attr("y", y);
    out.attr("width", width);
    out.attr("height", height);
    out.endElement();
}

}
