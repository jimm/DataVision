package jimm.datavision.gui.cmd;
import jimm.datavision.*;
import jimm.datavision.gui.SectionWidget;
import jimm.util.I18N;

/**
 * Handles suppression proc edits.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SuppressionProcEditCommand extends CommandAdapter {

protected SectionWidget sectionWidget;
protected boolean oldHidden;
protected boolean newHidden;
protected String oldText;
protected String newText;
protected String oldLanguage;
protected String newLanguage;

public SuppressionProcEditCommand(SectionWidget sectionWidget, boolean hidden,
				  String formulaText, String language)
{
    super(I18N.get("SuppressionProcEditCommand.name"));
    this.sectionWidget = sectionWidget;

    SuppressionProc sp = sectionWidget.getSection().getSuppressionProc();
    newHidden = hidden;
    oldHidden = sp.isHidden();

    Formula f = sp.getFormula();

    newText = formulaText;
    oldText = f.getExpression();

    newLanguage = language;
    oldLanguage = f.getLanguage();
}

public void perform() {
    SuppressionProc sp = sectionWidget.getSection().getSuppressionProc();
    sp.setHidden(newHidden);

    Formula f = sp.getFormula();
    f.setExpression(newText);
    f.setLanguage(newLanguage);

    // Possible background color change
    sectionWidget.getFieldPanel().setHidden(newHidden);
}

public void undo() {
    SuppressionProc sp = sectionWidget.getSection().getSuppressionProc();
    sp.setHidden(oldHidden);

    Formula f = sp.getFormula();
    f.setExpression(oldText);
    f.setLanguage(oldLanguage);

    // Possible background color change
    sectionWidget.getFieldPanel().setHidden(oldHidden);
}

}
