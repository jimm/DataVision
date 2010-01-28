package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.gui.cmd.Command;
import jimm.datavision.gui.cmd.SectionResizeCommand;
import jimm.datavision.gui.cmd.SectionPageBreakCommand;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A section widget is the visual representation of a report section.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SectionWidget extends JPanel implements ActionListener {

public static final int LHS_WIDTH = 125;

protected static final Font DEFAULT_POPUP_FONT =
    new Font("Serif", Font.PLAIN, 10);

static final Color NORMAL_COLOR = Color.white;
static final Color SUPPRESSED_COLOR = Color.lightGray;

protected String name;
protected String popupName;
protected Designer designer;
protected Section section;
protected SectionNameLabel label;
protected SectionFieldPanel fieldPanel;
protected JPopupMenu popup;
protected JMenuItem nameItem, editSuppress;
protected JCheckBoxMenuItem togglePageBreak;
protected JMenuItem deleteGroup, addGroup, deleteSection, insertSection;

/**
 * An inner class that handles display of the popup menu.
 */
class PopupListener extends MouseAdapter {
public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
private void maybeShowPopup(MouseEvent e) {
    if (e.isPopupTrigger())
	showPopup(e);
}
}

/**
 * Constructor.
 *
 * @param win parent design window
 * @param sect the report section
 * @param name a name such as "Report Header (a)"
 */
public SectionWidget(Designer win, Section sect, String name) {
    super();

    designer = win;
    section = sect;
    this.name = name;
    this.popupName = "";

    setLayout(new SectionLayout());
    setPreferredSize(new Dimension(getTotalWidth(), getHeight()));

    buildPopupMenu();
    addMouseListener(new PopupListener());

    // LHS name of section
    buildDisplayName();

    // Fields
    fieldPanel = new SectionFieldPanel(this);
    fieldPanel.setLayout(null);
    add(fieldPanel);
    for (Iterator iter = section.fields(); iter.hasNext(); ) {
	Field f = (Field)iter.next();
	FieldWidget fw = f.makeWidget(this);
	fieldPanel.add(fw.getComponent(), 0);	// Add to top of visual stack.
    }

    // Let field panel set background color of itself and fields based
    // on "always hide" suppression.
    fieldPanel.setHidden(section.isHidden());

    // If design window has initiated the drop of a new text field, create
    // one. Else deselect all fields.
    fieldPanel.addMouseListener(new PopupListener() {
	public void mouseClicked(MouseEvent e) {
	    deselectAll();
	    if (designer.isPlacingNewTextField())
		createNewTextField(e); // Calls acceptNewTextField()
	}
	});

    // Resizer bar
    add(new SectionResizer(this, designer.sectionContainer));
}

/**
 * Builds the popup menu.
 */
protected void buildPopupMenu() {
    popup = new JPopupMenu(popupName);

    nameItem =
	MenuUtils.addToMenu(this, popup, "SectionWidget.popup_dummy_title",
			    DEFAULT_POPUP_FONT);
    popup.addSeparator();
    editSuppress =
	MenuUtils.addToMenu(this, popup, "SectionWidget.popup_suppress",
			    DEFAULT_POPUP_FONT);
    togglePageBreak =
	MenuUtils.addCheckboxToMenu(this, popup,
				    "SectionWidget.popup_page_break",
				    DEFAULT_POPUP_FONT);
    popup.addSeparator();
    MenuUtils.addToMenu(this, popup, "SectionWidget.popup_shrink",
			DEFAULT_POPUP_FONT);
    popup.addSeparator();
    deleteGroup =
	MenuUtils.addToMenu(this, popup, "SectionWidget.popup_delete_group",
			    DEFAULT_POPUP_FONT);
    addGroup =
	MenuUtils.addToMenu(this, popup, "SectionWidget.popup_add_group",
			    DEFAULT_POPUP_FONT);
    popup.addSeparator();
    deleteSection =
	MenuUtils.addToMenu(this, popup, "SectionWidget.popup_delete_section",
			    DEFAULT_POPUP_FONT);
    insertSection =
	MenuUtils.addToMenu(this, popup, "SectionWidget.popup_insert_section",
			    DEFAULT_POPUP_FONT);
}

/**
 * Performs some action based on the action command string (the menu
 * item text).
 */
public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command == null) return;
    if ("suppress".equals(command))
	editSuppression();
    else if ("page_break".equals(command))
	togglePageBreak();
    else if ("delete_group".equals(command))
	designer.deleteGroupContaining(section);
    else if ("add_group".equals(command))
	designer.openNewGroupWin();
    else if ("delete_section".equals(command))
	designer.deleteSection(section);
    else if ("insert_section_below".equals(command))
	designer.insertSectionBelow(section);
    else if ("shrink_to_fit".equals(command))
	shrinkToFit();
}

