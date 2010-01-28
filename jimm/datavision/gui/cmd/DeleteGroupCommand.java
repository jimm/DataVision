package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Group;
import jimm.datavision.gui.Designer;
import jimm.util.I18N;

public class DeleteGroupCommand extends CommandAdapter {

protected Designer designer;
protected Report report;
protected Group group;

/**
 * Constructor.
 */
public DeleteGroupCommand(Designer designer, Report report, Group group) {
    super(I18N.get("DeleteGroupCommand.name"));

    this.designer = designer;
    this.report = report;
    this.group = group;
}

public void perform() {
    report.removeGroup(group);
    designer.rebuildGroups();
}

public void undo() {
    report.addGroup(group);
    designer.rebuildGroups();
}

}
