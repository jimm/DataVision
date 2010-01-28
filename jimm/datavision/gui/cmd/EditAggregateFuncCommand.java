package jimm.datavision.gui.cmd;
import jimm.datavision.field.AggregateField;
import jimm.util.I18N;

/**
 * A command for changing an aggregate field's function.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class EditAggregateFuncCommand extends CommandAdapter {

protected AggregateField aggregateField;
protected String newFunctionName;
protected String oldFunctionName;

public EditAggregateFuncCommand(AggregateField f, String functionName) {
    super(I18N.get("EditAggregateFuncCommand.name"));
    aggregateField = f;
    newFunctionName = functionName;
    oldFunctionName = f.getFunction();
}

public void perform() {
    aggregateField.setFunction(newFunctionName);
}

public void undo() {
    aggregateField.setFunction(oldFunctionName);
}
}