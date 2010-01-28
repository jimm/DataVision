package jimm.datavision.gui;

/**
 * The field widget walker interface is used by those wishing to perform an
 * action on every field widget in a report design window. It is used as an
 * argument to <code>Designer.withWidgetsDo</code>. Typical use:
 *
 * <pre><code>
 *    designer.withWidgetsDo(new FieldWidgetWalker() {
 *        public void step(FieldWidget f) {
 *            // Do something with the field
 *        }
 *    });
 * </code></pre>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface FieldWidgetWalker {

/**
 * This method is called once for each field, when used from within
 * <code>designer.withWidgetsDo</code>.
 *
 * @param f a field widget
 */
public void step(FieldWidget f);

}
