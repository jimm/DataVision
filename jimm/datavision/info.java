package jimm.datavision;
import jimm.util.Getopts;

/**
 * This class is a holder of version and copyright information.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class info {

public static final int VERSION_MAJOR = 1;
public static final int VERSION_MINOR = 2;
public static final int VERSION_TWEAK = 0;
public static final String VERSION_SUFFIX = "";

public static final String Version =
    "" + VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_TWEAK + VERSION_SUFFIX;
public static final String Copyright =
    "Copyright (c) 2001-2008 Jim Menard, jimm@io.com";
public static final String URL = "http://datavision.sourceforge.net";

/**
 * usage: info [-v] [-c] [-u] [-n]
 *
 * Prints version (-v), copyright (-c), and/or URL (-u). If -n is specified,
 * each is terminated with a newline. If it is not, no newline is output but
 * they are separated by spaces if more than one of -v, -c, or -u was also
 * specified.
 */
public static void main(String[] args) {
    Getopts g = new Getopts("vcun", args);
    boolean moreThanOne =
	((g.hasOption('v') ? 1 : 0)
	 + (g.hasOption('c') ? 1 : 0)
	 + (g.hasOption('u') ? 1 : 0)) > 1;
    String separator = g.hasOption('n') ? "\n" : " ";

    if (g.hasOption('v')) System.out.print(Version);
    if (moreThanOne) System.out.print(separator);
    if (g.hasOption('c')) System.out.print(Copyright);
    if (moreThanOne) System.out.print(separator);
    if (g.hasOption('u')) System.out.print(URL);

    if (g.hasOption('n')) System.out.println();
}

}
