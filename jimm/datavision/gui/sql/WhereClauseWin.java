package jimm.datavision.gui.sql;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.CodeEditorWin;
import jimm.datavision.Report;
import jimm.datavision.gui.cmd.WhereClauseEditCommand;
import jimm.util.I18N;

/**
 * This dialog lets the user edit the report query's where clause.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see jimm.datavision.source.Query
 * @see jimm.datavision.gui.cmd.WhereClauseEditCommand
 */
public class WhereClauseWin extends CodeEditorWin {

protected Report report;

/**
 * Constructor.
 *
 * @param designer the design window to which this dialog belongs
 * @param report the...um...I forgot
 */
public WhereClauseWin(Designer designer, Report report) {
    super(designer, report,
	  report.getDataSource().getQuery().getEditableWhereClause(),
	  I18N.get("WhereClauseWin.title"), null, null);
    this.report = report;
}

public void save(String text) {
    command =
	new WhereClauseEditCommand(report.getDataSource().getQuery(), text);
}

}
