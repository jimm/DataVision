package jimm.datavision;

/**
 * The section walker interface is used by those wishing to perform an
 * action on every section in a report. It is used as an argument to
 * <code>Report.withSectionsDo</code>. Typical use:
 *
 * <pre><code>
 *    report.withSectionsDo(new SectionWalker() {
 *        public void step(Section s) {
 *            // Do something with the section
 *        }
 *    });
 * </code></pre>
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public interface SectionWalker {

/**
 * This method is called once for each section, when used from within
 * <code>Report.withSectionsDo</code>.
 *
 * @param s a section
 */
public void step(Section s);

}
