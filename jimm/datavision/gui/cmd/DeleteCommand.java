package jimm.datavision.gui.cmd;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.util.I18N;
import java.util.*;

/**
 * Deletes a list of field widgets.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DeleteCommand extends CommandAdapter {

protected Designer designer;
protected Collection fieldWidgets;

/**
 * Constructor.
 */
public DeleteCommand(Designer designer, ArrayList selectedFields) {
    this(designer, selectedFields, I18N.get("DeleteCommand.name"));
}

/**
 * The delegated constructor.
 */
protected DeleteCommand(Designer designer, ArrayList selectedFields,
			String name)
{
    super(name);
    this.designer = designer;
    fieldWidgets = (ArrayList)selectedFields.clone();
}

public void perform() {
    HashSet affectedSections = new HashSet();
    designer.deselectAll();

    for (Iterator iter = fieldWidgets.iterator(); iter.hasNext(); ) {
	FieldWidget fw = (FieldWidget)iter.next();
	fw.doDelete();		// Widget deletes itself and field from report
	affectedSections.add(fw.getSectionWidget());
    }

    for (Iterator iter = affectedSections.iterator(); iter.hasNext(); )
	((SectionWidget)iter.next()).repaint();

    designer.enableMenuItems();
}

public void undo() {
    for (Iterator iter = fieldWidgets.iterator(); iter.hasNext(); ) {
	FieldWidget fw = (FieldWidget)iter.next();
	fw.moveToSection(fw.getSectionWidget());
	designer.select(fw, true, false);
    }
    // Don't need to call Designer.enableMenuItems because each call
    // to Designer.select already does that.
}

}
