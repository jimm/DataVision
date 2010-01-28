package jimm.datavision.test;
import jimm.datavision.Report;
import jimm.datavision.test.mock.source.MockDataSource;
import jimm.util.XMLWriter;
import java.awt.Color;
import java.io.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class XMLWriterTest extends TestCase {

protected static final File EXAMPLE_REPORT =
    new File(AllTests.testDataFile("test.xml"));
protected static final File PARAM_INPUT_FILE =
        new File(AllTests.testDataFile("test_parameters.xml"));

protected StringWriter s;
protected XMLWriter out;
protected String linesep;

public static Test suite() {
    return new TestSuite(XMLWriterTest.class);
}

public XMLWriterTest(String name) {
    super(name);
}

public void setUp() {
    s = new StringWriter();
    out = new XMLWriter(s, false, 1); // Indent level == 1
    linesep = System.getProperty("line.separator").toString();
}

public void testXML_1() {
    Color c = new Color(12, 34, 56);
    out.xmlDecl("UTF-8");
    out.startElement("foo");
    out.startElement("bar");
    out.endElement();
    out.startElement("bletch");
    out.attr("color", c);
    out.endElement();
    out.endElement();
    out.close();

    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + linesep
		 + "<foo>" + linesep
		 + " <bar/>" + linesep
		 + " <bletch color=\"" + c.getRed() + ';' + c.getGreen()
		 + ';' + c.getBlue() + ';' + c.getAlpha() + "\"/>" + linesep
		 + "</foo>" + linesep,
		 s.toString());
}

public void testXML_2() {
    out.xmlDecl("UTF-8");
    out.startElement("foo");
    out.attr("a1", 1);
    out.attr("a2", 3.5);
    out.attr("a3", 'x');
    out.startElement("bar");
    out.endElement();
    out.textElement("text", "contents");
    out.cdataElement("cdata", "cdata contents");
    out.startElement("bletch");
    out.endElement();
    out.endElement();
    out.close();

    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + linesep
		 + "<foo a1=\"1\" a2=\"3.5\" a3=\"x\">" + linesep
		 + " <bar/>" + linesep
		 + " <text>contents</text>" + linesep
		 + " <cdata><![CDATA[cdata contents]]></cdata>" + linesep
		 + " <bletch/>" + linesep
		 + "</foo>" + linesep,
		 s.toString());
}

public void testReportWrite() {
    Report report = null;
    try {
	report = new Report();
	report.setDataSource(new MockDataSource(report));
	report.read(EXAMPLE_REPORT); // Must come after setting password
	report.setParameterXMLInput(PARAM_INPUT_FILE);

	File f = File.createTempFile("xml-writer-test", ".xml");
	f.deleteOnExit();
	report.writeFile(f.getPath());
	report.read(f);
	f.delete();
    }
    catch (Exception e) {
	fail(e.toString());
	e.printStackTrace();
    }
}

public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
}

}
