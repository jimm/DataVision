package jimm.properties;
import java.util.*;
import java.io.*;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

public class TranslateOMatic
    extends JFrame
    implements ActionListener, TreeSelectionListener {

// ================================================================

/**
 * A bundle association holds on to the "from" and "to" bundles, the
 * prefix name (e.g., "datavision" or "menu"), and a list of
 * exclusions---entries that should not be displayed.
 */
class BundleAssoc {

String prefix;
ResourceBundle from;
MutableResourceBundle to;
List exclusions;

BundleAssoc(String prefix, ResourceBundle from, MutableResourceBundle to) {
    this.prefix = prefix;
    this.from = from;
    this.to = to;
    exclusions = new ArrayList();
}

void addExclusion(String str) {
    exclusions.add(str);
}

boolean exclude(String str) {
    int pos = str.lastIndexOf('.');
    if (pos >= 0)
	str = str.substring(pos + 1);
    return exclusions.contains(str);
}

}

// ================================================================

/**
 * Represents a single entry change; used to remember what was being
 * edited.
 */
class Translation {
MutableResourceBundle to;
String key;

Translation(MutableResourceBundle to, String key) {
    this.to = to;
    this.key = key;
}

void save(String str) {
    to.setString(key, str.trim());
}
}

// ================================================================

/**
 * A mutable resource bundle.
 */
class MutableResourceBundle {

String prefix;
String language;
String country;
ResourceBundle bundle;
HashMap newValues;

MutableResourceBundle(String prefix, String language, String country) {
    this.prefix = prefix;
    this.language = language;
    this.country = country;
    bundle = ResourceBundle.getBundle(prefix, new Locale(language, country));
    newValues = new HashMap();
}

String getString(String key) {
    String str = (String)newValues.get(key);
    if (str == null)
	str = bundle.getString(key);
    return str;
}

void setString(String key, String value) {
    if (value != null) {
	value = value.trim();
	if (value.length() == 0) // Store null for empty strings
	    value = null;
    }
    newValues.put(key, value);
}

String fileName() {
    return prefix + "_" + language + "_" + country + ".properties";
}
}

// ================================================================

static final Dimension WINDOW_SIZE = new Dimension(600, 350);
static final Dimension MIN_SIZE = new Dimension(100, 50);
static final int START_DIVIDER_LOCATION = 150;
static final int TEXT_FIELD_SIZE = 40;

HashMap bundles;
ResourceBundle settings;
DefaultTreeModel model;
JTree tree;
JLabel fromField;
JTextField toField;
Translation xlation;
String encoding;

public TranslateOMatic(String localeLanguage, String localeCountry,
		       String encodingName)
{
    super("Translate-O-Matic");

    bundles = new HashMap();
    settings = ResourceBundle.getBundle("translate");

    encoding = encodingName;
    if (encoding == null)
	encoding = settings.getString("default_encoding");
    else
	encoding = encoding.toUpperCase();

    buildModel(localeLanguage, localeCountry);
    buildWindow();

    // Make sure we close the window when the user asks to close the window.
    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) { maybeClose(); }
    });

    pack();
    show();
}

protected void buildModel(String localeLanguage, String localeCountry) {
    List prefixes = split(settings.getString("bundles"));
    for (Iterator iter = prefixes.iterator(); iter.hasNext(); ) {
	String prefix = (String)iter.next();
	ResourceBundle from = ResourceBundle.getBundle(prefix);
	MutableResourceBundle to =
	    new MutableResourceBundle(prefix, localeLanguage, localeCountry);

	BundleAssoc assoc = new BundleAssoc(prefix, from, to);
	bundles.put(prefix, assoc);

	// Exclusions
	try {
	    String exclusions = settings.getString("exclusions." + prefix);
	    List l = split(exclusions);
	    for (Iterator iter2 = l.iterator(); iter2.hasNext(); )
		assoc.addExclusion(iter2.next().toString());
	}
	catch (MissingResourceException mre) {
	    // Ignore missing resources
	}
    }

    DefaultMutableTreeNode top = new DefaultMutableTreeNode();
    createNodes(top);
    model = new DefaultTreeModel(top);
}

/**
 * Creates tree nodes.
 *
 * @param top top-level tree node
 */
protected void createNodes(DefaultMutableTreeNode top) {
    for (Iterator iter = bundles.values().iterator(); iter.hasNext(); )
	createNode(top, (BundleAssoc)iter.next());
}

protected void createNode(DefaultMutableTreeNode top, BundleAssoc assoc) {
    String name = assoc.prefix.substring(0, 1).toUpperCase()
	+ assoc.prefix.substring(1);
    DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(name);
    top.add(categoryNode);

    HashMap subnodes = new HashMap();
    for (Enumeration e = assoc.from.getKeys(); e.hasMoreElements(); ) {
	String key = e.nextElement().toString();
	if (assoc.exclude(key))
	    continue;

	String firstPart = firstPartOf(key);
	if (firstPart.equals(key))
	    categoryNode.add(new DefaultMutableTreeNode(key));
	else {
	    DefaultMutableTreeNode subnode =
		(DefaultMutableTreeNode)subnodes.get(firstPart);
	    if (subnode == null) {
		subnode = new DefaultMutableTreeNode(firstPart);
		subnodes.put(firstPart, subnode);
		categoryNode.add(subnode);
	    }
	    subnode.add(new DefaultMutableTreeNode(key));
	}
    }
}

