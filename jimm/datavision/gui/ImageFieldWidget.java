package jimm.datavision.gui;
import jimm.datavision.field.ImageField;
import jimm.datavision.layout.swing.SwingImageField;
import java.util.Observable;

public class ImageFieldWidget extends FieldWidget {

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param image a report image field
 */
public ImageFieldWidget(SectionWidget sw, ImageField image) {
    super(sw, new SwingImageField(image));
}

public void update(Observable obj, Object arg) {
    swingField.format();	// Redo image
    jimm.datavision.field.Rectangle b = getField().getBounds();
    getComponent().setBounds((int)b.x, (int)b.y, (int)b.width, (int)b.height);
}

public boolean usesFormat() {
    return false;
}

/**
 * Performs whatever is necessary to select or deselct self. Called by
 * {@link Designer#select}.
 *
 * @param makeSelected new selection state
 */
void doSelect(boolean makeSelected) {
    if (selected != makeSelected) {
	selected = makeSelected;
	getComponent().repaint(); // Reflect border changes
    }
}

}

