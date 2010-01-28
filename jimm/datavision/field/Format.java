package jimm.datavision.field;
import jimm.datavision.Writeable;
import jimm.util.XMLWriter;
import java.awt.Color;
import java.awt.Font;
import java.util.Observable;

/**
 * A format describes how to display a field. It specifies font family name,
 * size, attributes (bold, italic, underline, wrap), alignment, and print
 * format.
 * <p>
 * If a field's value is <code>null</code>, then the getter returns the value
 * of the report's default field's format (which will never be
 * <code>null</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Format extends Observable implements Writeable, Cloneable {

protected static final String DEFAULT_FONT_FAMILY_NAME = "Times New Roman";

protected static final int DEFAULT_SIZE = 11;
protected static final Color DEFAULT_COLOR = Color.black;

/** Used to specify left alignment. */
public static final int ALIGN_LEFT = 0;
/** Used to specify center alignment. */
public static final int ALIGN_CENTER = 1;
/** Used to specify right alignment. */
public static final int ALIGN_RIGHT = 2;

protected Field field;
protected String fontFamilyName;
protected Double size;		// In points
protected Boolean bold;
protected Boolean italic;
protected Boolean underline;
protected Boolean wrap;
protected Integer align;	// ALIGN_{LEFT,CENTER,RIGHT}
protected String format;	// Output formatting string
protected Color color;
protected Font font;		// Lazily instantiated

/**
 * Returns an <code>ALIGN_*</code> constant, given one of "left",
 * "center", or "right". If the specified string is null or is not one
 * of these values, <code>ALIGN_LEFT</code> is returned.
 *
 * @param s the string "left", "center", or "right" (case is not
 * significant)
 * @return one of <code>ALIGN_LEFT</code>, <code>ALIGN_CENTER</code>,
 * or <code>ALIGN_RIGHT</code>
 */
public static int alignFromString(String s) {
    if (s == null) return ALIGN_LEFT;
    String copy = s.toLowerCase();
    if ("center".equals(copy)) return ALIGN_CENTER;
    if ("right".equals(copy)) return ALIGN_RIGHT;
    return ALIGN_LEFT;
}

/**
 * Given an <code>ALIGN_*</code> constant, return the string used
 * to represent that value in a report XML file. If <i>align</i> is not
 * one of those values, returns "left".
 *
 * @param align one of "left", "center", or "right"
 */
public static String alignToString(int align) {
    switch (align) {
    case ALIGN_RIGHT: return "right";
    case ALIGN_CENTER: return "center";
    default: return "left";
    }
}

public static Format createEmptyFormat() {
    return new Format();
}

public static Format createDefaultFormat() {
    return new DefaultFormat();
}

/**
 * Constructor. Creates an empty format.
 */
Format() {}

/**
 * Normally you don't need to call this, because {@link Field#setFormat}
 * calls this.
 *
 * @param f a field
 */
void setField(Field f) { field = f; }

/**
 * Returns a clone of this format.
 */
public Object clone() {
    Format f = new Format();
    fillClonedField(f);
    return f;
}

protected void fillClonedField(Format f) {
    f.field = field;
    f.setFontFamilyName(getFontFamilyName());
    f.setSize(getSize());
    f.setBold(isBold());
    f.setItalic(isItalic());
    f.setUnderline(isUnderline());
    f.setWrap(isWrap());
    f.setAlign(getAlign());
    f.setFormat(getFormat());
    f.setColor(getColor());
}

public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Format))
	return false;
    if (obj == this)
	return true;

    Format f = (Format)obj;
    // These getters never return null, because if our ivars are null we look
    // up to the default field's format for its value.
    return getSize() == f.getSize()
	&& isBold() == f.isBold()
	&& isItalic() == f.isItalic()
	&& isUnderline() == f.isUnderline()
	&& isWrap() == f.isWrap()
	&& getAlign() == f.getAlign()
	&& (format == f.getFormat()
	    || (format != null && format.equals(f.getFormat())))
	&& getColor().equals(f.getColor());
}

