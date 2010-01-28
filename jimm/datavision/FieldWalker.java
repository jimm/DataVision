package jimm.datavision;
import jimm.datavision.field.Field;

/**
 * The field walker interface is used by those wishing to perform an action
 * on every field in a report. It is used as an argument to
 * <code>Report.withFieldsDo</code>. Typical use:
 *
 * <pre><code>
 *    report.withFieldsDo(new FieldWalker() {
 *        public void step(Field f) {
 *            // Do something with the field
 *        }
 *    });
 * </code></pre>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface FieldWalker {

/**
 * This method is called once for each field, when used from within
 * <code>Report.withFieldsDo</code>.
 *
 * @param f a field
 */
public void step(Field f);

}
