package jimm.datavision.source.sql;

/**
 * A helper class used by a SQL query while parsing the WHERE clause.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see SQLQuery
 */
public class ParserHelper {

protected String str;
protected int startPos;
protected String prevToken;
protected int prevTokenStartPos;
protected int endBeforeToken;

public ParserHelper(String s, int pos) {
    str = s;
    startPos = pos;

    findPreviousSQLToken();
}

/**
 * Finds the token before the one that starts at <var>startPos</var> in
 * <var>str</var>. The token will not include the whitespace surrounding
 * it, if any.
 */
protected void findPreviousSQLToken() {
    if (startPos == 0) {
	prevToken = "";
	prevTokenStartPos = 0;
	endBeforeToken = 0;
	return;
    }

    // Move backwards, skipping whitespace, to the end of the previous token.
    prevTokenStartPos = startPos - 1;
    while (prevTokenStartPos >= 0
	   && Character.isSpaceChar(str.charAt(prevTokenStartPos)))
	--prevTokenStartPos;

    int tokenEnd = prevTokenStartPos + 1;

    char c = str.charAt(prevTokenStartPos);
    boolean prevWordIsAlpha = (Character.isLetterOrDigit(c) || c == '_');

    // Continue moving backwards, stopping when we get to whitespace or '}'
    // or a char that is not of the same type.
    while (prevTokenStartPos >= 0
	   && !Character.isSpaceChar(c = str.charAt(prevTokenStartPos))
	   && !(c == '}')	// Always stop at '}'
	   && (prevWordIsAlpha
	       ? (Character.isLetterOrDigit(c) || c == '_')
	       : !(Character.isLetterOrDigit(c) || c == '_')))
	--prevTokenStartPos;
    ++prevTokenStartPos;

    // Set flag which tells us if there is a space before the previous token
//     spaceBeforePrevToken = prevTokenStartPos > 0
// 	&& Character.isSpaceChar(str.charAt(prevTokenStartPos - 1));

    // Find the index of the character after the token before this token.
    // That's the same as the beginning of this whitespace before this
    // token or, if none, the beginning of this token.
    if (prevTokenStartPos == 0)
	endBeforeToken = 0;
    else {
	endBeforeToken = prevTokenStartPos - 1;
	while (endBeforeToken >= 0
	       && Character.isSpaceChar(str.charAt(endBeforeToken)))
	    --endBeforeToken;
	++endBeforeToken;
    }

    // Finally, grab the token itself, sans whitespace.
    prevToken = str.substring(prevTokenStartPos, tokenEnd);
}

/**
 * Returns the previous token, sans whitespace.
 *
 * @return a possibly empty string
 */
public String getPrevToken() { return prevToken; }

/**
 * Returns the starting position of the previous token in the original
 * string, not including any leading whitespace.
 *
 * @return a string index pointing to the start of the previous token
 */
public int getPrevTokenStartPos() { return prevTokenStartPos; }

public int getEndBeforeToken() { return endBeforeToken; }

}
