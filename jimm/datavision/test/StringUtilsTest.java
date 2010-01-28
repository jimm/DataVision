package jimm.datavision.test;
import jimm.util.StringUtils;
import jimm.util.Replacer;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class StringUtilsTest extends TestCase {

public static Test suite() {
    return new TestSuite(StringUtilsTest.class);
}

public StringUtilsTest(String name) {
    super(name);
}

public void testEscapeXML() {
    assertEquals("", StringUtils.escapeXML(null));
    assertEquals("", StringUtils.escapeXML(""));
    assertEquals("abc&amp;def", StringUtils.escapeXML("abc&def"));
    assertEquals("abc&quot;def&apos;", StringUtils.escapeXML("abc\"def'"));
    assertEquals("&lt;&gt;", StringUtils.escapeXML("<>"));
}

public void testUnescapeXML() {
    assertEquals("", StringUtils.unescapeXML(null));
    assertEquals("", StringUtils.unescapeXML(""));
    assertEquals("abc&def", StringUtils.unescapeXML("abc&amp;def"));
    assertEquals("abc\"def'", StringUtils.unescapeXML("abc&quot;def&apos;"));
    assertEquals("<>", StringUtils.unescapeXML("&lt;&gt;"));
    assertEquals("abc def", StringUtils.unescapeXML("abc&#20;def"));
}

public void testNewlines() {
    assertEquals("", StringUtils.newlinesToXHTMLBreaks(null));
    assertEquals("", StringUtils.newlinesToXHTMLBreaks(""));
    assertEquals("\n<br />", StringUtils.newlinesToXHTMLBreaks("\n"));
    assertEquals("\n<br />", StringUtils.newlinesToXHTMLBreaks("\r"));
    assertEquals("\n<br />", StringUtils.newlinesToXHTMLBreaks("\r\n"));
    assertEquals("hello\n<br />world",
		 StringUtils.newlinesToXHTMLBreaks("hello\nworld"));
    assertEquals("hello\n<br />world",
		 StringUtils.newlinesToXHTMLBreaks("hello\rworld"));
    assertEquals("hello\n<br />world",
		 StringUtils.newlinesToXHTMLBreaks("hello\r\nworld"));
}

public void testSplit() {
    ArrayList typicalAnswer = new ArrayList();
    typicalAnswer.add("a");
    typicalAnswer.add("b");
    typicalAnswer.add("c");

    ArrayList origStringAnswer = new ArrayList();
    origStringAnswer.add("abc");

    // Special cases
    assertNull(StringUtils.split(null, null));
    assertEquals(origStringAnswer, StringUtils.split("abc", null));
    assertEquals(origStringAnswer, StringUtils.split("abc", "x"));

    assertEquals(typicalAnswer, StringUtils.split("a.b.c", "."));
    assertEquals(typicalAnswer, StringUtils.split("a**&*b**&*c", "**&*"));

    ArrayList answer = new ArrayList(typicalAnswer);
    answer.add(0, "");
    assertEquals(answer, StringUtils.split(".a.b.c", "."));
    assertEquals(answer, StringUtils.split("**&*a**&*b**&*c", "**&*"));

    answer = new ArrayList(typicalAnswer);
    answer.add(1, "");
    assertEquals(answer, StringUtils.split("a..b.c", "."));
    assertEquals(answer, StringUtils.split("a**&***&*b**&*c", "**&*"));

    answer = new ArrayList(typicalAnswer);
    answer.add("");
    assertEquals(answer, StringUtils.split("a.b.c.", "."));
    assertEquals(answer, StringUtils.split("a**&*b**&*c**&*", "**&*"));

    answer = new ArrayList(typicalAnswer);
    answer.add(1, "");
    answer.add("");
    assertEquals(answer, StringUtils.split("a..b.c.", "."));
    assertEquals(answer, StringUtils.split("a**&***&*b**&*c**&*", "**&*"));
}

public void testJoin() {
    assertEquals("", StringUtils.join(null, null));
    assertEquals("", StringUtils.join(null, ","));

    ArrayList list = new ArrayList();
    assertEquals("", StringUtils.join(list, null));
    assertEquals("", StringUtils.join(list, ","));

    list.add("a");
    assertEquals("a", StringUtils.join(list, null));
    assertEquals("a", StringUtils.join(list, ","));

    list.add("xyzzy");
    assertEquals("axyzzy", StringUtils.join(list, null));
    assertEquals("a,xyzzy", StringUtils.join(list, ","));
    assertEquals("a-*-xyzzy", StringUtils.join(list, "-*-"));
}

public void testReplace() {
    Replacer r = new Replacer() {
	public Object replace(String s) { return "x"; }
	};

    assertNull(StringUtils.replaceDelimited("", "", null, null));
    assertNull(StringUtils.replaceDelimited(null, null, null, null));
    assertEquals("abcde", StringUtils.replaceDelimited(null, null, r, "abcde"));
    assertEquals("abcde", StringUtils.replaceDelimited("", "", r, "abcde"));
    assertEquals("", StringUtils.replaceDelimited("{%", "}", r, ""));

    assertEquals("abxde", StringUtils.replaceDelimited("{%", "}", r, "ab{%c}de"));
    assertEquals("xabxdex", StringUtils.replaceDelimited("{%", "}", r,
						   "{%}ab{%c}de{%   }"));
    assertEquals("abcd{%e", StringUtils.replaceDelimited("{%", "}", r, "abcd{%e"));
    assertEquals("x.nil? ? nil : x / 100.0",
		 StringUtils.replaceDelimited("{", "}", r,
					"{jobs.hourly rate}.nil? ? nil : {jobs.hourly rate} / 100.0"));
}

public void testReplaceNotAfter() {
    Replacer r = new Replacer() {
	public Object replace(String s) { return "x"; }
	};

    assertNull(StringUtils.replaceDelimited("#", "", "", null, null));
    assertNull(StringUtils.replaceDelimited("#", "#", "#", null, null));
    assertNull(StringUtils.replaceDelimited("#", null, null, null, null));
    assertEquals("abcde", StringUtils.replaceDelimited("#", null, null, r, "abcde"));
    assertEquals("abcde", StringUtils.replaceDelimited("#", "", "", r, "abcde"));
    assertEquals("", StringUtils.replaceDelimited("#", "{%", "}", r, ""));

    assertEquals("abxde",
		 StringUtils.replaceDelimited("#", "{%", "}", r, "ab{%c}de"));
    assertEquals("xabxdex", StringUtils.replaceDelimited("#", "{%", "}", r,
						   "{%}ab{%c}de{%   }"));
    assertEquals("abcd{%e",
		 StringUtils.replaceDelimited("#", "{%", "}", r, "abcd{%e"));

    assertEquals("ab#{c}de",
		 StringUtils.replaceDelimited("#", "{", "}", r, "ab#{c}de"));
    assertEquals("ab##{c}de",
		 StringUtils.replaceDelimited("#", "{", "}", r, "ab##{c}de"));
    assertEquals("ab##{x}dex",
		 StringUtils.replaceDelimited("#", "{", "}", r, "ab##{{c}}de{z}"));
    assertEquals("ab#{x}de",
		 StringUtils.replaceDelimited("#", "{", "}", r, "ab#{{c}}de"));
    assertEquals("ab#{x}de",
		 StringUtils.replaceDelimited("#", "{%", "}", r, "ab#{{%c}}de"));
}

public void testAvoidInfiniteLoop() {
    final int[] count = new int[1];
    count[0] = 0;
    Replacer r = new Replacer() {
	public Object replace(String s) {
	    if (++count[0] == 2)
		fail("caught in an infinite loop");
	    return "{" + s + "}";
	}
	};
    assertEquals("a{}b",
		 StringUtils.replaceDelimited(null, "{", "}", r, "a{}b"));
}

public void testLineSplit() {
    ArrayList answer = new ArrayList();
    answer.add("");

    List split = StringUtils.splitIntoLines("");
    assertEquals(answer.size(), split.size());
    assertEquals(answer, split);

    // The three flavors of line endings
    String[] endings = { "\n", "\r", "\r\n" };

    // We want to see an array with one empty string (the text before the
    // newline). There is no string after the newline.
    for (int i = 0; i < endings.length; ++i) {
	split = StringUtils.splitIntoLines(endings[i]);
	assertEquals(answer.size(), split.size());
	assertEquals(answer, split);
    }

    answer.add("x");
    for (int i = 0; i < endings.length; ++i) {
	split = StringUtils.splitIntoLines(endings[i] + "x");
	assertEquals(answer.size(), split.size());
	assertEquals(answer, split);
    }

    answer = new ArrayList();
    answer.add("line one");
    answer.add("part deux");
    answer.add("three's a crowd, eh?");
    answer.add("");

    split = StringUtils.splitIntoLines("line one\npart deux\nthree's a crowd, eh?"
				 + "\n\n");
    assertEquals(answer.size(), split.size());
    assertEquals(answer, split);

    // Test two newlines in a row
    answer = new ArrayList();
    answer.add("two newlines after this line");
    answer.add("");
    answer.add("part deux");
    answer.add("three's a crowd, eh?");
    answer.add("");

    split = StringUtils.splitIntoLines("two newlines after this line\n\npart deux\nthree's a crowd, eh?"
				 + "\n\n");
    assertEquals(answer.size(), split.size());
    assertEquals(answer, split);

    split = StringUtils.splitIntoLines("two newlines after this line\r\n\r\npart deux\r\nthree's a crowd, eh?"
				 + "\n\n");
    assertEquals(answer.size(), split.size());
    assertEquals(answer, split);

    split = StringUtils.splitIntoLines("two newlines after this line\r\rpart deux\rthree's a crowd, eh?"
				 + "\r\r");
    assertEquals(answer.size(), split.size());
    assertEquals(answer, split);
}

public void testSplitUp() {
    StringBuffer buf = new StringBuffer();
    StringUtils.splitUp(buf, null);
    assertEquals("", buf.toString());

    buf = new StringBuffer();
    StringUtils.splitUp(buf, "abcde abcde abcde abcde", 12);
    assertEquals("abcde abcde\nabcde abcde", buf.toString());

    buf = new StringBuffer();
    StringUtils.splitUp(buf, "abcdeabcdeabcdeabcde", 12);
    assertEquals("abcdeabcdeabcdeabcde", buf.toString());

    buf = new StringBuffer();
    StringUtils.splitUp(buf, "  abcde", 12);
    assertEquals("abcde", buf.toString());

    buf = new StringBuffer();
    StringUtils.splitUp(buf, "  abcde      abcde     abcde    abcde  ", 12);
    assertEquals("abcde\nabcde\nabcde\nabcde", buf.toString());
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
