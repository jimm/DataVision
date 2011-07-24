package jimm.util;

/**
 * Used by {@link StringUtils#replaceDelimited} for replacing delimited
 * strings within one string with other strings.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public interface Replacer {

/**
 * Given a string, returns any object, including <code>null</code>. How's
 * that for generic?
 *
 * @param str a string (surprise!)
 */
public Object replace(String str);

}
