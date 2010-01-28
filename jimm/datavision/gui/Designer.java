package jimm.datavision.gui;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.source.DataSource;
import jimm.datavision.layout.swing.SwingLE;
import jimm.datavision.gui.sql.*;
import jimm.datavision.gui.cmd.*;
import jimm.util.I18N;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;

/**
 * The abstract superclass of {@link Report} designer windows and applets.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class Designer implements ActionListener, Observer {

public static final int GRID_SIZE = 8;

public static final int ALIGN_TOP = 0;
public static final int ALIGN_MIDDLE = 1;
public static final int ALIGN_BOTTOM = 2;
public static final int ALIGN_LEFT = 3;
public static final int ALIGN_CENTER = 4;
public static final int ALIGN_RIGHT = 5;
public static final int ALIGN_SNAP_TO_GRID = 6;

public static final int SIZE_SAME_WIDTH = 0;
public static final int SIZE_SAME_HEIGHT = 1;
public static final int SIZE_SAME_SIZE = 2;

protected static ArrayList designWindows = new ArrayList();
protected static boolean exitWhenLastWindowClosed = true;
protected static JFileChooser chooser;

protected Report report;
/** The frame may be null; it will be the parent of dialog boxes. */
protected JFrame frame;
/** The container of the root pane---the highest level widget. */
protected RootPaneContainer rootPaneContainer;	// Set by subclasses
protected CommandHistory commandHistory;
protected ArrayList sectionWidgets;
protected String reportFilePath;
protected JLayeredPane sectionContainer;
protected ArrayList selectedFields;
protected JScrollPane scroller;
protected JMenuItem undoItem, redoItem, cutItem, copyItem, pasteItem,
    delSelectionItem, delGroupItem, delSectionItem, aggrItem, groupItem,
    sectItem, formatItem, borderItem, boundsItem, defaultFormatItem,
    tableJoinItem, sqlQueryTextItem, connectionItem, selectRecordsItem,
    sortByItem, groupByItem, runItem, exportItem, subreportItem;
protected JMenu alignSubmenu, sizeSubmenu, paperSizeSubmenu;
protected boolean placingNewTextField;
protected boolean ignoreKeys;

/**
 * Adds a window to the list of open design windows. Also notifies the
 * error handler class that we it should use the GUI to display error
 * messages.
 *
 * @param win a design window
 */
public static void addWindow(Designer win) {
    designWindows.add(win);
}

/**
 * Returns the design window associated with the specified report, or
 * <code>null</code> if one is not found.
 *
 * @param r a report
 * @return a design window or <code>null</code> if one is not found
 */
public static Designer findWindowFor(Report r) {
    for (Iterator iter = designWindows.iterator(); iter.hasNext(); ) {
	Designer win = (Designer)iter.next();
	if (win.report == r)
	    return win;
    }
    return null;
}

/**
 * Sets value of flag that determines if the application should exit
 * when the last design window is closed. The default value is
 * <code>true</code>.
 */
public static void setExitWhenLastWinClosed(boolean exit) {
    exitWhenLastWindowClosed = exit;
}

/**
 * Deletes a window from the list of windows. If there are no more design
 * windows left and <var>exitWhenLastWindowClosed</var> is
 * <code>true</code> (which it is by default), exits the application.
 *
 * @param win a design window
 */
protected static void deleteWindow(Designer win) {
    designWindows.remove(win);
    if (designWindows.isEmpty() && exitWhenLastWindowClosed)
	System.exit(0);
}

/**
 * Closes each open design window (unless user cancels). If all are closed,
 * quits the application.
 */
protected static void maybeQuit() {
    // Can't use iterator over original list 'cause we're deleting items
    // from the list.
    ArrayList copy = (ArrayList)designWindows.clone();
    for (Iterator iter = copy.iterator(); iter.hasNext(); )
	((Designer)iter.next()).maybeClose();
}

public static JFileChooser getChooser() {
    if (chooser == null)
	chooser = new JFileChooser();
    return chooser;
}

/**
 * Sets the current directory for the chooser based on
 * the directory stored in the java preferences for
 * value 'key'.  Default key is reportDir
 */
public static void setPrefsDir(JFileChooser jfc, String key) {
    // Set the starting report directory (null is default home directory)
    // Store the report directory in the preferences for this package
    if (jfc != null) {
        if (key == null || key.trim().equals(""))
            key="reportDir";
        Preferences prefs = Preferences.userRoot().node("/jimm/datavision");
        String dir = prefs.get(key,null);
        if (dir != null)
            jfc.setCurrentDirectory(new File(dir));
    }
}

/**
 * Store the JFileChoosers current directory in the preferences
 * this 'remembers' the last directory used so the user isn't
 * having to navigate to their report directory each time.
 */
public static void savePrefsDir(JFileChooser jfc, String key) {
    if (jfc != null) {
        if (key == null || key.trim().equals(""))
            key="reportDir";
        String selectedFile = jfc.getSelectedFile().getPath();
        if (selectedFile != null) {
            Preferences prefs = Preferences.userRoot().node("/jimm/datavision");
            String dir = prefs.get(key,null);
            // If the file path has changed then store it in the
            // preferences
            String jfcap=jfc.getSelectedFile().getAbsolutePath();
            if (jfcap != null) {
                File f=new File(jfcap);
                String newdir=f.getParent();
                if (newdir == null)
                    newdir=dir;
                if (newdir != null) {
                    boolean changed=true;
                    if (dir != null && newdir.compareTo(dir) == 0)
                        changed=false;
                    if (changed)
                        prefs.put(key,newdir);
                }
            }
        }
    }
}

/**
 * Constructor. Reads the named report file or, if it's <code>null</code>,
 * creates a new, empty report.
 *
 * @param f an XML report file; may be <code>null</code>
 * @param databasePassword string to give to report; OK if it's
 * <code>null</code>
 * @param rpc the root pane container (the <code>JFrame</code> or
 * <code>JApplet</code>)
 * @param jframe the design window; may be <code>null</code> (for example,
 * when the designer is an applet)
 */
