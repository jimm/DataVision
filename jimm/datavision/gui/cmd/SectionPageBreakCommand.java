package jimm.datavision.gui.cmd;
import jimm.datavision.Section;
import jimm.util.I18N;

/**
 * Toggles the state of a section's page break flag.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SectionPageBreakCommand extends CommandAdapter {

protected Section section;

public SectionPageBreakCommand(Section section) {
    super(I18N.get(section.hasPageBreak()
		   ? "SectionPageBreakCommand.off_name"
		   : "SectionPageBreakCommand.on_name"));
    this.section = section;
}

public void perform() {
    boolean newState = !section.hasPageBreak();
    section.setPageBreak(newState);
}

public void undo() {
    perform();
}

}
