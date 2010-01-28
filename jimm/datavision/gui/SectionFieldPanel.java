package jimm.datavision.gui;
import jimm.datavision.ErrorHandler;
import jimm.datavision.gui.cmd.NewDraggedFieldCommand;
import java.awt.Color;
import java.awt.Component;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.JPanel;

/**
 * This is the panel that holds {@link FieldWidget}s within a {@link
 * SectionWidget}. It can handle dragged fields.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SectionFieldPanel extends JPanel implements DropTargetListener {

protected SectionWidget sectionWidget;

/**
 * Constructor.
 *
 * @param sw the section widget
 */
public SectionFieldPanel(SectionWidget sw) {
    super();
    sectionWidget = sw;
    new DropTarget(this,	// component
		   DnDConstants.ACTION_COPY_OR_MOVE, // actions
		   this);	// DropTargetListener
}

/**
 * Accepts fields dragged from the field picker window.
 */
public void drop(DropTargetDropEvent e) {
    try {
	DataFlavor stringFlavor = DataFlavor.stringFlavor;
	Transferable tr = e.getTransferable();
	if (e.isDataFlavorSupported(stringFlavor)) {
	    addField(e, (String)tr.getTransferData(stringFlavor));
	    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
	    e.dropComplete(true);
	}
	else {
	    e.rejectDrop();
	}
    }
    catch(Exception ex) {
	ErrorHandler.error(ex);
    }
}

public void dragEnter(DropTargetDragEvent e) { }
public void dragExit(DropTargetEvent e) { }
public void dragOver(DropTargetDragEvent e) { }
public void dropActionChanged(DropTargetDragEvent e) { }

/**
 * Creates and adds a new field. The type and contents of the field are
 * determined by the string that was dropped. If the field is a
 * <code>ColumnField</code> and this is a detail section, add a text title
 * field as well.
 *
 * @param e the drop event
 * @param dropString the string received from the field picker window
 */
protected void addField(DropTargetDropEvent e, String dropString) {
    sectionWidget.performCommand(new NewDraggedFieldCommand(sectionWidget,
							    dropString, e));
}

/**
 * Performs visual changes that reflect the "hidden" state of the section.
 * Called by the command that edits the suppression proc of a section.
 *
 * @param isHidden new suppressed state
 */
public void setHidden(boolean isHidden) {
    Color c = isHidden ? SectionWidget.SUPPRESSED_COLOR
	: SectionWidget.NORMAL_COLOR;
    setBackground(c);
    Component[] kids = getComponents();
    for (int i = 0; i < kids.length; ++i)
	kids[i].setBackground(c);
}

}