public Designer(File f, String databasePassword,
		RootPaneContainer rpc, JFrame jframe)
{
    // In subclasses, calling super(fileName, password, rpc, frame)
    // is a pain when frame == rpc because it is difficult to create
    // the frame, assign it to a temp, and pass the temp into both frame
    // and rpc. Therefore, when you pass in a frame but no rpc we assume
    // frame == rpc (because a JFrame is a RootPaneContainer).
    frame = jframe;
    rootPaneContainer = rpc;
    if (frame != null && rootPaneContainer == null)
	rootPaneContainer = frame;

    ErrorHandler.useGUI(true);	// Must set before opening report
    selectedFields = new ArrayList();

    // Must create command history before asking for database information
    // because that can cause a command to be created.
    commandHistory = new CommandHistory();

    StatusDialog statusDialog =
	new StatusDialog(frame, I18N.get("DesignWin.status_title"), false,
			 f == null
			 ? I18N.get("DesignWin.creating_empty")
			 : I18N.get("DesignWin.reading_xml"));

    // readReport() sets our report ivar as a side-effect.
    boolean askForDbConnInfo = readReport(f, databasePassword);

    if (report == null ||
	(report.getDataSource() == null && !askForDbConnInfo))
    {
	// User cancelled when asked for password. Close this window and
	// quit if this is the only report open.
	statusDialog.dispose();
	closeMe();
	Designer.deleteWindow(this); // Will quit if no other windows
	return;
    }

    if (askForDbConnInfo) {
	openDbConnWin(true);	// Modally ask for database connection info
	if (report.getDataSource() == null) {
	    // User cancelled. Close this window and quit if this is the
	    // only report open.
	    statusDialog.dispose();
	    closeMe();
	    Designer.deleteWindow(this); // Will quit if no other windows
	    return;
	}
    }

    statusDialog.update(I18N.get("DesignWin.building_win"));

    buildWindow();
    commandHistory.setMenuItems(undoItem, redoItem);
    enableMenuItems();

    // Might be using this code for dragging to select multiple fields.
//      SelectionHandler h = new SelectionHandler();
//      frame.addMouseListener(h);
//      frame.addMouseMotionListener(h);

    Designer.addWindow(this);

    // Let the format window start a thread to load font names. It will
    // only load them the first time this is called, but that's not our
    // problem, now is it?
    FormatWin.loadFontChoices();

    statusDialog.dispose();
}

protected void closeMe() {
    if (frame != null)
	frame.dispose();
}

public void update(Observable o, Object arg) {
    enableMenuItems();
}

/**
 * Performs a command.
 *
 * @param cmd a command
 */
public void performCommand(Command cmd) {
    commandHistory.perform(cmd);
}

/**
 * Adds to the command history a command that has already been performed.
 *
 * @param cmd a command
 */
public void addCommand(Command cmd) {
    commandHistory.add(cmd);
}

/**
 * Reads the named report file or, if it's <code>null</code>, creates
 * a new, empty report. Returns <code>true</code> if we need to ask
 * the user for connection info because this is a new report.
 *
 * @param f report XML file
 * @param databasePassword string to give to report; OK if it's
 * <code>null</code>
 * @return <code>true</code> if we need to ask the user for connection info
 */
protected boolean readReport(File f, String databasePassword) {
    if (f != null) {
	reportFilePath = f.getPath();
	report = new Report();
	report.setDatabasePassword(databasePassword);
	try {
	    report.read(f);
	    return false;
	}
	catch (UserCancellationException uce) {
	    report = null;	// Signal report open cancelled
	    return true;	// Doesn't matter what we return
	}
	catch (Exception e) {
	    ErrorHandler.error(e);
	    report = new Report();
	    return true;
	}
    }
    else {
	report = new Report();
	reportFilePath = null;
	return true;
    }
}

public void setIgnoreKeys(boolean ignore) { ignoreKeys = ignore; }

/**
 * Builds the window components.
 */
protected void buildWindow() {
    rootPaneContainer.getRootPane().setJMenuBar(buildMenuBar());
    buildSections();
}

/**
 * Builds the window menu bar.
 */
protected JMenuBar buildMenuBar() {
    JMenuBar bar = new JMenuBar();
    bar.add(buildFileMenu());
    bar.add(buildEditMenu());
    bar.add(buildInsertMenu());
    bar.add(buildFormatMenu());
    bar.add(buildDatabaseMenu());
    bar.add(buildReportMenu());
    bar.add(Box.createHorizontalGlue());
    bar.add(buildHelpMenu());

    return bar;
}

/**
 * Builds and returns the "File" menu.
 *
 * @return a menu
 */
protected JMenu buildFileMenu() {
    JMenu menu = MenuUtils.readMenu("DesignWin.menu_file");

    MenuUtils.addToMenu(this, menu, "DesignWin.menu_file_new");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_file_open");
    menu.addSeparator();
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_file_save");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_file_save_as");

    menu.addSeparator();

    ButtonGroup orientationGroup = new ButtonGroup();
    ButtonGroup nameGroup = new ButtonGroup();
    menu.add(paperSizeSubmenu =
	     MenuUtils.buildPaperSizeMenu(this,
					  report.getPaperFormat(),
					  orientationGroup, nameGroup));
    menu.addSeparator();
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_file_close");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_file_quit");

    return menu;
}

/**
 * Builds and returns the "Edit" menu.
 *
 * @return a menu
 */
protected JMenu buildEditMenu() {
    JMenu menu = MenuUtils.readMenu("DesignWin.menu_edit");

    undoItem = MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_undo");
    redoItem = MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_redo");
    menu.addSeparator();
    cutItem = MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_cut");
    copyItem = MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_copy");
    pasteItem = MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_paste");
    menu.addSeparator();
    delSelectionItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_del_fields");
    menu.addSeparator();
    delGroupItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_del_group");
    delSectionItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_edit_del_section");

    return menu;
}

/**
 * Builds and returns the "Insert" menu.
 *
 * @return a menu
 */
protected JMenu buildInsertMenu() {
    JMenu menu = MenuUtils.readMenu("DesignWin.menu_insert");

    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_column");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_text");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_formula");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_param");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_usercol");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_special");
    aggrItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_aggr");
    menu.addSeparator();
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_image");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_line");
    menu.addSeparator();
    groupItem = MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_group");
    sectItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_section");
    subreportItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_insert_subreport");

    return menu;
}

/**
 * Builds and returns the "Format" menu.
 *
 * @return a menu
 */
protected JMenu buildFormatMenu() {
    JMenu menu = MenuUtils.readMenu("DesignWin.menu_format");

    formatItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_format_format");
    borderItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_format_border");
    menu.addSeparator();
    boundsItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_format_bounds");
    menu.add(alignSubmenu = MenuUtils.buildAlignMenu(this, null));
    menu.add(sizeSubmenu = MenuUtils.buildSizeMenu(this, null));
    menu.addSeparator();
    defaultFormatItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_format_default");

    return menu;
}

/**
 * Builds and returns the "Database" menu.
 *
 * @return a menu
 */
protected JMenu buildDatabaseMenu() {
    JMenu menu = MenuUtils.readMenu("DesignWin.menu_database");

    tableJoinItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_database_linker");
    menu.addSeparator();
//      MenuUtils.addToMenu(this, menu, "DesignWin.menu_database_join");
//      menu.addSeparator();
    sqlQueryTextItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_database_sql");
    menu.addSeparator();
    connectionItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_connection");

    return menu;
}

