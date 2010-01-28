package jimm.datavision.gui;
import jimm.datavision.field.Rectangle;
import java.awt.Point;

/**
 * We save pre-move information because we need to know the original
 * mouse position, we may need to the field's original position, and
 * because we need to know our original position when finally moving.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class PreMoveInfo {

public Rectangle origBounds;
public Point startMouseScreenPos;
public Point screenPos;
public SectionWidget sectionWidget;

/**
 * Constructor.
 *
 * @param fw a field widget
 * @param mouseScreenPos the location of the mouse in screen coordinates
 */
PreMoveInfo(FieldWidget fw, Point mouseScreenPos) {
    origBounds = new Rectangle(fw.getField().getBounds());
    sectionWidget = fw.getSectionWidget();
    screenPos = fw.getComponent().getLocationOnScreen();
    startMouseScreenPos = mouseScreenPos;
}
}
