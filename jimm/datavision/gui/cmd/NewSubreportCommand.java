package jimm.datavision.gui.cmd;
import jimm.datavision.Report;
import jimm.datavision.SectionArea;
import jimm.datavision.Subreport;
import jimm.datavision.Point;
import jimm.datavision.field.Field;
import jimm.datavision.field.Rectangle;
import jimm.datavision.source.sql.Database;
import jimm.datavision.source.sql.SubreportDatabase;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.FieldWidget;
import java.io.File;
import java.util.Collection;

public class NewSubreportCommand extends InsertFieldCommand {

protected Designer designer;
protected Report report;
protected Subreport subreport;

public NewSubreportCommand(Designer designer, Report report, File f,
			   Collection newJoins)
    throws Exception
{
    super(designer.findSectionWidgetFor(report.getFirstSectionByArea(SectionArea.DETAIL)),
	  "subreport", new Point(0, report.getFirstSectionByArea(SectionArea.DETAIL).getMinHeight()));

    this.designer = designer;
    this.report = report;

    subreport = new Subreport(report, null);
    Database db = (Database)report.getDataSource();
    subreport.setDataSource(new SubreportDatabase(db.getConnection(),
						  subreport));
    subreport.read(f);
    subreport.addAllJoins(newJoins);
}

protected Rectangle initialFieldBounds() {
    return new Rectangle(insertLoc.getX(),
			 insertLoc.getY() - (int)(Field.DEFAULT_HEIGHT / 2),
			 (double)(Field.DEFAULT_WIDTH * 4),
			 (double)Field.DEFAULT_HEIGHT);
}

protected Object initialFieldValue() {
    return subreport.getId();
}

protected FieldWidget createFieldWidget(Field f) {
    return new FieldWidget(null, f);
}

}
