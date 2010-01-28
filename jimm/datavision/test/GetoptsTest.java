package jimm.datavision.test;
import jimm.util.Getopts;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class GetoptsTest extends TestCase {

protected static final String DEFAULT_ARGS_LIST[] = {
    "-a", "-b", "-c", "-f", "farg", "-g", "-eearg",
    "non-option-arg one", "non-option-arg two"
};

protected String[] args;
protected Getopts g;

public static Test suite() {
    return new TestSuite(GetoptsTest.class);
}

public GetoptsTest(String name) {
    super(name);
}

public void setUp() {
    args = DEFAULT_ARGS_LIST;
    g = new Getopts("abcd:e:f:gh:i", args);
}

public void testSimpleOptions() {
    String[] args = {
	"-a", "-b", "-c", "-f", "farg", "-g", "-eearg",
	"non-option-arg one", "non-option-arg two",
    };
    Getopts g = new Getopts("abcd:e:f:gh:i", args);

    assertTrue(!g.error());

    assertTrue(g.hasOption('a'));
    assertTrue(g.hasOption('b'));
    assertTrue(g.hasOption('c'));
    assertTrue(!g.hasOption('d'));
    assertTrue(g.hasOption('e'));
    assertTrue(g.hasOption('f'));
    assertTrue(g.hasOption('g'));
    assertTrue(!g.hasOption('h'));
    assertTrue(!g.hasOption('i'));
}

public void testIllegalArg() {
    // Add new illegal argument -z to front of list
    String[] argsWithIllegalValue = new String[args.length + 1];
    System.arraycopy(args, 0, argsWithIllegalValue, 1, args.length);
    argsWithIllegalValue[0] = "-z";
    g = new Getopts("abcd:e:f:gh:i", argsWithIllegalValue);

    assertTrue(g.error());	// That -z doesn't belong
    assertTrue(!g.hasOption('z'));
}

public void testDefaultValues() {
    assertEquals("", g.option('d'));
    assertNull(g.option('d', null));
    assertEquals("xyzzy", g.option('d', "xyzzy"));
    assertEquals("earg", g.option('e'));
    assertEquals("farg", g.option('f'));
}

public void testRemainingArgs() {
    assertEquals(2, g.argc());
    assertEquals(args[args.length - 2], g.argv(0));
    assertEquals(args[args.length - 1], g.argv(1));
}

public void testSimpleCommandLine() {
    String[] args = { "-p", "password", "filename" };
    Getopts g = new Getopts("cdhlxnp:s:r:", args);

    assertTrue(!g.error());
    assertEquals("password", g.option('p'));
    assertEquals(1, g.argc());
    assertEquals("filename", g.argv(0));
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