/**
 * Builds and returns the "Report" menu.
 *
 * @return a menu
 */
protected JMenu buildReportMenu() {
    JMenu menu = MenuUtils.readMenu("DesignWin.menu_report");

    runItem = MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_run");
    exportItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_export");
    menu.addSeparator();
    selectRecordsItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_select");
    sortByItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_sort");
    groupByItem =
	MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_group");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_start_formula");
    menu.addSeparator();
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_scripting_langs");
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_report_summary");

    return menu;
}

/**
 * Builds and returns the "Help" menu.
 *
 * @return a menu
 */
protected JMenu buildHelpMenu() {
    JMenu menu = MenuUtils.readMenu("DesignWin.menu_help");

    MenuUtils.addToMenu(this, menu, "DesignWin.menu_help_help");
    menu.addSeparator();
    MenuUtils.addToMenu(this, menu, "DesignWin.menu_help_about");

    return menu;
}

/**
 * Builds the sections and adds them to the section container.
 */
protected void buildSections() {
    rootPaneContainer.getContentPane().setLayout(new BorderLayout());

    sectionWidgets = new ArrayList();
    sectionContainer = new JLayeredPane();
    sectionContainer.setLayout(new DesignWinLayout());

    // Can't just call report.each_section because we want to append
    // '(a)', '(b)', etc. if there is more than one section in the group.
    buildSectionsInArea(report.headers());
    buildSectionsInArea(report.pageHeaders());

    // Add headers for each group.
    for (Iterator iter = report.groups(); iter.hasNext(); ) {
	Group g = (Group)iter.next();
	buildSectionsInArea(g.headers());
    }

    buildSectionsInArea(report.details());

    // Add footers for each group, where the groups are in reverse order.
    for (Iterator iter = report.groupsReversed(); iter.hasNext(); ) {
	Group g = (Group)iter.next();
	buildSectionsInArea(g.footers());
    }

    buildSectionsInArea(report.footers());
    buildSectionsInArea(report.pageFooters());

    renameSectionWidgets();	// Muck with section names (e.g., add " (a)")

    // Put sectionContainer within scroller and add scroller to content
    // pane.
    int width = SectionWidget.LHS_WIDTH
	+ (int)report.getPaperFormat().getWidth();
    scroller = new JScrollPane(sectionContainer);
    scroller.setPreferredSize(new Dimension(width + 4, 400));
    rootPaneContainer.getContentPane().add(scroller, BorderLayout.CENTER);
}

/**
 * Builds the section widgets for the sections in an area and adds them to the
 * section container. We name the widgets with a separate call to {@link
 * #renameSectionWidgets}.
 * <p>
 * We also start observing each section. Though our caller is called more than
 * once, it is OK to call addObserver() on an Observable multiple times.
 *
 * @param area contains sections
 */
protected void buildSectionsInArea(SectionArea area) {
    for (Iterator iter = area.iterator(); iter.hasNext(); ) {
	Section sect = (Section)iter.next();
	sect.addObserver(this);	// Start observing changes

	SectionWidget sw = new SectionWidget(this, sect, "");
	sectionWidgets.add(sw);

	// Add to palette layer (one above default layer) because we want
	// to move field widgets below section widgets temporarily at the
	// end of a drag (see putDown()).
	sectionContainer.add(sw, JLayeredPane.PALETTE_LAYER);
    }
}

/**
 * Recalculates section names for all sections in the report. Calls {@link
 * #renameSectionWidgetsIn} for each group of sections.
 */
protected void renameSectionWidgets() {
    renameSectionWidgetsIn(report.headers(), I18N.get("Report.report_header"),
			   null);
    renameSectionWidgetsIn(report.pageHeaders(),
			   I18N.get("Report.page_header"), null);
    int i = 1;
    for (Iterator iter = report.groups(); iter.hasNext(); ++i) {
	Group g = (Group)iter.next();
	renameSectionWidgetsIn(g.headers(), I18N.get("DesignWin.group") + " #"
			       + i + ' ' + I18N.get("DesignWin.header"), g);
    }
    renameSectionWidgetsIn(report.details(), I18N.get("Report.detail"), null);
    i = 1;
    for (Iterator iter = report.groups(); iter.hasNext(); ++i) {
	Group g = (Group)iter.next();
	renameSectionWidgetsIn(g.footers(), I18N.get("DesignWin.group") + " #"
			       + i + ' ' + I18N.get("DesignWin.footer"), g);
    }
    renameSectionWidgetsIn(report.footers(), I18N.get("Report.report_footer"),
			   null);
    renameSectionWidgetsIn(report.pageFooters(),
			   I18N.get("Report.page_footer"), null);
}

/**
 * Recalculates section names for a collection of sections. Called from {@link
 * #renameSectionWidgets}. Also sets the section's popup menu's first item
 * text.
 *
 * @param area a section area
 * @param prefix prepended to all section names in the collection
 * @param group the group containing this section list; may be
 * <code>null</code>
 */
protected void renameSectionWidgetsIn(SectionArea area, String prefix,
				      Group group)
{
    SectionWidget firstSectionWidget = null;
    boolean firstSectionFixed = false;
    int i = 0;
    for (Iterator iter = area.iterator(); iter.hasNext(); ++i) {
	Section s = (Section)iter.next();
	SectionWidget sw = findSectionWidgetFor(s);
	if (firstSectionWidget == null) {
	    sw.setDisplayName(prefix);
	    firstSectionWidget = sw;
	}
	else {
	    if (!firstSectionFixed) {
		firstSectionWidget.setDisplayName(prefix + " (a)");
		firstSectionFixed = true;
	    }
	    sw.setDisplayName(prefix + " (" + (char)('a' + i) + ")");
	}
	sw.setPopupName(group == null ? prefix : group.getSelectableName());
    }
}

/**
 * Enables or disables menu items based on field and window state.
 */
