package jimm.datavision.source.charsep;
import jimm.datavision.*;
import jimm.datavision.source.Query;
import jimm.datavision.source.Column;
import java.util.ArrayList;

/**
 * A query used in character-separated file queries.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 * @see CharSepSource
 */
public class CharSepQuery extends Query {

protected ArrayList<Column> charSepCols;

public CharSepQuery(Report r) {
    super(r);
    charSepCols = new ArrayList<Column>();
}

void addColumn(Column col) {
    charSepCols.add(col);
}

public void findSelectablesUsed() {
    super.findSelectablesUsed();
    for (Column col : charSepCols)
	if (!selectables.contains(col)) selectables.add(col);
}

}