public int hashCode() {
    String formatString = getFormat();
    return (color == null ? 0 : color.hashCode())
	+ (formatString == null ? 0 : formatString.hashCode())
	+ (int)getSize()
	+ (isBold() ? 101 : 100)
	+ (isItalic() ? 501 : 500)
	+ (isUnderline() ? 1001 : 1000)
	+ (isWrap() ? 5001 : 5000)
	+ (getAlign() + 10000);
}

/**
 * Returns this field's report's default field's format (*whew*).
 */
public Format getDefaultFormat() {
    return field.getReport().getDefaultField().getFormat();
}

/**
 * Returns the font family name for this format.
 *
 * @return the font family name
 */
public String getFontFamilyName() {
    return fontFamilyName == null ? getDefaultFormat().getFontFamilyName()
	: fontFamilyName;
}

/**
 * Sets the font family name
 *
 * @param newFontFamilyName the new font family name
 */
public void setFontFamilyName(String newFontFamilyName) {
    if (newFontFamilyName != null) {
	newFontFamilyName = newFontFamilyName.trim();
	if (newFontFamilyName.length() == 0)
	    newFontFamilyName = null;
    }

    if (fontFamilyName != newFontFamilyName
	&& (fontFamilyName == null
	    || !fontFamilyName.equals(newFontFamilyName)))
    {
	fontFamilyName = newFontFamilyName;
	font = null;
	setChanged();
	notifyObservers();
    }
}

/**
 * Based on our font family name, alignment flags, and size, return a
 * font. Never returns <code>null</code>.
 *
 * @return a font; never <code>null</code>
 */
public Font getFont() {
    if (font == null) {
	String name = getFontFamilyName();

	int style = 0;
	if (isBold())
	    style = Font.BOLD;
	if (isItalic())
	    style |= Font.ITALIC;
	if (style == 0)
	    style = Font.PLAIN;

	font = new Font(name, style, (int)getSize());
    }
    return font;
}

/** Clears the font we may be holding on to. */
public void clearFontCache() { font = null; }

/**
 * Returns the size for this format.
 *
 * @return the size
 */
public double getSize() {
    return size == null ? getDefaultFormat().getSize() : size.doubleValue();
}

/**
 * Sets the size
 *
 * @param newSize the new size
 */
