package jimm.datavision.gui.cmd;
import jimm.datavision.Point;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.field.ImageField;
import jimm.datavision.ErrorHandler;
import jimm.datavision.gui.SectionWidget;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.ImageFieldWidget;
import jimm.util.I18N;
import javax.swing.ImageIcon;

/**
 * Inserts a new image field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class NewImageFieldCommand extends InsertFieldCommand {

protected String imageURL;

public NewImageFieldCommand(SectionWidget sw, String url) {
    super(sw, "image", new Point(0, 0));
    imageURL = url;
}

public void perform() {
    super.perform();

    if (!((ImageField)fw.getField()).canLoad()) {
	ErrorHandler.error(I18N.get("DesignWin.image_load_err_1")
			   + ' ' + imageURL + ' '
			   + I18N.get("DesignWin.image_load_err_2"),
			   I18N.get("DesignWin.image_load_err_title"));
    }

}

protected Rectangle initialFieldBounds() {
    ImageIcon imageIcon = ((ImageField)fw.getField()).getImageIcon();
    return new Rectangle(insertLoc.x, insertLoc.y, imageIcon.getIconWidth(),
			 imageIcon.getIconHeight());
}

protected Object initialFieldValue() {
    return imageURL;
}

protected FieldWidget createFieldWidget(Field f) {
    return new ImageFieldWidget(null, (ImageField)f);
}

}
