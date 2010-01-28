package jimm.datavision.test.mock.source;
import jimm.datavision.Selectable;
import jimm.datavision.source.DataCursor;
import jimm.datavision.source.Query;
import java.util.List;
import java.util.ArrayList;

public class MockAggregateDataCursor extends DataCursor {

protected static final Object[][] DATA = {
  { "A", "B", "D", new Integer(2) },
  { "A", "B", "D", new Integer(24) },
  { "A", "B", "D", new Integer(3) },
  { "A", "C", "D", new Integer(12) },
  { "A", "C", "D", new Integer(42) }
};

protected static final String[] SELECTABLES = {
  "aggregate_test.col1", "aggregate_test.col2", "aggregate_test.col3",
  "aggregate_test.value"
};

static int indexOfSelectable(Selectable sel) {
  String selId = sel.getId().toString();
  for (int i = 0; i < SELECTABLES.length; ++i)
    if (selId.equals(SELECTABLES[i]))
      return i;

  throw new RuntimeException("can't find selectable \"" +
			     sel.getDisplayName() + "\" with id " + selId);
}

protected int recordIndex;

public MockAggregateDataCursor(Query q) {
  recordIndex = -1;
}

protected List readRowData() {
  ++recordIndex;
  if (recordIndex >= DATA.length)
    return null;

  List row = new ArrayList(DATA[recordIndex].length);
  for (int i = 0; i < DATA[recordIndex].length; ++i)
    row.add(DATA[recordIndex][i]);

  return row;
}

}
