package jimm.datavision.gui;
import java.awt.*;

/**
 * A layout manager for {@link SectionWidget}s' contents.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class SectionLayout implements LayoutManager {

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
    return parent.getPreferredSize();
}

/**
 * Lays out the container in the specified panel.
 *
 * @param parent the component which needs to be laid out
 */
public void layoutContainer(Container parent) {
    SectionWidget sw = (SectionWidget)parent;
    int height = sw.getSectionHeight();
    Component[] components = parent.getComponents();

    // LHS name label
    components[0].setBounds(0, 0, SectionWidget.LHS_WIDTH, height);

    // Fields
    components[1].setBounds(SectionWidget.LHS_WIDTH, 0, sw.getPaperWidth(),
			    height);

    // Resizer dragger bar
    components[2].setBounds(0, height, sw.getTotalWidth(),
			    SectionResizer.HEIGHT);
}

}
