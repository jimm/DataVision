package jimm.datavision.gui.cmd;
import jimm.datavision.Scripting;
import jimm.util.I18N;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs changes to a report's scripting language information.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ScriptingCommand extends CommandAdapter {

protected Scripting scripting;
protected String newDefaultLang;
protected Map newLangs;
protected String origDefaultLang;
protected Map origLangs;

public ScriptingCommand(Scripting scripting, String newDefaultLang,
			Map newLangs)
{
    super(I18N.get("ScriptingCommand.name"));

    this.scripting = scripting;
    this.newDefaultLang = newDefaultLang;
    this.newLangs = newLangs;

    origDefaultLang = scripting.getDefaultLanguage();
    origLangs = new HashMap(scripting.getLanguages()); // Make a copy
}

public void perform() {
    scripting.setDefaultLanguage(newDefaultLang);
    scripting.replaceLanguages(newLangs);
}

public void undo() {
    scripting.setDefaultLanguage(origDefaultLang);
    scripting.replaceLanguages(origLangs);
}

}
