package jimm.datavision.gui.cmd;
import jimm.datavision.gui.Designer;

/**
 * Pasteable objects are what get put on the clipboard. They know how to paste
 * themselves and how to undo that operation. Pasteables aren't commands. The
 * {@link PasteCommand} needs to operate on whatever is in the clibpard but it
 * can't know what to do with whatever is there, so it relies on pasteables to
 * do the job.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
interface Pasteable {

public void paste(Designer designer);
public void undo(Designer designer);

}
