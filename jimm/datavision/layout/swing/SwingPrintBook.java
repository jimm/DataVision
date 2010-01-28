package jimm.datavision.layout.swing;
import jimm.datavision.UserCancellationException;
import jimm.datavision.gui.StatusDialog;
import jimm.util.I18N;
import java.awt.print.*;
import java.util.List;

/**
 * Returns printable pages to a print job.
 */
class SwingPrintBook implements Pageable {

protected StatusDialog statusDialog;
protected List pageContents;
protected PageFormat pageFormat;
protected boolean wasBuiltForUs;

SwingPrintBook(List pages, PageFormat format) {
    pageContents = pages;
    pageFormat = format;
}

void setStatusDialog(StatusDialog statusDialog) {
    this.statusDialog = statusDialog;
}

public int getNumberOfPages() {
    return pageContents.size();
}

public PageFormat getPageFormat(int pageIndex) {
    return pageFormat;
}

/**
 * Returns the specified swing page. If the page has already been constructed
 * for display, we return that page. If it has not, we create and return
 * a new, temporary page.
 */
public Printable getPrintable(int pageIndex) {
    SwingPageContents contents;

    // "Forget" previous page if it was built 'specially for this print job.
    if (wasBuiltForUs) {
	contents = (SwingPageContents)pageContents.get(pageIndex - 1);
	contents.forgetPage();
    }

    // If user cancelled, tell the rest of the world.
    if (statusDialog.isCancelled()) {
	throw new UserCancellationException();
    }
    statusDialog.update(I18N.get("SwingPrintBook.printing_page")
			+ ' ' + (pageIndex + 1) + ' '
			+ I18N.get("SwingPrintBook.of") + ' '
			+ getNumberOfPages());

    contents = (SwingPageContents)pageContents.get(pageIndex);
    Printable page = null;

    page = contents.getPage();
    wasBuiltForUs = !contents.isPageBuilt();

    return page;
}

}
