package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Formula;
import jimm.util.I18N;

/**
 * A command for changing the {@link Report}'s startup {@link Formula}'s code
 * text.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ReportStartupScriptEditCommand extends CommandAdapter {

protected Report report;
protected String newExpression;
protected String oldExpression;
protected String oldLanguage;
protected String newLanguage;

public ReportStartupScriptEditCommand(Report report, String expression,
				      String language)
{
    super(I18N.get("ReportStartupScriptEditCommand.name"));
    this.report = report;

    Formula f = report.getStartFormula();

    newExpression = expression;
    oldExpression = f == null ? null : f.getExpression();

    newLanguage = language;
    oldLanguage = f == null ? null : f.getLanguage();
}

public void perform() {
    Formula f = null;
    if (oldExpression == null) {
	f = new Formula(null, report, "", "");
	report.setStartFormula(f);
    }
    else
	f = report.getStartFormula();

    f.setEditableExpression(newExpression);
    f.setLanguage(newLanguage);
}

public void undo() {
    if (oldExpression == null)
	report.setStartFormula(null);
    else {
	Formula f = report.getStartFormula();
	f.setEditableExpression(oldExpression);
	f.setLanguage(oldLanguage);
    }
}

}
