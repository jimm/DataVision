package jimm.datavision.gui;
import jimm.datavision.Report;
import jimm.util.I18N;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import java.awt.BorderLayout;

/**
 * This is the abstract superclass of windows used for editing paragraphs of
 * scripting code such as formulas
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class ScriptEditorWin extends CodeEditorWin {

protected JComboBox languageMenu;

/**
 * Constructor.
 *
 * @param designer the design window to which this dialog belongs
 * @param report the report
 * @param initialText the initial text to edit
 * @param title the window title
 * @param errorSuffixKey I18N lookup key for error text suffix; may be
 * <code>null</code>
 * @param errorTitleKey I18N lookup key for error window title; may be
 * <code>null</code>
 */
public ScriptEditorWin(Designer designer, Report report, String initialText,
		       String title, String errorSuffixKey,
		       String errorTitleKey)
{
    super(designer, report, initialText, title, errorSuffixKey, errorTitleKey);
}

/** Returns language name selected in drop-down menu. */
protected String getLanguage() {
    return (String)languageMenu.getSelectedItem();
}

/**
 * Sets dropdown menu's language.
 *
 * @param lang a scripting language name
 */
protected void setLanguage(String lang) {
    languageMenu.setSelectedItem(lang);
}

/**
 * Adds a scripting language dropdown.
 */
protected void buildWindow(Report report, String initialText) {
    getContentPane().add(northPanel(report), BorderLayout.NORTH);

    super.buildWindow(report, initialText);
}

protected JComponent northPanel(Report report) {
    EditFieldLayout efl = new EditFieldLayout();
    Object[] langs = report.getScripting().getLanguages().keySet().toArray();
    languageMenu = efl.addComboBox(I18N.get("ScriptEditorWin.scripting_lang"),
				   langs);
    return efl.getPanel();
}

}
