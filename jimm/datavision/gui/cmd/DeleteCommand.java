package jimm.datavision.gui.cmd;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.util.I18N;
import java.util.*;

/**
 * Deletes a list of field widgets.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public class DeleteCommand extends CommandAdapter {

protected Designer designer;
protected Collection<FieldWidget> fieldWidgets;

/**
 * Constructor.
 */
public DeleteCommand(Designer designer, ArrayList<FieldWidget> selectedFields) {
    this(designer, selectedFields, I18N.get("DeleteCommand.name"));
}

/**
 * The delegated constructor.
 */
@SuppressWarnings("unchecked")
protected DeleteCommand(Designer designer, ArrayList<FieldWidget> selectedFields,
			String name)
{
    super(name);
    this.designer = designer;
    fieldWidgets = (ArrayList<FieldWidget>)selectedFields.clone();
}

public void perform() {
    HashSet<SectionWidget> affectedSections = new HashSet<SectionWidget>();
    designer.deselectAll();

    for (FieldWidget fw : fieldWidgets) {
	fw.doDelete();		// Widget deletes itself and field from report
	affectedSections.add(fw.getSectionWidget());
    }

    for (SectionWidget sw : affectedSections)
	sw.repaint();

    designer.enableMenuItems();
}

public void undo() {
    for (FieldWidget fw : fieldWidgets) {
	fw.moveToSection(fw.getSectionWidget());
	designer.select(fw, true, false);
    }
    // Don't need to call Designer.enableMenuItems because each call
    // to Designer.select already does that.
}

}
