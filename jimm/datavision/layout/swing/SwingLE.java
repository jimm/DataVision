package jimm.datavision.layout.swing;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.layout.LayoutEngine;
import jimm.datavision.gui.ExportWin;
import jimm.datavision.gui.MenuUtils;
import jimm.datavision.gui.StatusDialog;
import jimm.util.I18N;
import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * <code>SwingLE</code> is a layout engine that creates a Swing window.
 * The window can be printed by selecting the appropriate menu item.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see SwingPageContents
 * @see SwingPage
 * @see SwingField
 * @see SwingPrintBook
 */
public class SwingLE extends LayoutEngine {

protected static final Dimension WINDOW_START_SIZE = new Dimension(600, 400);
protected static final String PRINT_ICON = "images/Print16.gif";
protected static final String EXPORT_ICON = "images/Export16.gif";
protected static final String FIRST_ICON = "images/Home16.gif";
protected static final String PREV_ICON = "images/Back16.gif";
protected static final String NEXT_ICON = "images/Forward16.gif";
protected static final String LAST_ICON = "images/Down16.gif";

protected JFrame frame;
protected Dimension pageDim;
protected JPanel cardPanel;
protected JScrollPane scroller;
protected int displayPageNum;	// Starts at 1
protected ArrayList pageContents;
protected SwingPageContents pageBeingBuilt;
protected JLabel pageCountLabel;
protected Action printAction;
protected Action closeAction;
protected Action exportAction;
protected Action goFirstAction;
protected Action goPrevAction;
protected Action goNextAction;
protected Action goLastAction;

/**
 * Constructor.
 */
public SwingLE() {
    super();
    pageContents = new ArrayList();
    ErrorHandler.useGUI(true);
}

public void cancel() {
    super.cancel();
    close();
}

public JFrame getJFrame() { return frame; }

/**
 * Creates window and displays a blank page.
 */
protected void doStart() {
    pageDim = new Dimension((int)pageWidth(), (int)pageHeight());
    frame = new JFrame(report.getTitle());
    makeActions();
    makeMenu(frame);
    frame.getContentPane().add(makeToolbar(), BorderLayout.NORTH);

    // Card panel for displaying pages
    cardPanel = new JPanel();
    CardLayout cardLayout = new CardLayout(0, 0);
    cardPanel.setLayout(cardLayout);

    // Set sizes of scroller (window size) and card panel (page size)
    cardPanel.setPreferredSize(pageDim);

    // New, blank, dummy card we can display until the first page is
    // generated.
    JPanel blankPage = new JPanel();
    blankPage.setBackground(Color.white);
    blankPage.setPreferredSize(pageDim);
    cardPanel.add(blankPage, "blank page");
    cardLayout.show(cardPanel, "blank page");

    // Scroller containing the card panel
    scroller = new JScrollPane(cardPanel);
    scroller.setPreferredSize(WINDOW_START_SIZE);
    frame.getContentPane().add(scroller, BorderLayout.CENTER);

    displayPageNum = 0;

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	    close();
	}
	});

    frame.pack();
    frame.setVisible(true);
}

/**
 * Done loading report.
 */
protected void doEnd() {
    printAction.setEnabled(true);

    if (pageContents.size() > 1) // Start prebuilding the report's last page
	pageBeingBuilt.prebuildPage();

    // Calling displayPage() will force page 1 to finish building and
    // will display it.
    if (displayPageNum == 0)
	displayPage(1);
}

/**
 * Creates the actions used by menu items and toolbar widgets.
 */
protected void makeActions() {
    URL url = getClass().getClassLoader().getResource(PRINT_ICON);
    String str = I18N.get(I18N.MENU_FILE_PREFIX, "SwingLE.action_print");
    printAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) { printReport(); }
	};
    printAction.putValue(Action.SHORT_DESCRIPTION, str);
    printAction.setEnabled(false);

    str = I18N.get(I18N.MENU_FILE_PREFIX, "SwingLE.action_close");
    closeAction = new AbstractAction(str) {
	public void actionPerformed(ActionEvent e) { close(); }
	};

    url = getClass().getClassLoader().getResource(EXPORT_ICON);
    str = I18N.get(I18N.MENU_FILE_PREFIX, "SwingLE.action_export");
    exportAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) { export(); }
	};
    exportAction.putValue(Action.SHORT_DESCRIPTION, str);

    url = getClass().getClassLoader().getResource(FIRST_ICON);
    str = I18N.get(I18N.MENU_FILE_PREFIX, "SwingLE.action_first_page");
    goFirstAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) { displayFirstPage(); }
	};
    goFirstAction.putValue(Action.SHORT_DESCRIPTION, str);
    goFirstAction.setEnabled(false);

    url = getClass().getClassLoader().getResource(PREV_ICON);
    str = I18N.get(I18N.MENU_FILE_PREFIX, "SwingLE.action_previous_page");
    goPrevAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) { displayPrevPage(); }
	};
    goPrevAction.putValue(Action.SHORT_DESCRIPTION, str);
    goPrevAction.setEnabled(false);

    url = getClass().getClassLoader().getResource(NEXT_ICON);
    str = I18N.get(I18N.MENU_FILE_PREFIX, "SwingLE.action_next_page");
    goNextAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) { displayNextPage(); }
	};
    goNextAction.putValue(Action.SHORT_DESCRIPTION, str);
    goNextAction.setEnabled(false);

    url = getClass().getClassLoader().getResource(LAST_ICON);
    str = I18N.get(I18N.MENU_FILE_PREFIX, "SwingLE.action_last_page");
    goLastAction = new AbstractAction(str, new ImageIcon(url, str)) {
	public void actionPerformed(ActionEvent e) { displayLastPage(); }
	};
    goLastAction.putValue(Action.SHORT_DESCRIPTION, str);
    goLastAction.setEnabled(false);
}

