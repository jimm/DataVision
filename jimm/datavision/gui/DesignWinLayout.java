package jimm.datavision.gui;
import java.awt.*;

/**
 * A custom layout manager for the section widgets inside a design window.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class DesignWinLayout implements LayoutManager {

/**
 * Adds the specified component with the specified name to the layout.
 *
 * @param name the component name
 * @param comp the component to be added
 */
public void addLayoutComponent(String name, Component comp) {
}

/**
 * Removes the specified component from the layout.
 *
 * @param comp the component to be removed
 */
public void removeLayoutComponent(Component comp) {
}

/**
 * Calculates the preferred size dimensions for the specified panel given
 * the components in the specified parent container.
 *
 * @param parent the component to be laid out
 * @see #minimumLayoutSize
 */
public Dimension preferredLayoutSize(Container parent) {
    return minimumLayoutSize(parent);
}

/**
 * Calculates the minimum size dimensions for the specified panel given the
 * components in the specified parent container.
 *
 * @param parent the component to be laid out
 * @see #preferredLayoutSize
 */
public Dimension minimumLayoutSize(Container parent) {
    int width = 0;
    int height = 0;
    Component[] components = parent.getComponents();
    for (int i = 0; i < components.length; ++i) {
	Component c = components[i];
	if (c instanceof SectionWidget) { // Ignore floating widgets
	    Dimension dim = c.getPreferredSize();
	    if (width == 0) width = dim.width;
	    height += dim.height;
	}
    }
    return new Dimension(width, height);
}

/**
 * Lays out the container in the specified panel.
 *
 * @param parent the component which needs to be laid out
 */
public void layoutContainer(Container parent) {
    int y = 0;
    Component[] components = parent.getComponents();
    for (int i = 0; i < components.length; ++i) {
	Component c = components[i];
	if (c instanceof SectionWidget) { // Ignore floating widgets
	    Dimension dim = c.getPreferredSize();
	    c.setBounds(0, y, dim.width, dim.height);
	    y += dim.height;
	}
    }
}

}