public void setSize(double newSize) {
    Double newSizeObj = new Double(newSize);
    if (size != newSizeObj && (size == null || !size.equals(newSizeObj))) {
	size = newSizeObj;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the bold state.
 *
 * @return the bold state
 */
public boolean isBold() {
    return bold == null ? getDefaultFormat().isBold() : bold.booleanValue();
}

/**
 * Sets the bold state.
 *
 * @param newBold the new value
 */
public void setBold(boolean newBold) {
    Boolean newBoldObj = newBold ? Boolean.TRUE : Boolean.FALSE;
    if (bold != newBoldObj && (bold == null || !bold.equals(newBoldObj))) {
	bold = newBoldObj;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the italic state.
 *
 * @return the italic state
 */
public boolean isItalic() {
    return italic == null ? getDefaultFormat().isItalic()
	: italic.booleanValue();
}

/**
 * Sets the italic state.
 *
 * @param newItalic the new value
 */
public void setItalic(boolean newItalic) {
    Boolean newItalicObj = newItalic ? Boolean.TRUE : Boolean.FALSE;
    if (italic != newItalicObj
	&& (italic == null || !italic.equals(newItalicObj)))
    {
	italic = newItalicObj;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the underline state.
 *
 * @return the underline state
 */
public boolean isUnderline() {
    return underline == null ? getDefaultFormat().isUnderline()
	: underline.booleanValue();
}

/**
 * Sets the underline state.
 *
 * @param newUnderline the new underline state
 */
public void setUnderline(boolean newUnderline) {
    Boolean newUnderlineObj = newUnderline ? Boolean.TRUE : Boolean.FALSE;
    if (underline != newUnderlineObj
	&& (underline == null || !underline.equals(newUnderlineObj)))
    {
	underline = newUnderlineObj;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the wrap state.
 *
 * @return the wrap state
 */
public boolean isWrap() {
    return wrap == null ? getDefaultFormat().isWrap() : wrap.booleanValue();
}

/**
 * Sets the wrap state.
 *
 * @param newWrap the new wrap state
 */
public void setWrap(boolean newWrap) {
    Boolean newWrapObj = newWrap ? Boolean.TRUE : Boolean.FALSE;
    if (wrap != newWrapObj && (wrap == null || !wrap.equals(newWrapObj))) {
	wrap = newWrapObj;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the alignment.
 *
 * @return one of the <code>ALIGN_*</code> values
 */
public int getAlign() {
    return align == null ? getDefaultFormat().getAlign() : align.intValue();
}

/**
 * Sets the alignment.
 *
 * @param newAlign one of the <code>ALIGN_*</code> values
 */
public void setAlign(int newAlign) {
    Integer newAlignObj = new Integer(newAlign);
    if (align != newAlignObj && (align == null || !align.equals(newAlignObj))) {
	align = newAlignObj;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the format string for this field. May return <code>null</code>.
 *
 * @return the format string, possibly <code>null</code>
 */
public String getFormat() {
    return format == null ? getDefaultFormat().getFormat() : format;
}

/**
 * Sets the format string.
 *
 * @param newFormat the new format string
 */
public void setFormat(String newFormat) {
    if (format != newFormat && (format == null || !format.equals(newFormat))) {
	format = newFormat;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns the color for this format.
 *
 * @return the color
 */
public Color getColor() {
    return color == null ? getDefaultFormat().getColor() : color;
}

/**
 * Sets the color
 *
 * @param newColor the new color
 */
public void setColor(Color newColor) {
    if (color != newColor) {
	color = newColor;
	setChanged();
	notifyObservers();
    }
}

/**
 * Returns a string representation of this format. Mainly used for
 * debugging.
 *
 * @return pretty string, inn't it?
 */
public String toString() {
    return "Format[size=" + size + ", bold=" + bold + ", italic=" + italic
	+ ", underline=" + underline + ", wrap=" + wrap + ", align="
	+ (align == null ? "null" : alignToString(align.intValue()))
	+ ", format=" + format + ", color=" + color + ", font="
	+ fontFamilyName + "]";
}    

/**
 * Writes this format as an XML tag. Only writes the differences between this
 * format and the default one.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    Format def = getDefaultFormat();

    if (this.equals(def))	// Don't write anything if there are no diffs
	return;

    out.startElement("format");
    if (fontFamilyName != null
	&& !def.getFontFamilyName().equals(fontFamilyName))
	out.attr("font", fontFamilyName);
    if (size != null && size.doubleValue() != def.getSize())
	out.attr("size", size);
    if (bold != null && bold.booleanValue() != def.isBold())
	out.attr("bold", bold);
    if (italic != null && italic.booleanValue() != def.isItalic())
	out.attr("italic", italic);
    if (underline != null && underline.booleanValue() != def.isUnderline())
	out.attr("underline", underline);
    if (wrap != null && wrap.booleanValue() != def.isWrap())
	out.attr("wrap", wrap);
    if (align != null && align.intValue() != def.getAlign())
	out.attr("align", alignToString(align.intValue()));
    if (color != null && !color.equals(def.getColor()))
	out.attr("color", color);
    if (format != null && !format.equals(def.getFormat())
	&& format.length() > 0)
	out.attr("format", format);
    out.endElement();
}

}

/** Only used by the report. */
class DefaultFormat extends Format {

/**
 * Creates a format with all default values filled in.
 */
DefaultFormat() {
    fontFamilyName = DEFAULT_FONT_FAMILY_NAME;
    size = new Double(DEFAULT_SIZE);
    bold = italic = underline = Boolean.FALSE;
    wrap = Boolean.TRUE;
    align = new Integer(ALIGN_LEFT);
    color = DEFAULT_COLOR;
}

public String getFormat() { return format; }

/**
 * Returns a clone of this format.
 */
public Object clone() {
    DefaultFormat f = new DefaultFormat();
    fillClonedField(f);
    return f;
}

/**
 * Writes this format as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out) {
    out.startElement("format");
    out.attr("font", fontFamilyName);
    out.attr("size", size);
    out.attr("bold", bold);
    out.attr("italic", italic);
    out.attr("underline", underline);
    out.attr("wrap", wrap);
    out.attr("align", alignToString(align.intValue()));
    out.attr("color", color);
    if (format != null && format.length() > 0)
	out.attr("format", format);
    out.endElement();
}

}
