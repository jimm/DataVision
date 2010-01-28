package jimm.datavision.gui.cmd;
import jimm.datavision.Point;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.gui.SectionWidget;
import jimm.datavision.gui.SectionFieldPanel;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.TextFieldWidget;
import java.awt.event.MouseEvent;
import java.awt.Component;

/**
 * Inserts a new text field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NewTextFieldCommand extends InsertFieldCommand {

public NewTextFieldCommand(SectionWidget sw, MouseEvent e) {
    super(sw, "text", new Point(e.getPoint()));

    // Translate insertLoc's mouse coordinates to SectionFieldPanel
    // coordinates.
    Component c = e.getComponent();
    while (!(c instanceof SectionFieldPanel)) {
	java.awt.Rectangle bounds = c.getBounds();
	insertLoc.translate(bounds.x, bounds.y);
	c = c.getParent();
	if (c == null)		// Should never happen
	    break;
    }
}

public void perform() {
    super.perform();
    ((TextFieldWidget)fw).startEditing(); // Selects the widget, too
}

protected Rectangle initialFieldBounds() {
    return new Rectangle(insertLoc.getX(),
			 insertLoc.getY() - (int)(Field.DEFAULT_HEIGHT / 2),
			 (double)Field.DEFAULT_WIDTH,
			 (double)Field.DEFAULT_HEIGHT);
}

protected Object initialFieldValue() {
    return "";
}

protected FieldWidget createFieldWidget(Field f) {
    return new TextFieldWidget(null, f);
}

}
