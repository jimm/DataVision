package jimm.util;
import java.awt.Color;
import jimm.util.StringUtils;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * An XML writer is a print writer that knows how to output XML elements
 * and make the output look pretty.
 * <p>
 * Calling <code>indent</code> and <code>outdent</code> changes the
 * indentation level. The default indentation width is 4 spaces.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class XMLWriter extends PrintWriter {

// ================================================================
static class ElementInfo {
String name;
boolean outdentBeforeEnd;
ElementInfo(String name) {
    this.name = name;
    outdentBeforeEnd = false;
}
}
// ================================================================

protected static final int DEFAULT_INDENTATION_WIDTH = 4;

protected int width;
protected int level;
protected boolean newline;
protected ArrayList elementStack;
protected boolean inElementStart;

/**
 * Constructor, same as the <code>PrintWriter</code> version.
 *
 * @param out an output stream
 */
public XMLWriter(OutputStream out) {
    super(out);
    init(DEFAULT_INDENTATION_WIDTH);
}

/**
 * Constructor, same as <code>PrintWriter</code> version without the
 * <i>width</i> parameter.
 *
 * @param out an output stream
 * @param autoFlush if <code>true</code>, the <code>println()</code> methods
 * will flush the output bufferset to flush after every line
 * @param width indentation width in spaces
 */
public XMLWriter(OutputStream out, boolean autoFlush, int width) {
    super(out, autoFlush);
    init(width);
}

/**
 * Constructor, same as the <code>PrintWriter</code> version.
 *
 * @param out a writer
 */
public XMLWriter(Writer out) {
    super(out);
    init(DEFAULT_INDENTATION_WIDTH);
}

/**
 * Constructor, same as the <code>PrintWriter</code> version.
 *
 * @param out a writer
 * @param autoFlush if <code>true</code>, the <code>println()</code> methods
 * will flush the output bufferset to flush after every line
 * @param width indentation width in spaces
 */
public XMLWriter(Writer out, boolean autoFlush, int width) {
    super(out, autoFlush);
    init(width);
}

/**
 * Initializes some instance variables. Called from constructors.
 *
 * @param indentationWidth number of spaces per indentation level
 */
protected void init(int indentationWidth) {
    width = indentationWidth;
    level = 0;
    newline = true;
    elementStack = new ArrayList();
    inElementStart = false;
}

/**
 * Increases the indentation level by one.
 */
public void indent() { ++level; }

/**
 * Decreases the indentation level by one.
 */
public void outdent() { if (--level < 0) level = 0; }

public void print(boolean b) { doIndent(); super.print(b); }
public void print(char c) {
    doIndent();
    super.print(c);
    if (c == '\n') newline = true;
}
public void print(char[] s) {
    for (int i = 0; i < s.length; ++i) print(s[i]);
}
public void print(double d) { doIndent(); super.print(d); }
public void print(float f) { doIndent(); super.print(f); }
public void print(int i) { doIndent(); super.print(i); }
public void print(long l) { doIndent(); super.print(l); }
public void print(Object obj) { print(obj.toString()); }

/**
 * This method does not handle newlines embedded in the string.
 *
 * @param str the string to output
 */
public void print(String str) { doIndent(); super.print(str); }

public void println() {
    super.println();
    newline = true;
}
public void println(boolean b) { doIndent(); super.println(b); }
public void println(char c) {
    doIndent();
    super.println(c);
    if (c == '\n') {
	newline = true;
	doIndent();
	newline = true;
    }
}
public void println(char[] s) {
    for (int i = 0; i < s.length; ++i) print(s[i]);
    println();
}
public void println(double d) { doIndent(); super.println(d); newline = true; }
public void println(float f) { doIndent(); super.println(f); newline = true; }
public void println(int i) { doIndent(); super.println(i); newline = true; }
public void println(long l) { doIndent(); super.println(l); newline = true; }
public void println(Object obj) { println(obj.toString()); }
// FIX: this is not correct, but it is not worth fixing right now.
// It does not handle newlines embedded in the string, but I do not care.
public void println(String str) {
    doIndent();
    super.println(str);
    newline = true;
}

/**
 * Performs indentation by printing the correct number of tabs and spaces.
 */
protected void doIndent() {
    if (newline) {
	int spaces = level * width;
	while (spaces >= 8) {
	    super.print("\t");
	    spaces -= 8;
	}
	super.print("        ".substring(0, spaces));
	newline = false;
    }
}

public void xmlDecl(String encoding) {
    println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
}

/** Writes the end of the start of an element. */
protected void finishStartElement(boolean outputNewline) {
    inElementStart = false;
    print('>');
    if (outputNewline) println();
}

/** Writes the end of the start of an element. */
protected void finishStartElement() {
    inElementStart = false;
    println('>');
}

protected void parentShouldOutdent() {
    ElementInfo info = (ElementInfo)elementStack.get(elementStack.size() - 1);
    info.outdentBeforeEnd = true;
}

/**
 * Starts an element. This may be followed by zero or more calls to
 * <code>attribute</code>. The start-element will be closed by the first
 * following call to any method other than attribute.
 */
public void startElement(String name) {
    if (inElementStart) {
	finishStartElement();
	indent();
	parentShouldOutdent();
    }
    elementStack.add(new ElementInfo(name));
    inElementStart = true;
    doIndent();
    print("<" + name);
}

/** Writes an attribute. */
public void attr(String name, String value) {
    print(" " + name + "=\"" + StringUtils.escapeXML(value) + "\"");
}

public void attr(String name, double value) {
    print(" " + name + "=\"" + value + "\"");
}

public void attr(String name, int value) {
    print(" " + name + "=\"" + value + "\"");
}

public void attr(String name, char value) {
    attr(name, "" + value);
}

public void attr(String name, boolean bool) {
    attr(name, bool ? "true" : "false");
}

public void attr(String name, Color c) {
    attr(name, "" + c.getRed() + ';' + c.getGreen() + ';' + c.getBlue() + ';'
	 + c.getAlpha());
}

public void attr(String name, Object value) {
    attr(name, value.toString());
}

/**
 * Ends an element. This may output an end-tag or close the current
 * start-tag as an empty element.
 */
public void endElement() {
    ElementInfo info =
	(ElementInfo)elementStack.remove(elementStack.size() - 1);
    if (info.outdentBeforeEnd)
	outdent();

    if (inElementStart) {
	inElementStart = false;
	println("/>");
    }
    else {
	doIndent();
	println("</" + info.name + ">");
    }
}

public void cdataElement(String name, String text) {
    startElement(name);
    cdata(text);
    endElement();

//     boolean in = false;
//     if (inElementStart) {
// 	finishStartElement();
// 	in = true;
//     }
//     if (in) indent();
//     doIndent();
//     print("<" + name + ">");
//     cdata(text);
//     println("</" + name + ">");
//     if (in) outdent();
}

public void cdata(String text) {
    if (inElementStart)
	finishStartElement(false);
    print("<![CDATA[" + (text == null ? "" : text) + "]]>");
}

public void textElement(String name, String text) {
    startElement(name);
    text(text);
    endElement();

//     boolean in = false;
//     if (inElementStart) {
// 	finishStartElement();
// 	in = true;
//     }
//     if (in) indent();
//     doIndent();
//     print("<" + name + ">");
//     text(text);
//     println("</" + name + ">");
//     if (in) outdent();
}

public void text(String text) {
    if (inElementStart)
	finishStartElement(false);
    print(StringUtils.escapeXML(text == null ? "" : text));
}

public void comment(String text) {
    if (inElementStart)
	finishStartElement();
    doIndent();
    println("<!-- " + text + " -->");
}

}