public void enableMenuItems() {
    int numSelected = countSelectedFields();
    boolean someFieldSelected = numSelected > 0;
    boolean multipleFieldsSelected = numSelected > 1;
    FieldWidget first = someFieldSelected ? (FieldWidget)selectedFields.get(0)
	: null;

    DataSource ds = report.getDataSource();

    // Edit menu
    Section s = (first == null) ? null : first.getSectionWidget().getSection();

    cutItem.setEnabled(someFieldSelected);
    copyItem.setEnabled(someFieldSelected);
    pasteItem.setEnabled(!Clipboard.instance().isEmpty());
    delSelectionItem.setEnabled(someFieldSelected);
    delGroupItem.setEnabled(someFieldSelected && report.isInsideGroup(s));
    delSectionItem.setEnabled(someFieldSelected && !report.isOneOfAKind(s));

    // Insert menu
    if (numSelected == 1) {	// One field is selected
	// Only enable aggregates if the selected field is a field
	// for which aggregates make sense.
	aggrItem.setEnabled(first.getField().canBeAggregated());
    }
    else
	aggrItem.setEnabled(false);
    sectItem.setEnabled(someFieldSelected);
    subreportItem.setEnabled(ds.canJoinTables());

    // Format menu
    if (someFieldSelected) {
	// Only enable if some field is formattable
	boolean enable = someSelectedFieldUsesFormat();
	formatItem.setEnabled(enable);
    }
    else
	formatItem.setEnabled(false);
    borderItem.setEnabled(someFieldSelected);
    boundsItem.setEnabled(someFieldSelected);
    alignSubmenu.setEnabled(multipleFieldsSelected);
    sizeSubmenu.setEnabled(multipleFieldsSelected);

    // Database menu
    tableJoinItem.setEnabled(ds.canJoinTables());
    sqlQueryTextItem.setEnabled(ds.isSQLGenerated());
    connectionItem.setEnabled(ds.isConnectionEditable());

    // Report menu
    runItem.setEnabled(ds.canRunReports());
    exportItem.setEnabled(ds.canRunReports());
    selectRecordsItem.setEnabled(ds.areRecordsSelectable());
    sortByItem.setEnabled(ds.areRecordsSortable());
    groupByItem.setEnabled(ds.canGroupRecords());
}

/**
 * Returns <code>true</code> if there is some selected field that can
 * be formatted.
 *
 * @return <code>true</code> if there is some selected field that can
 * be formatted
 */
public boolean someSelectedFieldUsesFormat() {
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	if (((FieldWidget)iter.next()).usesFormat())
	    return true;
    return false;
}


String action(String name) {
    return I18N.get(I18N.MENU_FILE_PREFIX, "DesignWin.menu_" + name
		    + ".action");
}

String action(String menu, String name) {
    return I18N.get(I18N.MENU_FILE_PREFIX, menu + ".menu_" + name + ".action");
}

/**
 * Handles user actions.
 */
public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd == null || cmd.length() == 0)
	return;

    // File menu
    if (cmd.equals(action("file_new"))) newReport();
    else if (cmd.equals(action("file_open"))) openReport();
    else if (cmd.equals(action("file_save"))) saveReport();
    else if (cmd.equals(action("file_save_as"))) saveReportAs();
    else if (cmd.equals(action("file_close"))) maybeClose();
    else if (cmd.equals(action("file_quit"))) maybeQuit();

    // Edit menu
    else if (cmd.equals(action("edit_undo"))) commandHistory.undo();
    else if (cmd.equals(action("edit_redo"))) commandHistory.redo();
    else if (cmd.equals(action("edit_cut")))
	commandHistory.perform(new CutCommand(this, selectedFields));
    else if (cmd.equals(action("edit_copy"))) copySelectedFields();
    else if (cmd.equals(action("edit_paste"))) paste();
    else if (cmd.equals(action("edit_del_fields"))) deleteSelectedFields();
    else if (cmd.equals(action("edit_del_group"))) {
	FieldWidget fw = (FieldWidget)selectedFields.get(0);
	deleteGroupContaining(fw.getSectionWidget().getSection());
    }
    else if (cmd.equals(action("edit_del_section"))) {
	FieldWidget fw = (FieldWidget)selectedFields.get(0);
	deleteSection(fw.getSectionWidget().getSection());
    }

    // Insert menu
    else if (cmd.equals(action("insert_column")))
	openFieldPickerWin(FieldPickerWin.REPORT_DATABASE_FIELDS);
    else if (cmd.equals(action("insert_text"))) placeNewTextField();
    else if (cmd.equals(action("insert_formula")))
	openFieldPickerWin(FieldPickerWin.FORMULAS);
    else if (cmd.equals(action("insert_usercol")))
	openFieldPickerWin(FieldPickerWin.USERCOLS);
    else if (cmd.equals(action("insert_param")))
	openFieldPickerWin(FieldPickerWin.PARAMETERS);
    else if (cmd.equals(action("insert_aggr"))) openAggregateWin();
    else if (cmd.equals(action("insert_special")))
	openFieldPickerWin(FieldPickerWin.SPECIAL_FIELDS);
    else if (cmd.equals(action("insert_image"))) createImageField();
//      else if (cmd.equals(action("insert_line"))) placeNewLine();
    else if (cmd.equals(action("insert_group"))) openNewGroupWin();
    else if (cmd.equals(action("insert_section"))) insertSection();
    else if (cmd.equals(action("insert_subreport"))) insertSubreport();

    // Format menu
    else if (cmd.equals(action("format_format"))) openFormatWin(0);
    else if (cmd.equals(action("format_border"))) openFormatWin(1);
    else if (cmd.equals(action("format_bounds"))) openBoundsWin();
    else if (cmd.equals(action("format_default"))) openDefaultFormatWin();

    // Format/Align submenu
    else if (cmd.equals(action("Align", "tops"))) align(ALIGN_TOP);
    else if (cmd.equals(action("Align", "middles"))) align(ALIGN_MIDDLE);
    else if (cmd.equals(action("Align", "bottoms"))) align(ALIGN_BOTTOM);
    else if (cmd.equals(action("Align", "lefts"))) align(ALIGN_LEFT);
    else if (cmd.equals(action("Align", "centers"))) align(ALIGN_CENTER);
    else if (cmd.equals(action("Align", "rights"))) align(ALIGN_RIGHT);
    else if (cmd.equals(action("Align", "snap"))) align(ALIGN_SNAP_TO_GRID);

    // Format/Size submenu
    else if (cmd.equals(action("Size", "same_width"))) size(SIZE_SAME_WIDTH);
    else if (cmd.equals(action("Size", "same_height"))) size(SIZE_SAME_HEIGHT);
    else if (cmd.equals(action("Size", "same_size"))) size(SIZE_SAME_SIZE);

    // Database menu
    else if (cmd.equals(action("database_linker"))) openVisTableWin();
    else if (cmd.equals(action("database_sql"))) showSQL();
    else if (cmd.equals(action("connection"))) openDbConnWin(false);

    // Report menu
    else if (cmd.equals(action("report_run"))) runReport();
    else if (cmd.equals(action("report_export"))) exportReport();
    else if (cmd.equals(action("report_select"))) openWhereClauseEditor();
    else if (cmd.equals(action("report_sort"))) openSortWin();
    else if (cmd.equals(action("report_group"))) openGroupWin();
    else if (cmd.equals(action("report_start_formula"))) openStartupScriptEditor();
    else if (cmd.equals(action("report_scripting_langs"))) openScriptingWin();
    else if (cmd.equals(action("report_summary"))) openDescripWin();

    // Help menu
    else if (cmd.equals(action("help_help"))) help();
    else if (cmd.equals(action("help_about"))) about();

    // Paper size menu
    else if (cmd.equals(action("MenuUtils", "paper_size_portrait")))
	changePaperOrientation(PaperFormat.PORTRAIT);
    else if (cmd.equals(action("MenuUtils", "paper_size_landscape")))
	changePaperOrientation(PaperFormat.LANDSCAPE);
    else {
	PaperFormat p =
	    PaperFormat.get(report.getPaperFormat().getOrientation(), cmd);
	changePaperSize(p);
    }
}

