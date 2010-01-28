package jimm.datavision.gui;
import jimm.datavision.Scripting;
import jimm.datavision.gui.cmd.ScriptingCommand;
import jimm.util.I18N;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

/**
 * Editor dialog for a report's scripting language list and default language.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class ScriptingWin
    extends EditWin
    implements ItemListener, ActionListener, DocumentListener
{

protected static final int LANG_NAME_COLS = 16;
protected static final int CLASS_NAME_COLS = 40;

protected Scripting scripting;
protected JComboBox defaultLangMenu;
protected JTextField langName;
protected JTextField langClass;
protected JButton addLangButton;
protected JButton testLangButton;
protected String defaultLang;
protected Map languages;

/**
 * Constructor.
 *
 * @param designer the window to which this dialog belongs
 * @param scripting the scripting language information container
 */
public ScriptingWin(Designer designer, Scripting scripting) {
    super(designer, I18N.get("ScriptingWin.title"), "ScriptingCommand.name");

    this.scripting = scripting;
    defaultLang = scripting.getDefaultLanguage();
    languages = new HashMap(scripting.getLanguages()); // Make a copy

    buildWindow();
    pack();
    setVisible(true);
}

/**
 * Builds the window contents.
 */
protected void buildWindow() {
    // All edit fields
    JPanel editorPanel = buildEditor();

    // OK, Apply, Revert, and Cancel Buttons
    JPanel buttonPanel = closeButtonPanel();

    // Add values and buttons to window
    getContentPane().add(editorPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    fillEditFields();
}

protected JPanel buildEditor() {
    EditFieldLayout efl = new EditFieldLayout();

    defaultLangMenu = efl.addComboBox(I18N.get("ScriptingWin.default_lang"),
				     langChoices());
    defaultLangMenu.addItemListener(this);

    efl.skipRow();
    langName = efl.addTextField(I18N.get("ScriptingWin.add_name"), "",
				LANG_NAME_COLS);
    langClass = efl.addTextField(I18N.get("ScriptingWin.add_class"), "",
				 CLASS_NAME_COLS);

    langName.getDocument().addDocumentListener(this);
    langClass.getDocument().addDocumentListener(this);

    JPanel buttonPanel = new JPanel();
    addLangButton = new JButton(I18N.get("ScriptingWin.add_button"));
    testLangButton = new JButton(I18N.get("ScriptingWin.test_button"));

    buttonPanel.add(addLangButton);
    buttonPanel.add(testLangButton);
    efl.add("", buttonPanel);

    addLangButton.addActionListener(this);
    testLangButton.addActionListener(this);

    return efl.getPanel();
}

protected Object[] langChoices() {
    return languages.keySet().toArray();
}

/** Fill with initial values. */
protected void fillEditFields() {
    fillEditFields(defaultLang);
}

/** Fill drop-down menu and text fields based on <var>lang</var>. */
protected void fillEditFields(String lang) {
    defaultLangMenu.setSelectedItem(lang);
    langName.setText(lang);
    langClass.setText((String)languages.get(lang));
    enableButtons();
}

/** When language selected from popup, populate name and class fields. */
public void itemStateChanged(ItemEvent e) {
    fillEditFields(e.getItem().toString());
}

public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (I18N.get("ScriptingWin.add_button").equals(cmd))
	addLanguage();
    else if (I18N.get("ScriptingWin.test_button").equals(cmd))
	testLanguage();
    else
	super.actionPerformed(e);
}

protected void addLanguage() {
    String name = langName.getText().trim();
    String klass = langClass.getText().trim();

    if (languages.get(name) == null) // Only add to menu if new
	defaultLangMenu.addItem(name);
    languages.put(name, klass); // Add or replace
    fillEditFields();
}

protected void testLanguage() {
    String klass = langClass.getText().trim();

    String msg = null;
    int type;
    if (scripting.canFind(klass)) {
	msg = I18N.get("ScriptingWin.lang_ok");
	type = JOptionPane.INFORMATION_MESSAGE;
    }
    else {
	msg = I18N.get("ScriptingWin.lang_err");
	type = JOptionPane.ERROR_MESSAGE;
    }
    JOptionPane.showMessageDialog(null, msg, I18N.get("ScriptingWin.title"),
				  type);
}

public void changedUpdate(DocumentEvent e) { enableButtons(); }
public void insertUpdate(DocumentEvent e) { enableButtons(); }
public void removeUpdate(DocumentEvent e) { enableButtons(); }

protected void enableButtons() {
    boolean enable = langName.getText().trim().length() > 0
	&& langClass.getText().trim().length() > 0;
    addLangButton.setEnabled(enable);
    testLangButton.setEnabled(enable);
}

protected void doSave() {
    defaultLang = (String)defaultLangMenu.getSelectedItem();
    ScriptingCommand cmd =
	new ScriptingCommand(scripting, defaultLang, languages);
    cmd.perform();
    commands.add(cmd);
}

protected void doRevert() {
    fillEditFields();
}

}
