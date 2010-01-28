package jimm.util;
import java.awt.FontMetrics;
import java.util.*;

/**
 * Globally available utility classes, mostly for string manipulation.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class StringUtils {

protected static final int DEFAULT_MAX_MESSAGE_WIDTH = 78;

/**
 * Returns a list of substrings created by splitting the given string at
 * the given delimiter. The return value will be <code>null</code> if the
 * string is <code>null</code>, else it will be a non-empty list of strings.
 * If <var>delim</var> is <code>null</code> or is not found in the string,
 * the list will contain one element: the original string.
 * <p>
 * This isn't the same thing as using a tokenizer. <var>delim</var> is
 * a literal string, not a set of characters any of which may be a
 * delimiter.
 *
 * @param str the string we're splitting
 * @param delim the delimter string
 */
public static List split(String str, String delim) {
    if (str == null)
	return null;

    ArrayList list = new ArrayList();

    if (delim == null) {
	list.add(str);
	return list;
    }

    int subStart, afterDelim = 0;
    int delimLength = delim.length();
    while ((subStart = str.indexOf(delim, afterDelim)) != -1) {
	list.add(str.substring(afterDelim, subStart));
	afterDelim = subStart + delimLength;
    }
    if (afterDelim <= str.length())
	list.add(str.substring(afterDelim));

    return list;
}

/**
 * Returns a string consisting of all members of a collection separated
 * by the specified string. The <code>toString</code> method of each
 * collection member is called to convert it to a string.
 *
 * @param c a collection of objects
 * @param joinWith the string that will separate each member of the collection
 */
public static String join(Collection c, String joinWith) {
    if (c == null)
	return "";

    StringBuffer buf = new StringBuffer();
    boolean first = true;
    for (Iterator iter = c.iterator(); iter.hasNext(); ) {
	if (first) first = false;
	else if (joinWith != null) buf.append(joinWith);
	buf.append(iter.next().toString());
    }
    return buf.toString();
}

/**
 * Returns an array of strings, one for each line in the string. Lines end
 * with any of cr, lf, or cr lf. A line ending at the end of the string
 * will not output a further, empty string.
 * <p>
 * This code assumes <var>str</var> is not <code>null</code>.
 *
 * @param str the string to split
 * @return a non-empty list of strings
 */
public static List splitIntoLines(String str) {
    ArrayList strings = new ArrayList();

    int len = str.length();
    if (len == 0) {
	strings.add("");
	return strings;
    }

    int lineStart = 0;

    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	if (c == '\r') {
	    int newlineLength = 1;
	    if ((i + 1) < len && str.charAt(i + 1) == '\n')
		newlineLength = 2;
	    strings.add(str.substring(lineStart, i));
	    lineStart = i + newlineLength;
	    if (newlineLength == 2) // skip \n next time through loop
		++i;
	}
	else if (c == '\n') {
	    strings.add(str.substring(lineStart, i));
	    lineStart = i + 1;
	}
    }
    if (lineStart < len)
	strings.add(str.substring(lineStart));

    return strings;
}

/**
 * Appends a string to a string buffer, adding extra newlines so the message
 * is not too wide. Max width is not guaranteed; if there is no space in a
 * line before <code>DEFAULT_MAX_MESSAGE_WIDTH</code> then the next one after
 * it will be used insetead. Each line will be trimmed before and after it's
 * added, so some whitespace may be goofed up. This is used for error message
 * wrapping, so it's not critical that whitespace be preserved.
 * <p>
 * TODO Looks for space, not all whitespace. This should probably change.
 *
 * @param buf the string buffer
 * @param str the string
 */
public static void splitUp(StringBuffer buf, String str) {
    splitUp(buf, str, DEFAULT_MAX_MESSAGE_WIDTH);
}

/**
 * Appends a string to a string buffer, adding extra newlines so the
 * message is not too wide. Max width is not guaranteed; if there is no space
 * in a line before <var>maxWidth</var> then the next one after it will be
 * used instead. Each line will be trimmed before and after it's added,
 * so some whitespace may be goofed up. This is used for error message
 * wrapping, so it's not critical that whitespace be preserved.
 * <p>
 * TODO Looks for space, not all whitespace. This should probably change.
 *
 * @param buf the string buffer
 * @param str the string
 * @param maxWidth maximum number of chars in each line
 */
public static void splitUp(StringBuffer buf, String str, int maxWidth) {
    if (str == null)
	return;

    str = str.trim();
    while (str.length() >= maxWidth) {
	int pos = str.lastIndexOf(' ', maxWidth);
	if (pos == -1) {	// No spaces before; look for first one after
	    pos = str.indexOf(' ', maxWidth);
	    if (pos == -1)
		break;
	}
	buf.append(str.substring(0, pos).trim());
	buf.append("\n");
	str = str.substring(pos + 1).trim();
    }
    buf.append(str);
}