protected void changePaperOrientation(int orientation) {
    changePaperSize(PaperFormat.get(orientation,
				    report.getPaperFormat().getName()));
}

protected void changePaperSize(PaperFormat p) {
    if (p != null) {
	performCommand(new PaperSizeCommand(report, this, p));
	// The paper size menu gets updated as a side effect of the
	// command's call to paperSizeChanged().
    }
}

/**
 * Update paper orientation and size menus.
 */
public void updatePaperSizeMenu(PaperFormat p) {
    paperSizeSubmenu.getItem(p.getOrientation() == PaperFormat.PORTRAIT
			     ? 0 : 1)
	.setSelected(true);
    int i = 3;
    for (Iterator iter = PaperFormat.names(); iter.hasNext(); ++i) {
	String name = (String)iter.next();
	if (name.equals(p.getName()))
	    paperSizeSubmenu.getItem(i).setSelected(true);
    }
}

public void paperSizeChanged(PaperFormat p) {
    for (Iterator iter = sectionWidgets.iterator(); iter.hasNext(); )
	((SectionWidget)iter.next()).paperSizeChanged();

    Dimension d = new Dimension(SectionWidget.LHS_WIDTH
				+ (int)report.getPaperFormat().getWidth(),
				sectionContainer.getHeight());
    sectionContainer.setPreferredSize(d);
    sectionContainer.invalidate();
    rootPaneContainer.getRootPane().validate();

    updatePaperSizeMenu(p);
}

public Frame getFrame() { return frame; }

public void invalidate() { rootPaneContainer.getRootPane().invalidate(); }

public Report getReport() { return report; }

/**
 * Creates a new report in a new design window.
 */
protected void newReport() {
    new DesignWin(null);	// Open a new window
}

/**
 * Opens an existing report in a new design window.
 */
protected void openReport() {
    JFileChooser jfc=getChooser();
    setPrefsDir(jfc,null);
    int returnVal = jfc.showOpenDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        savePrefsDir(jfc,null); // save report directory
	new DesignWin(getChooser().getSelectedFile());
}
}

/**
 * Saves the current report.
 */
protected void saveReport() {
    if (reportFilePath == null)
	saveReportAs();
    else
	writeReportFile(reportFilePath);
}

/**
 * Saves the current report in a different file.
 */
protected void saveReportAs() {
    JFileChooser jfc=getChooser();
    setPrefsDir(jfc,null);
    int returnVal = jfc.showSaveDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
    savePrefsDir(jfc,null); // save report directory
	reportFilePath = getChooser().getSelectedFile().getPath();
	writeReportFile(reportFilePath);
    }
}

/**
 * Writes the current report to the specified file. Also tells the
 * command history the report has been saved so it knows how to report
 * if any changes have been made from this point on.
 *
 * @param fileName a file name
 */
protected void writeReportFile(String fileName) {
    report.writeFile(fileName);
    commandHistory.setBaseline();
}

/**
 * Exports the report output using one of the layout engines.
 */
protected void exportReport() {
    new ExportWin(getFrame(), report);
}

/**
 * Saves the report if it is changed (some command has been performed) and
 * closes the current design window. If there are no more open design
 * windows, exist the application.
 */
protected void maybeClose() {
    if (commandHistory.isChanged()) {
	String str = I18N.get("DesignWin.save_question");
	switch (JOptionPane.showConfirmDialog(frame, str)) {
	case JOptionPane.YES_OPTION:
	    saveReport();
	case JOptionPane.NO_OPTION:
	    break;
	case JOptionPane.CANCEL_OPTION:
	    return;		// Don't close window
	}
    }
    closeMe();
    Designer.deleteWindow(this);
}

/**
 * Runs and displays the report.
 */
protected void runReport() {
    report.setLayoutEngine(new SwingLE());
    report.run();
}

/**
 * Returns the number of selected fields.
 *
 * @return the number of selected fields
 */
int countSelectedFields() {
    return selectedFields.size();
}

/**
 * Hands each field widget to the specified {@link FieldWidgetWalker}.
 *
 * @param perambulator a field widget walker
 */
void withWidgetsDo(FieldWidgetWalker perambulator) {
    for (Iterator iter = sectionWidgets.iterator(); iter.hasNext(); ) {
	SectionWidget sw = (SectionWidget)iter.next();
	Object[] kids = sw.fieldPanel.getComponents();
	for (int i = 0; i < kids.length; ++i) {
	    FieldWidget fw = FieldWidget.findFieldWidgetOwning(kids[i]);
	    if (fw != null)
		perambulator.step(fw);
	}
    }
}

/**
 * Hands each selected field widget's field to the specified {@link
 * FieldWalker}.
 *
 * @param perambulator a field walker
 */
void withSelectedFieldsDo(FieldWalker perambulator) {
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	perambulator.step(((FieldWidget)iter.next()).getField());
}

/**
 * Deletes the group that contains the specified section.
 *
 * @param section a report section
 */
void deleteGroupContaining(Section section) {
    Group group = report.findGroup(section);
    if (group != null)
	commandHistory.perform(new DeleteGroupCommand(this, report, group));
}

/**
 * Inserts a new section below the section containing the first selected
 * field widget. Only called when at least one widget is selected.
 */
void insertSection() {
    Field f = firstSelectedFieldWidget().getField();
    if (f != null)
	insertSectionBelow(f.getSection());
}

/**
 * Inserts a new section below the specified section.
 *
 * @param s a report section
 */
void insertSectionBelow(Section s) {
    commandHistory.perform(new NewSectionCommand(this, report, s));
}

/**
 * Inserts a section widget at the specified position in the list. Also starts
 * observing the section and renames all section widgets (adding "(a)", for
 * example. Called from {@link NewSectionCommand#perform}.
 *
 * @param sw the section widget to insert
 * @param putAfter <var>sw</var> goes after this widget; may be
 * <code>null</code>
 */
