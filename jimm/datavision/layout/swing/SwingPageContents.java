package jimm.datavision.layout.swing;
import jimm.datavision.ErrorHandler;
import jimm.datavision.field.Field;
import jimm.datavision.field.ImageField;
import java.awt.Dimension;
import java.awt.CardLayout;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Iterator;

// This file also contains the SwingPageField class.

/**
 * Holds report page contents and creates {@link SwingPage} instances
 * when requested.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see SwingLE
 * @see SwingPageField
 */
class SwingPageContents {

ArrayList pageFields;
SwingPage page;
int pageNumber;
JPanel parent;
Dimension pageDim;
Thread buildThread;

/**
 * Constructor.
 *
 * @param parent the panel in which this page will be displayed; its
 * layout must be a <code>CardLayout</code>
 * @param pageNumber the page number, starting at 1
 * @param dim the page dimensions
 */
SwingPageContents(JPanel parent, int pageNumber, Dimension dim) {
    pageFields = new ArrayList();
    this.pageNumber = pageNumber;
    this.parent = parent;
    pageDim = dim;
}

/**
 * Adds a new field, its display value, and its position on the page.
 *
 * @param f a report field
 * @param v the field's display value (retrieved from the database or
 * otherwise calculated)
 * @param r the display position and size
 */
void add(Field f, String v, java.awt.Rectangle r) {
    pageFields.add(new SwingPageField(f, v, r));
}

/**
 * Returns <code>true</code> if the swing page has been built.
 */
boolean isPageBuilt() { return page != null; }

/**
 * Returns the swing page. If the page is being built, wait for the page
 * building to complete before returning the page. If page construction
 * has not started, build the page and return it.
 *
 * @return a <code>SwingPage</code>
 */
SwingPage getPage() {
    if (page == null) {
	if (buildThread != null) { // Wait for page build to finish
	    try {
		// One more check because the thread may have finished
		if (buildThread != null) {
		    synchronized(buildThread) {
			buildThread.join();
		    }
		}
	    }
	    catch (InterruptedException e) {
		ErrorHandler.error(e);
		// page may be null
	    }
	}
	else
	    buildPage();
    }
    return page;
}

/**
 * Displays the swing page, building it first if necessary.
 */
void showPage() {
    getPage();			// Make sure page completely built
    CardLayout cardLayout = (CardLayout)parent.getLayout();
    cardLayout.show(parent, "page " + pageNumber);
}

/**
 * Builds the swing page and adds it to the parent, all in a separate
 * thread. Calls {@link #buildPage}.
 * <p>
 * This isn't entirely thread safe, but it's close enough for our purposes.
 */
void prebuildPage() {
    if (page != null || buildThread != null)
	return;

    buildThread = new Thread(new Runnable() {
	public void run() {
	    buildPage();
	    buildThread = null;
	}
	});
    buildThread.start();
}

/**
 * Builds the swing page and adds it to the parent.
 */
void buildPage() {
    SwingPage newPage = new SwingPage();
    newPage.setPreferredSize(pageDim);

    for (Iterator iter = pageFields.iterator(); iter.hasNext();) {
	SwingPageField spf = (SwingPageField)iter.next();

	// Create field and add to page.
	SwingField sf;
	if (spf.field instanceof ImageField)
	    sf = new SwingImageField((ImageField)spf.field);
	else
	    sf = new SwingTextField(spf.field, spf.value);

	sf.getComponent().setBounds(spf.rect);
	newPage.add(sf.getComponent());
    }

    // Add page to parent
    parent.add(newPage, "page " + pageNumber);

    page = newPage;
}

/**
 * Forgets the page we have built, removes it from its parent, and restores
 * the field information so we can build the page later.
 */
void forgetPage() {
    if (!isPageBuilt())
	return;

    CardLayout cardLayout = (CardLayout)parent.getLayout();
    cardLayout.removeLayoutComponent(page);
    parent.remove(page);

    page = null;
}
}

/**
 * Holds a report field, its value when this instance is created, and
 * a rectangle for page placement.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 * @see SwingPage
 * @see SwingLE
 * @see SwingPageContents
 */
class SwingPageField {

Field field;
String value;
java.awt.Rectangle rect;

/**
 * Constructor.
 *
 * @param f a report field
 * @param v the field's display value (retrieved from the database or
 * otherwise calculated)
 * @param r the display position and size
 */
SwingPageField(Field f, String v, java.awt.Rectangle r) {
    field = f;
    value = v;
    rect = r;
}
}
