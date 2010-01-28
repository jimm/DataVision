package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.PaperFormat;
import jimm.datavision.gui.Designer;
import jimm.util.I18N;

/**
 * Change a report's paper size.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class PaperSizeCommand extends CommandAdapter {

Report report;
Designer designer;
PaperFormat origFormat;
PaperFormat newFormat;

public PaperSizeCommand(Report r, Designer win, PaperFormat p) {
    super(I18N.get("PaperSizeCommand.name"));
    report = r;
    designer = win;
    origFormat = report.getPaperFormat();
    newFormat = p;
}

public void perform() {
    report.setPaperFormat(newFormat);
    designer.paperSizeChanged(newFormat);
    designer.invalidate();
}

public void undo() {
    report.setPaperFormat(origFormat);
    designer.paperSizeChanged(origFormat);
    designer.invalidate();
}

}