/**
 * Returns an array of strings, one for each line in the string after it
 * has been wrapped to fit lines of <var>maxWidth</var>. Lines end
 * with any of cr, lf, or cr lf. A line ending at the end of the string
 * will not output a further, empty string.
 * <p>
 * This code assumes <var>str</var> is not <code>null</code>.
 *
 * @param str the string to split
 * @param fm needed for string width calculations
 * @param maxWidth the max line width, in points
 * @return a non-empty list of strings
 */
public static List wrap(String str, FontMetrics fm, int maxWidth) {
    List lines = splitIntoLines(str);
    if (lines.size() == 0)
	return lines;

    ArrayList strings = new ArrayList();
    for (Iterator iter = lines.iterator(); iter.hasNext(); )
	wrapLineInto((String)iter.next(), strings, fm, maxWidth);
    return strings;
}

/**
 * Given a line of text and font metrics information, wrap the line and
 * add the new line(s) to <var>list</var>.
 *
 * @param line a line of text
 * @param list an output list of strings
 * @param fm font metrics
 * @param maxWidth maximum width of the line(s)
 */
public static void wrapLineInto(String line, List list, FontMetrics fm,
				int maxWidth)
{
    int len = line.length();
    int width;
    while (len > 0 && (width = fm.stringWidth(line)) > maxWidth) {
	// Guess where to split the line. Look for the next space before
	// or after the guess.
	int guess = len * maxWidth / width;
	String before = line.substring(0, guess).trim();

	width = fm.stringWidth(before);
	int pos;
	if (width > maxWidth)	// Too long
	    pos = findBreakBefore(line, guess);
	else {			// Too short or possibly just right
	    pos = findBreakAfter(line, guess);
	    if (pos != -1) {	// Make sure this doesn't make us too long
		before = line.substring(0, pos).trim();
		if (fm.stringWidth(before) > maxWidth)
		    pos = findBreakBefore(line, guess);
	    }
	}
	if (pos == -1) pos = guess;	// Split in the middle of the word

	list.add(line.substring(0, pos).trim());
	line = line.substring(pos).trim();
	len = line.length();
    }
    if (len > 0)
	list.add(line);
}

/**
 * Returns the index of the first whitespace character or '-' in
 * <var>line</var> that is at or before <var>start</var>. Returns -1 if no
 * such character is found.
 *
 * @param line a string
 * @param start where to star looking
 */
public static int findBreakBefore(String line, int start) {
    for (int i = start; i >= 0; --i) {
	char c = line.charAt(i);
	if (Character.isWhitespace(c) || c == '-')
	    return i;
    }
    return -1;
}

/**
 * Returns the index of the first whitespace character or '-' in
 * <var>line</var> that is at or after <var>start</var>. Returns -1 if no
 * such character is found.
 *
 * @param line a string
 * @param start where to star looking
 */
public static int findBreakAfter(String line, int start) {
    int len = line.length();
    for (int i = start; i < len; ++i) {
	char c = line.charAt(i);
	if (Character.isWhitespace(c) || c == '-')
	    return i;
    }
    return -1;
}

/**
 * Returns a string with HTML special characters replaced by their entity
 * equivalents.
 *
 * @param str the string to escape
 * @return a new string without HTML special characters
 */
public static String escapeHTML(String str) {
    if (str == null || str.length() == 0)
	return "";

    StringBuffer buf = new StringBuffer();
    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	switch (c) {
	case '&': buf.append("&amp;"); break;
	case '<': buf.append("&lt;"); break;
	case '>': buf.append("&gt;"); break;
	case '"': buf.append("&quot;"); break;
	case '\'': buf.append("&apos;"); break;
	default: buf.append(c); break;
	}
    }
    return buf.toString();
}

/**
 * Returns a new string where all newlines (&quot;\n&quot;, &quot;\r&quot;,
 * or &quot;\r\n&quot;) have been replaced by &quot;\n&quot; plus XHTML
 * break tags (&quot;\n&lt;br /&gt;&quot;).
 * <p>
 * We don't call <code>splitIntoLines</code> because that method does not
 * tell us if the string ended with a newline or not.
 *
 * @param str any string
 * @return a new string with all newlines replaced by
 * &quot;\n&lt;br /&gt;&quot;
 */
public static String newlinesToXHTMLBreaks(String str) {
    if (str == null || str.length() == 0)
	return "";

    StringBuffer buf = new StringBuffer();
    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	switch (c) {
	case '\n': buf.append("\n<br />"); break;
	case '\r':
	    if (i + 1 < len && str.charAt(i + 1) == '\n') // Look for '\n'
		++i;
	    buf.append("\n<br />");	
	    break;
	default:
	    buf.append(c); break;
	}
    }
    return buf.toString();
}

