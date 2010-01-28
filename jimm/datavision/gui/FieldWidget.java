package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.layout.swing.AbstractSwingField;
import jimm.datavision.layout.swing.SwingTextField;
import jimm.util.I18N;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.util.Observable;
import java.util.Observer;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

/**
 * A field widget is the visual representation of a text-based report field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldWidget
    implements MouseInputListener, DropTargetListener, ActionListener,
	       KeyListener, Observer
{

protected static final int GRAB_EDGE_WIDTH = 4;

protected static final int ACTION_INACTION = 0;
protected static final int ACTION_MOVE = 1;
protected static final int ACTION_STRETCH_LEFT = 2;
protected static final int ACTION_STRETCH_RIGHT = 3;
protected static final int ACTION_STRETCH_TOP = 4;
protected static final int ACTION_STRETCH_BOTTOM = 5;
protected static final int ACTION_POPPING_UP_MENU = 6;

/** Minimum field with and height. */
protected static final int MIN_SIZE = 2;

protected static final Font POPUP_FONT = new Font("Serif", Font.PLAIN, 10);

protected static HashMap componentMap = new HashMap();

protected AbstractSwingField swingField;
protected SectionWidget sectionWidget;
protected int action;
protected boolean actionStartedYet;
protected boolean selected;
protected boolean mouseChangedSelectedState;
protected PreMoveInfo preMoveInfo;
protected PreStretchInfo preStretchInfo;
protected JPopupMenu popup;
protected JMenu alignSubmenu;
protected JMenu sizeSubmenu;
protected JMenuItem nameItem;
protected JMenuItem showOrHide;
protected JMenuItem formatMenuItem;
protected JMenuItem aggregatesMenuItem;

/**
 * Returns the field widget that owns a particular visual component.
 *
 * @return a field widget or <code>null</code>
 */
static FieldWidget findFieldWidgetOwning(Object c) {
    return (FieldWidget)componentMap.get(c);
}

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param field a report field
 */
public FieldWidget(SectionWidget sw, Field field) {
    this(sw, new SwingTextField(field, field.designLabel()));
}

/**
 * Constructor.
 *
 * @param sw section widget in which the field's new widget will reside
 * @param asf an abstract swing field
 */
protected FieldWidget(SectionWidget sw, AbstractSwingField asf) {
    sectionWidget = sw;
    swingField = asf;
    action = ACTION_INACTION;

    jimm.datavision.field.Rectangle b = getField().getBounds();
    getComponent().setBounds((int)b.x, (int)b.y, (int)b.width, (int)b.height);

    getComponent().setBorder(new FWBorder(this));
    getComponent().addMouseListener(this);
    getComponent().addMouseMotionListener(this);
    getComponent().addKeyListener(this); // Frank W. Zammetti
    buildPopupMenu();

    // Allow drops. All we do is pass them on to our parent.
    new DropTarget(this.getComponent(),
		   DnDConstants.ACTION_COPY_OR_MOVE, // actions
		   this);	// DropTargetListener

    // Set color, etc. based on field's visiblity
    setVisibilityLook();

    // Start observing field changes
    getField().addObserver(this);

    // Add to the class's map so we can find this widget later, given
    // a GUI component.
    componentMap.put(getComponent(), this);
}

/**
 * Builds the popup menu.
 */