public void insertSectionWidgetAfter(SectionWidget sw, SectionWidget putAfter)
{
    int insertIndex = sectionWidgets.indexOf(putAfter);
    sectionWidgets.add(insertIndex + 1, sw);

    // Add to palette layer (one above default layer) because
    // we want to move field widgets below section widgets
    // temporarily at the end of a drag (see putDown()).
    sectionContainer.add(sw, JLayeredPane.PALETTE_LAYER, insertIndex + 1);

    sw.getSection().addObserver(this);
    renameSectionWidgets(); // Muck with names (e.g., add " (a)")
}

/**
 * Deletes the specified section. Does nothing if this section is the only
 * one of its kind. (This method should only be called if the section is
 * not one-of-a-kind. No harm done if you do, though.)
 *
 * @param s a report section
 */
public void deleteSection(Section s) {
    if (s != null && !report.isOneOfAKind(s))
	commandHistory.perform(new DeleteSectionCommand(this, report, s));
}

/**
 * Deletes a section from the report and the design window and returns the
 * section widget <em>above</em> the section's. If <var>s</var> is the first
 * section in the report, returns <code>null</code>. Called from commands;
 * don't call this yourself. Insetad call {@link #deleteSection}.
 *
 * @param s the section to delete
 * @return the section widget above the section's, or <code>null</code>
 * if there is none
 */
public SectionWidget doDeleteSection(Section s) {
    SectionWidget sw = findSectionWidgetFor(s);
    int index = sectionWidgets.indexOf(sw);
    SectionWidget widgetBefore = (index == 0) ? null
	: (SectionWidget)sectionWidgets.get(index - 1);

    s.deleteObserver(this);
    report.removeSection(s);
    sectionContainer.remove(sw);
    sectionWidgets.remove(sw);
    renameSectionWidgets();

    return widgetBefore;
}

/**
 * Opens the dialog that starts the process of inserting a sub-report.
 */
protected void insertSubreport() {
    new SubreportWin(this, report);
}

/**
 * Rebuilds the group sections and redisplays the report.
 */
public void rebuildGroups() {
    deselectAll();
    rootPaneContainer.getContentPane().remove(scroller);
    buildSections();
    if (frame != null)
	frame.pack();
}

/**
 * Creates and adds a new text field to the first section of the page header.
 *
 * @param x where to place the title
 * @param width how wide it should be
 * @param title the string to display
 * @return the newly-created widget
 */
public FieldWidget addTitleField(int x, int width, String title) {
    Section s = report.getFirstSectionByArea(SectionArea.PAGE_HEADER);

    // Create the field.
    Field f = Field.create(null, report, s, "text", title, true);
    jimm.datavision.field.Rectangle b = f.getBounds();
    b.setBounds(x, 0, width, Field.DEFAULT_HEIGHT);

    // Make the field underlined and bold.
    f.getFormat().setBold(true);
    f.getFormat().setUnderline(true);

    // Add the field to the report section.
    s.addField(f);

    // Create widget and add to section widget.
    TextFieldWidget tfw = new TextFieldWidget(null, f);
    tfw.moveToSection(findSectionWidgetFor(s));
    return tfw;
}

/**
 * Aligns the selected fields to the first selected field (chronologically
 * speaking).
 *
 * @param which alignment constant.
 */
protected void align(int which) {
    if (selectedFields.isEmpty())
	return;

    // Align the widgets based on the position of the first selected field.
    CompoundCommand cmd =
	new CompoundCommand(I18N.get("FieldAlignCommand.name"));
    Field first = firstSelectedFieldWidget().getField();
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	cmd.add(new FieldAlignCommand((FieldWidget)iter.next(), which, first));

    commandHistory.perform(cmd);
}

/**
 * Resize the selected fields based on the first selected field
 * (chronologically speaking) and the specified resize (width, height, both).
 *
 * @param which size constant
 */
protected void size(int which) {
    if (selectedFields.isEmpty())
	return;

    // Resize the widgets based on the position of the first selected field.
    CompoundCommand cmd =
	new CompoundCommand(I18N.get("FieldResizeCommand.name"));
    Field first = firstSelectedFieldWidget().getField();
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	cmd.add(new FieldResizeCommand((FieldWidget)iter.next(), which,
				       first));

    commandHistory.perform(cmd);
}

/**
 * Returns first selected field widget (chronologically speaking). May
 * return <code>null</code> if no fields are selected
 *
 * @return field widget selected the earliest, or <code>null</code> if
 * no fields are selected
 */
FieldWidget firstSelectedFieldWidget() {
    return selectedFields.isEmpty()
	? null : (FieldWidget)selectedFields.get(0);
}

/**
 * Selects a field widget, possibly deselecting all others everywhere.
 * Called from section widget.
 *
 * @param fieldWidget a field widget
 * @param makeSelected new selection state
 * @param deselectOthers if <code>true</code>, all other fields in all
 * sections are deselected first
 */
public void select(FieldWidget fieldWidget, boolean makeSelected,
	    boolean deselectOthers)
{
    if (deselectOthers)
	deselectAll();

    fieldWidget.doSelect(makeSelected);
    if (makeSelected) {
	if (!selectedFields.contains(fieldWidget)) // Don't add it twice
	    selectedFields.add(fieldWidget);
    }
    else {
	selectedFields.remove(fieldWidget);
    }

    enableMenuItems();
}

/**
 * Deselect all fields. Called from {@link SectionWidget#deselectAll}.
 */
public void deselectAll() {
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	((FieldWidget)iter.next()).doSelect(false);
    selectedFields.clear();

    enableMenuItems();
}

/**
 * Copies the selected fields to the clipboard. We need to create {@link
 * jimm.datavision.gui.cmd.Pasteable} objects.
 */
protected void copySelectedFields() {
    ArrayList pasteables = new ArrayList();
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	pasteables.add(new FieldClipping((FieldWidget)iter.next()));
    Clipboard.instance().setContents(pasteables);

    pasteItem.setEnabled(true);
}

protected void paste() {
    CompoundCommand cmd = new CompoundCommand(I18N.get("PasteCommand.name"));
    if (selectedFields.size() > 0)
	cmd.add(new DeleteCommand(this, selectedFields));
    cmd.add(new PasteCommand(this));
    commandHistory.perform(cmd);
}

/**
 * Delete selected fields.
 */
protected void deleteSelectedFields() {
    deleteSelectedFieldsAnd(null);
}

/**
 * Delete specified field and all selected fields.
 *
 * @param oneMore an additional field to delete; may be <code>null</code>
 */
protected void deleteSelectedFieldsAnd(FieldWidget oneMore) {
    ArrayList fields = new ArrayList(selectedFields);
    if (oneMore != null && !fields.contains(oneMore))
	fields.add(oneMore);
    commandHistory.perform(new DeleteCommand(this, fields));
}

/**
 * Toggles the visibility of all selected fields plus the one passed in.
 * Called by a section widget.
 *
 * @see FieldWidget#doSetVisibility
 */
