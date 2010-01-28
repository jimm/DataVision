package jimm.datavision.gui;
import jimm.datavision.Section;

/**
 * We save pre-stretch information so we have a place to hold information
 * like original mouse click position and minimum legal mouse position.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class PreStretchInfo {

public jimm.datavision.field.Rectangle origBounds;
public java.awt.Point startMouseScreenPos;
public java.awt.Rectangle sectionBounds;
public java.awt.Rectangle screenBounds;

/**
 * Constructor.
 *
 * @param fw a field widget
 * @param mouseScreenPos the location of the mouse in screen coordinates
 */
PreStretchInfo(FieldWidget fw, java.awt.Point mouseScreenPos) {
    jimm.datavision.field.Rectangle b = fw.getField().getBounds();

    // Copy of field's bounds
    origBounds =
	new jimm.datavision.field.Rectangle(b.x, b.y, b.width, b.height);

    // Section's bounding rectangle
    Section sect = fw.getSectionWidget().getSection();
    sectionBounds = new java.awt.Rectangle(0, 0, (int)sect.getWidth(),
					   (int)sect.getMinHeight());

    // Field's bounds in screen coordinates
    java.awt.Point screenPos = fw.getComponent().getLocationOnScreen();
    screenBounds = new java.awt.Rectangle(screenPos.x, screenPos.y,
					  (int)b.width, (int)b.height);

    startMouseScreenPos = mouseScreenPos;
}
}