/**
 * Modifies menu items based on the state of the section.
 */
protected void enablePopupMenuItems() {
    nameItem.setText(popupName);
    togglePageBreak.setSelected(section.hasPageBreak());
    deleteSection.setEnabled(!section.getReport().isOneOfAKind(section));
    deleteGroup.setEnabled(section.getReport().isInsideGroup(section));
}

/**
 * Constructs the section name widget that is displayed to the left of
 * the section.
 */
protected void buildDisplayName() {
    label = new SectionNameLabel(name, this);
    add(label);
}

/**
 * Set the section display name.
 *
 * @param name the new name
 */
public void setDisplayName(String name) {
    this.name = name;
    label.setText(name);
}

/**
 * Set the popup menu name, also displayed as first, disabled menu item.
 *
 * @param popupName the new name
 */
public void setPopupName(String popupName) {
    this.popupName = popupName;
    nameItem.setText(popupName);
}

/**
 * Returns the report we are representing.
 *
 * @return a report
 */
public Report getReport() { return section.getReport(); }

/**
 * Returns the section we are representing.
 *
 * @return a report section
 */
public Section getSection() { return section; }

/**
 * Returns the {@link SectionArea} of the {@link Section} (report header, page
 * footer, etc.)
 *
 * @return the section's <code>SectionArea</code>
 */
public SectionArea getSectionArea() { return section.getArea(); }

/**
 * Returns the design window containing this section widget
 *
 * @return a design window
 */
public Designer getDesigner() { return designer; }

/**
 * Returns the width of the report paper (the white part upon which fields
 * are placed).
 *
 * @return the paper width
 */
public int getPaperWidth() {
    return (int)section.getWidth();
}

/**
 * Returns the width of the section, including the left-hand side name.
 *
 * @return the total width
 */
public int getTotalWidth() {
    return LHS_WIDTH + getPaperWidth();
}

/**
 * Returns the height of the section, including the resizer bar.
 *
 * @return the total height
 */
public int getHeight() {
    return (int)section.getMinHeight() + SectionResizer.HEIGHT;
}

/**
 * Returns the height of the report section.
 *
 * @return report section height
 */
public int getSectionHeight() { return (int)section.getMinHeight(); }

/**
 * Returns the minimum height the section needs to fit all of its fields.
 *
 * @return minimum height
 */
public int getMinSectionHeight() {
    int minY = 0;
    Component[] fieldWidgets = fieldPanel.getComponents();
    for (int i = 0; i < fieldWidgets.length; ++i) {
	int y = fieldWidgets[i].getBounds().y
	    + fieldWidgets[i].getBounds().height;
	if (y > minY)
	    minY = y;
    }
    return minY;
}

/**
 * Resizes this widget. Called by the design window whenever the user
 * selects a new paper size.
 */
void paperSizeChanged() {
    setPreferredSize(new Dimension(getTotalWidth(), getHeight()));
    invalidate();
}

/**
 * Toggles the suppressed flag of the section.
 */
void editSuppression() {
    new SuppressionProcWin(designer, this);
}

