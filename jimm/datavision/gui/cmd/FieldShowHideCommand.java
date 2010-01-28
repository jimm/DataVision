package jimm.datavision.gui.cmd;
import jimm.datavision.gui.FieldWidget;
import jimm.util.I18N;

/**
 * Shows or hides a field.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldShowHideCommand extends CommandAdapter {

protected FieldWidget fw;
protected boolean newVisibility;

public FieldShowHideCommand(FieldWidget fw, String nameKey,
			    boolean newVisibility)
{
    super(I18N.get(nameKey));

    this.fw = fw;
    this.newVisibility = newVisibility;
}

public void perform() {
    fw.doSetVisibility(newVisibility);
}

public void undo() {
    fw.doSetVisibility(!newVisibility);
}

}
