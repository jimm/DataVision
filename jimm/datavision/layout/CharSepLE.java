package jimm.datavision.layout;
import jimm.datavision.*;
import jimm.datavision.field.*;
import java.io.*;

/**
 * <code>CharSepLE</code> is a layout engine that outputs text data files.
 * Output is one line per row of data. Column data is separated by a
 * user-specified character.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class CharSepLE extends SortedLayoutEngine {

protected char sepChar;
protected boolean first;

/**
 * Constructor.
 *
 * @param out the output writer
 * @param sepChar the character used to separate column data
 */
public CharSepLE(PrintWriter out, char sepChar) {
    super(out);
    this.sepChar = sepChar;
}

/**
 * This override handles output of a section.
 *
 * @param sect section
 */
protected void doOutputSection(Section sect) {
    first = true;
    super.doOutputSection(sect);
    out.println();
}

/**
 * This override handles output of a field.
 *
 * @param field a field
 */
protected void doOutputField(Field field) {
    String fieldAsString = field.toString();
    if (fieldAsString == null)
	fieldAsString = "";

    if (first)
	first = false;
    else
	out.print(sepChar);

    // Make fieldAsString safe for comma- or tab-separated data
    out.print(asSafeSepString(fieldAsString));
}

protected void doOutputImage(ImageField image) {
    doOutputField(image);
}

/**
 * Ignores line output.
 *
 * @param line a line
 */
protected void doOutputLine(Line line) {}

/**
 * Return a string that's safe to use in a comma-delimited data file.
 * Returns a new string with all double quotes replaced by two double
 * quotes. If there are any double quotes, commas, or newlines in the
 * string, the returned string will be surrounded by double quotes.
 *
 * @param str a string to be used in a comma-delimited data file
 * @return a new string that's safe for use in a comma-delimited data file
 */
protected String asSafeSepString(String str) {
    if (str == null)
	return "";

    StringBuffer buf = new StringBuffer();
    boolean needsToBeQuoted = false;
    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	switch (c) {
	case '"':
	    buf.append("\"\"");
	    needsToBeQuoted = true;
	    break;
	case '\n':
	case '\r':
	    buf.append(c);
	    needsToBeQuoted = true;
	    break;
	default:
	    buf.append(c);
	    if (c == sepChar)
		needsToBeQuoted = true;
	    break;
	}
    }

    if (needsToBeQuoted) {
	buf.insert(0, '"');
	buf.append('"');
    }
    return buf.toString();
}

}
