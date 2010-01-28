package jimm.datavision.layout.swing;
import jimm.datavision.field.ImageField;
import javax.swing.*;

/**
 * An image.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see SwingLE
 * @see jimm.datavision.field.ImageField
 */
public class SwingImageField extends AbstractSwingField {

/**
 * Constructor.
 *
 * @param f image field
 */
public SwingImageField(ImageField f) {
    super(f, new JLabel());
    format();
}

/**
 * Loads the image.
 */
public void format() {
    ((JLabel)component).setIcon(((ImageField)field).getImageIcon());
    makeBorders();
}

}
