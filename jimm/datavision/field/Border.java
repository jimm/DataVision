package jimm.datavision.field;
import jimm.datavision.Section;
import jimm.datavision.Line;
import jimm.datavision.Point;
import jimm.datavision.Writeable;
import jimm.datavision.layout.LineDrawer;
import jimm.util.XMLWriter;
import java.awt.Color;
import java.util.Observer;
import java.util.Observable;

/**
 * A border is a visual decoration around a report field. Each of its four
 * edges (top, left, bottom, and right) is a {@link BorderEdge} and may be
 * <code>null</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Border
    extends Observable
    implements Writeable, Cloneable, Observer
{

protected static final int BORDER_LINE_SPACE_MULT = 3;
protected static final Color DEFAULT_COLOR = Color.black;

protected Field field;
protected BorderEdge top, left, bottom, right; // May be null
protected Color color;

/**
 * Constructs a new border for the specified field.
 *
 * @param field a report field
 */
public Border(Field field) {
    this.field = field;
}

/**
 * Returns a clone of this border. All edges are cloned as well.
 */
public Object clone() {
    Border b = new Border(field);
    BorderEdge e;

    if ((e = getTop()) == null || e.getNumber() == 0)
	b.setTop(null);
    else
	b.setTop((BorderEdge)e.clone());

    if ((e = getBottom()) == null || e.getNumber() == 0)
	b.setBottom(null);
    else
	b.setBottom((BorderEdge)e.clone());

    if ((e = getLeft()) == null || e.getNumber() == 0)
	b.setLeft(null);
    else
	b.setLeft((BorderEdge)e.clone());

    if ((e = getRight()) == null || e.getNumber() == 0)
	b.setRight(null);
    else
	b.setRight((BorderEdge)e.clone());

    b.setColor(getColor());

    return b;
}

/**
 * For testing only. Checks color and edges but not field.
 *
 * @see jimm.datavision.test.ReportTest#testCloning
 */
public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Border)) return false;
    if (obj == this) return true;

    Border b = (Border)obj;
    if (color == null && b.color != null) return false;
    if (color != null && !color.equals(b.color)) return false;

    BorderEdge e, e2;
    int n, n2;
    e = top;
    e2 = b.top;
    n = e == null ? 0 : e.number;
    n2 = e2 == null ? 0 : e2.number;
    if (n != n2) return false;

    e = left;
    e2 = b.left;
    n = e == null ? 0 : e.number;
    n2 = e2 == null ? 0 : e2.number;
    if (n != n2) return false;

    e = bottom;
    e2 = b.bottom;
    n = e == null ? 0 : e.number;
    n2 = e2 == null ? 0 : e2.number;
    if (n != n2) return false;

    e = right;
    e2 = b.right;
    n = e == null ? 0 : e.number;
    n2 = e2 == null ? 0 : e2.number;
    if (n != n2) return false;

    return true;
}

public int hashCode() {
    int hc = 0;
    if (color != null) hc += color.hashCode();
    if (top != null) hc += top.hashCode();
    if (left != null) hc += left.hashCode();
    if (bottom != null) hc += bottom.hashCode();
    if (right != null) hc += right.hashCode();
    return hc;
}

/**
 * Used only when cloning a field, this sets our field. Otherwise, don't
 * call this.
 *
 * @param f the new field
 */
public void setField(Field f) { field = f; }

protected void finalize() throws Throwable {
    if (top != null) top.deleteObserver(this);
    if (left != null) left.deleteObserver(this);
    if (bottom != null) bottom.deleteObserver(this);
    if (right != null) right.deleteObserver(this);
}

public void update(Observable o, Object arg) {
    setChanged();
    notifyObservers(arg);
}

/**
 * Returns the border's top edge. May return <code>null</code>.
 *
 * @return the top edge
 */
public BorderEdge getTop() { return top; }

/**
 * Sets the top edge. <i>newTop</i> may be <code>null</code>.
 *
 * @param newTop the new edge
 */
