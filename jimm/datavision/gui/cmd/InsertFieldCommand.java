package jimm.datavision.gui.cmd;
import jimm.datavision.Point;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.util.I18N;

/**
 * Abstract superclass for inserting new fields.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
abstract public class InsertFieldCommand extends CommandAdapter {

protected FieldWidget fw;
protected SectionWidget sw;
protected Point insertLoc;
protected String fieldType;
protected SectionResizeCommand sectionResizeCommand;

public InsertFieldCommand(SectionWidget sw, String fieldType, Point insertLoc)
{
    super(I18N.get("InsertFieldCommand.name"));

    this.sw = sw;
    this.fieldType = fieldType;
    this.insertLoc = insertLoc;
    sectionResizeCommand = new SectionResizeCommand(sw);
}

public void perform() {
    if (fw == null) {
	Field f = createField();
	fw = createFieldWidget(f);
    }

    fw.getField().getBounds().setBounds(initialFieldBounds());

    fw.moveToSection(sw);
    sectionResizeCommand.perform();
}

public void undo() {
    sw.removeField(fw);
    sectionResizeCommand.undo();
    sw.repaint();
}

/**
 * Creates the field. This default behavior calls <code>Field.create</code>,
 * passing it a type string.
 */
protected Field createField() {
    // Create field. Specify null section so the call to
    // FieldWidget.moveToSection() will do the correct thing.
    return Field.create(null, sw.getReport(), null, fieldType,
			initialFieldValue(), true);
}

protected abstract Rectangle initialFieldBounds();

protected abstract Object initialFieldValue();

protected abstract FieldWidget createFieldWidget(Field f);

}