/**
 * Toggles the page break flag of the section.
 */
void togglePageBreak() {
    performCommand(new SectionPageBreakCommand(section));
}

/**
 * Shrinks this section widget to the minimum height required. This method
 * is only called from the popup menu. It should not be called as part
 * of a larger operation because it creates a command that allows undo/redo.
 */
public void shrinkToFit() {
    SectionResizeCommand cmd = new SectionResizeCommand(this);
    growBy(getMinSectionHeight() - getSectionHeight());
    performCommand(cmd);
}

/**
 * Grows this section widget to the minimum height required. This method,
 * unlike <code>shrinkToFit</code>, is always called as part of some other
 * operation.
 */
public void growToFit() {
    int dy = getMinSectionHeight() - getSectionHeight();

    if (dy > 0)
	growBy(dy);
}

/**
 * Resizes the section. Called by resizer bar and by commands that
 * grow sections as a side effect.
 *
 * @param dy delta y
 */
public void growBy(int dy) {
    if (dy == 0) return;

    // Make sure section fits all fields.
    int origHeight = (int)section.getMinHeight();
    int newHeight = origHeight + dy;
    int minHeight = getMinSectionHeight();
    if (newHeight < minHeight) {
	newHeight = minHeight;
	if (newHeight == origHeight)
	    return;
    }

    section.setMinHeight(newHeight); // Hight not including resizer
    setPreferredSize(new Dimension(getTotalWidth(), getHeight())); // Incl. resizer
    revalidate();
}

/**
 * Grows or shrinks the section widget and executes a command that allows
 * this action to be undone/redone. Calls {@link #growBy} to grow
 * or shrink, then lets our window execute the command that remembers
 * the size change for later undo/redo.
 * <p>
 * The command does not change our height. It remembers the old and new
 * heights for later undo/redo.
 *
 * @param dy delta height
 * @param cmd a section resize command
 * @see #performCommand
 */
public void resizeBy(int dy, SectionResizeCommand cmd) {
    growBy(dy);
    performCommand(cmd);
}

/**
 * Passes a command up to the design window for execution.
 *
 * @param cmd a command
 * @see Designer#performCommand
 */
public void performCommand(Command cmd) {
    designer.performCommand(cmd);
}

/**
 * Passes responsiblity up to the design window.
 * @see Designer#setIgnoreKeys
 */
public void setIgnoreKeys(boolean ignoreKeys) {
    designer.setIgnoreKeys(ignoreKeys);
}

/**
 * Passes this request up to the design window.
 *
 * @param x where to place the title
 * @param width how wide it should be
 * @param title the string to display
 * @return the newly-created widget
 * @see Designer#addTitleField
 */
public FieldWidget addTitleField(int x, int width, String title) {
    return designer.addTitleField(x, width, title);
}

/**
 * Passes on to the design window the request to pick up all selected fields
 * for dragging (not just the specified field). Called from {@link
 * FieldWidget#mousePressed}.
 *
 * @param mouseScreenPos the location of the mouse in screen coordinates
 * @see Designer#pickUp
 */
void pickUp(java.awt.Point mouseScreenPos) {
    designer.pickUp(mouseScreenPos);
}

/**
 * Passes on to the design window the request to put down all fields being
 * dragged (not just the specified field). Called from {@link
 * FieldWidget#mouseReleased}.
 *
 * @param f the field in which the mouse has been clicked
 * @param origScreenPos the original location of the field in screen
 * coordinates
 * @param mouseScreenPos the location of the mouse in screen coordinates
 * @see Designer#putDown
 */
void putDown(FieldWidget f, java.awt.Point origScreenPos,
	     java.awt.Point mouseScreenPos)
{
    designer.putDown(f, origScreenPos, mouseScreenPos);
}

/**
 * Passes on to the design window the request to start stretching all selected
 * fields (not just the specified field). Called from {@link
 * FieldWidget#mousePressed}.
 *
 * @param mouseScreenPos the location of the mouse in screen coordinates
 * @see Designer#startStretching
 */
