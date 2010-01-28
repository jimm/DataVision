package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.gui.cmd.FormulaEditCommand;
import jimm.util.I18N;
import java.util.Observable;
import java.util.Observer;

/**
 * This dialog is for editing {@link Formula} code.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see FormulaWidget
 * @see jimm.datavision.gui.cmd.FormulaEditCommand
 */
public class FormulaWin extends ScriptEditorWin implements Observer {

protected Formula formula;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the report
 * @param formula the formula whose text needs editing
 */
public FormulaWin(Designer designer, Report report, Formula formula) {
    super(designer, report, formula.getEditableExpression(),
	  I18N.get("FormulaWin.title_prefix") + ' ' + formula.getName(),
	  "FormulaWin.error_unchanged", "FormulaWin.error_title");
    this.formula = formula;
    formula.addObserver(this);
    setLanguage(formula.getLanguage());
}

protected void finalize() throws Throwable {
    formula.deleteObserver(this);
    super.finalize();
}

public void update(Observable o, Object arg) {
    setTitle(I18N.get("FormulaWin.title_prefix") + ' ' + formula.getName());
    codeField.setText(formula.getEditableExpression());
}

/**
 * Creates and executes a command that changes the formula's eval string and
 * language. If there is an error, the command is cancelled (never sent to the
 * design window).
 *
 * @param text the new eval string
 */
public void save(String text) {
    formula.deleteObserver(this);
    command = new FormulaEditCommand(formula, text, getLanguage());
}

}
