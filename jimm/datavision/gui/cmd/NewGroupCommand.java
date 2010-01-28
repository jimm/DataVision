package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Group;
import jimm.datavision.Selectable;
import jimm.datavision.gui.Designer;
import jimm.util.I18N;

public class NewGroupCommand extends CommandAdapter {

protected Designer designer;
protected Report report;
protected Group group;

public NewGroupCommand(Designer designer, Report report, Selectable selectable,
		       int sortOrder)
{
    super(I18N.get("NewGroupCommand.name"));

    this.designer = designer;
    this.report = report;
    group = Group.create(report, selectable);
    group.setSortOrder(sortOrder);
}

public void perform() {
    report.addGroup(group);
    designer.rebuildGroups();
}

public void undo() {
    report.removeGroup(group);
    designer.rebuildGroups();
}

}
