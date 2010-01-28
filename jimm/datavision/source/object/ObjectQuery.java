package jimm.datavision.source.object;
import jimm.datavision.*;
import jimm.datavision.source.Query;
import jimm.datavision.source.Column;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A query used in object data source queries.
 *
 * @author Frank W. Zammetti, <a href="mailto:fzammetti@omnytex.com">fzammetti@omnytex.com</a>
 * @see ObjectSource
 */
public class ObjectQuery extends Query {

protected ArrayList objectCols;

public ObjectQuery(Report r) {
    super(r);
    objectCols = new ArrayList();
}

void addColumn(Column col) {
    objectCols.add(col);
}

public void findSelectablesUsed() {
    super.findSelectablesUsed();
    for (Iterator iter = objectCols.iterator(); iter.hasNext(); ) {
	Column col = (Column)iter.next();
	if (!selectables.contains(col)) selectables.add(col);
    }
}

}