void setFieldVisibility(boolean newVisiblity, FieldWidget fw) {
    if (selectedFields.isEmpty() && fw == null)
	return;

    String nameKey = newVisiblity ? "FieldShowCommand.name"
	: "FieldHideCommand.name";

    CompoundCommand cmd = new CompoundCommand(I18N.get(nameKey));
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	cmd.add(new FieldShowHideCommand((FieldWidget)iter.next(), nameKey,
					 newVisiblity));
    if (fw != null && !selectedFields.contains(fw))
	cmd.add(new FieldShowHideCommand(fw, nameKey, newVisiblity));

    commandHistory.perform(cmd);
}

/**
 * Asks design window to create and accepts a new text field. Called from
 * {@link SectionWidget#createNewTextField}.
 *
 * @see Designer#createNewTextField
 */
void createNewTextField(SectionWidget sw, MouseEvent e) {
    commandHistory.perform(new NewTextFieldCommand(sw, e));

    // Accept the drop
    acceptNewTextField();
}

/**
 * Picks up the field widget because field dragging is starting.
 * Called from SectionWidget#pickUp.
 *
 * @param mouseScreenPos the location of the mouse in screen coordinates
 */
void pickUp(java.awt.Point mouseScreenPos) {
    Dimension size = sectionContainer.getBounds().getSize();
    sectionContainer.setPreferredSize(size);
    sectionContainer.setMinimumSize(size);

    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); ) {
	FieldWidget fw = (FieldWidget)iter.next();
	fw.pickUp(mouseScreenPos);

	// Add to the drag layer of our section container.
	sectionContainer.add(fw.getComponent(), JLayeredPane.DRAG_LAYER);

	// Recalculate bounds relative to the section container.
	jimm.datavision.field.Rectangle b = fw.getField().getBounds();
	b.setBounds(b.x + SectionWidget.LHS_WIDTH,
		    b.y + fw.getSectionWidget().getBounds().y,
		    b.width, b.height);
    }
}

/**
 * Puts the dragged field widgets down inside the sections they are floating
 * above. Called from {@link SectionWidget#putDown}.
 *
 * @param f the field widget being dragged; all other selected fields
 * have been dragged along with it
 * @param origScreenPos the original location of the field in screen
 * coordinates
 * @param mouseScreenPos the current mouse position in screen coordinates;
 * note 
 */
void putDown(FieldWidget f, java.awt.Point origScreenPos,
	     java.awt.Point mouseScreenPos)
{
    // Move all dragged fields under everything else so getComponentAt()
    // will not return this field.
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	sectionContainer.setLayer(((FieldWidget)iter.next()).getComponent(),
				  JLayeredPane.DEFAULT_LAYER.intValue());

    CompoundCommand cmd =
	new CompoundCommand(I18N.get("FieldMoveCommand.name"));

    // Move to new section. Each field may be dropped into a different
    // section.
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); ) {
	FieldWidget fw = (FieldWidget)iter.next();
	SectionWidget sw =
	    getSectionWidgetUnder(fw.getComponent().getLocationOnScreen());
	if (sw == null)		// Field snaps back to orig pos if sw is null
	    fw.snapBack();
	else
	    cmd.add(new FieldMoveCommand(fw, sw));
    }

    if (cmd.numCommands() > 0)
	commandHistory.perform(cmd);
}

/**
 * Starts stretching all selected fields. Called from {@link
 * SectionWidget#startStretching}.
 *
 * @param mouseScreenPos the location of the mouse in screen coordinates
 */
void startStretching(java.awt.Point mouseScreenPos) {
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); ) {
	FieldWidget fw = (FieldWidget)iter.next();
	fw.startStretching(mouseScreenPos);
    }
}

/**
 * Tells each field to stop stretching and creates a command that will undo
 * all that stretching. Called from {@link SectionWidget#stopStretching}.
 *
 * @param f the field widget being dragged; all other selected fields
 * have been dragged along with it
 * @param origBounds the field's original bounds
 */
void stopStretching(FieldWidget f, jimm.datavision.field.Rectangle origBounds)
{
    CompoundCommand cmd =
	new CompoundCommand(I18N.get("FieldStretchCommand.name"));

    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); ) {
	FieldWidget fw = (FieldWidget)iter.next();
	cmd.add(new FieldStretchCommand(fw, origBounds));
	fw.stopStretching();
    }

    if (cmd.numCommands() > 0)
	commandHistory.perform(cmd);
}

/**
 * Returns the section widget under the mouse. Returns <code>null</code>
 * unless the mouse is over a section field panel (the white area on
 * which fields belong).
 *
 * @param screenPos a position in screen coordinates
 * @return the SectionWidget under the mouse
 */
protected SectionWidget getSectionWidgetUnder(java.awt.Point screenPos) {
    // Translate screenPos to sectionContainer coords
    java.awt.Point scScreenPos = sectionContainer.getLocationOnScreen();
    java.awt.Point scPos = new java.awt.Point(screenPos.x - scScreenPos.x,
					      screenPos.y - scScreenPos.y);

    if (scPos.x < SectionWidget.LHS_WIDTH) // Reject if in LHS name
	return null;

    // We've landed on a section field panel, a field in the section, or a
    // section name label. Crawl up the parent hierarchy until we find
    // first the section field panel (insuring we are in the right place)
    // and then the section.
    Component c = sectionContainer.getComponentAt(scPos);
    while (c != null && !(c instanceof SectionWidget))
	c = c.getParent();
    return (SectionWidget)c;
}

/**
 * Drags the selected field widgets a few pixels. Called from section that
 * contains field being dragged.
 *
 * @param action a {@link FieldWidget}<code>.ACTION_*</code> constant
 * @param mouseScreenPos mouse screen position
*/
protected void dragSelectedWidgets(int action, java.awt.Point mouseScreenPos) {
    for (Iterator iter = selectedFields.iterator(); iter.hasNext(); )
	((FieldWidget)iter.next()).doDrag(action, mouseScreenPos);
}

/**
 * Opens a new or existing field picker window.
 *
 * @param startingType the index of the starting type to display
 */
protected void openFieldPickerWin(int startingType) {
    new FieldPickerWin(this, report, startingType);
}

/**
 * Opens a new or existing field aggregate window. We should
 * only get here if there is exactly one selected field.
 */
protected void openAggregateWin() {
    new AggregatesWin(this, (FieldWidget)selectedFields.get(0));
}

/**
 * Opens a dialog that asks the user to select an image file. Creates an
 * image in the report header.
 */
