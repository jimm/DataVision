package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Group;
import jimm.datavision.gui.FieldWidget;
import jimm.util.I18N;

/**
 * A command for deleting a aggregate to a field from a particular section.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DeleteAggregateCommand extends AbstractAggregateCommand {

/**
 * Constructor.
 *
 * @param report the report containing the field and the aggregate
 * @param fw the field widget from which we are deleting the aggregate
 * @param aggregateWidget the aggregate's field widget
 * @param funcName current function of aggregate, in case we undo
 * @param group if <code>null</code>, the aggregate is deleted from the
 * report footer; else the aggregate is deleted from the group's footer.
 */
public DeleteAggregateCommand(Report report, FieldWidget fw,
			     FieldWidget aggregateWidget, String funcName,
			      Group group)
{
    super(report, fw, group, funcName,
	  I18N.get("DeleteAggregateCommand.name"));
    this.aggregateWidget = aggregateWidget;
}

public void perform() {
    deleteAggregate();
}

public void undo() {
    createAggregate();
}

}
