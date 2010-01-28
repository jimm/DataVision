package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.field.ColumnField;
import jimm.datavision.field.SpecialField;
import jimm.datavision.gui.cmd.FPDeleteCommand;
import jimm.datavision.source.DataSource;
import jimm.datavision.source.Column;
import jimm.datavision.source.Table;
import jimm.datavision.gui.Designer;
import jimm.datavision.gui.cmd.NameableRenameCommand;
import jimm.datavision.gui.cmd.FPCutCommand;
import jimm.util.I18N;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * A window that lets the user drag any field available onto the report and
 * create, edit, delete, and rename formulas, parameters, and user columns.
 * <p>
 * Uses a {@link FieldPickerTree}. The classes used to store information about
 * leaf nodes are subclasses of {@link FPLeafInfo} and are found in
 * FPLeafInfo.java.
 *
 * @see FPTableInfo
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
class FieldPickerWin
    extends JDialog
    implements ActionListener, TreeSelectionListener, TreeWillExpandListener,
	       Observer
{

public static final int REPORT_DATABASE_FIELDS = 0;
public static final int FORMULAS = 1;
public static final int PARAMETERS = 2;
public static final int USERCOLS = 3;
public static final int SPECIAL_FIELDS = 4;
public static final int ALL_DATABASE_FIELDS = 5;
//  public static final int GROUP_NAME_FIELDS = XXXX;

protected Report report;
protected Designer designer;
protected JMenuItem cutItem, editFormulaItem, renameFormulaItem,
    editParameterItem, renameParameterItem,
    editUserColumnItem, renameUserColumnItem, 
    deleteItem;
protected FieldPickerTree tree;
protected DefaultTreeModel treeModel;
protected DefaultMutableTreeNode formulaCategoryNode;
protected DefaultMutableTreeNode parameterCategoryNode;
protected DefaultMutableTreeNode userColumnCategoryNode;
protected DefaultMutableTreeNode selectedNode;
protected FPLeafInfo selectedInfo;
protected Comparator nameComparator;

/**
 * Constructor.
 *
 * @param designer the design window to which this dialog belongs
 * @param report the report
 * @param startingType the index of the starting type to display
 */
FieldPickerWin(Designer designer, Report report, int startingType) {
    super(designer.getFrame(), I18N.get("FieldPickerWin.title"));
    this.report = report;
    this.designer = designer;

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    nameComparator = new Comparator() {
	public int compare(Object o1, Object o2) {
	    String name1 = ((Nameable)o1).getName();
	    String name2 = ((Nameable)o2).getName();
	    return name1.compareTo(name2);
	}};

    buildWindow(startingType);

    pack();
    show();
}

public void update(Observable o, Object arg) {
    // Primitive and overkill, but it works.
    tree.repaint();
}

/**
 * Builds the contents of the window.
 *
 * @param startingType the index of the starting type to display
 */
protected void buildWindow(int startingType) {
    buildMenuBar();

    DefaultMutableTreeNode top = new DefaultMutableTreeNode();
    createNodes(top);
    treeModel = new DefaultTreeModel(top);

    tree = new FieldPickerTree(treeModel);
    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode
	(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // Make specified starting type visible and expanded
    tree.expandRow(startingType);

    // Listen for selection changes and node expansions
    tree.addTreeSelectionListener(this);
    tree.addTreeWillExpandListener(this);

    // Create the scroll pane and add the tree to it. 
    JScrollPane treeView = new JScrollPane(tree);
    treeView.setMinimumSize(new Dimension(100, 100));
    treeView.setPreferredSize(new Dimension(200, 300));

    getContentPane().add(treeView, BorderLayout.CENTER);
}

/**
 * Builds the window menu bar.
 */
protected void buildMenuBar() {
    JMenuBar bar = new JMenuBar();
    bar.add(buildFileMenu());
    bar.add(buildEditMenu());
    bar.add(buildFieldMenu());
    setJMenuBar(bar);
}

/**
 * Builds and returns the "File" menu.
 *
 * @return a menu
 */
protected JMenu buildFileMenu() {
    JMenu menu = MenuUtils.readMenu("FieldPickerWin.menu_file");
    MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_file_close");
    return menu;
}

/**
 * Builds and returns the "Edit" menu.
 *
 * @return a menu
 */
protected JMenu buildEditMenu() {
    JMenu menu = MenuUtils.readMenu("FieldPickerWin.menu_edit");

    MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_edit_undo");
    MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_edit_redo");
    menu.addSeparator();
    cutItem = MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_edit_cut");
    MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_edit_copy");
    MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_edit_paste");
    deleteItem =
	MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_edit_delete");

    return menu;
}

/**
 * Builds and returns the "Field" menu.
 *
 * @return a menu
 */
protected JMenu buildFieldMenu() {
    JMenu menu = MenuUtils.readMenu("FieldPickerWin.menu_field");

    MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_field_new_formula");
    editFormulaItem =
	MenuUtils.addToMenu(this, menu,
			    "FieldPickerWin.menu_field_edit_formula");
    renameFormulaItem =
	MenuUtils.addToMenu(this, menu,
			    "FieldPickerWin.menu_field_rename_formula");

    menu.addSeparator();

    MenuUtils.addToMenu(this, menu, "FieldPickerWin.menu_field_new_param");
    editParameterItem =
	MenuUtils.addToMenu(this, menu,
			    "FieldPickerWin.menu_field_edit_param");
    renameParameterItem =
	MenuUtils.addToMenu(this, menu,
			    "FieldPickerWin.menu_field_rename_param");

    menu.addSeparator();

    MenuUtils.addToMenu(this, menu,
			"FieldPickerWin.menu_field_new_usercol");
    editUserColumnItem =
	MenuUtils.addToMenu(this, menu,
			    "FieldPickerWin.menu_field_edit_usercol");
    renameUserColumnItem =
	MenuUtils.addToMenu(this, menu,
			    "FieldPickerWin.menu_field_rename_usercol");

    return menu;
}

/**
 * Creates tree nodes.
 *
 * @param top top-level tree node
 */
protected void createNodes(DefaultMutableTreeNode top) {
    createUsedDatabaseTables(top);
    createFormulas(top);
    createParameters(top);
    createUserColumns(top);
    createSpecialFields(top);
    createAllDatabaseTables(top);
}

/**
 * Creates nodes representing tables and columns for columns used by report.
 *
 * @param top top-level tree node
 */
protected void createUsedDatabaseTables(DefaultMutableTreeNode top) {
    DefaultMutableTreeNode categoryNode =
	new DefaultMutableTreeNode(I18N.get("FieldPickerWin.db_fields"));
    top.add(categoryNode);

    // Store list of tables actually used by the report in a sorted set.
    final TreeSet tables = new TreeSet(nameComparator);
    final TreeSet noTableCols = new TreeSet(nameComparator);

    // Walk the list of all columns used by the report, adding the table
    // to the sorted set of tables.
    report.withFieldsDo(new FieldWalker() {
	public void step(Field f) {
	    if (f instanceof ColumnField) {
		Column col = ((ColumnField)f).getColumn();
		Table t = col.getTable();
		if (t == null)
		    noTableCols.add(col);
		else
		    tables.add(t);
	    }
	}
	});

    // Add tables and columns under tables
    for (Iterator iter = tables.iterator(); iter.hasNext(); )
	addTableNode(categoryNode,  (Table)iter.next());

    // Add colums that have no table
    for (Iterator iter = noTableCols.iterator(); iter.hasNext(); ) {
	Column column = (Column)iter.next();
	ColumnInfo info = new ColumnInfo(column, designer);
	categoryNode.add(new DefaultMutableTreeNode(info, false));
    }
}

/**
 * Creates nodes representing formula fields.
 *
 * @param top top-level tree node
 */
protected void createFormulas(DefaultMutableTreeNode top) {
    formulaCategoryNode =
	new DefaultMutableTreeNode(I18N.get("FieldPickerWin.formulas"));
    top.add(formulaCategoryNode);

    TreeSet formulas = new TreeSet(nameComparator);

    for (Iterator iter = report.formulas(); iter.hasNext(); )
	formulas.add(iter.next());

    for (Iterator iter = formulas.iterator(); iter.hasNext(); ) {
	Formula f = (Formula)iter.next();
	FormulaInfo info = new FormulaInfo(report, f, designer);
	formulaCategoryNode.add(new DefaultMutableTreeNode(info));
	f.addObserver(this);
    }
}

/**
 * Creates nodes representing parameter fields.
 *
 * @param top top-level tree node
 */
protected void createParameters(DefaultMutableTreeNode top) {
    parameterCategoryNode =
	new DefaultMutableTreeNode(I18N.get("FieldPickerWin.parameters"));
    top.add(parameterCategoryNode);

    TreeSet parameters = new TreeSet(nameComparator);

    for (Iterator iter = report.parameters(); iter.hasNext(); )
	parameters.add(iter.next());

    for (Iterator iter = parameters.iterator(); iter.hasNext(); ) {
	Parameter p = (Parameter)iter.next();
	ParameterInfo info = new ParameterInfo(report, p, designer);
	parameterCategoryNode.add(new DefaultMutableTreeNode(info));
	p.addObserver(this);
    }
}

/**
 * Creates nodes representing user column fields.
 *
 * @param top top-level tree node
 */
protected void createUserColumns(DefaultMutableTreeNode top) {
    userColumnCategoryNode =
	new DefaultMutableTreeNode(I18N.get("FieldPickerWin.usercols"));
    top.add(userColumnCategoryNode);

    TreeSet usercols = new TreeSet(nameComparator);

    for (Iterator iter = report.userColumns(); iter.hasNext(); )
	usercols.add(iter.next());

    for (Iterator iter = usercols.iterator(); iter.hasNext(); ) {
	UserColumn uc = (UserColumn)iter.next();
	UserColumnInfo info = new UserColumnInfo(report, uc, designer);
	userColumnCategoryNode.add(new DefaultMutableTreeNode(info));
	uc.addObserver(this);
    }
}

/**
 * Creates nodes representing each possible special field.
 *
 * @param top top-level tree node
 */
protected void createSpecialFields(DefaultMutableTreeNode top) {
    DefaultMutableTreeNode categoryNode =
	new DefaultMutableTreeNode(I18N.get("FieldPickerWin.specials"));
    top.add(categoryNode);

    HashMap strs = SpecialField.specialFieldNames();
    TreeSet sortedKeys = new TreeSet(strs.keySet());

    for (Iterator iter = sortedKeys.iterator(); iter.hasNext(); ) {
	String key = (String)iter.next();
	String val = (String)strs.get(key);
	key = SpecialField.TYPE_STRING + ':' + key;
	SpecialInfo info = new SpecialInfo(val, key, designer);
					   
	DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
	categoryNode.add(node);
    }
}

/**
 * Creates notes representing tables and columns for all tables in the
 * database.
 *
 * @param top top-level tree node
 */
protected void createAllDatabaseTables(DefaultMutableTreeNode top) {
    DefaultMutableTreeNode categoryNode =
	new DefaultMutableTreeNode(I18N.get("FieldPickerWin.all"));
    top.add(categoryNode);

    // Store list of tables actually used by the report in a sorted set.
    final TreeSet tables = new TreeSet(nameComparator);
    final TreeSet noTableCols = new TreeSet(nameComparator);

    // Walk data source's list of tables. If there are no tables for the
    // data source, then instead add all columns to the noTableCols set.
    DataSource source = report.getDataSource();
    Iterator iter = source.tables();
    if (iter != null) {
	while (iter.hasNext())
	    tables.add(iter.next());
    }

    if (tables.isEmpty()) {
	for (iter = source.columns(); iter.hasNext(); )
	    noTableCols.add(iter.next());
    }

    // Add nodes for tables and columns under tables
    for (iter = tables.iterator(); iter.hasNext(); )
	addTableNode(categoryNode, (Table)iter.next());

    // Add nodes for columns that have no table
    for (iter = noTableCols.iterator(); iter.hasNext(); ) {
	Column column = (Column)iter.next();
	ColumnInfo info = new ColumnInfo(column, designer);
	categoryNode.add(new DefaultMutableTreeNode(info, false));
    }
}

/**
 * Creates and adds a node representing a data source table. The node is
 * given one dummy node that will be removed when the table node loads its
 * column nodes.
 *
 * @param categoryNode the parent node
 * @param table the database table
 */
protected void addTableNode(DefaultMutableTreeNode categoryNode, Table table) {
    FPTableInfo info = new FPTableInfo(table, designer);
    DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(info);
    info.setTableNode(tableNode);
    categoryNode.add(tableNode);

    // Add a dummy node that will be removed when the table node
    // loads its column nodes.
    tableNode.add(new DefaultMutableTreeNode(""));
}

/**
 * Opens a name editor to (re)name a nameable object. Returns <code>true</code>
 * if the user clicked OK, <code>false</code> if the user clicked Cancel.
 *
 * @param nameable a nameable object
 * @param editTitleKey I18N lookup key for "edit" title
 * @param newTitleKey I18N lookup key for "new" title
 * @param promptKey I18N lookup key for prompt
 * @param unnamedKey I18N lookup key for "unnamed" name
 * @return <code>true</code> if the user clicked OK, <code>false</code>
 * if the user clicked Cancel
 */
protected boolean rename(Nameable nameable, String newTitleKey,
			 String editTitleKey, String promptKey,
			 String unnamedKey)
{
    String oldName = nameable.getName();
    String title = (oldName == null || oldName.length() == 0)
	? I18N.get(newTitleKey) : I18N.get(editTitleKey);
    String name = new AskStringDialog((JFrame)this.getOwner(), title,
				      I18N.get(promptKey), oldName)
	.getString();

    if (name == null)		// User cancelled
	return false;

    if (name.length() == 0)
	name = I18N.get(unnamedKey);
    designer.performCommand(new NameableRenameCommand(nameable, oldName,
						       name));
    return true;
}

/**
 * Adds a newly created editable object to the tree, makes it visible,
 * and opens its editor.
 *
 * @param info describes what is being added to add to the tree
 * @param categoryNode where in the tree to put the new item
 */
protected void addEditableToTree(FPLeafInfo info,
				 DefaultMutableTreeNode categoryNode)
{
    // Add to tree
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
    treeModel.insertNodeInto(node, categoryNode, categoryNode.getChildCount());

    // Make leaf visible and make it the current selection
    TreePath path = new TreePath(node.getPath());
    tree.scrollPathToVisible(path);
    tree.setSelectionPath(path);

    editSelection();		// Open editor
}


/**
 * Creates a new formula, adds it to the report and the tree, and opens
 * the formula editor.
 */
protected void newFormula() {
    // Create formula and let user enter name
    Formula f = new Formula(null, report, "", "");
    if (renameFormula(f) == false)
	return;			// User cancelled

    // Add to report and to tree and open editor
    report.addFormula(f);
    addEditableToTree(new FormulaInfo(report, f, designer),
		      formulaCategoryNode);
}

/**
 * Opens a name editor to rename the currently select formula. Returns
 * <code>true</code> if the user clicked OK, <code>false</code> if the
 * user clicked Cancel.
 *
 * @return <code>true</code> if the user clicked OK, <code>false</code>
 * if the user clicked Cancel
 */
protected boolean renameFormula() {
    return renameFormula((Formula)selectedInfo.getLeaf());
}

/**
 * Opens a name editor to (re)name a formula. Returns <code>true</code>
 * if the user clicked OK, <code>false</code> if the user clicked Cancel.
 *
 * @param f a formula
 * @return <code>true</code> if the user clicked OK, <code>false</code>
 * if the user clicked Cancel
 */
protected boolean renameFormula(Formula f) {
    return rename(f, "FieldPickerWin.new_formula_name_title",
		  "FieldPickerWin.edit_formula_name_title",
		  "FieldPickerWin.formula_name_prompt",
		  "FieldPickerWin.unnamed_formula");
}

/**
 * Opens an editor on the currently selected item.
 */
protected void editSelection() {
    selectedInfo.openEditor();
}

/**
 * Creates a new parameter, adds it to the report and the tree, and opens
 * the parameter editor.
 */
protected void newParameter() {
    // Create parameter and let user edit it.
    Parameter p = new Parameter(null, report);
    if (renameParameter(p) == false)
	return;			// User cancelled

    // Add to report and to tree and open editor
    report.addParameter(p);
    addEditableToTree(new ParameterInfo(report, p, designer),
		      parameterCategoryNode);
}

/**
 * Opens a name editor to rename the currently select parameter. Returns
 * <code>true</code> if the user clicked OK, <code>false</code> if the
 * user clicked Cancel.
 *
 * @return <code>true</code> if the user clicked OK, <code>false</code>
 * if the user clicked Cancel
 */
protected boolean renameParameter() {
    return renameParameter((Parameter)selectedInfo.getLeaf());
}

/**
 * Opens a name editor to (re)name a parameter. Returns <code>true</code>
 * if the user clicked OK, <code>false</code> if the user clicked Cancel.
 *
 * @param p a parameter
 * @return <code>true</code> if the user clicked OK, <code>false</code>
 * if the user clicked Cancel
 */
protected boolean renameParameter(Parameter p) {
    return rename(p, "FieldPickerWin.new_param_name_title",
		  "FieldPickerWin.edit_param_name_title",
		  "FieldPickerWin.param_name_prompt",
		  "FieldPickerWin.unnamed_parameter");
}

/**
 * Creates a new user column, adds it to the report and the tree, and opens
 * the user column editor.
 */
protected void newUserColumn() {
    // Create user column and let user enter name
    UserColumn uc = new UserColumn(null, report, "");
    if (renameUserColumn(uc) == false)
	return;			// User cancelled

    // Add to report and to tree and open editor
    report.addUserColumn(uc);
    addEditableToTree(new UserColumnInfo(report, uc, designer),
		      userColumnCategoryNode);
}

/**
 * Opens a name editor to rename the currently select user column. Returns
 * <code>true</code> if the user clicked OK, <code>false</code> if the
 * user clicked Cancel.
 *
 * @return <code>true</code> if the user clicked OK, <code>false</code>
 * if the user clicked Cancel
 */
protected boolean renameUserColumn() {
    return renameUserColumn((UserColumn)selectedInfo.getLeaf());
}

/**
 * Opens a name editor to (re)name a user column. Returns <code>true</code>
 * if the user clicked OK, <code>false</code> if the user clicked Cancel.
 *
 * @param f a user column
 * @return <code>true</code> if the user clicked OK, <code>false</code>
 * if the user clicked Cancel
 */
protected boolean renameUserColumn(UserColumn f) {
    return rename(f, "FieldPickerWin.new_usercol_name_title",
		  "FieldPickerWin.edit_usercol_name_title",
		  "FieldPickerWin.usercol_name_prompt",
		  "FieldPickerWin.unnamed_usercol");
}

/**
 * Handles user actions. Actions are only allowed when legal. For example,
 * the "Cut" menu item will only be enabled when a delete operation is
 * possible.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    // File menu
    if ("close".equals(cmd)) dispose();

    // Edit menu
    else if ("cut".equals(cmd))
	designer.performCommand(new FPCutCommand(report, designer, tree,
						  selectedNode));

    // Field menu
    else if ("new_formula".equals(cmd)) newFormula();
    else if ("edit_formula".equals(cmd)) editSelection();
    else if ("rename_formula".equals(cmd)) renameFormula();
    else if ("new_parameter".equals(cmd)) newParameter();
    else if ("edit_parameter".equals(cmd)) editSelection();
    else if ("rename_parameter".equals(cmd)) renameParameter();
    else if ("new_user_column".equals(cmd)) newUserColumn();
    else if ("edit_user_column".equals(cmd)) editSelection();
    else if ("rename_user_column".equals(cmd)) renameUserColumn();
    else if ("delete".equals(cmd))
	designer.performCommand(new FPDeleteCommand(report, designer, tree,
						    selectedNode));
}

/**
 * Modifies the menu in response to a change in the tree's selection.
 */
public void valueChanged(TreeSelectionEvent e) {
    if (e.isAddedPath()) {
	selectedNode
	    = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
	Object obj = selectedNode.getUserObject();
	if (obj instanceof FPLeafInfo)
	    selectedInfo = (FPLeafInfo)obj;
	else
	    selectedInfo = null;

	if (selectedInfo == null) {
	    cutItem.setEnabled(false);
	    editFormulaItem.setEnabled(false);
	    editParameterItem.setEnabled(false);
	    editUserColumnItem.setEnabled(false);
	    renameFormulaItem.setEnabled(false);
	    renameParameterItem.setEnabled(false);
	    renameUserColumnItem.setEnabled(false);
            deleteItem.setEnabled(false);
	}
	else {
	    boolean isFormula = selectedInfo instanceof FormulaInfo;
	    boolean isParameter = selectedInfo instanceof ParameterInfo;
	    boolean isUserColumn = selectedInfo instanceof UserColumnInfo;

	    cutItem.setEnabled(selectedInfo.isDeletable());
	    deleteItem.setEnabled(selectedInfo.isDeletable());
	    editFormulaItem.setEnabled(isFormula);
	    editParameterItem.setEnabled(isParameter);
	    editUserColumnItem.setEnabled(isUserColumn);
	    renameFormulaItem.setEnabled(isFormula);
	    renameParameterItem.setEnabled(isParameter);
	    renameUserColumnItem.setEnabled(isUserColumn);
	}

    }
    else {
	selectedInfo = null;
	cutItem.setEnabled(false);
	editFormulaItem.setEnabled(false);
	editParameterItem.setEnabled(false);
	editUserColumnItem.setEnabled(false);
	renameFormulaItem.setEnabled(false);
	renameParameterItem.setEnabled(false);
	renameUserColumnItem.setEnabled(false);
    }
}

public void treeWillExpand(TreeExpansionEvent e) {
    DefaultMutableTreeNode node
	= (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
    Object obj = node.getUserObject();
    if (obj instanceof FPTableInfo)
	((FPTableInfo)obj).loadColumns();
}

public void treeWillCollapse(TreeExpansionEvent e) {
}

}