/**
 * Builds the window components.
 */
protected void buildWindow() {
    buildMenuBar();
    buildContents();
    getRootPane().setPreferredSize(WINDOW_SIZE);
}

/**
 * Builds the window menu bar.
 */
protected void buildMenuBar() {
    JMenuBar bar = new JMenuBar();
    bar.add(buildFileMenu());
    getRootPane().setJMenuBar(bar);
}

/**
 * Builds and returns the "File" menu.
 *
 * @return a menu
 */
protected JMenu buildFileMenu() {
    JMenu m = new JMenu("File");
    JMenuItem i = new JMenuItem("Quit");
    i.addActionListener(this);
    m.add(i);
    return m;
}

/**
 * Builds window contents.
 */
protected void buildContents() {
    buildTree();

    JScrollPane treeScrollPane = new JScrollPane(tree);
    treeScrollPane.setMinimumSize(MIN_SIZE);

    Box box = Box.createVerticalBox();
    box.add(fromField = new JLabel(" "));
    fromField.setHorizontalAlignment(SwingConstants.LEFT);
    box.add(toField = new JTextField(TEXT_FIELD_SIZE));
    JPanel p = new JPanel();
    p.add(box);
    p.setMinimumSize(MIN_SIZE);

    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				   treeScrollPane, p);
    sp.setDividerLocation(START_DIVIDER_LOCATION);
    sp.setPreferredSize(WINDOW_SIZE);

    getContentPane().add(sp);
}

protected void buildTree() {
    tree = new JTree(model);
    tree.setRootVisible(false);
    tree.getSelectionModel()
	.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // Listen for selection changes
    tree.addTreeSelectionListener(this);
}

public void valueChanged(TreeSelectionEvent e) {
    // Save previously selected value
    if (xlation != null)
	xlation.save(toField.getText());

    TreePath path = e.getPath();
    if (path == null)
	return;

    // We ignore the 0'th entry which is an unnamed, unused root element.
    Object[] elements = path.getPath();
    if (elements.length < 3)
	return;

    String bundleName = elements[1].toString().toLowerCase();
    BundleAssoc assoc = (BundleAssoc)bundles.get(bundleName);

    String key = elements[elements.length - 1].toString();
    String fromString = null, toString = null;
    try {
	fromString = assoc.from.getString(key);
	toString = assoc.to.getString(key);

	xlation = new Translation(assoc.to, key);

	fromField.setText(fromString);
	toField.setText(toString);
    }
    catch (MissingResourceException mre) {
	xlation = null;

	fromField.setText(" ");
	toField.setText("");
    }
}

/**
 * Handles user actions.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if ("Quit".equals(cmd)) maybeClose();
}

/**
 * Returns a string that can be written as a resource bundle value.
 */
String escape(String str) {
    if (str == null || str.length() == 0)
	return str;

    StringBuffer buf = new StringBuffer();

    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	switch (c) {
	case '=':
	case ':':
	    buf.append('\\');
	    buf.append(c);
	    break;
	case '\n':
	    buf.append("\\n");
	    break;
	case '\r':
	    buf.append("\\r");
	    break;
	case '\t':
	    buf.append("\\t");
	    break;
	default:
	    if (i == 0 && Character.isWhitespace(c))
		buf.append('\\');
	    buf.append(c);
	    break;
	}
    }

    return buf.toString();
}

/**
 * Save the file and close the window.
 */
protected void save() {
    try {
	for (Iterator iter = bundles.values().iterator(); iter.hasNext(); ) {
	    BundleAssoc assoc = (BundleAssoc)iter.next();
	    File f = new File(assoc.to.fileName());
	    PrintWriter out =
		new PrintWriter(new OutputStreamWriter(new FileOutputStream(f),
						       encoding));

	    for (Enumeration e = assoc.from.getKeys(); e.hasMoreElements(); ) {
		String key = e.nextElement().toString();
		String val = escape(assoc.to.getString(key));
		if (val != null)
		    out.println(key + " = " + val);
	    }

	    out.flush();
	    out.close();
	}
    }
    catch (IOException ioe) {
	System.err.println(ioe.toString());
    }
}

/**
 * Save the file and close the window.
 */
protected void maybeClose() {
    // Save currently selected value
    if (xlation != null)
	xlation.save(toField.getText());

    save();
    dispose();
    System.exit(0);
}

protected String firstPartOf(String key) {
    int pos = key.indexOf('.');
    return (pos == -1) ? key : key.substring(0, pos);
}

protected List split(String str) {
    if (str == null)
	return null;

    ArrayList list = new ArrayList();

    int subStart, afterDelim = 0;
    while ((subStart = str.indexOf(',', afterDelim)) != -1) {
	list.add(str.substring(afterDelim, subStart).trim());
	afterDelim = subStart + 1;
    }
    if (afterDelim <= str.length())
	list.add(str.substring(afterDelim).trim());

    return list;
}

public static void main(String[] args) {
    try {
	if (args[0].toLowerCase() == "en" && args[1].toUpperCase() == "US") {
	    System.err.println("Can't modify en_US files");
	    System.exit(1);
	}
	new TranslateOMatic(args[0], args[1],
			    (args.length >= 3) ? args[2] : null);
    }
    catch (Exception e) {
	e.printStackTrace();
    }
}

}