/**
 * Returns a string with XML special characters replaced by their entity
 * equivalents.
 *
 * @param str the string to escape
 * @return a new string without XML special characters
 */
public static String escapeXML(String str) {
    return escapeHTML(str);
}

/**
 * Returns a string with XML entities replaced by their normal characters.
 *
 * @param str the string to un-escape
 * @return a new normal string
 */
public static String unescapeXML(String str) {
    if (str == null || str.length() == 0)
	return "";

    StringBuffer buf = new StringBuffer();
    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	if (c == '&') {
	    int pos = str.indexOf(";", i);
	    if (pos == -1) {	// Really evil
		buf.append('&');
	    }
	    else if (str.charAt(i+1) == '#') {
		int val = Integer.parseInt(str.substring(i+2, pos), 16);
		buf.append((char)val);
		i = pos;
	    }
	    else {
		String substr = str.substring(i, pos + 1);
		if (substr.equals("&amp;"))
		    buf.append('&');
		else if (substr.equals("&lt;"))
		    buf.append('<');
		else if (substr.equals("&gt;"))
		    buf.append('>');
		else if (substr.equals("&quot;"))
		    buf.append('"');
		else if (substr.equals("&apos;"))
		    buf.append('\'');
		else		// ????
		    buf.append(substr);
		i = pos;
	    }
	}
	else {
	    buf.append(c);
	}
    }
    return buf.toString();
}

/**
 * Returns a new string with all strings delimited by <var>start</var> and
 * <var>end</var> replaced by whatever is generated by the
 * <code>Replacer</code> <var>r</var>. The delimiters themselves are
 * not part of the returned string.
 * <p>
 * If the <code>Replacer</code> ever returns <code>null</code>, we return
 * <code>null</code>.
 *
 * @param start the delimiter start (for example, &quot;{&#64;&quot;)
 * @param end the delimiter end (for example, &quot;}&quot;)
 * @param r the replacer; takes the text between <var>start</var> and
 * <var>end</var> and returns the replacement text
 * @param s the string we're munging
 * @return a new string munged by the replacer, or <code>null</code> if
 * the replacer ever returns <code>null</code>
 */
public static String replaceDelimited(String start, String end, Replacer r,
				      String s)
{
    return replaceDelimited(null, start, end, r, s);
}

/**
 * Returns a new string with all strings delimited by <var>start</var> and
 * <var>end</var> (but not immediately preceeded by <var>exceptAfter</var>)
 * replaced by whatever is generated by the <code>Replacer</code>
 * <var>r</var>. The delimiters themselves are not part of the returned
 * string.
 * <p>
 * If the <code>Replacer</code> ever returns <code>null</code>, we return
 * <code>null</code>.
 *
 * @param exceptAfter ignore <var>start</var> if it appears immediately
 * after this string; may be <code>null</code>
 * @param start the delimiter start (for example, &quot;{&#64;&quot;)
 * @param end the delimiter end (for example, &quot;}&quot;)
 * @param r the replacer; takes the text between <var>start</var> and
 * <var>end</var> and returns the replacement text
 * @param s the string we're munging
 * @return a new string munged by the replacer, or <code>null</code> if
 * the replacer ever returns <code>null</code>
 */
public static String replaceDelimited(String exceptAfter, String start,
				      String end, Replacer r, String s)
{
    if (s == null)
	return null;

    int startLength, endLength;
    if (start == null || end == null || (startLength = start.length()) == 0
	|| (endLength = end.length()) == 0)
	return s;

    int exceptAfterLength = exceptAfter == null ? 0 : exceptAfter.length();

    String str = new String(s);	// We're gonna munge the string, so copy it
    int pos, pos2;
    int searchFrom = 0;
    while ((pos = str.indexOf(start, searchFrom)) != -1) {
	// Skip this one if it is immediately preceeded by exceptAfter.
	if (exceptAfterLength > 0) {
	    int lookFrom = pos - exceptAfterLength;
	    if (lookFrom >= 0
		&& str.indexOf(exceptAfter, lookFrom) == lookFrom)
	    {
		searchFrom = pos + 1;
		continue;
	    }
	}

	pos2 = str.indexOf(end, pos + startLength);
	if (pos2 != -1) {
	    Object val = r.replace(str.substring(pos + startLength, pos2));
	    if (val == null)
		return null;
	    String valAsString = val.toString();
	    str = str.substring(0, pos) + valAsString
		+ str.substring(pos2 + endLength);
	    searchFrom = pos + valAsString.length();
	}
	else			// Didn't find end delimiter; stop right here
	    break;
    }
    return str;
}

/**
 * Returns <var>str</var> with leading and trailing spaces trimmed or, if
 * <var>str</var> is <code>null</code>, returns <code>null</code>.
 *
 * @return str trimmed or <code>null</code>
 */
public static String nullOrTrimmed(String str) {
    return str == null ? str : str.trim();
}

}
