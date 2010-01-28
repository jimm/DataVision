package jimm.util;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * This class finds the local version of any string. It also contains
 * a method for changing the language.
 * <p>
 * Each language should have a file called datavision_XX_YY.properties,
 * where XX is the language code (e.g., "en" for English, "fr" for French)
 * and YY is the country code (e.g., "US", "FR").
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class I18N {

public static final String RESOURCE_FILE_PREFIX = "datavision";
public static final String MENU_FILE_PREFIX = "menu";
public static final String PAPER_FILE_PREFIX = "paper";

protected static Locale locale;
protected static HashMap bundles;

// Initialize the language.
static {
    setLanguage(Locale.getDefault());
}

/**
 * Given a locale, start using the code short phrases for that lanuage.
 * Normally, you won't have to call this method. It gets called at startup
 * and sets the locale to the default one.
 *
 * @param l the new locale
 */
public static void setLanguage(Locale l) {
    if (!l.equals(locale)) {
	locale = l;
	bundles = new HashMap();
    }
}

/**
 * Returns the string corresponding to the specified string. Returns
 * <var>key</var> if <var>key</var> is either <code>null</code> or the
 * empty string. Reports an error if <var>key</var> does not exist.
 *
 * @param key the lookup key
 * @return the string corresponding to the specified lookup key
 * or <code>null</code> if there isn't one; if <var>key</var> is the
 * empty string, return it
 */
public static String get(String key) {
    return get(RESOURCE_FILE_PREFIX, key);
}

/**
 * Returns the string corresponding to the specified string in the bundle
 * file corresponding to the name <var>prefix</var>. Returns <var>key</var>
 * if <var>key</var> is either <code>null</code> or the empty string.
 * Reports an error if <var>key</var> does not exist.
 *
 * @param prefix the bundle file name prefix
 * @param key the lookup key
 * @return the string corresponding to the specified lookup key
 * or <code>null</code> if there isn't one; if <var>key</var> is the
 * empty string, return it
 */
public static String get(String prefix, String key) {
    if (key == null || prefix == null || prefix.length() == 0) return null;
    if (key.length() == 0) return "";

    String val = "";
    try {
	ResourceBundle strings = getBundle(prefix);
	val = strings.getString(key);
	if (val == null) val = key;
	else val = val.trim();
    }
    catch (MissingResourceException ex) {
	val = key;
    }
    return val;
}

/**
 * Returns the string corresponding to the specified string. Returns
 * <var>key</var> if <var>key</var> is either <code>null</code> or the
 * empty string. Reports <code>null</code> if <var>key</var> does not exist.
 *
 * @param key the lookup key
 * @return the string corresponding to the specified lookup key
 * or <code>null</code> if there isn't one; if <var>key</var> is the
 * empty string, return it
 */
public static String getNullIfMissing(String key) {
    return getNullIfMissing(RESOURCE_FILE_PREFIX, key);
}

/**
 * Returns the string corresponding to the specified string in the bundle
 * file corresponding to the name <var>prefix</var>. Returns <var>key</var>
 * if <var>key</var> is either <code>null</code> or the empty string.
 * Reports <code>null</code> if <var>key</var> does not exist.
 *
 * @param prefix the bundle file name prefix
 * @param key the lookup key
 * @return the string corresponding to the specified lookup key
 * or <code>null</code> if there isn't one; if <var>key</var> is the
 * empty string, return it
 */
public static String getNullIfMissing(String prefix, String key) {
    if (key == null || prefix == null || prefix.length() == 0) return null;
    if (key.length() == 0) return "";

    String val = null;
    try {
	ResourceBundle strings = getBundle(prefix);
	val = strings.getString(key);
	if (val == null) val = "";
	else val = val.trim();
    }
    catch (MissingResourceException ex) {
	val = null;
    }
    return val;
}

protected static ResourceBundle getBundle(String prefix) {
    ResourceBundle bundle = (ResourceBundle)bundles.get(prefix);
    if (bundle == null) {
	bundle = ResourceBundle.getBundle(prefix, locale);
	bundles.put(prefix, bundle);
    }
    return bundle;
}

}
