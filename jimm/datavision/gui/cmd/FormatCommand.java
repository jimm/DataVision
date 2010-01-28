package jimm.datavision.gui.cmd;
import jimm.datavision.field.Format;
import jimm.datavision.field.Border;
import jimm.datavision.field.Field;
import jimm.util.I18N;

/**
 * A command for changing a field's format and border.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FormatCommand extends CommandAdapter {

protected Field field;
protected Format origFormat;
protected Format newFormat;
protected Border origBorder;
protected Border newBorder;

public FormatCommand(Field f, Format format, Border border) {
    super(I18N.get("FormatCommand.name"));

    field = f;

    origFormat = field.getFormat();
    if (origFormat != null) origFormat = (Format)origFormat.clone();
    newFormat = format;

    origBorder = field.getBorder();
    if (origBorder != null) origBorder = (Border)origBorder.clone();
    newBorder = border;
}

public void perform() {
    field.setFormat(newFormat);
    field.setBorder(newBorder);
}

public void undo() {
    field.setFormat(origFormat);
    field.setBorder(origBorder);
}

}