protected void buildPopupMenu() {
    popup = new JPopupMenu();
    popup.setFont(POPUP_FONT);

    nameItem = MenuUtils.addToMenu(this, popup,
				   "FieldWidget.popup_dummy_title",
				   POPUP_FONT);
    nameItem.setText(getPopupNameText()); // Overwrite dummy title
    nameItem.setEnabled(false);
    popup.addSeparator();

    addCustomPopupItems();

    showOrHide =
	MenuUtils.addToMenu(this, popup, "FieldWidget.popup_hide", POPUP_FONT);
    MenuUtils.addToMenu(this, popup, "FieldWidget.popup_delete", POPUP_FONT);
    popup.addSeparator();
    formatMenuItem =
	MenuUtils.addToMenu(this, popup, "FieldWidget.popup_format",
			    POPUP_FONT);
    MenuUtils.addToMenu(this, popup, "FieldWidget.popup_border", POPUP_FONT);
    MenuUtils.addToMenu(this, popup, "FieldWidget.popup_bounds", POPUP_FONT);
    popup.addSeparator();
    aggregatesMenuItem =
	MenuUtils.addToMenu(this, popup, "FieldWidget.popup_aggr",
			    POPUP_FONT);
    popup.addSeparator();

    alignSubmenu = MenuUtils.buildAlignMenu(this, POPUP_FONT);
    alignSubmenu.setFont(POPUP_FONT);
    popup.add(alignSubmenu);	// Add Align submenu to popup menu

    sizeSubmenu = MenuUtils.buildSizeMenu(this, POPUP_FONT);
    sizeSubmenu.setFont(POPUP_FONT);
    popup.add(sizeSubmenu);	// Add Size submenu to popup menu
}

/**
 * This hook lets subclasses customize the popup menu. By default,
 * nothing happens.
 */
protected void addCustomPopupItems() {}

protected void finalize() throws Throwable {
    getField().deleteObserver(this);
}

public void update(Observable obj, Object arg) {
    swingField.format();	// Redo font, style, etc.
    jimm.datavision.field.Rectangle b = getField().getBounds();
    JTextPane textPane = (JTextPane)getComponent();

    double width = b.width;
    if (width < MIN_SIZE) width = MIN_SIZE;
    double height = b.height;
    if (height < MIN_SIZE) height = MIN_SIZE;

    textPane.setBounds((int)b.x, (int)b.y, (int)width, (int)height);
    textPane.setText(getField().designLabel());
}

/**
 * Returns <code>true</code> if this field can be formatted.
 *
 * @return <code>true</code> if this field can be formatted
 */
public boolean usesFormat() {
    return true;
}

/**
 * Returns string to use for popup menu's first item, the (disabled)
 * name of this field.
 */
protected String getPopupNameText() {
    return getField().designLabel();
}

/**
 * Returns the section widget containing this field widget.
 *
 * @return the section widget containing this field widget
 */
public SectionWidget getSectionWidget() { return sectionWidget; }

/**
 * Returns <code>true</code> if the field is selected.
 *
 * @return <code>true</code> if the field is selected
 */
boolean isSelected() { return selected; }

/**
 * Align this field in relation to <i>prototype</i>.
 *
 * @param which one of the <code>Designer.ALIGN_*</code> constants
 * @param prototype the field to which this one should be aligned
 */
public void align(int which, Field prototype) {
    jimm.datavision.field.Rectangle b = getField().getBounds();
    jimm.datavision.field.Rectangle pb = prototype.getBounds();
    switch (which) {
    case Designer.ALIGN_TOP:
	b.setY(pb.y);
	break;
    case Designer.ALIGN_MIDDLE:
	double middle = pb.y + pb.height / 2;
	b.setY(middle - b.height / 2);
	break;
    case Designer.ALIGN_BOTTOM:
	b.setY(pb.y + pb.height - b.height);
	break;
    case Designer.ALIGN_LEFT:
	b.setX(pb.x);
	break;
    case Designer.ALIGN_CENTER:
	double center = pb.x + pb.width / 2;
	b.setX(center - b.width / 2);
	break;
    case Designer.ALIGN_RIGHT:
	b.setX(pb.x + pb.width - b.width);
	break;
    case Designer.ALIGN_SNAP_TO_GRID:
	sectionWidget.snapToGrid(b);
	break;
    }
}

/**
 * Resize this field in relation to <var>prototype</var>.
 *
 * @param which one of the <code>Designer.SIZE_SAME_*</code> constants
 * @param prototype the field from which this one should take sizes
 */
