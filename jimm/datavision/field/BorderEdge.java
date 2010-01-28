package jimm.datavision.field;
import jimm.util.XMLWriter;
import java.util.Observable;

/**
 * A <i>border edge</i> represents one of the four edges of a {@link Border}.
 * It has its own line style, thickness, and number of lines. <p> Note: line
 * thickness is currently ignored.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class BorderEdge extends Observable implements Cloneable {

/** Draw simple lines. This is the default style. */
public static final int STYLE_LINE = 0;
/** Draw dashed lines. */
public static final int STYLE_DASH = 1;
/** Draw dotted lines. */
public static final int STYLE_DOT = 2;

public static final int DEFAULT_STYLE = STYLE_LINE;
public static final int DEFAULT_NUMBER = 1;
public static final double DEFAULT_THICKNESS = 1.0;

protected int style;		// none, line, dash, dot
protected double thickness;	// in points
protected int number;

/**
 * Returns the integer style constant associated with the specified string.
 * The string must be <code>null</code>, "none", "line", "dash", or "dot"
 * and comes from the XML files that describe reports. If the string is
 * <code>null</code>, then we return the default value of STYLE_LINE;
 *
 * @param styleStr one of <code>null</code>, "none", "line", "dash", or "dot"
 * @return one of the <code>STYLE_*</code> constants; <code>STYLE_LINE</code>
 * is returned if styleStr is <code>null</code>
 */
public static int styleFromString(String styleStr) {
    if (styleStr == null || styleStr.length() == 0) // Default
	return DEFAULT_STYLE;

    styleStr = styleStr.toLowerCase();
    int style = DEFAULT_STYLE;
    if (styleStr.equals("line")) style = STYLE_LINE;
    else if (styleStr.equals("dash")) style = STYLE_DASH;
    else if (styleStr.equals("dot")) style = STYLE_DOT;

    return style;
}

/**
 * Creates a new edge with <code>DEFAULT_NUMBER</code> lines of
 * <code>DEFAULT_STYLE</code> and <code>DEFAULT_THICKNESS</code>.
 */
public BorderEdge() {
    this(DEFAULT_STYLE, DEFAULT_THICKNESS, DEFAULT_NUMBER);
}

/**
 * Creates a new edge with <code>DEFAULT_NUMBER</code> lines of
 * the specified style and <code>DEFAULT_THICKNESS</code>.
 *
 * @param style one of the <code>STYLE_</code> constants
 */
public BorderEdge(int style) {
    this(style, DEFAULT_THICKNESS, DEFAULT_NUMBER);
}

/**
 * Creates a new edge with <code>DEFAULT_NUMBER</code> lines of
 * the specified style and thickness.
 *
 * @param style one of the <code>STYLE_</code> constants
 * @param thickness line thickness
 */
public BorderEdge(int style, double thickness) {
    this(style, thickness, DEFAULT_NUMBER);
}

/**
 * Creates a new edge with <var>number</var> lines of the specified style
 * and thickness.
 *
 * @param style one of the <code>STYLE_</code> constants
 * @param thickness line thickness
 * @param number the number of liens to draw
 */
public BorderEdge(int style, double thickness, int number) {
    this.style = style;
    this.thickness = thickness;
    this.number = number;
}

/**
 * Returns a clone of this border. All edges are cloned as well.
 */
public Object clone() {
    BorderEdge be = new BorderEdge(style, thickness, number);
    return be;
}

public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof BorderEdge)) return false;
    if (obj == this) return true;

    BorderEdge be = (BorderEdge)obj;
    return number == be.number
	&& thickness == be.thickness
	&& style == be.style;
}

public int hashCode() {
    return style + number * 10 + (int)(thickness * 1000);
}

/**
 * Returns the edge's style.
 *
 * @return one of the <code>STYLE_</code> constants
 */
public int getStyle() { return style; }

/**
 * Sets the edge's style.
 *
 * @param newStyle one of the <code>STYLE_</code> constants
 */
public void setStyle(int newStyle) {
    if (style != newStyle) {
	style = newStyle;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the edge's thickness.
 *
 * @return the thickness
 */
public double getThickness() { return thickness; }

/**
 * Sets the edge's thickness.
 *
 * @param newThickness line thickness
 */
public void setThickness(double newThickness) {
    if (thickness != newThickness) {
	thickness = newThickness;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the number of lines to draw along this border edge.
 *
 * @return the number of lines to draw along this border edge
 */
public int getNumber() { return number; }

/**
 * Sets the number of lines to draw along this border edge.
 *
 * @param newNumber the number of lines to draw
 */
public void setNumber(int newNumber) {
    if (number != newNumber) {
	number = newNumber;
	setChanged();
	notifyObservers();
    }
}

/**
 * Writes this edge as an XML tag.
 *
 * @param out a writer that knows how to write XML
 * @param location from the border; the string "top", "bottom", "left", or
 * "right"
 */
public void writeXML(XMLWriter out, String location) {
    String styleStr = null;
    if (style != DEFAULT_STYLE) {
	switch (style) {
	case STYLE_LINE: styleStr = "line"; break;
	case STYLE_DASH: styleStr = "dash"; break;
	case STYLE_DOT: styleStr = "dot"; break;
	}
    }

    out.startElement("edge");
    out.attr("location", location);
    if (number != DEFAULT_NUMBER)
	out.attr("number", number);
    if (thickness != DEFAULT_THICKNESS)
	out.attr("thickness", thickness);
    if (styleStr != null)
	out.attr("style", styleStr);
    out.endElement();
}

public String toString() {
    return "BorderEdge[style=" + style + ", thickness=" + thickness
	+ ", number=" + number + "]";
}

}
