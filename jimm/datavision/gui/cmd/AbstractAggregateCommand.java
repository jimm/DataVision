package jimm.datavision.gui.cmd;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;

/**
 * A command for adding a aggregate to a field for a particular section.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class AbstractAggregateCommand extends CommandAdapter {

protected Report report;
protected FieldWidget fieldWidget;
protected Group group;
protected FieldWidget aggregateWidget;
protected String functionName;

/**
 * Constructor.
 *
 * @param report the report containing the field and the aggregate
 * @param fw the field widget to which we are adding a aggregate
 * @param group if <code>null</code>, the aggregate is added to the
 * report footer; else the aggregate is added to the first section in
 * the group's footer.
 * @param commandName the command name
 */
public AbstractAggregateCommand(Report report, FieldWidget fw, Group group,
			      String functionName, String commandName)
{
    super(commandName);
    this.report = report;
    fieldWidget = fw;
    this.group = group;
    this.functionName = functionName;
}

public FieldWidget getAggregateWidget() { return aggregateWidget; }

protected void createAggregate() {
    // Find the proper footer. Don't worry; report and group footers always
    // have at least one section.
    Section s = (group == null) ? report.footers().first()
      : group.footers().first();

    Field originalField = fieldWidget.getField();
    Object id = originalField.getId();
//      if (originalField instanceof AggregateField)	// Can't be true
//  	id = ((AggregateField)originalField).getFieldId();

    // Create the aggregate field. Set the group (OK if null).
    AggregateField aggregate =
	(AggregateField)Field.create(null, report, s, functionName, id, true);
    aggregate.setGroup(group);
		
    // If we already have a aggregate widget (we already created one, or
    // this is a delete command and we are re-creating the deleted
    // aggregate), copy the format and bounds from that. Else, copy the
    // bounds and format from the field we are aggregating and make the
    // format bold.
    Format fmt = null;
    Rectangle bounds = null;
    if (aggregateWidget == null) {
	fmt = originalField.getFormat();
	fmt = (Format)fmt.clone();
	fmt.setBold(true);

	bounds = new Rectangle(originalField.getBounds());
    }
    else {
	fmt = aggregateWidget.getField().getFormat();
	fmt = (Format)fmt.clone();

	bounds = new Rectangle(aggregateWidget.getField().getBounds());
    }
    aggregate.setFormat(fmt);
    aggregate.setBounds(bounds);

    // Add the field to the section
    s.addField(aggregate);

    // Create the widget and add it to the proper section widget.
    SectionWidget sectionWidget =
	Designer.findWindowFor(report).findSectionWidgetFor(s);

    aggregateWidget = new FieldWidget(null, aggregate);
    sectionWidget.addField(aggregateWidget);

    // For some reason we need to force a repaint of the section. Can't
    // just call invalidate.
    sectionWidget.repaint();
}

protected void deleteAggregate() {
    java.awt.Component parent = aggregateWidget.getComponent().getParent();
    aggregateWidget.doDelete();
    parent.repaint();
}

}