public void setTop(BorderEdge newTop) {
    if (top != newTop) {
	if (top != null) top.deleteObserver(this);
	top = newTop;
	if (top != null) top.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the border's left edge. May return <code>null</code>.
 *
 * @return the left edge
 */
public BorderEdge getLeft() { return left; }

/**
 * Sets the left edge. <i>newLeft</i> may be <code>null</code>.
 *
 * @param newLeft the new edge
 */
public void setLeft(BorderEdge newLeft) {
    if (left != newLeft) {
	if (left != null) left.deleteObserver(this);
	left = newLeft;
	if (left != null) left.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the border's bottom edge. May return <code>null</code>.
 *
 * @return the bottom edge
 */
public BorderEdge getBottom() { return bottom; }

/**
 * Sets the bottom edge. <i>newBottom</i> may be <code>null</code>.
 *
 * @param newBottom the new edge
 */
public void setBottom(BorderEdge newBottom) {
    if (bottom != newBottom) {
	if (bottom != null) bottom.deleteObserver(this);
	bottom = newBottom;
	if (bottom != null) bottom.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the border's right edge. May return <code>null</code>.
 *
 * @return the right edge
 */
public BorderEdge getRight() { return right; }

/**
 * Sets the right edge. <i>newRight</i> may be <code>null</code>.
 *
 * @param newRight the new edge
 */
public void setRight(BorderEdge newRight) {
    if (right != newRight) {
	if (right != null) right.deleteObserver(this);
	right = newRight;
	if (right != null) right.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Retrieves the border color.
 *
 * @return the border color
 */
public Color getColor() { return color; }

/**
 * Sets the border color.
 *
 * @param c the new color
 */
public void setColor(Color c) {
    if (color != c) {
	color = c;
	setChanged();
	notifyObservers();
    }
}

/**
 * For each edge, hand the lines that make up that edge to the specified
 * line drawer's <code>drawLine</code> method.
 *
 * @param ld a line drawer
 */
public void eachLine(LineDrawer ld, Object arg) {
    Rectangle rect = field.getBounds();
    Section section = field.getSection();
    BorderEdge edge;
    Line line;

    edge = top;
    if (edge != null && edge.getThickness() > 0 && edge.getNumber() > 0) {
	line = new Line(null, section, edge.getThickness(),
			null, true, new Point(rect.x, rect.y),
			new Point(rect.x + rect.width, rect.y));
	for (int i = 0; i < edge.getNumber(); ++i) {
	    ld.drawLine(line, arg);
	    line.getPoint(0).y += BORDER_LINE_SPACE_MULT;
	    line.getPoint(1).y += BORDER_LINE_SPACE_MULT;
	}
    }

    edge = bottom;
    if (edge != null && edge.getThickness() > 0 && edge.getNumber() > 0) {
	line = new Line(null, section, edge.getThickness(),
			null, true, new Point(rect.x, rect.y + rect.height),
			new Point(rect.x + rect.width, rect.y + rect.height));
	for (int i = 0; i < edge.getNumber(); ++i) {
	    ld.drawLine(line, arg);
	    line.getPoint(0).y -= BORDER_LINE_SPACE_MULT;
	    line.getPoint(1).y -= BORDER_LINE_SPACE_MULT;
	}
    }

    edge = left;
    if (edge != null && edge.getThickness() > 0 && edge.getNumber() > 0) {
	line = new Line(null, section, edge.getThickness(),
			null, true, new Point(rect.x, rect.y),
			new Point(rect.x, rect.y + rect.height));
	for (int i = 0; i < edge.getNumber(); ++i) {
	    ld.drawLine(line, arg);
	    line.getPoint(0).x += BORDER_LINE_SPACE_MULT;
	    line.getPoint(1).x += BORDER_LINE_SPACE_MULT;
	}
    }

    edge = right;
    if (edge != null && edge.getThickness() > 0 && edge.getNumber() > 0) {
	line = new Line(null, section, edge.getThickness(),
			null, true, new Point(rect.x + rect.width, rect.y),
			new Point(rect.x + rect.width, rect.y + rect.height));
	for (int i = 0; i < edge.getNumber(); ++i) {
	    ld.drawLine(line, arg);
	    line.getPoint(0).x -= BORDER_LINE_SPACE_MULT;
	    line.getPoint(1).x -= BORDER_LINE_SPACE_MULT;
	}
    }
}
	    
/**
 * Returns <code>true</code> if this border's edges are all
 * <code>null</code> or have zero count or width.
 *
 * @return <code>true</code> if this border's edges are all
 * <code>null</code> or have zero count or width
 */
public boolean isEmpty() {
    return isEmptyEdge(top) && isEmptyEdge(bottom) && isEmptyEdge(left)
	&& isEmptyEdge(right);
}

/**
 * Returns <code>true</code> if <var>edge</var> is <code>null</code> or has
 * zero count or width.
 *
 * @return <code>true</code> if <var>edge</var> is <code>null</code> or has
 * zero count or width
 */
protected boolean isEmptyEdge(BorderEdge edge) {
    return edge == null || edge.getNumber() == 0 || edge.getThickness() == 0;
}

/**
 * Writes this border as an XML tag. Asks each edge to write itself as well.
 * If {@link #isEmpty} returns <code>true</code>, returns without writing
 * anything.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    if (isEmpty())
	return;

    out.startElement("border");
    if (color != null && !color.equals(DEFAULT_COLOR))
	out.attr("color", color);

    if (top != null && top.getNumber() != 0) top.writeXML(out, "top");
    if (bottom != null && bottom.getNumber() != 0)
	bottom.writeXML(out, "bottom");
    if (left != null && left.getNumber() != 0) left.writeXML(out, "left");
    if (right != null && right.getNumber() != 0) right.writeXML(out, "right");

    out.endElement();
}

}

