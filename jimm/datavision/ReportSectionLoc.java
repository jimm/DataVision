package jimm.datavision;

/**
 * Returned by a report when asked about a section's location within
 * the report. Handed back to the report when it is asked to re-insert
 * a section. Used by <code>DeleteSectionCommand</code>.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see jimm.datavision.gui.cmd.DeleteSectionCommand
 */
public class ReportSectionLoc {

Section section;
SectionArea area;
int index;

public ReportSectionLoc(Section s, SectionArea a, int i) {
    section = s;
    area = a;
    index = i;
}

}
