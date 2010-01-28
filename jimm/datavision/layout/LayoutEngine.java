package jimm.datavision.layout;
import jimm.datavision.*;
import jimm.datavision.field.Field;
import jimm.datavision.field.ImageField;
import java.io.PrintWriter;
import java.util.*;

/**
 * A layout engine is responsible for formatting and outputting report data.
 * <code>LayoutEngine</code> is an abstract class. The Template design
 * pattern is heavily used to provide a framework for concrete layout
 * engine subclasses.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public abstract class LayoutEngine {

/** The number of points per inch. */
public static final int POINTS_PER_INCH = 72;

protected static final int SECT_REPORT_HEADER = 0;
protected static final int SECT_REPORT_FOOTER = 1;
protected static final int SECT_PAGE_HEADER = 2;
protected static final int SECT_PAGE_FOOTER = 3;
protected static final int SECT_GROUP_HEADER = 4;
protected static final int SECT_GROUP_FOOTER = 5;
protected static final int SECT_DETAIL = 6;

/** Set by report in <code>Report.setLayoutEngine</code>. */
protected Report report;
protected int pageNumber;
protected PrintWriter out;
protected boolean newPage;
protected double pageHeight;
protected double pageWidth;
protected double pageHeightUsed;
protected Section currentSection;
protected boolean wantsMoreData;
protected int previousSectionArea;

/**
 * Constructor.
 */
public LayoutEngine() {
    this(null);
}

/**
 * Constructor.
 *
 * @param out output print writer
 */
public LayoutEngine(PrintWriter out) {
    report = null;
    this.out = out;
    wantsMoreData = true;
    previousSectionArea = -1;
}

public void setReport(Report r) { report = r; }

/**
 * Returns the page width in points. This value is only valid after
 * {@link #start} has been called.
 *
 * @return the page width in points
 */
public double pageWidth() { return pageWidth; }

/**
 * Returns the page height in points. This value is only valid after
 * {@link #start} has been called.
 *
 * @return the page height in points
 */
public double pageHeight() { return pageHeight; }

/**
 * Called by a report before retrieving each row of data, this method returns
 * <code>true</code> if this layout engine wants more data. Concrete
 * subclasses can set the <code>wantsMoreData</code> instance variable
 * to <code>false</code> if the user cancells a report run, for example.
 *
 * @return <code>true</code> if this layout engine would like more data
 */
public boolean wantsMoreData() { return wantsMoreData; }

/**
 * Called by someone else running the report to cancel all the hard work
 * this layout engine has performed.
 */
public void cancel() {
    closeOutput();
}

/**
 * Called by the report at the beginning of a report run.
 * <p>
 * By this time, <code>report</code> will be non-<code>null</code>
 * because we have been added to a report (which in turn sets our
 * <code>report</code> instance variable).
 */
public void start() {
    newPage = true;
    pageHeightUsed = 0;
    pageNumber = 0;
    pageHeight = report.getPaperFormat().getHeight();
    pageWidth = report.getPaperFormat().getWidth();
    if (wantsMoreData) {
	doStart();
	startPage();
    }
}

/**
 * Called by <code>start</code> as a chance to insert behavior when the
 * report starts.
 */
protected void doStart() { }

/**
 * Called by the report at the end of a report run.
 */
public void end() {
    if (wantsMoreData) {
	for (Iterator iter = report.footers().iterator(); iter.hasNext(); )
	    outputSection((Section)iter.next(), SECT_REPORT_FOOTER);
	endPage(true);
	doEnd();
    }
    closeOutput();
}

/**
 * Called by <code>end</code> as a chance to insert behavior when the
 * report ends.
 */
protected void doEnd() {}


/**
 * Called by <code>end</code> to let this layout engine clean up a bit.
 */
protected void closeOutput() {
    if (out != null) {
	out.flush();
	out.close();
	out = null;
    }
}

/**
 * Called by the report when group headers need to be output. Once one
 * group header is output, we output all remaining group headers.
 * <p>
 * We need to explicitly evaluate all formulas in the headers that will be
 * output because {@link #checkRemainingPageLength} causes formulas in
 * detail and footers to be evaluated. Those formulas may depend upon
 * values in these headers.
 *
 * @param isLastRow if <code>true</code>, this is the last row of the report
 */
