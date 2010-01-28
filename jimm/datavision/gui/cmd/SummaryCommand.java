package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.util.I18N;

/**
 * A command for changing a field's summary.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SummaryCommand extends CommandAdapter {

protected Report report;
protected String origName;
protected String origTitle;
protected String origAuthor;
protected String origDescription;
protected String newName;
protected String newTitle;
protected String newAuthor;
protected String newDescription;

public SummaryCommand(Report report, String newName, String newTitle,
		      String newAuthor, String newDescription)
{
    super(I18N.get("SummaryCommand.name"));
    this.report = report;
    this.newName = newName;
    this.newTitle = newTitle;
    this.newAuthor = newAuthor;
    this.newDescription = newDescription;

    origName = report.getName();
    origTitle = report.getTitle();
    origAuthor = report.getAuthor();
    origDescription = report.getDescription();
}

public void perform() {
    report.setName(newName);
    report.setTitle(newTitle);
    report.setAuthor(newAuthor);
    report.setDescription(newDescription);
}

public void undo() {
    report.setName(origName);
    report.setTitle(origTitle);
    report.setAuthor(origAuthor);
    report.setDescription(origDescription);
}

}
