package jimm.datavision;
import jimm.util.XMLWriter;
import java.util.*;

/**
 * Writes the element of a list of {@link Writeable} objects as XML.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ListWriter {

/**
 * Writes the elements of a list of Writeable objects as XML. Each object's
 * <code>writeXML</code> method is called.
 *
 * @param out the writer
 * @param list the collection of objects to write
 */
public static void writeList(XMLWriter out, Collection list) {
  writeList(out, list, null);
}

/**
 * Writes the elements of a list of Writeable objects as XML. An open tag is
 * written if <var>name</var> is not <code>null</code>, then each object's
 * <code>writeXML</code> method is called, then a closing tag is written if
 * needed. If the list is empty, nothing at all gets written.
 *
 * @param out the writer
 * @param list the collection of objects to write
 * @param name the XML tag name to use; if <code>null</code>, no begin/end
 * element is written
 */
public static void writeList(XMLWriter out, Collection list, String name) {
    if (list.isEmpty())
	return;

    if (name != null)
      out.startElement(name);
    for (Iterator iter = list.iterator(); iter.hasNext(); )
	((Writeable)iter.next()).writeXML(out);
    if (name != null)
      out.endElement();
}

}