public void size(int which, Field prototype) {
    jimm.datavision.field.Rectangle b = getField().getBounds();
    jimm.datavision.field.Rectangle pb = prototype.getBounds();
    switch (which) {
    case Designer.SIZE_SAME_WIDTH:
	b.setWidth(pb.width);
	break;
    case Designer.SIZE_SAME_HEIGHT:
	b.setHeight(pb.height);
	break;
    case Designer.SIZE_SAME_SIZE:
	b.setWidth(pb.width);
	b.setHeight(pb.height);
	break;
    }
}

/**
 * Selects this field. If the shift key is down (we don't want to deselect
 * other fields), toggles the selection state instead. If
 * <i>deselectOthers</i> is <code>true</code>, do so. Eventually
 * {@link #doSelect} will be called.
 *
 * @param deselectOthers if <code>true</code>, do so
 */
void select(boolean deselectOthers) {
    sectionWidget.select(this, !selected, deselectOthers);
}

/**
 * Performs whatever is necessary to select or deselct self. Called by
 * {@link Designer#select}.
 *
 * @param makeSelected new selection state
 */
void doSelect(boolean makeSelected) {
    // For some reason, characters between the original selection click and
    // a deselection click get selected. Un-select all characters.
    JTextPane textPane = (JTextPane)getComponent();
    textPane.setCaretPosition(0);
    textPane.moveCaretPosition(0);

    if (selected != makeSelected) {
	selected = makeSelected;
	textPane.repaint(); // Reflect border changes
    }
}

/**
 * If the user is placing a new text field, pass it on to the section
 * widget; else do nothing.
 *
 * @param e mousevent
 */
public void mouseClicked(MouseEvent e) {
    if (sectionWidget.designer.isPlacingNewTextField())
	sectionWidget.createNewTextField(e);
    else {
	if (!mouseChangedSelectedState) {
	    select(!e.isShiftDown());
	}
    }
}

/**
 * Asks section to drag (move, resize) all selected widgets together. (The
 * section, in turn, asks the window to do the same.)
 *
 * @param e mouse event
 */
public void mouseDragged(MouseEvent e) {
    if (action == ACTION_INACTION || action == ACTION_POPPING_UP_MENU)
	return;

    if (!selected) {
	select(!e.isShiftDown());
	mouseChangedSelectedState = true;
    }

    // Set ePos to screen position of click
    java.awt.Point screenMousePos = e.getPoint();
    java.awt.Point screenPos = getComponent().getLocationOnScreen();
    screenMousePos.translate(screenPos.x, screenPos.y);

    if (!actionStartedYet) {
	actionStartedYet = true;
	switch (action) {
	case ACTION_MOVE:
	    // will eventually call our pickUp()
	    sectionWidget.pickUp(screenMousePos);
	    break;
	case ACTION_STRETCH_LEFT:
	case ACTION_STRETCH_RIGHT:
	case ACTION_STRETCH_TOP:
	case ACTION_STRETCH_BOTTOM:
	    // will eventually call our startStretching()
	    sectionWidget.startStretching(screenMousePos);
	    break;
	}
    }

    sectionWidget.dragSelectedWidgets(action, screenMousePos);
}

/**
 * Changes cursor if this widget is selected.
 *
 * @param e mouse event
 */
public void mouseEntered(MouseEvent e) {
    if (selected && !sectionWidget.designer.isPlacingNewTextField())
	cursorForPosition(e);
}

/**
 * Changes cursor if this widget is selected.
 *
 * @param e mouse event
 */
public void mouseExited(MouseEvent e) {
    if (selected && !sectionWidget.designer.isPlacingNewTextField())
	resetCursor();
}

/**
 * Changes cursor if this widget is selected.
 *
 * @param e mouse event
 */
public void mouseMoved(MouseEvent e) {
    if (selected && !sectionWidget.designer.isPlacingNewTextField())
	cursorForPosition(e);
}

/**
 * When the mouse is pressed, do the Right Thing(tm). Handles popup menu,
 * selecting, shift-selecting, and prepping for movement.
 *
 * @param e mouse event
 */
public void mousePressed(MouseEvent e) {
    mouseChangedSelectedState = false;

    if (mousePressReleaseCommon(e))
	return;

    cursorForPosition(e);
    action = actionFromPosition(e);

    actionStartedYet = false;	// Used to detect start of moves and stretches
}