public void groupHeaders(boolean isLastRow) {
    if (!wantsMoreData)
	return;

    boolean headerWasOutput = false;
    for (Iterator iter = report.groups(); iter.hasNext(); ) {
	Group g = (Group)iter.next();
	if (headerWasOutput || g.isNewValue()) {
	    for (Iterator i2 = g.headers().iterator(); i2.hasNext(); )
		((Section)i2.next()).evaluateFormulas();
	    headerWasOutput = true;
	}
    }
//     boolean headerWasOutput;

    checkRemainingPageLength(isLastRow, true);

    headerWasOutput = false;
    for (Iterator iter = report.groups(); iter.hasNext(); ) {
	Group g = (Group)iter.next();
	if (headerWasOutput || g.isNewValue()) {
	    for (Iterator i2 = g.headers().iterator(); i2.hasNext(); )
		outputSection((Section)i2.next(), SECT_GROUP_HEADER);
	    headerWasOutput = true;
	}
    }
}

/**
 * Called by the report when a single detail row needs to be output.
 *
 * @param isLastRow if <code>true</code>, this is the last row of the report
 */
public void detail(boolean isLastRow) {
    if (!wantsMoreData)
	return;

    checkRemainingPageLength(isLastRow, true);
    for (Iterator iter = report.details().iterator(); iter.hasNext(); )
	outputSection((Section)iter.next(), SECT_DETAIL);
}

/**
 * Called by the report when group footers need to be output. When one
 * group footer is output, we make sure all of the footers before (above)
 * it are also output.
 *
 * @param isLastRow if <code>true</code>, this is the last row of the report
 */
public void groupFooters(boolean isLastRow) {
    if (!wantsMoreData)
	return;

    checkRemainingPageLength(isLastRow, false);

    // We need to output group footers backwards (Group n ... Group 1).
    // When a group footer is output, make sure all of the footers before
    // (above) it are also output. First, we walk the group list forwards
    // so we can see which group changed first and grab all groups
    // after that one.
    boolean footerWasOutput = false;
    ArrayList groupsToOutput = new ArrayList();
    for (Iterator iter = report.groups(); iter.hasNext(); ) {
	Group g = (Group)iter.next();
	if (footerWasOutput || g.isNewValue() || isLastRow) {
	    if (footerWasOutput) g.forceFooterOutput();
	    groupsToOutput.add(g);
	    footerWasOutput = true;
	}
    }

    // Now we reverse the list of groups and output them.
    Collections.reverse(groupsToOutput);
    for (Iterator iter = groupsToOutput.iterator(); iter.hasNext(); ) {
	Group g = (Group)iter.next();
	for (Iterator i2 = g.footers().iterator(); i2.hasNext(); )
	    outputSection((Section)i2.next(), SECT_GROUP_FOOTER);
    }
}

/**
 * Checks remaining page length and outputs a new page if we are at the
 * bottom of the page.
 *
 * @param isLastRow if <code>true</code>, this is the last row of the report
 * @param includeDetail if <code>true</code>, include height of detail
 * sections
 */
protected void checkRemainingPageLength(boolean isLastRow,
					boolean includeDetail)
{
    // NOTE: need to do this dynamically each row because each one
    // of the multiple detail sections may or may not be active for
    // this row.
    double detailHeight = includeDetail ? calcDetailHeight() : 0;
    double footerHeight = calcPageFooterHeight();
    if (isLastRow) footerHeight += calcReportFooterHeight();

    if ((pageHeightUsed + footerHeight + detailHeight) > pageHeight())
	endPage(isLastRow);

    if (newPage) startPage();
}

/**
 * Returns the current page number.
 *
 * @return the current page number
 */
public int pageNumber() { return pageNumber; }

/**
 * Starts a new page.
 */
protected void startPage() {
    if (!wantsMoreData)
	return;

    pageNumber += 1;
    pageHeightUsed = 0;
    newPage = false;

    doStartPage();
    if (pageNumber == 1) {
	for (Iterator iter = report.headers().iterator(); iter.hasNext(); )
	    outputSection((Section)iter.next(), SECT_REPORT_HEADER);
    }
    for (Iterator iter = report.pageHeaders().iterator(); iter.hasNext(); )
	outputSection((Section)iter.next(), SECT_PAGE_HEADER);
}

/**
 * Called by <code>startPage</code> as a chance to insert behavior when a
 * new page starts.
 */
protected void doStartPage() { }

/**
 * Ends a new page.
 *
 * @param isLastPage if <code>true</code>, this is the last page of the report
 */
protected void endPage(boolean isLastPage) {
    if (!wantsMoreData)
	return;

    for (Iterator iter = report.pageFooters().iterator(); iter.hasNext(); )
	outputSection((Section)iter.next(), SECT_PAGE_FOOTER);

    newPage = true;
    doEndPage();
}

/**
 * Called by <code>endPage</code> as a chance to insert behavior when a
 * new page ends.
 */
protected void doEndPage() { }

