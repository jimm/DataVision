package jimm.datavision.gui;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * The {#link FieldPickerWin} uses this JTree subclass.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldPickerTree
    extends JTree
    implements DragGestureListener, DragSourceListener
{

public FieldPickerTree(DefaultTreeModel model) {
    super(model);

    DragSource dragSource = DragSource.getDefaultDragSource();

    dragSource.createDefaultDragGestureRecognizer(
		  this,  // component where drag originates
		  DnDConstants.ACTION_COPY_OR_MOVE, // actions
		  this); // drag gesture recognizer

    // Listen for double clicks
    addMouseListener(new MouseAdapter() {
	public void mousePressed(MouseEvent e) {
//  	    int selRow = getRowForLocation(e.getX(), e.getY());
	    TreePath selPath = getPathForLocation(e.getX(), e.getY());
	    if (selPath != null && e.getClickCount() == 2)
		doubleClicked(selPath);
	    super.mousePressed(e);
	}
	});
}

/**
 * Handles double clicks. Opens an editor if one is available. The
 * <code>FPLeafInfo</code> subclasses know how to open themselves, and
 * it is harmless to try to open one that has no editor.
 *
 * @param selPath a tree path
 */
protected void doubleClicked(TreePath selPath) {
    DefaultMutableTreeNode typeNode =
	(DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
    Object obj = typeNode.getUserObject();
    if (obj instanceof FPLeafInfo)
	((FPLeafInfo)obj).openEditor();
}

public void dragGestureRecognized(DragGestureEvent e) {
    DefaultMutableTreeNode typeNode =
	(DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
    Object obj = typeNode.getUserObject();
    if (obj instanceof FPLeafInfo) {
	FPLeafInfo info = (FPLeafInfo)obj;
	if (info != null) {
	    e.startDrag(DragSource.DefaultCopyDrop, // cursor
			new StringSelection(info.dragString()), // transferable
			this);  // drag source listener
	}
    }
}

public void dragDropEnd(DragSourceDropEvent e) {}
public void dragEnter(DragSourceDragEvent e) {}
public void dragExit(DragSourceEvent e) {}
public void dragOver(DragSourceDragEvent e) {}
public void dropActionChanged(DragSourceDragEvent e) {}

/** Removes the currently selected node. */
public void removeCurrentNode() {
    TreePath currentSelection = getSelectionPath();
    if (currentSelection != null) {
	DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
	    (currentSelection.getLastPathComponent());
	MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
	if (parent != null) {
	    ((DefaultTreeModel)getModel()).removeNodeFromParent(currentNode);
	}
    } 
}


}
