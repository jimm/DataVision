package jimm.datavision;
import jimm.util.I18N;
import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A parameter reader reads an XML file and sets a {@link Report}'s parameter
 * values. This class is used when the report is being run from the
 * command line and the user has given us the name of an XML file containing
 * parameter elements.
 * <p>
 * Unlike a {@link ReportReader}, a parameter reader's constructor
 * takes not only the report but also the input method (file name, stream,
 * or reader). That way the report object doesn't have to know how to hold
 * on to those multipule input types.
 * 
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ParameterReader extends DefaultHandler {

/**
 * If there is no report element dtd-version attribute, this is the
 * default value to use.
 */
protected static final double DEFAULT_DTD_VERSION = 0.1;

protected Report report;
protected Parameter parameter;
protected String textData;
protected File inFile;
protected InputSource inInputSource;

/**
 * Constructor.
 *
 * @param report the report whose parameters we are setting
 * @param f the parameter XML file
 */
public ParameterReader(Report report, File f) {
    this.report = report;
    inFile = f;
}

/**
 * Constructor. To specify a URL, use <code>new
 * InputSource("http://...")</code>.
 *
 * @param report the report whose parameters we are setting
 * @param in the param XML input source
 */
public ParameterReader(Report report, InputSource in) {
    this.report = report;
    inInputSource = in;
}

/**
 * Returns the file name or, if that is <code>null</code>, the class name of
 * whatever input source was handed to a constructor.
 *
 * @return a file name or class name
 */
public String getInputName() {
    if (inFile != null)
	return inFile.getPath();
    if (inInputSource != null)
	return "org.xml.sax.InputSource";
    return "?";
}

/**
 * Reads parameter values from whichever input method was specified
 * in the constructor.
 */
public void read() throws Exception {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    if (inFile != null)
	parser.parse(inFile, this);
    else if (inInputSource != null)
	parser.parse(inInputSource, this);
}

public void startElement(final String namespaceURI, final String localName,
			 final String qName, final Attributes attributes)
    throws SAXException
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    // Get ready to start collecting text
    if (textData == null || textData.length() > 0)
	textData = new String();

    if ("parameter".equals(tagName)) {
	String id = attributes.getValue("id");
	parameter = report.findParameter(id);
	if (parameter == null)
	    ErrorHandler.error(I18N.get("ParameterReader.unknown_id") + ' '
			       + id + ' '
			       + I18N.get("ParameterReader.in_xml"));
    }
}

public void endElement(final String namespaceURI, final String localName,
		       final String qName)
    throws SAXException
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    if ("value".equals(tagName) && parameter != null)
	parameter.addValue(textData);
    else if ("parameter".equals(tagName))
	parameter = null;
}

/**
 * Reads text data. Text data inside a single tag can be broken up into
 * multiple calls to this method.
 */
public void characters(char ch[], int start, int length) {
    textData += new String(ch, start, length);
}

}
