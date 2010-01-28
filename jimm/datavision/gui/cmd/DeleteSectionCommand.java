package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Section;
import jimm.datavision.ReportSectionLoc;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.SectionWidget;
import jimm.util.I18N;

public class DeleteSectionCommand extends CommandAdapter {

protected Designer designer;
protected Report report;
protected Section section;
protected ReportSectionLoc sectionLoc;
protected SectionWidget sectionWidget;
protected SectionWidget sectionWidgetAbove;

/**
 * Constructor.
 */
public DeleteSectionCommand(Designer designer, Report report,
			    Section section)
{
    super(I18N.get("DeleteSectionCommand.name"));
    this.designer = designer;
    this.report = report;
    this.section = section;
    this.sectionWidget = designer.findSectionWidgetFor(section);
    sectionLoc = report.getSectionLocation(section);
}

public void perform() {
    // Removes section from report and window.
    sectionWidgetAbove = designer.doDeleteSection(section);
}

public void undo() {
    report.reinsertSection(sectionLoc);
    designer.insertSectionWidgetAfter(sectionWidget, sectionWidgetAbove);
}

}