/**
 * Outputs a section.
 *
 * @param sect the section to output
 * @param which the type of section (for example,
 * <code>SECT_PAGE_FOOTER</code>)
 */
protected void outputSection(Section sect, int which) {
    if (!wantsMoreData)
	return;

    if (sect.isVisibleForCurrentRow()) {
	// Insert a page break if requested.
	if (sect.hasPageBreak()
	    && previousSectionArea != SECT_PAGE_HEADER)
	{
	    endPage(false);
	    startPage();
	}

	// This must be after page break because calling endPage() and
	// startPage() changes the currentSection.
	currentSection = sect;
  // Insert a page break if section will get overwritten by the page footer.
  if (sect.getArea().getArea() != SECT_PAGE_FOOTER &&
    pageHeight - pageHeightUsed - calcPageFooterHeight() <
    currentSection.getOutputHeight()) {
		endPage(false);
		startPage();
		currentSection = sect;
	}
	report.evaluateFormulasIn(currentSection);
	doOutputSection(currentSection);

	pageHeightUsed += sect.getOutputHeight();
	previousSectionArea = which;
    }
    else {
	// Always eval formulas, even if the section is hidden
	report.evaluateFormulasIn(sect);
    }
}

/**
 * Called by <code>outputSection</code> as a chance to insert behavior
 * when a section is output.
 *
 * @param sect a section
 */
protected void doOutputSection(Section sect) {
    // Output the fields in the section
    for (Iterator iter = sect.fields(); iter.hasNext(); ) {
	Field f = (Field)iter.next();
	if (f.isVisible()) {
	    if (f instanceof ImageField)
		outputImage((ImageField)f);
	    else
		outputField(f);
	}
    }
    // Output the lines
    for (Iterator iter = sect.lines(); iter.hasNext(); ) {
	Line l = (Line)iter.next();
	if (l.isVisible()) outputLine(l);
    }
}

/**
 * Outputs a field.
 *
 * @param field the field to output
 */
protected void outputField(Field field) {
    if (wantsMoreData)		// Do nothing if we have cancelled
	doOutputField(field);
}

/**
 * Called by <code>outputField</code> as a chance to insert behavior
 * when a field is output.
 *
 * @param field a field
 */
protected abstract void doOutputField(Field field);

/**
 * Outputs a image.
 *
 * @param image the image field to output
 */
protected void outputImage(ImageField image) {
    if (wantsMoreData)		// Do nothing if we have cancelled
	doOutputImage(image);
}

/**
 * Called by <code>outputImage</code> as a chance to insert behavior
 * when a image is output.
 *
 * @param image an image field
 */
protected abstract void doOutputImage(ImageField image);

/**
 * Outputs a line.
 *
 * @param line the line to output
 */
protected void outputLine(Line line) {
    if (wantsMoreData)
	doOutputLine(line);
}

/**
 * Called by <code>outputLine</code> as a chance to insert behavior
 * when a line is output.
 *
 * @param line a line
 */
protected abstract void doOutputLine(Line line);

/**
 * Returns the current section type (header, footer, detail) as a string.
 *
 * @return a string representation of the current section type
 */
protected String currentSectionTypeAsString() {
    switch (currentSection.getArea().getArea()) {
    case SectionArea.REPORT_HEADER: return "report header";
    case SectionArea.REPORT_FOOTER: return "report footer";
    case SectionArea.PAGE_HEADER: return "page header";
    case SectionArea.PAGE_FOOTER: return "page footer";
    case SectionArea.GROUP_HEADER: return "group header";
    case SectionArea.GROUP_FOOTER: return "group footer";
    case SectionArea.DETAIL: return "detail";
    default: return "unknown";	// Should never happen
    }
}

/**
 * Returns the total height of all sections in the specified list.
 *
 * @param area a section area
 * @return the total height of all sections in the list
 */
protected double calcSectionHeights(SectionArea area) {
    double sum = 0;
    for (Iterator iter = area.iterator(); iter.hasNext(); ) {
	Section s = (Section)iter.next();
	if (s.isVisibleForCurrentRow())
	    sum += s.getOutputHeight();
    }
    return sum;
}

/**
 * Returns the total height of all detail sections.
 *
 * @return the total height of all detail sections
 */
protected double calcDetailHeight() {
    return calcSectionHeights(report.details());
}

/**
 * Returns the total height of all page footer sections.
 *
 * @return the total height of all page footer sections
 */
protected double calcPageFooterHeight() {
    return calcSectionHeights(report.pageFooters());
}

/**
 * Returns the total height of all report footer sections.
 *
 * @return the total height of all report footer sections
 */
protected double calcReportFooterHeight() {
    return calcSectionHeights(report.footers());
}

}