protected void createImageField() {
    String url =
	new AskStringDialog(frame, I18N.get("DesignWin.image_url_title"),
			    I18N.get("DesignWin.image_url_label")).getString();
    if (url != null) {
	Section s = report.getFirstSectionByArea(SectionArea.REPORT_HEADER);
	NewImageFieldCommand cmd =
	    new NewImageFieldCommand(findSectionWidgetFor(s), url);
	commandHistory.perform(cmd);
    }
}

/**
 * Opens a new or existing new group window.
 */
protected void openNewGroupWin() {
    new NewGroupWin(this, report);
}

/**
 * Sets the flag that tells everyone else that the user wants to place
 * a new text field.
 */
protected void placeNewTextField() {
    placingNewTextField = true;
    rootPaneContainer.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
}

/**
 * Returns <code>true</code> if the user is trying to place a new text field.
 *
 * @return <code>true</code> if the user is trying to place a new text field
 */
boolean isPlacingNewTextField() {
    return placingNewTextField;
}

/**
 * The caller has accepted the new text field.
 */
void acceptNewTextField() {
    placingNewTextField = false;
    rootPaneContainer.getRootPane().setCursor(null);
}

/**
 * The caller has rejected the new text field.
 */
void rejectNewTextField() {
    acceptNewTextField();
}

/**
 * Opens a new or existing field format window.
 *
 * @param whichTab the index of the tab to display when opened
 */
protected void openFormatWin(int whichTab) {
    FieldWidget fw = firstSelectedFieldWidget();
    if (fw != null)
	new FormatWin(this, fw.getField(), whichTab);
}

/**
 * Opens a new or existing field format window that lets the user edit the
 * report's default format and border.
 */
protected void openDefaultFormatWin() {
    new FormatWin(this, report.getDefaultField(), 0);
}

/**
 * Opens a new or existing bounds editor window.
 */
protected void openBoundsWin() {
    FieldWidget fw = firstSelectedFieldWidget();
    if (fw != null)
	new BoundsWin(this, fw.getField());
}

/**
 * Opens a new or existing visible table joiner window.
 */
protected void openVisTableWin() {
    new VisTableWin(this, report);
}

/**
 * Opens a new or existing where clause editor.
 */
protected void openWhereClauseEditor() {
    new WhereClauseWin(this, report);
}

/**
 * Opens a new or existing sort order window.
 */
protected void openSortWin() {
    new SortWin(this, report);
}

/**
 * Opens a new or existing group order window.
 */
protected void openGroupWin() {
    new GroupWin(this, report);
}

/**
 * Opens a starutp script editor.
 */
protected void openStartupScriptEditor() {
    new StartupScriptEditor(this, report);
}

/**
 * Opens a new or existing report formula language window.
 */
protected void openScriptingWin() {
    new ScriptingWin(this, report.getScripting());
}

/**
 * Opens a new or existing report description (name, title, etc.) window.
 */
protected void openDescripWin() {
    new DescripWin(this, report);
}

/**
 * Opens the help window.
 */
protected void help() {
    HelpWin helpWin = HelpWin.instance();
    helpWin.setState(Frame.NORMAL); // De-iconify. Why is this necessary?
    helpWin.setVisible(true);
    helpWin.toFront();
}

/**
 * Opens the about box.
 */
protected void about() {
    String msg = I18N.get("DesignWin.about_1") + info.Version + "\n"
	+ I18N.get("DesignWin.about_2") + "\n"
	+ info.URL + "\n\n"
	+ info.Copyright + ".\n\n"
	+ I18N.get("DesignWin.about_3");
    JOptionPane.showMessageDialog(null, msg, I18N.get("DesignWin.about_title"),
				  JOptionPane.PLAIN_MESSAGE);
}

/**
 * Opens a new or existing database connection info window.
 *
 * @param modal passed on to dialog constructor
 */
protected void openDbConnWin(boolean modal) {
    new DbConnWin(this, report, modal);
}

/**
 * Opens a new window containing the SQL query text.
 */
protected void showSQL() {
    new SQLQueryWin(frame, report.getDataSource().getQuery().toString());
}

/**
 * Returns the section widget containing the specified section.
 *
 * @param s section
 * @return the section widget containing the section
 */
public SectionWidget findSectionWidgetFor(Section s) {
    for (Iterator iter = sectionWidgets.iterator(); iter.hasNext(); ) {
	SectionWidget sw = (SectionWidget)iter.next();
	if (sw.section == s)
	    return sw;
    }
    return null;
}


/**
 * Snaps the rectangle to the grid.
 *
 * @param r a rectangle
 */
public void snapToGrid(jimm.datavision.field.Rectangle r) {
    int coord = (int)r.x;
    int mod = coord % GRID_SIZE;
    if (mod != 0) {
	if (mod <= GRID_SIZE / 2)
	    r.setX(coord - mod);
	else
	    r.setX(coord + GRID_SIZE - mod);
    }

    coord = (int)r.y;
    mod = coord % GRID_SIZE;
    if (mod != 0) {
	if (mod < GRID_SIZE / 2)
	    r.setY(coord - mod);
	else
	    r.setY(coord + GRID_SIZE - mod);
    }
}

// Might be using this code for dragging to select multiple fields. It
// should eventually live in a separate file.

//  class SelectionHandler extends MouseInputAdapter {

//  protected java.awt.Rectangle rect;
//  protected HashSet fields;
//  protected JPanel selectionOutline;

//  public void mousePressed(MouseEvent e) {
//      int x = e.getX() - SectionWidget.LHS_WIDTH;
//      if (x < 0)
//  	return;

//      deselectAll();

//      int y = e.getY();
//      rect = new java.awt.Rectangle(x, y, 0, 0);
//      selectionOutline = new JPanel();
//      selectionOutline.setBounds(rect);
//      rootPaneContainer.getContentPane().add(selectionOutline, 0); // Add to top of visual stack
//  }

//  public void mouseDragged(MouseEvent e) {
//      updateSize(e);
//  }

//  public void mouseReleased(MouseEvent e) {
//      updateSize(e);
//      remove(selectionOutline);
//  }

//  /* 
//   * Updates the size of the current rectangle, changes selection list, and
//   * calls {@link #repaint}. Because rect always has the same origin,
//   * translate it if the width or height is negative.
//   * 
//   * For efficiency, specify the painting region using arguments to the
//   * {@link #repaint} call.
//   */
//  void updateSize(MouseEvent e) {
//      if (selectionOutline == null)
//  	return;

//      int x = e.getX();
//      int y = e.getY();
//      rect.setSize(x - rect.x, y - rect.y);
//      selectionOutline.setSize(x - rect.x, y - rect.y);

//  //      selectFieldsWithin(rect);

//  //      java.awt.Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
//  //      repaint(totalRepaint.x, totalRepaint.y,
//  //  	    totalRepaint.width, totalRepaint.height);
//  }

//  }

}
