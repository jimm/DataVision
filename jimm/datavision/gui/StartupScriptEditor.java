package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.gui.cmd.ReportStartupScriptEditCommand;
import jimm.util.I18N;

/**
 * This dialog is for editing {@link Formula} code.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see FormulaWidget
 * @see jimm.datavision.gui.cmd.FormulaEditCommand
 */
public class StartupScriptEditor extends ScriptEditorWin {

protected Report report;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the report
 */
public StartupScriptEditor(Designer designer, Report report) {
    super(designer, report,
	  report.getStartFormula() == null ? ""
	  : report.getStartFormula().getEditableExpression(),
	  I18N.get("StartupScriptEditor.title_prefix"),
	  "StartupScriptEditor.error_unchanged",
	  "StartupScriptEditor.error_title");
    this.report = report;

    if (report.getStartFormula() != null)
      setLanguage(report.getStartFormula().getLanguage());
    else
      setLanguage(report.getScripting().getDefaultLanguage());

    // (We don't need to observe this formula)
}

/**
 * Creates and executes a command that changes the formula's eval string.
 * If there is an error, the command is cancelled (never sent to the
 * design window).
 *
 * @param text the new eval string
 */
public void save(String text) {
    command = new ReportStartupScriptEditCommand(report, text, getLanguage());
}

}
