package jimm.datavision.gui.sql;
import jimm.datavision.*;
import jimm.datavision.gui.Designer;
import jimm.datavision.source.*;
import jimm.datavision.gui.cmd.NewSubreportCommand;
import jimm.util.I18N;
import java.util.*;
import javax.swing.*;

/**
 * Starts the process of importing a sub-report.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class SubreportWin extends VisTableWin {

JLabel thisReportLabel, subreportLabel;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param report the...um...I forgot
 */
public SubreportWin(Designer designer, Report report) {
    super(designer, report, new Query(report));
}

protected void fillJoinsPanel() {
    delCheckBoxPanel.add(new JLabel(" "));

    thisReportLabel = new JLabel(I18N.get("SubreportWin.this_report"));
    fromPanel.add(thisReportLabel);
    thisReportLabel.setVisible(false);

    relationPanel.add(new JLabel(" "));

    subreportLabel = new JLabel(I18N.get("SubreportWin.subreport"));
    toPanel.add(subreportLabel);
    subreportLabel.setVisible(false);

    super.fillJoinsPanel();
}

protected void addNewJoin() {
    super.addNewJoin();
    thisReportLabel.setVisible(true);
    subreportLabel.setVisible(true);
}

protected void deleteSelectedJoins() {
    super.deleteSelectedJoins();
    thisReportLabel.setVisible(!joinFieldsList.isEmpty());
    subreportLabel.setVisible(!joinFieldsList.isEmpty());
}

protected void doSave() {
    JFileChooser chooser = Designer.getChooser();
    Designer.setPrefsDir(chooser,null);
    int returnVal = chooser.showOpenDialog(designer.getFrame());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
    Designer.savePrefsDir(chooser,null); // save report directory
	ArrayList newJoins = new ArrayList();
	for (Iterator iter = joinFieldsList.iterator(); iter.hasNext(); ) {
	    JoinFields jf = (JoinFields)iter.next();
	    Column from = columnFromDropdown(jf.from);
	    String relation = (String)jf.relation.getSelectedItem();
	    Column to = columnFromDropdown(jf.to);

	    newJoins.add(new Join(from, relation, to));
	}

	try {
	    NewSubreportCommand cmd =
		new NewSubreportCommand(designer, report,
					chooser.getSelectedFile(), newJoins);
	    cmd.perform();
	    commands.add(cmd);
	}
	catch (Exception e) {
	    ErrorHandler.error(e);
	}
    }
}

}
