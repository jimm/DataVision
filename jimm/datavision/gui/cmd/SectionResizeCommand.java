package jimm.datavision.gui.cmd;
import jimm.datavision.gui.SectionWidget;
import jimm.util.I18N;

/**
 * Mainly used by other commands to remember a section's old size
 * and restore it on an undo.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SectionResizeCommand extends CommandAdapter {

protected SectionWidget sw;
protected int oldSectionHeight;
protected int sectionHeightDelta;

public SectionResizeCommand(SectionWidget sw)
{
    super(I18N.get("SectionResizeCommand.name"));
    this.sw = sw;
    oldSectionHeight = sw.getHeight();
}

public void perform() {
    sectionHeightDelta = sw.getHeight() - oldSectionHeight;
}

public void undo() {
    sw.growBy(-sectionHeightDelta);
}

public void redo() {
    sw.growBy(sectionHeightDelta);
}

}
