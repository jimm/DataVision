package jimm.datavision.source.charsep;
import jimm.datavision.*;
import jimm.datavision.source.Query;
import jimm.datavision.source.Column;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A query used in character-separated file queries.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see CharSepSource
 */
public class CharSepQuery extends Query {

protected ArrayList charSepCols;

public CharSepQuery(Report r) {
    super(r);
    charSepCols = new ArrayList();
}

void addColumn(Column col) {
    charSepCols.add(col);
}

public void findSelectablesUsed() {
    super.findSelectablesUsed();
    for (Iterator iter = charSepCols.iterator(); iter.hasNext(); ) {
	Column col = (Column)iter.next();
	if (!selectables.contains(col)) selectables.add(col);
    }
}

}
