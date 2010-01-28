package jimm.datavision.field;
import jimm.datavision.*;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.util.I18N;
import jimm.util.XMLWriter;
import java.util.Observable;

/**
 * The abstract superclass of visual report fields that display text labels,
 * database columns, special values, aggregate values, formulas, and
 * parameters. A field has a bounds {@link Rectangle} that determines its
 * position within a section and an associated {@link Format} and {@link
 * Border} for determining how to display the field.
 * <p>
 * To avoid repeated font size and line width calculations, a {@link
 * FormattedValueCache} holds the formatted version of this field's value.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class Field
    extends Element
    implements Identity, Draggable, Cloneable {

public static final double DEFAULT_WIDTH = 120;
public static final double DEFAULT_HEIGHT = 16;

static Long maxIdSeen = new Long(0);

protected Long id;
protected Rectangle bounds;
protected Format format;
protected Border border;	// Possibly null
protected Object value;		// String or id (column, formula, etc.)
protected FormattedValueCache cache;

/**
 * This factory method constructs and returns a new instance of a subclass
 * of <code>Field</code> based on the <var>type</var> string.
 * <p>
 * If <var>id</var> is <code>null</code>, generates a new id number. This
 * number is one higher than any previously-seen id number. This does
 * <em>not</em> guarantee that no later field will be created manually with
 * the same id number.
 *
 * @param id the unique identifier for the new field; if <code>null</code>,
 * generate a new id
 * @param section the report section containing the field
 * @param type one of "special", "text", "column", "formula", "parameter",
 * "image", or one of the aggregate function names; found in report XML
 */
public static Field create(Long id, Report report, Section section,
			   String type, Object value, boolean visible)
{
    if (type == null || type.length() == 0)
	throw new IllegalArgumentException(I18N.get("Field.field_cap")
					   + " " + id + ": "
					   + I18N.get("Field.need_type"));
	
    type = type.toLowerCase();
    if (type.equals("special"))
	return new SpecialField(id, report, section, value, visible);
    else if (type.equals("text"))
	return new TextField(id, report, section, value, visible);
    else if (type.equals("column"))
	return new ColumnField(id, report, section, value, visible);
     else if (type.equals("formula"))
	return new FormulaField(id, report, section, value, visible);
    else if (type.equals("parameter"))
	return new ParameterField(id, report, section, value, visible);
    else if (type.equals("image"))
	return new ImageField(id, report, section, value, visible);
    else if (type.equals("usercol"))
	return new UserColumnField(id, report, section, value, visible);
    else if (type.equals("subreport"))
	return new SubreportField(id, report, section, value, visible);
    else if (AggregateField.isAggregateFunctionName(type))
	return new AggregateField(id, report, section, value, visible, type);

    throw new IllegalArgumentException(I18N.get("Field.field_cap") + " " + id
				       + ": " + I18N.get("Field.unknown")
				       + " \"" + type + "\"");
}

/**
 * Creates a field from a drag string. <var>str</var> should a string
 * created by some field's {@link #dragString} method.
 *
 * @param report the report containing this element
 * @param str a drag string
 * @return a new field
 */
public static Field createFromDragString(Report report, String str) {
    int pos = str.indexOf(":");
    if (pos == -1)
	return null;

    String type = str.substring(0, pos);
    String value = str.substring(pos + 1);
    return create(null, report, null, type, value, true);
}



/**
 * Constructor.
 *
 * @param id the unique identifier for the new field
 * @param report the report containing this element
 * @param section the report section containing the field
 * @param value the value this field represents visually
 * @param visible show/hide flag
 */
protected Field(Long id, Report report, Section section, Object value,
		boolean visible)
{
    super(report, section, visible);

    if (id == null)		// Generate new value
	id = new Long(maxIdSeen.longValue() + 1);
    if (id.compareTo(maxIdSeen) == 1)
	maxIdSeen = new Long(id.longValue());
    this.id = id;

    format = Format.createEmptyFormat();
    format.setField(this);
    format.addObserver(this);

    // The defaultField will be null only when we are creating the
    // default field itself.
    Field defaultField = report.getDefaultField();

    this.value = value;
    cache = new FormattedValueCache(this);
    bounds = defaultField != null
	? new Rectangle(0, 0, defaultField.getBounds().width,
			defaultField.getBounds().height)
	: new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    bounds.addObserver(this);

    // Make sure changes to the default field are reflected in the GUI
    if (defaultField != null)
	defaultField.addObserver(this);
}

/**
 * Returns a clone. Subclasses may need ot override this method to copy
 * additional instance variables that are not set in their constructors.
 *
 * @return an almost-ready clone of this object
 */
public Object clone() {
    Field f = Field.create(null, report, section, typeString(), value, true);
    f.bounds = new Rectangle(bounds);
    f.format = (Format)format.clone();
    f.format.setField(f);
    if (border == null)
	f.border = null;
    else {
	f.border = (Border)border.clone();
	f.border.setField(f);
    }
    return f;
}

protected void finalize() throws Throwable {
    bounds.deleteObserver(this);
    if (format != null) format.deleteObserver(this);
    if (border != null) border.deleteObserver(this);
}

public void update(Observable o, Object arg) {
    super.update(o, arg);
    if (format != null)
	format.clearFontCache();
}

public Object getId() { return id; }

/**
 * Returns the bounds rectangle for this field.
 *
 * @return the bounds rectangle
 */
public Rectangle getBounds() { return bounds; }

/**
 * Sets the bounds rectangle.
 *
 * @param newBounds the new bounds rectangle
 */