/**
 * Creates the window menu.
 *
 * @param frame the window that will contain the menu
 */
protected void makeMenu(JFrame frame) {
    JMenuBar menuBar = new JMenuBar();
    frame.setJMenuBar(menuBar);

    // File menu
    JMenu menu = MenuUtils.readMenu("SwingLE.menu_file");
    menuBar.add(menu);

    MenuUtils.addToMenu(menu, printAction, "SwingLE.action_print");
    menu.addSeparator();
    MenuUtils.addToMenu(menu, closeAction, "SwingLE.action_close");

    // View menu
    menu = MenuUtils.readMenu("SwingLE.menu_view");
    menuBar.add(menu);

    MenuUtils.addToMenu(menu, goFirstAction, "SwingLE.action_first_page");
    MenuUtils.addToMenu(menu, goPrevAction, "SwingLE.action_previous_page");
    MenuUtils.addToMenu(menu, goNextAction, "SwingLE.action_next_page");
    MenuUtils.addToMenu(menu, goLastAction, "SwingLE.action_last_page");

    // Report menu
    menu = MenuUtils.readMenu("SwingLE.menu_report");
    menuBar.add(menu);

    MenuUtils.addToMenu(menu, exportAction, "SwingLE.action_export");
}

/**
 * Creates and returns a new tool bar.
 */
protected JToolBar makeToolbar() {
    JToolBar bar = new JToolBar(javax.swing.SwingConstants.HORIZONTAL);
    bar.add(printAction);
    bar.addSeparator();
    bar.add(exportAction);
    bar.addSeparator();
    bar.add(goFirstAction);
    bar.add(goPrevAction);
    bar.add(goNextAction);
    bar.add(goLastAction);
    pageCountLabel = new JLabel(I18N.get("SwingLE.loading_first_page"));
    bar.addSeparator();
    bar.add(pageCountLabel);
    return bar;
}

/**
 * Creates a new page.
 */
protected void doStartPage() {
    int pageNum = pageContents.size() + 1;
    pageBeingBuilt = new SwingPageContents(cardPanel, pageNum, pageDim);
    pageContents.add(pageBeingBuilt);
}

/**
 * At the end of the first page, starts building it in a separate thread.
 * Check to see if the first page is done being built. If so, it is
 * displayed.
 */
protected void doEndPage() {
    int numPages = pageContents.size();
    if (numPages == 1) {	// The first page
	// Start building the page in a separate thread
	pageBeingBuilt.prebuildPage();
	pageCountLabel.setText(I18N.get("SwingLE.building_first_page"));
    }

    // If the first page is done being built and is not yet displayed,
    // display it.
    if (((SwingPageContents)pageContents.get(0)).isPageBuilt()
	&& displayPageNum == 0)
	displayPage(1);		// Updates page count label and nav actions
    else {
	updatePageCountLabel();
	updateNavActions();
    }
}

/**
 * Closes this window. Does not call <code>System.exit</code>.
 */
public void close() {
    if (frame != null) {
	frame.setVisible(false);
	frame.dispose();
    }
    wantsMoreData = false;	// Signal report that we're done

    // Clean up memory a bit
    pageContents = null;
    pageBeingBuilt = null;
}

/**
 * Opens the report export dialog.
 */
protected void export() {
    new ExportWin(frame, report);
}

/**
 * Updates the navigation buttons based on number of pages and the current
 * display page.
 */
protected void updateNavActions() {
    int numPages = pageContents.size();

    boolean canGoBack = numPages > 0 && displayPageNum > 1;
    goFirstAction.setEnabled(canGoBack);
    goPrevAction.setEnabled(canGoBack);

    boolean canGoForward = numPages > 0 && displayPageNum < numPages;
    goNextAction.setEnabled(canGoForward);
    goLastAction.setEnabled(canGoForward);
}

/**
 * Updates the page count label based on the current page number and the
 * total number of pages.
 */
