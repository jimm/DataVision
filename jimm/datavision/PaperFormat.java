package jimm.datavision;
import jimm.util.XMLWriter;
import java.awt.print.Paper;
import java.awt.print.PageFormat;
import java.util.*;

/**
 * The class manages lists of paper sizes and instances represents specific
 * paper sizes and orientations. Instances are immutable.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class PaperFormat extends Paper implements Nameable, Writeable {

public static final int PORTRAIT = 0;
public static final int LANDSCAPE = 1;

protected static final String RESOURCE_FILE_PREFIX = "paper";

protected static HashMap[] orientations;
protected static TreeSet names;
protected static PaperFormat defaultPaper;

protected String name;		// Immutable
protected int orientation;	// PORTRAIT or LANDSCAPE; immutable
protected PageFormat pageFormat;
protected String latexPaperSizeString;

static {
    orientations = new HashMap[2];
    orientations[PORTRAIT] = new HashMap();
    orientations[LANDSCAPE] = new HashMap();
    ArrayList nameList = new ArrayList();

    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_FILE_PREFIX,
						     Locale.getDefault());
    try {
	int numPaperSizes = Integer.parseInt(bundle.getString("num_paper_sizes"));
	for (int i = 0; i < numPaperSizes; ++i) {
	    String vals = bundle.getString("paper" + i);
	    int pos1 = vals.indexOf(',');
	    int pos2 = vals.indexOf(',', pos1 + 1);
	    int pos3 = vals.indexOf(',', pos2 + 1);
	    int pos4 = vals.indexOf(',', pos3 + 1);
	    int pos5 = vals.indexOf(',', pos4 + 1);
	    if (pos1 == -1 || pos2 == -1 || pos3 == -1 || pos4 == -1)
		continue;	// pos5 is optional

	    double w = Double.parseDouble(vals.substring(pos1+1, pos2));
	    double h = Double.parseDouble(vals.substring(pos2+1, pos3));
	    double hMargin = Double.parseDouble(vals.substring(pos3+1, pos4));
	    double vMargin = 0;
	    String latexPaperSizeString = null;
	    if (pos5 == -1)
		vMargin = Double.parseDouble(vals.substring(pos4+1));
	    else {
		vMargin = Double.parseDouble(vals.substring(pos4+1, pos5));
		latexPaperSizeString = vals.substring(pos5+1);
	    }

	    String name = vals.substring(0, pos1);
	    nameList.add(name);
	    PaperFormat p = addPaper(PORTRAIT, name, w, h, hMargin, vMargin,
				     latexPaperSizeString);
	    addPaper(LANDSCAPE, name, h, w, vMargin, hMargin,
		     latexPaperSizeString);
	    if (i == 0)		// Zero'th one is the default
		defaultPaper = p;
	}
    }
    catch (Exception e) {	// Number format exceptions, mostly
	ErrorHandler.error(e);
    }
    finally {
	// Copy list of names to a sorted set
	names = new TreeSet(nameList);
    }
}

private static PaperFormat addPaper(int orientation, String name,
				    double w, double h,
				    double hMargin, double vMargin,
				    String latexPaperSizeString)
{
    PaperFormat p = new PaperFormat(orientation, name, w, h, hMargin,
				    vMargin, latexPaperSizeString);
    orientations[orientation].put(name, p);
    return p;
}


public static PaperFormat get(int orientation, String name) {
    return (PaperFormat)orientations[orientation].get(name);
}

public static PaperFormat getDefault() {
    return defaultPaper;
}

public static Iterator names() {
    return names.iterator();
}

PaperFormat(int orientation, String name, double w, double h, double hMargin,
	    double vMargin, String latexPaperSizeString)
{
    this.orientation = orientation;
    this.name = name;
    this.latexPaperSizeString = latexPaperSizeString;
    setSize(w, h);
    setImageableArea(hMargin, vMargin, w - hMargin / 2.0, h - vMargin / 2.0);
}

public int getOrientation() { return orientation; }

public String getName() { return name; }

/** A paper format's name is immutable. */
public void setName(String name) { }

public String getLaTeXPaperSizeString() { return latexPaperSizeString; }

/**
 * Returns a <code>java.awt.print.PageFormat</code> that describes
 * our orientation, size, and margins. Used by print jobs.
 *
 * @return a page format
 * @see jimm.datavision.layout.swing.SwingLE#printReport
 */
public PageFormat getPageFormat() {
    if (pageFormat == null) {
	pageFormat = new PageFormat();
	if (orientation == PORTRAIT) {
	    pageFormat.setOrientation(PageFormat.PORTRAIT);
	    pageFormat.setPaper(this);
	}
	else {
	    pageFormat.setOrientation(PageFormat.LANDSCAPE);
	    // Apparently the paper object given to the page format needs
	    // to have the x, w, width, and height values be the same as
	    // the portrait values, not rotated.
	    pageFormat.setPaper(get(PORTRAIT, getName()));
	}
    }
    return pageFormat;
}

public void writeXML(XMLWriter out) {
    out.startElement("paper");
    out.attr("name", name);
    out.attr("orientation",
	     orientation == PORTRAIT ? "portrait" : "landscape");
    out.endElement();
}

}
