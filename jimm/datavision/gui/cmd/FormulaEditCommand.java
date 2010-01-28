package jimm.datavision.gui.cmd;
import jimm.datavision.Formula;
import jimm.util.I18N;

/**
 * A command for changing a {@link Formula}'s code text.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FormulaEditCommand extends CommandAdapter {

protected Formula formula;
protected String newExpression;
protected String oldExpression;
protected String newLanguage;
protected String oldLanguage;

public FormulaEditCommand(Formula formula, String expression, String language) {
    super(I18N.get("FormulaEditCommand.name"));
    this.formula = formula;
    newExpression = expression;
    oldExpression = formula.getExpression();
    newLanguage = language;
    oldLanguage = formula.getLanguage();
}

public void perform() {
    formula.setLanguage(newLanguage);
    formula.setEditableExpression(newExpression);
}

public void undo() {
    formula.setLanguage(oldLanguage);
    formula.setEditableExpression(oldExpression);
}

}