protected void updatePageCountLabel() {
    if (displayPageNum > 0)
	pageCountLabel.setText(I18N.get("SwingLE.page")
			       + ' ' + displayPageNum + ' '
			       + I18N.get("SwingLE.of")
			       + ' ' + pageContents.size());
}

/** Performs the "First Page" command. */
protected void displayFirstPage() {
    if (pageContents.size() > 0)
	displayPage(1);
}

/** Performs the "Next Page" command. */
protected void displayNextPage() {
    if (pageContents.size() > displayPageNum)
	displayPage(displayPageNum + 1);
}

/** Performs the "Previous Page" command. */
protected void displayPrevPage() {
    if (displayPageNum > 1)
	displayPage(displayPageNum - 1);
}

/** Performs the "Last Page" command. */
protected void displayLastPage() {
    int numPages = pageContents.size();
    if (numPages > 0)
	displayPage(numPages);
}

/**
 * Fills the window with the contents of the specified page. If necessary,
 * builds the page. Starts building the next and previous pages in separate
 * threads, if they have not already been built.
 *
 * @param num page number, starting at 1
 * @see SwingPageContents
 */
protected void displayPage(int num) {
    if (num == displayPageNum)
	return;

    // Retrieve page. Build if necessary. Show it.
    SwingPageContents contents = (SwingPageContents)pageContents.get(num - 1);
    if (!contents.isPageBuilt())
	pageCountLabel.setText(I18N.get("SwingLE.building_page")
			       + ' ' + num + "...");

    int vertPosition = scroller.getVerticalScrollBar().getValue();
    contents.showPage();	// Builds page if necessary
    scroller.getVerticalScrollBar().setValue(vertPosition);

    // If next page not already built, start building it in a separate
    // thread.
    int numPages = pageContents.size();
    if (numPages > num) { // Spawn thread to generate *next* page
	contents = (SwingPageContents)pageContents.get(num);
	contents.prebuildPage();
    }

    // If previous page not already built, start building it in a separate
    // thread.
//     if (num > 2) {		// We know pages 1 and 2 are already prebuilt
    if (num > 1) {
	contents = (SwingPageContents)pageContents.get(num - 2);
	contents.prebuildPage();
    }

    displayPageNum = num;
    updatePageCountLabel();
    updateNavActions();

    // Erase all other pages but the first and last pages and the two
    // surrounding pages we just started generating; they are probably the
    // most-visited.
    for (int i = 1; i < (numPages - 1); ++i) {
	if (i < (displayPageNum - 2) || i > (displayPageNum)) {
	    contents = (SwingPageContents)pageContents.get(i);
	    contents.forgetPage();
	}
    }
}

/**
 * Creates a new {@link SwingField} and adds it to the current page.
 *
 * @param field the report field
 */
protected void doOutputField(Field field) {
    if (!field.isVisible())
	return;

    String fieldAsString = field.toString();
    if (fieldAsString == null || fieldAsString.length() == 0) {
	// FIX: implement makeBorders
// 	makeBorders(field);
	return;
    }

    jimm.datavision.field.Rectangle bounds = field.getBounds();

    // Page footers are anchored to page bottom.
    int y = (currentSection.getArea().getArea() == SectionArea.PAGE_FOOTER)
	? (int)(pageHeight() - currentSection.getOutputHeight() + bounds.y)
	: (int)(pageHeightUsed + bounds.y);
    java.awt.Rectangle fieldBounds =
	new java.awt.Rectangle((int)bounds.x, y, (int)bounds.width,
			       (int)field.getOutputHeight());
    pageBeingBuilt.add(field, fieldAsString, fieldBounds);

    // FIX: implement makeBorders
//     makeBorders(field);
}

protected void doOutputImage(ImageField image) {
    doOutputField(image);
}

/**
 * Creates a new line. <em>Unimplemented</em>.
 *
 * @param line a line
 */
protected void doOutputLine(Line line) {}

/**
 * Prints the report.
 */
public void printReport() {
    final PrinterJob printJob = PrinterJob.getPrinterJob();
    PageFormat format = report.getPaperFormat().getPageFormat();
    SwingPrintBook book = new SwingPrintBook(pageContents, format);
    printJob.setPageable(book);

    if (printJob.printDialog()) {
	final StatusDialog statusDialog =
	    new StatusDialog(frame,
			     I18N.get("SwingLE.print_report_title"),
			     true,
			     I18N.get("SwingLE.print_report_status"));
	book.setStatusDialog(statusDialog);

	new Thread(new Runnable() {
	    public void run() {
		try {
		    printJob.print();
		}
		catch (UserCancellationException uce) {
		    printJob.cancel();
		}
		catch (Exception e) {
		    ErrorHandler.error(e);
		}
		finally {
		    if (statusDialog != null)
			statusDialog.dispose();
		}
	    }
	    }).start();
    }
}

}
