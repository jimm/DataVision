package jimm.datavision.gui;
import jimm.datavision.PaperFormat;
import jimm.util.I18N;
import jimm.util.StringUtils;
import java.awt.Font;
import java.awt.event.*;
import java.util.List;
import java.util.Iterator;
import javax.swing.*;

/**
 * Menu creation utilities.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class MenuUtils {

public static JMenu readMenu(String key) {
    JMenu menu = new JMenu(I18N.get(I18N.MENU_FILE_PREFIX, key));
    setKeys(menu, key);		// Read .keys property; set mnemonic
    return menu;
}

public static JMenuItem readItem(ActionListener listener, String key,
				 Font font)
{
    JMenuItem item = new JMenuItem(I18N.get(I18N.MENU_FILE_PREFIX, key));
    modifyItem(item, listener, key, font);
    return item;
}

public static JCheckBoxMenuItem readCheckboxItem(ActionListener listener,
						 String key, Font font)
{
    JCheckBoxMenuItem item =
	new JCheckBoxMenuItem(I18N.get(I18N.MENU_FILE_PREFIX, key));
    modifyItem(item, listener, key, font);
    return item;
}

protected static void modifyItem(JMenuItem item, ActionListener listener,
				 String key, Font font)
{
    if (listener != null)
	item.addActionListener(listener);
    if (font != null)
	item.setFont(font);

    String val = I18N.getNullIfMissing(I18N.MENU_FILE_PREFIX, key + ".action");
    if (val != null && val.length() > 0)
	item.setActionCommand(val);

    val = I18N.getNullIfMissing(I18N.MENU_FILE_PREFIX, key + ".enabled");
    if (val != null && val.length() > 0
	&& ("false".equalsIgnoreCase(val)
	    || "no".equalsIgnoreCase(val)))
	item.setEnabled(false);

    setKeys(item, key);		// Read .keys property; set mnemonic and accel
}

/**
 * Adds a single item to a menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param menu the menu
 * @param key the menu properties file lookup key
 */
public static JMenuItem addToMenu(ActionListener listener, JMenu menu,
				  String key)
{
    return addToMenu(listener, menu, key, null);
}

/**
 * Adds a single item to a menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param menu the menu
 * @param key the menu properties file lookup key
 * @param font font; may be <code>null</code>
 */
public static JMenuItem addToMenu(ActionListener listener, JMenu menu,
				  String key, Font font)
{
    JMenuItem item = readItem(listener, key, font);
    menu.add(item);
    return item;
}

/**
 * Adds a single item to a popup menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param menu the menu
 * @param key the menu properties file lookup key
 */
public static JMenuItem addToMenu(ActionListener listener, JPopupMenu menu,
				  String key)
{
    return addToMenu(listener, menu, key, null);
}

/**
 * Adds a single item to a popup menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param menu the menu
 * @param key the menu properties file lookup key
 * @param font font; may be <code>null</code>
 */
public static JMenuItem addToMenu(ActionListener listener, JPopupMenu menu,
				  String key, Font font)
{
    JMenuItem item = readItem(listener, key, font);
    menu.add(item);
    return item;
}

/**
 * Adds a single checkbox item to a menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param menu the menu
 * @param key the menu properties file lookup key
 * @param font font; may be <code>null</code>
 */
public static JCheckBoxMenuItem addCheckboxToMenu(ActionListener listener,
						  JMenu menu, String key,
						  Font font)
{
    JCheckBoxMenuItem item = readCheckboxItem(listener, key, font);
    menu.add(item);
    return item;
}

/**
 * Adds a single checkbox item to a popup menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param menu the menu
 * @param key the menu properties file lookup key
 * @param font font; may be <code>null</code>
 */
public static JCheckBoxMenuItem addCheckboxToMenu(ActionListener listener,
						  JPopupMenu menu, String key,
						  Font font)
{
    JCheckBoxMenuItem item = readCheckboxItem(listener, key, font);
    menu.add(item);
    return item;
}

/**
 * Adds an action to a menu.
 *
 * @param menu the menu
 * @param action the action
 * @param key the menu properties file lookup key
 */
public static JMenuItem addToMenu(JMenu menu, Action action, String key) {
    JMenuItem item = menu.add(action);
    setKeys(item, key);		// Read .keys property; set mnemonic and accel
    return item;
}

