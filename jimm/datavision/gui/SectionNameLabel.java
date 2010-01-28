package jimm.datavision.gui;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * A section name label displays the name of a section, for example "Report
 * Header (a)". A popup menu holds command affecting the section widget
 * with which this label is associated.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class SectionNameLabel extends JLabel {

protected static Font DEFAULT_FONT = new Font("Serif", Font.PLAIN, 10);

protected SectionWidget sectionWidget;

/**
 * An inner class that handles display of the popup menu.
 */
class PopupListener extends MouseAdapter {
public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
private void maybeShowPopup(MouseEvent e) {
    if (sectionWidget.designer.isPlacingNewTextField())
	sectionWidget.designer.rejectNewTextField();
    else if (e.isPopupTrigger())
	sectionWidget.showPopup(e);
}
}

/**
 * Constructor.
 *
 * @param name the section's name
 * @param sw the section widget we are labeling and controlling via a popup
 * menu
 */
SectionNameLabel(String name, SectionWidget sw) {
    super(name);
    sectionWidget = sw;
    setHorizontalAlignment(JLabel.CENTER);
    setVerticalAlignment(JLabel.TOP);
    setFont(DEFAULT_FONT);
    setForeground(Color.black);

    addMouseListener(new PopupListener());
}

}
