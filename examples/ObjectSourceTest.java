import jimm.datavision.*;
import jimm.datavision.layout.swing.*;
import jimm.datavision.source.*;
import jimm.datavision.source.object.*;
import java.util.*;
import java.sql.*;

// This is a simple test class that tests the ObjectSource data source.
// Used in conjunction with ObjectSourceTest.xml report file.
public class ObjectSourceTest {

  public static void main(String[] args) {

    try {

      // Create the data to report against.  Each element in the ArrayList named
      // data represents a row, and each element in the ArrayLists row1 and
      // row2 are columns in the given row.
      ArrayList data = new ArrayList();
      ArrayList row1 = new ArrayList();
      row1.add("Chicago");
      row1.add(new Integer(8));
      row1.add("This is the short description of job 1");
      row1.add(new Integer(800));
      row1.add(new java.util.Date(new GregorianCalendar().getTimeInMillis()));
      data.add(row1);
      ArrayList row2 = new ArrayList();
      row2.add("Arizona");
      row2.add(new Integer(12));
      row2.add("This is the short description of job 2");
      row2.add(new Integer(123));
      row2.add(new java.util.Date(new GregorianCalendar().getTimeInMillis() + 100000));
      data.add(row2);

      // Instantiate a Report object and read in the report XML file.  Note
      // that ObjectSourceTest.xml is simply a copy of charsep.xml, the
      // CharSepSource example report.  I made a copy just in case I want to
      // change this example up a bit later.
      Report report = new Report();
      report.read(new java.io.File("ObjectSourceTest.xml"));

      // Now create the ObjectSource data source.  Then, we have to define the
      // columns in our data.  This mimics what DataVision does behind the
      // scenes when you feed it a definition file for charsep data.  Note that
      // the data types are standard JDBC data types.
      ObjectSource src = new ObjectSource(report, data);
      src.addColumn(new Column("office.name", "office.name", Types.VARCHAR));
      src.addColumn(new Column("jobs.ID", "jobs.ID", Types.INTEGER));
      src.addColumn(new Column("jobs.title", "jobs.title", Types.VARCHAR));
      src.addColumn(new Column("jobs.hourly rate", "jobs.hourly rate", Types.INTEGER));
      src.addColumn(new Column("jobs.post_date", "jobs.post_date", Types.TIMESTAMP));
      report.setDataSource(src);

      // Any layout engine should work, but we'll use Swing for this example.
      // The additional code ensures it closes properly.  Then, run the
      // report in this thread.
      report.setLayoutEngine(
        new SwingLE() {
          public void close() {
            super.close();
            System.exit(0);
          }
        }
      );
      report.runReport();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
