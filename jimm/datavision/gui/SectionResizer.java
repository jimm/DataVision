package jimm.datavision.gui;
import jimm.datavision.gui.cmd.SectionResizeCommand;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.BevelBorder;

/**
 * A section resizer is a bar that the user can drag to resize a section.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class SectionResizer extends JPanel implements MouseInputListener {

public static final int HEIGHT = 6;

protected static final Color BEVEL_HILIGHT = new Color(0xdd, 0xdd, 0xdd);
protected static final Color SPLITTER_COLOR = new Color(0x99, 0x99, 0x99);
protected static final Color BEVEL_SHADOW = new Color(0x66, 0x66, 0x66);
protected static final Color GHOST_COLOR =
    new Color(0x99, 0x99, 0xff, 0x80);
protected static boolean someoneDragging = false;

protected SectionWidget target;
protected Point start;
protected int minY;
protected Point mousePos;
protected JPanel dragGhost;
protected int localY;
protected JLayeredPane parentWhileDragging;
protected int parentWhileDraggingScreenY;
protected SectionResizeCommand sectionResizeCommand;

/**
 * Constructor.
 *
 * @param target the section widget we will be resizing
 * @param parentWhileDragging the widget that will be our parent while
 * we are being dragged
 */
SectionResizer(SectionWidget target, JLayeredPane parentWhileDragging) {
    super();
    this.target = target;
    this.parentWhileDragging = parentWhileDragging;
    setBackground(SPLITTER_COLOR);
    setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,
					      BEVEL_HILIGHT, BEVEL_SHADOW));
    addMouseListener(this);
    addMouseMotionListener(this);
}

/**
 * Handles mouse clicks. Nothing to do.
 *
 * @param e a mouse event
 */
public void mouseClicked(MouseEvent e) {}

/**
 * Handles mouse entered events by changing the cursor.
 *
 * @param e a mouse event
 */
public void mouseEntered(MouseEvent e) {
    if (!someoneDragging)
	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}

/**
 * Handles mouse exited events by changing the cursor.
 *
 * @param e a mouse event
 */
public void mouseExited(MouseEvent e) {
    if (!someoneDragging)
	setCursor(null);
}

/**
 * Handles mouse moved events. Nothing to do.
 *
 * @param e a mouse event
 */
public void mouseMoved(MouseEvent e) {}

/**
 * Handles mouse presses by starting to drag.
 *
 * @param e a mouse event
 */
public void mousePressed(MouseEvent e) {
    if (target.designer.isPlacingNewTextField()) {
	target.designer.rejectNewTextField();
	return;
    }

    // Set mousePos to screen position of click
    mousePos = e.getPoint();
    localY = mousePos.y;
    java.awt.Point screenPos = getLocationOnScreen();
    mousePos.translate(screenPos.x, screenPos.y);

    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    someoneDragging = true;

    start = new Point(mousePos);
    parentWhileDraggingScreenY =
	parentWhileDragging.getLocationOnScreen().y;
    minY = target.getLocationOnScreen().y - parentWhileDraggingScreenY
	+ target.getMinSectionHeight();

    dragGhost = new JPanel();
    dragGhost.setBackground(GHOST_COLOR);
    parentWhileDragging.add(dragGhost, JLayeredPane.DRAG_LAYER);
    dragGhost.setBounds(0, start.y - localY - parentWhileDraggingScreenY,
			parentWhileDragging.getWidth(), HEIGHT);

    sectionResizeCommand = new SectionResizeCommand(target);
}

/**
 * Handles mouse drag events by moving this resizer.
 *
 * @param e a mouse event
 */
public void mouseDragged(MouseEvent e) {
    // Set ePos to screen position of click
    java.awt.Point ePos = e.getPoint();
    java.awt.Point screenPos = getLocationOnScreen();
    ePos.translate(screenPos.x, screenPos.y);

    if (mousePos == null || ePos.y == mousePos.y)
	return;			// Nothing to do

    int newY = ePos.y - localY - parentWhileDraggingScreenY;
    // Make sure we don't get too small.
    if (newY < minY)
	newY = minY;

    dragGhost.setLocation(0, newY);
    mousePos = ePos;
}

/**
 * Handles mouse released events by repositioning self and asking the
 * target section widget to resize itself.
 *
 * @param e a mouse event
 */
public void mouseReleased(MouseEvent e) {
    someoneDragging = false;

    // Set ePos to screen position of click
    java.awt.Point ePos = e.getPoint();
    java.awt.Point screenPos = getLocationOnScreen();
    ePos.translate(screenPos.x, screenPos.y);

    parentWhileDragging.remove(dragGhost);

    // Tell the target to resize itself. Do not call SectionWidget.growBy()
    // directly. See SectionWidget.resizedBy().
    target.resizeBy(ePos.y - start.y - localY, sectionResizeCommand);

    // When dragging bottom-most section, the ghost is left behind in the
    // remaining gray section of the window. Force a repaint (sigh).
    parentWhileDragging.repaint();
}

}
