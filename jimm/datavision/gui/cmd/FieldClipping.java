package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.SectionArea;
import jimm.datavision.Section;
import jimm.datavision.field.Field;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.SectionWidget;

/**
 * A field clipping gets copied to the clipboard when a field widget is
 * cut. It contains not only a field widget but also the widget's original
 * section area.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class FieldClipping implements Pasteable {

protected Field origField;
protected FieldWidget newWidget;
protected SectionWidget origSectionWidget;
protected SectionArea sectionArea;
protected SectionResizeCommand sectionResizeCommand;

public FieldClipping(FieldWidget fw) {
    origField = fw.getField();

    // Remember the original section widget so we can attempt to put it
    // there again, even if this field widget gets moved in the mean time.
    origSectionWidget = fw.getSectionWidget();

    // In case the original section no longer exists, or we are pasting
    // into a different report, remember the area of the section so we
    // can paste into the same area later.
    sectionArea = origSectionWidget.getSectionArea();
}

public void paste(Designer designer) {
    Report pasteReport = designer.getReport();
    Report origReport = origSectionWidget.getReport();
    SectionWidget sw = null;

    if (pasteReport == origReport) {
	if (pasteReport.contains(origSectionWidget.getSection()))
	    sw = origSectionWidget;
	else
	    sw = sectionWidgetBySectionArea(designer);
    }
    else {			// Different reports; go by section area
	sw = sectionWidgetBySectionArea(designer);
    }


    Field newField = (Field)origField.clone();
    newWidget = newField.makeWidget(sw);

    sectionResizeCommand = new SectionResizeCommand(sw);
    newWidget.moveToSection(sw); // Possibly resizes section
    sectionResizeCommand.perform();

    designer.select(newWidget, true, false);

    // Don't need to call Designer.enableMenuItems because each call
    // to Designer.select already does that.
}

protected SectionWidget sectionWidgetBySectionArea(Designer designer) {
    Report report = designer.getReport();
    Section s = report.getFirstSectionByArea(sectionArea.getArea());
    if (s == null)		// Can be null if area was group, for example
	// Will not be null; there is always at least one report header section
	s = report.getFirstSectionByArea(SectionArea.REPORT_HEADER);
    return designer.findSectionWidgetFor(s);
}

public void undo(Designer designer) {
    newWidget.doDelete();	// Widget deletes itself and field from report
    sectionResizeCommand.undo();
    designer.enableMenuItems();
}

}
