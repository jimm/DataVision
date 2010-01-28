package jimm.datavision.layout;
import jimm.datavision.Line;


/**
 * The line drawer interface is used by those wishing to draw all lines
 * in a border. Typical use:
 *
 * <pre><code>
 *    field.getBorderOrDefault().eachLine(fieldWalker);
 * </code></pre>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface LineDrawer {

/**
 * This method is called once for each line, when used from within
 * {@link jimm.datavision.field.Border#eachLine}.
 *
 * @param l a line
 * @param arg whatever you want it to be
 */
public void drawLine(Line l, Object arg);

}
