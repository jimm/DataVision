package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Group;
import jimm.datavision.gui.FieldWidget;
import jimm.util.I18N;

/**
 * A command for adding a aggregate to a field for a particular section.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NewAggregateCommand extends AbstractAggregateCommand {

/**
 * Constructor.
 *
 * @param report the report containing the field and the aggregate
 * @param fw the field widget to which we are adding a aggregate
 * @param group if <code>null</code>, the aggregate is added to the
 * report footer; else the aggregate is added to the first section in
 * the group's footer.
 * @param functionName the aggregate function name
 */
public NewAggregateCommand(Report report, FieldWidget fw, Group group,
			 String functionName) {
    super(report, fw, group, functionName, I18N.get("NewAggregateCommand.name"));
    fieldWidget = fw;
    this.group = group;
}

public void perform() {
    createAggregate();
}

public void undo() {
    deleteAggregate();
}

}