/**
 * When the mouse is released and we have been dragging this field,
 * drop this one and all others that are being dragged.
 *
 * @param e mouse event
 */
public void mouseReleased(MouseEvent e) {
    if (mousePressReleaseCommon(e))
	return;

    switch (action) {
    case ACTION_MOVE:
	if (actionStartedYet) {
	    // Put down this and all selected fields.
	    // Set mousePos to screen position of mouse.
	    java.awt.Point screenMousePos = e.getPoint();
	    java.awt.Point screenPos = getComponent().getLocationOnScreen();
	    screenMousePos.translate(screenPos.x, screenPos.y);

	    // Put all selected widgets down. Might not be put down in same
	    // section.
	    sectionWidget.putDown(this, preMoveInfo.screenPos, screenMousePos);
	}
	break;
    case ACTION_STRETCH_LEFT:
    case ACTION_STRETCH_RIGHT:
    case ACTION_STRETCH_TOP:
    case ACTION_STRETCH_BOTTOM:
	if (actionStartedYet) {
	    // Stop stretching all selected widgets down.
	    sectionWidget.stopStretching(this, preStretchInfo.origBounds);
	}
	break;
    }

    action = ACTION_INACTION;
}

/**
 * Performs checks and behaviors common to both mouse presses and mouse
 * release events. Returns <code>true</code> if the event was handled by
 * this method and should be ignored by the caller.
 *
 * @param e the mouse event
 * @return <code>true</code> if the event was handled by this method and
 * should be ignored by the caller
 */
protected boolean mousePressReleaseCommon(MouseEvent e) {
    if (sectionWidget.designer.isPlacingNewTextField()) {
	sectionWidget.createNewTextField(e);
	return true;
    }

    if (e.isPopupTrigger()) {
	showPopupMenu(e);
	return true;
    }

    return false;
}

/**
 * Returns the information we saved before starting to move this widget.
 *
 * @return an object containing the information we saved before starting
 * to move this widget
 */
public PreMoveInfo getPreMoveInfo() { return preMoveInfo; }

/**
 * Prepares for movement by remembering where we are now and removing
 * ourself from the section view widget (but not the section model).
 */
public void pickUp(java.awt.Point mouseScreenPos) {
    preMoveInfo = new PreMoveInfo(this, mouseScreenPos);

    // Remove from section view widget, but not section model in report.
    sectionWidget.removeField(this);
}

/**
 * Place this field into a section widget. Our bounds rectangle is in
 * window coordinates; translate to section coordinates.
 */
public void putDown(SectionWidget sw) {
    // Recalculate bounds
    jimm.datavision.field.Rectangle b = getField().getBounds();
    b.setBounds(b.x - SectionWidget.LHS_WIDTH, b.y - sw.getBounds().y,
		b.width, b.height);

    // Move model and view to new section
    moveToSection(sw);

    preMoveInfo = null;
}

/**
 * Move back to original location in original section widget.
 */
void snapBack() {
    getField().getBounds().setBounds(preMoveInfo.origBounds);
    moveToSection(preMoveInfo.sectionWidget);
}

/**
 * Prepares for stretching by creating a <code>PreStretchInfo</code>.
 *
 * @param mouseScreenPos the location of the mouse in screen coordinates
 */
public void startStretching(java.awt.Point mouseScreenPos) {
    preStretchInfo = new PreStretchInfo(this, mouseScreenPos);
}

/**
 * Stop stretching.
 */
public void stopStretching() {
    preStretchInfo = null;
}

/**
 * Displays popup menu, after enabling/disabling the appropriate items.
 */
protected void showPopupMenu(MouseEvent e) {
    showOrHide.setText(getField().isVisible()
		       ? I18N.get(I18N.MENU_FILE_PREFIX,
				  "FieldWidget.popup_hide")
		       : I18N.get(I18N.MENU_FILE_PREFIX,
				  "FieldWidget.popup_show"));

    enableMenuItems();

    action = ACTION_POPPING_UP_MENU;
    popup.show(e.getComponent(), e.getX(), e.getY());
}

