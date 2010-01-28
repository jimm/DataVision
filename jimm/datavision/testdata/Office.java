package jimm.datavision.testdata;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Generates test data for the office table. Used by the
 * <code>CreateData</code> classes found in the database subdirectories.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Office {

public static final int NUM_OFFICES = 3;

public int id;
public String name, abbrev, fax, email;
public boolean visible;

/**
 * Returns an iterator over the office test data.
 *
 * @return an iterator over <code>Office</code> objects
 */
public static Iterator offices() {
    ArrayList offices = new ArrayList();
    offices.add(new Office(1, "New York", "NY", "(212) 555-1234",
			   "nyc_jobs@example.com", true));
    offices.add(new Office(2, "New Jersey", "NJ", "(973) 555-1234",
			   "nj_jobs@example.com", true));
    offices.add(new Office(3, "Chicago", "Chicago", "(312) 555-1234",
			   "chicago_jobs@example.com", true));
    return offices.iterator();
}

public Office(int i, String n, String a, String f, String e, boolean v) {
    id = i; name = n; abbrev = a; fax = f; email = e; visible = v;
}

}
