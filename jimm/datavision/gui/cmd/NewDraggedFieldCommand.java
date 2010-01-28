package jimm.datavision.gui.cmd;
import jimm.datavision.Section;
import jimm.datavision.Point;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.field.ColumnField;
import jimm.datavision.source.Column;
import jimm.datavision.gui.SectionWidget;
import jimm.datavision.gui.FieldWidget;
import java.awt.dnd.DropTargetDropEvent;

/**
 * Inserts a new text field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NewDraggedFieldCommand extends InsertFieldCommand {

protected FieldWidget titleField;
protected SectionResizeCommand titleSectionResizeCommand;

public NewDraggedFieldCommand(SectionWidget sw, String dropString,
			      DropTargetDropEvent e)
{
    super(sw, dropString, new Point(e.getLocation()));
}

public void perform() {
    super.perform();

    // If this is a detail section and the field represents a database
    // column, add a title field whose name is the name of the column.
    SectionWidget sw = fw.getSectionWidget();
    Field f = fw.getField();
    if (sw.getSection().isDetail() && (f instanceof ColumnField)) {
	Column col = (Column)((ColumnField)f).getColumn();
	String name = col.getName();
	name = name.substring(0, 1).toUpperCase()
	    + name.substring(1).toLowerCase();

	// The title section resize command may not be used.
	Section titleSection = f.getReport().pageHeaders().first();
	SectionWidget titleSectionWidget =
	    sw.getDesigner().findSectionWidgetFor(titleSection);
	titleSectionResizeCommand =
	    new SectionResizeCommand(titleSectionWidget);

	// Possible the title field. If no title field added, titleField
	// will be null.
	titleField = sw.addTitleField((int)insertLoc.getX(),
				      (int)Field.DEFAULT_WIDTH, name);

	if (titleField != null)
	    titleSectionResizeCommand.perform();
	else
	    titleSectionResizeCommand = null;
    }
}

public void undo() {
    super.undo();
    if (titleField != null) {
	titleField.doDelete();
	titleSectionResizeCommand.undo();
	titleField.getSectionWidget().repaint();
    }
}

public void redo() {
    super.redo();
    if (titleField != null)
	titleField.moveToSection(titleField.getSectionWidget());
}

/**
 * Creates the field. This override creates a dragged field.
 */
protected Field createField() {
    // Create field. Specify null section so the call to
    // FieldWidget.moveToSection() will do the correct thing.
    //
    // field type string is drop string
    return Field.createFromDragString(sw.getReport(), fieldType);
}

protected Rectangle initialFieldBounds() {
    return new Rectangle(insertLoc.getX(), insertLoc.getY(),
			 Field.DEFAULT_WIDTH, Field.DEFAULT_HEIGHT);
}

protected Object initialFieldValue() {
    return null;
}

protected FieldWidget createFieldWidget(Field f) {
    return f.makeWidget(null);
}

}
