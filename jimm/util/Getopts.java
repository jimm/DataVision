package jimm.util;
import java.util.HashMap;

/**
 * Getopts is similar to the UN*X getopt() system call. It parses an array of
 * Strings (usually the command line), looking for specified option flags and
 * values.
 * <p>
 * An instance of Getopts parses the whole args list at once, and stores the
 * option flags and values that it finds.
 *
 * @author Jim Menard,
 * <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Getopts {
String[] argv;
HashMap options = new HashMap();
boolean errorFlag = false;

/**
 * This constructor takes a list of legal options and a list of (usually
 * command line) arguments. Each option in optionListString may be followed
 * by a ':' to signify that option takes an argument.
 *
 * @param optionListString option chars with optional ':' specifying arg.
 * For example, "ab:c" specifies three options, a, b, and c. Option b takes
 * a (required) argument.
 * @param args array of command line arguments
 */
public Getopts(String optionListString, String[] args) {
    String optChoices = optionListString;

    for (int index = 0; index < args.length; ++index) {
	String arg = args[index];
	if (arg.startsWith("-")) {
	    char optionChar = arg.charAt(1);
	    int optionLoc = optChoices.indexOf(optionChar);
	    if (optionLoc == -1)
		errorFlag = true;
	    else {
		// Look for argument, if any
		boolean hasArgument =
		    optChoices.length() > optionLoc + 1 &&
		    optChoices.charAt(optionLoc + 1) == ':';
		if (hasArgument) {
		    String optarg = arg.substring(2);
		    if (optarg.equals("")) {
			++index;
			try {
			    optarg = args[index];
			}
			catch (Exception e) { // Catch ArrayOutOfBounds
			    optarg = "";
			    errorFlag = true;
			}
		    }
		    options.put(new Character(optionChar), optarg);
		}
		else {
		    // No arg, store empty string
		    options.put(new Character(optionChar), "");
		}
	    }
	}
	else {			// End of options. Store rest of args
	    argv = new String[args.length - index];
	    int offset = index;
	    while (index < args.length) {
		argv[index - offset] = args[index];
		++index;
	    }
	    break;
	}
    }
}

/**
 * 
 * Return true if there was an error while parsing the command line.
 */
public boolean error() {
    return errorFlag;
}

/**
 * Returns existence of an option.
 *
 * @return true of option 'c' exists, else return false.
 * @param c any character
 */
public boolean hasOption(char c) {
    if (options == null)
	return false;
    return options.containsKey(new Character(c));
}

/**
 * Return an option or, if missing, the empty string.
 *
 * @return option string, or "" if error or option has no argument
 * @param c the option whose value is returned
 */
public String option(char c) {
    return option(c, "");
}

/**
 * Return an option or, if missing, a default value.
 *
 * @return option string, or defaultValue if error or option has no argument
 * @param c the option whose value is returned
 * @param defaultValue the value to return if there is no such option
 */
public String option(char c, String defaultValue) {
    if (options == null)
	return defaultValue;

    String s;
    try {
	Object o = options.get(new Character(c));
	if (o == null || !(o instanceof String))
	    s = defaultValue;
	else
	    s = (String)o;
    }
    catch (Exception e) {
	s = defaultValue;
    }
    return s;
}

/**
 * Return the remaining command-line arguments.
 *
 * @return an array of Strings
 * @see #argc
 * @see #argv
 */
public String[] args() {
    return argv;
}

/**
 * Return the number of non-option args.
 */
public int argc() {
    if (argv == null)
	return 0;
    return argv.length;
}
/**
 * Return a command line argument or "" if <var>argv</var> is
 * <code>null</code>. Index starts at 0.
 *
 * @param index which argument to return
 * @return the index'th arg or "" if <var>argv</var> is <code>null</code>
 */
public String argv(int index) {
    if (argv == null)
	return "";

    return argv[index];
}
}
