package jimm.datavision;
import jimm.util.XMLWriter;
import java.util.*;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFException;

/**
 * Bean Scripting Framework management.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class Scripting implements Writeable {

protected static final String DEFAULT_LANGUAGE = "Ruby";
protected static final String DEFAULT_CLASS =
    "org.jruby.javasupport.bsf.JRubyEngine";

protected Report report;
protected String defaultLanguage;
protected Map languages;
protected Map managers;

public Scripting(Report report) {
    this.report = report;
    defaultLanguage = DEFAULT_LANGUAGE;
    languages = new HashMap();
    languages.put(DEFAULT_LANGUAGE, DEFAULT_CLASS);
    managers = new HashMap();
}

/** Returns an immutable copy of the map of languages. */
public Map getLanguages() { return Collections.unmodifiableMap(languages); }

/** Adds a language to our list. */
public void addLanguage(String language, String className) {
    languages.put(language, className);
}

/**
 * Replace the contents of our language map with the contents of
 * <var>langs</var>.
 *
 * @param langs maps language names to class names
 */
public void replaceLanguages(Map langs) {
    languages.clear();
    languages.putAll(langs);
}

/** Returns the default language name. */
public String getDefaultLanguage() { return defaultLanguage; }

/** Sets the default language name. */
public void setDefaultLanguage(String language) {
    if (language == null)
	throw new IllegalArgumentException("default language may not be null");
    defaultLanguage = language;
}

/**
 * Returns true if we can load class <var>klass</var>.
 */
public boolean canFind(String klass) {
    BSFManager manager = new BSFManager();
    boolean found = false;
    try {
	manager.getClassLoader().loadClass(klass);
	found = true;
    }
    catch (ClassNotFoundException e) {}
    return found;
}

/**
 * Evaluates an <var>evalString</var> using <var>language</var> and returns
 * the results.
 *
 * @param language the language to use
 * @param evalString the string to evaluate
 * @param name a name (for example, a formula name) to display with error
 * messages
 * @return the result
 */
public Object eval(String language, String evalString, String name)
    throws BSFException
{
    return getBsfManager(language).eval(language, name, 1, 1, evalString);
}

/** Returns BSFManager for the default language. */
public BSFManager getBsfManager() throws BSFException {
    return getBsfManager(defaultLanguage);
}

/** Returns BSFManager for <var>language</var>. */
public BSFManager getBsfManager(String language) throws BSFException {
    BSFManager manager = (BSFManager)managers.get(language);
    if (manager == null) {
	manager = new BSFManager();
	manager.declareBean("report", report, Report.class);
	BSFManager.registerScriptingEngine(language,
					   (String)languages.get(language), null);
	managers.put(language, manager);
    }
    return manager;
}

public void writeXML(XMLWriter out) {
    out.startElement("bean-scripting-framework");
    out.attr("default-language", defaultLanguage);

    for (Iterator iter = languages.keySet().iterator(); iter.hasNext(); ) {
	String language = (String)iter.next();
	out.startElement("language");
	out.attr("name", language);
	out.attr("class", languages.get(language));
	out.endElement();
    }

    out.endElement();
}

}
