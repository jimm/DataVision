package jimm.datavision.test;
import jimm.datavision.source.sql.ParserHelper;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Tests {@link ParserHelper}.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ParserHelperTest extends TestCase {

public static Test suite() {
    return new TestSuite(ParserHelperTest.class);
}

public ParserHelperTest(String name) {
    super(name);
}

public void testParamEqString() {
    ParserHelper ph = new ParserHelper("{?1} = 'foo'", 0);
    assertEquals("", ph.getPrevToken());
    assertEquals(0, ph.getPrevTokenStartPos());
    assertEquals(0, ph.getEndBeforeToken());
}

public void testColEqParam() {
    ParserHelper ph = new ParserHelper("{office.name} = {?1}", 16);
    assertEquals("=", ph.getPrevToken());
    assertEquals(14, ph.getPrevTokenStartPos());
    assertEquals(13, ph.getEndBeforeToken());
}

public void testColEqParamNoSpaces() {
    ParserHelper ph = new ParserHelper("{office.name}={?1}", 14);
    assertEquals("=", ph.getPrevToken());
    assertEquals(13, ph.getPrevTokenStartPos());
    assertEquals(13, ph.getEndBeforeToken());
}

public void testColNeqParam() {
    ParserHelper ph = new ParserHelper("{office.name}!={?1}", 15);
    assertEquals("!=", ph.getPrevToken());
    assertEquals(13, ph.getPrevTokenStartPos());
    assertEquals(13, ph.getEndBeforeToken());
}

public void testColIsParam() {
    ParserHelper ph = new ParserHelper("{office.name}is{?1}", 15);
    assertEquals("is", ph.getPrevToken());
    assertEquals(13, ph.getPrevTokenStartPos());
    assertEquals(13, ph.getEndBeforeToken());
}

public void testColIsSpaceAfterParam() {
    ParserHelper ph = new ParserHelper("{office.name}is {?1}", 16);
    assertEquals("is", ph.getPrevToken());
    assertEquals(13, ph.getPrevTokenStartPos());
    assertEquals(13, ph.getEndBeforeToken());
}

public void testColIsSpaceBeforeParam() {
    ParserHelper ph = new ParserHelper("{office.name} is{?1}", 16);
    assertEquals("is", ph.getPrevToken());
    assertEquals(14, ph.getPrevTokenStartPos());
    assertEquals(13, ph.getEndBeforeToken());
}

public void testColIsNotParam() {
    ParserHelper ph = new ParserHelper("{office.name} is not {?1}", 21);
    assertEquals("not", ph.getPrevToken());
    assertEquals(17, ph.getPrevTokenStartPos());
    assertEquals(16, ph.getEndBeforeToken());
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
