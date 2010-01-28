package jimm.datavision;
import jimm.util.XMLWriter;

/**
 * Identifies objects that implement the <code>writeXML</code> method.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface Writeable {

/**
 * Writes this object as an XML tag.
 *
 * @param out a writer that knows how to write XML
 */
public void writeXML(XMLWriter out);

}
