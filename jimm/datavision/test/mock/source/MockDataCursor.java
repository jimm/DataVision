package jimm.datavision.test.mock.source;
import jimm.datavision.Selectable;
import jimm.datavision.source.DataCursor;
import jimm.datavision.source.Query;
import java.util.List;
import java.util.ArrayList;

public class MockDataCursor extends DataCursor {

protected static final String[] CITIES = {
  "Chicago", "New Jersey", "New York"
};
protected static final String[] SHORT_NAMES = { "CH", "NJ", "NY" };
protected static final String[] SELECTABLES = {
  "office.name", "2", "jobs.post_date", "jobs.ID",
  "jobs.title", "jobs.hourly rate"
};

protected int cityIndex;
protected int jobIndex;
protected Query query;

static int indexOfSelectable(Selectable sel) {
  String selId = sel.getId().toString();
  for (int i = 0; i < SELECTABLES.length; ++i)
    if (selId.equals(SELECTABLES[i]))
      return i;

  throw new RuntimeException("can't find selectable \"" +
			     sel.getDisplayName() + "\" with id " + selId);
}

public MockDataCursor(Query q) {
  jobIndex = cityIndex = -1;
  query = q;
}

protected List readRowData() {
  ++jobIndex;
  if ((jobIndex % 100) == 0)	// 100 jobs per city
    ++cityIndex;
  if (cityIndex >= CITIES.length) // no more cities; report is done
    return null;
  // When using parameter, stop after Chicago
  if (query.getEditableWhereClause().startsWith("office.name = ") &&
      cityIndex > 0)
    return null;

  List row = new ArrayList();
  row.add(CITIES[cityIndex]);	// office.name
  row.add(SHORT_NAMES[cityIndex]); // Short Office Name (id "2")
  row.add(new java.util.Date()); // jobs.post_date
  row.add(new Integer(jobIndex)); // jobs.ID
  row.add("This is the short description of job " + jobIndex); // jobs.title
  row.add(jobIndex == 0 ? null	// jobs."hourly rate"
	  : new Integer(jobIndex * 100));

  return row;
}

}