public void setBounds(Rectangle newBounds) {
    if (bounds != newBounds) {
	bounds.deleteObserver(this);
	bounds = newBounds;
	bounds.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the height needed to output the current value of this field.
 * This default implementation returns the height of the field as
 * defined in the report designer (the bounds height).
 */
public double getOutputHeight() {
    return cache.getOutputHeight(getValue());
}

/**
 * Returns the format for this field. May return <code>null</code>.
 *
 * @return the format, possibly <code>null</code>
 */
public Format getFormat() { return format; }

/**
 * Sets the format. If this field already has a format, you can just modify
 * it instead of giving it a completely new one.
 *
 * @param newFormat the format
 */
public void setFormat(Format newFormat) {
    if (format != newFormat) {
	if (format != null) format.deleteObserver(this);
	format = newFormat;
	format.setField(this);
	if (format != null) format.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the border for this field. May return <code>null</code>.
 *
 * @return the border, possibly <code>null</code>
 */
public Border getBorder() { return border; }

/**
 * Returns the border for this field or, if it is <code>null</code>, the
 * report's default border. If we return the default border, we clone it
 * in order to give it this field.
 *
 * @return this field's border or the default border
 */
public Border getBorderOrDefault() {
    if (border != null)
	return border;

    Border b = (Border)report.getDefaultField().getBorder().clone();
    b.setField(this);
    return b;
}

/**
 * Sets the border.
 *
 * @param newBorder the new border
 */
public void setBorder(Border newBorder) {
    if (border != newBorder) {
	if (border != null) border.deleteObserver(this);
	border = newBorder;
	if (border != null) border.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the value for this field. May return <code>null</code>.
 *
 * @return the value, possibly <code>null</code>
 */
public Object getValue() { return value; }

/**
 * Sets the value.
 *
 * @param newValue the new value
 */
public void setValue(Object newValue) {
    if (value != newValue) {
	value = newValue;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns a new widget of the appropriate <code>FieldWidget</code>
 * subclass for this field. Subclasses override this method to return
 * different types of widgets.
 *
 * @param sw a field widget
 */
public FieldWidget makeWidget(SectionWidget sw) {
    return new FieldWidget(sw, this);
}

/**
 * Returns the string that specifies this field's type in the report XML.
 *
 * @return a string representing this field's type; used in XML files
 */
public abstract String typeString();

/**
 * Returns the string used to identify a field type when dragging.
 * Usually returns {@link #typeString} plus a value or an id.
 *
 * @return the string used to identify the field when dragging
 */
public abstract String dragString();

/**
 * Returns a string representing the field in the GUI during report design.
 *
 * @return a string useful for display in the design GUI
 */
public String designLabel() { return formulaString(); }

/**
 * Returns a string representing the field as it appears in a formula.
 *
 * @return a string useful in a formula
 */
public abstract String formulaString();

/**
 * Returns <code>true</code> if this field contains a reference to the
 * specified field. Most fields return <code>false</code>; only a {@link
 * AggregateField} or {@link FormulaField} would return <code>true</code>.
 *
 * @param f a field
 * @return <code>true</code> if this field contains a reference to the
 * specified field
 */
public boolean refersTo(Field f) {
    return false;
}

/**
 * Returns <code>true</code> if this field contains a reference to the
 * specified formula. Most fields return <code>false</code>; only a {@link
 * AggregateField} or {@link FormulaField} would return <code>true</code>.
 *
 * @param f a formula
 * @return <code>true</code> if this field contains a reference to the
 * specified field
 */
public boolean refersTo(Formula f) {
    return false;
}

/**
 * Returns <code>true</code> if this field contains a reference to the
 * specified user column. Most fields return <code>false</code>; only a {@link
 * AggregateField}, {@link UserColumnField}, or {@link FormulaField} would
 * return <code>true</code>.
 *
 * @param uc a user column
 * @return <code>true</code> if this field contains a reference to the
 * specified user column
 */
public boolean refersTo(UserColumn uc) {
    return false;
}

/**
 * Returns <code>true</code> if this field contains a reference to the
 * specified parameter. Most fields return <code>false</code>; only a {@link
 * AggregateField} or {@link FormulaField} would return <code>true</code>.
 *
 * @param p a parameter
 * @return <code>true</code> if this field contains a reference to the
 * specified field
 */
public boolean refersTo(Parameter p) {
    return false;
}

/**
 * Returns <code>true</code> if this field can be aggregated. This method
 * returns <code>false</code> by default but is overridded by classes whose
 * values may be aggregated.
 *
 * @return <code>true</code> if this field can be aggregated
 */
public boolean canBeAggregated() {
    return false;
}

/**
 * Returns this fields formatted value, ready for display in the report.
 * If this field is invisible, or <code>getValue</code> returns
 * <code>null</code> then this method will return <code>null</code>.
 *
 * @return the report display string; may be <code>null</code>
 */
public String toString() {
    if (!visible) return null;
    return cache.getFormattedString(getValue());
}

/**
 * Writes this field as an XML tag. Writes bounds, border, and format.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("field");
    out.attr("id", id);
    out.attr("type", typeString());
    out.attr("value", value);
    if (!visible)
	out.attr("visible", visible);

    writeFieldGuts(out);

    out.endElement();
}

/**
 * Writes objects contained within this field (bounds, border, and format).
 *
 * @param out a writer that knows how to write XML
 */
protected void writeFieldGuts(XMLWriter out) {
    bounds.writeXML(out);
    if (format != null) format.writeXML(out);
    if (border != null) border.writeXML(out);
}

}