/**
 * Enables or disables popup menu items based on field and window state.
 */
protected void enableMenuItems() {
    boolean canFormat = usesFormat()
	|| sectionWidget.designer.someSelectedFieldUsesFormat();
    formatMenuItem.setEnabled(canFormat);

    int numSelectedFields = sectionWidget.designer.countSelectedFields();
    if (numSelectedFields >= 2) {
	// More than two fields selected.
	for (int i = 0; i < alignSubmenu.getItemCount(); ++i)
	    alignSubmenu.getItem(i).setEnabled(true);
	sizeSubmenu.setEnabled(true);
	aggregatesMenuItem.setEnabled(false);
    }
    else {
	// Only one item selected or this item is not selected.
	for (int i = 0; i < alignSubmenu.getItemCount() - 1; ++i)
	    alignSubmenu.getItem(i).setEnabled(false);
	sizeSubmenu.setEnabled(false);

	// Ask the AggregateField class if we can aggregate this field.
	aggregatesMenuItem.setEnabled(getField().canBeAggregated());
    }
}

/**
 * Given a mouse event, returns the <code>ACTION_*</code> constant
 * associated with the mouse position within the field.
 */
protected int actionFromPosition(MouseEvent e) {
    int ex = e.getX();
    int ey = e.getY();
    if (ex <= GRAB_EDGE_WIDTH)
	return ACTION_STRETCH_LEFT;
    else if (ex >= getField().getBounds().width - GRAB_EDGE_WIDTH)
	return ACTION_STRETCH_RIGHT;
    else if (ey <= GRAB_EDGE_WIDTH)
	return ACTION_STRETCH_TOP;
    else if (ey >= getField().getBounds().height - GRAB_EDGE_WIDTH)
	return ACTION_STRETCH_BOTTOM;
    else
	return ACTION_MOVE;
}

/**
 * If this field is selected, sets the cursor based on the current mouse
 * position in the widget. If the field is unselected, resets the cursor.
 *
 * @param e a mouse event
 */