void startStretching(java.awt.Point mouseScreenPos) {
    designer.startStretching(mouseScreenPos);
}

/**
 * Passes on to the design window the request to stop stretching all fields
 * being stretched (not just the specified field). Called from {@link
 * FieldWidget#mouseReleased}.
 *
 * @param f the field in which the mouse has been clicked
 * @param origBounds the field's original bounds
 * @see Designer#putDown
 */
void stopStretching(FieldWidget f, jimm.datavision.field.Rectangle origBounds)
{
    designer.stopStretching(f, origBounds);
}

/**
 * Tells the window to drag (move, resize) all selected fields. Called
 * from one field widget when it's being manipulated with the mouse.
 *
 * @param action a <code>FieldWidget.ACTION_*</code> constant
 * @param mouseScreenPos the location of the mouse in screen coordinates
 * @see FieldWidget#mouseDragged
 * @see Designer#dragSelectedWidgets
 */
void dragSelectedWidgets(int action, java.awt.Point mouseScreenPos) {
    designer.dragSelectedWidgets(action, mouseScreenPos);
}

/**
 * Selects or deselcts a field widget, possibly deselecting all others
 * everywhere. Called from field widget itself, this passes the request
 * on to the design window.
 *
 * @param fieldWidget a field widget
 * @param makeSelected if <code>true</code>, select this field; else
 * deselect it
 * @param deselectOthers if <code>true</code>, all other fields in all
 * sections are deselected first
 * @see Designer#select
 */
void select(FieldWidget fieldWidget, boolean makeSelected,
	    boolean deselectOthers)
{
    designer.select(fieldWidget, makeSelected, deselectOthers);
}

/**
 * Deselects all fields in all sections. Tells the design window to do
 * so.
 *
 * @see Designer#deselectAll
 */
void deselectAll() {
    designer.deselectAll();
}

/**
 * Adds field widget to panel. Does not affect models. Field retains its
 * selection state.
 *
 * @param fw field widget to add
 */
public void addField(FieldWidget fw) {
    fieldPanel.add(fw.getComponent(), 0); // Add to top of visual stack.
    fw.sectionWidget = this;
    fw.getComponent().setBackground(section.isHidden()
				    ? SUPPRESSED_COLOR : NORMAL_COLOR);
    growToFit();
}

/**
 * Removes field widget from panel, but do not change field model's relation
 * with section model. Field retains its selection state.
 * <p>
 * To delete a field widget completely, see {@link
 * Designer#deleteSelectedFields}.
 *
 * @param fw field widget to remove
 * @see #addField
 */
public void removeField(FieldWidget fw) {
    fieldPanel.remove(fw.getComponent());
}

public SectionFieldPanel getFieldPanel() { return fieldPanel; }

/**
 * Asks the design window to snap the rectangle to it's grid.
 *
 * @param r a rectangle
 */
void snapToGrid(jimm.datavision.field.Rectangle r) {
    designer.snapToGrid(r);
}

/**
 * Sets the visibility of all selected fields plus the one passed in.
 * Passes the buck to the design window.
 *
 * @see FieldWidget#toggleVisibility
 * @see Designer#setFieldVisibility
 */
void setFieldVisibility(boolean newVisiblity, FieldWidget fw) {
    designer.setFieldVisibility(newVisiblity, fw);
}

/**
 * Asks design window to create and accepts a new text field.
 *
 * @see Designer#createNewTextField
 */
void createNewTextField(MouseEvent e) {
    designer.createNewTextField(this, e);
}

/**
 * Displays popup menu, after enabling and disabling menu items.
 *
 * @param e mouse event that caused popup to do its thing
 */
void showPopup(MouseEvent e) {
    enablePopupMenuItems();
    popup.show(e.getComponent(), e.getX(), e.getY());
}

}
