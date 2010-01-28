package jimm.datavision.source;
import jimm.datavision.source.Column;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;

/**
 * Reads metadata from XML, creates columns, and hands them to a data source.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see DataSource
 */
class MetadataReader extends DefaultHandler {

protected DataSource source;

MetadataReader(DataSource source) {
    this.source = source;
}

public void read(InputSource inputSource) throws Exception {
    SAXParserFactory.newInstance().newSAXParser().parse(inputSource, this);
}

public void startElement(final String namespaceURI, final String localName,
			 final String qName, final Attributes attributes)
    throws SAXException
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    if ("column".equals(tagName)) {
	String colName = attributes.getValue("name");
	int type = Column.typeFromString(attributes.getValue("type"));
	Column col = new Column(colName, colName, type);
	col.setDateParseFormat(attributes.getValue("date-format"));

	source.addColumn(col);
    }
}

}
