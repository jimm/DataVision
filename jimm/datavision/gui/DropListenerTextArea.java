package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.SpecialField;
import jimm.datavision.source.Column;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.JTextArea;

/**
 * A text area that accepts drags containing report fields. Used by
 * {@link CodeEditorWin}s.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class DropListenerTextArea
    extends JTextArea
    implements DropTargetListener
{

protected Report report;

public DropListenerTextArea(Report report, String text) {
    super(text);
    this.report = report;
    new DropTarget(this,	// component
		   DnDConstants.ACTION_COPY_OR_MOVE, // actions
		   this);	// DropTargetListener
}

public void drop(DropTargetDropEvent e) {
    try {
	DataFlavor stringFlavor = DataFlavor.stringFlavor;
	Transferable tr = e.getTransferable();
	if (e.isDataFlavorSupported(stringFlavor)) {
	    String str = (String)tr.getTransferData(stringFlavor);
	    if (str.startsWith("column:")) {
		Column col = report.findColumn(str.substring(7));
		replaceSelection("{" + col.fullName() + "}");
	    }
	    else if (str.startsWith("parameter:")) {
		Parameter param = report.findParameter(str.substring(10));
		replaceSelection(param.designLabel());
	    }
	    else if (str.startsWith("formula:")) {
		Formula formula = report.findFormula(str.substring(8));
		replaceSelection(formula.designLabel());
	    }
	    else if (str.startsWith("usercol:")) {
		UserColumn uc = report.findUserColumn(str.substring(8));
		replaceSelection(uc.designLabel());
	    }
	    else if (str.startsWith("special:")) {
		SpecialField sf = new SpecialField(null, report, null,
						   str.substring(8), false);
		replaceSelection(sf.formulaString());
	    }
	    else
		replaceSelection(str);

	    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
	    e.dropComplete(true);
	}
	else
	    e.rejectDrop();
    }
    catch(Exception ex) {
	ErrorHandler.error(ex);
    }
}

public void dragEnter(DropTargetDragEvent e) { }
public void dragExit(DropTargetEvent e) { }
public void dragOver(DropTargetDragEvent e) { }
public void dropActionChanged(DropTargetDragEvent e) { }
}
