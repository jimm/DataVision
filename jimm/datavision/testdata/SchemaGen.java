package jimm.datavision.testdata;
import java.io.File;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Skeleton for creating a schema.sql file by reading an XML description
 * of a schema.
 * 
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */

public abstract class SchemaGen extends DefaultHandler {

protected String type;
protected int size;
protected boolean notNull;
protected boolean primaryKey;
protected boolean isFirstColumn;

public void run(String schemaXMLFile) {
    try {
	SAXParserFactory.newInstance().newSAXParser()
	    .parse(new File(schemaXMLFile), this);
    }
    catch (Exception e) {
	e.printStackTrace();
	System.exit(1);
    }
}

public void startElement(final String namespaceURI, final String localName,
			 final String qName, final Attributes attributes)
    throws SAXException
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    if ("table".equals(tagName)) table(attributes);
    else if ("column".equals(tagName)) column(attributes);
}

public void endElement(final String namespaceURI, final String localName,
		       final String qName)
    throws SAXException
{
    String tagName = localName;
    if (tagName == null || tagName.length() == 0)
        tagName = qName;

    if ("table".equals(tagName)) endTable();
}

/**
 * Parses a table XML tag and calls <code>makeTable</code>.
 *
 * @param attributes XML element attributes
 * @see #makeTable
 */
protected void table(Attributes attributes) {
    makeTable(attributes.getValue("name"));
    isFirstColumn = true;
}

/**
 * Parses a column XML tag and calls <code>printColumn</code>. Also handles
 * commas and indentation.
 *
 * @param attributes XML element attributes
 * @see #makeTable
 */
protected void column(Attributes attributes) {
    String name = attributes.getValue("name");
    String type = attributes.getValue("type");
    String sizeStr = attributes.getValue("size");
    int size = 0;
    if (sizeStr != null)
	size = Integer.parseInt(sizeStr);
    boolean notNull = (attributes.getValue("not-null") != null);
    boolean primaryKey = (attributes.getValue("primary-key") != null);

    if (!isFirstColumn)
	System.out.print(",");
    System.out.println();
    System.out.print("\t");

    printColumn(name, type, size, notNull, primaryKey);
    isFirstColumn = false;
}

/**
 * Outputs the SQL needed to create a database table. Optionally prints
 * the SQL needed to destroy the table first.
 */
protected abstract void makeTable(String tableName);

protected String printableName(String name) {
    if (name.indexOf(' ') >= 0 || !name.equals(name.toLowerCase()))
	return "\"" + name + "\"";
    else
	return name;
}

/**
 * Outputs the SQL needed to close a create table statement.
 */
protected abstract void endTable();

/**
 * Prints the SQL needed to create a database column within a create table
 * statement.
 */
protected void printColumn(String columnName, String type, int size,
			   boolean notNull, boolean primaryKey)
{
    printColumnName(columnName);
    printType(type, size);
    if (notNull) printNotNull();
    if (primaryKey) printPrimaryKey();
}

/**
 * Prints the column name, taking into account case, blanks, and other
 * possibly funky things.
 */
protected void printColumnName(String columnName) {
    System.out.print(printableName(columnName));
}

/**
 * Prints the SQL that defines a column's type.
 */
protected abstract void printType(String type, int size);

/**
 * Prints the SQL that defines a column as NOT NULL.
 */
protected abstract void printNotNull();

/**
 * Prints the SQL that defines a column as a primary key.
 */
protected abstract void printPrimaryKey();

}