protected void cursorForPosition(MouseEvent e) {
    if (!selected) {
	resetCursor();
	return;
    }

    switch (action == ACTION_INACTION ? actionFromPosition(e) : action) {
    case ACTION_MOVE:
	getComponent().setCursor(Cursor
				 .getPredefinedCursor(Cursor.MOVE_CURSOR));
	break;
    case ACTION_STRETCH_LEFT:
	getComponent().setCursor(Cursor
				 .getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
	break;
    case ACTION_STRETCH_RIGHT:
	getComponent().setCursor(Cursor
				 .getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
	break;
    case ACTION_STRETCH_TOP:
	getComponent().setCursor(Cursor
				 .getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
	break;
    case ACTION_STRETCH_BOTTOM:
	getComponent().setCursor(Cursor
				 .getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
	break;
    }
}

/**
 * Resets the cursor to its default.
 */
protected void resetCursor() {
    getComponent().setCursor(null);
}

/**
 * Performs a drag or a stretch. Called from {@link
 * Designer#dragSelectedWidgets}.
 *
 * @param action a <code>ACTION_*</code> constant
 * @param mouseScreenPos the location of the mouse in screen coordinates
 * @see Designer#dragSelectedWidgets
 */
void doDrag(int action, java.awt.Point mouseScreenPos) {
    jimm.datavision.field.Rectangle b = getField().getBounds();

    if (action == ACTION_MOVE) {
	int dx = mouseScreenPos.x - preMoveInfo.startMouseScreenPos.x;
	int dy = mouseScreenPos.y - preMoveInfo.startMouseScreenPos.y;
	// Must take into account the fact that we are floating on the
	// section container. See Designer.pickUp.
	b.setBounds(preMoveInfo.origBounds.x + dx + SectionWidget.LHS_WIDTH,
		    preMoveInfo.origBounds.y + dy
			+ sectionWidget.getBounds().y,
		    preMoveInfo.origBounds.width,
		    preMoveInfo.origBounds.height);
	return;
    }

    int dx = mouseScreenPos.x - preStretchInfo.startMouseScreenPos.x;
    int dy = mouseScreenPos.y - preStretchInfo.startMouseScreenPos.y;
    java.awt.Rectangle newBounds;

    switch (action) {
    case ACTION_STRETCH_LEFT:
	newBounds =
	    new java.awt.Rectangle((int)preStretchInfo.origBounds.x + dx,
				   (int)preStretchInfo.origBounds.y,
				   (int)preStretchInfo.origBounds.width - dx,
				   (int)preStretchInfo.origBounds.height);
	break;
    case ACTION_STRETCH_RIGHT:
	newBounds =
	    new java.awt.Rectangle((int)preStretchInfo.origBounds.x,
				   (int)preStretchInfo.origBounds.y,
				   (int)preStretchInfo.origBounds.width + dx,
				   (int)preStretchInfo.origBounds.height);
	break;
    case ACTION_STRETCH_TOP:
	newBounds =
	    new java.awt.Rectangle((int)preStretchInfo.origBounds.x,
				   (int)preStretchInfo.origBounds.y + dy,
				   (int)preStretchInfo.origBounds.width,
				   (int)preStretchInfo.origBounds.height - dy);
	break;
    case ACTION_STRETCH_BOTTOM:
	newBounds =
	    new java.awt.Rectangle((int)preStretchInfo.origBounds.x,
				   (int)preStretchInfo.origBounds.y,
				   (int)preStretchInfo.origBounds.width,
				   (int)preStretchInfo.origBounds.height + dy);
	break;
    default:
	return;
    }

    // Make sure new bounds fit within the section
    newBounds = newBounds.intersection(preStretchInfo.sectionBounds);

    // Make sure new bounds are not too small.
    switch (action) {
    case ACTION_STRETCH_LEFT:
	if (newBounds.width < MIN_SIZE) {
	    dx = MIN_SIZE - newBounds.width;
	    newBounds.x -= dx;
	    newBounds.width = MIN_SIZE;
	}
	break;
    case ACTION_STRETCH_RIGHT:
    if (newBounds.width < MIN_SIZE)
	newBounds.width = MIN_SIZE;
	break;
    case ACTION_STRETCH_TOP:
	if (newBounds.height < MIN_SIZE) {
	    dy = MIN_SIZE - newBounds.height;
	    newBounds.y -= dy;
	    newBounds.height = MIN_SIZE;
	}
	break;
    case ACTION_STRETCH_BOTTOM:
	if (newBounds.height < MIN_SIZE)
	    newBounds.height = MIN_SIZE;
	break;
    default:
	return;
    }

    b.setBounds(newBounds);
}

/**
 * Asks the window delete this field and all selected fields.
 */
protected void delete() {
    sectionWidget.designer.deleteSelectedFieldsAnd(this);
}

/**
 * Deletes this field from its section. Deletes the report field from the
 * report section (the model) and the field widget from the parent widget
 * (the view/controller).
 */
public void doDelete() {
    getField().getSection().removeField(getField());
    getComponent().getParent().remove(getComponent());
}

/**
 * Moves both field view and model to a new section. Technically, moves
 * model to a new section and adds view to section widget.
 *
 * @param sw a section widget
 */
public void moveToSection(SectionWidget sw) {
    // Bounds are already relative to new section widget

    Section currSection = getField().getSection();
    Section newSection = sw.section;
    if (newSection != currSection) {
	if (currSection != null)
	    currSection.removeField(getField());
	newSection.addField(getField());
    }

    // Always move to new section widget, because the act of dragging
    // lifted this widget from that section widget. (That's why we don't
    // need to remove it from the current section widget).
    sw.addField(this);

    // Not sure why this is necessary, but sometimes it is.
    getComponent().repaint();
}

/**
 * Handles drop of a dragged field. Passes request on to parent view.
 *
 * @param e drop event
 * @see SectionFieldPanel#drop
 */
public void drop(DropTargetDropEvent e) {
    ((DropTargetListener)getComponent().getParent()).drop(e);
}

public void dragEnter(DropTargetDragEvent e) { }
public void dragExit(DropTargetEvent e) { }
public void dragOver(DropTargetDragEvent e) { }
public void dropActionChanged(DropTargetDragEvent e) { }

/**
 * Performs some action based on the action command string (the menu
 * item text).
 */
public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command == null) return;

    Designer designer = sectionWidget.designer;

    if ("hide".equals(command) || "show".equals(command))
	toggleVisibility();
    else if ("delete".equals(command))
	delete();

    else if ("align_top".equals(command))
	designer.align(Designer.ALIGN_TOP);
    else if ("align_middle".equals(command))
	designer.align(Designer.ALIGN_MIDDLE);
    else if ("align_bottom".equals(command))
	designer.align(Designer.ALIGN_BOTTOM);
    else if ("align_left".equals(command))
	designer.align(Designer.ALIGN_LEFT);
    else if ("align_center".equals(command))
	designer.align(Designer.ALIGN_CENTER);
    else if ("align_right".equals(command))
	designer.align(Designer.ALIGN_RIGHT);
    else if ("snap_to_grid".equals(command)) {
	if (designer.countSelectedFields() > 0)
	    designer.align(Designer.ALIGN_SNAP_TO_GRID);
	else
	    align(Designer.ALIGN_SNAP_TO_GRID, getField());
    }

    else if ("size_width".equals(command))
	designer.size(Designer.SIZE_SAME_WIDTH);
    else if ("size_height".equals(command))
	designer.size(Designer.SIZE_SAME_HEIGHT);
    else if ("size_size".equals(command))
	designer.size(Designer.SIZE_SAME_SIZE);

    else if ("format".equals(command))
	new FormatWin(designer, this.getField(), 0);
    else if ("border".equals(command))
	new FormatWin(designer, this.getField(), 1);
    else if ("bounds".equals(command))
	new BoundsWin(designer, this.getField());
    else if ("aggregates".equals(command))
	new AggregatesWin(designer, this);
}

public String toString() {
    return getField().designLabel();
}

/**
 * Toggles the visiblity of this field and all selected fields.
 */
void toggleVisibility() {
    boolean newVisibility = !getField().isVisible();
    sectionWidget.setFieldVisibility(newVisibility, this);
}

/**
 * Sets the visiblity of this field. Called directly and/or indirectly
 * from <code>toggleVisibility</code>.
 */
public void doSetVisibility(boolean newVisibility) {
    getField().setVisible(newVisibility);
    setVisibilityLook();
}

/**
 * Sets the look of the field based on the current visiblity flag value. The
 * {@link jimm.datavision.layout.swing.AbstractSwingField} does all the work.
 * We just ask our <var>swingField</var> to re-format itself.
 */
protected void setVisibilityLook() {
    swingField.format();
}

Color getColor() { return swingField.getColor(); }

public Field getField() { return swingField.getField(); }

public JComponent getComponent() { return swingField.getComponent(); }

/* This code fixes the delete key not working. Basically, I discovered, for
 * reasons I frankly don't get at the moment, attaching the KeyListener to the
 * frame in DesignWin wasn't working (I'm beting it's some sort of event
 * bubbling issue with which I am unfamiliar). So, it had to be attached at
 * the widget level. So, putting here in FieldWidget applies it to all other
 * widgets. The keyPressed and keyTyped events we want to basically ignore, as
 * they only apply to TextFieldWidgets, and in that case, that class will
 * override keyTyped, which is what it's interested in. So, we implement the
 * delete code that was previously an anonymous inner class in DesignWin, and
 * we're good!
 * <p>
 * Frank W. Zammetti
 */

public void keyPressed(KeyEvent ke)  { return; }
public void keyTyped(KeyEvent ke)    { return; }
public void keyReleased(KeyEvent ke) {
    if (!sectionWidget.designer.ignoreKeys) {
	int code = ke.getKeyCode();
	if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE)
	    sectionWidget.designer.deleteSelectedFields();
    }
}

}