protected static void setKeys(JMenuItem item, String key) {
    String keys =
	I18N.getNullIfMissing(I18N.MENU_FILE_PREFIX, key + ".keys");
    if (keys == null)
	return;

    List split = StringUtils.split(keys, " ");

    if (split.size() >= 1) {	// Mnemonic key
	key = (String)split.get(0);
	item.setMnemonic((int)key.charAt(0));
    }

    if (split.size() >= 2) {	// Accelerator key
	key = (String)split.get(1);
	int stroke = (int)key.charAt(0);
	int mask = ActionEvent.CTRL_MASK;
	if ("DEL".equals(key)) {
	    stroke = KeyEvent.VK_DELETE;
	    mask = 0;
	}
	item.setAccelerator(KeyStroke.getKeyStroke(stroke, mask));
    }
}

/**
 * Returns a new align menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param font font; may be <code>null</code>
 * @return a new menu
 */
public static JMenu buildAlignMenu(ActionListener listener, Font font)
{
    JMenu menu = readMenu("Align.menu");

    MenuUtils.addToMenu(listener, menu, "Align.menu_tops", font);
    MenuUtils.addToMenu(listener, menu, "Align.menu_middles", font);
    MenuUtils.addToMenu(listener, menu, "Align.menu_bottoms", font);
    MenuUtils.addToMenu(listener, menu, "Align.menu_lefts", font);
    MenuUtils.addToMenu(listener, menu, "Align.menu_centers", font);
    MenuUtils.addToMenu(listener, menu, "Align.menu_rights", font);
    MenuUtils.addToMenu(listener, menu, "Align.menu_snap", font);

    return menu;
}

/**
 * Returns a new size menu.
 *
 * @param listener action listener for item; may be <code>null</code>
 * @param font font; may be <code>null</code>
 * @return a new menu
 */
public static JMenu buildSizeMenu(ActionListener listener, Font font)
{
    JMenu menu = readMenu("Size.menu");

    MenuUtils.addToMenu(listener, menu, "Size.menu_same_width", font);
    MenuUtils.addToMenu(listener, menu, "Size.menu_same_height", font);
    MenuUtils.addToMenu(listener, menu, "Size.menu_same_size", font);

    return menu;
}

/**
 * Returns a new paper size menu.
 *
 * @param listener action button listener for item; may be <code>null</code>
 * @param currChoice current paper choice (its orientation and name will be
 * pre-selected)
 * @param orientationGroup a radio button group for orientations
 * @param nameGroup a radio button group for names
 * @return a new menu
 */
public static JMenu buildPaperSizeMenu(ActionListener listener,
				       PaperFormat currChoice,
				       ButtonGroup orientationGroup,
				       ButtonGroup nameGroup)
{
    JMenu menu = readMenu("MenuUtils.menu_paper_size");

    JRadioButtonMenuItem item = null;

    // Portrait
    String key = "MenuUtils.menu_paper_size_portrait";
    String str = I18N.get(I18N.MENU_FILE_PREFIX, key);
    item = new JRadioButtonMenuItem(str);

    String action =
	I18N.getNullIfMissing(I18N.MENU_FILE_PREFIX, key + ".action");
    if (action != null && action.length() > 0)
	item.setActionCommand(action);

    orientationGroup.add(item);
    if (listener != null) item.addActionListener(listener);
    if (currChoice.getOrientation() == PaperFormat.PORTRAIT)
	item.setSelected(true);
    menu.add(item);

    // Landscape
    key = "MenuUtils.menu_paper_size_landscape";
    str = I18N.get(I18N.MENU_FILE_PREFIX, key);
    item = new JRadioButtonMenuItem(str);

    action = I18N.getNullIfMissing(I18N.MENU_FILE_PREFIX, key + ".action");
    if (action != null && action.length() > 0)
	item.setActionCommand(action);

    orientationGroup.add(item);
    if (listener != null) item.addActionListener(listener);
    if (currChoice.getOrientation() == PaperFormat.LANDSCAPE)
	item.setSelected(true);
    menu.add(item);

    menu.addSeparator();

    // Paper sizes
    for (Iterator iter = PaperFormat.names(); iter.hasNext();) {
	String name = (String)iter.next();
	item = new JRadioButtonMenuItem(name);
	nameGroup.add(item);
	if (listener != null) item.addActionListener(listener);

	if (name.equals(currChoice.getName()))
	    item.setSelected(true);

	menu.add(item);
    }

    return menu;
}
}
