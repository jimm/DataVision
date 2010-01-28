package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.Section;
import jimm.util.I18N;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.SectionWidget;

public class NewSectionCommand extends CommandAdapter {

protected Designer designer;
protected Report report;
protected Section sectionAbove;
protected Section section;
protected SectionWidget swAbove;

public NewSectionCommand(Designer designer, Report report, Section putBelow)
{
    super(I18N.get("NewSectionCommand.name"));

    this.designer = designer;
    this.report = report;
    sectionAbove = putBelow; // New section goes below "sectionAbove"
    swAbove = designer.findSectionWidgetFor(sectionAbove);
}

public void perform() {
    section = report.insertSectionBelow(sectionAbove);
    SectionWidget sw = new SectionWidget(designer, section, "");
    designer.insertSectionWidgetAfter(sw, swAbove);
}

public void undo() {
    // Removes section as observer and calls renameSectionWidgets
    designer.doDeleteSection(section);
}

}
