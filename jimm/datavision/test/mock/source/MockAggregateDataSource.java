package jimm.datavision.test.mock.source;
import jimm.datavision.Report;
import jimm.datavision.Selectable;
import jimm.datavision.source.DataCursor;

public class MockAggregateDataSource extends MockDataSource {

public MockAggregateDataSource(Report r) {
  super(r);
}

public DataCursor execute() throws Exception {
  return new MockAggregateDataCursor(getQuery());
}

public int indexOfSelectable(Selectable sel) {
  return MockAggregateDataCursor.indexOfSelectable(sel);
}

}
